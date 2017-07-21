package com.wordpress.icemc.gsmcodes.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.activities.HomeActivity;
import com.wordpress.icemc.gsmcodes.model.Operator;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;
import com.wordpress.icemc.gsmcodes.utilities.GSMCodeUtils;

import java.util.List;

public class OperatorAdapter extends  RecyclerView.Adapter<OperatorAdapter.MyViewHolder>{

    private List<Operator> operators;
    private Context context;
    private SharedPreferences sharedPreferences;

    public OperatorAdapter(Context context, List<Operator> operators) {
        this.context = context;
        this.operators = operators;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.operator_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final int pos = position;
        Operator operator = operators.get(position);
        holder.description.setText(operator.getDescription());
        holder.name.setText(operator.getName());

        // loading logo
        Glide.with(context).load(GSMCodeUtils.getLogoFromOperatorName(operator.getName())).into(holder.logo);

       View.OnClickListener listener =  new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        sharedPreferences.edit().putString(ApplicationConstants.LAST_OPERATOR_USED,
                        operators.get(pos).getName())
                        .apply();
                Intent intent = new Intent(context, HomeActivity.class);
                context.startActivity(intent);
            }
        };
        holder.card.setOnClickListener(listener);
        holder.logo.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return operators.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView logo;
        public TextView name, description, count;
        public CardView card;

        public MyViewHolder(View itemView) {
            super(itemView);
            logo = (ImageView) itemView.findViewById(R.id.operator_logo);
            name = (TextView) itemView.findViewById(R.id.operator_name);
            //count = (TextView) itemView.findViewById(R.id.count);
            description = (TextView) itemView.findViewById(R.id.operator_description);
            card = (CardView) itemView.findViewById(R.id.operator_card);

        }
    }
}
