package com.wordpress.icemc.gsmcodes.model;

public class Tag
{
    
    private String name;
    private String operator;
    
    private Tag(Tag.Builder builder) {
        name = builder.name;
        operator = builder.operator;
    }
    
    public static class Builder {
        private String name;
        private String operator;
        
        public Builder name(String name){
            this.name = name;
            return this;
        }
        
        public Builder operator(String name){
            this.operator = name;
            return this;
        }
        
        public Tag build() {
            return new Tag(this);
        }
    }
    
    public String getName(){
        return name;
    }
    
    public String getOperator(){
        return operator;
    }
}
