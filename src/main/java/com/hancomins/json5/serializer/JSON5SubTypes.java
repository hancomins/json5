package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 복수의 @JSON5SubType을 담는 컨테이너
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5SubTypes {
    JSON5SubType[] value();
}
