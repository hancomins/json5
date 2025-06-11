package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON에 키가 없을 때 기본값을 유지하는 기능을 테스트합니다.
 */
@DisplayName("기본값 유지 기능 테스트")
public class KeepDefaultValueTest {

    @JSON5Type
    public static class SimpleTestClass {
        @JSON5Value
        private String name = "defaultName";
        
        @JSON5Value
        private int age = 25;
        
        @JSON5Value
        private boolean active = true;
        
        @JSON5Value
        private double score = 100.0;
        
        // 기본 생성자
        public SimpleTestClass() {}
        
        // Getter/Setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
    
    @JSON5Type
    public static class CollectionTestClass {
        @JSON5Value
        private List<String> items = Arrays.asList("default1", "default2");
        
        @JSON5Value
        private Map<String, String> metadata = new HashMap<String, String>() {{
            put("key1", "defaultValue1");
            put("key2", "defaultValue2");
        }};
        
        // 기본 생성자
        public CollectionTestClass() {}
        
        // Getter/Setter
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }
        
        public Map<String, String> getMetadata() { return metadata; }
        public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
    }
    
    @JSON5Type
    public static class NestedTestClass {
        @JSON5Value
        private String id = "defaultId";
        
        @JSON5Value
        private SimpleTestClass nested = new SimpleTestClass();
        
        // 기본 생성자
        public NestedTestClass() {}
        
        // Getter/Setter
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public SimpleTestClass getNested() { return nested; }
        public void setNested(SimpleTestClass nested) { this.nested = nested; }
    }

    @Test
    @DisplayName("기본 타입 필드의 기본값이 유지되어야 함")
    public void testPrimitiveFieldsKeepDefaultValues() {
        // Given: JSON에 일부 필드만 있음
        JSON5Object json = new JSON5Object();
        json.put("name", "John");
        // age, active, score는 JSON에 없음
        
        // When: 역직렬화 수행
        SimpleTestClass result = JSON5Serializer.fromJSON5Object(json, SimpleTestClass.class);
        
        // Then: JSON에 있는 필드는 JSON 값으로, 없는 필드는 기본값 유지
        assertEquals("John", result.getName()); // JSON에서 설정된 값
        assertEquals(25, result.getAge()); // 기본값 유지
        assertTrue(result.isActive()); // 기본값 유지
        assertEquals(100.0, result.getScore()); // 기본값 유지
    }
    
    @Test
    @DisplayName("모든 필드가 JSON에 없으면 모든 기본값이 유지되어야 함")
    public void testAllFieldsKeepDefaultValues() {
        // Given: 빈 JSON
        JSON5Object json = new JSON5Object();
        
        // When: 역직렬화 수행
        SimpleTestClass result = JSON5Serializer.fromJSON5Object(json, SimpleTestClass.class);
        
        // Then: 모든 필드가 기본값 유지
        assertEquals("defaultName", result.getName());
        assertEquals(25, result.getAge());
        assertTrue(result.isActive());
        assertEquals(100.0, result.getScore());
    }
    
    @Test
    @DisplayName("JSON에 null 값이 있으면 null로 설정되어야 함")
    public void testNullValuesOverrideDefaults() {
        // Given: JSON에 null 값 포함
        JSON5Object json = new JSON5Object();
        json.put("name", (String) null);
        json.put("age", 30);
        
        // When: 역직렬화 수행
        SimpleTestClass result = JSON5Serializer.fromJSON5Object(json, SimpleTestClass.class);
        
        // Then: null은 null로 설정, 나머지는 기본값 유지
        assertNull(result.getName()); // null로 설정
        assertEquals(30, result.getAge()); // JSON 값으로 설정
        assertTrue(result.isActive()); // 기본값 유지
        assertEquals(100.0, result.getScore()); // 기본값 유지
    }
    
    @Test
    @DisplayName("컬렉션 필드의 기본값이 유지되어야 함")
    public void testCollectionFieldsKeepDefaultValues() {
        // Given: JSON에 컬렉션 필드가 없음
        JSON5Object json = new JSON5Object();
        // items, metadata 필드 없음
        
        // When: 역직렬화 수행
        CollectionTestClass result = JSON5Serializer.fromJSON5Object(json, CollectionTestClass.class);
        
        // Then: 컬렉션 필드들이 기본값 유지
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        assertEquals("default1", result.getItems().get(0));
        assertEquals("default2", result.getItems().get(1));
        
        assertNotNull(result.getMetadata());
        assertEquals(2, result.getMetadata().size());
        assertEquals("defaultValue1", result.getMetadata().get("key1"));
        assertEquals("defaultValue2", result.getMetadata().get("key2"));
    }
    
    @Test
    @DisplayName("중첩 객체의 기본값이 유지되어야 함")
    public void testNestedObjectKeepsDefaultValues() {
        // Given: JSON에 nested 객체 정보가 없음
        JSON5Object json = new JSON5Object();
        json.put("id", "customId");
        // nested 필드 없음
        
        // When: 역직렬화 수행
        NestedTestClass result = JSON5Serializer.fromJSON5Object(json, NestedTestClass.class);
        
        // Then: id는 JSON 값으로, nested는 기본값 유지
        assertEquals("customId", result.getId());
        assertNotNull(result.getNested());
        assertEquals("defaultName", result.getNested().getName());
        assertEquals(25, result.getNested().getAge());
        assertTrue(result.getNested().isActive());
        assertEquals(100.0, result.getNested().getScore());
    }
    
    @Test
    @DisplayName("기존 객체에 역직렬화할 때 기본값이 유지되어야 함")
    public void testDeserializeToExistingObjectKeepsDefaults() {
        // Given: 기존 객체가 있고 JSON에 일부 필드만 있음
        SimpleTestClass existing = new SimpleTestClass();
        existing.setName("existingName");
        existing.setAge(99);
        existing.setActive(false);
        existing.setScore(50.5);
        
        JSON5Object json = new JSON5Object();
        json.put("name", "updatedName");
        // age, active, score는 JSON에 없음
        
        // When: 기존 객체에 역직렬화
        SimpleTestClass result = JSON5Serializer.fromJSON5Object(json, existing);
        
        // Then: JSON에 있는 필드만 업데이트, 나머지는 기존 값 유지
        assertEquals("updatedName", result.getName()); // JSON에서 업데이트
        assertEquals(99, result.getAge()); // 기존 값 유지
        assertFalse(result.isActive()); // 기존 값 유지
        assertEquals(50.5, result.getScore()); // 기존 값 유지
        
        // 같은 객체 인스턴스여야 함
        assertSame(existing, result);
    }
    
    @Test
    @DisplayName("JSON5ElementExtractor.isMissingKey() 메소드 테스트")
    public void testIsMissingKeyMethod() {
        // Given
        Object normalValue = "test";
        Object missingKeyMarker = JSON5ElementExtractor.MISSING_KEY_MARKER;
        Object nullValue = null;
        
        // Then
        assertFalse(JSON5ElementExtractor.isMissingKey(normalValue));
        assertTrue(JSON5ElementExtractor.isMissingKey(missingKeyMarker));
        assertFalse(JSON5ElementExtractor.isMissingKey(nullValue));
    }
    
    @Test
    @DisplayName("JSON5ElementExtractor.hasKey() 메소드 테스트")
    public void testHasKeyMethod() {
        // Given
        JSON5Object jsonObject = new JSON5Object();
        jsonObject.put("existingKey", "value");
        jsonObject.put("nullKey", (String) null);
        
        // Then - JSON5Object 테스트
        assertTrue(JSON5ElementExtractor.hasKey(jsonObject, "existingKey"));
        assertTrue(JSON5ElementExtractor.hasKey(jsonObject, "nullKey")); // null 값도 키가 존재하는 것
        assertFalse(JSON5ElementExtractor.hasKey(jsonObject, "nonExistingKey"));
        
        // null 체크
        assertFalse(JSON5ElementExtractor.hasKey(null, "key"));
        assertFalse(JSON5ElementExtractor.hasKey(jsonObject, null));
    }
    
    @Test
    @DisplayName("JSON5ElementExtractor에서 MISSING_KEY_MARKER 반환 테스트")
    public void testMissingKeyMarkerReturned() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("existingKey", "value");
        
        // When & Then - 존재하는 키
        Object existingValue = JSON5ElementExtractor.getFrom(json, "existingKey", Types.String);
        assertEquals("value", existingValue);
        assertFalse(JSON5ElementExtractor.isMissingKey(existingValue));
        
        // When & Then - 존재하지 않는 키
        Object missingValue = JSON5ElementExtractor.getFrom(json, "nonExistingKey", Types.String);
        assertTrue(JSON5ElementExtractor.isMissingKey(missingValue));
        assertEquals(JSON5ElementExtractor.MISSING_KEY_MARKER, missingValue);
    }
}
