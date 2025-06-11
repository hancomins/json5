package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 프리미티브 값에서 객체를 생성하는 생성자를 지정합니다.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5ValueConstructor {
    /**
     * null 값 입력 시 동작
     */
    NullHandling onNull() default NullHandling.DEFAULT;
}
