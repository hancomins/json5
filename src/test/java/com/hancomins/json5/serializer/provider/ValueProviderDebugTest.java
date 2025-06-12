package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("값 공급자 디버그 테스트")
class ValueProviderDebugTest {
    
    @JSON5ValueProvider
    static class UserId {
        private final String id;
        
        @JSON5ValueConstructor
        public UserId(String id) {
            this.id = id;
        }
        
        @JSON5ValueExtractor
        public String getId() {
            return id;
        }
    }
    
    @JSON5Type
    static class UserData {
        @JSON5Value
        private UserId userId;
        
        @JSON5Value
        private String name;
        
        public UserData() {
        }
        
        public UserId getUserId() {
            return userId;
        }
        
        public void setUserId(UserId userId) {
            this.userId = userId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Test
    @DisplayName("직렬화 결과 디버깅")
    void debugSerialization() {
        // Given
        UserData original = new UserData();
        original.setUserId(new UserId("user-123"));
        original.setName("John Doe");
        
        // When - 직렬화
        JSON5Object serialized = JSON5Serializer.toJSON5Object(original);
        
        // Then - 결과 출력
        System.out.println("Serialized JSON: " + serialized.toString());
        System.out.println("UserId field: " + serialized.get("userId"));
        System.out.println("UserId field type: " + (serialized.get("userId") != null ? serialized.get("userId").getClass() : "null"));
        
        // 직렬화 결과 검증
        assertNotNull(serialized);
        assertTrue(serialized.has("userId"));
        assertTrue(serialized.has("name"));
        assertEquals("John Doe", serialized.getString("name"));
        
        // UserId가 String으로 직렬화되었는지 확인
        Object userIdValue = serialized.get("userId");
        if (userIdValue instanceof String) {
            assertEquals("user-123", userIdValue);
            System.out.println("✓ UserId가 String으로 직렬화됨: " + userIdValue);
        } else {
            System.out.println("✗ UserId가 String으로 직렬화되지 않음: " + userIdValue);
        }
    }
    
    @Test
    @DisplayName("역직렬화 디버깅")
    void debugDeserialization() {
        // Given - 수동으로 JSON 생성
        JSON5Object json = new JSON5Object();
        json.put("userId", "user-123");
        json.put("name", "John Doe");
        
        System.out.println("Input JSON: " + json.toString());
        
        try {
            // When - 역직렬화
            UserData deserialized = JSON5Serializer.fromJSON5Object(json, UserData.class);
            
            // Then
            assertNotNull(deserialized);
            System.out.println("✓ 역직렬화 성공");
            
            if (deserialized.getUserId() != null) {
                System.out.println("✓ UserId 객체 생성됨: " + deserialized.getUserId().getId());
                assertEquals("user-123", deserialized.getUserId().getId());
            } else {
                System.out.println("✗ UserId 객체가 null");
            }
            
            assertEquals("John Doe", deserialized.getName());
            
        } catch (Exception e) {
            System.out.println("✗ 역직렬화 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
