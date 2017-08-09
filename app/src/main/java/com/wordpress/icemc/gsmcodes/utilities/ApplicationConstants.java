package com.wordpress.icemc.gsmcodes.utilities;

import android.net.Uri;

public class ApplicationConstants {
    //Global authority for the content provider
    public static final String AUTHORITY = "com.wordpress.icemc.gsmcodes.providers.myprovider";

    //Content URI to obtain codes that contain a specific tag
    public static final Uri CODES_WITH_TAG_CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/codes_with_tag");
    //Content URI to obtain codes that contain a specific tag
    public static final Uri SEARCH_CONTENT_URI = Uri.parse("content://" + ApplicationConstants.AUTHORITY + "/search");
    /**
     * This is a sentinel value that represents a number in a code string
     * e.g *166*@# . @ here represents a phone number(provided by the user)
     * which will replace @ before the code is executed
     */
    public static final String NUMBER_SENTINEL = "@";

    public static final String TAG_ALL_OPERATORS_SENTINEL = "All";

    //Constants for About activity
    public static final String APP_URL = "https://drive.google.com/open?id=0B3yz9blaMcZSd1FmNGlMYjhXUHc";
    public static final String HELP_URL = "https://icemc.github.io/code-box/code-box-faq/";
    public static final String EMAIL = "mailto:icemc500@gmail.com";
    public static final String GIT_HUB = "https://github.com/icemc/code-box-public";
    public static String SHARE_CONTENT = "A simple app that contains most USSD codes of mobile operators in Cameroon:\n" + APP_URL + "\n- " + "designed by Abanda Ludovic";

    //Shared preference field for last operator used.
    public static final String LAST_OPERATOR_USED = "lastOperator";
    //Shared preference key to evaluate if all data has been stored in the database
    public static final String IS_DATABASE_DATA_CORRECT = "isAllDataStored";

    public static  final String[] TRANSLATIONS_AVAILABLE = {"en", "fr"};
    public static final String APP_LANGUAGE = "currentAppLanguage";
    public static final String[] LANGUAGES = {"English", "Francais"};
}
