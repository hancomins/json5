package com.hancomins.json5.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 역직렬화에 사용할 생성자를 지정합니다.
 * 여러 생성자가 있을 때 priority가 높은 것을 우선 선택합니다.
 * 
 * <p>사용 예제:</p>
 * <pre>
 * {@code
 * @JSON5Type
 * public class User {
 *     private final String name;
 *     private final int age;
 *     
 *     // 기본 생성자
 *     public User() {
 *         this.name = "Unknown";
 *         this.age = 0;
 *     }
 *     
 *     // 생성자 기반 역직렬화
 *     @JSON5Creator
 *     public User(@JSON5Property("name") String name,
 *                 @JSON5Property("age") int age) {
 *         this.name = name;
 *         this.age = age;
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSON5Creator {
    /**
     * 생성자 선택 우선순위 (높을수록 우선, 기본값: 0)
     * 
     * @return 우선순위 값
     */
    int priority() default 0;
}
