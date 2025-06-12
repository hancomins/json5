package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fluent API 간단한 문제 확인 테스트
 */
@DisplayName("Fluent API 간단한 문제 확인")
public class FluentApiSimpleDebugTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.builder().build();
    }
    
    @Test
    @DisplayName("ignoreFields 동작 확인")
    void testIgnoreFieldsBasic() {
        try {
            // Given
            SimpleTestObject obj = new SimpleTestObject("test", 42);
            System.out.println("Original object: " + obj);
            
            // When - value 필드 무시하고 직렬화
            SerializationBuilder builder = serializer.forSerialization();
            System.out.println("SerializationBuilder created: " + builder);
            
            SerializationBuilder builderWithIgnore = builder.ignoreFields("value");
            System.out.println("ignoreFields applied: " + builderWithIgnore);
            
            JSON5Object result = builderWithIgnore.serialize(obj);
            System.out.println("Serialization result: " + result);
            
            if (result != null) {
                System.out.println("Result keys: " + result.keySet());
                System.out.println("Has 'name': " + result.has("name"));
                System.out.println("Has 'value': " + result.has("value"));
                
                if (result.has("name")) {
                    System.out.println("Name value: " + result.getString("name"));
                }
                if (result.has("value")) {
                    System.out.println("Value value: " + result.getInt("value"));
                }
            }
            
            // Then
            assertNotNull(result);
            assertTrue(result.has("name"));
            assertFalse(result.has("value")); // 무시된 필드는 없어야 함
            
        } catch (Exception e) {
            System.err.println("Error in testIgnoreFieldsBasic: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @DisplayName("기존 객체 역직렬화 동작 확인")
    void testDeserializeToExistingObject() {
        try {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "updated");
            json.put("value", 999);
            System.out.println("JSON to deserialize: " + json);
            
            SimpleTestObject existingObj = new SimpleTestObject("original", 42);
            System.out.println("Existing object before: " + existingObj);
            System.out.println("Existing object ID: " + System.identityHashCode(existingObj));
            
            // When
            DeserializationBuilder builder = serializer.forDeserialization();
            System.out.println("DeserializationBuilder created: " + builder);
            
            SimpleTestObject result = builder.deserialize(json, existingObj);
            System.out.println("Deserialization result: " + result);
            System.out.println("Result object ID: " + System.identityHashCode(result));
            System.out.println("Are same object? " + (existingObj == result));
            
            if (result != null) {
                System.out.println("Result name: " + result.getName());
                System.out.println("Result value: " + result.getValue());
            }
            
            // Then
            assertNotNull(result);
            assertSame(existingObj, result); // 같은 객체 인스턴스여야 함
            assertEquals("updated", result.getName());
            assertEquals(999, result.getValue());
            
        } catch (Exception e) {
            System.err.println("Error in testDeserializeToExistingObject: " + e.getMessage());
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
