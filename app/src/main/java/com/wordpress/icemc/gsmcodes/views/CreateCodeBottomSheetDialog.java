package com.wordpress.icemc.gsmcodes.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private View noTagView;
    private TextView tagButton;
    private ArrayList<String> tagsList = new ArrayList<>();
    private Button okButton, cancelButton;
    private LinearLayout tagsContainer;
    private LayoutInflater layoutInflater;

    private AddCodeListener listener;
    public CreateCodeBottomSheetDialog(@NonNull Context context, AddCodeListener listener) {
        super(context, R.style.MaterialBottomSheet);
        this.listener = listener;
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        tagButton = (TextView) dialogView.findViewById(R.id.textView);

        okButton = (Button) dialogView.findViewById(R.id.btn_create_code_dialog_bottom_sheet_ok);
        cancelButton = (Button) dialogView.findViewById(R.id.btn_create_code_dialog_bottom_sheet_cancel);
        tagsContainer =  (LinearLayout) dialogView.findViewById(R.id.tags_container);

        noTagView = layoutInflater.inflate(R.layout.category_text_view, null);
        TextView noTagTextView = (TextView) noTagView.findViewById(R.id.category_text_view);
        noTagTextView.setText(R.string.no_tags_msg);
        noTagTextView.setOnClickListener(tagListener);
        tagButton.setOnClickListener(tagListener);
        tagsContainer.setOnClickListener(tagListener);
        tagsContainer.addView(noTagView);

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

    @Override
    public void onTagSelectionFinished(List<String> allTags) {
        tagsList.clear();
        tagsContainer.removeAllViews();
        if(allTags != null && allTags.size() > 0) {
            tagsList.addAll(allTags);
            for (String s: allTags) {
                View v = layoutInflater.inflate(R.layout.category_text_view, null);
                TextView t = (TextView) v.findViewById(R.id.category_text_view);
                t.setText(s);
                t.setOnClickListener(tagListener);
                tagsContainer.addView(v);
            }

        } else {
            tagsContainer.addView(noTagView);
        }
    }

    private View.OnClickListener tagListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            listener.onTagsButtonClick(tagsList);
        }
    };

    public interface AddCodeListener {
        void onOKButtonClick ();
        void onTagsButtonClick (List<String> tagList);
    }

}
