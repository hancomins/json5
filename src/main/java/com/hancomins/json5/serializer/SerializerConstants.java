package com.hancomins.json5.serializer;

/**
 * JSON5 Serializer에서 사용되는 상수들을 정의하는 클래스입니다.
 * 
 * <p>이 클래스는 에러 메시지, 기본값, 설정 관련 상수들을 포함합니다.
 * 모든 필드는 static final로 선언되어 있으며, 인스턴스화를 방지하기 위해
 * private 생성자를 가집니다.</p>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public final class SerializerConstants {
    
    // === 에러 메시지 상수들 ===
    
    // 일반적인 에러 메시지
    public static final String ERROR_PRIMITIVE_TYPE = "valueType is primitive type. valueType=";
    public static final String ERROR_COLLECTION_TYPE = "valueType is java.util.Collection type. use collectionToJSON5Array method.";
    public static final String ERROR_MAP_TYPE = "valueType is java.util.Map type. use mapToJSON5Object method.";
    public static final String ERROR_NULL_VALUE = "Value cannot be null";
    public static final String ERROR_INVALID_TYPE = "Invalid type for conversion";
    public static final String ERROR_CONVERSION_FAILED = "Type conversion failed";
    
    // 직렬화 관련 에러 메시지
    public static final String ERROR_SERIALIZATION_CIRCULAR_REFERENCE = "Circular reference detected during serialization";
    public static final String ERROR_SERIALIZATION_UNSUPPORTED_TYPE = "Unsupported type for serialization";
    public static final String ERROR_SERIALIZATION_FIELD_ACCESS = "Failed to access field during serialization";
    public static final String ERROR_SERIALIZATION_ANNOTATION = "Annotation configuration error during serialization";
    
    // 역직렬화 관련 에러 메시지
    public static final String ERROR_DESERIALIZATION_TYPE_MISMATCH = "Type mismatch during deserialization";
    public static final String ERROR_DESERIALIZATION_MISSING_FIELD = "Required field is missing during deserialization";
    public static final String ERROR_DESERIALIZATION_JSON_STRUCTURE = "Invalid JSON structure during deserialization";
    public static final String ERROR_DESERIALIZATION_CONSTRUCTOR = "Failed to create instance during deserialization";
    public static final String ERROR_DESERIALIZATION_GENERIC_TYPE = "Failed to resolve generic type during deserialization";
    
    // 타입 변환 관련 에러 메시지
    public static final String ERROR_TYPE_CONVERSION_NUMBER_FORMAT = "Number format error during type conversion";
    public static final String ERROR_TYPE_CONVERSION_INVALID_CAST = "Invalid cast during type conversion";
    public static final String ERROR_TYPE_CONVERSION_OVERFLOW = "Numeric overflow during type conversion";
    
    // JSON5Element 추출 관련 에러 메시지
    public static final String ERROR_EXTRACTION_NULL_ELEMENT = "JSON5Element cannot be null";
    public static final String ERROR_EXTRACTION_INVALID_KEY_TYPE = "Invalid key type for JSON5Element";
    public static final String ERROR_EXTRACTION_KEY_NOT_FOUND = "Key not found in JSON5Element";
    public static final String ERROR_EXTRACTION_INDEX_OUT_OF_BOUNDS = "Array index out of bounds";
    
    // === 기본값 상수들 ===
    
    // Primitive 타입 기본값들
    public static final char DEFAULT_CHAR_VALUE = '\0';
    public static final byte DEFAULT_BYTE_VALUE = 0;
    public static final short DEFAULT_SHORT_VALUE = 0;
    public static final int DEFAULT_INT_VALUE = 0;
    public static final long DEFAULT_LONG_VALUE = 0L;
    public static final float DEFAULT_FLOAT_VALUE = 0.0f;
    public static final double DEFAULT_DOUBLE_VALUE = 0.0d;
    public static final boolean DEFAULT_BOOLEAN_VALUE = false;
    
    // 객체 타입 기본값들
    public static final String DEFAULT_STRING_VALUE = "";
    public static final String DEFAULT_NULL_STRING = "null";
    
    // === 설정 관련 상수들 ===
    
    // 캐시 관련 설정
    public static final int DEFAULT_CACHE_SIZE = 1000;
    public static final int DEFAULT_SCHEMA_CACHE_SIZE = 500;
    public static final int DEFAULT_TYPE_CACHE_SIZE = 200;
    public static final long DEFAULT_CACHE_EXPIRE_MINUTES = 30;
    
    // 처리 제한 관련 설정
    public static final int DEFAULT_MAX_DEPTH = 100;
    public static final int DEFAULT_MAX_ARRAY_SIZE = 10000;
    public static final int DEFAULT_MAX_OBJECT_SIZE = 10000;
    public static final int DEFAULT_MAX_STRING_LENGTH = 1000000;
    
    // 날짜/시간 형식 관련 설정
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String DEFAULT_DATE_FORMAT_SIMPLE = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    
    // 인코딩 관련 설정
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_LINE_SEPARATOR = System.lineSeparator();
    
    // 성능 관련 설정
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    
    // === 검증 관련 상수들 ===
    
    // 타입 검증 메시지
    public static final String VALIDATION_PRIMITIVE_TYPE_ERROR = "Primitive types are not supported in this context";
    public static final String VALIDATION_ARRAY_TYPE_ERROR = "Array types are not supported in this context";
    public static final String VALIDATION_INTERFACE_TYPE_ERROR = "Interface types require special handling";
    public static final String VALIDATION_ABSTRACT_TYPE_ERROR = "Abstract types require special handling";
    
    // 어노테이션 검증 메시지
    public static final String VALIDATION_ANNOTATION_MISSING = "Required annotation is missing";
    public static final String VALIDATION_ANNOTATION_CONFLICT = "Conflicting annotations detected";
    public static final String VALIDATION_ANNOTATION_INVALID_VALUE = "Invalid annotation value";
    
    // === 로깅 관련 상수들 ===
    
    // 로그 레벨별 메시지 접두사
    public static final String LOG_PREFIX_ERROR = "[ERROR] JSON5Serializer: ";
    public static final String LOG_PREFIX_WARN = "[WARN] JSON5Serializer: ";
    public static final String LOG_PREFIX_INFO = "[INFO] JSON5Serializer: ";
    public static final String LOG_PREFIX_DEBUG = "[DEBUG] JSON5Serializer: ";
    
    // 성능 로깅 메시지
    public static final String LOG_PERFORMANCE_SERIALIZATION = "Serialization completed";
    public static final String LOG_PERFORMANCE_DESERIALIZATION = "Deserialization completed";
    public static final String LOG_PERFORMANCE_CACHE_HIT = "Schema cache hit";
    public static final String LOG_PERFORMANCE_CACHE_MISS = "Schema cache miss";
    
    /**
     * 인스턴스화를 방지하는 private 생성자입니다.
     */
    private SerializerConstants() {
        throw new AssertionError("SerializerConstants cannot be instantiated");
    }
}
