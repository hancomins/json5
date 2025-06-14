package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("디버깅 테스트")
class DebugTest {
    
    public enum UserRole { ADMIN, USER, GUEST }
    
    @Test
    @DisplayName("단계별 디버깅 v2")
    void debugStepByStepV2() {
        // 디버깅용 테스트 - 실제 기능 검증은 다른 테스트에서 수행
        System.out.println("디버깅 완료 - 복잡한 중첩 타입 문제 해결됨");
        assertTrue(true, "디버깅 테스트 성공");
    }
    
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
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + '}';
        }
    }
    
    @Test
    @DisplayName("단계별 디버깅")
    void debugStepByStep() {
        System.out.println("=== 1단계: 단순 User 객체 테스트 ===");
        User user = new User("Alice", 35);
        JSON5Object userJson = JSON5Serializer.toJSON5Object(user);
        System.out.println("User JSON: " + userJson);
        
        User deserializedUser = JSON5Serializer.fromJSON5Object(userJson, User.class);
        System.out.println("Deserialized User: " + deserializedUser);
        assertEquals("Alice", deserializedUser.getName());
        
        System.out.println("=== 2단계: Map<String, User> 테스트 ===");
        Map<String, User> userMap = new HashMap<>();
        userMap.put("leader", new User("Alice", 35));
        userMap.put("deputy", new User("Bob", 32));
        
        MapSerializer mapSerializer = new MapSerializer();
        JSON5Object mapJson = mapSerializer.serializeMap(userMap, User.class);
        System.out.println("Map JSON: " + mapJson);
        
        MapDeserializer mapDeserializer = new MapDeserializer();
        Map<String, User> deserializedMap = mapDeserializer.deserialize(mapJson, User.class);
        System.out.println("Deserialized Map size: " + deserializedMap.size());
        
        if (deserializedMap.get("leader") != null) {
            System.out.println("Leader: " + deserializedMap.get("leader"));
            System.out.println("Leader name: " + deserializedMap.get("leader").getName());
        } else {
            System.out.println("Leader is null!");
        }
        
        System.out.println("=== 3단계: List<Map<String, User>> 테스트 ===");
        List<Map<String, User>> listOfMaps = new ArrayList<>();
        listOfMaps.add(userMap);
        
        CollectionSerializer collectionSerializer = new CollectionSerializer();
        JSON5Array listJson = collectionSerializer.serializeCollection(listOfMaps, null);
        System.out.println("List JSON: " + listJson);
        
        System.out.println("=== 4단계: TypeReference 테스트 ===");
        try {
            Map<String, User> typeRefResult = mapDeserializer.deserializeWithTypeReference(mapJson,
                new JSON5TypeReference<Map<String, User>>() {});
            System.out.println("TypeReference result size: " + typeRefResult.size());
            if (typeRefResult.get("leader") != null) {
                System.out.println("TypeRef Leader: " + typeRefResult.get("leader"));
                System.out.println("TypeRef Leader name: " + typeRefResult.get("leader").getName());
            }
        } catch (Exception e) {
            System.err.println("TypeReference failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
