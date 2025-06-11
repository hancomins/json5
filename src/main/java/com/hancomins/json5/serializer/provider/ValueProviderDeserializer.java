package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.JSON5ValueConstructor;
import com.hancomins.json5.serializer.NullHandling;
import com.hancomins.json5.serializer.constructor.ConstructorInfo;
import com.hancomins.json5.util.DataConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 프리미티브 값을 값 공급자 객체로 역직렬화합니다.
 */
public class ValueProviderDeserializer {
    private final ValueProviderRegistry registry;
    
    public ValueProviderDeserializer(ValueProviderRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * 프리미티브 값에서 객체를 생성
     */
    public <T> T deserialize(Object value, Class<T> targetClass) {
        ValueProviderInfo info = registry.getValueProviderInfo(targetClass);
        
        if (info == null || !info.canDeserialize()) {
            throw new JSON5SerializerException(
                "No value constructor found for class: " + targetClass.getName());
        }
        
        return createObject(value, info, targetClass);
    }
    
    /**
     * 생성자를 호출하여 객체 생성
     */
    @SuppressWarnings("unchecked")
    private <T> T createObject(Object value, ValueProviderInfo info, Class<T> targetClass) {
        try {
            ConstructorInfo constructorInfo = info.getConstructor();
            Constructor<?> constructor = constructorInfo.getConstructor();
            
            Object parameterValue = prepareParameterValue(value, info, constructorInfo);
            
            return (T) constructor.newInstance(parameterValue);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new JSON5SerializerException(
                "Failed to create object using constructor: " + info.getConstructor().getConstructor(), e);
        }
    }
    
    /**
     * 생성자 파라미터 값 준비
     */
    private Object prepareParameterValue(Object value, ValueProviderInfo info, ConstructorInfo constructorInfo) {
        if (value == null) {
            JSON5ValueConstructor annotation = constructorInfo.getConstructor()
                .getAnnotation(JSON5ValueConstructor.class);
            NullHandling nullHandling = annotation.onNull();
            
            switch (nullHandling) {
                case EXCEPTION:
                    throw new JSON5SerializerException("Null value not allowed for constructor");
                case EMPTY_OBJECT:
                    return getDefaultValue(constructorInfo.getConstructor().getParameterTypes()[0]);
                case DEFAULT:
                default:
                    return null;
            }
        }
        
        Class<?> parameterType = constructorInfo.getConstructor().getParameterTypes()[0];
        
        // 엄격 모드에서는 타입이 정확히 일치해야 함
        if (info.isStrictTypeMatching()) {
            if (!parameterType.isAssignableFrom(value.getClass())) {
                throw new JSON5SerializerException(
                    String.format("Type mismatch in strict mode: expected %s but got %s",
                        parameterType.getName(), value.getClass().getName()));
            }
            return value;
        } else {
            // 느슨한 모드에서는 DataConverter를 사용하여 변환 시도
            Object result = DataConverter.convertSafely(parameterType, value, getDefaultValue(parameterType));
            if (result != null) {
                return result;
            }
            
            throw new JSON5SerializerException(
                String.format("Failed to convert %s to %s using DataConverter",
                    value.getClass().getName(), parameterType.getName()));
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
