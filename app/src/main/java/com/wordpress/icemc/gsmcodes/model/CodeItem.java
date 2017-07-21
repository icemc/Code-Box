package com.wordpress.icemc.gsmcodes.model;

public class CodeItem {
    private Code code;
    private int color;

    public  CodeItem(Code code, int color) {
        this.code = code;
        this.color = color;
    }
    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
