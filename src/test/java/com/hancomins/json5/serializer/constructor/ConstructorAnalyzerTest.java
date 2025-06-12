package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.serializer.JSON5Creator;
import com.hancomins.json5.serializer.JSON5Property;
import com.hancomins.json5.serializer.MissingValueStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConstructorAnalyzer 테스트")
class ConstructorAnalyzerTest {
    
    private ConstructorAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        analyzer = new ConstructorAnalyzer();
    }
    
    @Test
    @DisplayName("@JSON5Creator 어노테이션이 붙은 생성자를 찾아야 함")
    void shouldFindCreatorConstructors() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithCreator.class);
        
        // Then
        assertEquals(2, constructors.size());
        
        // 우선순위 순으로 정렬되어 있는지 확인
        assertTrue(constructors.get(0).getPriority() >= constructors.get(1).getPriority());
    }
    
    @Test
    @DisplayName("우선순위에 따라 생성자가 정렬되어야 함")
    void shouldSortConstructorsByPriority() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithCreator.class);
        
        // Then
        assertEquals(2, constructors.size());
        assertEquals(1, constructors.get(0).getPriority()); // 높은 우선순위
        assertEquals(0, constructors.get(1).getPriority()); // 낮은 우선순위
    }
    
    @Test
    @DisplayName("가장 적합한 생성자를 선택해야 함")
    void shouldSelectBestConstructor() {
        // Given
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithCreator.class);
        
        // When
        ConstructorInfo bestConstructor = analyzer.selectBestConstructor(constructors);
        
        // Then
        assertNotNull(bestConstructor);
        assertEquals(1, bestConstructor.getPriority());
        assertEquals(1, bestConstructor.getParameterCount());
    }
    
    @Test
    @DisplayName("@JSON5Creator가 없는 클래스는 빈 리스트를 반환해야 함")
    void shouldReturnEmptyListForNonCreatorClass() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithoutCreator.class);
        
        // Then
        assertTrue(constructors.isEmpty());
        assertFalse(analyzer.hasCreatorConstructor(TestClassWithoutCreator.class));
    }
    
    @Test
    @DisplayName("파라미터 정보를 올바르게 분석해야 함")
    void shouldAnalyzeParameterInfoCorrectly() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithMultipleParams.class);
        
        // Then
        assertEquals(1, constructors.size());
        
        ConstructorInfo constructorInfo = constructors.get(0);
        List<ParameterInfo> parameters = constructorInfo.getParameters();
        assertEquals(4, parameters.size());
        
        // 첫 번째 파라미터 확인
        ParameterInfo param1 = parameters.get(0);
        assertEquals("name", param1.getJsonPath());
        assertEquals(String.class, param1.getParameterType());
        assertFalse(param1.isRequired());
        assertEquals(MissingValueStrategy.DEFAULT_VALUE, param1.getMissingStrategy());
        
        // 두 번째 파라미터 확인 (required = true)
        ParameterInfo param2 = parameters.get(1);
        assertEquals("age", param2.getJsonPath());
        assertEquals(int.class, param2.getParameterType());
        assertTrue(param2.isRequired());
        
        // 세 번째 파라미터 확인 (중첩 경로)
        ParameterInfo param3 = parameters.get(2);
        assertEquals("profile.email", param3.getJsonPath());
        assertEquals(String.class, param3.getParameterType());
        
        // 네 번째 파라미터 확인 (EXCEPTION 전략)
        ParameterInfo param4 = parameters.get(3);
        assertEquals("department", param4.getJsonPath());
        assertEquals(String.class, param4.getParameterType());
        assertEquals(MissingValueStrategy.EXCEPTION, param4.getMissingStrategy());
    }
    
    @Test
    @DisplayName("@JSON5Property가 없는 파라미터가 있으면 null을 반환해야 함")
    void shouldReturnNullForInvalidConstructor() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(TestClassWithInvalidConstructor.class);
        
        // Then
        assertTrue(constructors.isEmpty());
    }
    
    @Test
    @DisplayName("null 클래스에 대해 빈 리스트를 반환해야 함")
    void shouldReturnEmptyListForNullClass() {
        // When
        List<ConstructorInfo> constructors = analyzer.analyzeConstructors(null);
        
        // Then
        assertTrue(constructors.isEmpty());
        assertFalse(analyzer.hasCreatorConstructor(null));
    }
    
    @Test
    @DisplayName("빈 리스트에서 최적 생성자 선택 시 null 반환")
    void shouldReturnNullForEmptyConstructorList() {
        // When
        ConstructorInfo bestConstructor = analyzer.selectBestConstructor(null);
        
        // Then
        assertNull(bestConstructor);
        
        // When
        bestConstructor = analyzer.selectBestConstructor(List.of());
        
        // Then
        assertNull(bestConstructor);
    }
    
    // 테스트용 클래스들
    static class TestClassWithCreator {
        private final String name;
        private final int data;
        
        // 기본 생성자
        public TestClassWithCreator() {
            this.name = "default";
            this.data = 0;
        }
        
        // 우선순위 낮은 생성자
        @JSON5Creator
        public TestClassWithCreator(@JSON5Property("name") String name) {
            this.name = name;
            this.data = 0;
        }
        
        // 우선순위 높은 생성자
        @JSON5Creator(priority = 1)
        public TestClassWithCreator(@JSON5Property("data") int data) {
            this.name = "from-data";
            this.data = data;
        }
    }
    
    static class TestClassWithoutCreator {
        private String name;
        
        public TestClassWithoutCreator() {
            this.name = "default";
        }
        
        public TestClassWithoutCreator(String name) {
            this.name = name;
        }
    }
    
    static class TestClassWithMultipleParams {
        private final String name;
        private final int age;
        private final String email;
        private final String department;
        
        @JSON5Creator
        public TestClassWithMultipleParams(
                @JSON5Property("name") String name,
                @JSON5Property(value = "age", required = true) int age,
                @JSON5Property("profile.email") String email,
                @JSON5Property(value = "department", onMissing = MissingValueStrategy.EXCEPTION) String department) {
            this.name = name;
            this.age = age;
            this.email = email;
            this.department = department;
        }
    }
    
    static class TestClassWithInvalidConstructor {
        private final String name;
        private final int age;
        
        @JSON5Creator
        public TestClassWithInvalidConstructor(
                @JSON5Property("name") String name,
                int age) { // @JSON5Property가 없음
            this.name = name;
            this.age = age;
        }
    }
}
