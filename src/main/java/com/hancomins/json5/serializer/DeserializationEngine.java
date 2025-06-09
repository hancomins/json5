package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

import java.util.*;

/**
 * JSON5 역직렬화를 담당하는 중앙 엔진
 * 
 * 3.3 단계에서 생성된 클래스로, 기존 JSON5Serializer의 복잡한 역직렬화 로직을
 * 타입별로 분리하여 관리하는 역할을 담당합니다.
 */
public class DeserializationEngine {
    
    private final ObjectDeserializer objectDeserializer;
    private final CollectionDeserializer collectionDeserializer;
    private final MapDeserializer mapDeserializer;
    
    public DeserializationEngine() {
        this.objectDeserializer = new ObjectDeserializer();
        this.collectionDeserializer = new CollectionDeserializer();
        this.mapDeserializer = new MapDeserializer();
    }
    
    /**
     * JSON5Object를 지정된 클래스의 객체로 역직렬화
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz) {
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object object = typeSchema.newInstance();
        return (T) objectDeserializer.deserialize(json5Object, object);
    }
    
    /**
     * JSON5Object를 기존 객체에 역직렬화
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject) {
        return objectDeserializer.deserialize(json5Object, targetObject);
    }
    
    /**
     * JSON5Object를 Map으로 역직렬화
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param valueType Map의 값 타입
     * @param <T> 값 타입
     * @return 역직렬화된 Map
     */
    public <T> Map<String, T> deserializeToMap(JSON5Object json5Object, Class<T> valueType) {
        return mapDeserializer.deserialize(json5Object, valueType);
    }
    
    /**
     * JSON5Array를 List로 역직렬화
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param valueType List의 요소 타입
     * @param <T> 요소 타입
     * @return 역직렬화된 List
     */
    public <T> List<T> deserializeToList(JSON5Array json5Array, Class<T> valueType) {
        return collectionDeserializer.deserializeToList(json5Array, valueType, null, false, null);
    }
    
    /**
     * JSON5Array를 List로 역직렬화 (오류 무시 옵션 포함)
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param valueType List의 요소 타입
     * @param writingOptions 작성 옵션
     * @param ignoreError 오류 무시 여부
     * @param defaultValue 기본값
     * @param <T> 요소 타입
     * @return 역직렬화된 List
     */
    public <T> List<T> deserializeToList(JSON5Array json5Array, Class<T> valueType, 
                                       com.hancomins.json5.options.WritingOptions writingOptions, 
                                       boolean ignoreError, T defaultValue) {
        return collectionDeserializer.deserializeToList(json5Array, valueType, writingOptions, ignoreError, defaultValue);
    }
    
    /**
     * JSON5Array를 Collection으로 역직렬화 (Schema 기반)
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param schemaArrayValue 스키마 배열 값
     * @param parent 부모 객체
     * @param context 역직렬화 컨텍스트
     */
    public void deserializeToCollection(JSON5Array json5Array, ISchemaArrayValue schemaArrayValue, 
                                      Object parent, DeserializationContext context) {
        collectionDeserializer.deserializeToCollection(json5Array, schemaArrayValue, parent, context);
    }
    
    /**
     * JSON5Object를 Map으로 역직렬화 (Schema 기반)
     * 
     * @param target 대상 Map
     * @param json5Object 역직렬화할 JSON5Object
     * @param valueType 값 타입
     * @param context 역직렬화 컨텍스트
     * @return 역직렬화된 Map
     */
    @SuppressWarnings("rawtypes")
    public Map deserializeToMap(Map target, JSON5Object json5Object, Class<?> valueType, 
                               DeserializationContext context) {
        return mapDeserializer.deserialize(target, json5Object, valueType, context);
    }
}
