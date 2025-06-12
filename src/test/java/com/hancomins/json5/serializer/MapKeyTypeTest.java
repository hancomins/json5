package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Map 다양한 Key 타입 지원 테스트")
class MapKeyTypeTest {
    
    public enum UserRole { ADMIN, USER, GUEST }
    
    @Test
    @DisplayName("MapKeyConverter - Key 타입 지원 여부 확인")
    void testIsSupportedKeyType() {
        // String - 지원
        assertTrue(MapKeyConverter.isSupportedKeyType(String.class));
        
        // Enum - 지원
        assertTrue(MapKeyConverter.isSupportedKeyType(UserRole.class));
        
        // Primitive/Wrapper - 지원
        assertTrue(MapKeyConverter.isSupportedKeyType(Integer.class));
        assertTrue(MapKeyConverter.isSupportedKeyType(int.class));
        assertTrue(MapKeyConverter.isSupportedKeyType(Long.class));
        assertTrue(MapKeyConverter.isSupportedKeyType(Boolean.class));
        
        // 지원하지 않는 타입
        assertFalse(MapKeyConverter.isSupportedKeyType(Object.class));
        assertFalse(MapKeyConverter.isSupportedKeyType(List.class));
    }
    
    @Test
    @DisplayName("MapKeyConverter - String Key 변환 테스트")
    void testConvertKeyToString() {
        // String
        assertEquals("test", MapKeyConverter.convertKeyToString("test"));
        
        // Enum
        assertEquals("ADMIN", MapKeyConverter.convertKeyToString(UserRole.ADMIN));
        
        // Integer
        assertEquals("123", MapKeyConverter.convertKeyToString(123));
        
        // Long
        assertEquals("456", MapKeyConverter.convertKeyToString(456L));
        
        // Boolean
        assertEquals("true", MapKeyConverter.convertKeyToString(true));
        
        // null
        assertNull(MapKeyConverter.convertKeyToString(null));
    }
    
    @Test
    @DisplayName("MapKeyConverter - String을 Key로 변환 테스트")
    void testConvertStringToKey() {
        // String
        assertEquals("test", MapKeyConverter.convertStringToKey("test", String.class));
        
        // Enum
        assertEquals(UserRole.ADMIN, MapKeyConverter.convertStringToKey("ADMIN", UserRole.class));
        assertEquals(UserRole.USER, MapKeyConverter.convertStringToKey("USER", UserRole.class));
        
        // Integer
        assertEquals(Integer.valueOf(123), MapKeyConverter.convertStringToKey("123", Integer.class));
        
        // Long
        assertEquals(Long.valueOf(456), MapKeyConverter.convertStringToKey("456", Long.class));
        
        // Boolean
        assertEquals(Boolean.TRUE, MapKeyConverter.convertStringToKey("true", Boolean.class));
        
        // null
        assertNull(MapKeyConverter.convertStringToKey(null, String.class));
    }
    
    @Test
    @DisplayName("Enum Key Map 직렬화/역직렬화")
    void testEnumKeyMap() {
        // Given
        Map<UserRole, List<String>> permissions = new HashMap<>();
        permissions.put(UserRole.ADMIN, Arrays.asList("all"));
        permissions.put(UserRole.USER, Arrays.asList("read"));
        
        // When - 직렬화 (Phase 3 TypeReference 사용)
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(permissions,
            new JSON5TypeReference<Map<UserRole, List<String>>>() {});
        
        // Then - Key가 String으로 변환되어야 함
        assertTrue(serialized.has("ADMIN"));
        assertTrue(serialized.has("USER"));
        assertTrue(serialized.get("ADMIN") instanceof JSON5Array);
        assertTrue(serialized.get("USER") instanceof JSON5Array);
        
        // When - 역직렬화 (Phase 3 TypeReference 사용)
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<UserRole, List<String>>>() {});
        
        // Then
        assertEquals(2, deserialized.size());
        assertTrue(deserialized.containsKey(UserRole.ADMIN));
        assertTrue(deserialized.containsKey(UserRole.USER));
        assertEquals(1, deserialized.get(UserRole.ADMIN).size());
        assertEquals("all", deserialized.get(UserRole.ADMIN).get(0));
        assertEquals(1, deserialized.get(UserRole.USER).size());
        assertEquals("read", deserialized.get(UserRole.USER).get(0));
    }
    
    @Test
    @DisplayName("Integer Key Map 직렬화/역직렬화")
    void testIntegerKeyMap() {
        // Given
        Map<Integer, String> userNames = new HashMap<>();
        userNames.put(1, "Alice");
        userNames.put(2, "Bob");
        userNames.put(100, "Charlie");
        
        // When - 직렬화 (Phase 3 TypeReference 사용)
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(userNames,
            new JSON5TypeReference<Map<Integer, String>>() {});
        
        // Then - Key가 String으로 변환되어야 함
        assertTrue(serialized.has("1"));
        assertTrue(serialized.has("2"));
        assertTrue(serialized.has("100"));
        assertEquals("Alice", serialized.getString("1"));
        assertEquals("Bob", serialized.getString("2"));
        assertEquals("Charlie", serialized.getString("100"));
        
        // When - 역직렬화 (Phase 3 TypeReference 사용)
        MapDeserializer deserializer = new MapDeserializer();
        Map<Integer, String> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<Integer, String>>() {});
        
        // Then
        assertEquals(3, deserialized.size());
        assertEquals("Alice", deserialized.get(1));
        assertEquals("Bob", deserialized.get(2));
        assertEquals("Charlie", deserialized.get(100));
    }
    
    @Test
    @DisplayName("Long Key Map 직렬화/역직렬화")
    void testLongKeyMap() {
        // Given
        Map<Long, Boolean> statusMap = new HashMap<>();
        statusMap.put(1000L, true);
        statusMap.put(2000L, false);
        
        // When - 직렬화 (Phase 3 TypeReference 사용)
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(statusMap,
            new JSON5TypeReference<Map<Long, Boolean>>() {});
        
        // Then
        assertTrue(serialized.has("1000"));
        assertTrue(serialized.has("2000"));
        assertEquals(true, serialized.getBoolean("1000"));
        assertEquals(false, serialized.getBoolean("2000"));
        
        // When - 역직렬화 (Phase 3 TypeReference 사용)
        MapDeserializer deserializer = new MapDeserializer();
        Map<Long, Boolean> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<Long, Boolean>>() {});
        
        // Then
        assertEquals(2, deserialized.size());
        assertEquals(Boolean.TRUE, deserialized.get(1000L));
        assertEquals(Boolean.FALSE, deserialized.get(2000L));
    }
    
    @Test
    @DisplayName("지원하지 않는 Key 타입은 예외 발생")
    void testUnsupportedKeyType() {
        JSON5Object json = new JSON5Object();
        json.put("test", "value");
        
        MapDeserializer deserializer = new MapDeserializer();
        
        assertThrows(JSON5SerializerException.class, () -> {
            deserializer.deserializeWithKeyType(json, Object.class, String.class);
        });
        
        assertThrows(JSON5SerializerException.class, () -> {
            deserializer.deserializeWithKeyType(json, List.class, String.class);
        });
    }
}
