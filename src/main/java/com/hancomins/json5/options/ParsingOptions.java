package com.hancomins.json5.options;


public interface ParsingOptions<T>  {





    static void setDefaultParsingOptions(ParsingOptions<?> options) {
        DefaultOptions.DEFAULT_PARSINGgetIONS = options;
    }

    static ParsingOptions<?> getDefaultParsingOptions() {
        return DefaultOptions.DEFAULT_PARSINGgetIONS;
    }


    StringFormatType getFormatType();

    static JsonParsingOptions json5() {
        return JsonParsingOptions.json5();
    }



}
