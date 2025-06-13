package com.hancomins.json5.serializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Collection의 타입 정보를 담는 클래스
 */
public class CollectionTypeInfo {
    private final Class<?> collectionClass;
    private final Type elementType;
    
    public CollectionTypeInfo(Class<?> collectionClass, Type elementType) {
        this.collectionClass = collectionClass;
        this.elementType = elementType;
    }
    
    public Class<?> getCollectionClass() {
        return collectionClass;
    }
    
    public Type getElementType() {
        return elementType;
    }
    
    public Class<?> getElementClass() {
        if (elementType instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) elementType).getRawType();
        }
        return (Class<?>) elementType;
    }
    
    @Override
    public String toString() {
        return "CollectionTypeInfo{collectionClass=" + collectionClass.getSimpleName() + 
               ", elementType=" + elementType + "}";
    }
}
