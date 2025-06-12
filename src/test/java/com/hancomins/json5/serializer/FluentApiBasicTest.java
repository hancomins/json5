package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fluent API의 기본 기능들을 테스트하는 클래스입니다.
 */
@DisplayName("Fluent API 기본 기능 테스트")
public class FluentApiBasicTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.builder().build();
    }
    
    @Nested
    @DisplayName("SerializationBuilder 기본 기능 테스트")
    class SerializationBuilderBasicTest {
        
        @Test
        @DisplayName("기본 직렬화 테스트")
        void testBasicSerialization() {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            
            // When
            JSON5Object result = serializer.forSerialization()
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getString("name"));
            assertEquals(42, result.getInt("value"));
        }
        
        @Test
        @DisplayName("WritingOptions 설정 테스트")
        void testWithWritingOptions() {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            
            // When
            JSON5Object result = serializer.forSerialization()
                .withWritingOptions(WritingOptions.json5Pretty())
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertEquals(WritingOptions.json5Pretty(), result.getWritingOptions());
        }
        
        @Test
        @DisplayName("null 값 포함 테스트")
        void testIncludeNullValues() {
            // Given
            SimpleTestObject obj = new SimpleTestObject(null, 42);
            
            // When
            JSON5Object result = serializer.forSerialization()
                .includeNullValues()
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.has("name"));
            assertNull(result.get("name"));
            assertEquals(42, result.getInt("value"));
        }
        
        @Test
        @DisplayName("필드 무시 테스트")
        void testIgnoreFields() {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            
            // When
            JSON5Object result = serializer.forSerialization()
                .ignoreFields("value")
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getString("name"));
            assertFalse(result.has("value")); // 무시된 필드
        }
        
        @Test
        @DisplayName("메소드 체이닝 테스트")
        void testMethodChaining() {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            
            // When - 여러 옵션을 체인으로 연결
            JSON5Object result = serializer.forSerialization()
                .withWritingOptions(WritingOptions.json5())
                .includeNullValues()
                .ignoreFields("nonexistent")
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getString("name"));
            assertEquals(42, result.getInt("value"));
            assertEquals(WritingOptions.json5(), result.getWritingOptions());
        }
    }
    
    @Nested
    @DisplayName("DeserializationBuilder 기본 기능 테스트")
    class DeserializationBuilderBasicTest {
        
        @Test
        @DisplayName("기본 역직렬화 테스트")
        void testBasicDeserialization() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("value", 42);
            
            // When
            SimpleTestObject result = serializer.forDeserialization()
                .deserialize(json, SimpleTestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
            assertEquals(42, result.getValue());
        }
        
        @Test
        @DisplayName("오류 무시 테스트")
        void testIgnoreErrors() {
            // Given - 잘못된 데이터 타입
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("value", "not_a_number");
            
            // When
            SimpleTestObject result = serializer.forDeserialization()
                .ignoreErrors()
                .deserialize(json, SimpleTestObject.class);
            
            // Then - 오류가 무시되고 객체가 생성됨
            assertNotNull(result);
            assertEquals("test", result.getName());
            // value는 기본값(0)이 될 것
        }
        
        @Test
        @DisplayName("엄격한 타입 검사 테스트")
        void testStrictTypeChecking() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("value", 42);
            
            // When
            SimpleTestObject result = serializer.forDeserialization()
                .withStrictTypeChecking(true)
                .deserialize(json, SimpleTestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
            assertEquals(42, result.getValue());
        }
        
        @Test
        @DisplayName("기본값 설정 테스트")
        void testWithDefaultValue() {
            // Given - 빈 JSON
            JSON5Object json = new JSON5Object();
            
            // When
            SimpleTestObject defaultObj = new SimpleTestObject("default", 999);
            SimpleTestObject result = serializer.forDeserialization()
                .ignoreErrors()
                .withDefaultValue(defaultObj)
                .deserialize(json, SimpleTestObject.class);
            
            // Then - 기본값이 사용되지는 않지만 오류 처리에 사용됨
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("기존 객체에 역직렬화 테스트")
        void testDeserializeToExistingObject() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "updated");
            json.put("value", 999);
            
            SimpleTestObject existingObj = new SimpleTestObject("original", 42);
            
            // When
            SimpleTestObject result = serializer.forDeserialization()
                .deserialize(json, existingObj);
            
            // Then
            assertNotNull(result);
            assertSame(existingObj, result); // 같은 객체 인스턴스
            assertEquals("updated", result.getName());
            assertEquals(999, result.getValue());
        }
        
        @Test
        @DisplayName("메소드 체이닝 테스트")
        void testMethodChaining() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("value", 42);
            
            // When - 여러 옵션을 체인으로 연결
            SimpleTestObject result = serializer.forDeserialization()
                .ignoreErrors()
                .withStrictTypeChecking(false)
                .withDefaultValue(new SimpleTestObject("default", 0))
                .deserialize(json, SimpleTestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
            assertEquals(42, result.getValue());
        }
    }
    
    @Test
    @DisplayName("전체 통합 테스트")
    void testFullIntegration() {
        // Given
        SimpleTestObject original = new SimpleTestObject("integration_test", 12345);
        
        // When - 직렬화 후 역직렬화
        JSON5Object json = serializer.forSerialization()
            .withWritingOptions(WritingOptions.json5Pretty())
            .serialize(original);
        
        SimpleTestObject restored = serializer.forDeserialization()
            .withStrictTypeChecking(true)
            .deserialize(json, SimpleTestObject.class);
        
        // Then
        assertNotNull(json);
        assertNotNull(restored);
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getValue(), restored.getValue());
        assertEquals(WritingOptions.json5Pretty(), json.getWritingOptions());
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
        
        @Override
        public String toString() {
            return "SimpleTestObject{name='" + name + "', value=" + value + "}";
        }
    }
}
