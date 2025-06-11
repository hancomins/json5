package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.serializer.constructor.ConstructorDeserializer;
import com.hancomins.json5.serializer.constructor.ConstructorAnalyzer;
import com.hancomins.json5.serializer.polymorphic.PolymorphicDeserializer;
import com.hancomins.json5.serializer.polymorphic.TypeInfoAnalyzer;
import com.hancomins.json5.serializer.provider.ValueProviderRegistry;
import com.hancomins.json5.serializer.provider.ValueProviderDeserializer;

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
    
    // 값 공급자 관련 컴포넌트 (데직렬화엔진과 동일한 인스턴스 공유)
    private static final ValueProviderRegistry VALUE_PROVIDER_REGISTRY = SerializationEngine.VALUE_PROVIDER_REGISTRY;
    private static final ValueProviderDeserializer VALUE_PROVIDER_DESERIALIZER = 
        new ValueProviderDeserializer(VALUE_PROVIDER_REGISTRY);
    
    private final ObjectDeserializer objectDeserializer;
    private final CollectionDeserializer collectionDeserializer;
    private final MapDeserializer mapDeserializer;
    private final ConstructorDeserializer constructorDeserializer;
    private final ConstructorAnalyzer constructorAnalyzer;
    private final PolymorphicDeserializer polymorphicDeserializer;
    private final TypeInfoAnalyzer typeInfoAnalyzer;
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
        this.constructorDeserializer = new ConstructorDeserializer();
        this.constructorAnalyzer = new ConstructorAnalyzer();
        this.polymorphicDeserializer = new PolymorphicDeserializer();
        this.polymorphicDeserializer.setDeserializationEngine(this);
        this.typeInfoAnalyzer = new TypeInfoAnalyzer();
        this.strategyFactory = DeserializationStrategyFactory.createDefault();
    }
    
    public DeserializationEngine(DeserializationStrategyFactory strategyFactory) {
        this.configuration = SerializerConfiguration.getDefault();
        this.objectDeserializer = new ObjectDeserializer();
        this.collectionDeserializer = new CollectionDeserializer();
        this.mapDeserializer = new MapDeserializer();
        this.constructorDeserializer = new ConstructorDeserializer();
        this.constructorAnalyzer = new ConstructorAnalyzer();
        this.polymorphicDeserializer = new PolymorphicDeserializer();
        this.polymorphicDeserializer.setDeserializationEngine(this);
        this.typeInfoAnalyzer = new TypeInfoAnalyzer();
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
        // 값 공급자 역직렬화 시도
        if (VALUE_PROVIDER_REGISTRY.isValueProvider(clazz)) {
            try {
                Object value = extractValueForValueProvider(json5Object);
                return VALUE_PROVIDER_DESERIALIZER.deserialize(value, clazz);
            } catch (Exception e) {
                // 값 공급자 역직렬화 실패 시 기존 방식으로 처리
                System.err.println("Value provider deserialization failed for " + 
                    clazz.getName() + ": " + e.getMessage());
            }
        }
        
        // 1. 다형성 타입 확인 및 처리
        if (typeInfoAnalyzer.isPolymorphicType(clazz)) {
            try {
                return polymorphicDeserializer.deserialize(json5Object, clazz);
            } catch (Exception e) {
                System.err.println("Polymorphic deserialization failed, falling back to default: " + e.getMessage());
            }
        }
        
        // 2. 생성자 기반 역직렬화 시도
        if (constructorAnalyzer.hasCreatorConstructor(clazz)) {
            try {
                return constructorDeserializer.deserialize(json5Object, clazz);
            } catch (Exception e) {
                System.err.println("Constructor deserialization failed, falling back to default: " + e.getMessage());
            }
        }
        
        // 3. 전략 패턴 시도
        Object strategyResult = tryDeserializeWithStrategy(json5Object, clazz);
        if (strategyResult != null) {
            return (T) strategyResult;
        }
        
        // 4. 기존 방식으로 처리
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
        // 기존 객체에 역직렬화할 때는 전략 패턴을 사용하지 않고
        // 바로 ObjectDeserializer를 사용하여 기존 객체를 업데이트함
        return objectDeserializer.deserialize(json5Object, targetObject, null);
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
     * 값 공급자를 위한 값 추출
     */
    private Object extractValueForValueProvider(JSON5Object json5Object) {
        // 단순 값인 경우와 래핑된 값인 경우 모두 처리
        if (json5Object.has("value")) {
            return json5Object.get("value");
        }
        
        // 단일 키-값 쌍인 경우 값 반환
        if (json5Object.size() == 1) {
            return json5Object.values().iterator().next();
        }
        
        // 복잡한 객체인 경우 JSON5Object 자체 반환
        return json5Object;
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
     * ConstructorDeserializer를 반환합니다.
     * 
     * @return ConstructorDeserializer
     */
    public ConstructorDeserializer getConstructorDeserializer() {
        return constructorDeserializer;
    }
    
    /**
     * ConstructorAnalyzer를 반환합니다.
     * 
     * @return ConstructorAnalyzer
     */
    public ConstructorAnalyzer getConstructorAnalyzer() {
        return constructorAnalyzer;
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
        
        // 1. 다형성 타입 확인 및 처리
        if (typeInfoAnalyzer.isPolymorphicType(clazz)) {
            try {
                return polymorphicDeserializer.deserialize(json5Object, clazz);
            } catch (Exception e) {
                // 다형성 역직렬화 실패 시 기존 방식으로 fallback
                System.err.println("Polymorphic deserialization failed, falling back to default: " + e.getMessage());
            }
        }
        
        // 2. 생성자 기반 역직렬화 시도
        if (constructorAnalyzer.hasCreatorConstructor(clazz)) {
            try {
                return constructorDeserializer.deserialize(json5Object, clazz);
            } catch (Exception e) {
                // 생성자 역직렬화 실패 시 기존 방식으로 fallback
                System.err.println("Constructor deserialization failed, falling back to default: " + e.getMessage());
            }
        }
        
        // 3. 전략 패턴 시도
        Object strategyResult = tryDeserializeWithStrategy(json5Object, clazz, context);
        if (strategyResult != null) {
            return (T) strategyResult;
        }
        
        // 4. 기존 방식으로 처리
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
        
        // 기존 객체에 역직렬화할 때는 전략 패턴을 사용하지 않고
        // 바로 ObjectDeserializer를 사용하여 기존 객체를 업데이트함
        return objectDeserializer.deserialize(json5Object, targetObject, context);
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
