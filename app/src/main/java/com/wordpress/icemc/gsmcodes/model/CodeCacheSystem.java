package com.wordpress.icemc.gsmcodes.model;

import android.content.Context;

import com.wordpress.icemc.gsmcodes.dao.CodeDao;
import com.wordpress.icemc.gsmcodes.listeners.GetCodesListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeCacheSystem {
    private GetCodesListener codesListener;
    private Context context;
    private final String operatorName;
    private final String tag;
    //Caching system
    private static HashMap<String, HashMap<String, List<CodeItem>>> codeItemCache = new HashMap<>();

    public CodeCacheSystem(Context context, String operatorName, String tag) {
        this.context = context;
        this.operatorName = operatorName;
        this.tag = tag;

    }

    public List<CodeItem> getCodeItems(){
        codesListener.onCodeLoaderStart();
        List<CodeItem> codes = new ArrayList<>();
        if (codeItemCache.containsKey(operatorName)) {
            HashMap<String, List<CodeItem>> codesHash = codeItemCache.get(operatorName);
            if (codesHash.containsKey(tag)) {
                codes = codesHash.get(tag);
            } else {
                CodeDao dao = new CodeDao(context);
                if (tag.equalsIgnoreCase(DefaultTags.ALL.toString())) {
                    //Get All codes for operator
                    List<Code> cds = dao.getCodesFromCursor(dao.getCodesCursorFromOperatorId(operatorName));
                    for(Code c: cds){
                        codes.add(new CodeItem(c, dao.getRandomMaterialColor("400")));
                    }
                    codesHash.put(DefaultTags.ALL.toString(), codes);
                } else if (tag.equalsIgnoreCase(DefaultTags.FAVORITE.toString())) {
                    //TODO Get Favorite code for operator
                } else {
                    //TODO get get Codes for operator with tag
                    //result = dao.getCodeItemsFromCursor(dao.)
                }
            }
        } else {
            HashMap<String, List<CodeItem>> codesHash = new HashMap<>();
            codeItemCache.put(operatorName, codesHash);
            CodeDao dao = new CodeDao(context);
            if (tag.equalsIgnoreCase(DefaultTags.ALL.toString())) {
                //Get All codes for operator
                List<Code> cds = dao.getCodesFromCursor(dao.getCodesCursorFromOperatorId(operatorName));
                for(Code c: cds){
                    codes.add(new CodeItem(c, dao.getRandomMaterialColor("400")));
                }
                codesHash.put(DefaultTags.ALL.toString(), codes);
            } else if (tag.equalsIgnoreCase(DefaultTags.FAVORITE.toString())) {
                //TODO Get Favorite code for operator
            } else {
                //TODO get get Codes for operator with tag
                //result = dao.getCodeItemsFromCursor(dao.)
            }
        }

        codesListener.onCodeLoaderFinished();
        return codes;

    }

    public void setCodesListener(GetCodesListener codesListener) {
        this.codesListener = codesListener;
    }

}
