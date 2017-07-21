package com.wordpress.icemc.gsmcodes.utilities;

import android.net.Uri;

public class ApplicationConstants {
    //Global authority for the content provider
    public static final String AUTHORITY = "com.wordpress.icemc.gsmcodes.providers.myprovider";

    //Content URI to obtain codes that contain a specific tag
    public static final Uri CODES_WITH_TAG_CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/codes_with_tag");

    /**
     * This is a sentinel value that represents a number in a code string
     * e.g *166*@# . @ here represents a phone number(provided by the user)
     * which will replace @ before the code is executed
     */
    public static final String NUMBER_SENTINEL = "@";

    public static final String SPACE_SENTINEL = "_";
    public static final String TAG_ALL_OPERATORS_SENTINEL = "All";

    //Constants for About activity
    public static final String APP_URL = "";
    public static final String EMAIL = "mailto:icemc500@gmail.com";
    public static final String GIT_HUB = "";
    public static final boolean SHARE_CONTENT = true;

    //Shared preference field for last operator used.
    public static final String LAST_OPERATOR_USED = "lastOperator";
}
