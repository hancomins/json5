package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Exception;

public class JSON5SerializerException extends JSON5Exception {
    public JSON5SerializerException(String message) {
        super(message);
    }

    public JSON5SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSON5SerializerException(Throwable cause) {
        super(cause);
    }
}
