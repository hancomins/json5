package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;

/**
 * 1.1 단계 Utils 클래스 분해 검증용 테스트 클래스입니다.
 * 
 * <p>이 클래스는 리팩토링이 올바르게 작동하는지 확인하기 위한 
 * 간단한 테스트를 제공합니다. 주요 검증 항목:</p>
 * <ul>
 *   <li>TypeConverter 클래스의 기본 변환 기능</li>
 *   <li>PrimitiveTypeConverter 클래스의 타입 변환 기능</li>
 *   <li>JSON5ElementExtractor 클래스의 값 추출 기능</li>
 *   <li>기존 Utils 클래스의 하위 호환성</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class RefactoringStep1Validator {
    
    /**
     * 1.1 단계 리팩토링 검증을 수행합니다.
     * 
     * @return 모든 테스트가 성공하면 true, 실패하면 false
     */
    public static boolean validateStep1Refactoring() {
        try {
            // TypeConverter 테스트
            if (!testTypeConverter()) {
                System.err.println("TypeConverter 테스트 실패");
                return false;
            }
            
            // PrimitiveTypeConverter 테스트
            if (!testPrimitiveTypeConverter()) {
                System.err.println("PrimitiveTypeConverter 테스트 실패");
                return false;
            }
            
            // JSON5ElementExtractor 테스트
            if (!testJSON5ElementExtractor()) {
                System.err.println("JSON5ElementExtractor 테스트 실패");
                return false;
            }
            
            // SerializerConstants 테스트
            if (!testSerializerConstants()) {
                System.err.println("SerializerConstants 테스트 실패");
                return false;
            }
            
            // 하위 호환성 테스트 (Utils 클래스)
            if (!testBackwardCompatibility()) {
                System.err.println("하위 호환성 테스트 실패");
                return false;
            }
            
            System.out.println("1.1 단계 리팩토링 검증 완료: 모든 테스트 통과");
            return true;
            
        } catch (Exception e) {
            System.err.println("1.1 단계 리팩토링 검증 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * TypeConverter 클래스의 기본 기능을 테스트합니다.
     */
    private static boolean testTypeConverter() {
        // String -> Integer 변환 테스트
        Object result = TypeConverter.convertValueFromString("123", Types.Integer);
        if (!Integer.valueOf(123).equals(result)) {
            System.err.println("String to Integer 변환 실패: expected=123, actual=" + result);
            return false;
        }
        
        // Number -> Double 변환 테스트
        result = TypeConverter.convertValueFromNumber(123, Types.Double);
        if (!Double.valueOf(123.0).equals(result)) {
            System.err.println("Number to Double 변환 실패: expected=123.0, actual=" + result);
            return false;
        }
        
        // null 값 처리 테스트
        result = TypeConverter.convertValue(null, Types.String);
        if (result != null) {
            System.err.println("null 값 처리 실패: expected=null, actual=" + result);
            return false;
        }
        
        // 안전한 변환 테스트
        result = TypeConverter.convertValueSafely("invalid", Types.Integer, -1);
        if (!Integer.valueOf(-1).equals(result)) {
            System.err.println("안전한 변환 실패: expected=-1, actual=" + result);
            return false;
        }
        
        System.out.println("TypeConverter 테스트 통과");
        return true;
    }
    
    /**
     * PrimitiveTypeConverter 클래스의 기능을 테스트합니다.
     */
    private static boolean testPrimitiveTypeConverter() {
        // primitive -> boxed 변환 테스트
        Class<?> result = PrimitiveTypeConverter.primitiveTypeToBoxedType(int.class);
        if (!Integer.class.equals(result)) {
            System.err.println("int.class -> Integer.class 변환 실패: expected=Integer.class, actual=" + result);
            return false;
        }
        
        // 이미 boxed 타입인 경우 테스트
        result = PrimitiveTypeConverter.primitiveTypeToBoxedType(String.class);
        if (!String.class.equals(result)) {
            System.err.println("String.class 변환 실패: expected=String.class, actual=" + result);
            return false;
        }
        
        // primitive 타입 확인 테스트
        boolean isPrimitive = PrimitiveTypeConverter.isPrimitiveType(int.class);
        if (!isPrimitive) {
            System.err.println("primitive 타입 확인 실패: int.class는 primitive여야 함");
            return false;
        }
        
        // wrapper 타입 확인 테스트
        boolean isWrapper = PrimitiveTypeConverter.isPrimitiveWrapperType(Integer.class);
        if (!isWrapper) {
            System.err.println("wrapper 타입 확인 실패: Integer.class는 wrapper여야 함");
            return false;
        }
        
        // boxed -> primitive 변환 테스트
        result = PrimitiveTypeConverter.boxedTypeToPrimitiveType(Integer.class);
        if (!int.class.equals(result)) {
            System.err.println("Integer.class -> int.class 변환 실패: expected=int.class, actual=" + result);
            return false;
        }
        
        System.out.println("PrimitiveTypeConverter 테스트 통과");
        return true;
    }
    
    /**
     * JSON5ElementExtractor 클래스의 기능을 테스트합니다.
     */
    private static boolean testJSON5ElementExtractor() {
        // JSON5Object에서 값 추출 테스트
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("testString", "hello");
        json5Object.put("testInt", 123);
        json5Object.put("testNull", (Object) null);
        
        // String 값 추출
        Object result = JSON5ElementExtractor.getFrom(json5Object, "testString", Types.String);
        if (!"hello".equals(result)) {
            System.err.println("JSON5Object String 추출 실패: expected=hello, actual=" + result);
            return false;
        }
        
        // Integer 값 추출
        result = JSON5ElementExtractor.getFrom(json5Object, "testInt", Types.Integer);
        if (!Integer.valueOf(123).equals(result)) {
            System.err.println("JSON5Object Integer 추출 실패: expected=123, actual=" + result);
            return false;
        }
        
        // null 값 추출
        result = JSON5ElementExtractor.getFrom(json5Object, "testNull", Types.String);
        if (result != null) {
            System.err.println("JSON5Object null 추출 실패: expected=null, actual=" + result);
            return false;
        }
        
        // JSON5Array에서 값 추출 테스트
        JSON5Array json5Array = new JSON5Array();
        json5Array.add("array_hello");
        json5Array.add(456);
        json5Array.add((Object) null);
        
        // String 값 추출
        result = JSON5ElementExtractor.getFrom(json5Array, 0, Types.String);
        if (!"array_hello".equals(result)) {
            System.err.println("JSON5Array String 추출 실패: expected=array_hello, actual=" + result);
            return false;
        }
        
        // Integer 값 추출
        result = JSON5ElementExtractor.getFrom(json5Array, 1, Types.Integer);
        if (!Integer.valueOf(456).equals(result)) {
            System.err.println("JSON5Array Integer 추출 실패: expected=456, actual=" + result);
            return false;
        }
        
        // 안전한 추출 테스트
        result = JSON5ElementExtractor.getFromSafely(json5Array, 10, Types.String, "default");
        if (!"default".equals(result)) {
            System.err.println("안전한 추출 실패: expected=default, actual=" + result);
            return false;
        }
        
        // 타입 확인 테스트
        boolean isArray = JSON5ElementExtractor.isArray(json5Array);
        if (!isArray) {
            System.err.println("isArray 확인 실패: JSON5Array는 array여야 함");
            return false;
        }
        
        boolean isObject = JSON5ElementExtractor.isObject(json5Object);
        if (!isObject) {
            System.err.println("isObject 확인 실패: JSON5Object는 object여야 함");
            return false;
        }
        
        System.out.println("JSON5ElementExtractor 테스트 통과");
        return true;
    }
    
    /**
     * SerializerConstants 클래스의 기능을 테스트합니다.
     */
    private static boolean testSerializerConstants() {
        // 상수 값 확인 테스트
        if (SerializerConstants.DEFAULT_CHAR_VALUE != '\0') {
            System.err.println("DEFAULT_CHAR_VALUE 확인 실패: expected=\\0, actual=" + SerializerConstants.DEFAULT_CHAR_VALUE);
            return false;
        }
        
        if (SerializerConstants.DEFAULT_INT_VALUE != 0) {
            System.err.println("DEFAULT_INT_VALUE 확인 실패: expected=0, actual=" + SerializerConstants.DEFAULT_INT_VALUE);
            return false;
        }
        
        if (!SerializerConstants.ERROR_PRIMITIVE_TYPE.contains("primitive")) {
            System.err.println("ERROR_PRIMITIVE_TYPE 확인 실패: primitive 문자열을 포함해야 함");
            return false;
        }
        
        System.out.println("SerializerConstants 테스트 통과");
        return true;
    }
    
    /**
     * Utils 클래스의 하위 호환성을 테스트합니다.
     */
    @SuppressWarnings("deprecation")
    private static boolean testBackwardCompatibility() {
        // Utils.primitiveTypeToBoxedType 테스트
        Class<?> result = Utils.primitiveTypeToBoxedType(int.class);
        if (!Integer.class.equals(result)) {
            System.err.println("Utils.primitiveTypeToBoxedType 실패: expected=Integer.class, actual=" + result);
            return false;
        }
        
        // Utils.convertValueFromString 테스트
        Object convertResult = Utils.convertValueFromString("456", Types.Integer);
        if (!Integer.valueOf(456).equals(convertResult)) {
            System.err.println("Utils.convertValueFromString 실패: expected=456, actual=" + convertResult);
            return false;
        }
        
        // Utils.convertValue 테스트
        convertResult = Utils.convertValue("789", Types.Integer);
        if (!Integer.valueOf(789).equals(convertResult)) {
            System.err.println("Utils.convertValue 실패: expected=789, actual=" + convertResult);
            return false;
        }
        
        // Utils.getFrom 테스트
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("testKey", "testValue");
        Object extractResult = Utils.getFrom(json5Object, "testKey", Types.String);
        if (!"testValue".equals(extractResult)) {
            System.err.println("Utils.getFrom 실패: expected=testValue, actual=" + extractResult);
            return false;
        }
        
        System.out.println("하위 호환성 테스트 통과");
        return true;
    }
    
    /**
     * 메인 메소드 - 테스트 실행용
     */
    public static void main(String[] args) {
        System.out.println("=== JSON5 Serializer 1.1 단계 리팩토링 검증 시작 ===");
        
        boolean success = validateStep1Refactoring();
        
        if (success) {
            System.out.println("=== 검증 성공: 1.1 단계 리팩토링이 정상적으로 완료되었습니다 ===");
            System.exit(0);
        } else {
            System.err.println("=== 검증 실패: 1.1 단계 리팩토링에 문제가 있습니다 ===");
            System.exit(1);
        }
    }
}
