package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;

/**
 * 직렬화 전략을 정의하는 인터페이스입니다.
 * 
 * <p>이 인터페이스는 Strategy 패턴을 적용하여 다양한 타입의 객체를
 * 직렬화하는 전략을 정의합니다. 각 구현체는 특정 타입이나 조건에
 * 특화된 직렬화 로직을 제공합니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * SerializationStrategy strategy = new PrimitiveSerializationStrategy();
 * if (strategy.canHandle(obj, type)) {
 *     JSON5Element result = strategy.serialize(obj, context);
 * }
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 * @see PrimitiveSerializationStrategy
 * @see ComplexObjectSerializationStrategy
 */
public interface SerializationStrategy {
    
    /**
     * 이 전략이 주어진 객체와 타입을 처리할 수 있는지 확인합니다.
     * 
     * @param obj 처리할 객체
     * @param type 객체의 타입 정보
     * @return 처리 가능하면 true, 그렇지 않으면 false
     */
    boolean canHandle(Object obj, Types type);
    
    /**
     * 주어진 객체를 JSON5Element로 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Element
     * @throws SerializationException 직렬화 중 오류가 발생한 경우
     */
    JSON5Element serialize(Object obj, SerializationContext context);
    
    /**
     * 전략의 우선순위를 반환합니다.
     * 
     * <p>낮은 숫자일수록 높은 우선순위를 가집니다.
     * 여러 전략이 같은 객체를 처리할 수 있을 때
     * 우선순위가 높은 전략이 먼저 선택됩니다.</p>
     * 
     * @return 우선순위 (1이 가장 높음)
     */
    int getPriority();
}
