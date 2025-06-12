package com.hancomins.json5.serializer;

/**
 * 필수 값이 누락된 경우의 처리 전략을 정의합니다.
 */
public enum MissingValueStrategy {
    /**
     * 기본값을 사용합니다 (null, 0, false 등).
     */
    DEFAULT_VALUE,
    
    /**
     * JSON5SerializerException을 발생시킵니다.
     */
    EXCEPTION
}
