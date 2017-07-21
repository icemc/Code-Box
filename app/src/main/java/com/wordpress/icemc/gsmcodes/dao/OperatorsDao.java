package com.wordpress.icemc.gsmcodes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.wordpress.icemc.gsmcodes.model.Operator;

import java.util.ArrayList;
import java.util.List;

import static com.wordpress.icemc.gsmcodes.providers.OperatorProviderAPI.OperatorColumns;

/**
 * This class is used to encapsulate all access to the {@link com.wordpress.icemc.gsmcodes.providers.GSMCodeContentProvider}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class OperatorsDao {
    private Context context;

    public OperatorsDao(Context context) {
        this.context = context;
    }

    public Cursor getOperatorsCursor() {
        return getOperatorsCursor(null, null, null, OperatorColumns.NAME + " ASC");
    }

    public Uri saveOperator(ContentValues values) {
        return context.getContentResolver().insert(OperatorColumns.CONTENT_URI, values);
    }

    public Cursor getOperatorsCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(OperatorColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public List<Operator> getOperatorsFromCursor(Cursor cursor) {
        List<Operator> operators = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(OperatorColumns.NAME);
                    int descriptionIndex = cursor.getColumnIndex(OperatorColumns.DESCRIPTION);
                    int logoPathIndex = cursor.getColumnIndex(OperatorColumns.LOGO_PATH);
                    int telIndex = cursor.getColumnIndex(OperatorColumns.PHONE_NUMBER);

                    Operator operator = new Operator.Builder()
                            .name(cursor.getString(nameIndex))
                            .description(cursor.getString(descriptionIndex))
                            .logoPath(cursor.getInt(logoPathIndex))
                            .phoneNumer(cursor.getString(telIndex))
                            .build();

                    operators.add(operator);
                    Log.d(OperatorsDao.class.getSimpleName(), "collected: " + operator);
                }
            } finally {
                cursor.close();
            }
        }
        Log.d(OperatorsDao.class.getSimpleName(), "Total: " + operators.size());
        return operators;
    }

    public ContentValues getValuesFromObject(Operator operator) {
        ContentValues values = new ContentValues();
        values.put(OperatorColumns.NAME, operator.getName());
        values.put(OperatorColumns.DESCRIPTION, operator.getDescription());
        values.put(OperatorColumns.LOGO_PATH, operator.getLogoPath());
        values.put(OperatorColumns.PHONE_NUMBER,operator.getPhoneNumber());
        return values;
    }
}
