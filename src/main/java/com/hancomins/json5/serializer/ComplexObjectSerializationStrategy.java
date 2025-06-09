package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import java.util.Collection;
import java.util.Map;

/**
 * 복합 객체 직렬화 전략 구현체입니다.
 * 
 * <p>이 클래스는 복잡한 객체, 컬렉션, 맵 등에 대한 직렬화를 담당합니다.
 * @JSON5Type 어노테이션이 붙은 커스텀 객체나 복합 구조를 가진 
 * 객체들을 처리합니다.</p>
 * 
 * <h3>지원하는 타입:</h3>
 * <ul>
 *   <li>@JSON5Type 어노테이션이 붙은 커스텀 객체</li>
 *   <li>컬렉션 타입 (List, Set, Queue 등)</li>
 *   <li>맵 타입 (Map, HashMap, TreeMap 등)</li>
 *   <li>추상 타입 및 인터페이스</li>
 *   <li>기타 복합 객체</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 * @see SerializationStrategy
 * @see PrimitiveSerializationStrategy
 */
public class ComplexObjectSerializationStrategy implements SerializationStrategy {
    
    /**
     * 우선순위 상수 - 복합 객체는 기본 타입보다 낮은 우선순위를 가집니다.
     */
    private static final int PRIORITY = 3;
    
    private final ObjectSerializer objectSerializer;
    private final CollectionSerializer collectionSerializer;
    private final MapSerializer mapSerializer;
    
    /**
     * 기본 생성자입니다.
     * 필요한 직렬화 컴포넌트들을 초기화합니다.
     */
    public ComplexObjectSerializationStrategy() {
        this.objectSerializer = new ObjectSerializer();
        this.collectionSerializer = new CollectionSerializer();
        this.mapSerializer = new MapSerializer();
    }
    
    /**
     * 의존성 주입을 위한 생성자입니다.
     * 
     * @param objectSerializer 객체 직렬화기
     * @param collectionSerializer 컬렉션 직렬화기
     * @param mapSerializer 맵 직렬화기
     */
    public ComplexObjectSerializationStrategy(ObjectSerializer objectSerializer, 
                                            CollectionSerializer collectionSerializer,
                                            MapSerializer mapSerializer) {
        this.objectSerializer = objectSerializer;
        this.collectionSerializer = collectionSerializer;
        this.mapSerializer = mapSerializer;
    }
    
    /**
     * 이 전략이 주어진 객체와 타입을 처리할 수 있는지 확인합니다.
     * 
     * @param obj 처리할 객체
     * @param type 객체의 타입 정보
     * @return 복합 객체면 true, 그렇지 않으면 false
     */
    @Override
    public boolean canHandle(Object obj, Types type) {
        if (obj == null) {
            return false;
        }
        
        // 복합 타입들 처리
        switch (type) {
            case Object:
            case AbstractObject:
            case Collection:
            case Map:
            case GenericType:
                return true;
            case JSON5Object:
            case JSON5Array:
            case JSON5Element:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 복합 객체를 JSON5Element로 직렬화합니다.
     * 
     * <p>타입에 따라 적절한 직렬화기를 선택하여 처리합니다:
     * <ul>
     *   <li>컬렉션 → CollectionSerializer</li>
     *   <li>맵 → MapSerializer</li>
     *   <li>일반 객체 → ObjectSerializer</li>
     * </ul></p>
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Element
     * @throws SerializationException 직렬화 중 오류가 발생한 경우
     */
    @Override
    public JSON5Element serialize(Object obj, SerializationContext context) {
        if (obj == null) {
            throw SerializationException.unsupportedType("Cannot serialize null object", null);
        }
        
        Types type = Types.of(obj.getClass());
        
        try {
            switch (type) {
                case Collection:
                    return serializeCollection(obj, context);
                    
                case Map:
                    return serializeMap(obj, context);
                    
                case JSON5Object:
                    return (JSON5Object) obj;
                    
                case JSON5Array:
                    return (JSON5Array) obj;
                    
                case JSON5Element:
                    return (JSON5Element) obj;
                    
                case Object:
                case AbstractObject:
                case GenericType:
                default:
                    return serializeObject(obj, context);
            }
        } catch (Exception e) {
            throw SerializationException.unsupportedType(
                "Failed to serialize object of type: " + obj.getClass().getName(), 
                e
            ).addContext("objectType", obj.getClass().getName())
             .addContext("objectValue", obj.toString());
        }
    }
    
    /**
     * 컬렉션을 직렬화합니다.
     * 
     * @param obj 컬렉션 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Array
     */
    private JSON5Array serializeCollection(Object obj, SerializationContext context) {
        if (!(obj instanceof Collection)) {
            throw SerializationException.unsupportedType(
                "Expected Collection but got: " + obj.getClass().getName(), null);
        }
        
        Collection<?> collection = (Collection<?>) obj;
        return collectionSerializer.serializeCollection(collection, null);
    }
    
    /**
     * 맵을 직렬화합니다.
     * 
     * @param obj 맵 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Object
     */
    private JSON5Object serializeMap(Object obj, SerializationContext context) {
        if (!(obj instanceof Map)) {
            throw SerializationException.unsupportedType(
                "Expected Map but got: " + obj.getClass().getName(), null);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>) obj;
        return mapSerializer.serializeMap(map, null);
    }
    
    /**
     * 일반 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Object
     */
    private JSON5Object serializeObject(Object obj, SerializationContext context) {
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        
        if (typeSchema == null) {
            throw SerializationException.unsupportedType(
                "No TypeSchema found for class: " + clazz.getName(), null);
        }
        
        JSON5Object root = new JSON5Object();
        return objectSerializer.serializeObject(typeSchema, obj, root, context);
    }
    
    /**
     * 전략의 우선순위를 반환합니다.
     * 
     * <p>복합 객체는 기본 타입보다 낮은 우선순위(3)를 가집니다.</p>
     * 
     * @return 우선순위 3
     */
    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
