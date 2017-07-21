package com.wordpress.icemc.gsmcodes.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;

public class CodeProviderAPI {
    // This class cannot be instantiated
    private CodeProviderAPI() {
    }

    //Operator table
    public static final class CodeColumns implements BaseColumns {
        //This class can not be instantiated
        private CodeColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/codes");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "gsmcodes_codes";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "gsmcodes_codes";

        //These are the only things needed for an insert
        public static final String CODE = "code";
        public static final String NAME = "name";
        public static final String OPERATOR_NAME = "operatorName";
        public static final String IS_FAVOURITE = "isFavourite";
        public static final String DESCRIPTION = "description";
        public static final String INPUT_FIELD = "inputField";
    }
}
