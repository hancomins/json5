package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.MissingTypeStrategy;
import com.hancomins.json5.serializer.TypeInclusion;
import com.hancomins.json5.serializer.path.JSON5PathExtractor;

/**
 * JSON에서 구체적인 타입을 결정합니다.
 */
public class TypeResolver {
    
    /**
     * JSON 객체에서 타입을 해결합니다.
     * @param json5Object JSON 객체
     * @param typeInfo 타입 정보
     * @return 구체적인 클래스
     * @throws JSON5SerializerException 타입을 결정할 수 없는 경우
     */
    public Class<?> resolveType(JSON5Object json5Object, TypeInfo typeInfo) {
        // 1. 타입 정보 추출 시도
        String typeValue = extractTypeValue(json5Object, typeInfo);
        
        if (typeValue != null) {
            // 2. 타입 이름으로 구체적인 클래스 찾기
            Class<?> concreteType = typeInfo.getConcreteType(typeValue);
            if (concreteType != null) {
                return concreteType;
            }
        }
        
        // 3. 타입 정보가 없거나 매칭되지 않는 경우
        return handleMissingType(typeInfo, typeValue);
    }
    
    /**
     * JSON에서 타입 정보가 있는지 확인합니다.
     * @param json5Object JSON 객체
     * @param typeInfo 타입 정보
     * @return 타입 정보 존재 여부
     */
    public boolean hasTypeInformation(JSON5Object json5Object, TypeInfo typeInfo) {
        return extractTypeValue(json5Object, typeInfo) != null;
    }
    
    /**
     * 기본 구현체를 가져옵니다.
     * @param typeInfo 타입 정보
     * @return 기본 구현체 클래스
     */
    public Class<?> getDefaultImplementation(TypeInfo typeInfo) {
        if (typeInfo.hasDefaultImpl()) {
            return typeInfo.getDefaultImpl();
        }
        return null;
    }
    
    /**
     * JSON에서 타입 값을 추출합니다.
     * @param json5Object JSON 객체
     * @param typeInfo 타입 정보
     * @return 타입 값, 없으면 null
     */
    private String extractTypeValue(JSON5Object json5Object, TypeInfo typeInfo) {
        String typeProperty = typeInfo.getTypeProperty();
        
        // JSON5PathExtractor를 사용하여 중첩 경로에서 값 추출
        Object value = JSON5PathExtractor.extractValue(json5Object, typeProperty);
        
        if (JSON5PathExtractor.isMissingValue(value)) {
            return null;
        }
        
        // 값을 문자열로 변환
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        }
        
        return null;
    }
    
    /**
     * 타입 정보가 없거나 매칭되지 않는 경우를 처리합니다.
     * @param typeInfo 타입 정보
     * @param typeValue 찾은 타입 값 (null일 수 있음)
     * @return 사용할 클래스
     * @throws JSON5SerializerException 해결할 수 없는 경우
     */
    private Class<?> handleMissingType(TypeInfo typeInfo, String typeValue) {
        MissingTypeStrategy strategy = typeInfo.getMissingStrategy();
        
        switch (strategy) {
            case DEFAULT_IMPL:
                if (typeInfo.hasDefaultImpl()) {
                    return typeInfo.getDefaultImpl();
                }
                // 기본 구현체도 없으면 예외 발생
                throwMissingTypeException(typeInfo, typeValue);
                break;
                
            case EXCEPTION:
                throwMissingTypeException(typeInfo, typeValue);
                break;
        }
        
        return null; // 도달하지 않음
    }
    
    /**
     * 타입을 찾을 수 없을 때 예외를 발생시킵니다.
     */
    private void throwMissingTypeException(TypeInfo typeInfo, String typeValue) {
        String message;
        if (typeValue == null) {
            message = String.format(
                "타입 정보를 찾을 수 없습니다. 경로: '%s', 지원되는 타입: %s", 
                typeInfo.getTypeProperty(), 
                typeInfo.getSupportedTypeNames()
            );
        } else {
            message = String.format(
                "알 수 없는 타입: '%s' (경로: '%s'). 지원되는 타입: %s", 
                typeValue, 
                typeInfo.getTypeProperty(), 
                typeInfo.getSupportedTypeNames()
            );
        }
        
        throw new JSON5SerializerException(message);
    }
}
