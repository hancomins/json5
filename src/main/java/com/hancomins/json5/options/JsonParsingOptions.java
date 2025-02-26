package com.hancomins.json5.options;


@SuppressWarnings("UnusedReturnValue")
public class JsonParsingOptions implements ParsingOptions<JsonParsingOptions> {

        private StringFormatType formatType = StringFormatType.JSON5;

        private boolean allowUnquoted = true;
        private boolean allowComments = true;
        private boolean ignoreTrailingData = false;
        private boolean skipComments = false;
        private boolean ignoreControlCharacters = true;
        private boolean isAllowControlCharacters = true;

        private JsonParsingOptions() {
        }


        public static JsonParsingOptions json5() {
            JsonParsingOptions jsonParsingOptions = new JsonParsingOptions();
            jsonParsingOptions.setAllowUnquoted(true);
            jsonParsingOptions.setAllowComments(true);
            jsonParsingOptions.formatType = StringFormatType.JSON5;
            return jsonParsingOptions;
        }


        public JsonParsingOptions setAllowControlCharacters(boolean allowControlCharacters) {
            isAllowControlCharacters = allowControlCharacters;
            return this;
        }

        public boolean isAllowControlCharacters() {
            return isAllowControlCharacters;
        }




        public boolean isAllowUnquoted() {
            return allowUnquoted;
        }

        public boolean isAllowComments() {
            return allowComments;
        }

        public JsonParsingOptions setAllowUnquoted(boolean allowUnquoted) {
            this.allowUnquoted = allowUnquoted;
            return this;
        }

        public JsonParsingOptions setSkipComments(boolean skipComments) {
            this.skipComments = skipComments;
            return this;
        }

        public boolean isSkipComments() {
            return skipComments;
        }


        public boolean isIgnoreControlCharacters() {
            return ignoreControlCharacters;
        }

        public JsonParsingOptions setIgnoreControlCharacters(boolean ignoreControlCharacters) {
            this.ignoreControlCharacters = ignoreControlCharacters;
            return this;
        }


        public JsonParsingOptions setAllowComments(boolean allowComments) {
            this.allowComments = allowComments;
            return this;
        }


        public boolean isIgnoreTrailingData() {
            return ignoreTrailingData;
        }

        public JsonParsingOptions setIgnoreTrailingData(boolean ignoreTrailingData) {
            this.ignoreTrailingData = ignoreTrailingData;
            return this;
        }



    @Override
    public StringFormatType getFormatType() {
        return formatType;
    }
}
