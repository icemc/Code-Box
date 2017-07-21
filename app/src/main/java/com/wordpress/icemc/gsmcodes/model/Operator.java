package com.wordpress.icemc.gsmcodes.model;

/**
 * Created by Abanda on 6/19/2017.
 */
public class Operator {
    private String name;
    private int logo;
    private String description;
    private String tel;

    private Operator(Operator.Builder builder) {
        name = builder.name;
        logo = builder.logo;
        description = builder.description;
        tel = builder.tel;
    }

    public static class Builder {
        private String name;
        private int logo;
        private String description;
        private String tel;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder logoPath(int resId) {
            this.logo = resId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder phoneNumer(String tel) {
            this.tel = tel;
            return  this;
        }

        public Operator build() {
            return new Operator(this);
        }
    }

    public String getName() {
        return name;
    }

    public int getLogoPath() {
        return logo;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return tel;
    }

}
