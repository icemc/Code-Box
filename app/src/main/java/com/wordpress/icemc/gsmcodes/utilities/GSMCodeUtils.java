package com.wordpress.icemc.gsmcodes.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.exceptions.UnmatchedCodeInputFieldsAndSentinelsException;
import com.wordpress.icemc.gsmcodes.model.InputField;
import com.wordpress.icemc.gsmcodes.model.SupportedOperators;

import java.util.List;

public class GSMCodeUtils {
    private  GSMCodeUtils() {
    }

    public static String setCodeStringUsingInputFields(final String code, InputField[] inputFields) throws
            UnmatchedCodeInputFieldsAndSentinelsException {

        String[] fields  = code.split(ApplicationConstants.NUMBER_SENTINEL);
        if (inputFields != null) {
            if(!code.contains(ApplicationConstants.NUMBER_SENTINEL)) {
                //Normally if inputFields != null there there should be at least one occurrence of NUMBER_SENTINEL
                return code;
            }

            if((fields.length - 1) != inputFields.length) {
                Log.e(GSMCodeUtils.class.getSimpleName(), "Number of input fields dont match number of Sentinel values");
                throw new UnmatchedCodeInputFieldsAndSentinelsException();
            }

            String result = fields[0];
            for(int i = 0; i < inputFields.length; i++) {
                result = result + inputFields[i].getTitle() + fields[i+1];
            }

            return result;
        }

        return code;
    }

    public static String setCodeStringUsingEditTexts(final String code, List<EditText> texts) throws
            UnmatchedCodeInputFieldsAndSentinelsException {

        String[] fields  = code.split(ApplicationConstants.NUMBER_SENTINEL);
        if (texts != null) {
            if(!code.contains(ApplicationConstants.NUMBER_SENTINEL)) {
                //Normally if inputFields != null there there should be at least one occurrence of NUMBER_SENTINEL
                return code;
            }

            if((fields.length - 1) != texts.size()) {
                Log.e(GSMCodeUtils.class.getSimpleName(), "Number of input fields dont match number of Sentinel values: Total fields: " + fields.length + " Total sentinels: " + texts.size());
                throw  new UnmatchedCodeInputFieldsAndSentinelsException();
            }

            String result = fields[0];
            for(int i = 0; i < texts.size(); i++) {
                result = result + texts.get(i).getText() + fields[i+1];
            }

            return result;
        }

        return code;
    }

    public static int getLogoFromOperatorName(String operatorName) {

        switch (SupportedOperators.fromString(operatorName)) {
            case CAMTEL:
                return R.drawable.logo_camtel;
            case MTN_CAMEROON:
                return R.drawable.logo_mtn;

            case ORANGE_CAMEROON:
                return R.drawable.logo_orange;
            case NEXTTEL:
                return R.drawable.logo_nexttel;
            default:
                return R.drawable.logo_nexttel;
        }
    }


    /**
     * Converting dp to pixel
     */
    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static Uri ussdToCallableUri(String ussd) {
        String uriString = "";
        String hash = Uri.encode("#");

        if (!ussd.startsWith("tel:")) {
            uriString += "tel:";
        }

        for (char c : ussd.toCharArray()) {
            if (c == '#') {
                uriString += hash;
            } else if(c == '+') {
                continue;
            } else {
                uriString += c;
            }
        }

        return Uri.parse(uriString);
    }
}
