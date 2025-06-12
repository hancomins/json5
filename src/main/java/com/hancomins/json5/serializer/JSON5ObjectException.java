package com.hancomins.json5.serializer;

/**
 * JSON5Object 처리 과정에서 발생하는 예외 클래스입니다.
 * 
 * <p>이 클래스는 기존 JSON5ObjectException의 기능을 유지하면서
 * 새로운 예외 계층 구조에 맞게 개선되었습니다. JSON5Object의 
 * 생성, 조작, 변환 과정에서 발생하는 오류를 처리합니다.</p>
 * 
 * <h3>주요 사용 사례:</h3>
 * <ul>
 *   <li>JSON5Object 생성 실패</li>
 *   <li>속성 접근 실패</li>
 *   <li>타입 변환 실패</li>
 *   <li>구조 검증 실패</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 1.0
 */
public class JSON5ObjectException extends RuntimeException {
    
    /** JSON5Object 관련 에러 코드 상수들 */
    public static final String PROPERTY_ACCESS_ERROR = "JSON5OBJECT_PROPERTY_ACCESS_ERROR";
    public static final String TYPE_CONVERSION_ERROR = "JSON5OBJECT_TYPE_CONVERSION_ERROR";
    public static final String STRUCTURE_VALIDATION_ERROR = "JSON5OBJECT_STRUCTURE_VALIDATION_ERROR";
    public static final String CREATION_ERROR = "JSON5OBJECT_CREATION_ERROR";
    
    /** 에러 코드 */
    private final String errorCode;
    
    /**
     * 메시지만으로 JSON5Object 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     */
    public JSON5ObjectException(String message) {
        super(message);
        this.errorCode = "JSON5OBJECT_ERROR";
    }
    
    /**
     * 에러 코드와 메시지로 JSON5Object 예외를 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 오류 메시지
     */
    public JSON5ObjectException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "JSON5OBJECT_ERROR";
    }

    /**
     * 메시지와 원인 예외로 JSON5Object 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public JSON5ObjectException(String message, Exception cause) {
        super(message, cause);
        this.errorCode = "JSON5OBJECT_ERROR";
    }
    
    /**
     * 에러 코드, 메시지, 원인 예외로 JSON5Object 예외를 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public JSON5ObjectException(String errorCode, String message, Exception cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "JSON5OBJECT_ERROR";
    }
    
    /**
     * 에러 코드를 반환합니다.
     * 
     * @return 에러 코드
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 속성 접근 오류를 위한 팩토리 메소드입니다.
     * 
     * @param propertyName 접근하려던 속성명
     * @param objectInfo JSON5Object 정보
     * @param cause 원인 예외
     * @return 속성 접근 오류 예외
     */
    public static JSON5ObjectException propertyAccessError(String propertyName, String objectInfo, Exception cause) {
        return new JSON5ObjectException(PROPERTY_ACCESS_ERROR, 
            "Failed to access property in JSON5Object: " + propertyName + 
            (objectInfo != null ? " (object: " + objectInfo + ")" : ""), cause);
    }
    
    /**
     * 타입 변환 오류를 위한 팩토리 메소드입니다.
     * 
     * @param fromType 원본 타입
     * @param toType 대상 타입
     * @param value 변환하려던 값
     * @return 타입 변환 오류 예외
     */
    public static JSON5ObjectException typeConversionError(String fromType, String toType, Object value) {
        return new JSON5ObjectException(TYPE_CONVERSION_ERROR, 
            "Type conversion failed from " + fromType + " to " + toType + 
            " (value: " + (value != null ? value.toString() : "null") + ")");
    }
    
    /**
     * 구조 검증 오류를 위한 팩토리 메소드입니다.
     * 
     * @param expectedStructure 예상 구조
     * @param actualStructure 실제 구조
     * @return 구조 검증 오류 예외
     */
    public static JSON5ObjectException structureValidationError(String expectedStructure, String actualStructure) {
        return new JSON5ObjectException(STRUCTURE_VALIDATION_ERROR, 
            "JSON5Object structure validation failed. Expected: " + expectedStructure + 
            ", Actual: " + actualStructure);
    }
    
    /**
     * JSON5Object 생성 오류를 위한 팩토리 메소드입니다.
     * 
     * @param source 생성 소스 정보
     * @param cause 원인 예외
     * @return 생성 오류 예외
     */
    public static JSON5ObjectException creationError(String source, Exception cause) {
        return new JSON5ObjectException(CREATION_ERROR, 
            "Failed to create JSON5Object from: " + source, cause);
    }
    
    @Override
    public String toString() {
        return getClass().getName() + " [" + errorCode + "]: " + getMessage();
    }
}
