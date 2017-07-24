package com.wordpress.icemc.gsmcodes.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.wordpress.icemc.gsmcodes.model.Code;
import com.wordpress.icemc.gsmcodes.model.CodeItem;
import com.wordpress.icemc.gsmcodes.providers.GSMCodeContentProvider;
import com.wordpress.icemc.gsmcodes.utilities.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.wordpress.icemc.gsmcodes.providers.CodeProviderAPI.CodeColumns;

/**
 * This class is used to encapsulate all access to the {@link GSMCodeContentProvider}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class CodeDao {

    private Context context;

    public CodeDao(@NonNull Context context) {
        this.context = context;
    }

    public Cursor getCodesCursor() {
        return getCodesCursor(null, null, null, null);
    }

    public Cursor getCodesCursorFromOperatorId(String operatorId) {
        String selection = CodeColumns.OPERATOR_NAME + "=?";
        String[] selectionArgs = {operatorId};
        String sortOrder = CodeColumns.NAME + " ASC";

        return getCodesCursor(null, selection, selectionArgs, sortOrder);
    }

    public Cursor getCodesCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(CodeColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public Uri saveCode(ContentValues values) {
        return context.getContentResolver().insert(CodeColumns.CONTENT_URI, values);
    }

    public int deleteCode(Code code) {
        String selection = CodeColumns.CODE + "=? " + "AND " + CodeColumns.OPERATOR_NAME + "=?";
        String[] selectionArgs = {code.getCode(), code.getOperator()};
        return context.getContentResolver().delete(CodeColumns.CONTENT_URI, selection, selectionArgs);
    }

    public int updateCode(Code code) {
        String selection = CodeColumns.CODE + "=? " + "AND " + CodeColumns.OPERATOR_NAME + "=?";
        String[] selectionArgs = {code.getCode(), code.getOperator()};
        return context.getContentResolver().update(CodeColumns.CONTENT_URI, getValuesFromObject(code), selection, selectionArgs);
    }

    public List<Code> getCodesFromCursor(Cursor cursor) {
        List<Code> codes = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(CodeColumns.NAME);
                    int codeIndex = cursor.getColumnIndex(CodeColumns.CODE);
                    int operatorIndex = cursor.getColumnIndex(CodeColumns.OPERATOR_NAME);
                    int descriptionIndex = cursor.getColumnIndex(CodeColumns.DESCRIPTION);
                    int inputFieldIndex = cursor.getColumnIndex(CodeColumns.INPUT_FIELD);
                    int favouriteIndex = cursor.getColumnIndex(CodeColumns.IS_FAVOURITE);

                    Code code = null;
                    try {
                        Code.Builder builder = new Code.Builder();
                        String inputFields = cursor.getString(inputFieldIndex);
                        if(!inputFields.equals(" ")) {
                            builder.inputFields(JsonUtils.convertJSONArrayIntoInputFieldArray(
                                    new JSONArray(inputFields)));
                        } else {
                            builder.inputFields(null);
                        }
                        builder.code(cursor.getString(codeIndex))
                                .name(cursor.getString(nameIndex))
                                .operator(cursor.getString(operatorIndex))
                                .description(cursor.getString(descriptionIndex))
                                .isFavourite(Boolean.valueOf(cursor.getString(favouriteIndex)));
                        code = builder.build();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    codes.add(code);
                }
            } finally {
                cursor.close();
            }
        }
        return codes;
    }

    public List<CodeItem> getCodeItemsFromCursor(Cursor cursor) {
        List<CodeItem> codes = new ArrayList<>();
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int nameIndex = cursor.getColumnIndex(CodeColumns.NAME);
                    int codeIndex = cursor.getColumnIndex(CodeColumns.CODE);
                    int operatorIndex = cursor.getColumnIndex(CodeColumns.OPERATOR_NAME);
                    int descriptionIndex = cursor.getColumnIndex(CodeColumns.DESCRIPTION);
                    int inputFieldIndex = cursor.getColumnIndex(CodeColumns.INPUT_FIELD);
                    int favouriteIndex = cursor.getColumnIndex(CodeColumns.IS_FAVOURITE);

                    Code code = null;
                    try {
                        Code.Builder builder = new Code.Builder();
                        String inputFields = cursor.getString(inputFieldIndex);
                        if(!inputFields.equals(" ")) {
                            builder.inputFields(JsonUtils.convertJSONArrayIntoInputFieldArray(
                                    new JSONArray(inputFields)));
                        } else {
                            builder.inputFields(null);
                        }
                        builder.code(cursor.getString(codeIndex))
                                .name(cursor.getString(nameIndex))
                                .operator(cursor.getString(operatorIndex))
                                .description(cursor.getString(descriptionIndex))
                                .isFavourite(Boolean.valueOf(cursor.getString(favouriteIndex)));
                        code = builder.build();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    codes.add(new CodeItem(code, getRandomMaterialColor("400")));
                }
            } finally {
                cursor.close();
            }
        }
        return codes;
    }

    public ContentValues getValuesFromObject(Code code){
        ContentValues values = new ContentValues();
        values.put(CodeColumns.NAME, code.getName());
        values.put(CodeColumns.CODE, code.getCode());
        values.put(CodeColumns.OPERATOR_NAME, code.getOperator());
        values.put(CodeColumns.DESCRIPTION, code.getDescription());
        values.put(CodeColumns.IS_FAVOURITE, Boolean.valueOf(code.isFavourite()).toString());
        if (code.getInputFields() != null) {
            values.put(CodeColumns.INPUT_FIELD, JsonUtils.convertInputFieldArrayIntoJSONString(
                    code.getInputFields()));
        } else {
            values.put(CodeColumns.INPUT_FIELD, " ");
        }

        return values;
    }


    /**
     * chooses a random color from array.xml
     */
    public int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLUE);
            colors.recycle();
        }
        return returnColor;
    }
}
