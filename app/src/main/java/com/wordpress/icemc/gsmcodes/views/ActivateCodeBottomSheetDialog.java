package com.wordpress.icemc.gsmcodes.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.dao.TagMapDao;
import com.wordpress.icemc.gsmcodes.listeners.ContactButtonClickListener;
import com.wordpress.icemc.gsmcodes.listeners.ContactPickListener;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.InputField;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;

import java.util.ArrayList;
import java.util.List;

public class ActivateCodeBottomSheetDialog extends BottomSheetDialog implements ContactPickListener{
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private final Code code;
    private Button btn_dialog_bottom_sheet_ok, btn_dialog_bottom_sheet_cancel;
    private ImageView share, delete;
    private LinearLayout tags;

    private ContactButtonClickListener contactButtonClickListener;


    public ActivateCodeBottomSheetDialog(Context context, Code code, ContactButtonClickListener contactButtonClickListener){
        super(context, R.style.MaterialBottomSheet);
        this.code = code;
        this.contactButtonClickListener = contactButtonClickListener;

        inflateSheet();
    }

    private void inflateSheet() {

        View dialogView = getLayoutInflater().inflate(R.layout.activate_code_bottom_sheet, null);
        ImageView logo = (ImageView) dialogView.findViewById(R.id.operator_logo_small);
        delete = (ImageView) dialogView.findViewById(R.id.delete_button);
        share = (ImageView) dialogView.findViewById(R.id.share_button);
        share.getDrawable().mutate().setColorFilter(
                getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        delete.getDrawable().mutate().setColorFilter(
                getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        tags = (LinearLayout) dialogView.findViewById(R.id.categories_container);
        btn_dialog_bottom_sheet_ok = (Button) dialogView.findViewById(R.id.btn_dialog_bottom_sheet_ok);
        btn_dialog_bottom_sheet_cancel = (Button) dialogView.findViewById(R.id.btn_dialog_bottom_sheet_cancel);
        TextView name = (TextView) dialogView.findViewById(R.id.name_bottom_sheet);
        TextView description = (TextView) dialogView.findViewById(R.id.description_bottom_sheet);
        TextView formattedCode = (TextView) dialogView.findViewById(R.id.code_bottom_sheet);
        LinearLayout inputFieldsLayouts = (LinearLayout) dialogView.findViewById(
                R.id.input_fields_layout);

        Glide.with(getContext()).load(GSMCodeUtils.getLogoFromOperatorName(code.getOperator())).into(logo);

        //Input all the tags for this particular code in tags linear layout
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TagMapDao dao = new TagMapDao(getContext());
        List<String> allTags = dao.getTagsFromTagMapsCursor(dao.getTagMapCursorForCode(code));
        if(allTags.size() > 0) {
            for (String tag : allTags) {
                View v = li.inflate(R.layout.category_text_view, null);
                TextView t = (TextView) v.findViewById(R.id.category_text_view);
                t.setText(tag);
                tags.addView(v);
            }
        } else {
            View v = li.inflate(R.layout.category_text_view, null);
            TextView t = (TextView) v.findViewById(R.id.category_text_view);
            t.setText(R.string.no_categories);
            tags.addView(v);
        }

        name.setText(code.getName());
        description.setText(code.getDescription());
        formattedCode.setText("Code: " + GSMCodeUtils.setCodeStringUsingInputFields(code.getCode(), code.getInputFields()));

        setContentView(dialogView);
        //TODO inflate the inflate the input field layout here
        if (code.getInputFields() != null) {
            InputField[] inputFields = code.getInputFields();
            if (needsContactButton(inputFields)) {
                for (InputField i: inputFields) {
                    if (i.getInputType() == com.wordpress.icemc.gsmcodes.model.InputType.PHONE_NUMBER) {
                        View l = li.inflate(R.layout.code_bottom_dialog_row_wrapper_with_contact, null);
                        EditText editText = (EditText) l.findViewById(R.id.bottom_dialog_edit_text);
                        //editText.setHint(i.getTitle());
                        editText.setMaxLines(1);
                        editText.setSingleLine(true);
                        editText.setInputType(InputType.TYPE_CLASS_PHONE);
                        editTexts.add(editText);
                        TextInputLayout inputLayout = (TextInputLayout) l.findViewById(R.id.bottom_dialog_input_layout);
                        inputLayout.setHint(i.getTitle());
                        inputLayout.setHintAnimationEnabled(true);
                        final int index = editTexts.size() - 1;
                        final ImageView contactButton = (ImageView) l.findViewById(R.id.bottom_sheet_contact_button);
                        contactButton.getDrawable().mutate().setColorFilter(
                                getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);

                        contactButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                contactButtonClickListener.onContactButtonClicked(index, true);
                            }
                        });

                        inputFieldsLayouts.addView(l);
                    }
                    else {
                        View l = li.inflate(R.layout.code_bottom_dialog_row_wrapper_with_space, null);
                        EditText editText = (EditText) l.findViewById(R.id.bottom_dialog_edit_text);
                        //editText.setHint(i.getTitle());
                        editText.setMaxLines(1);
                        editText.setSingleLine(true);
                        if(i.getInputType() == com.wordpress.icemc.gsmcodes.model.InputType.PASS_CODE) {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        }
                        editTexts.add(editText);
                        TextInputLayout inputLayout = (TextInputLayout) l.findViewById(R.id.bottom_dialog_input_layout);
                        inputLayout.setHint(i.getTitle());
                        inputLayout.setHintAnimationEnabled(true);
                        inputFieldsLayouts.addView(l);
                    }

                }
            } else {
                for (InputField i : inputFields) {
                    View l = li.inflate(R.layout.code_bottom_dialog_row_wrapper, null);
                    EditText editText = (EditText) l.findViewById(R.id.bottom_dialog_edit_text);
                    //editText.setHint(i.getTitle());
                    editText.setMaxLines(1);
                    editText.setSingleLine(true);
                    if (i.getInputType() == com.wordpress.icemc.gsmcodes.model.InputType.PASS_CODE) {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    } else {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                    editTexts.add(editText);
                    TextInputLayout inputLayout = (TextInputLayout) l.findViewById(R.id.bottom_dialog_input_layout);
                    inputLayout.setHint(i.getTitle());
                    inputLayout.setHintAnimationEnabled(true);
                    inputFieldsLayouts.addView(l);

                }
            }
        }
    }

    private boolean needsContactButton(InputField[] inputFields) {
        for (InputField i: inputFields) {
            if (i.getInputType() == com.wordpress.icemc.gsmcodes.model.InputType.PHONE_NUMBER) {
                return true;
            }
        }
        return false;
    }

    public Code getCode() {
        return code;
    }

    public Button getBtn_dialog_bottom_sheet_ok() {
        return btn_dialog_bottom_sheet_ok;
    }

    public Button getBtn_dialog_bottom_sheet_cancel() {
        return btn_dialog_bottom_sheet_cancel;
    }

    public ImageView getShareButton() {
        return share;
    }

    public ImageView getDeleteButton() {
        return delete;
    }

    public ArrayList<EditText> getEditTexts() {
        return editTexts;
    }

    @Override
    public void onContactPicked(int editTextIndex, String text) {
        editTexts.get(editTextIndex).setText(text);
    }
}
