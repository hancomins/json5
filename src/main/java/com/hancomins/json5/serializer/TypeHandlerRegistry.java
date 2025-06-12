package com.hancomins.json5.serializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TypeHandler들을 등록하고 관리하는 레지스트리입니다.
 * 
 * <p>이 클래스는 다음 기능을 제공합니다:</p>
 * <ul>
 *   <li>TypeHandler 등록 및 제거</li>
 *   <li>타입에 적합한 TypeHandler 검색</li>
 *   <li>우선순위 기반 TypeHandler 선택</li>
 *   <li>성능 최적화를 위한 핸들러 캐싱</li>
 * </ul>
 * 
 * <p>Thread-safe하며 동시성 환경에서 안전하게 사용할 수 있습니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class TypeHandlerRegistry {
    
    private final List<TypeHandler> handlers;
    private final Map<String, TypeHandler> handlerCache;
    private final Object lock = new Object();
    
    /**
     * 기본 생성자입니다.
     */
    public TypeHandlerRegistry() {
        this.handlers = new ArrayList<>();
        this.handlerCache = new ConcurrentHashMap<>();
    }
    
    /**
     * TypeHandler를 등록합니다.
     * 우선순위에 따라 자동으로 정렬됩니다.
     * 
     * @param handler 등록할 TypeHandler
     * @throws IllegalArgumentException handler가 null인 경우
     */
    public void registerHandler(TypeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("TypeHandler cannot be null");
        }
        
        synchronized (lock) {
            handlers.add(handler);
            // 우선순위 순으로 정렬 (낮은 값이 높은 우선순위)
            handlers.sort(Comparator.comparingInt(h -> h.getPriority().getLevel()));
            // 캐시 무효화
            handlerCache.clear();
        }
    }
    
    /**
     * TypeHandler를 제거합니다.
     * 
     * @param handler 제거할 TypeHandler
     * @return 제거되었으면 true, 존재하지 않았으면 false
     */
    public boolean removeHandler(TypeHandler handler) {
        synchronized (lock) {
            boolean removed = handlers.remove(handler);
            if (removed) {
                handlerCache.clear();
            }
            return removed;
        }
    }
    
    /**
     * 지정된 타입을 처리할 수 있는 TypeHandler를 찾습니다.
     * 
     * @param type Types enum 값
     * @param clazz 실제 클래스 타입
     * @return 적합한 TypeHandler, 없으면 null
     */
    public TypeHandler getHandler(Types type, Class<?> clazz) {
        String cacheKey = createCacheKey(type, clazz);
        
        // 캐시에서 먼저 확인
        TypeHandler cachedHandler = handlerCache.get(cacheKey);
        if (cachedHandler != null) {
            return cachedHandler;
        }
        
        // 핸들러 검색
        synchronized (lock) {
            for (TypeHandler handler : handlers) {
                if (handler.canHandle(type, clazz)) {
                    handlerCache.put(cacheKey, handler);
                    return handler;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 지정된 타입을 처리할 수 있는 모든 TypeHandler를 반환합니다.
     * 우선순위 순으로 정렬되어 반환됩니다.
     * 
     * @param type Types enum 값
     * @param clazz 실제 클래스 타입
     * @return 적합한 TypeHandler들의 리스트
     */
    public List<TypeHandler> getAllHandlers(Types type, Class<?> clazz) {
        List<TypeHandler> result = new ArrayList<>();
        
        synchronized (lock) {
            for (TypeHandler handler : handlers) {
                if (handler.canHandle(type, clazz)) {
                    result.add(handler);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 등록된 모든 TypeHandler를 반환합니다.
     * 
     * @return TypeHandler들의 불변 리스트
     */
    public List<TypeHandler> getAllHandlers() {
        synchronized (lock) {
            return new ArrayList<>(handlers);
        }
    }
    
    /**
     * 핸들러 캐시를 초기화합니다.
     */
    public void clearCache() {
        handlerCache.clear();
    }
    
    /**
     * 등록된 핸들러 수를 반환합니다.
     * 
     * @return 핸들러 수
     */
    public int getHandlerCount() {
        synchronized (lock) {
            return handlers.size();
        }
    }
    
    /**
     * 캐시된 핸들러 수를 반환합니다.
     * 
     * @return 캐시된 핸들러 수
     */
    public int getCacheSize() {
        return handlerCache.size();
    }
    
    /**
     * 캐시 키를 생성합니다.
     */
    private String createCacheKey(Types type, Class<?> clazz) {
        if (clazz == null) {
            return type.name() + "::null";
        }
        return type.name() + "::" + clazz.getName();
    }
    
    /**
     * TypeHandlerRegistry의 Builder 클래스입니다.
     */
    public static class Builder {
        private final TypeHandlerRegistry registry;
        
        public Builder() {
            this.registry = new TypeHandlerRegistry();
        }
        
        /**
         * TypeHandler를 추가합니다.
         * 
         * @param handler 추가할 TypeHandler
         * @return 이 Builder 인스턴스
         */
        public Builder addHandler(TypeHandler handler) {
            registry.registerHandler(handler);
            return this;
        }
        
        /**
         * 기본 TypeHandler들을 추가합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder withDefaultHandlers() {
            registry.registerHandler(new PrimitiveTypeHandler());
            registry.registerHandler(new CollectionTypeHandler());
            registry.registerHandler(new MapTypeHandler());
            registry.registerHandler(new ObjectTypeHandler());
            registry.registerHandler(new GenericTypeHandler());
            return this;
        }
        
        /**
         * TypeHandlerRegistry를 빌드합니다.
         * 
         * @return 구성된 TypeHandlerRegistry
         */
        public TypeHandlerRegistry build() {
            return registry;
        }
    }
}
