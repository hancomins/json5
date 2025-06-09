package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;

/**
 * 기본 타입(primitive) 및 단순 타입의 값 처리를 담당하는 프로세서입니다.
 * 
 * <p>이 클래스는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>기본 타입 (byte, short, int, long, float, double, boolean, char)</li>
 *   <li>래퍼 타입 (Byte, Short, Integer, Long, Float, Double, Boolean, Character)</li>
 *   <li>String 타입</li>
 *   <li>Enum 타입</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class PrimitiveValueProcessor implements ValueProcessor {
    
    @Override
    public boolean canHandle(Types type) {
        return Types.isSingleType(type) || 
               type == Types.String;
    }
    
    @Override
    public Object getValue(Object parent, SchemaValueAbs schema) {
        Object value = null;
        int index = 0;
        int size = schema.getAllSchemaValueList().size();
        
        while (value == null && index < size) {
            SchemaValueAbs duplicatedSchema = schema.getAllSchemaValueList().get(index);
            value = duplicatedSchema.onGetValue(parent);
            
            if (value != null && !schema.equalsValueType(duplicatedSchema)) {
                value = TypeConverter.convertValue(value, duplicatedSchema.getType());
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
        
        // 타입 변환이 필요한 경우 변환 수행
        if (!schema.getValueTypeClass().isAssignableFrom(value.getClass())) {
            value = TypeConverter.convertValue(value, schema.getType());
        }
        
        schema.onSetValue(parent, value);
    }
}
