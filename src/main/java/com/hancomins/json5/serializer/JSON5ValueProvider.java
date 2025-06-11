package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 클래스를 프리미티브 타입으로 직렬화하고, 프리미티브 타입에서 커스텀 클래스로 역직렬화하는 
 * 양방향 변환 기능을 제공합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5ValueProvider {
    /**
     * 변환 대상 프리미티브 타입 (자동 감지 가능)
     */
    Class<?> targetType() default Void.class;
    
    /**
     * null 값 처리 방식
     */
    NullHandling nullHandling() default NullHandling.DEFAULT;
    
    /**
     * 타입 변환 엄격 모드
     * true: 타입이 정확히 일치해야 함
     * false: DataConverter를 통한 타입 변환 허용
     */
    boolean strictTypeMatching() default true;
}
