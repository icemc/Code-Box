package com.wordpress.icemc.gsmcodes.utilities;


import android.content.Context;
import android.util.Log;

import com.wordpress.icemc.gsmcodes.dao.CodeDao;
import com.wordpress.icemc.gsmcodes.dao.OperatorsDao;
import com.wordpress.icemc.gsmcodes.dao.TagMapDao;
import com.wordpress.icemc.gsmcodes.dao.TagsDao;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.CodeWrapper;
import com.wordpress.icemc.gsmcodes.model.InputField;
import com.wordpress.icemc.gsmcodes.model.InputType;
import com.wordpress.icemc.gsmcodes.model.Operator;
import com.wordpress.icemc.gsmcodes.model.Tag;
import com.wordpress.icemc.gsmcodes.model.TagMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public  class JsonUtils {


    //These fields are used by more than one JSON object
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String OPERATOR = "Operator";

    //JSON field names for codes
    public static final String CODE_CODE = "Code";
    public static final String CODE_INPUT_FIELDS = "InputFields";
    public static final String CODE_TAGS = "Tags";
    public static final String INPUT_TYPE_NAME = "InputType";
    public static final String TITLE_NAME = "Title";

    //JSON field names for operators
    public static final String OPERATOR_COLOR = "Color";
    public static final String OPERATOR_LOGO = "Logo";
    private static final String TAG = JsonUtils.class.getSimpleName();
    private static final String OPERATOR_PHONE_NUMBER = "Tel";

    //JSON field names for Tags
        //No specific field for tags

    //This class can not be initialized
    private JsonUtils() {
    }

//    public static String convertInputFieldToJSONString(InputField inputField) {
//        if(inputField != null){
//            return convertInputFieldIntoJSONObject(inputField).toString();
//        }
//        return null;
//    }

    private static  JSONObject convertInputFieldIntoJSONObject(InputField inputField){
        JSONObject object = null;
        if (inputField != null) {
            object = new JSONObject();
            try {
                object.put(INPUT_TYPE_NAME, inputField.getInputType().toString());
                object.put(TITLE_NAME, inputField.getTitle());

            } catch (JSONException e) {
                //TODO signal about the parsing error or return null
            }
        }
        return object;
    }

    private static InputField convertJSONObjectIntoInputField(JSONObject object) {
        InputField inputField = null;
        if(object != null) {
            inputField = new InputField();
            try {
                inputField.setInputType(InputType.fromString(object.getString(INPUT_TYPE_NAME)));
                inputField.setTitle(object.getString(TITLE_NAME));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return inputField;
    }

    public static String convertInputFieldArrayIntoJSONString(InputField[] inputFields){
        JSONArray jsonArray = new JSONArray();
        if(inputFields != null){
            for(InputField i: inputFields){
                jsonArray.put(convertInputFieldIntoJSONObject(i));
            }
            return jsonArray.toString();
        } else {
            return  " ";
        }

    }

    public static InputField[] convertJSONArrayIntoInputFieldArray(JSONArray jsonArray) {
        List<InputField> inputFields = new ArrayList<>();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    inputFields.add(convertJSONObjectIntoInputField(
                            jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return inputFields.toArray(new InputField[inputFields.size()]);
    }

    private static List<String> convertJSONArrayIntoTags(JSONArray object) {
        List<String > tags = null;
         if (object != null) {
             tags = new ArrayList<>();
             for (int i = 0; i < object.length(); i++) {
                 try {
                     String tagName = object.getString(i);
                     tags.add(tagName);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         }
        return  tags;
    }

    private static CodeWrapper convertJSONObjectIntoCodeWrapper(JSONObject object) {
        CodeWrapper codeWrapper = null;
        try {
            Code.Builder builder = new Code.Builder();
            builder.name(object.getString(NAME))
                    .description(object.getString(DESCRIPTION))
                    .code(object.getString(CODE_CODE))
                    .operator(object.getString(OPERATOR));
            try {
                builder.inputFields(convertJSONArrayIntoInputFieldArray
                        (object.getJSONArray(CODE_INPUT_FIELDS)));
            } catch (JSONException ex) {
                builder.inputFields(null);
            }

            //The favourite field is not included as a JSON field
            //So by default it's set to false
            builder.isFavourite(false);
            Code code = builder.build();
            List<String> tags = convertJSONArrayIntoTags(object.getJSONArray(CODE_TAGS));
            codeWrapper = new CodeWrapper(code, tags);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return codeWrapper;
    }

    private static Tag convertJSONObjectIntoTag(JSONObject object) {
        Tag tag = null;
        Tag.Builder builder = new Tag.Builder();
        try {
            builder.name(object.getString(NAME))
                    .operator(object.getString(OPERATOR));
            tag = builder.build();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return tag;
    }

    private static Operator convertJSONObjectIntoOperator(JSONObject object) {
        Operator operator = null;
        Operator.Builder builder = new Operator.Builder();
        try {
            String name = object.getString(NAME);
            builder.name(name)
                    .description(object.getString(DESCRIPTION))
                    .logoPath(GSMCodeUtils.getLogoFromOperatorName(name))
                    .phoneNumer(object.getString(OPERATOR_PHONE_NUMBER));
            operator = builder.build();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return operator;
    }

    private static String loadJSONFromAsset(Context context, String fileName){

        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = 1024;
            byte[] buffer = new byte[size];

            //Use ByteArrayOutputStream to get the exact number of bytes in the InputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len;
            while ((len = is.read(buffer, 0, size)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }

            buffer = byteArrayOutputStream.toByteArray();

            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    private static  List<CodeWrapper> getCodesFromJSONFile(Context context, String fileName) {
        String json = loadJSONFromAsset(context, fileName);
        JSONArray array;
        List<CodeWrapper> codes = null;
        try {
            array = new JSONObject(json).getJSONArray("Codes");
            codes = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                codes.add(convertJSONObjectIntoCodeWrapper(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return codes;
    }

    private static  List<Operator> getOperatorsFromJSONFile(Context context, String fileName) {
        String json = loadJSONFromAsset(context, fileName);
        JSONArray array;
        List<Operator> operators = null;
        try {
            array = new JSONObject(json).getJSONArray("Operators");
            operators = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                operators.add(convertJSONObjectIntoOperator(array.getJSONObject(i)));
                Log.d(TAG, "obtained operator No: " + i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return operators;
    }

    private static  List<Tag> getTagsFromJSONFile(Context context, String fileName) {
        String json = loadJSONFromAsset(context, fileName);
        JSONArray array;
        List<Tag> tags = null;
        try {
            array = new JSONObject(json).getJSONArray("Tags");
            tags = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                tags.add(convertJSONObjectIntoTag(array.getJSONObject(i)));
                Log.d(TAG, "obtained Tag No: " + i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public static void saveJSONCodesToDatabase(Context context, String fileName) {
        List<CodeWrapper> codeWrappers = getCodesFromJSONFile(context, fileName);
        if(codeWrappers != null) {
            CodeDao codeDao = new CodeDao(context);
            TagMapDao tagMapDao = new TagMapDao(context);
            for(CodeWrapper c: codeWrappers) {
                codeDao.saveCode(codeDao.getValuesFromObject(c.getCode()));
                for (String t: c.getTags()) {
                    tagMapDao.saveTagMap(tagMapDao.getValuesFromObject(
                            new TagMap.Builder()
                                    .tagId(t)
                                    .codeId(c.getCode().getCode())
                                    .build()));
                }
            }
        } else {
            //TODO report to UI using a toast msg
            Log.w(JsonUtils.class.getSimpleName(), "Failed to save codes into database");
        }
    }

    public static void saveJSONOperatorsToDatabase(Context context, String fileName) {
        List<Operator> operators = getOperatorsFromJSONFile(context, fileName);
        if(operators != null) {
            OperatorsDao dao = new OperatorsDao(context);
            for(Operator o: operators) {
                dao.saveOperator(dao.getValuesFromObject(o));
            }
        } else {
            //TODO report to UI using a toast msg
            Log.w(JsonUtils.class.getSimpleName(), "Failed to save operators into database");
        }
    }

    public static void saveJSONTagsToDatabase(Context context, String fileName) {
        List<Tag> tags = getTagsFromJSONFile(context, fileName);
        if(tags != null) {
            TagsDao dao = new TagsDao(context);
            for(Tag t: tags) {
                dao.saveTag(dao.getValuesFromObject(t));
            }
        } else {
            //TODO report to UI using a toast msg
            Log.w(JsonUtils.class.getSimpleName(), "Failed to save tags into database");
        }
    }
}
