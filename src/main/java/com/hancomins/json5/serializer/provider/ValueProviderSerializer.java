package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.NullHandling;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 값 공급자 객체를 프리미티브 값으로 직렬화합니다.
 */
public class ValueProviderSerializer {
    private final ValueProviderRegistry registry;
    
    public ValueProviderSerializer(ValueProviderRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * 객체를 프리미티브 값으로 변환
     */
    public Object serialize(Object object) {
        if (object == null) {
            return null;
        }
        
        Class<?> clazz = object.getClass();
        ValueProviderInfo info = registry.getValueProviderInfo(clazz);
        
        if (info == null || !info.canSerialize()) {
            throw new JSON5SerializerException(
                "No value extractor found for class: " + clazz.getName());
        }
        
        return extractValue(object, info.getExtractor());
    }
    
    /**
     * 추출자 메서드를 호출하여 값을 추출
     */
    private Object extractValue(Object object, ExtractorInfo extractor) {
        try {
            Method method = extractor.getMethod();
            Object result = method.invoke(object);
            
            if (result == null) {
                NullHandling nullHandling = extractor.getNullHandling();
                switch (nullHandling) {
                    case EXCEPTION:
                        throw new JSON5SerializerException(
                            "Null value not allowed for extractor: " + method.getName());
                    case EMPTY_OBJECT:
                        return getDefaultValue(extractor.getReturnType());
                    case DEFAULT:
                    default:
                        return null;
                }
            }
            
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new JSON5SerializerException(
                "Failed to extract value using method: " + extractor.getMethod().getName(), e);
        }
    }
    
    /**
     * 타입별 기본값 반환
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == String.class) return "";
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == float.class || type == Float.class) return 0.0f;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == byte.class || type == Byte.class) return (byte) 0;
        if (type == short.class || type == Short.class) return (short) 0;
        if (type == char.class || type == Character.class) return '\0';
        if (type == byte[].class) return new byte[0];
        return null;
    }
}
