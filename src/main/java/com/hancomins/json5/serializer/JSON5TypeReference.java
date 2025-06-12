package com.hancomins.json5.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Jackson의 TypeReference와 유사한 제네릭 타입 정보 보존 클래스
 * 
 * 사용법:
 * new JSON5TypeReference<Map<UserRole, List<String>>>() {}
 */
public abstract class JSON5TypeReference<T> {
    private final Type type;
    
    protected JSON5TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        } else {
            throw new JSON5SerializerException("TypeReference must be parameterized");
        }
    }
    
    public Type getType() {
        return type;
    }
    
    /**
     * 타입이 Map인지 확인
     */
    public boolean isMapType() {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return Map.class.isAssignableFrom((Class<?>) rawType);
        }
        return false;
    }
    
    /**
     * 타입이 Collection인지 확인
     */
    public boolean isCollectionType() {
        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return Collection.class.isAssignableFrom((Class<?>) rawType);
        }
        return false;
    }
    
    /**
     * Map 타입 정보를 분석하여 Key, Value 타입을 추출
     */
    public MapTypeInfo analyzeMapType() {
        if (!isMapType()) {
            throw new JSON5SerializerException("Type is not a parameterized Map: " + type);
        }
        
        ParameterizedType paramType = (ParameterizedType) type;
        Type[] args = paramType.getActualTypeArguments();
        
        if (args.length != 2) {
            throw new JSON5SerializerException("Map must have exactly 2 type arguments");
        }
        
        return new MapTypeInfo(args[0], args[1]);
    }
    
    /**
     * Collection 타입 정보를 분석하여 요소 타입을 추출
     */
    public CollectionTypeInfo analyzeCollectionType() {
        if (!isCollectionType()) {
            throw new JSON5SerializerException("Type is not a parameterized Collection: " + type);
        }
        
        ParameterizedType paramType = (ParameterizedType) type;
        Type[] args = paramType.getActualTypeArguments();
        
        if (args.length != 1) {
            throw new JSON5SerializerException("Collection must have exactly 1 type argument");
        }
        
        Class<?> rawType = (Class<?>) paramType.getRawType();
        return new CollectionTypeInfo(rawType, args[0]);
    }
    
    @Override
    public String toString() {
        return "JSON5TypeReference{type=" + type + "}";
    }
}
