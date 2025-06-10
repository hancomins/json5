package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;

/**
 * 일반 객체 타입(@JSON5Type 어노테이션이 붙은 클래스 등)을 처리하는 TypeHandler입니다.
 * 
 * <p>이 핸들러는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>@JSON5Type 어노테이션이 붙은 사용자 정의 클래스</li>
 *   <li>TypeSchema가 등록된 클래스</li>
 *   <li>기본형, 컬렉션, 맵이 아닌 모든 객체</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class ObjectTypeHandler implements TypeHandler {
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        // GenericType과 AbstractObject는 GenericTypeHandler가 처리하도록 제외
        if (type == Types.GenericType || type == Types.AbstractObject) {
            return false;
        }
        
        return type == Types.Object || 
               (clazz != null && TypeSchemaMap.getInstance().hasTypeInfo(clazz)) ||
               (type != Types.Collection && type != Types.Map && !Types.isSingleType(type) && 
                type != Types.String && clazz != null && !clazz.isEnum());
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        // SerializationEngine 사용 (TypeSchema 기반)
        if (context.getSerializationEngine() != null && 
            TypeSchemaMap.getInstance().hasTypeInfo(value.getClass())) {
            return context.getSerializationEngine().serialize(value);
        }
        
        // Fallback: 기본 JSON5Serializer 사용
        return JSON5Serializer.toJSON5Object(value);
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
        
        // DeserializationEngine 사용 (TypeSchema 기반)
        if (context.getDeserializationEngine() != null && 
            TypeSchemaMap.getInstance().hasTypeInfo(targetType)) {
            return context.getDeserializationEngine().deserialize(obj, targetType);
        }
        
        // Fallback: 기본 JSON5Serializer 사용
        return JSON5Serializer.fromJSON5Object(obj, targetType);
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.NORMAL; // 일반 객체는 보통 우선순위
    }
}
