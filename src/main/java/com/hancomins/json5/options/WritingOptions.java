package com.hancomins.json5.options;

public interface WritingOptions {


    static void setDefaultWritingOptions(WritingOptions options) {
        DefaultOptions.DEFAULT_WRITINGgetIONS = options;
    }

    static WritingOptions getDefaultWritingOptions() {
        return DefaultOptions.DEFAULT_WRITINGgetIONS;
    }


    static JSON5WriterOption json5() {
        return JSON5WriterOption.json5();
    }

    static JSON5WriterOption json5Pretty() {
        return JSON5WriterOption.prettyJson5();
    }

    static JSON5WriterOption json() {
        return JSON5WriterOption.json();
    }

    static JSON5WriterOption jsonPretty() {
        return JSON5WriterOption.prettyJson();
    }

}
