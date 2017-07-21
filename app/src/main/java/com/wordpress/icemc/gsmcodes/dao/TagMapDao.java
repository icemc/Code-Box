package com.wordpress.icemc.gsmcodes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.wordpress.icemc.gsmcodes.model.TagMap;

import java.util.ArrayList;
import java.util.List;

import static com.wordpress.icemc.gsmcodes.providers.TagMapProviderAPI.TagMapColumns;

/**
 * This class is used to encapsulate all access to the {@link com.wordpress.icemc.gsmcodes.providers.GSMCodeContentProvider}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class TagMapDao {
    private Context context;

    public TagMapDao(Context context) {
        this.context = context;
    }

    public Cursor getTagMapCursor() {
        return getTagMapCursor(null, null, null, null);
    }

    public Cursor getTagsCursorFromCodeId(String codeName) {
        String[] projection = {TagMapColumns.TAG_NAME};
        String selection = TagMapColumns.CODE_CODE + "=?";
        String[] selectionArgs = {codeName};

        return getTagMapCursor(projection, selection, selectionArgs, null);
    }

    public Cursor getTagMapCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(TagMapColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Uri saveTagMap(ContentValues values) {
        return context.getContentResolver().insert(TagMapColumns.CONTENT_URI, values);
    }

    public List<TagMap> getTagMapFromCursor(Cursor cursor) {
        List<TagMap> tagMaps = new ArrayList<>();
        if(cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int codeNameIndex = cursor.getColumnIndex(TagMapColumns.CODE_CODE);
                    int tagNameIndex = cursor.getColumnIndex(TagMapColumns.TAG_NAME);

                    TagMap tagMap = new TagMap.Builder()
                            .tagId(cursor.getString(tagNameIndex))
                            .codeId(cursor.getString(codeNameIndex))
                            .build();

                    tagMaps.add(tagMap);
                }
            } finally {
                cursor.close();
            }
        }
        return tagMaps;
    }

    public ContentValues getValuesFromObject(TagMap tagMap) {
        ContentValues values = new ContentValues();
        values.put(TagMapColumns.CODE_CODE, tagMap.getCodeId());
        values.put(TagMapColumns.TAG_NAME, tagMap.getTagId());

        return values;
    }
}
