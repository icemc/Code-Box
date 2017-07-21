package com.wordpress.icemc.gsmcodes.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import com.wordpress.icemc.gsmcodes.R;

public class ContactButton extends ImageView{


    private int editTextId;
    public ContactButton(Context context, int editTextId) {
        super(context);
        this.editTextId = editTextId;

//        setContactDrawable();
   }

    public void setContactDrawable() {
        setImageDrawable(getResources().getDrawable(R.drawable.ic_contacts_black_24dp));
        getDrawable().mutate().setColorFilter(
                getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
    }

    public int getEditTextId() {
        return editTextId;
    }

    public void setEditTextId(int editTextId) {
        this.editTextId = editTextId;
    }

}
