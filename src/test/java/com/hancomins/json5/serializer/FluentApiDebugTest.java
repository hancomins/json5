package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fluent API 디버그 테스트
 */
@DisplayName("Fluent API 디버그 테스트")
public class FluentApiDebugTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        try {
            serializer = JSON5Serializer.builder().build();
            System.out.println("Serializer created successfully: " + serializer);
        } catch (Exception e) {
            System.err.println("Error creating serializer: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @DisplayName("기본 Fluent API 동작 확인")
    void testBasicFluentApi() {
        try {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            System.out.println("Test object created: " + obj);
            
            // When - forSerialization() 메소드 테스트
            SerializationBuilder builder = serializer.forSerialization();
            System.out.println("SerializationBuilder created: " + builder);
            
            // 기본 직렬화 테스트
            JSON5Object result = builder.serialize(obj);
            System.out.println("Serialization result: " + result);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getString("name"));
            assertEquals(42, result.getInt("value"));
            
        } catch (Exception e) {
            System.err.println("Error in testBasicFluentApi: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @DisplayName("WritingOptions 설정 테스트")
    void testWithWritingOptions() {
        try {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            
            // When
            JSON5Object result = serializer.forSerialization()
                .withWritingOptions(WritingOptions.json5Pretty())
                .serialize(obj);
            
            System.out.println("Result: " + result);
            System.out.println("Result WritingOptions: " + 
                (result != null ? result.getWritingOptions() : "null"));
            
            // Then
            assertNotNull(result);
            assertEquals(WritingOptions.json5Pretty(), result.getWritingOptions());
            
        } catch (Exception e) {
            System.err.println("Error in testWithWritingOptions: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @DisplayName("Deserialization Builder 테스트")
    void testDeserializationBuilder() {
        try {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("value", 42);
            
            // When
            DeserializationBuilder builder = serializer.forDeserialization();
            System.out.println("DeserializationBuilder created: " + builder);
            
            SimpleTestObject result = builder.deserialize(json, SimpleTestObject.class);
            System.out.println("Deserialization result: " + result);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
            assertEquals(42, result.getValue());
            
        } catch (Exception e) {
            System.err.println("Error in testDeserializationBuilder: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
