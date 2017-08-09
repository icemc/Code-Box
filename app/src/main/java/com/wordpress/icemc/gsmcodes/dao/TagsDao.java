package com.wordpress.icemc.gsmcodes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.wordpress.icemc.gsmcodes.model.Tag;

import java.util.ArrayList;
import java.util.List;

import static com.wordpress.icemc.gsmcodes.providers.TagProviderAPI.TagColumns;

/**
 * This class is used to encapsulate all access to the {@link com.wordpress.icemc.gsmcodes.providers.GSMCodeContentProvider}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class TagsDao {
    private Context context;

    public TagsDao(Context context) {
        this.context = context;
    }

    public Cursor getTagsCursor() {
        return getTagsCursor(null, null, null, null);
    }

    public Cursor getTagsCursorFromOperatorId(String codeName) {

        return new TagMapDao(context).getTagsCursorFromCodeId(codeName);
    }

    public Cursor getTagsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(TagColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Uri saveTag(ContentValues values) {
        return context.getContentResolver().insert(TagColumns.CONTENT_URI, values);
    }
    public List<Tag> getTagsFromCursor(Cursor cursor) {
        List<Tag> tags = new ArrayList<>();
        if(cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(TagColumns.NAME);
                    int operatorNameIndex = cursor.getColumnIndex(TagColumns.OPERATOR_NAME);

                    Tag tag = new Tag.Builder()
                            .name(cursor.getString(nameIndex))
                            .operator(cursor.getString(operatorNameIndex))
                            .build();

                    tags.add(tag);
                }
            } finally {
                cursor.close();
            }
        }
        return tags;
    }

    public ContentValues getValuesFromObject(Tag tag) {
        ContentValues values = new ContentValues();
        values.put(TagColumns.NAME, tag.getName());
        values.put(TagColumns.OPERATOR_NAME, tag.getOperator());
        return values;
    }

    public void deleteTags() {
        context.getContentResolver().delete(TagColumns.CONTENT_URI, null, null);
    }
}
