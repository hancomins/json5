package com.hancomins.json5.serializer;

/**
 * 직렬화 과정에서 발생하는 특화된 예외 클래스입니다.
 * 
 * <p>JSON5 객체로의 직렬화 과정에서 발생하는 오류를 처리하기 위한 
 * 전용 예외 클래스입니다. 직렬화 관련 에러 코드와 컨텍스트 정보를 
 * 제공합니다.</p>
 * 
 * <h3>주요 사용 사례:</h3>
 * <ul>
 *   <li>순환 참조 감지 시</li>
 *   <li>직렬화 불가능한 타입 처리 시</li>
 *   <li>어노테이션 설정 오류 시</li>
 *   <li>필드 접근 권한 오류 시</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class SerializationException extends JSON5SerializerException {
    
    /** 직렬화 관련 에러 코드 상수들 */
    public static final String CIRCULAR_REFERENCE = "SERIALIZATION_CIRCULAR_REFERENCE";
    public static final String UNSUPPORTED_TYPE = "SERIALIZATION_UNSUPPORTED_TYPE";
    public static final String FIELD_ACCESS_ERROR = "SERIALIZATION_FIELD_ACCESS_ERROR";
    public static final String ANNOTATION_ERROR = "SERIALIZATION_ANNOTATION_ERROR";
    public static final String TYPE_CONVERSION_ERROR = "SERIALIZATION_TYPE_CONVERSION_ERROR";
    public static final String COLLECTION_ERROR = "SERIALIZATION_COLLECTION_ERROR";
    public static final String MAP_ERROR = "SERIALIZATION_MAP_ERROR";
    
    /**
     * 메시지만으로 직렬화 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     */
    public SerializationException(String message) {
        super("SERIALIZATION_ERROR", message);
    }
    
    /**
     * 에러 코드와 메시지로 직렬화 예외를 생성합니다.
     * 
     * @param errorCode 직렬화 에러 코드
     * @param message 오류 메시지
     */
    public SerializationException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * 메시지와 원인 예외로 직렬화 예외를 생성합니다.
     * 
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public SerializationException(String message, Throwable cause) {
        super("SERIALIZATION_ERROR", message, cause);
    }
    
    /**
     * 에러 코드, 메시지, 원인 예외로 직렬화 예외를 생성합니다.
     * 
     * @param errorCode 직렬화 에러 코드
     * @param message 오류 메시지
     * @param cause 원인 예외
     */
    public SerializationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * 순환 참조 오류를 위한 팩토리 메소드입니다.
     * 
     * @param objectType 순환 참조가 발생한 객체 타입
     * @param path 순환 참조 경로
     * @return 순환 참조 예외
     */
    public static SerializationException circularReference(Class<?> objectType, String path) {
        SerializationException exception = new SerializationException(CIRCULAR_REFERENCE, 
            "Circular reference detected during serialization");
        exception.addContext("objectType", objectType != null ? objectType.getName() : "unknown");
        exception.addContext("path", path);
        return exception;
    }
    
    /**
     * 지원되지 않는 타입 오류를 위한 팩토리 메소드입니다.
     * 
     * @param unsupportedType 지원되지 않는 타입
     * @param fieldName 필드명
     * @return 지원되지 않는 타입 예외
     */
    public static SerializationException unsupportedType(Class<?> unsupportedType, String fieldName) {
        SerializationException exception = new SerializationException(UNSUPPORTED_TYPE, 
            "Unsupported type for serialization");
        exception.addContext("type", unsupportedType != null ? unsupportedType.getName() : "unknown");
        exception.addContext("field", fieldName);
        return exception;
    }
    
    /**
     * 필드 접근 오류를 위한 팩토리 메소드입니다.
     * 
     * @param fieldName 접근 실패한 필드명
     * @param objectType 객체 타입
     * @param cause 원인 예외
     * @return 필드 접근 예외
     */
    public static SerializationException fieldAccessError(String fieldName, Class<?> objectType, Throwable cause) {
        SerializationException exception = new SerializationException(FIELD_ACCESS_ERROR, 
            "Failed to access field during serialization", cause);
        exception.addContext("field", fieldName);
        exception.addContext("objectType", objectType != null ? objectType.getName() : "unknown");
        return exception;
    }
    
    /**
     * 어노테이션 설정 오류를 위한 팩토리 메소드입니다.
     * 
     * @param annotationType 어노테이션 타입
     * @param fieldName 필드명
     * @param reason 오류 이유
     * @return 어노테이션 오류 예외
     */
    public static SerializationException annotationError(Class<?> annotationType, String fieldName, String reason) {
        SerializationException exception = new SerializationException(ANNOTATION_ERROR, 
            "Annotation configuration error during serialization");
        exception.addContext("annotation", annotationType != null ? annotationType.getSimpleName() : "unknown");
        exception.addContext("field", fieldName);
        exception.addContext("reason", reason);
        return exception;
    }
}
