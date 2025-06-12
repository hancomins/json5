package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 추상 클래스나 인터페이스의 다형성 역직렬화 정보를 정의합니다.
 * Jackson의 @JsonTypeInfo와 유사한 기능을 제공합니다.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5TypeInfo {
    /**
     * 타입 결정에 사용할 JSON 키 경로
     * 중첩 경로 지원: "type", "meta.type", "vehicle.info.type"
     */
    String property();
    
    /**
     * 타입 정보가 포함되는 방식
     * PROPERTY: 별도 속성으로 포함
     * EXISTING_PROPERTY: 기존 속성을 타입 정보로도 활용
     */
    TypeInclusion include() default TypeInclusion.PROPERTY;
    
    /**
     * 기본 구현체 (매칭되는 타입이 없을 때 사용)
     */
    Class<?> defaultImpl() default Void.class;
    
    /**
     * 타입 정보가 없을 때 동작
     * DEFAULT_IMPL: 기본 구현체 사용
     * EXCEPTION: 예외 발생
     */
    MissingTypeStrategy onMissingType() default MissingTypeStrategy.DEFAULT_IMPL;
}
