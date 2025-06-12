package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.MissingValueStrategy;
import com.hancomins.json5.serializer.path.JSON5PathExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParameterValueResolver 테스트")
class ParameterValueResolverTest {
    
    private ParameterValueResolver resolver;
    
    @BeforeEach
    void setUp() {
        resolver = new ParameterValueResolver();
    }
    
    @Test
    @DisplayName("기본 타입의 기본값을 올바르게 반환해야 함")
    void shouldReturnCorrectDefaultValues() {
        // Boolean
        assertEquals(false, resolver.getDefaultValue(boolean.class));
        assertNull(resolver.getDefaultValue(Boolean.class));
        
        // Character
        assertEquals('\0', resolver.getDefaultValue(char.class));
        assertNull(resolver.getDefaultValue(Character.class));
        
        // Numeric types
        assertEquals((byte) 0, resolver.getDefaultValue(byte.class));
        assertEquals((short) 0, resolver.getDefaultValue(short.class));
        assertEquals(0, resolver.getDefaultValue(int.class));
        assertEquals(0L, resolver.getDefaultValue(long.class));
        assertEquals(0.0f, resolver.getDefaultValue(float.class));
        assertEquals(0.0d, resolver.getDefaultValue(double.class));
        
        // Wrapper types
        assertNull(resolver.getDefaultValue(Byte.class));
        assertNull(resolver.getDefaultValue(Short.class));
        assertNull(resolver.getDefaultValue(Integer.class));
        assertNull(resolver.getDefaultValue(Long.class));
        assertNull(resolver.getDefaultValue(Float.class));
        assertNull(resolver.getDefaultValue(Double.class));
        
        // Reference types
        assertNull(resolver.getDefaultValue(String.class));
        assertNull(resolver.getDefaultValue(Object.class));
        assertNull(resolver.getDefaultValue(null));
    }
    
    @Test
    @DisplayName("누락된 값을 올바르게 처리해야 함")
    void shouldHandleMissingValues() {
        // Given
        ParameterInfo requiredParam = new ParameterInfo(
            "requiredField", String.class, MissingValueStrategy.DEFAULT_VALUE, true, 0
        );
        ParameterInfo optionalParam = new ParameterInfo(
            "optionalField", String.class, MissingValueStrategy.DEFAULT_VALUE, false, 1
        );
        ParameterInfo exceptionParam = new ParameterInfo(
            "exceptionField", String.class, MissingValueStrategy.EXCEPTION, false, 2
        );
        
        // When & Then
        // 필수 파라미터 누락 시 예외 발생
        assertThrows(JSON5SerializerException.class, () -> 
            resolver.resolveValue(JSON5PathExtractor.MISSING_VALUE_MARKER, String.class, requiredParam)
        );
        
        // 선택적 파라미터 누락 시 기본값 반환
        Object result = resolver.resolveValue(JSON5PathExtractor.MISSING_VALUE_MARKER, String.class, optionalParam);
        assertNull(result);
        
        // EXCEPTION 전략 파라미터 누락 시 예외 발생
        assertThrows(JSON5SerializerException.class, () -> 
            resolver.resolveValue(JSON5PathExtractor.MISSING_VALUE_MARKER, String.class, exceptionParam)
        );
    }
    
    @Test
    @DisplayName("null 값을 올바르게 처리해야 함")
    void shouldHandleNullValues() {
        // Given
        ParameterInfo requiredParam = new ParameterInfo(
            "requiredField", String.class, MissingValueStrategy.DEFAULT_VALUE, true, 0
        );
        ParameterInfo optionalParam = new ParameterInfo(
            "optionalField", String.class, MissingValueStrategy.DEFAULT_VALUE, false, 1
        );
        
        // When & Then
        // 필수 파라미터가 null이면 예외 발생
        assertThrows(JSON5SerializerException.class, () -> 
            resolver.resolveValue(null, String.class, requiredParam)
        );
        
        // 선택적 파라미터가 null이면 기본값 반환
        Object result = resolver.resolveValue(null, String.class, optionalParam);
        assertNull(result);
    }
    
    @Test
    @DisplayName("기본 타입 변환을 올바르게 수행해야 함")
    void shouldConvertPrimitiveTypes() {
        // Given
        ParameterInfo stringParam = new ParameterInfo(
            "field", String.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        ParameterInfo intParam = new ParameterInfo(
            "field", int.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        ParameterInfo booleanParam = new ParameterInfo(
            "field", boolean.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        ParameterInfo doubleParam = new ParameterInfo(
            "field", double.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        // String 변환
        Object stringResult = resolver.resolveValue("test", String.class, stringParam);
        assertEquals("test", stringResult);
        
        Object numberToStringResult = resolver.resolveValue(123, String.class, stringParam);
        assertEquals("123", numberToStringResult);
        
        // int 변환
        Object intResult = resolver.resolveValue("42", int.class, intParam);
        assertEquals(42, intResult);
        
        // boolean 변환
        Object booleanResult1 = resolver.resolveValue("true", boolean.class, booleanParam);
        assertEquals(true, booleanResult1);
        
        Object booleanResult2 = resolver.resolveValue("false", boolean.class, booleanParam);
        assertEquals(false, booleanResult2);
        
        // double 변환
        Object doubleResult = resolver.resolveValue("3.14", double.class, doubleParam);
        assertEquals(3.14, doubleResult);
    }
    
    @Test
    @DisplayName("래퍼 타입 변환을 올바르게 수행해야 함")
    void shouldConvertWrapperTypes() {
        // Given
        ParameterInfo integerParam = new ParameterInfo(
            "field", Integer.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        ParameterInfo booleanParam = new ParameterInfo(
            "field", Boolean.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        Object integerResult = resolver.resolveValue("123", Integer.class, integerParam);
        assertEquals(123, integerResult);
        
        Object booleanResult = resolver.resolveValue("true", Boolean.class, booleanParam);
        assertEquals(true, booleanResult);
    }
    
    @Test
    @DisplayName("이미 올바른 타입인 값은 그대로 반환해야 함")
    void shouldReturnValueAsIsIfCorrectType() {
        // Given
        ParameterInfo stringParam = new ParameterInfo(
            "field", String.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        ParameterInfo intParam = new ParameterInfo(
            "field", Integer.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        String stringValue = "test";
        Object stringResult = resolver.resolveValue(stringValue, String.class, stringParam);
        assertSame(stringValue, stringResult);
        
        Integer intValue = 42;
        Object intResult = resolver.resolveValue(intValue, Integer.class, intParam);
        assertSame(intValue, intResult);
    }
    
    @Test
    @DisplayName("예외 발생 조건을 올바르게 판단해야 함")
    void shouldCorrectlyDetermineExceptionConditions() {
        // Given
        ParameterInfo requiredParam = new ParameterInfo(
            "field", String.class, MissingValueStrategy.DEFAULT_VALUE, true, 0
        );
        ParameterInfo exceptionParam = new ParameterInfo(
            "field", String.class, MissingValueStrategy.EXCEPTION, false, 0
        );
        ParameterInfo normalParam = new ParameterInfo(
            "field", String.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        assertTrue(resolver.shouldThrowException(requiredParam));
        assertTrue(resolver.shouldThrowException(exceptionParam));
        assertFalse(resolver.shouldThrowException(normalParam));
    }
    
    @Test
    @DisplayName("변환 실패 시 예외를 발생시켜야 함")
    void shouldThrowExceptionOnConversionFailure() {
        // Given
        ParameterInfo intParam = new ParameterInfo(
            "field", int.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        assertThrows(JSON5SerializerException.class, () -> 
            resolver.resolveValue("not-a-number", int.class, intParam)
        );
    }
    
    @Test
    @DisplayName("Character 타입 변환을 올바르게 처리해야 함")
    void shouldHandleCharacterConversion() {
        // Given
        ParameterInfo charParam = new ParameterInfo(
            "field", char.class, MissingValueStrategy.DEFAULT_VALUE, false, 0
        );
        
        // When & Then
        Object result1 = resolver.resolveValue("A", char.class, charParam);
        assertEquals('A', result1);
        
        Object result2 = resolver.resolveValue("", char.class, charParam);
        assertEquals('\0', result2);
        
        Object result3 = resolver.resolveValue("Hello", char.class, charParam);
        assertEquals('H', result3); // 첫 번째 문자만 가져옴
    }
}
