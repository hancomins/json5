package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5ValueGetter {
    String value() default "";
    String key() default "";
    String comment() default "";
    String commentAfterKey() default "";

    boolean ignoreError() default false;

}
