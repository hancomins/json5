package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SubTypeRegistry의 테스트 클래스입니다.
 */
@DisplayName("서브타입 레지스트리 테스트")
class SubTypeRegistryTest {
    
    private SubTypeRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new SubTypeRegistry();
    }
    
    @Test
    @DisplayName("타입 정보를 등록하고 조회할 수 있어야 함")
    void shouldRegisterAndRetrieveTypeInfo() {
        // Given
        @JSON5TypeInfo(property = "type")
        @JSON5SubType(value = TestConcreteClass.class, name = "concrete")
        abstract class TestBaseClass {}
        
        // When
        TypeInfo typeInfo = registry.getTypeInfo(TestBaseClass.class);
        
        // Then
        assertNotNull(typeInfo);
        assertEquals("type", typeInfo.getTypeProperty());
        assertEquals(TestConcreteClass.class, typeInfo.getConcreteType("concrete"));
    }
    
    @Test
    @DisplayName("구체적인 타입을 올바르게 해결해야 함")
    void shouldResolveConcreteType() {
        // Given
        @JSON5TypeInfo(property = "type")
        @JSON5SubType(value = TestConcreteClass.class, name = "concrete")
        abstract class TestBaseClass {}
        
        // When
        Class<?> concreteType = registry.resolveConcreteType(TestBaseClass.class, "concrete");
        
        // Then
        assertEquals(TestConcreteClass.class, concreteType);
    }
    
    @Test
    @DisplayName("존재하지 않는 타입 이름에 대해 null을 반환해야 함")
    void shouldReturnNullForUnknownTypeName() {
        // Given
        @JSON5TypeInfo(property = "type")
        @JSON5SubType(value = TestConcreteClass.class, name = "concrete")
        abstract class TestBaseClass {}
        
        // When
        Class<?> concreteType = registry.resolveConcreteType(TestBaseClass.class, "unknown");
        
        // Then
        assertNull(concreteType);
    }
    
    @Test
    @DisplayName("기본 구현체가 있으면 반환해야 함")
    void shouldReturnDefaultImplWhenAvailable() {
        // Given
        @JSON5TypeInfo(property = "type", defaultImpl = TestConcreteClass.class)
        @JSON5SubType(value = TestConcreteClass.class, name = "concrete")
        abstract class TestBaseClass {}
        
        // When
        Class<?> concreteType = registry.resolveConcreteType(TestBaseClass.class, "unknown");
        
        // Then
        assertEquals(TestConcreteClass.class, concreteType);
    }
    
    @Test
    @DisplayName("다형성 타입이 아닌 클래스에 대해 false를 반환해야 함")
    void shouldReturnFalseForNonPolymorphicType() {
        // Given
        class TestClass {}
        
        // When
        boolean isPolymorphic = registry.isPolymorphicType(TestClass.class);
        
        // Then
        assertFalse(isPolymorphic);
    }
    
    @Test
    @DisplayName("캐시를 초기화할 수 있어야 함")
    void shouldClearCache() {
        // Given
        @JSON5TypeInfo(property = "type")
        abstract class TestClass {}
        
        registry.getTypeInfo(TestClass.class); // 캐시에 저장
        assertTrue(registry.getCacheSize() > 0);
        
        // When
        registry.clearCache();
        
        // Then
        assertEquals(0, registry.getCacheSize());
    }
    
    @Test
    @DisplayName("null 클래스에 대해 null을 반환해야 함")
    void shouldReturnNullForNullClass() {
        // When
        TypeInfo typeInfo = registry.getTypeInfo(null);
        
        // Then
        assertNull(typeInfo);
    }
    
    // 테스트용 더미 클래스
    @JSON5Type
    static class TestConcreteClass {
        @JSON5Value
        public String name;
    }
}
