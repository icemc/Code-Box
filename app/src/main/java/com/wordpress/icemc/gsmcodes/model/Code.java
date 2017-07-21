package com.wordpress.icemc.gsmcodes.model;

public class Code
{
    private String code;
    private String operator;
    private String name;
    private String description;
    private boolean isFavourite;
    private InputField[] inputFields;
    
    private Code(Code.Builder builder){
        code = builder.code;
        operator = builder.operator;
        name = builder.name;
        description = builder.description;
        this.isFavourite = builder.isFavourite;
        inputFields = builder.inputFields;
    }

    public static class Builder{
        private String code;
        private String operator;
        private String name;
        private String description;
        private boolean isFavourite;
        private InputField[] inputFields;

        
        public Builder() {
        }

        public Builder(Code code) {
            code(code.getCode())
                    .operator(code.getOperator())
                    .name(code.getName())
                    .description(code.getDescription())
                    .isFavourite(code.isFavourite())
                    .inputFields(code.getInputFields());
        }
        public Builder code(String code) {
            this.code = code; 
            return this;
        }
        
        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public Builder isFavourite(boolean fav) {
            isFavourite = fav;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        
        public Builder description(String description){
            this.description = description;
            return this;
        }
        
        public Builder inputFields(InputField[] inputFields){
            this.inputFields = inputFields;
            return this;
        }
        
        public Code build(){
            return new Code(this);
        }
    }
    
    public String getCode () {
        return code;
    }

    public String getOperator () {
        return operator;
    }

    public String getName () {
        return name;
    }

    public String getDescription () {
        return description;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public InputField[] getInputFields () {
        return inputFields;
    }
    
}
