package com.hancomins.json5.serializer;

/**
 * Schema 값 처리를 담당하는 프로세서 인터페이스입니다.
 * 
 * <p>이 인터페이스는 SchemaValueAbs의 복잡한 getValue/setValue 로직을
 * 타입별로 분리하여 단일 책임 원칙을 준수하도록 합니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public interface ValueProcessor {
    
    /**
     * 이 프로세서가 주어진 타입을 처리할 수 있는지 확인합니다.
     * 
     * @param type 확인할 타입
     * @return 처리 가능하면 true, 아니면 false
     */
    boolean canHandle(Types type);
    
    /**
     * 부모 객체에서 스키마에 따라 값을 가져옵니다.
     * 
     * @param parent 부모 객체
     * @param schema 스키마 정보
     * @return 추출된 값
     */
    Object getValue(Object parent, SchemaValueAbs schema);
    
    /**
     * 부모 객체에 스키마에 따라 값을 설정합니다.
     * 
     * @param parent 부모 객체
     * @param value 설정할 값
     * @param schema 스키마 정보
     */
    void setValue(Object parent, Object value, SchemaValueAbs schema);
}
