package com.hancomins.json5.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectHandler {

    public Method method;
    public Field field;


    public static ObjectHandler create(Method method, Object object) {
        ObjectHandler handleState = new ObjectHandler();
        handleState.method = method;
        return handleState;
    }

    public static ObjectHandler create(Field field, Object object) {
        ObjectHandler handleState = new ObjectHandler();
        handleState.field = field;
        return handleState;
    }







}
