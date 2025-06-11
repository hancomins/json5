package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("값 공급자 단계별 테스트")
class ValueProviderStepByStepTest {
    
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
        private String name;
        
        public UserData() {
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    @Test
    @DisplayName("1단계: UserId만 직렬화/역직렬화")
    void step1_userIdOnly() {
        // Given
        UserId userId = new UserId("test-123");
        
        // When - 직렬화
        ValueProviderRegistry registry = new ValueProviderRegistry();
        ValueProviderSerializer serializer = new ValueProviderSerializer(registry);
        Object serialized = serializer.serialize(userId);
        
        // Then - 직렬화 확인
        assertEquals("test-123", serialized);
        
        // When - 역직렬화
        ValueProviderDeserializer deserializer = new ValueProviderDeserializer(registry);
        UserId deserialized = deserializer.deserialize(serialized, UserId.class);
        
        // Then - 역직렬화 확인
        assertNotNull(deserialized);
        assertEquals("test-123", deserialized.getId());
    }
    
    @Test
    @DisplayName("2단계: UserData만 직렬화/역직렬화")
    void step2_userDataOnly() {
        // Given
        UserData userData = new UserData();
        userData.setName("John Doe");
        
        // When - 직렬화
        JSON5Object serialized = JSON5Serializer.toJSON5Object(userData);
        
        // Then - 직렬화 확인
        assertNotNull(serialized);
        assertEquals("John Doe", serialized.getString("name"));
        
        // When - 역직렬화
        UserData deserialized = JSON5Serializer.fromJSON5Object(serialized, UserData.class);
        
        // Then - 역직렬화 확인
        assertNotNull(deserialized);
        assertEquals("John Doe", deserialized.getName());
    }
    
    @Test
    @DisplayName("3단계: JSON5Object에 String 값을 직접 넣기")
    void step3_manualSerialization() {
        // Given
        UserId userId = new UserId("test-123");
        ValueProviderRegistry registry = new ValueProviderRegistry();
        ValueProviderSerializer serializer = new ValueProviderSerializer(registry);
        Object userIdValue = serializer.serialize(userId);
        
        // When - 수동으로 JSON5Object 생성
        JSON5Object json = new JSON5Object();
        json.put("userId", userIdValue);
        json.put("name", "John Doe");
        
        // Then
        assertEquals("test-123", json.getString("userId"));
        assertEquals("John Doe", json.getString("name"));
        
        // When - 수동으로 역직렬화
        String userIdStr = json.getString("userId");
        ValueProviderDeserializer deserializer = new ValueProviderDeserializer(registry);
        UserId deserializedUserId = deserializer.deserialize(userIdStr, UserId.class);
        
        // Then
        assertNotNull(deserializedUserId);
        assertEquals("test-123", deserializedUserId.getId());
    }
}
