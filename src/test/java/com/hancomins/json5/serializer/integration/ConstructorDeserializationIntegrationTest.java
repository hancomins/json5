package com.hancomins.json5.serializer.integration;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import com.hancomins.json5.serializer.constructor.ConstructorDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("생성자 기반 역직렬화 통합 테스트")
class ConstructorDeserializationIntegrationTest {
    
    private JSON5Serializer serializer;
    private ConstructorDeserializer constructorDeserializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.getInstance();
        constructorDeserializer = new ConstructorDeserializer();
    }
    
    @Test
    @DisplayName("기본 생성자 기반 역직렬화 테스트")
    void testBasicConstructorDeserialization() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        json.put("age", 30);
        
        // When
        User user = constructorDeserializer.deserialize(json, User.class);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals(30, user.getAge());
    }
    
    @Test
    @DisplayName("중첩 경로 접근 테스트")
    void testNestedPathAccess() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        json.put("age", 30);
        
        JSON5Object profile = new JSON5Object();
        profile.put("email", "john@example.com");
        profile.put("department", "Engineering");
        json.put("profile", profile);
        
        // When
        UserWithProfile user = constructorDeserializer.deserialize(json, UserWithProfile.class);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals(30, user.getAge());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("Engineering", user.getDepartment());
    }
    
    @Test
    @DisplayName("필수 필드 누락 시 예외 발생 테스트")
    void testRequiredFieldMissing() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        // age 필드 누락
        
        // When & Then
        assertThrows(JSON5SerializerException.class, () -> 
            constructorDeserializer.deserialize(json, UserWithRequiredAge.class)
        );
    }
    
    @Test
    @DisplayName("우선순위에 따른 생성자 선택 테스트")
    void testConstructorPrioritySelection() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("data", 42);
        
        // When
        UserWithPriority user = constructorDeserializer.deserialize(json, UserWithPriority.class);
        
        // Then
        assertNotNull(user);
        assertEquals("from-data-constructor", user.getName());
        assertEquals(42, user.getData());
    }
    
    @Test
    @DisplayName("JSON5Serializer와의 통합 테스트")
    void testIntegrationWithJSON5Serializer() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        json.put("age", 30);
        
        JSON5Object profile = new JSON5Object();
        profile.put("email", "john@example.com");
        profile.put("department", "Engineering");
        json.put("profile", profile);
        
        // When
        UserWithProfile user = serializer.deserialize(json, UserWithProfile.class);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals(30, user.getAge());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("Engineering", user.getDepartment());
    }
    
    @Test
    @DisplayName("기본값 처리 테스트")
    void testDefaultValueHandling() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        // age와 profile 정보 누락
        
        // When
        UserWithDefaults user = constructorDeserializer.deserialize(json, UserWithDefaults.class);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals(0, user.getAge()); // int 기본값
        assertNull(user.getEmail()); // String 기본값
    }
    
    @Test
    @DisplayName("복합 타입 처리 테스트")
    void testComplexTypeHandling() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John Doe");
        
        JSON5Object profileObj = new JSON5Object();
        profileObj.put("email", "john@example.com");
        profileObj.put("department", "Engineering");
        json.put("profile", profileObj);
        
        // When
        UserWithComplexType user = constructorDeserializer.deserialize(json, UserWithComplexType.class);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        // profile 객체는 현재 JSON5Element로 처리됨 (추후 개선 예정)
        assertNotNull(user.getProfile());
    }
    
    // 테스트용 클래스들
    @JSON5Type
    public static class User {
        private final String name;
        private final int age;
        
        @JSON5Creator
        public User(@JSON5Property("name") String name,
                   @JSON5Property("age") int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
    }
    
    @JSON5Type
    public static class UserWithProfile {
        private final String name;
        private final int age;
        private final String email;
        private final String department;
        
        @JSON5Creator
        public UserWithProfile(@JSON5Property("name") String name,
                              @JSON5Property("age") int age,
                              @JSON5Property("profile.email") String email,
                              @JSON5Property("profile.department") String department) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.department = department;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
    }
    
    @JSON5Type
    public static class UserWithRequiredAge {
        private final String name;
        private final int age;
        
        @JSON5Creator
        public UserWithRequiredAge(@JSON5Property("name") String name,
                                  @JSON5Property(value = "age", required = true) int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
    }
    
    @JSON5Type
    public static class UserWithPriority {
        private final String name;
        private final int data;
        
        @JSON5Creator
        public UserWithPriority(@JSON5Property("name") String name) {
            this.name = name;
            this.data = 0;
        }
        
        @JSON5Creator(priority = 1)
        public UserWithPriority(@JSON5Property("data") int data) {
            this.name = "from-data-constructor";
            this.data = data;
        }
        
        public String getName() { return name; }
        public int getData() { return data; }
    }
    
    @JSON5Type
    public static class UserWithDefaults {
        private final String name;
        private final int age;
        private final String email;
        
        @JSON5Creator
        public UserWithDefaults(@JSON5Property("name") String name,
                               @JSON5Property("age") int age,
                               @JSON5Property("profile.email") String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getEmail() { return email; }
    }
    
    @JSON5Type
    public static class UserWithComplexType {
        private final String name;
        private final Object profile; // 복합 타입 (현재는 JSON5Element로 처리)
        
        @JSON5Creator
        public UserWithComplexType(@JSON5Property("name") String name,
                                  @JSON5Property("profile") Object profile) {
            this.name = name;
            this.profile = profile;
        }
        
        public String getName() { return name; }
        public Object getProfile() { return profile; }
    }
}
