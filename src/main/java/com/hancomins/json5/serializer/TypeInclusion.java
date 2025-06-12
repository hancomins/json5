package com.hancomins.json5.serializer;

/**
 * 타입 정보가 포함되는 방식을 정의합니다.
 */
public enum TypeInclusion {
    /**
     * 별도 속성으로 포함
     */
    PROPERTY,
    
    /**
     * 기존 속성 활용
     */
    EXISTING_PROPERTY
}
