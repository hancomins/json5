package com.hancomins.json5.serializer;

public class JSON5ObjectException extends RuntimeException {

    public JSON5ObjectException(String message) {
        super(message);
    }

    public JSON5ObjectException(String message, Exception cause) {
        super(message,cause);
    }
}
