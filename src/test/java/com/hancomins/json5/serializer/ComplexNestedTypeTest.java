package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("복잡한 중첩 타입 테스트")
class ComplexNestedTypeTest {
    
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
        public String toString() {
            return "User{name='" + name + "', age=" + age + '}';
        }
        
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
    @DisplayName("Map<UserRole, List<Map<String,User>>> 복잡한 중첩 타입 테스트")
    void testComplexNestedType() {
        System.out.println("=== 복잡한 중첩 타입 테스트 시작 ===");
        
        // Given - 복잡한 중첩 구조 생성
        Map<UserRole, List<Map<String, User>>> complexData = new HashMap<>();
        
        // ADMIN 팀들
        List<Map<String, User>> adminTeams = new ArrayList<>();
        
        Map<String, User> team1 = new HashMap<>();
        team1.put("leader", new User("Alice", 35));
        team1.put("deputy", new User("Bob", 32));
        
        Map<String, User> team2 = new HashMap<>();
        team2.put("leader", new User("Charlie", 40));
        team2.put("deputy", new User("Diana", 28));
        
        adminTeams.add(team1);
        adminTeams.add(team2);
        
        // USER 팀들
        List<Map<String, User>> userTeams = new ArrayList<>();
        
        Map<String, User> team3 = new HashMap<>();
        team3.put("leader", new User("Eve", 30));
        team3.put("member", new User("Frank", 25));
        
        userTeams.add(team3);
        
        complexData.put(UserRole.ADMIN, adminTeams);
        complexData.put(UserRole.USER, userTeams);
        
        System.out.println("원본 데이터:");
        System.out.println("ADMIN 팀 수: " + complexData.get(UserRole.ADMIN).size());
        System.out.println("USER 팀 수: " + complexData.get(UserRole.USER).size());
        System.out.println("첫 번째 ADMIN 팀 리더: " + complexData.get(UserRole.ADMIN).get(0).get("leader"));
        
        // When - 직렬화
        System.out.println("\n=== 직렬화 시작 ===");
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = null;
        
        try {
            serialized = serializer.serializeWithTypeReference(complexData,
                new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
            System.out.println("직렬화 성공!");
            System.out.println("직렬화된 JSON: " + serialized.toString());
        } catch (Exception e) {
            System.err.println("직렬화 실패: " + e.getMessage());
            e.printStackTrace();
            fail("직렬화가 실패했습니다: " + e.getMessage());
        }
        
        assertNotNull(serialized, "직렬화 결과가 null입니다");
        
        // When - 역직렬화 (🎯 핵심 테스트 지점)
        System.out.println("\n=== 역직렬화 시작 (핵심 테스트) ===");
        MapDeserializer deserializer = new MapDeserializer();
        Map<UserRole, List<Map<String, User>>> deserialized = null;
        
        try {
            deserialized = deserializer.deserializeWithTypeReference(serialized,
                new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
            System.out.println("🎉 역직렬화 성공! (ClassCastException 해결됨)");
        } catch (ClassCastException e) {
            System.err.println("❌ ClassCastException 발생: " + e.getMessage());
            e.printStackTrace();
            fail("ClassCastException이 여전히 발생합니다: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ 기타 역직렬화 오류: " + e.getMessage());
            e.printStackTrace();
            fail("역직렬화가 실패했습니다: " + e.getMessage());
        }
        
        // Then - 검증
        System.out.println("\n=== 결과 검증 ===");
        assertNotNull(deserialized, "역직렬화 결과가 null입니다");
        System.out.println("역직렬화된 데이터: " + deserialized);
        System.out.println("역직렬화된 Map 크기: " + deserialized.size());
        System.out.println("역직렬화된 Map 키들: " + deserialized.keySet());
        
        assertEquals(2, deserialized.size(), "최상위 Map 크기가 일치하지 않습니다");
        
        // ADMIN 팀 검증
        List<Map<String, User>> deserializedAdminTeams = deserialized.get(UserRole.ADMIN);
        System.out.println("ADMIN 팀 리스트: " + deserializedAdminTeams);
        assertNotNull(deserializedAdminTeams, "ADMIN 팀 리스트가 null입니다");
        System.out.println("ADMIN 팀 리스트 크기: " + deserializedAdminTeams.size());
        assertEquals(2, deserializedAdminTeams.size(), "ADMIN 팀 수가 일치하지 않습니다");
        
        Map<String, User> deserializedTeam1 = deserializedAdminTeams.get(0);
        System.out.println("첫 번째 ADMIN 팀: " + deserializedTeam1);
        assertNotNull(deserializedTeam1, "첫 번째 ADMIN 팀이 null입니다");
        assertTrue(deserializedTeam1.containsKey("leader"), "leader 키가 없습니다");
        
        User deserializedLeader = deserializedTeam1.get("leader");
        assertNotNull(deserializedLeader, "팀 리더가 null입니다");
        
        // 🎯 핵심 검증: 실제로 User 객체이고 데이터가 올바른지 확인
        System.out.println("역직렬화된 리더: " + deserializedLeader);
        System.out.println("리더 이름: " + deserializedLeader.getName());
        System.out.println("리더 나이: " + deserializedLeader.getAge());
        
        assertEquals("Alice", deserializedLeader.getName(), "리더 이름이 일치하지 않습니다");
        assertEquals(35, deserializedLeader.getAge(), "리더 나이가 일치하지 않습니다");
        
        // USER 팀 검증
        List<Map<String, User>> deserializedUserTeams = deserialized.get(UserRole.USER);
        assertNotNull(deserializedUserTeams, "USER 팀 리스트가 null입니다");
        assertEquals(1, deserializedUserTeams.size(), "USER 팀 수가 일치하지 않습니다");
        
        Map<String, User> deserializedTeam3 = deserializedUserTeams.get(0);
        User deserializedEve = deserializedTeam3.get("leader");
        assertNotNull(deserializedEve, "Eve가 null입니다");
        assertEquals("Eve", deserializedEve.getName(), "Eve 이름이 일치하지 않습니다");
        assertEquals(30, deserializedEve.getAge(), "Eve 나이가 일치하지 않습니다");
        
        System.out.println("\n✅ 모든 검증 완료! 복잡한 중첩 타입이 성공적으로 처리되었습니다.");
        System.out.println("🎯 ClassCastException 문제가 해결되어 ParameterizedType도 안전하게 처리됩니다.");
    }
    
    @Test
    @DisplayName("단순한 중첩 타입 테스트 - Map<String, List<String>>")
    void testSimpleNestedType() {
        System.out.println("=== 단순한 중첩 타입 테스트 ===");
        
        // Given
        Map<String, List<String>> simpleData = new HashMap<>();
        simpleData.put("fruits", Arrays.asList("apple", "banana", "orange"));
        simpleData.put("colors", Arrays.asList("red", "green", "blue"));
        
        // When - 직렬화
        MapSerializer serializer = new MapSerializer();
        JSON5Object serialized = serializer.serializeWithTypeReference(simpleData,
            new JSON5TypeReference<Map<String, List<String>>>() {});
        
        System.out.println("직렬화 결과: " + serialized);
        
        // When - 역직렬화
        MapDeserializer deserializer = new MapDeserializer();
        Map<String, List<String>> deserialized = deserializer.deserializeWithTypeReference(serialized,
            new JSON5TypeReference<Map<String, List<String>>>() {});
        
        // Then
        assertNotNull(deserialized);
        assertEquals(2, deserialized.size());
        assertEquals(3, deserialized.get("fruits").size());
        assertEquals("apple", deserialized.get("fruits").get(0));
        assertEquals("red", deserialized.get("colors").get(0));
        
        System.out.println("✅ 단순한 중첩 타입 테스트 성공!");
    }
}
