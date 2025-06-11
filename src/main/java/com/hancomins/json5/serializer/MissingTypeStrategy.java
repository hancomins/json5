package com.hancomins.json5.serializer;

/**
 * 타입 정보가 없을 때의 동작 방식을 정의합니다.
 */
public enum MissingTypeStrategy {
    /**
     * 기본 구현체 사용
     */
    DEFAULT_IMPL,
    
    /**
     * 예외 발생
     */
    EXCEPTION
}
