package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 기본형 타입(primitive, wrapper, String, enum, byte[])을 처리하는 TypeHandler입니다.
 * 
 * <p>이 핸들러는 다음 타입들을 처리합니다:</p>
 * <ul>
 *   <li>기본형: byte, short, int, long, float, double, boolean, char</li>
 *   <li>래퍼형: Byte, Short, Integer, Long, Float, Double, Boolean, Character</li>
 *   <li>문자열: String, enum 타입</li>
 *   <li>바이트 배열: byte[]</li>
 *   <li>큰 수: BigDecimal, BigInteger</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class PrimitiveTypeHandler implements TypeHandler {
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        // 기본형 및 단순 타입들 처리
        return Types.isSingleType(type) || 
               type == Types.String || 
               (clazz != null && clazz.isEnum());
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        Types type = Types.of(value.getClass());
        
        // 기본형과 래퍼형은 그대로 반환
        if (Types.isSingleType(type)) {
            return value;
        }
        
        // enum은 String으로 변환
        if (value.getClass().isEnum()) {
            return value.toString();
        }
        
        // String은 그대로 반환
        if (type == Types.String) {
            return value;
        }
        
        // 처리할 수 없는 타입
        throw new SerializationException("PrimitiveTypeHandler cannot handle type: " + value.getClass().getName());
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        if (element == null) {
            return getDefaultValue(targetType);
        }
        
        Types targetTypes = Types.of(targetType);
        
        try {
            // 기본형 처리
            if (Types.isSingleType(targetTypes)) {
                return TypeConverter.convertValue(element, targetTypes);
            }
            
            // enum 처리
            if (targetType.isEnum()) {
                if (element instanceof String) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
                    return Enum.valueOf(enumClass, (String) element);
                }
                throw new DeserializationException("Cannot convert " + element.getClass().getSimpleName() + 
                                                 " to enum " + targetType.getSimpleName());
            }
            
            // String 처리
            if (targetTypes == Types.String) {
                return element.toString();
            }
            
            throw new DeserializationException("PrimitiveTypeHandler cannot handle target type: " + targetType.getName());
            
        } catch (Exception e) {
            if (e instanceof DeserializationException) {
                throw e;
            }
            throw new DeserializationException("Failed to deserialize to " + targetType.getSimpleName(), e);
        }
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.HIGHEST; // 기본형은 최고 우선순위
    }
    
    /**
     * 기본형 타입의 기본값을 반환합니다.
     */
    private Object getDefaultValue(Class<?> type) {
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        return null; // 참조형은 null
    }
}
