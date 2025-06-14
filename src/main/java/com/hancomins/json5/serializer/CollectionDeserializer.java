package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.DataConverter;
import com.hancomins.json5.serializer.polymorphic.TypeInfoAnalyzer;
import com.hancomins.json5.serializer.polymorphic.PolymorphicDeserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 컬렉션 타입의 역직렬화를 담당하는 클래스
 * 
 * JSON5Serializer의 json5ArrayToList, json5ArrayToCollectionObject 메소드의 
 * 복잡한 로직을 분리하여 컬렉션 역직렬화만을 전담으로 처리합니다.
 */
public class CollectionDeserializer {
    
    private final TypeInfoAnalyzer typeInfoAnalyzer;
    private final PolymorphicDeserializer polymorphicDeserializer;
    
    public CollectionDeserializer() {
        this.typeInfoAnalyzer = new TypeInfoAnalyzer();
        this.polymorphicDeserializer = new PolymorphicDeserializer();
    }
    
    /**
     * JSON5Array를 List로 역직렬화
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param valueType List의 요소 타입
     * @param writingOptions 작성 옵션
     * @param ignoreError 오류 무시 여부
     * @param defaultValue 기본값
     * @param <T> 요소 타입
     * @return 역직렬화된 List
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> deserializeToList(JSON5Array json5Array, Class<T> valueType, 
                                       WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        // 타입 검증
        validateValueType(valueType, ignoreError);
        
        Types types = Types.of(valueType);
        ArrayList<T> result = new ArrayList<>();
        
        for (int i = 0, n = json5Array.size(); i < n; ++i) {
            Object value = json5Array.get(i);
            if (value == null) {
                result.add(defaultValue);
                continue;
            }
            
            try {
                T convertedValue = convertValue(value, valueType, types, writingOptions, ignoreError, defaultValue);
                result.add(convertedValue);
            } catch (Exception e) {
                if (ignoreError) {
                    result.add(defaultValue);
                } else {
                    throw e;
                }
            }
        }
        
        return result;
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
        deserializeToCollection(json5Array, schemaArrayValue, parent, context, null);
    }
    
    /**
     * JSON5Array를 Collection으로 역직렬화 (Schema 기반, OnObtainTypeValue 포함)
     * 
     * @param json5Array 역직렬화할 JSON5Array
     * @param schemaArrayValue 스키마 배열 값
     * @param parent 부모 객체
     * @param context 역직렬화 컨텍스트
     * @param onObtainTypeValue 타입 값 획득 함수
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void deserializeToCollection(JSON5Array json5Array, ISchemaArrayValue schemaArrayValue, 
                                      Object parent, DeserializationContext context, 
                                      OnObtainTypeValue onObtainTypeValue) {
        List<CollectionItems> collectionItems = schemaArrayValue.getCollectionItems();
        int collectionItemIndex = 0;
        final int collectionItemSize = collectionItems.size();
        
        if (collectionItemSize == 0) {
            return;
        }
        
        CollectionItems collectionItem = collectionItems.get(collectionItemIndex);
        ArrayList<ArrayDeserializeItem> arrayDeserializeItems = new ArrayList<>();
        ArrayDeserializeItem objectItem = new ArrayDeserializeItem(json5Array, collectionItem.newInstance());
        int end = objectItem.getEndIndex();
        arrayDeserializeItems.add(objectItem);
        
        for (int index = 0; index <= end; ++index) {
            objectItem.setArrayIndex(index);
            
            if (collectionItem.isGeneric() || collectionItem.isAbstractType()) {
                // 제네릭 또는 추상 타입 처리
                JSON5Object json5Object = objectItem.json5Array.getJSON5Object(index);
                Object object = null;
                
                // 1. 다형성 타입인지 먼저 확인
                Class<?> elementType = collectionItem.getValueClass();
                if (elementType != null && typeInfoAnalyzer.isPolymorphicType(elementType) && json5Object != null) {
                    try {
                        object = polymorphicDeserializer.deserialize(json5Object, elementType);
                    } catch (Exception e) {
                        // 다형성 역직렬화 실패 시 기존 방식으로 fallback
                        CatchExceptionProvider.getInstance().catchException( "Polymorphic collection deserialization failed, falling back to @ObtainTypeValue", e);
                    }
                }
                
                // 2. 다형성 역직렬화에 실패했거나 다형성 타입이 아닌 경우 기존 방식 사용
                if (object == null) {
                    object = onObtainTypeValue != null ? onObtainTypeValue.obtain(json5Object) : null;
                }
                
                objectItem.collectionObject.add(object);
            } else if (collectionItem.getValueClass() != null) {
                // 기본 값 타입 처리
                Object value = getValueFromJSON5Array(objectItem.json5Array, index, schemaArrayValue);
                objectItem.collectionObject.add(value);
            } else {
                // 중첩 배열 처리
                JSON5Array innerArray = objectItem.json5Array.getJSON5Array(index);
                if (innerArray == null) {
                    objectItem.collectionObject.add(null);
                } else {
                    collectionItem = collectionItems.get(++collectionItemIndex);
                    Collection newCollection = collectionItem.newInstance();
                    objectItem.collectionObject.add(newCollection);
                    ArrayDeserializeItem newArrayDeserializeItem = new ArrayDeserializeItem(innerArray, newCollection);
                    arrayDeserializeItems.add(newArrayDeserializeItem);
                    index = -1;
                    end = newArrayDeserializeItem.getEndIndex();
                    objectItem = newArrayDeserializeItem;
                }
            }
            
            // 현재 배열 처리 완료 시 상위 레벨로 이동
            while (index == end) {
                arrayDeserializeItems.remove(arrayDeserializeItems.size() - 1);
                if (arrayDeserializeItems.isEmpty()) {
                    break;
                }
                objectItem = arrayDeserializeItems.get(arrayDeserializeItems.size() - 1);
                index = objectItem.index;
                end = objectItem.arraySize - 1;
                collectionItem = collectionItems.get(--collectionItemIndex);
            }
        }
        
        // 최종 결과를 부모 객체에 설정
        schemaArrayValue.setValue(parent, objectItem.collectionObject);
    }
    
    /**
     * 값 타입 검증
     */
    private void validateValueType(Class<?> valueType, boolean ignoreError) {
        if (valueType.isPrimitive()) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is primitive type. valueType=" + valueType.getName());
        } else if (Collection.class.isAssignableFrom(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is java.util.Collection type. Use a class that wraps your Collection. valueType=" + valueType.getName());
        } else if (Map.class.isAssignableFrom(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is java.util.Map type. Use a class that wraps your Map. valueType=" + valueType.getName());
        } else if (valueType.isArray() && Types.ByteArray != Types.of(valueType)) {
            if (ignoreError) {
                return;
            }
            throw new JSON5SerializerException("valueType is Array type. ArrayType cannot be used. valueType=" + valueType.getName());
        }
    }
    
    /**
     * 값 변환
     */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> valueType, Types types, 
                             WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        if (Number.class.isAssignableFrom(valueType)) {
            try {
                Number no = DataConverter.toBoxingNumberOfType(value, (Class<? extends Number>) valueType);
                return (T) no;
            } catch (NumberFormatException e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("valueType is Number type. But value is not Number type. valueType=" + valueType.getName());
            }
        } else if (Boolean.class == valueType) {
            if (value.getClass() == Boolean.class) {
                return (T) value;
            } else {
                return "true".equals(value.toString()) ? (T) Boolean.TRUE : (T) Boolean.FALSE;
            }
        } else if (Character.class == valueType) {
            try {
                if (value.getClass() == Character.class) {
                    return (T) value;
                } else {
                    return (T) (Character) DataConverter.toChar(value);
                }
            } catch (NumberFormatException e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("valueType is Character type. But value is not Character type. valueType=" + valueType.getName());
            }
        } else if (valueType == String.class) {
            if (writingOptions != null && value instanceof JSON5Element) {
                return (T) ((JSON5Element) value).toString(writingOptions);
            } else {
                return (T) value.toString();
            }
        } else if (value instanceof JSON5Object && JSON5Serializer.serializable(valueType)) {
            try {
                // ObjectDeserializer를 사용하여 역직렬화
                ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                Object target = valueType.getDeclaredConstructor().newInstance();
                value = objectDeserializer.deserialize((JSON5Object) value, target);
            } catch (Exception e) {
                if (ignoreError) {
                    return defaultValue;
                }
                throw new JSON5SerializerException("Failed to deserialize object. valueType=" + valueType.getName(), e);
            }
            return (T) value;
        }
        
        return defaultValue;
    }
    
    /**
     * JSON5Array에서 특정 인덱스의 값을 타입에 맞게 추출
     */
    private Object getValueFromJSON5Array(JSON5Array json5Array, int index, ISchemaArrayValue schemaArrayValue) {
        switch (schemaArrayValue.getEndpointValueType()) {
            case Byte:
                return json5Array.getByte(index);
            case Short:
                return json5Array.getShort(index);
            case Integer:
                return json5Array.getInt(index);
            case Long:
                return json5Array.getLong(index);
            case Float:
                return json5Array.getFloat(index);
            case Double:
                return json5Array.getDouble(index);
            case Boolean:
                return json5Array.getBoolean(index);
            case Character:
                return json5Array.getChar(index, '\0');
            case String:
                return json5Array.getString(index);
            case JSON5Array:
                return json5Array.getJSON5Array(index);
            case JSON5Object:
                return json5Array.getJSON5Object(index);
            case Object:
                JSON5Object json5Object = json5Array.getJSON5Object(index);
                if (json5Object != null) {
                    Object target = schemaArrayValue.getObjectValueTypeElement().newInstance();
                    ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                    return objectDeserializer.deserialize(json5Object, target);
                }
                break;
        }
        return null;
    }
    
    /**
     * TypeReference를 사용한 완전한 제네릭 타입 지원 Collection 역직렬화
     */
    @SuppressWarnings("unchecked")
    public <T> T deserializeWithTypeReference(JSON5Array json5Array, JSON5TypeReference<T> typeRef) {
        if (!typeRef.isCollectionType()) {
            throw new JSON5SerializerException("TypeReference must be a Collection type");
        }
        
        CollectionTypeInfo typeInfo = typeRef.analyzeCollectionType();
        
        Class<?> collectionClass = typeInfo.getCollectionClass();
        Type elementType = typeInfo.getElementType();
        Class<?> elementClass = typeInfo.getElementClass();
        
        // Collection 생성
        Collection<Object> result = createCollectionInstance(collectionClass);
        
        // 각 요소를 제네릭 타입 정보와 함께 역직렬화
        for (int i = 0; i < json5Array.size(); i++) {
            Object jsonElement = json5Array.get(i);
            Object deserializedElement = deserializeElementWithTypeInfo(jsonElement, elementType, elementClass);
            result.add(deserializedElement);
        }
        
        return (T) result;
    }
    
    /**
     * Collection 인스턴스 생성
     */
    private Collection<Object> createCollectionInstance(Class<?> collectionClass) {
        if (collectionClass.isInterface()) {
            if (List.class.isAssignableFrom(collectionClass)) {
                return new ArrayList<>();
            } else if (Set.class.isAssignableFrom(collectionClass)) {
                return new HashSet<>();
            } else {
                return new ArrayList<>(); // 기본값
            }
        } else {
            try {
                @SuppressWarnings("unchecked")
                Collection<Object> instance = (Collection<Object>) collectionClass.getDeclaredConstructor().newInstance();
                return instance;
            } catch (Exception e) {
                return new ArrayList<>(); // fallback
            }
        }
    }
    
    /**
     * 요소를 타입 정보와 함께 역직렬화
     */
    private Object deserializeElementWithTypeInfo(Object jsonElement, Type elementType, Class<?> elementClass) {
        if (jsonElement == null) {
            return null;
        }
        
        // 중첩 Collection 처리
        if (Collection.class.isAssignableFrom(elementClass) && jsonElement instanceof JSON5Array) {
            // 재귀적으로 중첩 Collection 처리
            return deserializeNestedCollection((JSON5Array) jsonElement, elementType);
        }
        
        // 중첩 Map 처리
        if (Map.class.isAssignableFrom(elementClass) && jsonElement instanceof JSON5Object) {
            // 중첩 Map 처리
            return deserializeNestedMapInCollection((JSON5Object) jsonElement, elementType);
        }
        
        // 커스텀 객체 처리
        if (!isPrimitiveOrWrapper(elementClass) && jsonElement instanceof JSON5Object) {
            return deserializeCustomObject((JSON5Object) jsonElement, elementClass);
        }
        
        // 기본 타입 변환
        try {
            return DataConverter.convertValue(elementClass, jsonElement);
        } catch (Exception e) {
            CatchExceptionProvider.getInstance().catchException(
                "Failed to convert element to type " + elementClass.getName(), e);
            return null;
        }
    }
    
    /**
     * 중첩 Collection 역직렬화
     */
    private Object deserializeNestedCollection(JSON5Array jsonArray, Type elementType) {
        if (elementType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) elementType;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            
            if (Collection.class.isAssignableFrom(rawType)) {
                // 기본 List로 처리 (동적 TypeReference 생성은 복잡하므로 간단히 구현)
                try {
                    Collection<Object> nestedResult = createCollectionInstance(rawType);
                    Type nestedElementType = paramType.getActualTypeArguments()[0];
                    // 🎯 핵심 수정: 안전한 타입 추출
                    Class<?> nestedElementClass = extractSafeClass(nestedElementType);
                    
                    for (int i = 0; i < jsonArray.size(); i++) {
                        Object jsonElement = jsonArray.get(i);
                        Object deserializedElement = deserializeElementWithTypeInfo(jsonElement, nestedElementType, nestedElementClass);
                        nestedResult.add(deserializedElement);
                    }
                    
                    return nestedResult;
                } catch (Exception e) {
                    CatchExceptionProvider.getInstance().catchException(
                        "Failed to deserialize nested collection", e);
                    return null;
                }
            }
        }
        
        // fallback - 기본 List로 처리
        return deserializeToList(jsonArray, Object.class, null, true, null);
    }
    
    /**
     * Collection 내 중첩 Map 역직렬화
     */
    private Object deserializeNestedMapInCollection(JSON5Object jsonObject, Type elementType) {
        if (elementType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) elementType;
            Class<?> rawType = (Class<?>) paramType.getRawType();
            
            if (Map.class.isAssignableFrom(rawType)) {
                // Map 타입 정보 추출
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length == 2) {
                    try {
                        MapTypeInfo mapTypeInfo = new MapTypeInfo(typeArgs[0], typeArgs[1]);
                        Object result = deserializeMapWithTypeInfo(jsonObject, mapTypeInfo);
                        return result;
                    } catch (Exception e) {
                        CatchExceptionProvider.getInstance().catchException(
                            "Failed to deserialize nested map in collection", e);
                    }
                }
            }
        }
        
        // fallback - 기본 Map으로 처리
        return jsonObject;
    }
    
    /**
     * Map을 타입 정보와 함께 역직렬화
     */
    private Object deserializeMapWithTypeInfo(JSON5Object jsonObject, MapTypeInfo mapTypeInfo) {
        try {
            Map<Object, Object> result = new HashMap<>();
            Class<?> keyClass = mapTypeInfo.getKeyClass();
            Class<?> valueClass = mapTypeInfo.getValueClass();
            
            for (String keyStr : jsonObject.keySet()) {
                // Key 변환
                Object convertedKey;
                try {
                    if (keyClass == String.class) {
                        convertedKey = keyStr;
                    } else {
                        convertedKey = MapKeyConverter.convertStringToKey(keyStr, keyClass);
                    }
                    
                    if (convertedKey == null) {
                        CatchExceptionProvider.getInstance().catchException(
                            "Failed to convert key: " + keyStr, new RuntimeException("Key conversion returned null"));
                        continue;
                    }
                } catch (Exception e) {
                    CatchExceptionProvider.getInstance().catchException(
                        "Failed to convert key '" + keyStr + "' to type " + keyClass.getName(), e);
                    continue;
                }
                
                // Value 변환
                Object jsonValue = jsonObject.get(keyStr);
                Object convertedValue = convertMapValueWithTypeInfo(jsonValue, mapTypeInfo, valueClass);
                
                result.put(convertedKey, convertedValue);
            }
            
            return result;
        } catch (Exception e) {
            CatchExceptionProvider.getInstance().catchException(
                "Failed to deserialize map with type info", e);
            return jsonObject; // fallback
        }
    }
    
    /**
     * Map 값을 타입 정보에 따라 변환
     */
    private Object convertMapValueWithTypeInfo(Object jsonValue, MapTypeInfo mapTypeInfo, Class<?> valueClass) {
        if (jsonValue == null) {
            return null;
        }
        
        // Collection 값 처리
        if (mapTypeInfo.isValueCollection() && jsonValue instanceof JSON5Array) {
            Type valueElementType = mapTypeInfo.getValueElementType();
            Class<?> valueElementClass = extractSafeClass(valueElementType);
            
            Collection<Object> valueCollection = createCollectionInstance(valueClass);
            JSON5Array valueArray = (JSON5Array) jsonValue;
            
            for (int i = 0; i < valueArray.size(); i++) {
                Object element = valueArray.get(i);
                Object convertedElement = convertElementValue(element, valueElementClass);
                valueCollection.add(convertedElement);
            }
            return valueCollection;
        }
        
        // 중첩 Map 값 처리
        if (mapTypeInfo.isValueNestedMap() && jsonValue instanceof JSON5Object) {
            // 재귀적으로 중첩 Map 처리
            Type valueType = mapTypeInfo.getValueType();
            if (valueType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) valueType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length == 2) {
                    MapTypeInfo nestedMapTypeInfo = new MapTypeInfo(typeArgs[0], typeArgs[1]);
                    return deserializeMapWithTypeInfo((JSON5Object) jsonValue, nestedMapTypeInfo);
                }
            }
        }
        
        // 커스텀 객체 처리
        if (jsonValue instanceof JSON5Object && !isPrimitiveOrWrapper(valueClass)) {
            try {
                Object instance = valueClass.getDeclaredConstructor().newInstance();
                ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                return objectDeserializer.deserialize((JSON5Object) jsonValue, instance);
            } catch (Exception e) {
                CatchExceptionProvider.getInstance().catchException(
                    "Failed to deserialize custom object of type " + valueClass.getName(), e);
                return null;
            }
        }
        
        // 기본 타입 변환
        return convertElementValue(jsonValue, valueClass);
    }
    
    /**
     * 요소 값 변환 (타입 안전 처리)
     */
    private Object convertElementValue(Object value, Class<?> targetClass) {
        if (value == null) {
            return null;
        }
        
        // JSON5Object를 커스텀 객체로 변환
        if (value instanceof JSON5Object && !isPrimitiveOrWrapper(targetClass)) {
            try {
                Object instance = targetClass.getDeclaredConstructor().newInstance();
                ObjectDeserializer objectDeserializer = new ObjectDeserializer();
                return objectDeserializer.deserialize((JSON5Object) value, instance);
            } catch (Exception e) {
                CatchExceptionProvider.getInstance().catchException(
                    "Failed to deserialize element to type " + targetClass.getName(), e);
                return null;
            }
        }
        
        // 기본 타입 변환
        try {
            return DataConverter.convertValue(targetClass, value);
        } catch (Exception e) {
            CatchExceptionProvider.getInstance().catchException(
                "Failed to convert element value to type " + targetClass.getName(), e);
            return null;
        }
    }
    
    /**
     * 커스텀 객체 역직렬화
     */
    private Object deserializeCustomObject(JSON5Object jsonObject, Class<?> elementClass) {
        try {
            ObjectDeserializer objectDeserializer = new ObjectDeserializer();
            Object instance = elementClass.getDeclaredConstructor().newInstance();
            return objectDeserializer.deserialize(jsonObject, instance);
        } catch (Exception e) {
            CatchExceptionProvider.getInstance().catchException(
                "Failed to deserialize custom object of type " + elementClass.getName(), e);
            return null;
        }
    }
    
    /**
     * primitive 또는 wrapper 타입인지 확인
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               type == Integer.class || type == Long.class || type == Double.class ||
               type == Float.class || type == Boolean.class || type == Character.class ||
               type == Byte.class || type == Short.class;
    }
    
    /**
     * Type에서 안전하게 Class를 추출하는 유틸리티 메서드
     * ParameterizedType인 경우 RawType을 반환하여 ClassCastException 방지
     */
    private Class<?> extractSafeClass(Type type) {
        if (type == null) {
            return Object.class;
        }
        
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            // ParameterizedType인 경우 RawType 추출
            // 예: Map<String,User> -> Map.class
            ParameterizedType paramType = (ParameterizedType) type;
            return (Class<?>) paramType.getRawType();
        } else {
            // 기타 타입 (GenericArrayType, WildcardType 등)
            return Object.class;
        }
    }
    
    /**
     * OnObtainTypeValue 함수형 인터페이스
     */
    @FunctionalInterface
    public interface OnObtainTypeValue {
        Object obtain(Object target);
    }
    
    /**
     * 배열 역직렬화 스택 아이템
     */
    @SuppressWarnings("rawtypes")
    private static class ArrayDeserializeItem {
        final JSON5Array json5Array;
        final Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        
        private ArrayDeserializeItem(JSON5Array json5Array, Collection collection) {
            this.json5Array = json5Array;
            this.arraySize = json5Array.size();
            this.collectionObject = collection;
        }
        
        private int getEndIndex() {
            return arraySize - 1;
        }
        
        /**
         * 역직렬화에서 사용됨.
         */
        private void setArrayIndex(int index) {
            this.index = index;
        }
    }
}
