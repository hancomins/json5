package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5TypeReference 완전한 제네릭 타입 지원 테스트")
class JSON5TypeReferenceTest {
    
    public enum UserRole { ADMIN, USER, GUEST }
    
    @Test
    @DisplayName("TypeReference 기본 기능 테스트")
    void testTypeReferenceBasics() {
        // Given
        JSON5TypeReference<Map<String, List<String>>> typeRef = 
            new JSON5TypeReference<Map<String, List<String>>>() {};
        
        // When & Then
        assertTrue(typeRef.isMapType());
        assertFalse(typeRef.isCollectionType());
        
        MapTypeInfo mapInfo = typeRef.analyzeMapType();
        assertEquals(String.class, mapInfo.getKeyClass());
        assertEquals(List.class, mapInfo.getValueClass());
        assertTrue(mapInfo.isValueCollection());
        assertEquals(String.class, mapInfo.getValueElementType());
    }
    
    @Test
    @DisplayName("Enum Key + Collection Value 완전한 타입 지원")
    void testEnumKeyWithCollectionValueFullType() {
        // Given
        Map<UserRole, List<String>> original = new HashMap<>();
        original.put(UserRole.ADMIN, Arrays.asList("all", "read", "write"));
        original.put(UserRole.USER, Arrays.asList("read"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(original,
            new JSON5TypeReference<Map<UserRole, List<String>>>() {});
        
        // When - 역직렬화 (완전한 타입 정보 보존)
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<UserRole, List<String>>>() {});
        
        // Then - 완전한 타입 정보 보존 확인
        assertEquals(2, deserialized.size());
        
        List<String> adminList = deserialized.get(UserRole.ADMIN);
        assertNotNull(adminList);
        assertEquals(3, adminList.size());
        assertEquals("all", adminList.get(0));    // ← 이제 null이 아님!
        assertEquals("read", adminList.get(1));
        assertEquals("write", adminList.get(2));
        
        List<String> userList = deserialized.get(UserRole.USER);
        assertNotNull(userList);
        assertEquals(1, userList.size());
        assertEquals("read", userList.get(0));
    }
    
    @Test
    @DisplayName("다양한 Collection 타입 지원")
    void testVariousCollectionTypes() {
        // Map<Integer, Set<String>> 테스트
        Map<Integer, Set<String>> setMap = new HashMap<>();
        setMap.put(1, new HashSet<>(Arrays.asList("a", "b", "c")));
        setMap.put(2, new HashSet<>(Arrays.asList("x", "y")));
        
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(setMap,
            new JSON5TypeReference<Map<Integer, Set<String>>>() {});
        
        MapDeserializer deserializer = new MapDeserializer();
        Map<Integer, Set<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<Integer, Set<String>>>() {});
        
        assertEquals(2, deserialized.size());
        assertEquals(3, deserialized.get(1).size());
        assertEquals(2, deserialized.get(2).size());
        assertTrue(deserialized.get(1).contains("a"));
        assertTrue(deserialized.get(2).contains("x"));
    }
    
    @Test
    @DisplayName("복합 타입 조합 테스트")
    void testComplexTypeCombinations() {
        // Map<String, List<Integer>> 테스트
        Map<String, List<Integer>> complexMap = new HashMap<>();
        complexMap.put("numbers", Arrays.asList(1, 2, 3, 4, 5));
        complexMap.put("primes", Arrays.asList(2, 3, 5, 7));
        
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(complexMap,
            new JSON5TypeReference<Map<String, List<Integer>>>() {});
        
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, List<Integer>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<String, List<Integer>>>() {});
        
        assertEquals(2, deserialized.size());
        assertEquals(5, deserialized.get("numbers").size());
        assertEquals(Integer.valueOf(1), deserialized.get("numbers").get(0));
        assertEquals(Integer.valueOf(2), deserialized.get("primes").get(0));
    }
    
    @Test
    @DisplayName("타입 불일치 시 적절한 예외 발생")
    void testTypeValidation() {
        JSON5Object json = new JSON5Object();
        json.put("key", "value");
        
        MapDeserializer deserializer = new MapDeserializer();
        
        // Collection이 아닌 TypeReference로 Collection을 역직렬화하려고 시도
        assertThrows(JSON5SerializerException.class, () -> {
            deserializer.deserializeWithTypeReference(json,
                new JSON5TypeReference<List<String>>() {});  // Map이 아닌 List TypeReference
        });
    }
    
    @Test
    @DisplayName("MapTypeInfo 분석 기능 테스트")
    void testMapTypeInfoAnalysis() {
        // Given
        JSON5TypeReference<Map<UserRole, List<String>>> typeRef = 
            new JSON5TypeReference<Map<UserRole, List<String>>>() {};
        
        // When
        MapTypeInfo typeInfo = typeRef.analyzeMapType();
        
        // Then
        assertEquals(UserRole.class, typeInfo.getKeyClass());
        assertEquals(List.class, typeInfo.getValueClass());
        assertTrue(typeInfo.isValueCollection());
        assertEquals(String.class, typeInfo.getValueElementType());
        assertFalse(typeInfo.isValueNestedMap());
    }
    
    @Test
    @DisplayName("CollectionTypeInfo 분석 기능 테스트")
    void testCollectionTypeInfoAnalysis() {
        // Given
        JSON5TypeReference<List<String>> typeRef = 
            new JSON5TypeReference<List<String>>() {};
        
        // When
        CollectionTypeInfo collectionInfo = typeRef.analyzeCollectionType();
        
        // Then
        assertEquals(List.class, collectionInfo.getCollectionClass());
        assertEquals(String.class, collectionInfo.getElementClass());
        assertEquals(String.class, collectionInfo.getElementType());
    }
    
    @Test
    @DisplayName("중첩 Map 타입 감지 테스트")
    void testNestedMapTypeDetection() {
        // Given
        JSON5TypeReference<Map<String, Map<String, Integer>>> typeRef = 
            new JSON5TypeReference<Map<String, Map<String, Integer>>>() {};
        
        // When
        MapTypeInfo typeInfo = typeRef.analyzeMapType();
        
        // Then
        assertEquals(String.class, typeInfo.getKeyClass());
        assertEquals(Map.class, typeInfo.getValueClass());
        assertFalse(typeInfo.isValueCollection());
        assertTrue(typeInfo.isValueNestedMap());
    }
}
