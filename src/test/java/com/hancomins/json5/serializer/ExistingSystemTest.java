package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 기존 JSON5Serializer가 정상 동작하는지 확인하는 테스트
 */
@DisplayName("기존 시스템 동작 확인 테스트")
public class ExistingSystemTest {
    
    @Test
    @DisplayName("기존 static 메소드 테스트")
    void testExistingStaticMethods() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When - 기존 static 메소드 사용
        JSON5Object json = JSON5Serializer.toJSON5Object(obj);
        SimpleTestObject restored = JSON5Serializer.fromJSON5Object(json, SimpleTestObject.class);
        
        // Then
        assertNotNull(json);
        assertNotNull(restored);
        assertEquals("test", restored.getName());
        assertEquals(42, restored.getValue());
    }
    
    @Test
    @DisplayName("기존 인스턴스 메소드 테스트")
    void testExistingInstanceMethods() {
        // Given
        JSON5Serializer serializer = JSON5Serializer.getInstance();
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        // When
        JSON5Object json = serializer.serialize(obj);
        SimpleTestObject restored = serializer.deserialize(json, SimpleTestObject.class);
        
        // Then
        assertNotNull(json);
        assertNotNull(restored);
        assertEquals("test", restored.getName());
        assertEquals(42, restored.getValue());
    }
    
    @Test
    @DisplayName("Builder 생성 테스트")
    void testBuilderCreation() {
        // When
        JSON5Serializer serializer = JSON5Serializer.builder().build();
        
        // Then
        assertNotNull(serializer);
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
