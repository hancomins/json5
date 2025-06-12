package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * Map 타입의 직렬화를 담당하는 클래스입니다.
 * 
 * <p>Map&lt;String, ?&gt; 형태의 맵을 JSON5Object로 변환하는 로직을 처리합니다.
 * 다양한 값 타입을 지원하며, 중첩된 Map과 Collection도 처리할 수 있습니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class MapSerializer {
    
    /**
     * Map을 JSON5Object로 직렬화합니다.
     * 
     * @param map 직렬화할 Map
     * @param valueType Map 값의 타입 (null 가능)
     * @return 직렬화된 JSON5Object
     */
    public JSON5Object serializeMap(Map<String, ?> map, Class<?> valueType) {
        JSON5Object json5Object = new JSON5Object();
        Set<? extends Map.Entry<String, ?>> entries = map.entrySet();
        Types types = valueType == null ? null : Types.of(valueType);
        
        for (Map.Entry<String, ?> entry : entries) {
            Object value = entry.getValue();
            String key = entry.getKey();
            
            // 값 타입이 지정되지 않은 경우 실제 값의 타입 사용
            if (value != null && valueType == null) {
                valueType = value.getClass();
                validateMapValue(valueType, key);
                types = Types.of(valueType);
            }
            
            Object serializedValue = serializeMapValue(value, types);
            json5Object.put(key, serializedValue);
        }
        
        return json5Object;
    }
    
    /**
     * Map의 개별 값을 직렬화합니다.
     * 
     * @param value 직렬화할 값
     * @param types 값의 타입 정보
     * @return 직렬화된 값
     */
    private Object serializeMapValue(Object value, Types types) {
        if (value == null) {
            return null;
        }
        
        // Collection 처리 강화
        if (value instanceof Collection<?>) {
            CollectionSerializer collectionSerializer = new CollectionSerializer();
            return collectionSerializer.serializeCollection((Collection<?>) value, null);
        }
        
        if (value instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            JSON5Object childObject = serializeMap((Map<String, ?>) value, null);
            return childObject;
        }
        
        if (types == Types.Object) {
            Types actualType = Types.of(value.getClass());
            if (Types.isSingleType(actualType)) {
                return value;
            } else {
                return new SerializationEngine().serialize(value);
            }
        }
        
        return value;
    }
    
    /**
     * Map 값의 유효성을 검증합니다.
     * 
     * @param valueType 값 타입
     * @param key 현재 키
     * @throws JSON5SerializerException 유효하지 않은 타입인 경우
     */
    private void validateMapValue(Class<?> valueType, String key) {
        ISchemaValue.assertValueType(valueType, null);
        
        if (key == null) {
            throw new JSON5SerializerException("Map key type is not String. Please use String key.");
        }
    }
}
