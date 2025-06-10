package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.options.WritingOptions;
import java.util.*;

/**
 * 직렬화 과정의 컨텍스트 정보를 관리하는 클래스입니다.
 * 
 * <p>직렬화 중에 필요한 상태 정보들을 담고 있으며, 순환 참조 방지를 위한
 * 부모 객체 맵과 중첩된 직렬화 작업을 위한 스택을 관리합니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class SerializationContext {
    
    /** 순환 참조 방지를 위한 부모 객체 맵 (ID -> 객체) */
    private final Map<Integer, Object> parentObjectMap;
    
    /** 중첩된 직렬화 작업을 위한 스택 */
    private final ArrayDeque<ObjectSerializeDequeueItem> dequeueStack;
    
    /** 루트 TypeSchema */
    private final TypeSchema rootTypeSchema;
    
    /** 루트 객체 */
    private final Object rootObject;
    
    /** TypeHandler 레지스트리 */
    private TypeHandlerRegistry typeHandlerRegistry;
    
    /** SerializationEngine 참조 */
    private SerializationEngine serializationEngine;
    
    /** WritingOptions */
    private WritingOptions writingOptions;
    
    /** null 값 포함 여부 */
    private boolean includeNullValues = false;
    
    /** 무시할 필드들 */
    private Set<String> ignoredFields = new HashSet<>();
    
    /**
     * 직렬화 컨텍스트를 생성합니다.
     * 
     * @param rootObject 루트 객체
     * @param rootTypeSchema 루트 TypeSchema
     */
    public SerializationContext(Object rootObject, TypeSchema rootTypeSchema) {
        this.parentObjectMap = new HashMap<>();
        this.dequeueStack = new ArrayDeque<>();
        this.rootObject = rootObject;
        this.rootTypeSchema = rootTypeSchema;
    }
    
    /**
     * 컨텍스트 항목을 스택에 추가합니다.
     * 
     * @param item 추가할 컨텍스트 항목
     */
    public void pushContext(ObjectSerializeDequeueItem item) {
        dequeueStack.add(item);
    }
    
    /**
     * 스택에서 컨텍스트 항목을 제거하고 반환합니다.
     * 
     * @return 제거된 컨텍스트 항목, 스택이 비어있으면 null
     */
    public ObjectSerializeDequeueItem popContext() {
        if (dequeueStack.isEmpty()) {
            return null;
        }
        return dequeueStack.removeFirst();
    }
    
    /**
     * 현재 스택의 첫 번째 항목을 반환합니다.
     * 
     * @return 첫 번째 컨텍스트 항목, 스택이 비어있으면 null
     */
    public ObjectSerializeDequeueItem getFirstContext() {
        return dequeueStack.isEmpty() ? null : dequeueStack.getFirst();
    }
    
    /**
     * 스택이 비어있는지 확인합니다.
     * 
     * @return 스택이 비어있으면 true
     */
    public boolean isStackEmpty() {
        return dequeueStack.isEmpty();
    }
    
    /**
     * 지정된 ID의 부모 객체를 조회합니다.
     * 
     * @param id 객체 ID
     * @return 해당 ID의 객체, 없으면 null
     */
    public Object getParentObject(int id) {
        return parentObjectMap.get(id);
    }
    
    /**
     * 부모 객체를 맵에 추가합니다.
     * 
     * @param id 객체 ID
     * @param obj 저장할 객체
     */
    public void putParentObject(int id, Object obj) {
        if (obj != null) {
            parentObjectMap.put(id, obj);
        }
    }
    
    /**
     * 부모 객체 맵에 지정된 ID가 있는지 확인합니다.
     * 
     * @param id 확인할 객체 ID
     * @return ID가 존재하면 true
     */
    public boolean containsParentObject(int id) {
        return parentObjectMap.containsKey(id);
    }
    
    /**
     * 루트 TypeSchema를 반환합니다.
     * 
     * @return 루트 TypeSchema
     */
    public TypeSchema getRootTypeSchema() {
        return rootTypeSchema;
    }
    
    /**
     * 루트 객체를 반환합니다.
     * 
     * @return 루트 객체
     */
    public Object getRootObject() {
        return rootObject;
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
     * SerializationEngine을 설정합니다.
     * 
     * @param serializationEngine 직렬화 엔진
     */
    public void setSerializationEngine(SerializationEngine serializationEngine) {
        this.serializationEngine = serializationEngine;
    }
    
    /**
     * SerializationEngine을 반환합니다.
     * 
     * @return 직렬화 엔진
     */
    public SerializationEngine getSerializationEngine() {
        return serializationEngine;
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
    
    /**
     * null 값 포함 여부를 설정합니다.
     * 
     * @param includeNullValues null 값 포함 여부
     */
    public void setIncludeNullValues(boolean includeNullValues) {
        this.includeNullValues = includeNullValues;
    }
    
    /**
     * null 값 포함 여부를 반환합니다.
     * 
     * @return null 값 포함 여부
     */
    public boolean isIncludeNullValues() {
        return includeNullValues;
    }
    
    /**
     * 무시할 필드들을 설정합니다.
     * 
     * @param ignoredFields 무시할 필드들
     */
    public void setIgnoredFields(Set<String> ignoredFields) {
        this.ignoredFields = ignoredFields != null ? ignoredFields : new HashSet<>();
    }
    
    /**
     * 무시할 필드들을 반환합니다.
     * 
     * @return 무시할 필드들
     */
    public Set<String> getIgnoredFields() {
        return ignoredFields;
    }
    
    /**
     * 지정된 필드가 무시되어야 하는지 확인합니다.
     * 
     * @param fieldName 필드명
     * @return 무시해야 하면 true
     */
    public boolean isIgnoredField(String fieldName) {
        return ignoredFields.contains(fieldName);
    }
    
    /**
     * 직렬화 작업 항목을 나타내는 내부 클래스입니다.
     */
    public static class ObjectSerializeDequeueItem {
        
        /** 키 반복자 */
        public final Iterator<Object> keyIterator;
        
        /** 스키마 노드 */
        public final ISchemaNode schemaNode;
        
        /** 결과 JSON5 요소 */
        public final JSON5Element resultElement;
        
        /**
         * 직렬화 작업 항목을 생성합니다.
         * 
         * @param keyIterator 키 반복자
         * @param schemaNode 스키마 노드
         * @param resultElement 결과 JSON5 요소
         */
        public ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode schemaNode, JSON5Element resultElement) {
            this.keyIterator = keyIterator;
            this.schemaNode = schemaNode;
            this.resultElement = resultElement;
        }
    }
}
