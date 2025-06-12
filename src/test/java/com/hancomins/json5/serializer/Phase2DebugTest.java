package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Phase 2 디버깅 테스트")
class Phase2DebugTest {
    
    public enum UserRole { 
        ADMIN, USER, GUEST 
    }
    
    @Test
    @DisplayName("디버깅: MapKeyConverter 단순 테스트")
    void testMapKeyConverterSimple() {
        // 기본 String Key 변환
        assertEquals("test", MapKeyConverter.convertKeyToString("test"));
        assertEquals("test", MapKeyConverter.convertStringToKey("test", String.class));
        
        // Enum Key 변환
        assertEquals("ADMIN", MapKeyConverter.convertKeyToString(UserRole.ADMIN));
        assertEquals(UserRole.ADMIN, MapKeyConverter.convertStringToKey("ADMIN", UserRole.class));
        
        System.out.println("MapKeyConverter 기본 변환 성공");
    }
    
    @Test
    @DisplayName("디버깅: Enum Key Map 직렬화만 테스트")
    void testEnumKeyMapSerializationOnly() {
        // Given
        Map<UserRole, String> enumMap = new HashMap<>();
        enumMap.put(UserRole.ADMIN, "Administrator");
        enumMap.put(UserRole.USER, "User");
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object result = serializer.serializeMapWithGenericKey(enumMap, String.class);
        
        // Then - 직렬화 결과 확인
        System.out.println("직렬화 결과: " + result.toString());
        assertTrue(result.has("ADMIN"));
        assertTrue(result.has("USER"));
        assertEquals("Administrator", result.getString("ADMIN"));
        assertEquals("User", result.getString("USER"));
        
        System.out.println("Enum Key Map 직렬화 성공");
    }
    
    @Test
    @DisplayName("디버깅: String Key + Collection Value 테스트")
    void testStringKeyWithCollectionValue() {
        // Given
        Map<String, List<String>> stringMap = new HashMap<>();
        stringMap.put("admin", Arrays.asList("read", "write"));
        stringMap.put("user", Arrays.asList("read"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMap(stringMap, List.class);
        
        // Then - 직렬화 결과 확인
        System.out.println("String+Collection 직렬화 결과: " + serialized.toString());
        assertTrue(serialized.has("admin"));
        assertTrue(serialized.has("user"));
        assertTrue(serialized.get("admin") instanceof JSON5Array);
        assertTrue(serialized.get("user") instanceof JSON5Array);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        @SuppressWarnings("unchecked")
        Map<String, List> deserialized = (Map<String, List>) deserializer.deserialize(serialized, List.class);
        
        // Then - 역직렬화 결과 확인
        System.out.println("String+Collection 역직렬화 결과 크기: " + deserialized.size());
        assertEquals(2, deserialized.size());
        
        List adminList = deserialized.get("admin");
        List userList = deserialized.get("user");
        
        System.out.println("admin list: " + (adminList != null ? adminList.toString() : "null"));
        System.out.println("user list: " + (userList != null ? userList.toString() : "null"));
        
        assertNotNull(adminList);
        assertNotNull(userList);
        assertEquals(2, adminList.size());
        assertEquals(1, userList.size());
        
        System.out.println("String Key + Collection Value 테스트 성공");
    }
    
    @Test
    @DisplayName("디버깅: Enum Key + String Value 테스트")  
    void testEnumKeyWithStringValue() {
        // Given
        Map<UserRole, String> enumMap = new HashMap<>();
        enumMap.put(UserRole.ADMIN, "Administrator");
        enumMap.put(UserRole.USER, "User");
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMapWithGenericKey(enumMap, String.class);
        System.out.println("Enum+String 직렬화 결과: " + serialized.toString());
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, String> deserialized = deserializer.deserializeWithKeyType(
            serialized, UserRole.class, String.class);
        
        // Then
        System.out.println("Enum+String 역직렬화 결과 크기: " + deserialized.size());
        assertEquals(2, deserialized.size());
        assertEquals("Administrator", deserialized.get(UserRole.ADMIN));
        assertEquals("User", deserialized.get(UserRole.USER));
        
        System.out.println("Enum Key + String Value 테스트 성공");
    }
}
