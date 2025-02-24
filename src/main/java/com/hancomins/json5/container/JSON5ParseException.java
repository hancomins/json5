package com.hancomins.json5.container;

import java.util.Locale;

public class JSON5ParseException extends RuntimeException {


    private static final boolean isKorean = Locale.getDefault().getLanguage().equals("ko");


    private int index = -1;
    private int line = -1;

    public JSON5ParseException() {

    }

    public JSON5ParseException(String message) {
        super(message);

    }

    public JSON5ParseException(String message, Throwable cause) {
        super(message, cause);

    }

    public JSON5ParseException(String message, int line, int index) {
        super(makeMessage(message, line, index));
        this.index = index;
        this.line = line;
    }

    public JSON5ParseException(String message, int line, int index, Throwable cause) {
        super(makeMessage(message, line, index), cause);
        this.index = index;
        this.line = line;
    }

    private static String makeMessage(String message, int line, int index) {
        if(isKorean) {
            return message +  "  (" + line + "번째 줄, " + (index - 1) + "번째 위치)";
        }
        return message + " (at line " + line + ", position " + (index - 1) + ")";
    }

}
