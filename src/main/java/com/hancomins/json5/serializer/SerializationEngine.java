package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * JSON5 직렬화를 담당하는 엔진 클래스입니다.
 * 
 * <p>이 클래스는 Java 객체를 JSON5 형식으로 변환하는 핵심 로직을 포함합니다.
 * 다양한 타입(기본형, 컬렉션, 맵, 복합 객체)에 대한 직렬화를 지원하며,
 * 제네릭 타입과 추상 타입도 처리할 수 있습니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * SerializationEngine engine = new SerializationEngine();
 * JSON5Object result = engine.serialize(myObject);
 * }</pre>
 * 
 * <h3>지원하는 타입:</h3>
 * <ul>
 *   <li>기본형 및 래퍼 타입 (int, Integer, String 등)</li>
 *   <li>컬렉션 타입 (List, Set, Queue 등)</li>
 *   <li>맵 타입 (Map, HashMap, TreeMap 등)</li>
 *   <li>@JSON5Type 어노테이션이 붙은 커스텀 객체</li>
 *   <li>제네릭 타입 및 추상 타입/인터페이스</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 1.0
 * @see ObjectSerializer
 * @see CollectionSerializer
 * @see MapSerializer
 */
public class SerializationEngine {
    
    private final ObjectSerializer objectSerializer;
    private final CollectionSerializer collectionSerializer;
    private final MapSerializer mapSerializer;
    private final SerializationStrategyFactory strategyFactory;
    private final SerializerConfiguration configuration;
    
    /**
     * 기본 직렬화 엔진을 생성합니다.
     * 기본 전략들이 자동으로 등록됩니다.
     */
    public SerializationEngine() {
        this(SerializerConfiguration.getDefault());
    }
    
    /**
     * 설정을 받는 직렬화 엔진을 생성합니다.
     * 
     * @param configuration 직렬화 설정
     */
    public SerializationEngine(SerializerConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration is null");
        this.objectSerializer = new ObjectSerializer();
        this.collectionSerializer = new CollectionSerializer();
        this.mapSerializer = new MapSerializer();
        this.strategyFactory = SerializationStrategyFactory.createDefault();
    }
    
    /**
     * 커스텀 전략 팩토리를 사용하는 직렬화 엔진을 생성합니다.
     * 
     * @param strategyFactory 사용할 전략 팩토리
     */
    public SerializationEngine(SerializationStrategyFactory strategyFactory) {
        this.configuration = SerializerConfiguration.getDefault();
        this.objectSerializer = new ObjectSerializer();
        this.collectionSerializer = new CollectionSerializer();
        this.mapSerializer = new MapSerializer();
        this.strategyFactory = Objects.requireNonNull(strategyFactory, "strategyFactory is null");
    }
    
    /**
     * 주어진 객체를 JSON5Object로 직렬화합니다.
     * 
     * <p>Strategy 패턴을 사용하여 객체 타입에 가장 적합한 직렬화 전략을 선택합니다.
     * 만약 적합한 전략이 없으면 기존 방식으로 fallback합니다.</p>
     * 
     * @param obj 직렬화할 객체 (null이 아니어야 함)
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     * @throws IllegalArgumentException obj가 null인 경우
     */
    public JSON5Object serialize(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        
        // Strategy 패턴 적용 시도
        JSON5Element result = serializeWithStrategy(obj);
        if (result instanceof JSON5Object) {
            return (JSON5Object) result;
        }
        
        // Strategy로 처리되지 않은 경우 기존 방식 사용
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        if (typeSchema == null) {
            throw new JSON5SerializerException("No TypeSchema found for class: " + clazz.getName());
        }
        return serializeTypeElement(typeSchema, obj);
    }
    
    /**
     * 전략 패턴을 사용하여 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @return 직렬화된 JSON5Element, 처리할 수 없으면 null
     */
    private JSON5Element serializeWithStrategy(Object obj) {
        if (obj == null) {
            return null;
        }
        
        Types type = Types.of(obj.getClass());
        
        // 기본 타입들은 전략을 사용하지 않고 직접 처리하여 무한 재귀 방지
        if (Types.isSingleType(type)) {
            // 기본 타입들은 JSON5Object.put()에서 직접 처리되므로 null 반환
            return null;
        }
        
        SerializationStrategy strategy = strategyFactory.getStrategy(obj, type);
        
        if (strategy != null) {
            try {
                // 직렬화 컨텍스트 생성
                SerializationContext context = createSerializationContext(obj);
                return strategy.serialize(obj, context);
            } catch (Exception e) {
                // 전략 실행 중 오류 발생 시 기존 방식으로 fallback
                // 로그는 남기되 예외는 던지지 않음
                System.err.println("Strategy serialization failed for " + obj.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * 직렬화 컨텍스트를 생성합니다.
     * 
     * @param obj 직렬화할 객체
     * @return 생성된 컨텍스트
     */
    private SerializationContext createSerializationContext(Object obj) {
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        
        if (typeSchema != null) {
            return new SerializationContext(obj, typeSchema);
        } else {
            // TypeSchema가 없는 경우 기본 컨텍스트 생성
            return new SerializationContext(obj, null);
        }
    }
    
    /**
     * TypeSchema를 기반으로 객체를 직렬화합니다.
     * 
     * <p>이 메소드는 JSON5Serializer의 복잡한 serializeTypeElement 로직을
     * 새로운 ObjectSerializer를 통해 처리합니다.</p>
     * 
     * @param typeSchema 타입 스키마 정보
     * @param rootObject 직렬화할 루트 객체
     * @return 직렬화된 JSON5Object
     */
    public JSON5Object serializeTypeElement(TypeSchema typeSchema, final Object rootObject) {
        if (rootObject == null) {
            return null;
        }
        
        // 기본 JSON5Object 생성 및 헤더/푸터 코멘트 설정
        JSON5Object root = createBaseJSON5Object(typeSchema);
        
        // 직렬화 컨텍스트 생성
        SerializationContext context = new SerializationContext(rootObject, typeSchema);
        
        // 새로운 ObjectSerializer를 통한 객체 직렬화
        return objectSerializer.serializeObject(typeSchema, rootObject, root, context);
    }
    
    /**
     * 기본 JSON5Object를 생성하고 코멘트 정보를 설정합니다.
     * 
     * @param typeSchema 타입 스키마
     * @return 코멘트가 설정된 JSON5Object
     */
    private JSON5Object createBaseJSON5Object(TypeSchema typeSchema) {
        JSON5Object root = new JSON5Object();
        
        String comment = typeSchema.getComment();
        String commentAfter = typeSchema.getCommentAfter();
        
        if (comment != null) {
            root.setHeaderComment(comment);
        }
        if (commentAfter != null) {
            root.setFooterComment(commentAfter);
        }
        
        return root;
    }
    
    /**
     * 컬렉션을 JSON5Array로 직렬화합니다.
     * 
     * @param collection 직렬화할 컬렉션
     * @param valueType 컬렉션 요소의 타입 (null 가능)
     * @return 직렬화된 JSON5Array
     */
    public JSON5Array serializeCollection(Collection<?> collection, Class<?> valueType) {
        return collectionSerializer.serializeCollection(collection, valueType);
    }
    
    /**
     * 스키마 정보가 있는 컬렉션을 직렬화합니다.
     * 
     * @param collection 직렬화할 컬렉션
     * @param schemaArrayValue 스키마 배열 정보
     * @return 직렬화된 JSON5Array
     */
    public JSON5Array serializeCollectionWithSchema(Collection<?> collection, ISchemaArrayValue schemaArrayValue) {
        return collectionSerializer.serializeCollectionWithSchema(collection, schemaArrayValue);
    }
    
    /**
     * Map을 JSON5Object로 직렬화합니다.
     * 
     * @param map 직렬화할 Map
     * @param valueType Map 값의 타입 (null 가능)
     * @return 직렬화된 JSON5Object
     */
    public JSON5Object serializeMap(Map<String, ?> map, Class<?> valueType) {
        return mapSerializer.serializeMap(map, valueType);
    }
    
    /**
     * 새로운 직렬화 전략을 등록합니다.
     * 
     * @param strategy 등록할 전략
     * @throws IllegalArgumentException strategy가 null인 경우
     */
    public void registerStrategy(SerializationStrategy strategy) {
        strategyFactory.registerStrategy(strategy);
    }
    
    /**
     * 등록된 모든 전략의 목록을 반환합니다.
     * 
     * @return 전략 목록의 불변 복사본
     */
    public List<SerializationStrategy> getAllStrategies() {
        return strategyFactory.getAllStrategies();
    }
    
    /**
     * 특정 객체와 타입에 대해 사용 가능한 전략을 반환합니다.
     * 
     * @param obj 확인할 객체
     * @param type 객체의 타입
     * @return 사용 가능한 전략, 없으면 null
     */
    public SerializationStrategy getStrategyFor(Object obj, Types type) {
        return strategyFactory.getStrategy(obj, type);
    }
    
    /**
     * 전략 캐시를 초기화합니다.
     */
    public void clearStrategyCache() {
        strategyFactory.clearCache();
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
     * 컨텍스트와 함께 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public JSON5Object serialize(Object obj, SerializationContext context) {
        Objects.requireNonNull(obj, "obj is null");
        Objects.requireNonNull(context, "context is null");
        
        // 컨텍스트에 엔진 설정
        context.setSerializationEngine(this);
        if (configuration.getTypeHandlerRegistry() != null) {
            context.setTypeHandlerRegistry(configuration.getTypeHandlerRegistry());
        }
        
        // Strategy 패턴 적용 시도
        JSON5Element result = serializeWithStrategy(obj, context);
        if (result instanceof JSON5Object) {
            return (JSON5Object) result;
        }
        
        // Strategy로 처리되지 않은 경우 기존 방식 사용
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        if (typeSchema == null) {
            throw new JSON5SerializerException("No TypeSchema found for class: " + clazz.getName());
        }
        return serializeTypeElement(typeSchema, obj);
    }
    
    /**
     * 컨텍스트와 함께 전략 패턴을 사용하여 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Element, 처리할 수 없으면 null
     */
    private JSON5Element serializeWithStrategy(Object obj, SerializationContext context) {
        if (obj == null) {
            return null;
        }
        
        Types type = Types.of(obj.getClass());
        
        // 기본 타입들은 전략을 사용하지 않고 직접 처리하여 무한 재귀 방지
        if (Types.isSingleType(type)) {
            return null;
        }
        
        SerializationStrategy strategy = strategyFactory.getStrategy(obj, type);
        
        if (strategy != null) {
            try {
                return strategy.serialize(obj, context);
            } catch (Exception e) {
                System.err.println("Strategy serialization failed for " + obj.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
}
