package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * 컬렉션 타입의 직렬화를 담당하는 클래스입니다.
 * 
 * <p>현재 구현에서는 안정성을 위해 기존 JSON5Serializer의 
 * deprecated 메소드들을 그대로 사용합니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class CollectionSerializer {
    
    /**
     * 컬렉션을 JSON5Array로 직렬화합니다.
     * 
     * @param collection 직렬화할 컬렉션
     * @param valueType 컬렉션 요소의 타입 (null 가능)
     * @return 직렬화된 JSON5Array
     */
    public JSON5Array serializeCollection(Collection<?> collection, Class<?> valueType) {
        return collectionObjectToJSON5Array_deprecated(collection, valueType);
    }
    
    /**
     * 스키마 정보가 있는 컬렉션을 직렬화합니다.
     * 
     * @param collection 직렬화할 컬렉션
     * @param schemaArrayValue 스키마 배열 정보
     * @return 직렬화된 JSON5Array
     */
    public JSON5Array serializeCollectionWithSchema(Collection<?> collection, ISchemaArrayValue schemaArrayValue) {
        return collectionObjectToSONArrayKnownSchema_deprecated(collection, schemaArrayValue);
    }
    
    // 기존 JSON5Serializer의 deprecated 메소드들을 복사
    private JSON5Array collectionObjectToJSON5Array_deprecated(Collection<?> collection, Class<?> valueType) {
        JSON5Array JSON5Array = new JSON5Array();
        Types types = valueType == null ? null : Types.of(valueType);
        for(Object object : collection) {
            if(object instanceof Collection<?>) {
                JSON5Array childArray = collectionObjectToJSON5Array_deprecated((Collection<?>)object, null);
                JSON5Array.add(childArray);
            } else if(object instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                JSON5Object childObject = mapObjectToJSON5Object_deprecated((Map<String, ?>)object, null);
                JSON5Array.add(childObject);
            } else if(types == Types.Object) {
                JSON5Object childObject = toJSON5Object_deprecated(object);
                JSON5Array.add(childObject);
            }
            else {
                JSON5Array.add(object);
            }
        }
        return JSON5Array;
    }
    
    private JSON5Array collectionObjectToSONArrayKnownSchema_deprecated(Collection<?> collection, ISchemaArrayValue ISchemaArrayValue) {
        JSON5Array resultJSON5Array = new JSON5Array();
        JSON5Array JSON5Array = resultJSON5Array;
        Iterator<?> iter = collection.iterator();
        TypeSchema objectValueTypeSchema = ISchemaArrayValue.getObjectValueTypeElement();
        Deque<ArraySerializeDequeueItem> arraySerializeDequeueItems = new ArrayDeque<>();
        ArraySerializeDequeueItem currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, JSON5Array);
        arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
        boolean isGeneric = ISchemaArrayValue.isGenericTypeValue();
        boolean isAbstractObject = ISchemaArrayValue.getEndpointValueType() == Types.AbstractObject;
        while(iter.hasNext()) {
            Object object = iter.next();
            if(object instanceof Collection<?>) {
                JSON5Array childArray = new JSON5Array();
                JSON5Array.add(childArray);
                JSON5Array = childArray;
                iter = ((Collection<?>)object).iterator();
                currentArraySerializeDequeueItem = new ArraySerializeDequeueItem(iter, JSON5Array);
                arraySerializeDequeueItems.add(currentArraySerializeDequeueItem);
            } else if(objectValueTypeSchema == null) {
                if(isGeneric || isAbstractObject) {
                    object = object == null ? null : toJSON5Object_deprecated(object);
                }
                JSON5Array.add(object);
            } else {
                if(object == null)  {
                    JSON5Array.add(null);
                } else {
                    JSON5Object childObject = serializeTypeElement_deprecated(objectValueTypeSchema, object);
                    JSON5Array.add(childObject);
                }
            }
            while(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                ArraySerializeDequeueItem arraySerializeDequeueItem = arraySerializeDequeueItems.getFirst();
                iter = arraySerializeDequeueItem.iterator;
                JSON5Array = arraySerializeDequeueItem.JSON5Array;
                if(!iter.hasNext() && !arraySerializeDequeueItems.isEmpty()) {
                    arraySerializeDequeueItems.removeFirst();
                }
            }
        }
        return resultJSON5Array;
    }
    
    // Helper 메소드들
    private JSON5Object mapObjectToJSON5Object_deprecated(Map<String, ?> map, Class<?> valueType) {
        MapSerializer mapSerializer = new MapSerializer();
        return mapSerializer.serializeMap(map, valueType);
    }
    
    private JSON5Object toJSON5Object_deprecated(Object object) {
        if (object == null) {
            return null;
        }
        
        Class<?> clazz = object.getClass();
        if (!JSON5Serializer.serializable(clazz)) {
            return null;
        }
        
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        if (typeSchema == null) {
            return null;
        }
        
        ObjectSerializer objectSerializer = new ObjectSerializer();
        SerializationContext context = new SerializationContext(object, typeSchema);
        JSON5Object rootJSON5Object = new JSON5Object();
        return objectSerializer.serializeObject(typeSchema, object, rootJSON5Object, context);
    }
    
    private JSON5Object serializeTypeElement_deprecated(TypeSchema typeSchema, Object object) {
        if (object == null) {
            return null;
        }
        
        ObjectSerializer objectSerializer = new ObjectSerializer();
        SerializationContext context = new SerializationContext(object, typeSchema);
        JSON5Object rootJSON5Object = new JSON5Object();
        return objectSerializer.serializeObject(typeSchema, object, rootJSON5Object, context);
    }
    
    /**
     * TypeReference를 사용한 Collection 직렬화
     */
    public <T> JSON5Array serializeWithTypeReference(T collection, JSON5TypeReference<T> typeRef) {
        if (!typeRef.isCollectionType()) {
            throw new JSON5SerializerException("TypeReference must be a Collection type");
        }
        
        if (!(collection instanceof Collection)) {
            throw new JSON5SerializerException("Object must be a Collection instance");
        }
        
        @SuppressWarnings("unchecked")
        Collection<?> coll = (Collection<?>) collection;
        
        CollectionTypeInfo typeInfo = typeRef.analyzeCollectionType();
        Class<?> elementClass = typeInfo.getElementClass();
        
        return serializeCollectionWithElementType(coll, elementClass);
    }
    
    /**
     * 요소 타입 정보를 활용한 Collection 직렬화
     */
    private JSON5Array serializeCollectionWithElementType(Collection<?> collection, Class<?> elementClass) {
        JSON5Array result = new JSON5Array();
        
        for (Object element : collection) {
            Object serializedElement = serializeElementWithTypeInfo(element, elementClass);
            result.add(serializedElement);
        }
        
        return result;
    }
    
    /**
     * 요소를 타입 정보와 함께 직렬화
     */
    private Object serializeElementWithTypeInfo(Object element, Class<?> elementClass) {
        if (element == null) {
            return null;
        }
        
        // 중첩 Collection 처리
        if (element instanceof Collection) {
            CollectionSerializer nestedSerializer = new CollectionSerializer();
            return nestedSerializer.serializeCollection((Collection<?>) element, null);
        }
        
        // 중첩 Map 처리
        if (element instanceof Map) {
            MapSerializer mapSerializer = new MapSerializer();
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) element;
            return mapSerializer.serializeMap(map, null);
        }
        
        // 커스텀 객체 처리
        if (!isPrimitiveOrWrapper(element.getClass())) {
            try {
                // 커스텀 객체를 JSON5Object로 직렬화
                if (JSON5Serializer.serializable(element.getClass())) {
                    TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(element.getClass());
                    if (typeSchema != null) {
                        ObjectSerializer objectSerializer = new ObjectSerializer();
                        SerializationContext context = new SerializationContext(element, typeSchema);
                        JSON5Object rootJSON5Object = new JSON5Object();
                        return objectSerializer.serializeObject(typeSchema, element, rootJSON5Object, context);
                    }
                }
                return null;
            } catch (Exception e) {
                CatchExceptionProvider.getInstance().catchException(
                    "Failed to serialize custom object", e);
                return null;
            }
        }
        
        // 기본 타입은 그대로 반환
        return element;
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
    
    // 내부 클래스
    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        JSON5Array JSON5Array;

        private ArraySerializeDequeueItem(Iterator<?> iterator, JSON5Array JSON5Array) {
            this.iterator = iterator;
            this.JSON5Array = JSON5Array;
        }
    }
}
