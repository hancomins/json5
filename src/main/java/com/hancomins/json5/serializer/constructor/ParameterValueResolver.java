package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.MissingValueStrategy;
import com.hancomins.json5.serializer.path.JSON5PathExtractor;

/**
 * 생성자 파라미터의 값을 해석하고 변환합니다.
 */
public class ParameterValueResolver {
    
    /**
     * 추출된 값을 대상 타입으로 변환합니다.
     * 
     * @param extractedValue JSON에서 추출된 원시 값
     * @param targetType 변환할 대상 타입
     * @param parameterInfo 파라미터 정보
     * @return 변환된 값
     * @throws JSON5SerializerException 변환 실패 또는 필수 값 누락 시
     */
    public Object resolveValue(Object extractedValue, Class<?> targetType, 
                             ParameterInfo parameterInfo) {
        
        // 값이 누락된 경우 처리
        if (JSON5PathExtractor.isMissingValue(extractedValue)) {
            return handleMissingValue(targetType, parameterInfo);
        }
        
        // null 값 처리
        if (extractedValue == null) {
            if (parameterInfo.isRequired()) {
                throw new JSON5SerializerException(
                    "Required parameter '" + parameterInfo.getJsonPath() + "' is null"
                );
            }
            return getDefaultValue(targetType);
        }
        
        // 타입 변환 수행
        return convertValue(extractedValue, targetType, parameterInfo);
    }
    
    /**
     * 타입에 따른 기본값을 반환합니다.
     * 
     * @param type 타입 클래스
     * @return 기본값
     */
    public Object getDefaultValue(Class<?> type) {
        if (type == null) {
            return null;
        }
        
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == char.class) return '\0';
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0d;
        }
        
        return null;
    }
    
    /**
     * 파라미터 정보에 따라 예외를 발생시켜야 하는지 확인합니다.
     * 
     * @param parameterInfo 파라미터 정보
     * @return 예외 발생 여부
     */
    public boolean shouldThrowException(ParameterInfo parameterInfo) {
        return parameterInfo.isRequired() || 
               parameterInfo.getMissingStrategy() == MissingValueStrategy.EXCEPTION;
    }
    
    /**
     * 누락된 값을 처리합니다.
     * 
     * @param targetType 대상 타입
     * @param parameterInfo 파라미터 정보
     * @return 처리된 값
     * @throws JSON5SerializerException 필수 값 누락 시
     */
    private Object handleMissingValue(Class<?> targetType, ParameterInfo parameterInfo) {
        if (shouldThrowException(parameterInfo)) {
            throw new JSON5SerializerException(
                "Required parameter '" + parameterInfo.getJsonPath() + 
                "' is missing from JSON"
            );
        }
        
        return getDefaultValue(targetType);
    }
    
    /**
     * 값을 대상 타입으로 변환합니다.
     * 
     * @param value 변환할 값
     * @param targetType 대상 타입
     * @param parameterInfo 파라미터 정보
     * @return 변환된 값
     * @throws JSON5SerializerException 변환 실패 시
     */
    private Object convertValue(Object value, Class<?> targetType, ParameterInfo parameterInfo) {
        try {
            // 이미 올바른 타입인 경우
            if (targetType.isInstance(value)) {
                return value;
            }
            
            // 기본 타입 변환
            if (targetType.isPrimitive() || isWrapperType(targetType)) {
                return convertPrimitiveValue(value, targetType);
            }
            
            // String 타입 변환
            if (targetType == String.class) {
                return value.toString();
            }
            
            // 다른 복합 타입의 경우, JSON5Element로 캐스팅하여 재귀적으로 처리
            // 이 부분은 나중에 JSON5Serializer와 통합할 때 구현
            return value;
            
        } catch (Exception e) {
            throw new JSON5SerializerException(
                "Failed to convert value for parameter '" + parameterInfo.getJsonPath() + 
                "': " + e.getMessage(), e
            );
        }
    }
    
    /**
     * 기본 타입 값을 변환합니다.
     * 
     * @param value 변환할 값
     * @param targetType 대상 타입
     * @return 변환된 값
     */
    private Object convertPrimitiveValue(Object value, Class<?> targetType) {
        String stringValue = value.toString();
        
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(stringValue);
        }
        if (targetType == char.class || targetType == Character.class) {
            return stringValue.length() > 0 ? stringValue.charAt(0) : '\0';
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return Byte.parseByte(stringValue);
        }
        if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(stringValue);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(stringValue);
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(stringValue);
        }
        if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(stringValue);
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(stringValue);
        }
        
        return value;
    }
    
    /**
     * 래퍼 타입인지 확인합니다.
     * 
     * @param type 확인할 타입
     * @return 래퍼 타입 여부
     */
    private boolean isWrapperType(Class<?> type) {
        return type == Boolean.class ||
               type == Character.class ||
               type == Byte.class ||
               type == Short.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Float.class ||
               type == Double.class;
    }
}
