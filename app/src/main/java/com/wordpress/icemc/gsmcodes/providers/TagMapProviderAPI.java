package com.wordpress.icemc.gsmcodes.providers;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;

/**
 * Created by Bomen on 6/19/2017.
 */
public class TagMapProviderAPI {

    // This class cannot be instantiated
    private TagMapProviderAPI() {
    }

    //Operator table
    public static final class TagMapColumns implements BaseColumns {
        //This class can not be instantiated
        private TagMapColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/tagmaps");
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "gsmcodes_tagmaps";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "gsmcodes_tagmaps";

        //These are the only things needed for an insert
        public static final String TAG_NAME = "tagName";
        public static final String CODE_CODE = "codeCode";
        public static final String CODE_OPERATOR = "codeOperator";


    }
}
