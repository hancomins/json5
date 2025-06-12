package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.options.WritingOptions;

import java.util.*;

/**
 * JSON5 역직렬화 과정에서 상태 정보를 관리하는 컨텍스트 클래스
 * 
 * SerializationContext와 유사하게 역직렬화 과정에서 필요한 상태 정보를
 * 중앙에서 관리합니다.
 */
public class DeserializationContext {
    
    private final Map<Integer, Object> parentObjectMap;
    private final Object rootObject;
    private final JSON5Object rootJson5Object;
    private final TypeSchema rootTypeSchema;
    
    /** TypeHandler 레지스트리 */
    private TypeHandlerRegistry typeHandlerRegistry;
    
    /** DeserializationEngine 참조 */
    private DeserializationEngine deserializationEngine;
    
    /** 오류 무시 여부 */
    private boolean ignoreError = false;
    
    /** 엄격한 타입 검사 여부 */
    private boolean strictTypeChecking = true;
    
    /** 기본값 */
    private Object defaultValue = null;
    
    /** WritingOptions */
    private WritingOptions writingOptions;
    
    /**
     * DeserializationContext 생성자
     * 
     * @param rootObject 루트 객체
     * @param rootJson5Object 루트 JSON5Object
     * @param rootTypeSchema 루트 타입 스키마
     */
    public DeserializationContext(Object rootObject, JSON5Object rootJson5Object, TypeSchema rootTypeSchema) {
        this.parentObjectMap = new HashMap<>();
        this.rootObject = rootObject;
        this.rootJson5Object = rootJson5Object;
        this.rootTypeSchema = rootTypeSchema;
    }
    
    /**
     * 부모 객체 맵에 객체를 등록
     * 
     * @param id 스키마 ID
     * @param object 객체
     */
    public void putParentObject(Integer id, Object object) {
        parentObjectMap.put(id, object);
    }
    
    /**
     * 부모 객체 맵에서 객체를 조회
     * 
     * @param id 스키마 ID
     * @return 객체 (없으면 null)
     */
    public Object getParentObject(Integer id) {
        return parentObjectMap.get(id);
    }
    
    /**
     * 부모 객체 맵에 객체가 있는지 확인
     * 
     * @param id 스키마 ID
     * @return 존재 여부
     */
    public boolean containsParentObject(Integer id) {
        return parentObjectMap.containsKey(id);
    }
    
    /**
     * 루트 객체 반환
     * 
     * @return 루트 객체
     */
    public Object getRootObject() {
        return rootObject;
    }
    
    /**
     * 루트 JSON5Object 반환
     * 
     * @return 루트 JSON5Object
     */
    public JSON5Object getRootJson5Object() {
        return rootJson5Object;
    }
    
    /**
     * 루트 타입 스키마 반환
     * 
     * @return 루트 타입 스키마
     */
    public TypeSchema getRootTypeSchema() {
        return rootTypeSchema;
    }
    
    /**
     * 모든 부모 객체 맵 반환 (내부 사용)
     * 
     * @return 부모 객체 맵
     */
    public Map<Integer, Object> getParentObjectMap() {
        return parentObjectMap;
    }
    
    /**
     * TypeHandlerRegistry를 설정합니다.
     * 
     * @param typeHandlerRegistry TypeHandler 레지스트리
     */
    public void setTypeHandlerRegistry(TypeHandlerRegistry typeHandlerRegistry) {
        this.typeHandlerRegistry = typeHandlerRegistry;
    }
    
    /**
     * TypeHandlerRegistry를 반환합니다.
     * 
     * @return TypeHandler 레지스트리
     */
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }
    
    /**
     * DeserializationEngine을 설정합니다.
     * 
     * @param deserializationEngine 역직렬화 엔진
     */
    public void setDeserializationEngine(DeserializationEngine deserializationEngine) {
        this.deserializationEngine = deserializationEngine;
    }
    
    /**
     * DeserializationEngine을 반환합니다.
     * 
     * @return 역직렬화 엔진
     */
    public DeserializationEngine getDeserializationEngine() {
        return deserializationEngine;
    }
    
    /**
     * 오류 무시 여부를 설정합니다.
     * 
     * @param ignoreError 오류 무시 여부
     */
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }
    
    /**
     * 오류 무시 여부를 반환합니다.
     * 
     * @return 오류 무시 여부
     */
    public boolean isIgnoreError() {
        return ignoreError;
    }
    
    /**
     * 엄격한 타입 검사 여부를 설정합니다.
     * 
     * @param strictTypeChecking 엄격한 타입 검사 여부
     */
    public void setStrictTypeChecking(boolean strictTypeChecking) {
        this.strictTypeChecking = strictTypeChecking;
    }
    
    /**
     * 엄격한 타입 검사 여부를 반환합니다.
     * 
     * @return 엄격한 타입 검사 여부
     */
    public boolean isStrictTypeChecking() {
        return strictTypeChecking;
    }
    
    /**
     * 기본값을 설정합니다.
     * 
     * @param defaultValue 기본값
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * 기본값을 반환합니다.
     * 
     * @return 기본값
     */
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * WritingOptions을 설정합니다.
     * 
     * @param writingOptions WritingOptions
     */
    public void setWritingOptions(WritingOptions writingOptions) {
        this.writingOptions = writingOptions;
    }
    
    /**
     * WritingOptions을 반환합니다.
     * 
     * @return WritingOptions
     */
    public WritingOptions getWritingOptions() {
        return writingOptions;
    }
    
    // =========================
    // 고급 체인 옵션들
    // =========================
    
    /** 엄격한 유효성 검사 여부 */
    private boolean strictValidation = false;
    
    /** 필드별 기본값 맵 */
    private Map<String, Object> fieldDefaults = new HashMap<>();
    
    /**
     * 엄격한 유효성 검사를 설정합니다.
     * 
     * @param strictValidation 엄격한 유효성 검사 여부
     */
    public void setStrictValidation(boolean strictValidation) {
        this.strictValidation = strictValidation;
    }
    
    /**
     * 엄격한 유효성 검사 여부를 반환합니다.
     * 
     * @return 엄격한 유효성 검사 여부
     */
    public boolean isStrictValidation() {
        return strictValidation;
    }
    
    /**
     * 필드별 기본값들을 설정합니다.
     * 
     * @param fieldDefaults 필드별 기본값 맵
     */
    public void setFieldDefaults(Map<String, Object> fieldDefaults) {
        this.fieldDefaults = fieldDefaults != null ? fieldDefaults : new HashMap<>();
    }
    
    /**
     * 필드별 기본값들을 반환합니다.
     * 
     * @return 필드별 기본값 맵
     */
    public Map<String, Object> getFieldDefaults() {
        return fieldDefaults;
    }
    
    /**
     * 지정된 필드의 기본값을 반환합니다.
     * 
     * @param fieldName 필드명
     * @return 기본값, 없으면 null
     */
    public Object getFieldDefault(String fieldName) {
        return fieldDefaults.get(fieldName);
    }
    
    /**
     * 지정된 필드에 대한 기본값이 있는지 확인합니다.
     * 
     * @param fieldName 필드명
     * @return 기본값이 있으면 true
     */
    public boolean hasFieldDefault(String fieldName) {
        return fieldDefaults.containsKey(fieldName);
    }
    
    /**
     * 안전한 값 얻기 - 에러 시 기본값 반환
     * 
     * @param json5Object JSON5Object
     * @param key 키
     * @param targetType 대상 타입
     * @param fieldDefault 필드 기본값
     * @return 값 또는 기본값
     */
    public Object getSafeValue(JSON5Object json5Object, String key, Class<?> targetType, Object fieldDefault) {
        try {
            if (json5Object == null || !json5Object.has(key)) {
                return fieldDefault != null ? fieldDefault : defaultValue;
            }
            
            Object value = json5Object.get(key);
            if (value == null) {
                return fieldDefault != null ? fieldDefault : defaultValue;
            }
            
            // 타입 체크
            if (strictTypeChecking && targetType != null && !targetType.isInstance(value)) {
                if (ignoreError) {
                    return fieldDefault != null ? fieldDefault : defaultValue;
                } else {
                    throw new JSON5SerializerException(
                        String.format("타입 불일치: 필드 '%s', 기대 타입 %s, 실제 타입 %s", 
                                    key, targetType.getSimpleName(), value.getClass().getSimpleName()));
                }
            }
            
            return value;
            
        } catch (Exception e) {
            if (ignoreError) {
                return fieldDefault != null ? fieldDefault : defaultValue;
            } else {
                throw new JSON5SerializerException("필드 " + key + " 처리 중 오류: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 필수 필드 검사
     * 
     * @param json5Object JSON5Object
     * @param requiredFields 필수 필드들
     * @throws JSON5SerializerException 필수 필드가 누락된 경우
     */
    public void validateRequiredFields(JSON5Object json5Object, String... requiredFields) {
        if (!strictValidation || requiredFields == null) {
            return;
        }
        
        for (String field : requiredFields) {
            if (!json5Object.has(field)) {
                if (ignoreError) {
                    return;
                } else {
                    throw new JSON5SerializerException("필수 필드 누락: " + field);
                }
            }
        }
    }
    
    /**
     * 디버깅용 문자열 표현
     * 
     * @return 문자열 표현
     */
    @Override
    public String toString() {
        return "DeserializationContext{" +
                "parentObjectCount=" + parentObjectMap.size() +
                ", rootObjectType=" + (rootObject != null ? rootObject.getClass().getSimpleName() : "null") +
                ", rootTypeSchema=" + (rootTypeSchema != null ? rootTypeSchema.getType().getSimpleName() : "null") +
                ", ignoreError=" + ignoreError +
                ", strictTypeChecking=" + strictTypeChecking +
                '}';
    }
}
