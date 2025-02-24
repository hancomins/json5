package com.hancomins.json5;

public class JSON5Exception extends RuntimeException{
    private static final long serialVersionUID = 0;

    public JSON5Exception(final String message) {
        super(message);
    }

    public JSON5Exception(int index, Object obj, String typeName) {
        super(ExceptionMessages.getJSON5ArrayValueConvertError(index, obj, typeName));
    }
    public JSON5Exception(int index, Object obj, String typeName, Throwable e) {
        super(ExceptionMessages.getJSON5ArrayValueConvertError(index, obj, typeName), e);
    }

    public JSON5Exception(String key, Object obj, String typeName) {
        super(ExceptionMessages.getJSON5ObjectValueConvertError(key, obj, typeName));
    }

    public JSON5Exception(String key, Object obj, String typeName, Throwable e) {
        super(ExceptionMessages.getJSON5ObjectValueConvertError(key, obj, typeName), e);
    }

    public JSON5Exception(final String message, final Throwable cause) {
        super(message, cause);
    }


    public JSON5Exception(final Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
