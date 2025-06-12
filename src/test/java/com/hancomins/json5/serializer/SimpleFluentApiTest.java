package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 간단한 Fluent API 테스트
 */
@DisplayName("간단한 Fluent API 테스트")
public class SimpleFluentApiTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.builder().build();
    }
    
    @Test
    @DisplayName("기본 SerializationBuilder 테스트")
    void testBasicSerializationBuilder() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When
        SerializationBuilder builder = serializer.forSerialization();
        
        // Then
        assertNotNull(builder);
    }
    
    @Test
    @DisplayName("기본 DeserializationBuilder 테스트")
    void testBasicDeserializationBuilder() {
        // When
        DeserializationBuilder builder = serializer.forDeserialization();
        
        // Then
        assertNotNull(builder);
    }
    
    @Test
    @DisplayName("고급 체인 옵션 - when/then 테스트")
    void testWhenThenChaining() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When
        SerializationBuilder builder = serializer.forSerialization()
            .when(o -> o instanceof SimpleTestObject)
            .then(b -> b.includeNullValues());
        
        // Then
        assertNotNull(builder);
    }
    
    @Test
    @DisplayName("고급 체인 옵션 - transform 테스트")
    void testTransformChaining() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When
        SerializationBuilder builder = serializer.forSerialization()
            .transform(o -> o); // 단순히 자기 자신을 반환
        
        // Then
        assertNotNull(builder);
    }
    
    @Test
    @DisplayName("고급 체인 옵션 - filter 테스트")
    void testFilterChaining() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When
        SerializationBuilder builder = serializer.forSerialization()
            .filter(o -> true); // 모든 객체 허용
        
        // Then
        assertNotNull(builder);
    }
    
    @Test
    @DisplayName("DeserializationBuilder 고급 체인 옵션 테스트")
    void testDeserializationAdvancedChaining() {
        // When
        DeserializationBuilder builder = serializer.forDeserialization()
            .validateWith(obj -> true)
            .enableStrictValidation()
            .withFieldDefault("name", "default");
        
        // Then
        assertNotNull(builder);
    }
    
    // 간단한 테스트용 클래스
    @JSON5Type
    public static class SimpleTestObject {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private int value;
        
        public SimpleTestObject() {}
        
        public SimpleTestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getValue() {
            return value;
        }
        
        public void setValue(int value) {
            this.value = value;
        }
    }
}
