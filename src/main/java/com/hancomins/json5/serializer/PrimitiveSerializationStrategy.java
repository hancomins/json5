package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;

/**
 * 기본 타입 직렬화 전략 구현체입니다.
 * 
 * <p>이 클래스는 기본형(primitive), 래퍼(wrapper), String, 
 * ByteArray 등의 단순한 타입들에 대한 직렬화를 담당합니다.</p>
 * 
 * <h3>지원하는 타입:</h3>
 * <ul>
 *   <li>기본형: byte, short, int, long, float, double, boolean, char</li>
 *   <li>래퍼형: Byte, Short, Integer, Long, Float, Double, Boolean, Character</li>
 *   <li>String 및 enum 타입</li>
 *   <li>byte[] 배열</li>
 *   <li>BigDecimal, BigInteger</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 * @see SerializationStrategy
 * @see ComplexObjectSerializationStrategy
 */
public class PrimitiveSerializationStrategy implements SerializationStrategy {
    
    /**
     * 우선순위 상수 - 기본 타입은 높은 우선순위를 가집니다.
     */
    private static final int PRIORITY = 1;
    
    /**
     * 이 전략이 주어진 객체와 타입을 처리할 수 있는지 확인합니다.
     * 
     * @param obj 처리할 객체
     * @param type 객체의 타입 정보
     * @return 기본 타입이면 true, 그렇지 않으면 false
     */
    @Override
    public boolean canHandle(Object obj, Types type) {
        if (obj == null) {
            return false;
        }
        
        // 기본형 및 단순 타입들 처리
        return Types.isSingleType(type);
    }
    
    /**
     * 기본 타입 객체를 JSON5Element로 직렬화합니다.
     * 
     * <p>기본 타입들은 JSON5의 primitive 값으로 직접 변환되므로
     * 별도의 JSON5Object나 JSON5Array를 생성하지 않고
     * 원시 값을 그대로 반환합니다.</p>
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트 (기본 타입에서는 미사용)
     * @return 직렬화된 원시 값을 담은 JSON5Element (실제로는 원시 값 자체)
     * @throws SerializationException 직렬화 중 오류가 발생한 경우
     */
    @Override
    public JSON5Element serialize(Object obj, SerializationContext context) {
        if (obj == null) {
            throw SerializationException.unsupportedType("Cannot serialize null object", null);
        }
        
        // 기본 타입들은 JSON5Element로 감싸지 않고 원시 값으로 처리
        // JSON5Object.put() 메소드가 내부적으로 적절히 처리함
        
        // 이 메소드는 실제로는 호출되지 않을 가능성이 높음
        // SerializationEngine에서 기본 타입들은 직접 처리하기 때문
        
        // 하지만 일관성을 위해 구현
        if (obj instanceof String) {
            // String은 JSON5Object에 직접 추가 가능
            return createPrimitiveWrapper(obj);
        } else if (obj instanceof Number) {
            // 숫자형은 JSON5Object에 직접 추가 가능
            return createPrimitiveWrapper(obj);
        } else if (obj instanceof Boolean) {
            // Boolean은 JSON5Object에 직접 추가 가능
            return createPrimitiveWrapper(obj);
        } else if (obj instanceof Character) {
            // Character는 String으로 변환
            return createPrimitiveWrapper(obj.toString());
        } else if (obj instanceof byte[]) {
            // byte[]는 JSON5Object에 직접 추가 가능
            return createPrimitiveWrapper(obj);
        } else {
            // 기타 기본 타입들
            return createPrimitiveWrapper(obj);
        }
    }
    
    /**
     * 기본 타입 값을 감싸는 임시 JSON5Object를 생성합니다.
     * 
     * <p>이는 일관된 인터페이스를 위한 것이며, 실제로는
     * SerializationEngine에서 기본 타입들을 직접 처리합니다.</p>
     * 
     * @param value 감쌀 값
     * @return 값이 담긴 임시 JSON5Object
     */
    private JSON5Element createPrimitiveWrapper(Object value) {
        JSON5Object wrapper = new JSON5Object();
        wrapper.put("value", value);
        return wrapper;
    }
    
    /**
     * 전략의 우선순위를 반환합니다.
     * 
     * <p>기본 타입은 가장 높은 우선순위(1)를 가집니다.</p>
     * 
     * @return 우선순위 1 (가장 높음)
     */
    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
