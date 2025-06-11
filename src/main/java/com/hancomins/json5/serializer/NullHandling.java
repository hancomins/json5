package com.hancomins.json5.serializer;

/**
 * null 값 처리 방식을 정의하는 열거형
 */
public enum NullHandling {
    DEFAULT,        // null을 그대로 유지 (객체는 null로 초기화)
    EXCEPTION,      // 예외 발생
    EMPTY_OBJECT    // 기본값으로 초기화 (int=0, String="", boolean=false 등)
}
