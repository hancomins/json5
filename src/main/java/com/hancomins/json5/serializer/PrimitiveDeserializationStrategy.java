package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.util.DataConverter;

/**
 * 기본 타입의 역직렬화를 담당하는 전략 클래스
 * 
 * 기본형(primitive), 래퍼형(wrapper), String, enum 등 단순한 타입의 
 * 역직렬화를 처리합니다.
 */
public class PrimitiveDeserializationStrategy implements DeserializationStrategy {
    
    @Override
    public boolean canHandle(Types type, Class<?> targetType) {
        return Types.isSingleType(type) || 
               String.class.equals(targetType) ||
               targetType.isEnum() ||
               type == Types.ByteArray;
    }
    
    @Override
    public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (json5Element == null) {
            return getDefaultValue(targetType);
        }
        
        // JSON5Object나 JSON5Array에서 값 추출
        Object value = extractValue(json5Element, targetType);
        
        if (value == null) {
            return getDefaultValue(targetType);
        }
        
        // enum 타입 처리
        if (targetType.isEnum()) {
            return convertToEnum(value, targetType);
        }
        
        // 기본 타입 변환
        return convertPrimitiveValue(value, targetType);
    }
    
    @Override
    public int getPriority() {
        return 10; // 높은 우선순위
    }
    
    /**
     * JSON5Element에서 값을 추출합니다.
     */
    private Object extractValue(JSON5Element json5Element, Class<?> targetType) {
        if (json5Element instanceof JSON5Object) {
            JSON5Object json5Object = (JSON5Object) json5Element;
            // JSON5Object에서 $value 키로 저장된 값 추출
            return json5Object.get("$value");
        } else if (json5Element instanceof JSON5Array) {
            JSON5Array json5Array = (JSON5Array) json5Element;
            // 배열의 첫 번째 요소 반환
            return json5Array.size() > 0 ? json5Array.get(0) : null;
        }
        return null;
    }
    
    /**
     * enum 타입으로 변환합니다.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertToEnum(Object value, Class<?> targetType) {
        try {
            if (value instanceof String) {
                return Enum.valueOf((Class<Enum>) targetType, (String) value);
            } else {
                return Enum.valueOf((Class<Enum>) targetType, value.toString());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 기본 타입으로 변환합니다.
     */
    private Object convertPrimitiveValue(Object value, Class<?> targetType) {
        try {
            // byte 배열 처리
            if (targetType == byte[].class || targetType == Byte[].class) {
                return DataConverter.toByteArray(value);
            }
            
            // 기본형 및 래퍼형 처리
            if (targetType == boolean.class || targetType == Boolean.class) {
                return DataConverter.toBoolean(value, false);
            } else if (targetType == byte.class || targetType == Byte.class) {
                return DataConverter.toByte(value, (byte) 0);
            } else if (targetType == short.class || targetType == Short.class) {
                return DataConverter.toShort(value, (short) 0);
            } else if (targetType == int.class || targetType == Integer.class) {
                return DataConverter.toInteger(value, 0);
            } else if (targetType == long.class || targetType == Long.class) {
                return DataConverter.toLong(value, 0L);
            } else if (targetType == float.class || targetType == Float.class) {
                return DataConverter.toFloat(value, 0.0f);
            } else if (targetType == double.class || targetType == Double.class) {
                return DataConverter.toDouble(value, 0.0);
            } else if (targetType == char.class || targetType == Character.class) {
                return DataConverter.toChar(value, '\0');
            } else if (targetType == String.class) {
                return DataConverter.toString(value);
            }
            
            return value;
        } catch (Exception e) {
            return getDefaultValue(targetType);
        }
    }
    
    /**
     * 타입별 기본값을 반환합니다.
     */
    private Object getDefaultValue(Class<?> targetType) {
        if (targetType == boolean.class) return false;
        if (targetType == byte.class) return (byte) 0;
        if (targetType == short.class) return (short) 0;
        if (targetType == int.class) return 0;
        if (targetType == long.class) return 0L;
        if (targetType == float.class) return 0.0f;
        if (targetType == double.class) return 0.0;
        if (targetType == char.class) return '\0';
        
        return null; // 래퍼형 및 기타 타입은 null
    }
}
