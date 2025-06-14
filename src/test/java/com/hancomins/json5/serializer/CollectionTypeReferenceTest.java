package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Collection TypeReference 완전한 제네릭 타입 지원 테스트")
class CollectionTypeReferenceTest {
    
    @Test
    @DisplayName("Collection TypeReference 기본 기능")
    void testCollectionTypeReferenceBasics() {
        // Given
        JSON5TypeReference<List<String>> typeRef = 
            new JSON5TypeReference<List<String>>() {};
        
        // When & Then
        assertFalse(typeRef.isMapType());
        assertTrue(typeRef.isCollectionType());
        
        CollectionTypeInfo collectionInfo = typeRef.analyzeCollectionType();
        assertEquals(List.class, collectionInfo.getCollectionClass());
        assertEquals(String.class, collectionInfo.getElementClass());
    }
    
    @Test
    @DisplayName("List<String> 완전한 타입 지원")
    void testListStringFullType() {
        // Given
        List<String> original = Arrays.asList("hello", "world", "test");
        
        // When - 직렬화
        CollectionSerializer serializer = new CollectionSerializer();
        JSON5Array serialized = serializer.serializeWithTypeReference(original,
            new JSON5TypeReference<List<String>>() {});
        
        // When - 역직렬화
        CollectionDeserializer deserializer = new CollectionDeserializer();
        List<String> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<List<String>>() {});
        
        // Then
        assertEquals(3, deserialized.size());
        assertEquals("hello", deserialized.get(0));
        assertEquals("world", deserialized.get(1));
        assertEquals("test", deserialized.get(2));
    }
    
    @Test
    @DisplayName("Set<Integer> 완전한 타입 지원")
    void testSetIntegerFullType() {
        // Given
        Set<Integer> original = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        
        // When - 직렬화
        CollectionSerializer serializer = new CollectionSerializer();
        JSON5Array serialized = serializer.serializeWithTypeReference(original,
            new JSON5TypeReference<Set<Integer>>() {});
        
        // When - 역직렬화
        CollectionDeserializer deserializer = new CollectionDeserializer();
        Set<Integer> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Set<Integer>>() {});
        
        // Then
        assertEquals(5, deserialized.size());
        assertTrue(deserialized.contains(1));
        assertTrue(deserialized.contains(2));
        assertTrue(deserialized.contains(3));
        assertTrue(deserialized.contains(4));
        assertTrue(deserialized.contains(5));
    }
    
    @Test
    @DisplayName("List<Map<String, Integer>> 복잡한 중첩 타입")
    void testListOfMaps() {
        // Given
        List<Map<String, Integer>> original = new ArrayList<>();
        
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("a", 1);
        map1.put("b", 2);
        
        Map<String, Integer> map2 = new HashMap<>();
        map2.put("x", 10);
        map2.put("y", 20);
        
        original.add(map1);
        original.add(map2);
        
        // When - 직렬화
        CollectionSerializer serializer = new CollectionSerializer();
        JSON5Array serialized = serializer.serializeWithTypeReference(original,
            new JSON5TypeReference<List<Map<String, Integer>>>() {});
        
        // When - 역직렬화
        CollectionDeserializer deserializer = new CollectionDeserializer();
        List<Map<String, Integer>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<List<Map<String, Integer>>>() {});
        
        // Then
        assertEquals(2, deserialized.size());
        
        Map<String, Integer> deserializedMap1 = deserialized.get(0);
        Map<String, Integer> deserializedMap2 = deserialized.get(1);
        
        assertEquals(2, deserializedMap1.size());
        assertEquals(2, deserializedMap2.size());
        
        assertTrue(deserializedMap1.containsKey("a"));
        assertTrue(deserializedMap1.containsKey("b"));
        assertEquals(Integer.valueOf(1), deserializedMap1.get("a"));
        assertEquals(Integer.valueOf(2), deserializedMap1.get("b"));
    }
    
    @Test
    @DisplayName("JSON5Serializer 통합 API 테스트")
    void testJSON5SerializerIntegratedAPI() {
        // Given
        List<String> original = Arrays.asList("integration", "test", "success");
        
        // When - 통합 API 사용
        JSON5Array serialized = (JSON5Array) JSON5Serializer.toJSON5WithTypeReference(original,
            new JSON5TypeReference<List<String>>() {});
        
        List<String> deserialized = JSON5Serializer.fromJSON5ArrayWithTypeReference(serialized,
            new JSON5TypeReference<List<String>>() {});
        
        // Then
        assertEquals(original, deserialized);
    }
    
    @Test
    @DisplayName("문자열 파싱 통합 테스트")
    void testStringParsingIntegration() {
        // Given
        String json5String = "[\"hello\", \"world\", \"json5\"]";
        
        // When
        List<String> result = JSON5Serializer.parseWithTypeReference(json5String,
            new JSON5TypeReference<List<String>>() {});
        
        // Then
        assertEquals(3, result.size());
        assertEquals("hello", result.get(0));
        assertEquals("world", result.get(1));
        assertEquals("json5", result.get(2));
    }
}
