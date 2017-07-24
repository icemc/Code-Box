package com.wordpress.icemc.gsmcodes.listeners;


public interface CodeAdapterListener {
    void onIconClicked(int position);

    void onCodeRowClicked(int position);

    void onPhoneClicked(int position);
    void onCodeRowSwiped(int position);
}
