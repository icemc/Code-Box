package com.wordpress.icemc.gsmcodes.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;

/**
 * Created by Abanda on 6/19/2017.
 */
public class OperatorProviderAPI {

    // This class cannot be instantiated
    private OperatorProviderAPI() {
    }

    //Operator table
    public static final class OperatorColumns implements BaseColumns {
        //This class can not be instantiated
        private OperatorColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/operators");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "gsmcodes_operators";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "gsmcodes_operators";

        //These are the only things needed for an insert
        public static final String NAME = "name";
        public static final String LOGO_PATH = "logoPath";
        public static final String DESCRIPTION = "description";
        public static final String PHONE_NUMBER = "tel";

    }
}
