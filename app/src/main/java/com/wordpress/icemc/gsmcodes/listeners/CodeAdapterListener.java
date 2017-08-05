package com.wordpress.icemc.gsmcodes.listeners;


public interface CodeAdapterListener {
    void onIconClicked(final int position);

    void onCodeRowClicked(final int position);

    void onPhoneClicked(final int position);
}
