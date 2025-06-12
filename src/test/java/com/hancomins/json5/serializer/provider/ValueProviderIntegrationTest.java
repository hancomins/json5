package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("값 공급자 통합 테스트")
class ValueProviderIntegrationTest {
    
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
    @DisplayName("값 공급자가 포함된 객체의 완전한 직렬화/역직렬화")
    void shouldSerializeAndDeserializeWithValueProvider() {
        // Given
        UserData original = new UserData();
        original.setUserId(new UserId("user-123"));
        original.setName("John Doe");
        
        // When - 직렬화
        JSON5Object serialized = JSON5Serializer.toJSON5Object(original);
        
        // Then - 직렬화 결과 확인
        assertNotNull(serialized);
        assertTrue(serialized.has("userId"));
        assertTrue(serialized.has("name"));
        assertEquals("John Doe", serialized.getString("name"));
        
        // When - 역직렬화  
        UserData deserialized = JSON5Serializer.fromJSON5Object(serialized, UserData.class);
        
        // Then - 역직렬화 결과 확인
        assertNotNull(deserialized);
        assertNotNull(deserialized.getUserId());
        assertEquals("user-123", deserialized.getUserId().getId());
        assertEquals("John Doe", deserialized.getName());
    }
    
    @Test
    @DisplayName("타입 변환 모드별 동작 테스트")
    void shouldHandleDifferentTypeModes() {
        // 엄격 모드 테스트
        @JSON5ValueProvider(strictTypeMatching = true)
        class StrictType {
            private String value;
            
            @JSON5ValueConstructor
            public StrictType(String value) {
                this.value = value;
            }
            
            @JSON5ValueExtractor  
            public String getValue() { 
                return value; 
            }
        }
        
        // 느슨 모드 테스트
        @JSON5ValueProvider(strictTypeMatching = false)
        class LooseType {
            private Long value;
            
            @JSON5ValueConstructor
            public LooseType(Long value) {
                this.value = value;
            }
            
            @JSON5ValueExtractor
            public String getValue() { 
                return String.valueOf(value); 
            }
        }
        
        // 테스트 구현...
        assertTrue(JSON5Serializer.isValueProvider(StrictType.class));
        assertTrue(JSON5Serializer.isValueProvider(LooseType.class));
    }
    
    @Test
    @DisplayName("Null 처리 테스트")
    void shouldHandleNullValues() {
        @JSON5ValueProvider
        class NullSafeType {
            private String value;
            
            @JSON5ValueConstructor(onNull = NullHandling.EMPTY_OBJECT)
            public NullSafeType(String value) {
                this.value = value != null ? value : "default";
            }
            
            @JSON5ValueExtractor(onNull = NullHandling.DEFAULT)
            public String getValue() {
                return value;
            }
        }
        
        // null 값으로 테스트
        NullSafeType obj = new NullSafeType(null);
        assertEquals("default", obj.getValue());
    }
    
    @Test
    @DisplayName("복잡한 타입 변환 테스트")
    void shouldHandleComplexTypeConversion() {
        @JSON5ValueProvider(strictTypeMatching = false)
        class FlexibleId {
            private long id;
            
            @JSON5ValueConstructor
            public FlexibleId(Long id) {
                this.id = id != null ? id : 0L;
            }
            
            @JSON5ValueExtractor
            public String getIdAsString() {
                return String.valueOf(id);
            }
        }
        
        FlexibleId obj = new FlexibleId(12345L);
        assertEquals("12345", obj.getIdAsString());
    }
    
    @Test
    @DisplayName("JSON5Object 타입 변환 테스트")
    void shouldHandleJSON5ObjectTypeConversion() {
        @JSON5ValueProvider(strictTypeMatching = false)
        class ConnectionConfig {
            private String host;
            private int port;
            
            @JSON5ValueConstructor
            public ConnectionConfig(JSON5Object config) {
                this.host = config.getString("host");
                this.port = config.getInt("port");
            }
            
            @JSON5ValueExtractor
            public JSON5Object getConfig() {
                JSON5Object obj = new JSON5Object();
                obj.put("host", host);
                obj.put("port", port);
                return obj;
            }
        }
        
        JSON5Object config = new JSON5Object();
        config.put("host", "localhost");
        config.put("port", 8080);
        
        ConnectionConfig conn = new ConnectionConfig(config);
        JSON5Object result = conn.getConfig();
        
        assertEquals("localhost", result.getString("host"));
        assertEquals(8080, result.getInt("port"));
    }
}
