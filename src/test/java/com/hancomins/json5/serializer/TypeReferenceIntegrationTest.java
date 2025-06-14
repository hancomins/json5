package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeReference 전체 통합 테스트")
class TypeReferenceIntegrationTest {
    
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
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof User)) return false;
            User user = (User) o;
            return age == user.age && Objects.equals(name, user.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
    
    @Test
    @DisplayName("최종 통합: 모든 Phase 기능 조합 테스트")
    void testAllPhasesIntegration() {
        // Given - Phase 1: Map 값으로 Collection
        //        Phase 2: 다양한 Key 타입 (Enum)
        //        Phase 3: TypeReference로 완전한 제네릭 타입 지원
        //        Phase 4: Collection에도 TypeReference 적용
        
        Map<UserRole, List<User>> complexData = new HashMap<>();
        
        List<User> adminUsers = Arrays.asList(
            new User("Alice", 30),
            new User("Bob", 25)
        );
        
        List<User> regularUsers = Arrays.asList(
            new User("Charlie", 22),
            new User("Diana", 28),
            new User("Eve", 24)
        );
        
        complexData.put(UserRole.ADMIN, adminUsers);
        complexData.put(UserRole.USER, regularUsers);
        
        // When - 완전한 TypeReference 지원으로 직렬화/역직렬화
        MapSerializer mapSerializer = new MapSerializer();
        JSON5Object serialized = mapSerializer.serializeWithTypeReference(complexData,
            new JSON5TypeReference<Map<UserRole, List<User>>>() {});
        
        MapDeserializer mapDeserializer = new MapDeserializer();
        Map<UserRole, List<User>> deserialized = mapDeserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<UserRole, List<User>>>() {});
        
        // Then - 모든 데이터와 타입 정보가 완벽하게 보존되어야 함
        assertEquals(2, deserialized.size());
        
        List<User> deserializedAdmins = deserialized.get(UserRole.ADMIN);
        List<User> deserializedUsers = deserialized.get(UserRole.USER);
        
        assertNotNull(deserializedAdmins);
        assertNotNull(deserializedUsers);
        
        assertEquals(2, deserializedAdmins.size());
        assertEquals(3, deserializedUsers.size());
        
        // 커스텀 객체의 완전한 복원 확인
        assertEquals("Alice", deserializedAdmins.get(0).getName());
        assertEquals(30, deserializedAdmins.get(0).getAge());
        assertEquals("Bob", deserializedAdmins.get(1).getName());
        assertEquals(25, deserializedAdmins.get(1).getAge());
        
        assertEquals("Charlie", deserializedUsers.get(0).getName());
        assertEquals(22, deserializedUsers.get(0).getAge());
    }
    
    @Test
    @DisplayName("성능 테스트: 대용량 데이터 처리")
    void testPerformanceWithLargeData() {
        // Given - 대용량 데이터 생성
        Map<Integer, List<String>> largeData = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            List<String> values = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                values.add("item_" + i + "_" + j);
            }
            largeData.put(i, values);
        }
        
        // When - 성능 측정
        long startTime = System.currentTimeMillis();
        
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(largeData,
            new JSON5TypeReference<Map<Integer, List<String>>>() {});
        
        MapDeserializer deserializer = new MapDeserializer();
        Map<Integer, List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<Integer, List<String>>>() {});
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Then - 결과 검증 및 성능 확인
        assertEquals(1000, deserialized.size());
        assertEquals(100, deserialized.get(0).size());
        assertEquals("item_0_0", deserialized.get(0).get(0));
        
        System.out.println("대용량 데이터 처리 시간: " + duration + "ms");
        assertTrue(duration < 5000, "처리 시간이 5초를 초과했습니다: " + duration + "ms");
    }
    
    @Test
    @DisplayName("에러 시나리오: 잘못된 TypeReference 사용")
    void testErrorScenarios() {
        JSON5Object jsonObject = new JSON5Object();
        jsonObject.put("key", "value");
        
        // Collection TypeReference로 Map 데이터 역직렬화 시도
        assertThrows(JSON5SerializerException.class, () -> {
            JSON5Serializer.fromJSON5ObjectWithTypeReference(jsonObject,
                new JSON5TypeReference<List<String>>() {});
        });
        
        JSON5Array jsonArray = new JSON5Array();
        jsonArray.add("test");
        
        // Map TypeReference로 Collection 데이터 역직렬화 시도
        assertThrows(JSON5SerializerException.class, () -> {
            JSON5Serializer.fromJSON5ArrayWithTypeReference(jsonArray,
                new JSON5TypeReference<Map<String, String>>() {});
        });
    }
    
    @Test
    @DisplayName("중첩 Collection 테스트")
    void testNestedCollections() {
        // Given - List<List<String>> 구조
        List<List<String>> nestedList = new ArrayList<>();
        nestedList.add(Arrays.asList("a1", "a2", "a3"));
        nestedList.add(Arrays.asList("b1", "b2"));
        nestedList.add(Arrays.asList("c1", "c2", "c3", "c4"));
        
        // When - 직렬화/역직렬화
        CollectionSerializer serializer = new CollectionSerializer();
        JSON5Array serialized = serializer.serializeWithTypeReference(nestedList,
            new JSON5TypeReference<List<List<String>>>() {});
        
        CollectionDeserializer deserializer = new CollectionDeserializer();
        List<List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<List<List<String>>>() {});
        
        // Then
        assertEquals(3, deserialized.size());
        assertEquals(3, deserialized.get(0).size());
        assertEquals(2, deserialized.get(1).size());
        assertEquals(4, deserialized.get(2).size());
        
        assertEquals("a1", deserialized.get(0).get(0));
        assertEquals("b2", deserialized.get(1).get(1));
        assertEquals("c4", deserialized.get(2).get(3));
    }
    
    @Test
    @DisplayName("Collection과 Map 혼합 타입 테스트")
    void testMixedCollectionAndMapTypes() {
        // Given - List<Map<String, Integer>>와 Map<String, List<Integer>> 조합
        List<Map<String, Integer>> listOfMaps = new ArrayList<>();
        Map<String, Integer> map1 = new HashMap<>();
        map1.put("score1", 95);
        map1.put("score2", 87);
        listOfMaps.add(map1);
        
        Map<String, Integer> map2 = new HashMap<>();
        map2.put("score1", 78);
        map2.put("score2", 92);
        listOfMaps.add(map2);
        
        // When - List<Map<String, Integer>> 테스트
        CollectionSerializer collectionSerializer = new CollectionSerializer();
        JSON5Array serializedCollection = collectionSerializer.serializeWithTypeReference(listOfMaps,
            new JSON5TypeReference<List<Map<String, Integer>>>() {});
        
        CollectionDeserializer collectionDeserializer = new CollectionDeserializer();
        List<Map<String, Integer>> deserializedCollection = collectionDeserializer.deserializeWithTypeReference(
            serializedCollection, new JSON5TypeReference<List<Map<String, Integer>>>() {});
        
        // Then
        assertEquals(2, deserializedCollection.size());
        assertEquals(Integer.valueOf(95), deserializedCollection.get(0).get("score1"));
        assertEquals(Integer.valueOf(92), deserializedCollection.get(1).get("score2"));
        
        // Given - Map<String, List<Integer>>
        Map<String, List<Integer>> mapOfLists = new HashMap<>();
        mapOfLists.put("group1", Arrays.asList(1, 2, 3));
        mapOfLists.put("group2", Arrays.asList(4, 5, 6, 7));
        
        // When - Map<String, List<Integer>> 테스트
        MapSerializer mapSerializer = new MapSerializer();
        JSON5Object serializedMap = mapSerializer.serializeWithTypeReference(mapOfLists,
            new JSON5TypeReference<Map<String, List<Integer>>>() {});
        
        MapDeserializer mapDeserializer = new MapDeserializer();
        Map<String, List<Integer>> deserializedMap = mapDeserializer.deserializeWithTypeReference(
            serializedMap, new JSON5TypeReference<Map<String, List<Integer>>>() {});
        
        // Then
        assertEquals(2, deserializedMap.size());
        assertEquals(3, deserializedMap.get("group1").size());
        assertEquals(4, deserializedMap.get("group2").size());
        assertEquals(Integer.valueOf(1), deserializedMap.get("group1").get(0));
        assertEquals(Integer.valueOf(7), deserializedMap.get("group2").get(3));
    }
}
