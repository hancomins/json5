package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

import java.util.*;

/**
 * Map 타입의 역직렬화를 담당하는 클래스
 * JSON5Serializer의 fromJSON5ObjectToMap 메소드의 복잡한 로직을 분리하여
 * Map 역직렬화만을 전담으로 처리합니다.
 */
public class MapDeserializer {
    
    /**
     * JSON5Object를 Map으로 역직렬화
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param valueType Map의 값 타입
     * @param <T> 값 타입
     * @return 역직렬화된 Map
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> deserialize(JSON5Object json5Object, Class<T> valueType) {
        // 타입 검증
        validateValueType(valueType);
        
        return (Map<String, T>) deserialize(null, json5Object, valueType, null, null);
    }
    
    /**
     * JSON5Object를 기존 Map에 역직렬화 (DeserializationContext 사용)
     * 
     * @param target 대상 Map
     * @param json5Object 역직렬화할 JSON5Object
     * @param valueType 값 타입
     * @param context 역직렬화 컨텍스트
     * @return 역직렬화된 Map
     */
    @SuppressWarnings("rawtypes")
    public Map deserialize(Map target, JSON5Object json5Object, Class<?> valueType, 
                         DeserializationContext context) {
        return deserialize(target, json5Object, valueType, context, null);
    }
    
    /**
     * JSON5Object를 기존 Map에 역직렬화 (OnObtainTypeValue 포함)
     * 
     * @param target 대상 Map
     * @param json5Object 역직렬화할 JSON5Object
     * @param valueType 값 타입
     * @param context 역직렬화 컨텍스트
     * @param onObtainTypeValue 타입 값 획득 함수
     * @return 역직렬화된 Map
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map deserialize(Map target, JSON5Object json5Object, Class<?> valueType,
                           @SuppressWarnings("unused") DeserializationContext context, OnObtainTypeValue onObtainTypeValue) {
        Types types = Types.of(valueType);
        if (target == null) {
            target = new HashMap<>();
        }
        
        Map finalTarget = target;
        
        // Collection 타입 처리 추가
        if (Collection.class.isAssignableFrom(valueType)) {
            json5Object.keySet().forEach(key -> {
                JSON5Array arrayValue = json5Object.getJSON5Array(key, null);
                if (arrayValue != null) {
                    try {
                        // CollectionDeserializer 활용
                        CollectionDeserializer collectionDeserializer = new CollectionDeserializer();
                        
                        // Collection의 제네릭 타입 추출 (Object로 fallback)
                        Class<?> elementType = Object.class; // 기본값
                        
                        // 단순한 List 생성 (구체적인 Collection 타입은 추후 개선)
                        List<?> deserializedCollection = collectionDeserializer.deserializeToList(
                            arrayValue, elementType, null, true, null);
                        
                        finalTarget.put(key, deserializedCollection);
                    } catch (Exception e) {
                        CatchExceptionProvider.getInstance().catchException(
                            "Failed to deserialize collection value for key: " + key, e);
                        finalTarget.put(key, null);
                    }
                } else {
                    finalTarget.put(key, null);
                }
            });
            return target;
        }
        
        if (onObtainTypeValue != null) {
            // 제네릭/추상 타입 처리
            json5Object.keySet().forEach(key -> {
                Object childInJson5Object = json5Object.get(key);
                if (childInJson5Object == null) {
                    finalTarget.put(key, null);
                    return;
                }
                
                Object targetChild = onObtainTypeValue.obtain(childInJson5Object);
                if (targetChild == null) {
                    finalTarget.put(key, null);
                    return;
                }
                
                Types targetChildTypes = Types.of(targetChild.getClass());
                if (childInJson5Object instanceof JSON5Object && !Types.isSingleType(targetChildTypes)) {
                    ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                    objectDeserializer.deserialize((JSON5Object) childInJson5Object, targetChild);
                }
                finalTarget.put(key, targetChild);
            });
        } else if (Types.isSingleType(types)) {
            // 기본 타입 처리
            json5Object.keySet().forEach(key -> {
                Object value = JSON5ElementExtractor.getFrom(json5Object, key, types);
                finalTarget.put(key, value);
            });
        } else if (types == Types.Object) {
            // 객체 타입 처리
            json5Object.keySet().forEach(key -> {
                JSON5Object child = json5Object.getJSON5Object(key, null);
                if (child != null) {
                    Object targetChild = createInstance(valueType);
                    if (targetChild != null) {
                        ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                        targetChild = objectDeserializer.deserialize(child, targetChild);
                        finalTarget.put(key, targetChild);
                    } else {
                        finalTarget.put(key, null);
                    }
                } else {
                    finalTarget.put(key, null);
                }
            });
        } else if (types == Types.JSON5Object) {
            // JSON5Object 타입 처리
            json5Object.keySet().forEach(key -> {
                JSON5Object child = json5Object.getJSON5Object(key, null);
                finalTarget.put(key, child);
            });
        } else if (types == Types.JSON5Array) {
            // JSON5Array 타입 처리
            json5Object.keySet().forEach(key -> {
                JSON5Array child = json5Object.getJSON5Array(key, null);
                finalTarget.put(key, child);
            });
        } else if (types == Types.JSON5Element) {
            // JSON5Element 타입 처리
            json5Object.keySet().forEach(key -> {
                Object child = json5Object.get(key);
                if (child instanceof JSON5Element) {
                    finalTarget.put(key, child);
                } else {
                    finalTarget.put(key, null);
                }
            });
        }
        
        return target;
    }
    
    /**
     * 값 타입 검증
     */
    private void validateValueType(Class<?> valueType) {
        if (valueType.isPrimitive()) {
            throw new JSON5SerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } 
        // Collection 타입 제한 제거 (기존 제한을 완화)
        else //noinspection StatementWithEmptyBody
            if (Collection.class.isAssignableFrom(valueType)) {
            // Collection 타입을 이제 허용 - 더 이상 예외를 던지지 않음
            // throw new JSON5SerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection. valueType=" + valueType.getName());
        } 
        else if (Map.class.isAssignableFrom(valueType)) {
            throw new JSON5SerializerException("valueType is java.util.Map type. Use a class that wraps your Map. valueType=" + valueType.getName());
        } else if (valueType.isArray() && Types.ByteArray != Types.of(valueType)) {
            throw new JSON5SerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
    }
    
    /**
     * 인스턴스 생성
     */
    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * OnObtainTypeValue 함수형 인터페이스
     */
    @FunctionalInterface
    public interface OnObtainTypeValue {
        Object obtain(Object target);
    }
}
