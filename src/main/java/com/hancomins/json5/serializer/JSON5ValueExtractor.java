package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 객체를 프리미티브 값으로 추출하는 메서드를 지정합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5ValueExtractor {
    /**
     * null 값을 반환할 때의 동작
     */
    NullHandling onNull() default NullHandling.DEFAULT;
}
