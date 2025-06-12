package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map 타입을 처리하는 TypeHandler입니다.
 * 
 * <p>이 핸들러는 java.util.Map의 모든 구현체를 처리합니다:</p>
 * <ul>
 *   <li>HashMap, LinkedHashMap, TreeMap</li>
 *   <li>ConcurrentHashMap</li>
 *   <li>Properties</li>
 * </ul>
 * 
 * <p>Map의 키는 반드시 String이어야 하며, 값은 모든 타입을 지원합니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class MapTypeHandler implements TypeHandler {
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        return type == Types.Map || 
               (clazz != null && Map.class.isAssignableFrom(clazz));
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        if (!(value instanceof Map)) {
            throw new SerializationException("Expected Map but got: " + value.getClass().getName());
        }
        
        Map<?, ?> map = (Map<?, ?>) value;
        
        // SerializationEngine의 MapSerializer 사용
        if (context.getSerializationEngine() != null) {
            @SuppressWarnings("unchecked")
            Map<String, ?> stringMap = (Map<String, ?>) map;
            return context.getSerializationEngine().serializeMap(stringMap, null);
        }
        
        // Fallback: 기본 직렬화
        JSON5Object obj = new JSON5Object();
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            
            // 키는 반드시 String이어야 함
            if (!(key instanceof String)) {
                throw new SerializationException("Map key must be String, but got: " + 
                                                (key == null ? "null" : key.getClass().getName()));
            }
            
            String keyStr = (String) key;
            
            if (val == null) {
                obj.put(keyStr, null);
            } else if (Types.isSingleType(Types.of(val.getClass())) || 
                      val.getClass().isEnum() || 
                      val instanceof String) {
                obj.put(keyStr, val);
            } else if (val instanceof Collection) {
                // 컬렉션은 CollectionTypeHandler로 처리
                TypeHandler collectionHandler = context.getTypeHandlerRegistry().getHandler(Types.Collection, val.getClass());
                if (collectionHandler != null) {
                    Object serializedVal = collectionHandler.handleSerialization(val, context);
                    obj.put(keyStr, serializedVal);
                } else {
                    // Fallback
                    obj.put(keyStr, JSON5Serializer.collectionToJSON5Array((Collection<?>) val));
                }
            } else if (val instanceof Map) {
                // 중첩 Map 재귀 처리
                Object serializedVal = handleSerialization(val, context);
                obj.put(keyStr, serializedVal);
            } else {
                // 복합 객체는 JSON5Serializer로 처리
                obj.put(keyStr, JSON5Serializer.toJSON5Object(val));
            }
        }
        
        return obj;
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        if (element == null) {
            return null;
        }
        
        if (!(element instanceof JSON5Object)) {
            throw new DeserializationException("Expected JSON5Object but got: " + element.getClass().getName());
        }
        
        JSON5Object obj = (JSON5Object) element;
        
        // DeserializationEngine의 MapDeserializer 사용
        if (context.getDeserializationEngine() != null) {
            return context.getDeserializationEngine().deserializeToMap(obj, Object.class);
        }
        
        // Fallback: 기본 역직렬화
        Map<String, Object> result = createMapInstance(targetType);
        
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            result.put(key, value); // 기본적으로 원래 값을 추가
        }
        
        return result;
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.HIGH; // Map은 높은 우선순위
    }
    
    /**
     * 지정된 타입에 해당하는 Map 인스턴스를 생성합니다.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> createMapInstance(Class<?> targetType) throws DeserializationException {
        try {
            // 인터페이스인 경우 기본 구현체 사용
            if (targetType == Map.class) {
                return new HashMap<>();
            } else if (targetType == ConcurrentHashMap.class) {
                return new ConcurrentHashMap<>();
            } else if (targetType == LinkedHashMap.class) {
                return new LinkedHashMap<>();
            } else if (targetType == TreeMap.class) {
                return new TreeMap<>();
            }
            
            // 구체 클래스인 경우 인스턴스 생성 시도
            if (Map.class.isAssignableFrom(targetType)) {
                return (Map<String, Object>) targetType.getDeclaredConstructor().newInstance();
            }
            
            throw new DeserializationException("Cannot create instance of: " + targetType.getName());
            
        } catch (DeserializationException e) {
            throw e;
        } catch (Exception e) {
            throw new DeserializationException("Failed to create Map instance of type: " + targetType.getName(), e);
        }
    }
}
