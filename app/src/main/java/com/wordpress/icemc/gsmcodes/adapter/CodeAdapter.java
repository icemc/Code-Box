package com.wordpress.icemc.gsmcodes.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.helpers.FlipAnimator;
import com.wordpress.icemc.gsmcodes.listeners.CodeAdapterListener;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.CodeItem;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;

import java.util.List;

public class CodeAdapter extends RecyclerView.Adapter<CodeAdapter.MyViewHolder>{
    private Context context;
    private List<CodeItem>  codes;
    private CodeAdapterListener listener;

    //Index is used to animate only one selected row
    private static int currentSelectedIndex = -1;

    //Index of last item Animated
    private  int lastItem = -1;

    public CodeAdapter(Context context, List<CodeItem> codes, CodeAdapterListener listener) {
        this.context = context;
        this.codes = codes;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.code_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        CodeItem item = codes.get(position);

        //display text view info
        holder.name.setText(item.getCode().getName());
        holder.description.setText(item.getCode().getDescription());

        //Display the first letter of code name in icon text
        holder.iconText.setText(item.getCode().getName().substring(0, 1));

        //apply background colour for oval drawable
        applyBGColor(holder, item);



        //apply icon animation
        applyIconAnimation(holder, position);

        //apply click events
        applyClickEvents(holder, position);


    }

    private void applyClickEvents(final MyViewHolder holder, final int position) {
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Apply animation
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
                holder.codeContainer.startAnimation(animation);
                listener.onIconClicked(position);
            }
        });

        holder.phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPhoneClicked(position);
            }
        });

        holder.codeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCodeRowClicked(position);
            }
        });

        holder.codeListWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPhoneClicked(position);
            }
        });

        holder.codeListWrapper.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Code code = codes.get(position).getCode();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, code.getOperator() + "\n"
                        + code.getName() + "\n"
                        + code.getDescription() + "\n"
                        + GSMCodeUtils.setCodeStringUsingInputFields(
                        code.getCode(), code.getInputFields()));
                intent.setType("text/plain");

                //Apply animation
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
                holder.codeContainer.startAnimation(animation);

                context.startActivity(intent);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });

        holder.codeContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO refactor to remove duplicate code
                Code code = codes.get(position).getCode();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, code.getOperator() + "\n"
                        + code.getName() + "\n"
                        + code.getDescription() + "\n"
                        + GSMCodeUtils.setCodeStringUsingInputFields(
                                    code.getCode(), code.getInputFields()));
                intent.setType("text/plain");

                //Apply animation
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
                holder.codeContainer.startAnimation(animation);

                context.startActivity(intent);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    private void applyBGColor(MyViewHolder holder, CodeItem item) {
            holder.background.setImageResource(R.drawable.bg_circle);
            holder.background.setColorFilter(item.getColor());
            holder.iconText.setVisibility(View.VISIBLE);
            holder.phone.getDrawable().mutate().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    private void applyIconAnimation(MyViewHolder holder, int position) {
        if (codes.get(position).getCode().isFavourite()) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(context, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(context, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }

    // As the views will be reused, sometimes the icon appears as
    // flipped because older view is reused. Reset the Y-axis to 0
    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    @Override
    public int getItemCount() {
        return codes.size();
    }

    public void toggleSelection(int position) {
        currentSelectedIndex = position;
        notifyItemChanged(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView name, description, iconText;
        public ImageView background, phone;
        public LinearLayout codeContainer;
        public android.support.v7.widget.CardView card;
        public RelativeLayout codeListWrapper, iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            codeListWrapper = (RelativeLayout) view.findViewById(R.id.code_list_wrapper);
            card = (CardView) view.findViewById(R.id.code_card);
            name = (TextView) view.findViewById(R.id.name);
            description = (TextView) view.findViewById(R.id.description);
            iconText = (TextView) view.findViewById(R.id.icon_text);
            phone = (ImageView) view.findViewById(R.id.icon_phone);
            background = (ImageView) view.findViewById(R.id.icon_profile);
            iconBack = (RelativeLayout) view.findViewById(R.id.icon_back);
            iconFront = (RelativeLayout) view.findViewById(R.id.icon_front);
            iconContainer = (RelativeLayout) view.findViewById(R.id.icon_container);
            codeContainer = (LinearLayout) view.findViewById(R.id.message_container);
        }
    }
}
