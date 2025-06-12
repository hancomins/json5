package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Map 고도화 통합 테스트")
class MapEnhancementIntegrationTest {

    public enum UserRole { ADMIN, USER, GUEST }
    
    @Test
    @DisplayName("Phase 1: Map 값으로 Collection 지원 테스트")
    void testMapWithCollectionValue() {
        // Given
        Map<String, List<String>> rolePermissions = new HashMap<>();
        rolePermissions.put("admin", Arrays.asList("read", "write", "delete"));
        rolePermissions.put("user", Arrays.asList("read"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMap(rolePermissions, List.class);
        
        // Then - 직렬화 검증
        assertTrue(serialized.has("admin"));
        assertTrue(serialized.has("user"));
        assertNotNull(serialized.get("admin"));
        assertNotNull(serialized.get("user"));
        
        // When - 역직렬화  
        MapDeserializer deserializer = new MapDeserializer();
        @SuppressWarnings("unchecked")
        Map<String, List> deserialized = (Map<String, List>) deserializer.deserialize(serialized, List.class);
        
        // Then - 역직렬화 검증
        assertEquals(2, deserialized.size());
        assertNotNull(deserialized.get("admin"));
        assertNotNull(deserialized.get("user"));
        assertEquals(3, deserialized.get("admin").size());
        assertEquals(1, deserialized.get("user").size());
        
        // List 내용 검증
        List adminList = deserialized.get("admin");
        assertTrue(adminList.contains("read"));
        assertTrue(adminList.contains("write"));
        assertTrue(adminList.contains("delete"));
        
        List userList = deserialized.get("user");
        assertTrue(userList.contains("read"));
    }
    
    @Test
    @DisplayName("Phase 2: 다양한 Key 타입 지원 테스트")
    void testVariousKeyTypes() {
        // Given - Enum Key Map
        Map<UserRole, String> enumKeyMap = new HashMap<>();
        enumKeyMap.put(UserRole.ADMIN, "Administrator");
        enumKeyMap.put(UserRole.USER, "Regular User");
        
        // When - Enum Key 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object enumSerialized = serializer.serializeMapWithGenericKey(enumKeyMap, String.class);
        
        // Then - Enum Key 직렬화 검증
        assertTrue(enumSerialized.has("ADMIN"));
        assertTrue(enumSerialized.has("USER"));
        assertEquals("Administrator", enumSerialized.getString("ADMIN"));
        assertEquals("Regular User", enumSerialized.getString("USER"));
        
        // When - Enum Key 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, String> enumDeserialized = deserializer.deserializeWithKeyType(
            enumSerialized, UserRole.class, String.class);
        
        // Then - Enum Key 역직렬화 검증
        assertEquals(2, enumDeserialized.size());
        assertEquals("Administrator", enumDeserialized.get(UserRole.ADMIN));
        assertEquals("Regular User", enumDeserialized.get(UserRole.USER));
        
        // Given - Integer Key Map
        Map<Integer, String> intKeyMap = new HashMap<>();
        intKeyMap.put(1, "First");
        intKeyMap.put(2, "Second");
        
        // When - Integer Key 직렬화/역직렬화
        JSON5Object intSerialized = serializer.serializeMapWithGenericKey(intKeyMap, String.class);
        Map<Integer, String> intDeserialized = deserializer.deserializeWithKeyType(
            intSerialized, Integer.class, String.class);
        
        // Then - Integer Key 검증
        assertEquals(2, intDeserialized.size());
        assertEquals("First", intDeserialized.get(1));
        assertEquals("Second", intDeserialized.get(2));
    }
    
    @Test
    @DisplayName("복합 시나리오: Enum Key + Collection Value")
    void testComplexScenario() {
        // Given
        Map<UserRole, List<String>> complexMap = new HashMap<>();
        complexMap.put(UserRole.ADMIN, Arrays.asList("all_permissions", "manage_users"));
        complexMap.put(UserRole.USER, Arrays.asList("read_only"));
        complexMap.put(UserRole.GUEST, Arrays.asList());
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMapWithGenericKey(complexMap, List.class);
        
        // Then - 직렬화 검증
        assertTrue(serialized.has("ADMIN"));
        assertTrue(serialized.has("USER"));
        assertTrue(serialized.has("GUEST"));
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        @SuppressWarnings("unchecked")
        Map<UserRole, List> deserialized = (Map<UserRole, List>) deserializer.deserializeWithKeyType(
            serialized, UserRole.class, List.class);
        
        // Then - 역직렬화 검증
        assertEquals(3, deserialized.size());
        assertEquals(2, deserialized.get(UserRole.ADMIN).size());
        assertEquals(1, deserialized.get(UserRole.USER).size());
        assertEquals(0, deserialized.get(UserRole.GUEST).size());
        
        // List 내용 검증
        List adminList = deserialized.get(UserRole.ADMIN);
        assertTrue(adminList.contains("all_permissions"));
        assertTrue(adminList.contains("manage_users"));
        
        List userList = deserialized.get(UserRole.USER);
        assertTrue(userList.contains("read_only"));
    }
    
    @Test
    @DisplayName("하위 호환성: 기존 String Key Map 동작 보장")
    void testBackwardCompatibility() {
        // Given - 기존 String Key Map
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("key1", "value1");
        stringMap.put("key2", "value2");
        
        // When - 기존 API 사용
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMap(stringMap, String.class);
        
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, String> deserialized = deserializer.deserialize(serialized, String.class);
        
        // Then - 기존 기능 정상 동작
        assertEquals(2, deserialized.size());
        assertEquals("value1", deserialized.get("key1"));
        assertEquals("value2", deserialized.get("key2"));
        
        // When - 새로운 API로도 동일 결과
        JSON5Object newSerialized = serializer.serializeMapWithGenericKey(stringMap, String.class);
        Map<String, String> newDeserialized = deserializer.deserializeWithKeyType(
            newSerialized, String.class, String.class);
        
        // Then - 동일 결과 보장
        assertEquals(deserialized, newDeserialized);
    }
    
    @Test
    @DisplayName("MapKeyConverter 단독 테스트")
    void testMapKeyConverterStandalone() {
        // 다양한 타입의 Key 변환 테스트
        
        // Boolean Key
        String boolKey = MapKeyConverter.convertKeyToString(true);
        assertEquals("true", boolKey);
        Boolean convertedBool = MapKeyConverter.convertStringToKey("false", Boolean.class);
        assertEquals(Boolean.FALSE, convertedBool);
        
        // Character Key  
        String charKey = MapKeyConverter.convertKeyToString('A');
        assertEquals("A", charKey);
        Character convertedChar = MapKeyConverter.convertStringToKey("B", Character.class);
        assertEquals(Character.valueOf('B'), convertedChar);
        
        // Byte Key
        String byteKey = MapKeyConverter.convertKeyToString((byte) 127);
        assertEquals("127", byteKey);
        Byte convertedByte = MapKeyConverter.convertStringToKey("100", Byte.class);
        assertEquals(Byte.valueOf((byte) 100), convertedByte);
    }
}
