package com.hancomins.json5.serializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 역직렬화 전략들을 관리하는 팩토리 클래스
 * 
 * Strategy 패턴을 통해 다양한 타입의 역직렬화 전략을 등록하고 관리합니다.
 * Thread-safe하게 구현되어 멀티스레드 환경에서 안전하게 사용할 수 있습니다.
 */
public class DeserializationStrategyFactory {
    
    private final List<DeserializationStrategy> strategies;
    private final Map<String, DeserializationStrategy> strategyCache;
    
    /**
     * 기본 전략들로 초기화된 팩토리를 생성합니다.
     */
    public DeserializationStrategyFactory() {
        this.strategies = new ArrayList<>();
        this.strategyCache = new ConcurrentHashMap<>();
        
        // 기본 전략들을 우선순위 순으로 등록
        registerDefaultStrategies();
    }
    
    /**
     * 기본 역직렬화 전략들을 등록합니다.
     */
    private void registerDefaultStrategies() {
        registerStrategy(new PrimitiveDeserializationStrategy());
        registerStrategy(new ComplexObjectDeserializationStrategy());
        registerStrategy(new GenericTypeDeserializationStrategy());
        
        // 우선순위에 따라 정렬
        sortStrategiesByPriority();
    }
    
    /**
     * 새로운 전략을 등록합니다.
     * 
     * @param strategy 등록할 전략
     */
    public void registerStrategy(DeserializationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        
        synchronized (strategies) {
            strategies.add(strategy);
            sortStrategiesByPriority();
        }
        
        // 캐시 무효화
        clearCache();
    }
    
    /**
     * 주어진 타입과 대상 클래스에 적합한 전략을 찾습니다.
     * 
     * @param type Types enum 값
     * @param targetType 대상 클래스 타입
     * @return 적합한 전략, 없으면 null
     */
    public DeserializationStrategy getStrategy(Types type, Class<?> targetType) {
        String cacheKey = generateCacheKey(type, targetType);
        
        // 캐시에서 먼저 확인
        DeserializationStrategy cachedStrategy = strategyCache.get(cacheKey);
        if (cachedStrategy != null) {
            return cachedStrategy;
        }
        
        // 전략 목록에서 검색
        synchronized (strategies) {
            for (DeserializationStrategy strategy : strategies) {
                if (strategy.canHandle(type, targetType)) {
                    strategyCache.put(cacheKey, strategy);
                    return strategy;
                }
            }
        }
        
        return null; // 적합한 전략이 없음
    }
    
    /**
     * 등록된 모든 전략을 반환합니다.
     * 
     * @return 전략 목록의 복사본
     */
    public List<DeserializationStrategy> getAllStrategies() {
        synchronized (strategies) {
            return new ArrayList<>(strategies);
        }
    }
    
    /**
     * 특정 전략을 제거합니다.
     * 
     * @param strategy 제거할 전략
     * @return 제거되었으면 true, 없었으면 false
     */
    public boolean removeStrategy(DeserializationStrategy strategy) {
        boolean removed;
        synchronized (strategies) {
            removed = strategies.remove(strategy);
        }
        
        if (removed) {
            clearCache();
        }
        
        return removed;
    }
    
    /**
     * 모든 전략을 제거합니다.
     */
    public void clearStrategies() {
        synchronized (strategies) {
            strategies.clear();
        }
        clearCache();
    }
    
    /**
     * 캐시를 초기화합니다.
     */
    public void clearCache() {
        strategyCache.clear();
    }
    
    /**
     * 등록된 전략의 개수를 반환합니다.
     * 
     * @return 전략 개수
     */
    public int getStrategyCount() {
        synchronized (strategies) {
            return strategies.size();
        }
    }
    
    /**
     * 기본 전략들로 초기화된 새로운 팩토리를 생성합니다.
     * 
     * @return 새로운 팩토리 인스턴스
     */
    public static DeserializationStrategyFactory createDefault() {
        return new DeserializationStrategyFactory();
    }
    
    /**
     * 빈 팩토리를 생성합니다 (기본 전략 없음).
     * 
     * @return 빈 팩토리 인스턴스
     */
    public static DeserializationStrategyFactory createEmpty() {
        DeserializationStrategyFactory factory = new DeserializationStrategyFactory();
        factory.clearStrategies();
        return factory;
    }
    
    /**
     * 전략들을 우선순위에 따라 정렬합니다.
     */
    private void sortStrategiesByPriority() {
        strategies.sort(Comparator.comparingInt(DeserializationStrategy::getPriority));
    }
    
    /**
     * 캐시 키를 생성합니다.
     */
    private String generateCacheKey(Types type, Class<?> targetType) {
        return type.name() + ":" + targetType.getName();
    }
    
    /**
     * 팩토리의 상태를 문자열로 표현합니다.
     */
    @Override
    public String toString() {
        return "DeserializationStrategyFactory{" +
                "strategiesCount=" + getStrategyCount() +
                ", cacheSize=" + strategyCache.size() +
                '}';
    }
}
