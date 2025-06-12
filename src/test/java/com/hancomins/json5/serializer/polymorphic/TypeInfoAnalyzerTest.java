package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TypeInfoAnalyzer의 테스트 클래스입니다.
 */
@DisplayName("타입 정보 분석기 테스트")
class TypeInfoAnalyzerTest {
    
    private TypeInfoAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new TypeInfoAnalyzer();
    }
    
    @Test
    @DisplayName("@JSON5TypeInfo가 붙은 클래스를 다형성 타입으로 인식해야 함")
    void shouldIdentifyPolymorphicType() {
        // Given
        @JSON5TypeInfo(property = "type")
        @JSON5SubType(value = TestConcreteClass.class, name = "concrete")
        abstract class TestBaseClass {}
        
        // When
        boolean isPolymorphic = analyzer.isPolymorphicType(TestBaseClass.class);
        
        // Then
        assertTrue(isPolymorphic);
    }
    
    @Test
    @DisplayName("@JSON5TypeInfo가 없는 클래스는 다형성 타입이 아님")
    void shouldNotIdentifyNonPolymorphicType() {
        // Given
        class TestClass {}
        
        // When
        boolean isPolymorphic = analyzer.isPolymorphicType(TestClass.class);
        
        // Then
        assertFalse(isPolymorphic);
    }
    
    @Test
    @DisplayName("타입 정보를 올바르게 분석해야 함")
    void shouldAnalyzeTypeInfoCorrectly() {
        // Given
        @JSON5TypeInfo(
            property = "vehicle.type", 
            include = TypeInclusion.EXISTING_PROPERTY,
            defaultImpl = TestConcreteClass.class,
            onMissingType = MissingTypeStrategy.EXCEPTION
        )
        @JSON5SubType(value = TestConcreteClass.class, name = "car")
        @JSON5SubType(value = AnotherConcreteClass.class, name = "bike")
        abstract class TestVehicle {}
        
        // When
        TypeInfo typeInfo = analyzer.analyzeTypeInfo(TestVehicle.class);
        
        // Then
        assertNotNull(typeInfo);
        assertEquals("vehicle.type", typeInfo.getTypeProperty());
        assertEquals(TypeInclusion.EXISTING_PROPERTY, typeInfo.getInclusion());
        assertEquals(TestConcreteClass.class, typeInfo.getDefaultImpl());
        assertEquals(MissingTypeStrategy.EXCEPTION, typeInfo.getMissingStrategy());
        
        // 서브타입 확인
        assertEquals(2, typeInfo.getSubTypes().size());
        assertEquals(TestConcreteClass.class, typeInfo.getConcreteType("car"));
        assertEquals(AnotherConcreteClass.class, typeInfo.getConcreteType("bike"));
    }
    
    @Test
    @DisplayName("기본값들이 올바르게 설정되어야 함")
    void shouldHaveCorrectDefaults() {
        // Given
        @JSON5TypeInfo(property = "type")
        @JSON5SubType(value = TestConcreteClass.class, name = "test")
        abstract class TestClass {}
        
        // When
        TypeInfo typeInfo = analyzer.analyzeTypeInfo(TestClass.class);
        
        // Then
        assertNotNull(typeInfo);
        assertEquals("type", typeInfo.getTypeProperty());
        assertEquals(TypeInclusion.PROPERTY, typeInfo.getInclusion()); // 기본값
        assertEquals(Void.class, typeInfo.getDefaultImpl()); // 기본값
        assertEquals(MissingTypeStrategy.DEFAULT_IMPL, typeInfo.getMissingStrategy()); // 기본값
        assertFalse(typeInfo.hasDefaultImpl()); // Void.class는 기본 구현체가 아님
    }
    
    @Test
    @DisplayName("서브타입이 없는 경우도 처리해야 함")
    void shouldHandleNoSubTypes() {
        // Given
        @JSON5TypeInfo(property = "type")
        abstract class TestClass {}
        
        // When
        TypeInfo typeInfo = analyzer.analyzeTypeInfo(TestClass.class);
        
        // Then
        assertNotNull(typeInfo);
        assertTrue(typeInfo.getSubTypes().isEmpty());
        assertTrue(typeInfo.getSupportedTypeNames().isEmpty());
    }
    
    // 테스트용 더미 클래스들
    @JSON5Type
    static class TestConcreteClass {
        @JSON5Value
        public String name;
    }
    
    @JSON5Type
    static class AnotherConcreteClass {
        @JSON5Value
        public String description;
    }
}
