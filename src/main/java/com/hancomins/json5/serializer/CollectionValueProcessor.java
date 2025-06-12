package com.hancomins.json5.serializer;

import java.util.Collection;

/**
 * 컬렉션 타입의 값 처리를 담당하는 프로세서입니다.
 * 
 * <p>이 클래스는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>List, Set, Queue 등 Collection 인터페이스 구현체</li>
 *   <li>배열 타입</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class CollectionValueProcessor implements ValueProcessor {
    
    @Override
    public boolean canHandle(Types type) {
        return type == Types.Collection;
    }
    
    @Override
    public Object getValue(Object parent, SchemaValueAbs schema) {
        Object value = null;
        int index = 0;
        int size = schema.getAllSchemaValueList().size();
        
        while (value == null && index < size) {
            SchemaValueAbs duplicatedSchema = schema.getAllSchemaValueList().get(index);
            value = duplicatedSchema.onGetValue(parent);
            
            // 컬렉션 타입은 직접 반환 (타입 변환하지 않음)
            if (value != null) {
                return value;
            }
            index++;
        }
        
        return value;
    }
    
    @Override
    public void setValue(Object parent, Object value, SchemaValueAbs schema) {
        if (value == null) {
            schema.onSetValue(parent, null);
            return;
        }
        
        // 컬렉션 타입 호환성 검사
        if (value instanceof Collection && schema instanceof ISchemaArrayValue) {
            schema.onSetValue(parent, value);
            return;
        }
        
        // 배열 타입 처리
        if (value.getClass().isArray() && schema.getType() == Types.Collection) {
            schema.onSetValue(parent, value);
            return;
        }
        
        // 타입 불일치 시 예외 발생
        throw new DeserializationException(
            SerializerConstants.ERROR_DESERIALIZATION_TYPE_MISMATCH,
            "Cannot assign " + value.getClass().getName() + " to " + schema.getValueTypeClass().getName()
        );
    }
}
