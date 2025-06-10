package com.hancomins.json5.serializer;

import java.util.function.Predicate;

/**
 * 조건에 따라 다른 TypeHandler를 선택하는 조건부 TypeHandler입니다.
 * 
 * <p>이 핸들러는 런타임 조건에 따라 서로 다른 처리 전략을 적용할 수 있습니다:</p>
 * <ul>
 *   <li>객체의 특정 속성값에 따른 다른 직렬화</li>
 *   <li>컨텍스트 정보에 따른 다른 처리</li>
 *   <li>환경 설정에 따른 동적 전략 선택</li>
 * </ul>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * ConditionalTypeHandler handler = ConditionalTypeHandler.builder(String.class)
 *     .when(str -> str.startsWith("encrypted:"), new EncryptedStringHandler())
 *     .when(str -> str.startsWith("base64:"), new Base64StringHandler())
 *     .otherwise(new DefaultStringHandler())
 *     .build();
 * }</pre>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class ConditionalTypeHandler implements TypeHandler {
    
    private final Class<?> targetType;
    private final ConditionEntry[] conditions;
    private final TypeHandler defaultHandler;
    
    private ConditionalTypeHandler(Class<?> targetType, ConditionEntry[] conditions, TypeHandler defaultHandler) {
        this.targetType = targetType;
        this.conditions = conditions;
        this.defaultHandler = defaultHandler;
    }
    
    @Override
    public boolean canHandle(Types type, Class<?> clazz) {
        return clazz != null && targetType.isAssignableFrom(clazz);
    }
    
    @Override
    public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
        if (value == null) {
            return null;
        }
        
        // 조건에 맞는 핸들러 찾기
        for (ConditionEntry condition : conditions) {
            if (condition.predicate.test(value)) {
                return condition.handler.handleSerialization(value, context);
            }
        }
        
        // 기본 핸들러 사용
        if (defaultHandler != null) {
            return defaultHandler.handleSerialization(value, context);
        }
        
        throw new SerializationException("No suitable handler found for value: " + value);
    }
    
    @Override
    public Object handleDeserialization(Object element, Class<?> targetType, 
                                      DeserializationContext context) throws DeserializationException {
        // 역직렬화는 기본 핸들러만 사용 (조건 판별이 어려움)
        if (defaultHandler != null) {
            return defaultHandler.handleDeserialization(element, targetType, context);
        }
        
        throw new DeserializationException("No default handler available for deserialization");
    }
    
    @Override
    public TypeHandlerPriority getPriority() {
        return TypeHandlerPriority.HIGH; // 조건부 핸들러는 높은 우선순위
    }
    
    /**
     * 조건과 핸들러의 쌍을 나타내는 내부 클래스입니다.
     */
    private static class ConditionEntry {
        final Predicate<Object> predicate;
        final TypeHandler handler;
        
        ConditionEntry(Predicate<Object> predicate, TypeHandler handler) {
            this.predicate = predicate;
            this.handler = handler;
        }
    }
    
    /**
     * ConditionalTypeHandler를 빌드하기 위한 Builder 클래스입니다.
     */
    public static class Builder {
        private final Class<?> targetType;
        private final java.util.List<ConditionEntry> conditions = new java.util.ArrayList<>();
        private TypeHandler defaultHandler;
        
        public Builder(Class<?> targetType) {
            this.targetType = targetType;
        }
        
        /**
         * 조건과 해당 조건에 사용할 핸들러를 추가합니다.
         * 
         * @param condition 조건 술어
         * @param handler 조건이 참일 때 사용할 핸들러
         * @return 이 Builder 인스턴스
         */
        public Builder when(Predicate<Object> condition, TypeHandler handler) {
            conditions.add(new ConditionEntry(condition, handler));
            return this;
        }
        
        /**
         * 모든 조건이 거짓일 때 사용할 기본 핸들러를 설정합니다.
         * 
         * @param handler 기본 핸들러
         * @return 이 Builder 인스턴스
         */
        public Builder otherwise(TypeHandler handler) {
            this.defaultHandler = handler;
            return this;
        }
        
        /**
         * ConditionalTypeHandler를 빌드합니다.
         * 
         * @return 구성된 ConditionalTypeHandler
         */
        public ConditionalTypeHandler build() {
            if (conditions.isEmpty()) {
                throw new IllegalStateException("At least one condition must be specified");
            }
            
            ConditionEntry[] conditionArray = conditions.toArray(new ConditionEntry[0]);
            return new ConditionalTypeHandler(targetType, conditionArray, defaultHandler);
        }
    }
    
    /**
     * 지정된 타입에 대한 조건부 핸들러 빌더를 생성합니다.
     * 
     * @param targetType 대상 타입
     * @return Builder 인스턴스
     */
    public static Builder builder(Class<?> targetType) {
        return new Builder(targetType);
    }
}
