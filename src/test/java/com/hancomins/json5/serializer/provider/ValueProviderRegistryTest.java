package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("값 공급자 레지스트리 테스트")
class ValueProviderRegistryTest {
    
    private ValueProviderRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new ValueProviderRegistry();
    }
    
    @JSON5ValueProvider
    static class SimpleProvider {
        private String value;
        
        @JSON5ValueConstructor
        public SimpleProvider(String value) {
            this.value = value;
        }
        
        @JSON5ValueExtractor
        public String getValue() {
            return value;
        }
    }
    
    static class NonProvider {
        private String value;
        
        public NonProvider(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    @Test
    @DisplayName("값 공급자 클래스를 올바르게 식별해야 함")
    void shouldIdentifyValueProviderClass() {
        assertTrue(registry.isValueProvider(SimpleProvider.class));
        assertFalse(registry.isValueProvider(NonProvider.class));
        assertFalse(registry.isValueProvider(null));
    }
    
    @Test
    @DisplayName("값 공급자 정보를 올바르게 분석해야 함")
    void shouldAnalyzeValueProviderInfo() {
        ValueProviderInfo info = registry.getValueProviderInfo(SimpleProvider.class);
        
        assertNotNull(info);
        assertEquals(SimpleProvider.class, info.getProviderClass());
        assertEquals(String.class, info.getTargetType());
        assertTrue(info.canSerialize());
        assertTrue(info.canDeserialize());
        assertTrue(info.isStrictTypeMatching());
    }
    
    @Test
    @DisplayName("값 공급자가 아닌 클래스는 null 반환해야 함")
    void shouldReturnNullForNonValueProvider() {
        ValueProviderInfo info = registry.getValueProviderInfo(NonProvider.class);
        assertNull(info);
    }
    
    @Test
    @DisplayName("캐싱이 올바르게 동작해야 함")
    void shouldCacheCorrectly() {
        assertEquals(0, registry.getCacheSize());
        
        ValueProviderInfo info1 = registry.getValueProviderInfo(SimpleProvider.class);
        assertEquals(1, registry.getCacheSize());
        
        ValueProviderInfo info2 = registry.getValueProviderInfo(SimpleProvider.class);
        assertEquals(1, registry.getCacheSize()); // 캐시 사용으로 증가하지 않음
        
        assertSame(info1, info2); // 동일한 인스턴스 반환
    }
    
    @Test
    @DisplayName("캐시 초기화가 동작해야 함")
    void shouldClearCache() {
        registry.getValueProviderInfo(SimpleProvider.class);
        assertEquals(1, registry.getCacheSize());
        
        registry.clearCache();
        assertEquals(0, registry.getCacheSize());
    }
    
    @Test
    @DisplayName("수동 등록이 동작해야 함")
    void shouldRegisterManually() {
        registry.registerValueProvider(SimpleProvider.class);
        assertEquals(1, registry.getCacheSize());
        
        ValueProviderInfo info = registry.getValueProviderInfo(SimpleProvider.class);
        assertNotNull(info);
    }
    
    @JSON5ValueProvider(strictTypeMatching = false)
    static class FlexibleProvider {
        private Long value;
        
        @JSON5ValueConstructor
        public FlexibleProvider(Long value) {
            this.value = value;
        }
        
        @JSON5ValueExtractor
        public String getValue() {
            return String.valueOf(value);
        }
    }
    
    @Test
    @DisplayName("느슨한 타입 매칭이 올바르게 동작해야 함")
    void shouldHandleFlexibleTypeMatching() {
        ValueProviderInfo info = registry.getValueProviderInfo(FlexibleProvider.class);
        
        assertNotNull(info);
        assertFalse(info.isStrictTypeMatching());
        assertEquals(String.class, info.getTargetType());
    }
    
    @JSON5ValueProvider
    static class InvalidProvider {
        @JSON5ValueConstructor
        public InvalidProvider(String value1, String value2) {
            // 파라미터가 2개라서 잘못됨
        }
        
        @JSON5ValueExtractor
        public String getValue() {
            return "test";
        }
    }
    
    @Test
    @DisplayName("잘못된 값 공급자는 예외를 발생시켜야 함")
    void shouldThrowExceptionForInvalidProvider() {
        assertThrows(JSON5SerializerException.class, () -> {
            registry.getValueProviderInfo(InvalidProvider.class);
        });
    }
}
