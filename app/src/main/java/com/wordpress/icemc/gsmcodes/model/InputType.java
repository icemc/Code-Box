package com.wordpress.icemc.gsmcodes.model;

public enum InputType
{
    NONE, DEFAULT, PHONE_NUMBER, PASS_CODE;
    
    public String toString() {
        switch(this) {
            case NONE: 
                return "None";
            case DEFAULT:
               return "Default";
            case PHONE_NUMBER:
                return "Phone number";
            case PASS_CODE:
                return "Pass code";
        }
        throw new IllegalStateException("Unhandled InputType value.");
    }
    
    public static InputType fromString(String toStringValue) {
        InputType[] types = InputType.values();
        for (InputType t : types) {
            if (t.toString().equalsIgnoreCase(toStringValue)) {
                return t;
            }
        }
        return null;
    }
}
