package com.wordpress.icemc.gsmcodes.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.listeners.TagSelectionListener;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;

import java.util.ArrayList;
import java.util.List;

public class CreateCodeBottomSheetDialog extends BottomSheetDialog implements TagSelectionListener {

    private EditText code, name, description;
    private TextView tags, tagButton;
    private ArrayList<String> tagsList = new ArrayList<>();
    private Button okButton, cancelButton;
    private HorizontalScrollView scrollView;

    private AddCodeListener listener;
    public CreateCodeBottomSheetDialog(@NonNull Context context, AddCodeListener listener) {
        super(context, R.style.MaterialBottomSheet);
        this.listener = listener;
        inflateSheet();
    }

    private void inflateSheet() {
        final View dialogView = getLayoutInflater().inflate(R.layout.add_bottom_sheet_dialog, null);
        ImageView logo = (ImageView) dialogView.findViewById(R.id.operator_logo);
        Glide.with(getContext()).load(GSMCodeUtils.getLogoFromOperatorName(
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
                        ApplicationConstants.LAST_OPERATOR_USED, ""))).into(logo);

        code = (EditText) dialogView.findViewById(R.id.code_code);
        name = (EditText) dialogView.findViewById(R.id.code_name);
        description = (EditText) dialogView.findViewById(R.id.code_description);
        tags = (TextView) dialogView.findViewById(R.id.tag_list);
        tagButton = (TextView) dialogView.findViewById(R.id.textView);

        okButton = (Button) dialogView.findViewById(R.id.btn_create_code_dialog_bottom_sheet_ok);
        cancelButton = (Button) dialogView.findViewById(R.id.btn_create_code_dialog_bottom_sheet_cancel);
        scrollView = (HorizontalScrollView) dialogView.findViewById(R.id.scroll_tag);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOKButtonClick();
            }
        });

        View.OnClickListener tagListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTagsButtonClick(tagsList);
            }
        };

        tags.setOnClickListener(tagListener);
        tagButton.setOnClickListener(tagListener);
        scrollView.setOnClickListener(tagListener);
        setContentView(dialogView);
    }


    public String getCode() {
        return code.getText().toString();
    }

    public String getName() {
        return name.getText().toString();
    }

    public String getDescription() {
        return description.getText().toString();
    }

    public List<String> getTags() {
        return tagsList;
    }
    public void setTags(List<String> allTags) {

    }

    @Override
    public void onTagSelectionFinished(List<String> allTags) {
        tagsList.clear();
        if(allTags != null && allTags.size() > 0) {
            tagsList.addAll(allTags);
            String tagsString = " " + allTags.get(0);
            for (String s: allTags.subList(1, allTags.size())) {
                tagsString +=  ", " + s;
            }
            tagsString += " ";
            tags.setText(tagsString);
        } else {
            tags.setText(getContext().getString(R.string.no_tags_msg));
        }
    }

    public interface AddCodeListener {
        void onOKButtonClick ();
        void onTagsButtonClick (List<String> tagList);
    }

}
