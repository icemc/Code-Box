package com.wordpress.icemc.gsmcodes.model;

public enum DefaultTags {
    ALL, FAVORITE;

    @Override
    public String toString() {
        switch (this) {
            case ALL:
                return "All";
            case FAVORITE:
                return "Favourite";
        }
        throw new IllegalStateException("Unhandled DefaultTag value.");
    }
}
