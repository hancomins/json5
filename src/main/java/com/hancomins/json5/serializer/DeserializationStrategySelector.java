package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.constructor.ConstructorAnalyzer;

/**
 * 역직렬화 전략을 선택하는 클래스입니다.
 * 클래스의 특성에 따라 가장 적절한 역직렬화 방법을 결정합니다.
 */
public class DeserializationStrategySelector {
    
    private final ConstructorAnalyzer constructorAnalyzer;
    
    /**
     * DeserializationStrategySelector 생성자
     */
    public DeserializationStrategySelector() {
        this.constructorAnalyzer = new ConstructorAnalyzer();
    }
    
    /**
     * 대상 타입과 JSON 객체에 따라 적절한 역직렬화 전략을 선택합니다.
     * 
     * @param targetType 대상 클래스
     * @param json5Object JSON 객체
     * @return 선택된 역직렬화 전략
     */
    public DeserializationStrategy selectStrategy(Class<?> targetType, JSON5Object json5Object) {
        if (targetType == null) {
            throw new JSON5SerializerException("Target type cannot be null");
        }
        
        // 1. 다형성 타입 확인 (추후 구현)
        // if (TypeInfoAnalyzer.isPolymorphicType(targetType)) {
        //     return DeserializationStrategy.POLYMORPHIC;
        // }
        
        // 2. 생성자 기반 확인
        if (constructorAnalyzer.hasCreatorConstructor(targetType)) {
            return DeserializationStrategy.CONSTRUCTOR;
        }
        
        // 3. 기본 전략
        return DeserializationStrategy.OBJECT;
    }
    
    /**
     * 역직렬화 전략 열거형
     */
    public enum DeserializationStrategy {
        /**
         * 생성자 기반 역직렬화
         */
        CONSTRUCTOR,
        
        /**
         * 다형성 역직렬화 (추후 구현)
         */
        POLYMORPHIC,
        
        /**
         * 기본 객체 역직렬화
         */
        OBJECT
    }
}
