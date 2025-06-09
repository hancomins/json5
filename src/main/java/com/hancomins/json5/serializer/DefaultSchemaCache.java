package com.hancomins.json5.serializer;

import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.WeakReference;

/**
 * 기본 Schema 캐시 구현체입니다.
 * 
 * <p>이 클래스는 동시성을 고려한 안전한 캐싱을 제공하며,
 * WeakReference를 사용하여 메모리 누수를 방지합니다.</p>
 * 
 * <h3>캐싱 전략:</h3>
 * <ul>
 *   <li>SchemaObjectNode: 강한 참조로 캐싱 (자주 사용되는 타입)</li>
 *   <li>SchemaElementNode: WeakReference로 캐싱 (메모리 효율성)</li>
 *   <li>동시성: ConcurrentHashMap 사용</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class DefaultSchemaCache implements SchemaCache {
    
    private final ConcurrentHashMap<Class<?>, SchemaObjectNode> schemaCache;
    private final ConcurrentHashMap<String, WeakReference<SchemaElementNode>> subTreeCache;
    
    /**
     * 기본 캐시 크기로 DefaultSchemaCache를 생성합니다.
     */
    public DefaultSchemaCache() {
        this(64, 256);
    }
    
    /**
     * 지정된 초기 크기로 DefaultSchemaCache를 생성합니다.
     * 
     * @param schemaInitialCapacity Schema 캐시 초기 크기
     * @param subTreeInitialCapacity SubTree 캐시 초기 크기
     */
    public DefaultSchemaCache(int schemaInitialCapacity, int subTreeInitialCapacity) {
        this.schemaCache = new ConcurrentHashMap<>(schemaInitialCapacity);
        this.subTreeCache = new ConcurrentHashMap<>(subTreeInitialCapacity);
    }
    
    @Override
    public SchemaObjectNode getCachedSchema(Class<?> type) {
        if (type == null) {
            return null;
        }
        return schemaCache.get(type);
    }
    
    @Override
    public void putSchema(Class<?> type, SchemaObjectNode schema) {
        if (type == null || schema == null) {
            return;
        }
        schemaCache.put(type, schema);
    }
    
    @Override
    public SchemaElementNode getCachedSubTree(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        
        WeakReference<SchemaElementNode> ref = subTreeCache.get(key);
        if (ref != null) {
            SchemaElementNode subTree = ref.get();
            if (subTree != null) {
                return subTree;
            } else {
                // WeakReference가 가비지 컬렉션된 경우 제거
                subTreeCache.remove(key);
            }
        }
        return null;
    }
    
    @Override
    public void putSubTree(String key, SchemaElementNode subTree) {
        if (key == null || key.isEmpty() || subTree == null) {
            return;
        }
        subTreeCache.put(key, new WeakReference<>(subTree));
    }
    
    @Override
    public void invalidateCache() {
        schemaCache.clear();
        subTreeCache.clear();
    }
    
    @Override
    public int size() {
        return schemaCache.size() + subTreeCache.size();
    }
    
    @Override
    public boolean isEmpty() {
        return schemaCache.isEmpty() && subTreeCache.isEmpty();
    }
    
    /**
     * Schema 캐시만의 크기를 반환합니다.
     * 
     * @return Schema 캐시 크기
     */
    public int getSchemasCacheSize() {
        return schemaCache.size();
    }
    
    /**
     * SubTree 캐시만의 크기를 반환합니다.
     * 
     * @return SubTree 캐시 크기
     */
    public int getSubTreeCacheSize() {
        return subTreeCache.size();
    }
    
    /**
     * 가비지 컬렉션된 WeakReference를 정리합니다.
     * 
     * <p>이 메소드는 주기적으로 호출하여 메모리를 정리할 수 있습니다.</p>
     * 
     * @return 제거된 항목 수
     */
    public int cleanupWeakReferences() {
        int removedCount = 0;
        for (String key : subTreeCache.keySet()) {
            WeakReference<SchemaElementNode> ref = subTreeCache.get(key);
            if (ref != null && ref.get() == null) {
                subTreeCache.remove(key);
                removedCount++;
            }
        }
        return removedCount;
    }
    
    /**
     * 캐시 통계 정보를 반환합니다.
     * 
     * @return 캐시 통계 문자열
     */
    public String getCacheStats() {
        cleanupWeakReferences(); // 통계 조회 시 정리 수행
        return String.format(
            "SchemaCache Stats - Schemas: %d, SubTrees: %d, Total: %d",
            getSchemasCacheSize(),
            getSubTreeCacheSize(),
            size()
        );
    }
}
