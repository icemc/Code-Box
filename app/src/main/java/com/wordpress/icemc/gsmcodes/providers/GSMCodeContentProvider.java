package com.wordpress.icemc.gsmcodes.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.wordpress.icemc.gsmcodes.R;
import com.wordpress.icemc.gsmcodes.dao.CodeDao;
import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.utilities.ApplicationConstants;

import java.util.HashMap;
import java.util.List;

import static com.wordpress.icemc.gsmcodes.providers.CodeProviderAPI.CodeColumns;
import static com.wordpress.icemc.gsmcodes.providers.OperatorProviderAPI.OperatorColumns;
import static com.wordpress.icemc.gsmcodes.providers.TagMapProviderAPI.TagMapColumns;
import static com.wordpress.icemc.gsmcodes.providers.TagProviderAPI.TagColumns;

public class GSMCodeContentProvider extends ContentProvider {
    private static final String DATABASE_NAME = "gsmcodes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String OPERATORS_TABLE = "operators_v1";
    private static final String TAGS_TABLE = "tags_v1";
    private static final String CODES_TABLE = "codes_v1";
    private static final String TAG_MAPS_TABLE = "tag_maps_v1";
    private static final String TAG = GSMCodeContentProvider.class.getSimpleName();

    private static HashMap<String, String> operatorsProjectionMap;
    private static HashMap<String, String> codesProjectionMap;
    private static HashMap<String, String> tagsProjectionMap;
    private static HashMap<String, String> tagMapsProjectionMap;

    private static final int OPERATORS = 1;
    private static final int OPERATOR_ID = 2;
    private static final int TAGS = 3;
    private static final int TAG_ID = 4;
    private static final int CODES = 5;
    private static final int CODE_ID = 6;
    private static final int TAG_MAPS = 7;
    private static final int TAG_MAP_ID = 8;
    private static final int CODES_WITH_TAG = 9;
    private static final int SEARCH = 10;

    private static final UriMatcher uriMatcher;

    public GSMCodeContentProvider() {
    }
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context, String databaseame) {
            super(context, databaseame, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onCreateNamed(db, OPERATORS_TABLE);
            onCreateNamed(db, TAGS_TABLE);
            onCreateNamed(db, CODES_TABLE);
            onCreateNamed(db, TAG_MAPS_TABLE);
        }

        private void onCreateNamed(SQLiteDatabase db, String tableName){
            switch (tableName) {
                case OPERATORS_TABLE:
                    db.execSQL("CREATE TABLE " + tableName + " ("
                            + OperatorColumns.NAME + " text primary key, "
                            + OperatorColumns.DESCRIPTION + " text not null, "
                            + OperatorColumns.PHONE_NUMBER + " text not null, "
                            + OperatorColumns.LOGO_PATH + " integer not null );");
                    return;

                case TAGS_TABLE:
                    db.execSQL("CREATE TABLE " + tableName + " ("
                            + TagColumns.NAME + " text primary key, "
                            + TagColumns.OPERATOR_NAME + " text  not null );");
                    return;
                case CODES_TABLE:
                    db.execSQL("CREATE TABLE " + tableName + " ("
                            + CodeColumns.CODE + " text not null, "
                            + CodeColumns.NAME + " text not null, "
                            + CodeColumns.OPERATOR_NAME + " text not null, "
                            + CodeColumns.IS_FAVOURITE + " text not null, "
                            + CodeColumns.DESCRIPTION + " text not null, "
                            + CodeColumns.INPUT_FIELD + " text not null, PRIMARY KEY("
                            + CodeColumns.CODE + ", " + CodeColumns.OPERATOR_NAME +"));");
                    return;
                case TAG_MAPS_TABLE:
                    db.execSQL("CREATE TABLE " + tableName + " ("
                            + TagMapColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + TagMapColumns.TAG_NAME + " text not null, "
                            + TagMapColumns.CODE_OPERATOR + " text not null, "
                            + TagMapColumns.CODE_CODE + " text not null );");
                    return;
                default:
                    throw new IllegalArgumentException("Unknown table name");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        String tableName = "";
        switch ((uriMatcher.match(uri))) {
            case OPERATORS:
                tableName = OPERATORS_TABLE;
                break;

            case TAGS:
                tableName = TAGS_TABLE;
                break;

            case CODES:
                tableName = CODES_TABLE;
                break;

            case TAG_MAPS:
                tableName = TAG_MAPS_TABLE;
                break;

            default:
                throw  new IllegalArgumentException("Unknown URI");
        }

        int count =  db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private DatabaseHelper databaseHelper;

    private DatabaseHelper getDbHelper() {

        if (databaseHelper != null) {
            return databaseHelper;
        }
        databaseHelper = new DatabaseHelper(this.getContext(), DATABASE_NAME);
        return databaseHelper;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case OPERATORS:
                return OperatorColumns.CONTENT_TYPE;
            case OPERATOR_ID:
                return OperatorColumns.CONTENT_ITEM_TYPE;
            case TAGS:
                return TagColumns.CONTENT_TYPE;
            case TAG_ID:
                return TagColumns.CONTENT_ITEM_TYPE;
            case CODES:
                return CodeColumns.CONTENT_TYPE;
            case CODE_ID:
                return CodeColumns.CONTENT_ITEM_TYPE;
            case TAG_MAPS:
                return TagMapColumns.CONTENT_TYPE;
            case TAG_MAP_ID:
                return TagMapColumns.CONTENT_ITEM_TYPE;
            default:
                throw  new IllegalArgumentException("Unknown URI");
        }
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        long rowId;

        switch (uriMatcher.match(uri)) {
            case OPERATORS:
                //TODO check if the operator already exists

                rowId = db.insert(OPERATORS_TABLE, null, values);
                //if (rowId > 0) {
                    return ContentUris.withAppendedId(OperatorColumns.CONTENT_URI, rowId);
                //}
                //break;

            case TAGS:
                //TODO check if the tag already exists

                rowId = db.insert(TAGS_TABLE, null, values);
                if (rowId > 0) {
                    return ContentUris.withAppendedId(TagColumns.CONTENT_URI, rowId);
                }
                break;

            case CODES:
                String selection = CodeColumns.CODE + "=? " + "AND " + CodeColumns.OPERATOR_NAME + "=?";
                String[] selectionArgs = {values.getAsString(CodeColumns.CODE), values.getAsString(CodeColumns.OPERATOR_NAME)};
                Cursor c = query(uri, null, selection, selectionArgs, null);
                if(c != null &&  c.moveToNext()) {
                    try {

                        CodeDao dao = new CodeDao(getContext());
                        c.moveToPrevious();
                        List<Code> codes = dao.getCodesFromCursor(c);
                        if (codes != null && codes.size() > 0) {
                            //Code not Saved
                            return null;
                        }
//                         else {
////                            Toast t = new Toast(getContext());
////                            t.setText(getContext().getString(R.string.error_code_not_saved));
////                            t.setDuration(Toast.LENGTH_LONG);
//                        }
                    } finally {
                        //c.close();
                    }
                }
                rowId = db.insert(CODES_TABLE, null, values);
                if (rowId > 0) {
                    return ContentUris.withAppendedId(CodeColumns.CONTENT_URI, rowId);
                }
                break;

            case TAG_MAPS:
                //TODO check if the tagMap already exists

                rowId = db.insert(TAG_MAPS_TABLE, null, values);
                if (rowId > 0) {
                    Log.d(TAG, "Inserted tag map with values: code = " + values.get(TagMapColumns.CODE_CODE) + " tag: " + values.get(TagMapColumns.TAG_NAME));
                    return ContentUris.withAppendedId(TagMapColumns.CONTENT_URI, rowId);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI");
        }

//        throw new SQLException("Failed to insert row into " + uri);
        return null;
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper dh = getDbHelper();
        return dh != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            switch ((uriMatcher.match(uri))) {
                case OPERATORS:
                    qb.setTables(OPERATORS_TABLE);
                    qb.setProjectionMap(operatorsProjectionMap);
                    break;

                case OPERATOR_ID:
                    //TODO set a query for this
                case TAGS:
                    qb.setTables(TAGS_TABLE);
                    qb.setProjectionMap(tagsProjectionMap);
                    break;

                case TAG_ID:
                    //TODO set a query for this

                case CODES:
                    qb.setTables(CODES_TABLE);
                    qb.setProjectionMap(codesProjectionMap);
                    break;

                case CODE_ID:
                    //TODO set a query for this

                case TAG_MAPS:
                    qb.setTables(TAG_MAPS_TABLE);
                    qb.setProjectionMap(tagMapsProjectionMap);
                    break;

                case TAG_MAP_ID:
                    //TODO set a query for this
                    break;
                case CODES_WITH_TAG:
                    String operatorName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(ApplicationConstants.LAST_OPERATOR_USED, "");
                    if(operatorName.equals("")) {
                        return null;
                    }

                    String query = "SELECT c.* "
                            + "FROM " + CODES_TABLE + " c, " + TAGS_TABLE + " t, " + TAG_MAPS_TABLE + " tm "
                            + "WHERE tm." + TagMapColumns.TAG_NAME + " = " + "'" + selection + "'"
                            + " AND " + "c." + CodeColumns.CODE + " = " + "tm." + TagMapColumns.CODE_CODE
                            + " AND " + "c." + CodeColumns.OPERATOR_NAME + " = " + "'" + operatorName + "'"
                            + " GROUP BY c." + CodeColumns.NAME
                            + " ORDER BY c." + CodeColumns.NAME + " ASC";

                    Cursor cursor = getDbHelper().getReadableDatabase().rawQuery(query, null);
                        cursor.setNotificationUri(getContext().getContentResolver(), uri);
                        return cursor;
                case SEARCH:
                    String searchOperatorName = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(ApplicationConstants.LAST_OPERATOR_USED, "");
                    if(searchOperatorName.equals("")) {
                        return null;
                    }

                    if (selectionArgs.length > 0) {
                        String tags = "'" + selectionArgs[0] + "'";
                        for (int i = 1; i < selectionArgs.length; i++) {
                            tags += ", " + "'" + selectionArgs[i] + "'";
                        }

                        String query2 = "SELECT c.* "
                                + " FROM " + CODES_TABLE + " AS c, " + TAGS_TABLE + " AS t, " + TAG_MAPS_TABLE + " AS tm "
                                + " WHERE  " + "c." + CodeColumns.CODE + " = " + "tm." + TagMapColumns.CODE_CODE
                                + " AND (tm." + TagMapColumns.TAG_NAME + " IN (" + tags + "))"
                                + " AND " + "c." + CodeColumns.OPERATOR_NAME + " = " + "'" + searchOperatorName + "'"
                                + " UNION ";

                        String query1 = "SELECT c.* "
                                + "FROM " + CODES_TABLE + " AS c "
                                + "WHERE c." + CodeColumns.OPERATOR_NAME + " = " + "'" + searchOperatorName + "'" + " AND"
                                + " c." + CodeColumns.NAME + " LIKE " + "'%" + selection + "%'"
                                + " ORDER BY c." + CodeColumns.NAME;

                        String searchQuery = query2 + query1;
                        Cursor searchCursor = getDbHelper().getReadableDatabase().rawQuery(searchQuery, null);
                        searchCursor.setNotificationUri(getContext().getContentResolver(), uri);
                        return searchCursor;
                    } else {
                        String query1 = "SELECT c.* "
                                + "FROM " + CODES_TABLE + " c "
                                + "WHERE c." + CodeColumns.OPERATOR_NAME + " = " + "'" + searchOperatorName + "'" + " AND"
                                + " c." + CodeColumns.NAME + " LIKE " + "'%" + selection + "%'"
                                + " GROUP BY c." + CodeColumns.NAME;

                        String searchQuery = query1;
                        Cursor searchCursor = getDbHelper().getReadableDatabase().rawQuery(searchQuery, null);
                        searchCursor.setNotificationUri(getContext().getContentResolver(), uri);
                        return searchCursor;
                    }
                default:
                    throw new IllegalArgumentException("Unknown URI");
            }

            //Get the database and run the query
            SQLiteDatabase db = getDbHelper().getReadableDatabase();
            Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

            // Tell the cursor what uri to watch, so it knows when its source data
            // changes

            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        String tableName = "";
        switch ((uriMatcher.match(uri))) {
            case OPERATORS:
                tableName = OPERATORS_TABLE;
                break;

            case TAGS:
                tableName = TAGS_TABLE;
                break;

            case CODES:
                tableName = CODES_TABLE;
                break;

            case TAG_MAPS:
                tableName = TAG_MAPS_TABLE;
                break;

            default:
                throw  new IllegalArgumentException("Unknown URI");
        }

        int count =  db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "operators", OPERATORS);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "operators/#", OPERATOR_ID);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "tags", TAGS);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "tags/#", TAG_ID);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "codes", CODES);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "codes/#", CODE_ID);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "tagmaps", TAG_MAPS);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "tagmaps/#", TAG_MAP_ID);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "codes_with_tag", CODES_WITH_TAG);
        uriMatcher.addURI(ApplicationConstants.AUTHORITY, "search", SEARCH);

        //Initialization of the operator projection map
        operatorsProjectionMap = new HashMap<>();
        operatorsProjectionMap.put(OperatorColumns.NAME, OperatorColumns.NAME);
        operatorsProjectionMap.put(OperatorColumns.DESCRIPTION, OperatorColumns.DESCRIPTION);
        operatorsProjectionMap.put(OperatorColumns.LOGO_PATH, OperatorColumns.LOGO_PATH);
        operatorsProjectionMap.put(OperatorColumns.PHONE_NUMBER, OperatorColumns.PHONE_NUMBER);

        //Initialization of the tag projection map
        tagsProjectionMap = new HashMap<>();
        tagsProjectionMap.put(TagColumns.NAME, TagColumns.NAME);
        tagsProjectionMap.put(TagColumns.OPERATOR_NAME, TagColumns.OPERATOR_NAME);

        //Initialization of the code projection map
        codesProjectionMap = new HashMap<>();
        codesProjectionMap.put(CodeColumns.CODE, CodeColumns.CODE);
        codesProjectionMap.put(CodeColumns.NAME, CodeColumns.NAME);
        codesProjectionMap.put(CodeColumns.IS_FAVOURITE, CodeColumns.IS_FAVOURITE);
        codesProjectionMap.put(CodeColumns.DESCRIPTION, CodeColumns.DESCRIPTION);
        codesProjectionMap.put(CodeColumns.OPERATOR_NAME, CodeColumns.OPERATOR_NAME);
        codesProjectionMap.put(CodeColumns.INPUT_FIELD, CodeColumns.INPUT_FIELD);

        //Initialization of the tag map projection map
        tagMapsProjectionMap = new HashMap<>();
        tagMapsProjectionMap.put(TagMapColumns._ID, TagMapColumns._ID);
        tagMapsProjectionMap.put(TagMapColumns.TAG_NAME, TagMapColumns.TAG_NAME);
        tagMapsProjectionMap.put(TagMapColumns.CODE_CODE, TagMapColumns.CODE_CODE);
        tagMapsProjectionMap.put(TagMapColumns.CODE_OPERATOR, TagMapColumns.CODE_OPERATOR);
    }


}
