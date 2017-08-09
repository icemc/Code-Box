package com.wordpress.icemc.gsmcodes.listeners;

import com.wordpress.icemc.gsmcodes.model.CodeLoadMessage;

/**
 * Created by Bomen on 8/4/2017.
 */

public interface SaveCodesListener {
    void onCodeLoaderFinished(CodeLoadMessage message);
    void onCodeLoaderStart();
}
