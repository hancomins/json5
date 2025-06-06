package com.hancomins.json5;

import java.util.Locale;

public class ExceptionMessages {
    private static final int LOCALE_INDEX_EN = 0;
    private static final int LOCALE_INDEX_KO = 1;

    private static int localeIndex = 0;

    static {
        String locale = Locale.getDefault().getLanguage();
        if (locale.equals("ko")) {
            localeIndex = LOCALE_INDEX_KO;
        } else {
            localeIndex = LOCALE_INDEX_EN;
        }
    }


    private static final String[] OBJECT_VALUE_CONVERT_ERROR = {"JSON5Object['%s'] value is '%s' and cannot be converted to a %s type.", "JSON5Object['%s'] 값이 '%s'이며 %s 타입으로 변환할 수 없습니다."};
    private static final String[] ARRAY_VALUE_CONVERT_ERROR = {"JSON5Array[%d] value is '%s' and cannot be converted to a %s type.", "JSON5Array[%d] 값이 '%s'이며 %s 타입으로 변환할 수 없습니다."};
    private static final String[] ARRAY_INDEX_OUT_OF_BOUNDS = {"JSON5Array[%d] is out of bounds. The size of the JSON5Array is %d.", "JSON5Array[%d]가 범위를 벗어났습니다. JSON5Array의 크기는 %d입니다."};
    private static final String[] OBJECT_KEY_NOT_FOUND = {"JSON5Object['%s'] is not found.", "JSON5Object['%s']가 없습니다."};
    private static final String[] CTRL_CHAR_NOT_ALLOWED = {"Control character(%s) is not allowed.", "제어 문자(%s)가 허용되지 않습니다."};


    private static final String[] KEY_NOT_FOUND = {"Key not found in JSON string at line %d, position %d.", "문자열의 %d번째 줄 %d번째 위치에서 키를 찾을 수 없습니다."};


    public static final String[] STRING_READ_ERROR = {"An error occurred while reading the string.", "문자열을 읽는중 에러가 발생하였습니다."};

    // { 또는 [ 를 찾을 수 없습니다.
    public static final String[] JSON5_BRACKET_NOT_FOUND = {"Cannot find '{' or '[' in JSON string.", " '{' 또는 '['를 찾을 수 없습니다."};

    public static final String[] END_OF_STREAM = {"Unexpected end of stream", "예상치 못한 스트림의 끝입니다."};

    public static final String[] UNEXPECTED_TOKEN_LONG = {"Unexpected token.('%c') One of \"%s\" is expected.", "예상치 못한 토큰. ('%c') 다음 \"%s\" 중에 하나가 와야합니다."};

    public static final String[] UNEXPECTED_TOKEN = {"Unexpected token '%c'.", "예상치 못한 토큰 '%c'." };

    public static final String[] NOT_ALLOWED_COMMENT = {"Comment is not allowed.", "주석은 허용되지 않습니다."};

    public static final String[] NOT_ALLOWED_UNQUOTED_STRING = {"Unquoted string is not allowed.", "\" 또는 ' 없는 문자열은 허용되지 않습니다."};

    public static final String[] CONFLICT_KEY_VALUE_TYPE = {"Conflict detected for key '%s': Value is defined as both an object(%s) and a value(%s).", "키 'a'에 대한 충돌이 감지되었습니다: 값이 객체(%s)와 값(%s)으로 동시에 정의되었습니다."};


    public static String formatMessage(String[] message, Object... args) {
        String localeMessage = message[localeIndex];
        return String.format(localeMessage, args);
    }


    static String getJSON5ObjectValueConvertError(String key, Object value, String type) {
        return String.format(OBJECT_VALUE_CONVERT_ERROR[localeIndex], key, value, type);
    }

    static String getJSON5ArrayValueConvertError(int index, Object value, String type) {
        return String.format(ARRAY_VALUE_CONVERT_ERROR[localeIndex], index, value, type);
    }

    static String getJSON5ArrayIndexOutOfBounds(int index, int size) {
        return String.format(ARRAY_INDEX_OUT_OF_BOUNDS[localeIndex], index, size);
    }

    static String getJSON5ObjectKeyNotFound(String key) {
        return String.format(OBJECT_KEY_NOT_FOUND[localeIndex], key);
    }

    static String getKeyNotFound(int line, int position) {
        return String.format(KEY_NOT_FOUND[localeIndex], line, position);
    }

    public static String getCtrlCharNotAllowed(char c) {
        // 컨트롤 문자를 String 표현으로 처리. 예를 들어 \n 은 \\n 으로 표시
        String ctrl = c == '\n' ? "\\n" : c == '\r' ? "\\r" : c == '\t' ? "\\t" : c == '\b' ? "\\b" : c == '\f' ? "\\f" : c == '\u000B' ? "\\v" : "\\x" + Integer.toHexString(c);
        return String.format(CTRL_CHAR_NOT_ALLOWED[localeIndex], ctrl);
    }





}
