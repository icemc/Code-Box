package com.wordpress.icemc.gsmcodes.model;

import java.util.List;

public class CodeWrapper {

    private Code code;
    private List<String> tags;

    public CodeWrapper(Code code, List<String> tags) {
        this.code = code;
        this.tags = tags;
    }

    public Code getCode() {
        return code;
    }

    public List<String> getTags() {
        return tags;
    }
}
