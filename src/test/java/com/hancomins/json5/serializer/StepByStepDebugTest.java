package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("단계별 디버깅 테스트")
class StepByStepDebugTest {
    
    public enum UserRole { ADMIN, USER, GUEST }
    
    @JSON5Type
    static class User {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private int age;
        
        public User() {}
        
        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + "}";
        }
    }
    
    @Test
    @DisplayName("1단계: 기본 Map<String, User> 테스트")
    void step1_basicMapTest() {
        System.out.println("=== 1단계: 기본 Map<String, User> 테스트 ===");
        
        // Given
        Map<String, User> userMap = new HashMap<>();
        userMap.put("leader", new User("Alice", 35));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeMap(userMap, User.class);
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, User> deserialized = deserializer.deserialize(serialized, User.class);
        
        // Then
        System.out.println("역직렬화 결과: " + deserialized);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        User leader = deserialized.get("leader");
        assertNotNull(leader);
        assertEquals("Alice", leader.getName());
        
        System.out.println("✅ 1단계 성공!");
    }
    
    @Test
    @DisplayName("2단계: Enum Key 테스트 - Map<UserRole, User>")
    void step2_enumKeyTest() {
        System.out.println("=== 2단계: Enum Key 테스트 ===");
        
        // Given
        Map<UserRole, User> enumMap = new HashMap<>();
        enumMap.put(UserRole.ADMIN, new User("Alice", 35));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(enumMap,
            new JSON5TypeReference<Map<UserRole, User>>() {});
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, User> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<UserRole, User>>() {});
        
        // Then
        System.out.println("역직렬화 결과: " + deserialized);
        System.out.println("역직렬화 Map 키들: " + deserialized.keySet());
        
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        assertTrue(deserialized.containsKey(UserRole.ADMIN));
        
        User admin = deserialized.get(UserRole.ADMIN);
        assertNotNull(admin);
        assertEquals("Alice", admin.getName());
        
        System.out.println("✅ 2단계 성공!");
    }
    
    @Test
    @DisplayName("3단계: Collection 값 테스트 - Map<String, List<String>>")
    void step3_collectionValueTest() {
        System.out.println("=== 3단계: Collection 값 테스트 ===");
        
        // Given
        Map<String, List<String>> collectionMap = new HashMap<>();
        collectionMap.put("fruits", Arrays.asList("apple", "banana"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(collectionMap,
            new JSON5TypeReference<Map<String, List<String>>>() {});
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<String, List<String>>>() {});
        
        // Then
        System.out.println("역직렬화 결과: " + deserialized);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        
        List<String> fruits = deserialized.get("fruits");
        System.out.println("fruits 리스트: " + fruits);
        assertNotNull(fruits);
        assertEquals(2, fruits.size());
        assertEquals("apple", fruits.get(0));
        
        System.out.println("✅ 3단계 성공!");
    }
    
    @Test
    @DisplayName("4단계: Enum Key + Collection Value - Map<UserRole, List<String>>")
    void step4_enumKeyCollectionValueTest() {
        System.out.println("=== 4단계: Enum Key + Collection Value 테스트 ===");
        
        // Given
        Map<UserRole, List<String>> complexMap = new HashMap<>();
        complexMap.put(UserRole.ADMIN, Arrays.asList("read", "write", "delete"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(complexMap,
            new JSON5TypeReference<Map<UserRole, List<String>>>() {});
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, List<String>> deserialized = null;
        
        try {
            deserialized = deserializer.deserializeWithTypeReference(serialized,
                new JSON5TypeReference<Map<UserRole, List<String>>>() {});
            System.out.println("🎉 역직렬화 성공!");
        } catch (Exception e) {
            System.err.println("❌ 역직렬화 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        // Then
        System.out.println("역직렬화 결과: " + deserialized);
        System.out.println("역직렬화 Map 크기: " + (deserialized != null ? deserialized.size() : "null"));
        
        if (deserialized != null && !deserialized.isEmpty()) {
            System.out.println("역직렬화 Map 키들: " + deserialized.keySet());
            
            for (Map.Entry<UserRole, List<String>> entry : deserialized.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                System.out.println("Value type: " + (entry.getValue() != null ? entry.getValue().getClass() : "null"));
            }
        }
        
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        assertTrue(deserialized.containsKey(UserRole.ADMIN));
        
        List<String> permissions = deserialized.get(UserRole.ADMIN);
        System.out.println("ADMIN permissions: " + permissions);
        System.out.println("ADMIN permissions type: " + (permissions != null ? permissions.getClass() : "null"));
        
        assertNotNull(permissions, "ADMIN permissions가 null입니다");
        assertEquals(3, permissions.size(), "ADMIN permissions 크기가 일치하지 않습니다");
        assertEquals("read", permissions.get(0), "첫 번째 permission이 일치하지 않습니다");
        
        System.out.println("✅ 4단계 성공!");
    }
    
    @Test
    @DisplayName("5단계: Map<String, Map<String, User>> 중첩 Map 테스트")
    void step5_nestedMapTest() {
        System.out.println("=== 5단계: 중첩 Map 테스트 ===");
        
        // Given
        Map<String, Map<String, User>> nestedMap = new HashMap<>();
        Map<String, User> team1 = new HashMap<>();
        team1.put("leader", new User("Alice", 35));
        nestedMap.put("team1", team1);
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(nestedMap,
            new JSON5TypeReference<Map<String, Map<String, User>>>() {});
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, Map<String, User>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<String, Map<String, User>>>() {});
        
        // Then
        System.out.println("역직렬화 결과: " + deserialized);
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        
        Map<String, User> deserializedTeam1 = deserialized.get("team1");
        System.out.println("team1: " + deserializedTeam1);
        System.out.println("team1 type: " + (deserializedTeam1 != null ? deserializedTeam1.getClass() : "null"));
        
        assertNotNull(deserializedTeam1);
        User leader = deserializedTeam1.get("leader");
        System.out.println("leader: " + leader);
        System.out.println("leader type: " + (leader != null ? leader.getClass() : "null"));
        
        assertNotNull(leader);
        assertEquals("Alice", leader.getName());
        
        System.out.println("✅ 5단계 성공!");
    }
}
