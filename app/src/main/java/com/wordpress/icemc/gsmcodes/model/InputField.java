package com.wordpress.icemc.gsmcodes.model;

public class InputField {
    private InputType inputType;
    private String title;
    
    public InputField(){
        this(InputType.NONE, "");
    }
    
    public InputField(InputType type, String title){
        this.title = title; 
        this.inputType = type;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public InputType getInputType() {
        return inputType;
    }

    public String getTitle() {
        return title;
    }
}
