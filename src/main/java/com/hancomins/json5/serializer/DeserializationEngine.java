package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

import java.util.*;

/**
 * JSON5 역직렬화를 담당하는 중앙 엔진
 * 
 * 3.3 단계에서 생성된 클래스로, 기존 JSON5Serializer의 복잡한 역직렬화 로직을
 * 타입별로 분리하여 관리하는 역할을 담당합니다.
 * 
 * 4.4 단계에서 Strategy 패턴을 적용하여 타입별 역직렬화 전략을 지원합니다.
 */
public class DeserializationEngine {
    
    private final ObjectDeserializer objectDeserializer;
    private final CollectionDeserializer collectionDeserializer;
    private final MapDeserializer mapDeserializer;
    private final DeserializationStrategyFactory strategyFactory;
    private final SerializerConfiguration configuration;
    
    public DeserializationEngine() {
        this(SerializerConfiguration.getDefault());
    }
    
    public DeserializationEngine(SerializerConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration is null");
        this.objectDeserializer = new ObjectDeserializer();
        this.collectionDeserializer = new CollectionDeserializer();
        this.mapDeserializer = new MapDeserializer();
        this.strategyFactory = DeserializationStrategyFactory.createDefault();
    }
    
    public DeserializationEngine(DeserializationStrategyFactory strategyFactory) {
        this.configuration = SerializerConfiguration.getDefault();
        this.objectDeserializer = new ObjectDeserializer();
        this.collectionDeserializer = new CollectionDeserializer();
        this.mapDeserializer = new MapDeserializer();
        this.strategyFactory = strategyFactory != null ? strategyFactory : DeserializationStrategyFactory.createDefault();
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
        // 전략 패턴 먼저 시도
        Object strategyResult = tryDeserializeWithStrategy(json5Object, clazz);
        if (strategyResult != null) {
            return (T) strategyResult;
        }
        
        // 기존 방식으로 처리
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
        // 전략 패턴 먼저 시도
        @SuppressWarnings("unchecked")
        T strategyResult = (T) tryDeserializeWithStrategy(json5Object, targetObject.getClass());
        if (strategyResult != null) {
            return strategyResult;
        }
        
        // 기존 방식으로 처리
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
    
    /**
     * 전략 패턴을 사용하여 역직렬화를 시도합니다.
     * 
     * @param json5Element 역직렬화할 JSON5Element
     * @param targetType 대상 클래스 타입
     * @return 역직렬화된 객체, 실패 시 null
     */
    public Object tryDeserializeWithStrategy(JSON5Element json5Element, Class<?> targetType) {
        if (json5Element == null || targetType == null) {
            return null;
        }
        
        Types type = Types.of(targetType);
        DeserializationStrategy strategy = strategyFactory.getStrategy(type, targetType);
        
        if (strategy != null) {
            try {
                DeserializationContext context = createDeserializationContext(json5Element, targetType);
                return strategy.deserialize(json5Element, targetType, context);
            } catch (Exception e) {
                // 전략 실패 시 null 반환하여 기존 방식으로 처리
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 역직렬화 컨텍스트를 생성합니다.
     */
    private DeserializationContext createDeserializationContext(JSON5Element json5Element, Class<?> targetType) {
        if (json5Element instanceof JSON5Object) {
            JSON5Object json5Object = (JSON5Object) json5Element;
            TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetType);
            Object rootObject = typeSchema.newInstance();
            
            DeserializationContext context = new DeserializationContext(rootObject, json5Object, typeSchema);
            context.setDeserializationEngine(this);
            
            return context;
        }
        
        // JSON5Array이거나 기타 경우
        return null;
    }
    
    /**
     * 새로운 전략을 등록합니다.
     * 
     * @param strategy 등록할 전략
     */
    public void registerStrategy(DeserializationStrategy strategy) {
        strategyFactory.registerStrategy(strategy);
    }
    
    /**
     * 등록된 모든 전략을 반환합니다.
     * 
     * @return 전략 목록
     */
    public List<DeserializationStrategy> getAllStrategies() {
        return strategyFactory.getAllStrategies();
    }
    
    /**
     * 전략 캐시를 초기화합니다.
     */
    public void clearStrategyCache() {
        strategyFactory.clearCache();
    }
    
    /**
     * 전략 팩토리를 반환합니다.
     * 
     * @return 전략 팩토리
     */
    public DeserializationStrategyFactory getStrategyFactory() {
        return strategyFactory;
    }
    
    /**
     * 설정을 반환합니다.
     * 
     * @return SerializerConfiguration
     */
    public SerializerConfiguration getConfiguration() {
        return configuration;
    }
    
    /**
     * 컨텍스트와 함께 JSON5Object를 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param context 역직렬화 컨텍스트
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz, DeserializationContext context) {
        Objects.requireNonNull(json5Object, "json5Object is null");
        Objects.requireNonNull(clazz, "clazz is null");
        Objects.requireNonNull(context, "context is null");
        
        // 컨텍스트에 엔진 설정
        context.setDeserializationEngine(this);
        if (configuration.getTypeHandlerRegistry() != null) {
            context.setTypeHandlerRegistry(configuration.getTypeHandlerRegistry());
        }
        
        // 전략 패턴 먼저 시도
        Object strategyResult = tryDeserializeWithStrategy(json5Object, clazz, context);
        if (strategyResult != null) {
            return (T) strategyResult;
        }
        
        // 기존 방식으로 처리
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object object = typeSchema.newInstance();
        return (T) objectDeserializer.deserialize(json5Object, object);
    }
    
    /**
     * 컨텍스트와 함께 JSON5Object를 기존 객체에 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param context 역직렬화 컨텍스트
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject, DeserializationContext context) {
        Objects.requireNonNull(json5Object, "json5Object is null");
        Objects.requireNonNull(targetObject, "targetObject is null");
        Objects.requireNonNull(context, "context is null");
        
        // 컨텍스트에 엔진 설정
        context.setDeserializationEngine(this);
        if (configuration.getTypeHandlerRegistry() != null) {
            context.setTypeHandlerRegistry(configuration.getTypeHandlerRegistry());
        }
        
        // 전략 패턴 먼저 시도
        @SuppressWarnings("unchecked")
        T strategyResult = (T) tryDeserializeWithStrategy(json5Object, targetObject.getClass(), context);
        if (strategyResult != null) {
            return strategyResult;
        }
        
        // 기존 방식으로 처리
        return objectDeserializer.deserialize(json5Object, targetObject);
    }
    
    /**
     * 컨텍스트와 함께 전략 패턴을 사용하여 역직렬화를 시도합니다.
     * 
     * @param json5Element 역직렬화할 JSON5Element
     * @param targetType 대상 클래스 타입
     * @param context 역직렬화 컨텍스트
     * @return 역직렬화된 객체, 실패 시 null
     */
    public Object tryDeserializeWithStrategy(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (json5Element == null || targetType == null) {
            return null;
        }
        
        Types type = Types.of(targetType);
        DeserializationStrategy strategy = strategyFactory.getStrategy(type, targetType);
        
        if (strategy != null) {
            try {
                return strategy.deserialize(json5Element, targetType, context);
            } catch (Exception e) {
                // 전략 실패 시 null 반환하여 기존 방식으로 처리
                return null;
            }
        }
        
        return null;
    }
}
