package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Element;

/**
 * 복합 객체 타입의 값 처리를 담당하는 프로세서입니다.
 * 
 * <p>이 클래스는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>일반 객체 타입 (Object)</li>
 *   <li>추상 타입 (AbstractObject)</li>
 *   <li>JSON5Element 타입</li>
 *   <li>제네릭 타입 (GenericType)</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class ObjectValueProcessor implements ValueProcessor {
    
    @Override
    public boolean canHandle(Types type) {
        return type == Types.Object || 
               type == Types.AbstractObject || 
               type == Types.JSON5Element ||
               type == Types.GenericType;
    }
    
    @Override
    public Object getValue(Object parent, SchemaValueAbs schema) {
        Object value = null;
        int index = 0;
        int size = schema.getAllSchemaValueList().size();
        
        while (value == null && index < size) {
            SchemaValueAbs duplicatedSchema = schema.getAllSchemaValueList().get(index);
            value = duplicatedSchema.onGetValue(parent);
            
            if (value != null && duplicatedSchema.getType() == Types.GenericType) {
                Types inType = Types.of(value.getClass());
                if (Types.isSingleType(inType) || Types.isJSON5Type(inType)) {
                    return value;
                } else {
                    return JSON5Object.fromObject(value);
                }
            }
            
            if (value != null && !schema.equalsValueType(duplicatedSchema)) {
                // 복합 타입은 기본적으로 타입 변환하지 않음
                if (!(schema instanceof ISchemaArrayValue) && !(schema instanceof ISchemaMapValue)) {
                    value = TypeConverter.convertValue(value, duplicatedSchema.getType());
                }
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
        
        // JSON5Element 타입 처리
        if (value instanceof JSON5Element && schema.getType() != Types.JSON5Element) {
            // JSON5Element를 적절한 타입으로 변환
            if (schema.getType() == Types.Object || schema.getType() == Types.AbstractObject) {
                try {
                    if (value instanceof JSON5Object) {
                        Object converted = JSON5Serializer.fromJSON5Object((JSON5Object) value, schema.getValueTypeClass());
                        schema.onSetValue(parent, converted);
                        return;
                    }
                } catch (Exception e) {
                    throw new DeserializationException(
                        SerializerConstants.ERROR_DESERIALIZATION_TYPE_MISMATCH,
                        "Failed to convert JSON5Object to " + schema.getValueTypeClass().getName(),
                        e
                    );
                }
            }
        }
        
        schema.onSetValue(parent, value);
    }
}
