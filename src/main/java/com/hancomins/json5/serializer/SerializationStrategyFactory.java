package com.hancomins.json5.serializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 직렬화 전략 팩토리 클래스입니다.
 * 
 * <p>이 클래스는 등록된 직렬화 전략들을 관리하고,
 * 주어진 객체와 타입에 가장 적합한 전략을 선택하는 역할을 합니다.
 * 전략들은 우선순위에 따라 정렬되어 관리됩니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * SerializationStrategyFactory factory = new SerializationStrategyFactory();
 * factory.registerStrategy(new PrimitiveSerializationStrategy());
 * factory.registerStrategy(new ComplexObjectSerializationStrategy());
 * 
 * SerializationStrategy strategy = factory.getStrategy(obj, type);
 * if (strategy != null) {
 *     JSON5Element result = strategy.serialize(obj, context);
 * }
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 * @see SerializationStrategy
 * @see PrimitiveSerializationStrategy
 * @see ComplexObjectSerializationStrategy
 */
public class SerializationStrategyFactory {
    
    /**
     * 등록된 전략들을 우선순위 순으로 관리하는 리스트
     */
    private final List<SerializationStrategy> strategies;
    
    /**
     * 성능 향상을 위한 전략 캐시
     * Key: 클래스 이름, Value: 해당 클래스에 적합한 전략
     */
    private final Map<String, SerializationStrategy> strategyCache;
    
    /**
     * 기본 생성자입니다.
     * 기본 전략들을 등록합니다.
     */
    public SerializationStrategyFactory() {
        this.strategies = new ArrayList<>();
        this.strategyCache = new ConcurrentHashMap<>();
        
        // 기본 전략들 등록 (우선순위 순)
        registerDefaultStrategies();
    }
    
    /**
     * 기본 전략들을 등록합니다.
     */
    private void registerDefaultStrategies() {
        registerStrategy(new PrimitiveSerializationStrategy());
        registerStrategy(new ComplexObjectSerializationStrategy());
    }
    
    /**
     * 새로운 직렬화 전략을 등록합니다.
     * 
     * <p>전략은 우선순위에 따라 정렬되어 저장됩니다.
     * 낮은 숫자일수록 높은 우선순위를 가집니다.</p>
     * 
     * @param strategy 등록할 전략
     * @throws IllegalArgumentException strategy가 null인 경우
     */
    public void registerStrategy(SerializationStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy is null");
        
        synchronized (strategies) {
            strategies.add(strategy);
            // 우선순위에 따라 정렬 (낮은 숫자가 높은 우선순위)
            strategies.sort(Comparator.comparingInt(SerializationStrategy::getPriority));
        }
        
        // 캐시 무효화
        strategyCache.clear();
    }
    
    /**
     * 주어진 객체와 타입에 가장 적합한 전략을 선택합니다.
     * 
     * <p>먼저 캐시에서 찾아보고, 없으면 등록된 전략들을 
     * 우선순위 순으로 확인하여 처리 가능한 첫 번째 전략을 반환합니다.</p>
     * 
     * @param obj 처리할 객체
     * @param type 객체의 타입 정보
     * @return 적합한 전략, 없으면 null
     */
    public SerializationStrategy getStrategy(Object obj, Types type) {
        if (obj == null) {
            return null;
        }
        
        String cacheKey = obj.getClass().getName() + "_" + type.name();
        
        // 캐시에서 먼저 확인
        SerializationStrategy cachedStrategy = strategyCache.get(cacheKey);
        if (cachedStrategy != null) {
            return cachedStrategy;
        }
        
        // 등록된 전략들을 우선순위 순으로 확인
        synchronized (strategies) {
            for (SerializationStrategy strategy : strategies) {
                if (strategy.canHandle(obj, type)) {
                    // 캐시에 저장
                    strategyCache.put(cacheKey, strategy);
                    return strategy;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 등록된 모든 전략의 목록을 반환합니다.
     * 
     * @return 전략 목록의 불변 복사본
     */
    public List<SerializationStrategy> getAllStrategies() {
        synchronized (strategies) {
            return new ArrayList<>(strategies);
        }
    }
    
    /**
     * 특정 타입을 처리할 수 있는 모든 전략을 반환합니다.
     * 
     * @param obj 처리할 객체
     * @param type 객체의 타입 정보
     * @return 처리 가능한 전략들의 리스트
     */
    public List<SerializationStrategy> getCompatibleStrategies(Object obj, Types type) {
        List<SerializationStrategy> compatibleStrategies = new ArrayList<>();
        
        synchronized (strategies) {
            for (SerializationStrategy strategy : strategies) {
                if (strategy.canHandle(obj, type)) {
                    compatibleStrategies.add(strategy);
                }
            }
        }
        
        return compatibleStrategies;
    }
    
    /**
     * 전략 캐시를 초기화합니다.
     * 
     * <p>새로운 전략이 등록되거나 기존 전략의 동작이 변경되었을 때 호출합니다.</p>
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
     * 모든 전략을 제거합니다.
     * 
     * <p>주로 테스트 용도로 사용됩니다.</p>
     */
    public void clearAllStrategies() {
        synchronized (strategies) {
            strategies.clear();
        }
        strategyCache.clear();
    }
    
    /**
     * 기본 팩토리 인스턴스를 생성합니다.
     * 
     * @return 기본 전략들이 등록된 팩토리
     */
    public static SerializationStrategyFactory createDefault() {
        return new SerializationStrategyFactory();
    }
}
