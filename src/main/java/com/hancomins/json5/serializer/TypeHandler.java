package com.hancomins.json5.serializer;

/**
 * JSON5 직렬화/역직렬화에서 특정 타입을 처리하는 핸들러 인터페이스입니다.
 * 
 * <p>각 TypeHandler는 특정 타입이나 타입 그룹에 대한 직렬화/역직렬화 로직을 담당합니다.
 * Strategy 패턴을 통해 타입별로 최적화된 처리가 가능합니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * public class CustomDateHandler implements TypeHandler {
 *     @Override
 *     public boolean canHandle(Types type, Class<?> clazz) {
 *         return Date.class.isAssignableFrom(clazz);
 *     }
 *     
 *     @Override
 *     public Object handleSerialization(Object value, SerializationContext context) {
 *         return ((Date) value).getTime();
 *     }
 *     
 *     @Override
 *     public Object handleDeserialization(JSON5Element element, Class<?> targetType, 
 *                                       DeserializationContext context) {
 *         long timestamp = element.asLong();
 *         return new Date(timestamp);
 *     }
 * }
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 * @see TypeHandlerRegistry
 * @see TypeHandlerFactory
 */
public interface TypeHandler {
    
    /**
     * 이 핸들러가 지정된 타입을 처리할 수 있는지 확인합니다.
     * 
     * @param type Types enum 값
     * @param clazz 실제 클래스 타입
     * @return 처리 가능하면 true, 아니면 false
     */
    boolean canHandle(Types type, Class<?> clazz);
    
    /**
     * 직렬화 처리를 수행합니다.
     * 
     * @param value 직렬화할 값
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 결과 (JSON5Element로 변환 가능한 값)
     * @throws SerializationException 직렬화 중 오류가 발생한 경우
     */
    Object handleSerialization(Object value, SerializationContext context) throws SerializationException;
    
    /**
     * 역직렬화 처리를 수행합니다.
     * 
     * @param element JSON5Element 값
     * @param targetType 대상 타입
     * @param context 역직렬화 컨텍스트
     * @return 역직렬화된 객체
     * @throws DeserializationException 역직렬화 중 오류가 발생한 경우
     */
    Object handleDeserialization(Object element, Class<?> targetType, 
                                DeserializationContext context) throws DeserializationException;
    
    /**
     * 이 핸들러의 우선순위를 반환합니다.
     * 여러 핸들러가 동일한 타입을 처리할 수 있을 때 우선순위가 높은 핸들러가 선택됩니다.
     * 
     * @return 우선순위 (낮은 값일수록 높은 우선순위)
     */
    default TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.NORMAL;
    }
    
    /**
     * TypeHandler의 우선순위를 정의하는 enum입니다.
     */
    enum TypeHandlerPriority {
        HIGHEST(1), 
        HIGH(2), 
        NORMAL(3), 
        LOW(4), 
        LOWEST(5);
        
        private final int level;
        
        TypeHandlerPriority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
        
        public boolean isHigherThan(TypeHandlerPriority other) {
            return this.level < other.level;
        }
    }
}
