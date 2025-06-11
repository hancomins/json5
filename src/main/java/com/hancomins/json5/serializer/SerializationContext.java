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
    
    // =========================
    // 고급 체인 옵션들
    // =========================
    
    /** 유지할 필드들 (설정되면 이 필드들만 직렬화) */
    private Set<String> onlyFields;
    
    /** 최대 직렬화 깊이 */
    private int maxDepth = -1;
    
    /** 최대 문자열 길이 */
    private int maxStringLength = -1;
    
    /** 현재 직렬화 깊이 */
    private int currentDepth = 0;
    
    /**
     * 유지할 필드들을 설정합니다.
     * 
     * @param onlyFields 유지할 필드들
     */
    public void setOnlyFields(Set<String> onlyFields) {
        this.onlyFields = onlyFields;
    }
    
    /**
     * 유지할 필드들을 반환합니다.
     * 
     * @return 유지할 필드들
     */
    public Set<String> getOnlyFields() {
        return onlyFields;
    }
    
    /**
     * 지정된 필드가 유지되어야 하는지 확인합니다.
     * onlyFields가 설정되지 않았으면 모든 필드가 유지됩니다.
     * 
     * @param fieldName 필드명
     * @return 유지해야 하면 true
     */
    public boolean shouldKeepField(String fieldName) {
        if (onlyFields == null || onlyFields.isEmpty()) {
            return !isIgnoredField(fieldName);
        }
        return onlyFields.contains(fieldName);
    }
    
    /**
     * 최대 깊이를 설정합니다.
     * 
     * @param maxDepth 최대 깊이
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
    /**
     * 최대 깊이를 반환합니다.
     * 
     * @return 최대 깊이
     */
    public int getMaxDepth() {
        return maxDepth;
    }
    
    /**
     * 현재 깊이를 증가시킵니다.
     */
    public void incrementDepth() {
        currentDepth++;
    }
    
    /**
     * 현재 깊이를 감소시킵니다.
     */
    public void decrementDepth() {
        if (currentDepth > 0) {
            currentDepth--;
        }
    }
    
    /**
     * 현재 깊이를 반환합니다.
     * 
     * @return 현재 깊이
     */
    public int getCurrentDepth() {
        return currentDepth;
    }
    
    /**
     * 최대 깊이를 초과했는지 확인합니다.
     * 
     * @return 최대 깊이를 초과했으면 true
     */
    public boolean isMaxDepthExceeded() {
        return maxDepth > 0 && currentDepth >= maxDepth;
    }
    
    /**
     * 최대 문자열 길이를 설정합니다.
     * 
     * @param maxStringLength 최대 문자열 길이
     */
    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }
    
    /**
     * 최대 문자열 길이를 반환합니다.
     * 
     * @return 최대 문자열 길이
     */
    public int getMaxStringLength() {
        return maxStringLength;
    }
    
    /**
     * 문자열이 최대 길이를 초과하는지 확인합니다.
     * 
     * @param str 확인할 문자열
     * @return 최대 길이를 초과하면 true
     */
    public boolean isStringTooLong(String str) {
        return maxStringLength > 0 && str != null && str.length() > maxStringLength;
    }
    
    /**
     * 문자열을 최대 길이로 자릅니다.
     * 
     * @param str 원본 문자열
     * @return 잘린 문자열
     */
    public String truncateString(String str) {
        if (str == null || maxStringLength <= 0 || str.length() <= maxStringLength) {
            return str;
        }
        return str.substring(0, maxStringLength) + "...";
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
