package com.hancomins.json5.serializer;

import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.serializer.provider.ValueProviderRegistry;
import com.hancomins.json5.serializer.provider.ValueProviderSerializer;
import com.hancomins.json5.serializer.provider.ValueProviderDeserializer;

/**
 * Map의 Key 타입 변환을 처리하는 클래스
 * 기존 시스템을 최대한 재활용하여 구현
 */
public class MapKeyConverter {
    
    private static final ValueProviderRegistry VALUE_PROVIDER_REGISTRY = new ValueProviderRegistry();
    private static final ValueProviderSerializer VALUE_PROVIDER_SERIALIZER = 
        new ValueProviderSerializer(VALUE_PROVIDER_REGISTRY);
    private static final ValueProviderDeserializer VALUE_PROVIDER_DESERIALIZER = 
        new ValueProviderDeserializer(VALUE_PROVIDER_REGISTRY);
    
    /**
     * Key 객체를 String으로 변환 (직렬화 시)
     */
    public static String convertKeyToString(Object key) {
        if (key == null) {
            return null;
        }
        
        if (key instanceof String) {
            return (String) key;
        }
        
        // 1. enum 처리 (DataConverter 재활용)
        if (key.getClass().isEnum()) {
            return key.toString(); // enum.name() 반환
        }
        
        // 2. primitive/wrapper 타입 처리 (DataConverter 재활용)
        if (isPrimitiveOrWrapper(key.getClass())) {
            return DataConverter.toString(key);
        }
        
        // 3. @JSON5ValueProvider 처리 (기존 시스템 재활용)
        if (VALUE_PROVIDER_REGISTRY.isValueProvider(key.getClass())) {
            try {
                Object serializedValue = VALUE_PROVIDER_SERIALIZER.serialize(key);
                return DataConverter.toString(serializedValue);
            } catch (Exception e) {
                CatchExceptionProvider.getInstance().catchException(
                    "Failed to serialize key using ValueProvider: " + key.getClass().getName(), e);
                return key.toString(); // fallback
            }
        }
        
        // 4. fallback - toString()
        return key.toString();
    }
    
    /**
     * String을 Key 객체로 변환 (역직렬화 시)
     */
    @SuppressWarnings("unchecked")
    public static <K> K convertStringToKey(String keyStr, Class<K> keyType) {
        if (keyStr == null) {
            return null;
        }
        
        if (keyType == String.class) {
            return (K) keyStr;
        }
        
        // 1. enum 처리 (DataConverter 재활용)
        if (keyType.isEnum()) {
            @SuppressWarnings("rawtypes")
            Enum enumValue = DataConverter.toEnum((Class<? extends Enum>) keyType, keyStr);
            return (K) enumValue;
        }
        
        // 2. primitive/wrapper 타입 처리 (DataConverter 재활용)
        if (isPrimitiveOrWrapper(keyType)) {
            Object converted = DataConverter.convertValue(keyType, keyStr);
            return (K) converted;
        }
        
        // 3. @JSON5ValueProvider 처리 (기존 시스템 재활용)
        if (VALUE_PROVIDER_REGISTRY.isValueProvider(keyType)) {
            try {
                return VALUE_PROVIDER_DESERIALIZER.deserialize(keyStr, keyType);
            } catch (Exception e) {
                CatchExceptionProvider.getInstance().catchException(
                    "Failed to deserialize key using ValueProvider: " + keyType.getName(), e);
                return null;
            }
        }
        
        // 4. 지원하지 않는 타입
        throw new JSON5SerializerException(
            "Unsupported Map key type: " + keyType.getName() + 
            ". Supported types: String, enum, primitive types, @JSON5ValueProvider classes");
    }
    
    /**
     * Key 타입이 지원되는지 확인
     */
    public static boolean isSupportedKeyType(Class<?> keyType) {
        if (keyType == String.class) {
            return true;
        }
        
        if (keyType.isEnum()) {
            return true;
        }
        
        if (isPrimitiveOrWrapper(keyType)) {
            return true;
        }
        
        if (VALUE_PROVIDER_REGISTRY.isValueProvider(keyType)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * primitive 또는 wrapper 타입인지 확인
     */
    private static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
               type == Integer.class || type == Long.class || type == Double.class ||
               type == Float.class || type == Boolean.class || type == Character.class ||
               type == Byte.class || type == Short.class;
    }
}
