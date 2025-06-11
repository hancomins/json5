package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 간단한 컴파일 테스트
 */
class SimplePolymorphicTest {
    
    @Test
    void testBasicCompilation() {
        // Given
        TypeInfoAnalyzer analyzer = new TypeInfoAnalyzer();
        SubTypeRegistry registry = new SubTypeRegistry();
        PolymorphicDeserializer deserializer = new PolymorphicDeserializer();
        
        // When & Then
        assertNotNull(analyzer);
        assertNotNull(registry);
        assertNotNull(deserializer);
    }
    
    @Test
    void testTypeInfoAnalyzer() {
        // Given
        TypeInfoAnalyzer analyzer = new TypeInfoAnalyzer();
        
        // When
        boolean isPolymorphic = analyzer.isPolymorphicType(String.class);
        
        // Then
        assertFalse(isPolymorphic);
    }
    
    @Test
    void testSubTypeRegistry() {
        // Given
        SubTypeRegistry registry = new SubTypeRegistry();
        
        // When
        TypeInfo typeInfo = registry.getTypeInfo(String.class);
        
        // Then
        assertNull(typeInfo);
    }
}
