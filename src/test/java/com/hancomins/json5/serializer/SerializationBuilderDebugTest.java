package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SerializationBuilder의 실제 동작을 단계별로 테스트
 */
@DisplayName("SerializationBuilder 디버그 테스트")
public class SerializationBuilderDebugTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.builder().build();
    }
    
    @Test
    @DisplayName("기본 직렬화 동작 확인")
    void testBasicSerialization() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        try {
            // When - 단계별로 확인
            SerializationBuilder builder = serializer.forSerialization();
            assertNotNull(builder);
            System.out.println("Builder 생성 성공");
            
            JSON5Object result = builder.serialize(obj);
            System.out.println("직렬화 완료: " + (result != null ? result.toString() : "null"));
            
            // Then
            assertNotNull(result);
            if (result.has("name")) {
                System.out.println("name 필드 존재: " + result.getString("name"));
            }
            if (result.has("value")) {
                System.out.println("value 필드 존재: " + result.getInt("value"));
            }
            
        } catch (Exception e) {
            System.err.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
            fail("예외 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("TypeSchema 확인")
    void testTypeSchema() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        try {
            // TypeSchema 확인
            TypeSchema schema = TypeSchemaMap.getInstance().getTypeInfo(obj.getClass());
            System.out.println("TypeSchema: " + (schema != null ? "존재" : "없음"));
            
            if (schema != null) {
                System.out.println("Schema Type: " + schema.getType());
                System.out.println("Schema explicit: " + schema.isExplicit());
            }
            
        } catch (Exception e) {
            System.err.println("TypeSchema 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    @DisplayName("기존 시스템과 비교")
    void testCompareWithExistingSystem() {
        // Given
        SimpleTestObject obj = new SimpleTestObject("test", 42);
        
        try {
            // 기존 시스템으로 직렬화
            JSON5Object oldResult = JSON5Serializer.toJSON5Object(obj);
            System.out.println("기존 시스템 결과: " + (oldResult != null ? oldResult.toString() : "null"));
            
            // 새로운 시스템으로 직렬화
            JSON5Object newResult = serializer.forSerialization().serialize(obj);
            System.out.println("새 시스템 결과: " + (newResult != null ? newResult.toString() : "null"));
            
            // 비교
            if (oldResult != null && newResult != null) {
                System.out.println("결과 동일성: " + oldResult.toString().equals(newResult.toString()));
            }
            
        } catch (Exception e) {
            System.err.println("비교 테스트 오류: " + e.getMessage());
            e.printStackTrace();
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
    }
}
