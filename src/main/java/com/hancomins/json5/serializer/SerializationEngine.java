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
    
    /**
     * 기본 직렬화 엔진을 생성합니다.
     */
    public SerializationEngine() {
        this.objectSerializer = new ObjectSerializer();
        this.collectionSerializer = new CollectionSerializer();
        this.mapSerializer = new MapSerializer();
    }
    
    /**
     * 주어진 객체를 JSON5Object로 직렬화합니다.
     * 
     * @param obj 직렬화할 객체 (null이 아니어야 함)
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     * @throws IllegalArgumentException obj가 null인 경우
     */
    public JSON5Object serialize(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        Class<?> clazz = obj.getClass();
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        if (typeSchema == null) {
            throw new JSON5SerializerException("No TypeSchema found for class: " + clazz.getName());
        }
        return serializeTypeElement(typeSchema, obj);
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
}
