package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 서브타입을 정의합니다. 여러 개 사용 가능합니다.
 * Jackson의 @JsonSubTypes.Type과 유사한 기능을 제공합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(JSON5SubTypes.class)
public @interface JSON5SubType {
    /**
     * 구현 클래스
     */
    Class<?> value();
    
    /**
     * JSON에서 이 타입을 나타내는 값
     */
    String name();
}
