package com.wordpress.icemc.gsmcodes.model;

/**
 * Created by Abanda on 6/19/2017.
 */
public class TagMap {
        private String codeId;
        private String tagId;

        private TagMap(TagMap.Builder builder) {
            codeId = builder.codeId;
            tagId = builder.tagId;
        }

        public static class Builder{
            private String codeId;
            private String tagId;

            public Builder codeId(String code) {
                codeId = code;
                return this;
            }

            public Builder tagId(String tag) {
                tagId = tag;
                return this;
            }

            public TagMap build() {
                return new TagMap(this);
            }
        }

        public String getCodeId() {
            return codeId;
        }

        public String getTagId() {
            return tagId;
        }
}
