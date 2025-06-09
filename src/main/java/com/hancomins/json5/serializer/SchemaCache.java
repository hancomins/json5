package com.hancomins.json5.serializer;

/**
 * Schema 캐싱을 담당하는 인터페이스입니다.
 * 
 * <p>이 인터페이스는 Schema 생성 성능을 향상시키기 위해
 * 생성된 Schema들을 메모리에 캐시하는 기능을 제공합니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public interface SchemaCache {
    
    /**
     * 주어진 타입에 대한 캐시된 SchemaObjectNode를 반환합니다.
     * 
     * @param type 조회할 타입
     * @return 캐시된 SchemaObjectNode, 없으면 null
     */
    SchemaObjectNode getCachedSchema(Class<?> type);
    
    /**
     * 주어진 타입에 대한 SchemaObjectNode를 캐시에 저장합니다.
     * 
     * @param type 타입
     * @param schema 저장할 SchemaObjectNode
     */
    void putSchema(Class<?> type, SchemaObjectNode schema);
    
    /**
     * 주어진 키에 대한 캐시된 SchemaElementNode를 반환합니다.
     * 
     * @param key 조회할 키
     * @return 캐시된 SchemaElementNode, 없으면 null
     */
    SchemaElementNode getCachedSubTree(String key);
    
    /**
     * 주어진 키에 대한 SchemaElementNode를 캐시에 저장합니다.
     * 
     * @param key 키
     * @param subTree 저장할 SchemaElementNode
     */
    void putSubTree(String key, SchemaElementNode subTree);
    
    /**
     * 모든 캐시를 무효화합니다.
     */
    void invalidateCache();
    
    /**
     * 현재 캐시 크기를 반환합니다.
     * 
     * @return 캐시된 항목 수
     */
    int size();
    
    /**
     * 캐시가 비어있는지 확인합니다.
     * 
     * @return 비어있으면 true, 아니면 false
     */
    boolean isEmpty();
}
