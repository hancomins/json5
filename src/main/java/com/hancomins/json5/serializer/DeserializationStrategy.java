package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;

/**
 * 타입별 역직렬화 전략을 정의하는 인터페이스
 * 
 * Strategy 패턴을 통해 각 타입에 특화된 역직렬화 로직을 구현합니다.
 * 이를 통해 새로운 타입 추가 시 기존 코드 수정 없이 확장 가능한 구조를 제공합니다.
 */
public interface DeserializationStrategy {
    
    /**
     * 이 전략이 주어진 타입과 대상 클래스를 처리할 수 있는지 확인합니다.
     * 
     * @param type Types enum 값
     * @param targetType 대상 클래스 타입
     * @return 처리 가능하면 true, 불가능하면 false
     */
    boolean canHandle(Types type, Class<?> targetType);
    
    /**
     * JSON5Element를 지정된 타입으로 역직렬화합니다.
     * 
     * @param json5Element 역직렬화할 JSON5Element
     * @param targetType 대상 클래스 타입
     * @param context 역직렬화 컨텍스트
     * @return 역직렬화된 객체
     */
    Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context);
    
    /**
     * 전략의 우선순위를 반환합니다.
     * 낮은 값일수록 높은 우선순위를 가집니다.
     * 
     * @return 우선순위 값 (1이 가장 높음)
     */
    default int getPriority() {
        return 100; // 기본 우선순위
    }
}
