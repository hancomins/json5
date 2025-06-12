package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 4.4 단계 타입별 역직렬화 전략 검증 클래스
 * 
 * DeserializationStrategy 인터페이스와 구현체들, DeserializationStrategyFactory,
 * 그리고 DeserializationEngine에 적용된 전략 패턴을 종합적으로 검증합니다.
 */
@DisplayName("4.4 단계: 타입별 역직렬화 전략 검증")
public class RefactoringStep44Validator {
    
    private DeserializationEngine engine;
    private DeserializationStrategyFactory factory;
    private JSON5Object testJson;
    private JSON5Array testArray;
    
    @BeforeEach
    void setUp() {
        // 기본 팩토리와 엔진 생성
        factory = DeserializationStrategyFactory.createDefault();
        engine = new DeserializationEngine(factory);
        
        // 테스트용 JSON 데이터 준비
        testJson = new JSON5Object();
        testJson.put("stringValue", "test");
        testJson.put("intValue", 42);
        testJson.put("boolValue", true);
        testJson.put("$value", "primitiveTest");
        
        testArray = new JSON5Array();
        testArray.put("item1");
        testArray.put("item2");
        testArray.put("item3");
    }
    
    @Nested
    @DisplayName("DeserializationStrategy 인터페이스 검증")
    class DeserializationStrategyTests {
        
        @Test
        @DisplayName("PrimitiveDeserializationStrategy 기본 동작 검증")
        void testPrimitiveStrategy() {
            PrimitiveDeserializationStrategy strategy = new PrimitiveDeserializationStrategy();
            
            // canHandle 검증
            assertTrue(strategy.canHandle(Types.String, String.class));
            assertTrue(strategy.canHandle(Types.Integer, Integer.class));
            assertTrue(strategy.canHandle(Types.Boolean, Boolean.class));
            assertFalse(strategy.canHandle(Types.Object, Map.class));
            
            // 우선순위 검증
            assertEquals(10, strategy.getPriority());
            
            // 역직렬화 검증
            DeserializationContext context = createMockContext();
            Object result = strategy.deserialize(testJson, String.class, context);
            assertEquals("primitiveTest", result);
        }
        
        @Test
        @DisplayName("ComplexObjectDeserializationStrategy 기본 동작 검증")
        void testComplexObjectStrategy() {
            ComplexObjectDeserializationStrategy strategy = new ComplexObjectDeserializationStrategy();
            
            // canHandle 검증
            assertTrue(strategy.canHandle(Types.JSON5Object, JSON5Object.class));
            assertTrue(strategy.canHandle(Types.JSON5Array, JSON5Array.class));
            assertTrue(strategy.canHandle(Types.JSON5Element, JSON5Element.class));
            assertFalse(strategy.canHandle(Types.String, String.class));
            
            // 우선순위 검증
            assertEquals(50, strategy.getPriority());
            
            // JSON5Object 역직렬화 검증
            DeserializationContext context = createMockContext();
            Object result = strategy.deserialize(testJson, JSON5Object.class, context);
            assertSame(testJson, result);
            
            // JSON5Array 역직렬화 검증
            Object arrayResult = strategy.deserialize(testArray, JSON5Array.class, context);
            assertSame(testArray, arrayResult);
        }
        
        @Test
        @DisplayName("GenericTypeDeserializationStrategy 기본 동작 검증")
        void testGenericTypeStrategy() {
            GenericTypeDeserializationStrategy strategy = new GenericTypeDeserializationStrategy();
            
            // canHandle 검증
            assertTrue(strategy.canHandle(Types.GenericType, List.class));
            assertTrue(strategy.canHandle(Types.AbstractObject, Map.class));
            assertTrue(strategy.canHandle(Types.Object, List.class)); // 인터페이스
            assertFalse(strategy.canHandle(Types.String, String.class));
            
            // 우선순위 검증
            assertEquals(80, strategy.getPriority());
        }
    }
    
    @Nested
    @DisplayName("DeserializationStrategyFactory 검증")
    class StrategyFactoryTests {
        
        @Test
        @DisplayName("기본 팩토리 생성 및 기본 전략 등록 확인")
        void testDefaultFactory() {
            DeserializationStrategyFactory defaultFactory = DeserializationStrategyFactory.createDefault();
            
            // 기본 전략들이 등록되어 있는지 확인
            List<DeserializationStrategy> strategies = defaultFactory.getAllStrategies();
            assertEquals(3, strategies.size());
            
            // 우선순위 순으로 정렬되어 있는지 확인
            for (int i = 1; i < strategies.size(); i++) {
                assertTrue(strategies.get(i-1).getPriority() <= strategies.get(i).getPriority());
            }
        }
        
        @Test
        @DisplayName("빈 팩토리 생성 확인")
        void testEmptyFactory() {
            DeserializationStrategyFactory emptyFactory = DeserializationStrategyFactory.createEmpty();
            assertEquals(0, emptyFactory.getStrategyCount());
        }
        
        @Test
        @DisplayName("전략 등록 및 조회")
        void testStrategyRegistrationAndRetrieval() {
            DeserializationStrategyFactory factory = DeserializationStrategyFactory.createEmpty();
            TestDeserializationStrategy testStrategy = new TestDeserializationStrategy();
            
            // 전략 등록
            factory.registerStrategy(testStrategy);
            assertEquals(1, factory.getStrategyCount());
            
            // 전략 조회
            DeserializationStrategy foundStrategy = factory.getStrategy(Types.String, String.class);
            assertSame(testStrategy, foundStrategy);
            
            // 캐시 확인 (두 번째 호출)
            DeserializationStrategy cachedStrategy = factory.getStrategy(Types.String, String.class);
            assertSame(testStrategy, cachedStrategy);
        }
        
        @Test
        @DisplayName("전략 제거")
        void testStrategyRemoval() {
            TestDeserializationStrategy testStrategy = new TestDeserializationStrategy();
            factory.registerStrategy(testStrategy);
            
            int initialCount = factory.getStrategyCount();
            boolean removed = factory.removeStrategy(testStrategy);
            
            assertTrue(removed);
            assertEquals(initialCount - 1, factory.getStrategyCount());
        }
        
        @Test
        @DisplayName("캐시 초기화")
        void testCacheClear() {
            TestDeserializationStrategy testStrategy = new TestDeserializationStrategy();
            factory.registerStrategy(testStrategy);
            
            // 캐시 생성
            factory.getStrategy(Types.String, String.class);
            
            // 캐시 초기화
            assertDoesNotThrow(() -> factory.clearCache());
        }
    }
    
    @Nested
    @DisplayName("DeserializationEngine 전략 패턴 통합 검증")
    class EngineIntegrationTests {
        
        @Test
        @DisplayName("엔진의 전략 기반 역직렬화")
        void testEngineStrategyDeserialization() {
            // JSON5Object → JSON5Object 역직렬화 (ComplexObjectStrategy 사용)
            Object result1 = engine.tryDeserializeWithStrategy(testJson, JSON5Object.class);
            assertSame(testJson, result1);
            
            // JSON5Array → JSON5Array 역직렬화 (ComplexObjectStrategy 사용)
            Object result2 = engine.tryDeserializeWithStrategy(testArray, JSON5Array.class);
            assertSame(testArray, result2);
            
            // 지원하지 않는 타입 (null 반환 확인)
            Object result3 = engine.tryDeserializeWithStrategy(testJson, StringBuilder.class);
            assertNull(result3);
        }
        
        @Test
        @DisplayName("엔진의 새로운 전략 등록")
        void testEngineStrategyRegistration() {
            TestDeserializationStrategy customStrategy = new TestDeserializationStrategy();
            
            // 전략 등록
            engine.registerStrategy(customStrategy);
            
            // 등록된 전략 확인
            List<DeserializationStrategy> strategies = engine.getAllStrategies();
            assertTrue(strategies.contains(customStrategy));
        }
        
        @Test
        @DisplayName("전략 실패 시 기존 방식으로 fallback")
        void testStrategyFallback() {
            // 실패하는 전략 등록
            engine.registerStrategy(new FailingDeserializationStrategy());
            
            // 전략이 실패해도 예외가 발생하지 않고 null이 반환되는지 확인
            Object result = engine.tryDeserializeWithStrategy(testJson, String.class);
            assertNull(result); // 실패 시 null 반환
        }
        
        @Test
        @DisplayName("엔진의 캐시 관리")
        void testEngineCacheManagement() {
            // 캐시 초기화 메소드 호출 시 예외가 발생하지 않는지 확인
            assertDoesNotThrow(() -> engine.clearStrategyCache());
            
            // 전략 팩토리 접근 확인
            assertNotNull(engine.getStrategyFactory());
        }
    }
    
    @Nested
    @DisplayName("실제 사용 시나리오 검증")
    class RealWorldScenarioTests {
        
        @Test
        @DisplayName("복합 타입의 역직렬화 시나리오")
        void testComplexTypeDeserialization() {
            // List<String> 시나리오
            JSON5Array stringArray = new JSON5Array();
            stringArray.put("a");
            stringArray.put("b");
            stringArray.put("c");
            
            Object listResult = engine.tryDeserializeWithStrategy(stringArray, ArrayList.class);
            assertNotNull(listResult);
            assertTrue(listResult instanceof List);
            
            // Map<String, Object> 시나리오
            JSON5Object mapJson = new JSON5Object();
            mapJson.put("key1", "value1");
            mapJson.put("key2", 42);
            mapJson.put("key3", true);
            
            Object mapResult = engine.tryDeserializeWithStrategy(mapJson, HashMap.class);
            assertNotNull(mapResult);
            assertTrue(mapResult instanceof Map);
        }
        
        @Test
        @DisplayName("전략 우선순위 기반 선택 시나리오")
        void testStrategyPrioritySelection() {
            // 높은 우선순위 전략 등록
            engine.registerStrategy(new HighPriorityTestStrategy());
            
            // String 타입 처리 시 높은 우선순위 전략이 선택되는지 확인
            DeserializationStrategy selectedStrategy = engine.getStrategyFactory().getStrategy(Types.String, String.class);
            assertTrue(selectedStrategy instanceof HighPriorityTestStrategy);
        }
        
        @Test
        @DisplayName("다양한 타입의 통합 처리 시나리오")
        void testVariousTypesIntegration() {
            // 기본형 타입들
            assertNotNull(engine.tryDeserializeWithStrategy(testJson, JSON5Object.class));
            assertNotNull(engine.tryDeserializeWithStrategy(testArray, JSON5Array.class));
            
            // 컬렉션 타입들
            assertNotNull(engine.tryDeserializeWithStrategy(testArray, ArrayList.class));
            assertNotNull(engine.tryDeserializeWithStrategy(testArray, LinkedList.class));
            
            // Map 타입들
            assertNotNull(engine.tryDeserializeWithStrategy(testJson, HashMap.class));
            assertNotNull(engine.tryDeserializeWithStrategy(testJson, TreeMap.class));
        }
    }
    
    @Nested
    @DisplayName("에러 처리 및 예외 상황 검증")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("null 입력 처리")
        void testNullInputHandling() {
            // null JSON5Element
            assertNull(engine.tryDeserializeWithStrategy(null, String.class));
            
            // null 타입
            assertNull(engine.tryDeserializeWithStrategy(testJson, null));
            
            // 둘 다 null
            assertNull(engine.tryDeserializeWithStrategy(null, null));
        }
        
        @Test
        @DisplayName("잘못된 전략 등록 처리")
        void testInvalidStrategyRegistration() {
            // null 전략 등록 시 예외 발생 확인
            assertThrows(IllegalArgumentException.class, () -> {
                factory.registerStrategy(null);
            });
        }
        
        @Test
        @DisplayName("전략 실행 중 예외 처리")
        void testStrategyExecutionException() {
            // 예외를 발생시키는 전략 등록
            engine.registerStrategy(new ExceptionThrowingStrategy());
            
            // 예외가 발생해도 엔진이 안전하게 처리하는지 확인
            assertDoesNotThrow(() -> {
                Object result = engine.tryDeserializeWithStrategy(testJson, String.class);
                assertNull(result); // 예외 발생 시 null 반환
            });
        }
    }
    
    // 테스트용 Helper 메소드들
    private DeserializationContext createMockContext() {
        TypeSchema mockSchema = TypeSchemaMap.getInstance().getTypeInfo(String.class);
        return new DeserializationContext("test", testJson, mockSchema);
    }
    
    // 테스트용 Strategy 구현체들
    private static class TestDeserializationStrategy implements DeserializationStrategy {
        @Override
        public boolean canHandle(Types type, Class<?> targetType) {
            return type == Types.String && targetType == String.class;
        }
        
        @Override
        public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
            return "test-result";
        }
        
        @Override
        public int getPriority() {
            return 100;
        }
    }
    
    private static class HighPriorityTestStrategy implements DeserializationStrategy {
        @Override
        public boolean canHandle(Types type, Class<?> targetType) {
            return type == Types.String && targetType == String.class;
        }
        
        @Override
        public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
            return "high-priority-result";
        }
        
        @Override
        public int getPriority() {
            return 1; // 최고 우선순위
        }
    }
    
    private static class FailingDeserializationStrategy implements DeserializationStrategy {
        @Override
        public boolean canHandle(Types type, Class<?> targetType) {
            return type == Types.String && targetType == String.class;
        }
        
        @Override
        public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
            throw new RuntimeException("Intentional failure for testing");
        }
        
        @Override
        public int getPriority() {
            return 1;
        }
    }
    
    private static class ExceptionThrowingStrategy implements DeserializationStrategy {
        @Override
        public boolean canHandle(Types type, Class<?> targetType) {
            return true; // 모든 타입 처리한다고 주장
        }
        
        @Override
        public Object deserialize(JSON5Element json5Element, Class<?> targetType, DeserializationContext context) {
            throw new RuntimeException("Strategy execution failed");
        }
        
        @Override
        public int getPriority() {
            return 1;
        }
    }
}
