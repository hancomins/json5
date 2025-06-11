package com.hancomins.json5.options;

public class JSON5WriterOption  implements WritingOptions {
    private boolean pretty = false;
    private boolean skipComments = false;
    private boolean isUnprettyArray = false;
    private int space = 4;
    private String depthString = "    ";

    private String keyQuote = "\"";
    private String valueQuote = "\"";


    private boolean allowUnquoted = false;
    private boolean allowSingleQuotes = false;
    private boolean isAllowLineBreak = false;


    public JSON5WriterOption() {
    }


    public static JSON5WriterOption json5() {
        JSON5WriterOption JSON5WriterOption = new JSON5WriterOption();
        JSON5WriterOption.setPretty(false);
        JSON5WriterOption.setUnprettyArray(false);
        JSON5WriterOption.setSpace(4);
        JSON5WriterOption.setKeyQuote("");
        JSON5WriterOption.setValueQuote("'");
        JSON5WriterOption.setAllowUnquoted(true);
        JSON5WriterOption.setAllowSingleQuotes(true);
        JSON5WriterOption.setAllowLineBreak(true);
        return JSON5WriterOption;
    }

    public static JSON5WriterOption json() {
        JSON5WriterOption JSON5WriterOption = new JSON5WriterOption();
        JSON5WriterOption.setPretty(false);
        JSON5WriterOption.setUnprettyArray(false);
        JSON5WriterOption.setSpace(4);
        JSON5WriterOption.setKeyQuote("\"");
        JSON5WriterOption.setValueQuote("'");
        JSON5WriterOption.setAllowUnquoted(false);
        JSON5WriterOption.setAllowSingleQuotes(false);
        JSON5WriterOption.setAllowLineBreak(false);
        JSON5WriterOption.setSkipComments(true);
        return JSON5WriterOption;
    }

    public static JSON5WriterOption prettyJson() {
        JSON5WriterOption JSON5WriterOption = json();
        JSON5WriterOption.setPretty(true);
        JSON5WriterOption.setSpace(4);
        JSON5WriterOption.setUnprettyArray(false);
        return JSON5WriterOption;
    }

    public static JSON5WriterOption prettyJson5() {
        JSON5WriterOption JSON5WriterOption = json5();
        JSON5WriterOption.setPretty(true);
        JSON5WriterOption.setSpace(4);
        JSON5WriterOption.setUnprettyArray(false);
        return JSON5WriterOption;
    }





    public boolean isSkipComments() {
        return skipComments;
    }

    public JSON5WriterOption setSkipComments(boolean skipComments) {
        this.skipComments = skipComments;
        return this;
    }

    public boolean isPretty() {
        return pretty;
    }

    public JSON5WriterOption setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    public boolean isUnprettyArray() {
        return isUnprettyArray;
    }

    public JSON5WriterOption setUnprettyArray(boolean isUnprettyArray) {
        this.isUnprettyArray = isUnprettyArray;
        return this;
    }

    public int getSpace() {
        return space;
    }

    public JSON5WriterOption setSpace(int space) {
        if(space == this.space) return this;
        if(space < 1) space = 1; // minimum space is 1
        else if(space > 10) space = 10; // maximum space is 8
        this.space = space;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < space; i++) {
            sb.append(' ');
        }
        this.depthString = sb.toString();
        return this;
    }



    public String getDepthString() {
        return depthString;
    }


    public String getKeyQuote() {
        return keyQuote;
    }

    public JSON5WriterOption setKeyQuote(String keyQuote) {
        this.keyQuote = keyQuote;
        return this;
    }

    public String getValueQuote() {
        return valueQuote;
    }

    public JSON5WriterOption setValueQuote(String valueQuote) {
        if(valueQuote.length() > 1)
            throw new IllegalArgumentException("valueQuote can not be more than one character");
        this.valueQuote = valueQuote;
        return this;
    }

    public boolean isAllowUnquoted() {
        return allowUnquoted;
    }

    public JSON5WriterOption setAllowUnquoted(boolean allowUnquoted) {
        this.allowUnquoted = allowUnquoted;
        return this;
    }

    public boolean isAllowSingleQuotes() {
        return allowSingleQuotes;
    }

    public JSON5WriterOption setAllowSingleQuotes(boolean allowSingleQuotes) {
        this.allowSingleQuotes = allowSingleQuotes;
        return this;
    }

    public boolean isAllowLineBreak() {
        return isAllowLineBreak;
    }

    public JSON5WriterOption setAllowLineBreak(boolean allowLineBreak) {
        this.isAllowLineBreak = allowLineBreak;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JSON5WriterOption that = (JSON5WriterOption) obj;
        return pretty == that.pretty &&
               skipComments == that.skipComments &&
               isUnprettyArray == that.isUnprettyArray &&
               space == that.space &&
               allowUnquoted == that.allowUnquoted &&
               allowSingleQuotes == that.allowSingleQuotes &&
               isAllowLineBreak == that.isAllowLineBreak &&
               java.util.Objects.equals(keyQuote, that.keyQuote) &&
               java.util.Objects.equals(valueQuote, that.valueQuote) &&
               java.util.Objects.equals(depthString, that.depthString);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(pretty, skipComments, isUnprettyArray, space, 
                                     keyQuote, valueQuote, allowUnquoted, allowSingleQuotes, 
                                     isAllowLineBreak, depthString);
    }

    @Override
    public String toString() {
        return "JSON5WriterOption{" +
               "pretty=" + pretty +
               ", skipComments=" + skipComments +
               ", isUnprettyArray=" + isUnprettyArray +
               ", space=" + space +
               ", keyQuote='" + keyQuote + '\'' +
               ", valueQuote='" + valueQuote + '\'' +
               ", allowUnquoted=" + allowUnquoted +
               ", allowSingleQuotes=" + allowSingleQuotes +
               ", isAllowLineBreak=" + isAllowLineBreak +
               "}";
    }

}
