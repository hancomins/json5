package com.hancomins.json5.options;


public interface ParsingOptions<T>  {





    static void setDefaultParsingOptions(ParsingOptions<?> options) {
        DefaultOptions.DEFAULT_PARSING = options;
    }

    static ParsingOptions<?> getDefaultParsingOptions() {
        return DefaultOptions.DEFAULT_PARSING;
    }


    StringFormatType getFormatType();

    static JsonParsingOptions json5() {
        return JsonParsingOptions.json5();
    }



}
