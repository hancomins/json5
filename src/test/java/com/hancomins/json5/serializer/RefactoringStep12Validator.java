package com.hancomins.json5.serializer;

import java.util.Map;

/**
 * 1.2 단계 예외 처리 개선 검증용 테스트 클래스입니다.
 * 
 * <p>이 클래스는 1.1 단계와 1.2 단계 리팩토링이 올바르게 작동하는지 
 * 확인하기 위한 종합적인 테스트를 제공합니다.</p>
 * 
 * <h3>검증 항목:</h3>
 * <ul>
 *   <li>1.1 단계: Utils 클래스 분해 결과</li>
 *   <li>1.2 단계: 예외 처리 계층 개선</li>
 *   <li>SerializerConstants 확장 기능</li>
 *   <li>새로운 예외 클래스들의 기능</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class RefactoringStep12Validator {
    
    /**
     * 1.1 ~ 1.2 단계 리팩토링 검증을 수행합니다.
     * 
     * @return 모든 테스트가 성공하면 true, 실패하면 false
     */
    public static boolean validateStep12Refactoring() {
        try {
            System.out.println("=== 1.1 단계 검증 시작 ===");
            
            // 1.1 단계 검증
            if (!RefactoringStep1Validator.validateStep1Refactoring()) {
                System.err.println("1.1 단계 검증 실패");
                return false;
            }
            
            System.out.println("=== 1.2 단계 검증 시작 ===");
            
            // 1.2 단계 검증
            if (!testExceptionHierarchy()) {
                System.err.println("예외 계층 구조 테스트 실패");
                return false;
            }
            
            if (!testSerializerConstantsExtended()) {
                System.err.println("확장된 SerializerConstants 테스트 실패");
                return false;
            }
            
            if (!testSerializationException()) {
                System.err.println("SerializationException 테스트 실패");
                return false;
            }
            
            if (!testDeserializationException()) {
                System.err.println("DeserializationException 테스트 실패");
                return false;
            }
            
            if (!testJSON5ObjectExceptionImprovement()) {
                System.err.println("개선된 JSON5ObjectException 테스트 실패");
                return false;
            }
            
            System.out.println("=== 1.1 ~ 1.2 단계 리팩토링 검증 완료: 모든 테스트 통과 ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("1.1 ~ 1.2 단계 리팩토링 검증 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 예외 계층 구조를 테스트합니다.
     */
    private static boolean testExceptionHierarchy() {
        // JSON5SerializerException 기본 기능 테스트
        JSON5SerializerException baseException = new JSON5SerializerException("TEST_ERROR", "Test message");
        
        if (!"TEST_ERROR".equals(baseException.getErrorCode())) {
            System.err.println("JSON5SerializerException 에러 코드 설정 실패");
            return false;
        }
        
        // 컨텍스트 추가 테스트
        baseException.addContext("testKey", "testValue")
                     .addContext("numericKey", 123);
        
        if (!"testValue".equals(baseException.getContext("testKey"))) {
            System.err.println("컨텍스트 추가/조회 실패");
            return false;
        }
        
        if (!baseException.hasContext("testKey")) {
            System.err.println("컨텍스트 존재 확인 실패");
            return false;
        }
        
        Map<String, Object> allContext = baseException.getAllContext();
        if (allContext.size() != 2) {
            System.err.println("전체 컨텍스트 조회 실패: expected=2, actual=" + allContext.size());
            return false;
        }
        
        // 상세 메시지 테스트
        String detailedMessage = baseException.getDetailedMessage();
        if (!detailedMessage.contains("TEST_ERROR") || !detailedMessage.contains("testKey=testValue")) {
            System.err.println("상세 메시지 생성 실패: " + detailedMessage);
            return false;
        }
        
        System.out.println("예외 계층 구조 테스트 통과");
        return true;
    }
    
    /**
     * 확장된 SerializerConstants 클래스를 테스트합니다.
     */
    private static boolean testSerializerConstantsExtended() {
        // 새로 추가된 에러 메시지 상수 확인
        if (!SerializerConstants.ERROR_SERIALIZATION_CIRCULAR_REFERENCE.contains("Circular reference")) {
            System.err.println("직렬화 에러 메시지 상수 확인 실패");
            return false;
        }
        
        if (!SerializerConstants.ERROR_DESERIALIZATION_TYPE_MISMATCH.contains("Type mismatch")) {
            System.err.println("역직렬화 에러 메시지 상수 확인 실패");
            return false;
        }
        
        // 새로 추가된 설정 상수 확인
        if (SerializerConstants.DEFAULT_SCHEMA_CACHE_SIZE != 500) {
            System.err.println("스키마 캐시 크기 상수 확인 실패");
            return false;
        }
        
        if (SerializerConstants.DEFAULT_MAX_ARRAY_SIZE != 10000) {
            System.err.println("최대 배열 크기 상수 확인 실패");
            return false;
        }
        
        // 로깅 관련 상수 확인
        if (!SerializerConstants.LOG_PREFIX_ERROR.contains("[ERROR]")) {
            System.err.println("로그 접두사 상수 확인 실패");
            return false;
        }
        
        // 검증 관련 상수 확인
        if (!SerializerConstants.VALIDATION_PRIMITIVE_TYPE_ERROR.contains("Primitive types")) {
            System.err.println("검증 메시지 상수 확인 실패");
            return false;
        }
        
        System.out.println("확장된 SerializerConstants 테스트 통과");
        return true;
    }
    
    /**
     * SerializationException 클래스를 테스트합니다.
     */
    private static boolean testSerializationException() {
        // 기본 생성자 테스트
        SerializationException basicException = new SerializationException("Basic serialization error");
        if (!"SERIALIZATION_ERROR".equals(basicException.getErrorCode())) {
            System.err.println("SerializationException 기본 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 순환 참조
        SerializationException circularRefException = SerializationException.circularReference(String.class, "/root/child/parent");
        if (!SerializationException.CIRCULAR_REFERENCE.equals(circularRefException.getErrorCode())) {
            System.err.println("순환 참조 예외 에러 코드 실패");
            return false;
        }
        
        if (!"String".equals(circularRefException.getContext("objectType"))) {
            System.err.println("순환 참조 예외 컨텍스트 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 지원되지 않는 타입
        SerializationException unsupportedTypeException = SerializationException.unsupportedType(Thread.class, "threadField");
        if (!SerializationException.UNSUPPORTED_TYPE.equals(unsupportedTypeException.getErrorCode())) {
            System.err.println("지원되지 않는 타입 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 필드 접근 오류
        RuntimeException cause = new RuntimeException("Access denied");
        SerializationException fieldAccessException = SerializationException.fieldAccessError("privateField", String.class, cause);
        if (!SerializationException.FIELD_ACCESS_ERROR.equals(fieldAccessException.getErrorCode())) {
            System.err.println("필드 접근 예외 에러 코드 실패");
            return false;
        }
        
        if (fieldAccessException.getCause() != cause) {
            System.err.println("필드 접근 예외 원인 예외 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 어노테이션 오류
        SerializationException annotationException = SerializationException.annotationError(JSON5Value.class, "annotatedField", "Invalid configuration");
        if (!SerializationException.ANNOTATION_ERROR.equals(annotationException.getErrorCode())) {
            System.err.println("어노테이션 예외 에러 코드 실패");
            return false;
        }
        
        System.out.println("SerializationException 테스트 통과");
        return true;
    }
    
    /**
     * DeserializationException 클래스를 테스트합니다.
     */
    private static boolean testDeserializationException() {
        // 기본 생성자 테스트
        DeserializationException basicException = new DeserializationException("Basic deserialization error");
        if (!"DESERIALIZATION_ERROR".equals(basicException.getErrorCode())) {
            System.err.println("DeserializationException 기본 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 타입 불일치
        DeserializationException typeMismatchException = DeserializationException.typeMismatch(Integer.class, String.class, "numberField");
        if (!DeserializationException.TYPE_MISMATCH.equals(typeMismatchException.getErrorCode())) {
            System.err.println("타입 불일치 예외 에러 코드 실패");
            return false;
        }
        
        if (!"Integer".equals(typeMismatchException.getContext("expectedType"))) {
            System.err.println("타입 불일치 예외 예상 타입 컨텍스트 실패");
            return false;
        }
        
        if (!"String".equals(typeMismatchException.getContext("actualType"))) {
            System.err.println("타입 불일치 예외 실제 타입 컨텍스트 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 필수 필드 누락
        DeserializationException missingFieldException = DeserializationException.missingRequiredField("requiredField", String.class);
        if (!DeserializationException.MISSING_REQUIRED_FIELD.equals(missingFieldException.getErrorCode())) {
            System.err.println("필수 필드 누락 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - JSON 구조 오류
        DeserializationException jsonStructureException = DeserializationException.invalidJsonStructure("object", "array", "/root/data");
        if (!DeserializationException.INVALID_JSON_STRUCTURE.equals(jsonStructureException.getErrorCode())) {
            System.err.println("JSON 구조 오류 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 생성자 오류
        NoSuchMethodException cause = new NoSuchMethodException("No default constructor");
        DeserializationException constructorException = DeserializationException.constructorError(String.class, cause);
        if (!DeserializationException.CONSTRUCTOR_ERROR.equals(constructorException.getErrorCode())) {
            System.err.println("생성자 오류 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 제네릭 타입 오류
        ClassCastException genericCause = new ClassCastException("Generic type resolution failed");
        DeserializationException genericException = DeserializationException.genericTypeError("List<String>", "listField", genericCause);
        if (!DeserializationException.GENERIC_TYPE_ERROR.equals(genericException.getErrorCode())) {
            System.err.println("제네릭 타입 오류 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 값 변환 오류
        NumberFormatException conversionCause = new NumberFormatException("Invalid number format");
        DeserializationException valueConversionException = DeserializationException.valueConversionError("invalid_number", Integer.class, "numberField", conversionCause);
        if (!DeserializationException.VALUE_CONVERSION_ERROR.equals(valueConversionException.getErrorCode())) {
            System.err.println("값 변환 오류 예외 에러 코드 실패");
            return false;
        }
        
        System.out.println("DeserializationException 테스트 통과");
        return true;
    }
    
    /**
     * 개선된 JSON5ObjectException 클래스를 테스트합니다.
     */
    private static boolean testJSON5ObjectExceptionImprovement() {
        // 기본 생성자 테스트
        JSON5ObjectException basicException = new JSON5ObjectException("Basic JSON5Object error");
        if (!"JSON5OBJECT_ERROR".equals(basicException.getErrorCode())) {
            System.err.println("JSON5ObjectException 기본 에러 코드 실패");
            return false;
        }
        
        // 에러 코드 포함 생성자 테스트
        JSON5ObjectException codedException = new JSON5ObjectException("CUSTOM_ERROR", "Custom error message");
        if (!"CUSTOM_ERROR".equals(codedException.getErrorCode())) {
            System.err.println("JSON5ObjectException 커스텀 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 속성 접근 오류
        Exception cause = new IllegalAccessException("Property access denied");
        JSON5ObjectException propertyException = JSON5ObjectException.propertyAccessError("testProperty", "TestObject", cause);
        if (!JSON5ObjectException.PROPERTY_ACCESS_ERROR.equals(propertyException.getErrorCode())) {
            System.err.println("속성 접근 오류 예외 에러 코드 실패");
            return false;
        }
        
        if (!propertyException.getMessage().contains("testProperty")) {
            System.err.println("속성 접근 오류 예외 메시지에 속성명 누락");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 타입 변환 오류
        JSON5ObjectException typeConversionException = JSON5ObjectException.typeConversionError("String", "Integer", "123abc");
        if (!JSON5ObjectException.TYPE_CONVERSION_ERROR.equals(typeConversionException.getErrorCode())) {
            System.err.println("타입 변환 오류 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 구조 검증 오류
        JSON5ObjectException structureException = JSON5ObjectException.structureValidationError("{ key: value }", "[ array ]");
        if (!JSON5ObjectException.STRUCTURE_VALIDATION_ERROR.equals(structureException.getErrorCode())) {
            System.err.println("구조 검증 오류 예외 에러 코드 실패");
            return false;
        }
        
        // 팩토리 메소드 테스트 - 생성 오류
        Exception creationCause = new RuntimeException("Creation failed");
        JSON5ObjectException creationException = JSON5ObjectException.creationError("invalid JSON string", creationCause);
        if (!JSON5ObjectException.CREATION_ERROR.equals(creationException.getErrorCode())) {
            System.err.println("생성 오류 예외 에러 코드 실패");
            return false;
        }
        
        // toString 메소드 테스트
        String toStringResult = creationException.toString();
        if (!toStringResult.contains("CREATION_ERROR") || !toStringResult.contains("JSON5ObjectException")) {
            System.err.println("toString 메소드 결과 부적절: " + toStringResult);
            return false;
        }
        
        System.out.println("개선된 JSON5ObjectException 테스트 통과");
        return true;
    }
    
    /**
     * 메인 메소드 - 테스트 실행용
     */
    public static void main(String[] args) {
        System.out.println("=== JSON5 Serializer 1.1 ~ 1.2 단계 리팩토링 검증 시작 ===");
        
        boolean success = validateStep12Refactoring();
        
        if (success) {
            System.out.println("=== 검증 성공: 1.1 ~ 1.2 단계 리팩토링이 정상적으로 완료되었습니다 ===");
            System.exit(0);
        } else {
            System.err.println("=== 검증 실패: 1.1 ~ 1.2 단계 리팩토링에 문제가 있습니다 ===");
            System.exit(1);
        }
    }
}
