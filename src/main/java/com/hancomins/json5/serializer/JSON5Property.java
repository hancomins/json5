package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 생성자 파라미터와 JSON 키를 매핑합니다.
 * 중첩된 경로 접근을 지원합니다.
 * 
 * <p>사용 예제:</p>
 * <pre>
 * {@code
 * @JSON5Creator
 * public User(@JSON5Property("name") String name,
 *             @JSON5Property("age") int age,
 *             @JSON5Property("profile.email") String email,
 *             @JSON5Property(value = "profile.department", required = true) String department) {
 *     // 생성자 구현
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5Property {
    /**
     * JSON에서 매핑할 키 경로
     * 예: "name", "user.profile.email", "settings.theme.color"
     * 
     * @return JSON 키 경로
     */
    String value();
    
    /**
     * 값이 없을 때 동작 방식
     * DEFAULT_VALUE: null/0/false 등 기본값 설정
     * EXCEPTION: JSON5SerializerException 발생
     * 
     * @return 누락 값 처리 전략
     */
    MissingValueStrategy onMissing() default MissingValueStrategy.DEFAULT_VALUE;
    
    /**
     * 필수 파라미터 여부 (true면 값이 없을 때 항상 예외 발생)
     * 
     * @return 필수 여부
     */
    boolean required() default false;
}
