package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * 복합 객체 타입의 역직렬화를 담당하는 전략 클래스
 * 
 * @JSON5Type 어노테이션이 붙은 커스텀 객체나 일반적인 POJO 객체,
 * JSON5Object, JSON5Array, JSON5Element 등의 역직렬화를 처리합니다.
 */
public class ComplexObjectDeserializationStrategy implements DeserializationStrategy {
    
    @Override
    public boolean canHandle(Types type, Class<?> targetType) {
        // JSON5 관련 타입들은 직접 처리
        if (type == Types.JSON5Object || type == Types.JSON5Array || type == Types.JSON5Element) {
            return true;
        }
        
        // 기본 타입들은 처리하지 않음
        if (Types.isSingleType(type)) {
            return false;
        }
        
        // 컬렉션과 맵은 처리
        if (Collection.class.isAssignableFrom(targetType) || Map.class.isAssignableFrom(targetType)) {
            return true;
        }
        
        // 일반 객체 타입 중에서 @JSON5Type 어노테이션이 있는 것만 처리
        if (type == Types.Object && JSON5Serializer.serializable(targetType)) {
            // 기본 타입들과 enum은 제외
            if (targetType.isPrimitive() || targetType.isEnum() || 
                targetType == String.class || Number.class.isAssignableFrom(targetType) ||
                targetType == Boolean.class || targetType == Character.class ||
                targetType == StringBuilder.class || targetType == StringBuffer.class) {
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (json5Element == null) {
            return null;
        }
        
        // JSON5Element 타입 처리
        if (targetType == JSON5Element.class) {
            return json5Element;
        }
        
        // JSON5Object 타입 처리
        if (targetType == JSON5Object.class) {
            if (json5Element instanceof JSON5Object) {
                return json5Element;
            }
            return null;
        }
        
        // JSON5Array 타입 처리
        if (targetType == JSON5Array.class) {
            if (json5Element instanceof JSON5Array) {
                return json5Element;
            }
            return null;
        }
        
        // 컬렉션 타입 처리
        if (Collection.class.isAssignableFrom(targetType)) {
            return deserializeCollection(json5Element, targetType, context);
        }
        
        // Map 타입 처리
        if (Map.class.isAssignableFrom(targetType)) {
            return deserializeMap(json5Element, targetType, context);
        }
        
        // 일반 객체 타입 처리
        return deserializeObject(json5Element, targetType, context);
    }
    
    @Override
    public int getPriority() {
        return 50; // 중간 우선순위
    }
    
    /**
     * 컬렉션 타입을 역직렬화합니다.
     */
    private Object deserializeCollection(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (!(json5Element instanceof JSON5Array)) {
            return null;
        }
        
        JSON5Array json5Array = (JSON5Array) json5Element;
        
        try {
            // 구체적인 컬렉션 클래스 결정
            Collection<Object> collection = createCollectionInstance(targetType);
            if (collection == null) {
                return null;
            }
            
            // 배열의 각 요소를 Object로 변환하여 추가
            for (int i = 0; i < json5Array.size(); i++) {
                Object element = json5Array.get(i);
                collection.add(element);
            }
            
            return collection;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Map 타입을 역직렬화합니다.
     */
    private Object deserializeMap(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (!(json5Element instanceof JSON5Object)) {
            return null;
        }
        
        JSON5Object json5Object = (JSON5Object) json5Element;
        
        try {
            // 구체적인 Map 클래스 결정
            Map<String, Object> map = createMapInstance(targetType);
            if (map == null) {
                return null;
            }
            
            // JSON5Object의 각 키-값 쌍을 Map에 추가
            for (String key : json5Object.keySet()) {
                Object value = json5Object.get(key);
                map.put(key, value);
            }
            
            return map;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 일반 객체를 역직렬화합니다.
     */
    private Object deserializeObject(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
        if (!(json5Element instanceof JSON5Object)) {
            return null;
        }
        
        JSON5Object json5Object = (JSON5Object) json5Element;
        
        try {
            // 인스턴스 생성
            Object instance = createInstance(targetType);
            if (instance == null) {
                return null;
            }
            
            // ObjectDeserializer를 사용하여 역직렬화
            ObjectDeserializer objectDeserializer = new ObjectDeserializer();
            return objectDeserializer.deserialize(json5Object, instance);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 컬렉션 인스턴스를 생성합니다.
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> createCollectionInstance(Class<?> collectionType) {
        try {
            // 인터페이스인 경우 구체 클래스로 매핑
            if (collectionType.isInterface()) {
                if (List.class.isAssignableFrom(collectionType)) {
                    return new ArrayList<>();
                } else if (Set.class.isAssignableFrom(collectionType)) {
                    return new HashSet<>();
                } else if (Queue.class.isAssignableFrom(collectionType)) {
                    return new LinkedList<>();
                } else if (Collection.class.isAssignableFrom(collectionType)) {
                    return new ArrayList<>(); // 기본값
                }
            } else {
                // 구체 클래스인 경우 직접 인스턴스 생성
                return (Collection<Object>) collectionType.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // 인스턴스 생성 실패 시 기본값 반환
        }
        
        return new ArrayList<>(); // 기본값
    }
    
    /**
     * Map 인스턴스를 생성합니다.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> createMapInstance(Class<?> mapType) {
        try {
            // 인터페이스인 경우 구체 클래스로 매핑
            if (mapType.isInterface()) {
                if (Map.class.isAssignableFrom(mapType)) {
                    return new HashMap<>();
                }
            } else {
                // 구체 클래스인 경우 직접 인스턴스 생성
                return (Map<String, Object>) mapType.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            // 인스턴스 생성 실패 시 기본값 반환
        }
        
        return new HashMap<>(); // 기본값
    }
    
    /**
     * 클래스의 인스턴스를 생성합니다.
     */
    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
