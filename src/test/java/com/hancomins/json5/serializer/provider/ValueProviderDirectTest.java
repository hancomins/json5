package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import com.hancomins.json5.serializer.provider.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("값 공급자 직접 테스트")
class ValueProviderDirectTest {
    
    @JSON5ValueProvider
    static class SimpleUserId {
        private final String id;
        
        @JSON5ValueConstructor
        public SimpleUserId(String id) {
            this.id = id;
        }
        
        @JSON5ValueExtractor
        public String getId() {
            return id;
        }
    }
    
    @Test
    @DisplayName("값 공급자 직접 직렬화 테스트")
    void shouldSerializeValueProviderDirectly() {
        // Given
        SimpleUserId userId = new SimpleUserId("test-123");
        ValueProviderRegistry registry = new ValueProviderRegistry();
        ValueProviderSerializer serializer = new ValueProviderSerializer(registry);
        
        // When
        Object result = serializer.serialize(userId);
        
        // Then
        assertNotNull(result);
        assertEquals("test-123", result);
    }
    
    @Test
    @DisplayName("값 공급자 직접 역직렬화 테스트")
    void shouldDeserializeValueProviderDirectly() {
        // Given
        String value = "test-123";
        ValueProviderRegistry registry = new ValueProviderRegistry();
        ValueProviderDeserializer deserializer = new ValueProviderDeserializer(registry);
        
        // When
        SimpleUserId result = deserializer.deserialize(value, SimpleUserId.class);
        
        // Then
        assertNotNull(result);
        assertEquals("test-123", result.getId());
    }
    
    @Test
    @DisplayName("값 공급자 등록 테스트")
    void shouldRegisterValueProvider() {
        // Given
        ValueProviderRegistry registry = new ValueProviderRegistry();
        
        // When
        boolean isProvider = registry.isValueProvider(SimpleUserId.class);
        
        // Then
        assertTrue(isProvider);
    }
}
