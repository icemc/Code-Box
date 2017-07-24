package com.wordpress.icemc.gsmcodes.model;

/**
 * Created by Bomen on 7/5/2017.
 */
public enum SupportedOperators {
    MTN_CAMEROON, ORANGE_CAMEROON, CAMTEL, NEXTTEL;

    public String toString() {
        switch(this) {
            case MTN_CAMEROON:
                return "MTN Cameroon";
            case ORANGE_CAMEROON:
                return "Orange Cameroon";
            case CAMTEL:
                return "Camtel";
            case NEXTTEL:
                return "Nexttel";
        }
        throw new IllegalStateException("Unhandled InputType value.");
    }

    public static SupportedOperators fromString(String toStringValue) {
        SupportedOperators[] types = SupportedOperators.values();
        for (SupportedOperators t : types) {
            if (t.toString().equals(toStringValue)) {
                return t;
            }
        }
        return null;
    }
}
