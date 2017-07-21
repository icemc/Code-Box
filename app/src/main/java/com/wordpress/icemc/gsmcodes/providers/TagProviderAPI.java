package com.wordpress.icemc.gsmcodes.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;

/**
 * Created by Bomen on 6/19/2017.
 */
public class TagProviderAPI {

    // This class cannot be instantiated
    private TagProviderAPI() {
    }

    //Operator table
    public static final class TagColumns implements BaseColumns {
        //This class can not be instantiated
        private TagColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/tags");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "gsmcodes_tags";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "gsmcodes_tags";

        //These are the only things needed for an insert
        public static final String NAME = "name";
        public static final String OPERATOR_NAME = "operatorName";


    }
}
