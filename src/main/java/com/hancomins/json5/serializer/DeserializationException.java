package com.hancomins.json5.serializer;

/**
 * 역직렬화 과정에서 발생하는 특화된 예외 클래스입니다.
 * 
 * <p>JSON5 객체에서 Java 객체로의 역직렬화 과정에서 발생하는 오류를 
 * 처리하기 위한 전용 예외 클래스입니다. 역직렬화 관련 에러 코드와 
 * 컨텍스트 정보를 제공합니다.</p>
 * 
 * <h3>주요 사용 사례:</h3>
 * <ul>
 *   <li>타입 불일치 오류 시</li>
 *   <li>필수 필드 누락 시</li>
 *   <li>JSON 구조 오류 시</li>
 *   <li>생성자 호출 실패 시</li>
 *   <li>제네릭 타입 해석 실패 시</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class DeserializationException extends JSON5SerializerException {
    
    /** 역직렬화 관련 에러 코드 상수들 */
    public static final String TYPE_MISMATCH = "DESERIALIZATION_TYPE_MISMATCH";
    public static final String MISSING_REQUIRED_FIELD = "DESERIALIZATION_MISSING_REQUIRED_FIELD";
    public static final String INVALID_JSON_STRUCTURE = "DESERIALIZATION_INVALID_JSON_STRUCTURE";
    public static final String CONSTRUCTOR_ERROR = "DESERIALIZATION_CONSTRUCTOR_ERROR";
    public static final String GENERIC_TYPE_ERROR = "DESERIALIZATION_GENERIC_TYPE_ERROR";
    public static final String VALUE_CONVERSION_ERROR = "DESERIALIZATION_VALUE_CONVERSION_ERROR";
    public static final String COLLECTION_ERROR = "DESERIALIZATION_COLLECTION_ERROR";
    public static final String MAP_ERROR = "DESERIALIZATION_MAP_ERROR";
    
    /**
     * 메시지만으로 역직렬화 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     */
    public DeserializationException(String message) {
        super("DESERIALIZATION_ERROR", message);
    }
    
    /**
     * 에러 코드와 메시지로 역직렬화 예외를 생성합니다.
     * 
     * @param errorCode 역직렬화 에러 코드
     * @param message 오류 메시지
     */
    public DeserializationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * 메시지와 원인 예외로 역직렬화 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public DeserializationException(String message, Throwable cause) {
        super("DESERIALIZATION_ERROR", message, cause);
    }
    
    /**
     * 에러 코드, 메시지, 원인 예외로 역직렬화 예외를 생성합니다.
     * 
     * @param errorCode 역직렬화 에러 코드
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public DeserializationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 타입 불일치 오류를 위한 팩토리 메소드입니다.
     * 
     * @param expectedType 예상 타입
     * @param actualType 실제 타입
     * @param fieldName 필드명
     * @return 타입 불일치 예외
     */
    public static DeserializationException typeMismatch(Class<?> expectedType, Class<?> actualType, String fieldName) {
        DeserializationException exception = new DeserializationException(TYPE_MISMATCH, 
            "Type mismatch during deserialization");
        exception.addContext("expectedType", expectedType != null ? expectedType.getName() : "unknown");
        exception.addContext("actualType", actualType != null ? actualType.getName() : "unknown");
        exception.addContext("field", fieldName);
        return exception;
    }
    
    /**
     * 필수 필드 누락 오류를 위한 팩토리 메소드입니다.
     * 
     * @param fieldName 누락된 필드명
     * @param targetType 대상 타입
     * @return 필수 필드 누락 예외
     */
    public static DeserializationException missingRequiredField(String fieldName, Class<?> targetType) {
        DeserializationException exception = new DeserializationException(MISSING_REQUIRED_FIELD, 
            "Required field is missing during deserialization");
        exception.addContext("field", fieldName);
        exception.addContext("targetType", targetType != null ? targetType.getName() : "unknown");
        return exception;
    }
    
    /**
     * JSON 구조 오류를 위한 팩토리 메소드입니다.
     * 
     * @param expectedStructure 예상 구조
     * @param actualStructure 실제 구조
     * @param path JSON 경로
     * @return JSON 구조 오류 예외
     */
    public static DeserializationException invalidJsonStructure(String expectedStructure, String actualStructure, String path) {
        DeserializationException exception = new DeserializationException(INVALID_JSON_STRUCTURE, 
            "Invalid JSON structure during deserialization");
        exception.addContext("expectedStructure", expectedStructure);
        exception.addContext("actualStructure", actualStructure);
        exception.addContext("path", path);
        return exception;
    }
    
    /**
     * 생성자 호출 실패 오류를 위한 팩토리 메소드입니다.
     * 
     * @param targetType 생성하려던 타입
     * @param cause 원인 예외
     * @return 생성자 오류 예외
     */
    public static DeserializationException constructorError(Class<?> targetType, Throwable cause) {
        DeserializationException exception = new DeserializationException(CONSTRUCTOR_ERROR, 
            "Failed to create instance during deserialization", cause);
        exception.addContext("targetType", targetType != null ? targetType.getName() : "unknown");
        return exception;
    }
    
    /**
     * 제네릭 타입 해석 실패 오류를 위한 팩토리 메소드입니다.
     * 
     * @param genericType 제네릭 타입 정보
     * @param fieldName 필드명
     * @param cause 원인 예외
     * @return 제네릭 타입 오류 예외
     */
    public static DeserializationException genericTypeError(String genericType, String fieldName, Throwable cause) {
        DeserializationException exception = new DeserializationException(GENERIC_TYPE_ERROR, 
            "Failed to resolve generic type during deserialization", cause);
        exception.addContext("genericType", genericType);
        exception.addContext("field", fieldName);
        return exception;
    }
    
    /**
     * 값 변환 오류를 위한 팩토리 메소드입니다.
     * 
     * @param value 변환하려던 값
     * @param targetType 대상 타입
     * @param fieldName 필드명
     * @param cause 원인 예외
     * @return 값 변환 오류 예외
     */
    public static DeserializationException valueConversionError(Object value, Class<?> targetType, String fieldName, Throwable cause) {
        DeserializationException exception = new DeserializationException(VALUE_CONVERSION_ERROR, 
            "Failed to convert value during deserialization", cause);
        exception.addContext("value", value != null ? value.toString() : "null");
        exception.addContext("valueType", value != null ? value.getClass().getName() : "null");
        exception.addContext("targetType", targetType != null ? targetType.getName() : "unknown");
        exception.addContext("field", fieldName);
        return exception;
    }
}
