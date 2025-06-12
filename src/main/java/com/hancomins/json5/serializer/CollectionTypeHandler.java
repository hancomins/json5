package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import java.util.*;

/**
 * 컬렉션 타입(List, Set, Queue 등)을 처리하는 TypeHandler입니다.
 * 
 * <p>이 핸들러는 java.util.Collection의 모든 구현체를 처리합니다:</p>
 * <ul>
 *   <li>List: ArrayList, LinkedList, Vector 등</li>
 *   <li>Set: HashSet, LinkedHashSet, TreeSet 등</li>
 *   <li>Queue: ArrayDeque, LinkedList, PriorityQueue 등</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class CollectionTypeHandler implements TypeHandler {
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        return type == Types.Collection || 
               (clazz != null && Collection.class.isAssignableFrom(clazz));
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof Collection)) {
            throw new SerializationException("Expected Collection but got: " + value.getClass().getName());
        }
        
        Collection<?> collection = (Collection<?>) value;
        
        // SerializationEngine의 CollectionSerializer 사용
        if (context.getSerializationEngine() != null) {
            return context.getSerializationEngine().serializeCollection(collection, null);
        }
        
        // Fallback: 기본 직렬화
        JSON5Array array = new JSON5Array();
        for (Object item : collection) {
            if (item == null) {
                array.add(null);
            } else if (Types.isSingleType(Types.of(item.getClass())) || 
                      item.getClass().isEnum() || 
                      item instanceof String) {
                array.add(item);
            } else if (item instanceof Collection) {
                // 재귀적으로 컬렉션 처리
                Object serializedItem = handleSerialization(item, context);
                array.add(serializedItem);
            } else if (item instanceof Map) {
                // Map은 MapTypeHandler로 처리하도록 위임
                TypeHandler mapHandler = context.getTypeHandlerRegistry().getHandler(Types.Map, item.getClass());
                if (mapHandler != null) {
                    Object serializedItem = mapHandler.handleSerialization(item, context);
                    array.add(serializedItem);
                } else {
                    // Fallback: JSON5Serializer 사용
                    array.add(JSON5Serializer.toJSON5Object(item));
                }
            } else {
                // 복합 객체는 JSON5Serializer로 처리
                array.add(JSON5Serializer.toJSON5Object(item));
            }
        }
        
        return array;
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        if (element == null) {
            return null;
        }
        
        if (!(element instanceof JSON5Array)) {
            throw new DeserializationException("Expected JSON5Array but got: " + element.getClass().getName());
        }
        
        JSON5Array array = (JSON5Array) element;
        
        // DeserializationEngine의 CollectionDeserializer 사용
        if (context.getDeserializationEngine() != null) {
            return context.getDeserializationEngine().deserializeToList(array, Object.class, null, false, null);
        }
        
        // Fallback: 기본 역직렬화
        Collection<Object> result = createCollectionInstance(targetType);
        
        for (int i = 0; i < array.size(); i++) {
            Object item = array.get(i);
            result.add(item); // 기본적으로 원래 값을 추가
        }
        
        return result;
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.HIGH; // 컬렉션은 높은 우선순위
    }
    
    /**
     * 지정된 타입에 해당하는 Collection 인스턴스를 생성합니다.
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> createCollectionInstance(Class<?> targetType) throws DeserializationException {
        try {
            // 인터페이스인 경우 기본 구현체 사용
            if (targetType == Collection.class || targetType == List.class) {
                return new ArrayList<>();
            } else if (targetType == Set.class) {
                return new HashSet<>();
            } else if (targetType == Queue.class || targetType == Deque.class) {
                return new ArrayDeque<>();
            }
            
            // 구체 클래스인 경우 인스턴스 생성 시도
            if (Collection.class.isAssignableFrom(targetType)) {
                return (Collection<Object>) targetType.getDeclaredConstructor().newInstance();
            }
            
            throw new DeserializationException("Cannot create instance of: " + targetType.getName());
            
        } catch (DeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException("Failed to create Collection instance of type: " + targetType.getName(), e);
        }
    }
}
