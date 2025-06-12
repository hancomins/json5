package com.hancomins.json5.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Map의 타입 정보를 담는 클래스
 */
public class MapTypeInfo {
    private final Type keyType;
    private final Type valueType;
    
    public MapTypeInfo(Type keyType, Type valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }
    
    public Type getKeyType() {
        return keyType;
    }
    
    public Type getValueType() {
        return valueType;
    }
    
    public Class<?> getKeyClass() {
        return (Class<?>) keyType;
    }
    
    public Class<?> getValueClass() {
        if (valueType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) valueType).getRawType();
        }
        return (Class<?>) valueType;
    }
    
    /**
     * Value가 Collection인지 확인
     */
    public boolean isValueCollection() {
        Class<?> valueClass = getValueClass();
        return Collection.class.isAssignableFrom(valueClass);
    }
    
    /**
     * Value가 Collection인 경우 요소 타입 반환
     */
    public Type getValueElementType() {
        if (valueType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) valueType;
            if (Collection.class.isAssignableFrom((Class<?>) paramType.getRawType())) {
                return paramType.getActualTypeArguments()[0];
            }
        }
        return null;
    }
    
    /**
     * Value가 중첩 Map인지 확인
     */
    public boolean isValueNestedMap() {
        if (valueType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) valueType).getRawType();
            return java.util.Map.class.isAssignableFrom((Class<?>) rawType);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "MapTypeInfo{keyType=" + keyType + ", valueType=" + valueType + "}";
    }
}
