package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.container.ArrayDataContainer;
import com.hancomins.json5.container.BaseDataContainer;

enum Types {
    Byte,
    Short,
    Integer,
    Long,
    Float,
    Double,
    Boolean,
    Character,
    String,
    ByteArray,
    AbstractObject,
    Object,
    Map,
    BigDecimal,
    BigInteger,
    JSON5Element,
    JSON5Object,
    JSON5Array,
    Collection,
    GenericType;



    static boolean isPrimitivableType(Types type) {
        return type == Byte || type == Short || type == Integer || type == Long || type == Float || type == Double || type == Boolean || type == Character;
    }

    static boolean isSingleType(Types type) {
        return type == Byte || type == Short || type == Integer || type == Long || type == Float || type == Double || type == Boolean || type == Character || type == String || type == ByteArray || type == BigDecimal || type == BigInteger;
    }

    static boolean isJSON5Type(Types type) {
        return type == JSON5Element || type == JSON5Object || type == JSON5Array;
    }




    static Types of(Class<?> type) {
        /*if(type.isAnonymousClass()) {
            Class<?> superClass = type.getSuperclass();
            if(superClass != null && superClass != Object.class) {
                return of(superClass);
            }
            else if(type.getInterfaces().length > 0) {
                return of(type.getInterfaces()[0]);
            }
        }*/

        if(type == byte.class || type == Byte.class) {
            return Byte;
        } else if(type == short.class || type == Short.class) {
            return Short;
        } else if(type == int.class || type == Integer.class) {
            return Integer;
        } else if(type == long.class || type == Long.class) {
            return Long;
        } else if(type == float.class || type == Float.class) {
            return Float;
        } else if(type == double.class || type == Double.class) {
            return Double;
        } else if(type == java.math.BigDecimal.class) {
            return BigDecimal;
        } else if(JSON5Object.class.isAssignableFrom(type) || JSON5Object.class.isAssignableFrom(type)) {
            return JSON5Object;
        } else if(JSON5Array.class.isAssignableFrom(type) || ArrayDataContainer.class.isAssignableFrom(type)) {
            return JSON5Array;
        } else if(JSON5Element.class.isAssignableFrom(type) || BaseDataContainer.class.isAssignableFrom(type)) {
            return JSON5Element;
        }
        else if(type == boolean.class || type == Boolean.class) {
            return Boolean;
        } else if(java.util.Map.class.isAssignableFrom(type)) {
            return Map;
        }
        else if(type == char.class || type == Character.class) {
            return Character;
        } else if(type == String.class || type.isEnum()) {
            return String;
        } else if(type == byte[].class ) {
            return ByteArray;
        } else if(java.util.Collection.class.isAssignableFrom(type)) {
            return Collection;
        } else if(type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
            return AbstractObject;
        } else {
            return Object;
        }


    }

}
