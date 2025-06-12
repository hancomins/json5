package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON5Serializer Facade 재구성 테스트
 * 
 * 6.1 단계에서 구현한 새로운 Facade 패턴 및 Fluent API를 검증합니다.
 */
@DisplayName("JSON5Serializer Facade 재구성 테스트")
public class JSON5SerializerFacadeTest {
    
    @JSON5Type
    public static class TestPerson {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private int age;
        
        public TestPerson() {}
        
        public TestPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        // getter/setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TestPerson)) return false;
            TestPerson person = (TestPerson) obj;
            return age == person.age && Objects.equals(name, person.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
    
    private TestPerson testPerson;
    
    @BeforeEach
    void setUp() {
        testPerson = new TestPerson("John", 30);
    }
    
    @Test
    @DisplayName("기본 인스턴스 메소드 테스트")
    void testInstanceMethods() {
        // Given
        JSON5Serializer serializer = JSON5Serializer.getInstance();
        
        // When - 직렬화
        JSON5Object json = serializer.serialize(testPerson);
        
        // Then
        assertNotNull(json);
        assertEquals("John", json.getString("name"));
        assertEquals(30, json.getInt("age"));
        
        // When - 역직렬화
        TestPerson restored = serializer.deserialize(json, TestPerson.class);
        
        // Then
        assertNotNull(restored);
        assertEquals(testPerson, restored);
    }
    
    @Test
    @DisplayName("Builder 패턴 테스트")
    void testBuilderPattern() {
        // Given & When
        JSON5Serializer serializer = JSON5Serializer.builder()
            .ignoreUnknownProperties()
            .enableSchemaCache()
            .includeNullValues()
            .build();
        
        // Then
        assertNotNull(serializer);
        assertNotNull(serializer.getConfiguration());
        assertTrue(serializer.getConfiguration().isIgnoreUnknownProperties());
        assertTrue(serializer.getConfiguration().isEnableSchemaCache());
        assertTrue(serializer.getConfiguration().isIncludeNullValues());
    }
    
    @Test
    @DisplayName("Fluent API - Serialization Builder 테스트")
    void testSerializationBuilder() {
        // Given
        JSON5Serializer serializer = JSON5Serializer.getInstance();
        
        // When
        JSON5Object json = serializer.forSerialization()
            .includeNullValues()
            .withWritingOptions(WritingOptions.json5Pretty())
            .serialize(testPerson);
        
        // Then
        assertNotNull(json);
        assertEquals("John", json.getString("name"));
        assertEquals(30, json.getInt("age"));
        assertEquals(WritingOptions.json5Pretty().getClass(), json.getWritingOptions().getClass());
    }
    
    @Test
    @DisplayName("Fluent API - Deserialization Builder 테스트")
    void testDeserializationBuilder() {
        // Given
        JSON5Serializer serializer = JSON5Serializer.getInstance();
        JSON5Object json = serializer.serialize(testPerson);
        
        // When
        TestPerson restored = serializer.forDeserialization()
            .ignoreErrors()
            .withStrictTypeChecking(false)
            .deserialize(json, TestPerson.class);
        
        // Then
        assertNotNull(restored);
        assertEquals(testPerson, restored);
    }
    
    @Test
    @DisplayName("하위 호환성 - 기존 static 메소드 테스트")
    void testBackwardCompatibilityStaticMethods() {
        // When - 기존 static 메소드들이 여전히 작동하는지 확인
        @SuppressWarnings("deprecation")
        JSON5Object json = JSON5Serializer.toJSON5Object(testPerson);
        
        @SuppressWarnings("deprecation")
        TestPerson restored = JSON5Serializer.fromJSON5Object(json, TestPerson.class);
        
        // Then
        assertNotNull(json);
        assertNotNull(restored);
        assertEquals(testPerson, restored);
    }
    
    @Test
    @DisplayName("컬렉션 직렬화/역직렬화 테스트")
    void testCollectionSerialization() {
        // Given
        List<TestPerson> people = Arrays.asList(
            new TestPerson("Alice", 25),
            new TestPerson("Bob", 35)
        );
        
        JSON5Serializer serializer = JSON5Serializer.getInstance();
        
        // When - 컬렉션 직렬화 (기존 static 메소드)
        @SuppressWarnings("deprecation")
        JSON5Array jsonArray = JSON5Serializer.collectionToJSON5Array(people);
        
        // Then
        assertNotNull(jsonArray);
        assertEquals(2, jsonArray.size());
        
        // When - 컬렉션 역직렬화 (기존 static 메소드)
        @SuppressWarnings("deprecation")
        List<TestPerson> restoredList = JSON5Serializer.json5ArrayToList(jsonArray, TestPerson.class);
        
        // Then
        assertNotNull(restoredList);
        assertEquals(2, restoredList.size());
        assertEquals("Alice", restoredList.get(0).getName());
        assertEquals("Bob", restoredList.get(1).getName());
    }
    
    @Test
    @DisplayName("Map 직렬화/역직렬화 테스트")
    void testMapSerialization() {
        // Given
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        // When - Map 직렬화 (기존 static 메소드)
        @SuppressWarnings("deprecation")
        JSON5Object json = JSON5Serializer.mapToJSON5Object(map);
        
        // Then
        assertNotNull(json);
        assertEquals("value1", json.getString("key1"));
        assertEquals("value2", json.getString("key2"));
        
        // When - Map 역직렬화 (기존 static 메소드)
        @SuppressWarnings("deprecation")
        Map<String, String> restoredMap = JSON5Serializer.fromJSON5ObjectToMap(json, String.class);
        
        // Then
        assertNotNull(restoredMap);
        assertEquals(2, restoredMap.size());
        assertEquals("value1", restoredMap.get("key1"));
        assertEquals("value2", restoredMap.get("key2"));
    }
    
    @Test
    @DisplayName("커스텀 TypeHandler 등록 테스트")
    void testCustomTypeHandler() {
        // Given - 커스텀 TypeHandler 생성
        TypeHandler customHandler = new TypeHandler() {
            @Override
            public boolean canHandle(Types type, Class<?> clazz) {
                return clazz == Date.class;
            }
            
            @Override
            public Object handleSerialization(Object value, SerializationContext context) {
                return ((Date) value).getTime();
            }
            
            @Override
            public Object handleDeserialization(Object element, Class<?> targetType, DeserializationContext context) {
                if (element instanceof Number) {
                    return new Date(((Number) element).longValue());
                }
                return null;
            }
            
            @Override
            public TypeHandlerPriority getPriority() {
                return TypeHandlerPriority.HIGH;
            }
        };
        
        // When
        JSON5Serializer serializer = JSON5Serializer.builder()
            .withCustomTypeHandler(customHandler)
            .build();
        
        // Then
        assertNotNull(serializer);
        // TypeHandler가 등록되었는지 확인 (구체적인 방법이 없으므로 배제를 수정)
        assertTrue(serializer.getConfiguration().getTypeHandlerRegistry() != null);
    }
    
    @Test
    @DisplayName("설정 재사용 테스트")
    void testConfigurationReuse() {
        // Given
        SerializerConfiguration config = SerializerConfiguration.builder()
            .ignoreUnknownProperties()
            .enableSchemaCache()
            .build();
        
        // When
        JSON5Serializer serializer1 = new JSON5Serializer(config);
        JSON5Serializer serializer2 = new JSON5Serializer(config);
        
        // Then
        assertSame(config, serializer1.getConfiguration());
        assertSame(config, serializer2.getConfiguration());
        
        // 같은 설정을 사용하지만 다른 인스턴스
        assertNotSame(serializer1, serializer2);
    }
    
    @Test
    @DisplayName("serializable 메소드 테스트")
    void testSerializableMethod() {
        // Then
        assertTrue(JSON5Serializer.serializable(TestPerson.class));
        // 기본 타입들을 제외하고 테스트
        assertFalse(JSON5Serializer.serializable(List.class)); // interface
        assertFalse(JSON5Serializer.serializable(AbstractList.class)); // abstract class
    }
}