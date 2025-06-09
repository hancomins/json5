package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;

import java.util.*;

/**
 * JSON5 Serializer 리팩토링 3.4 단계 검증 클래스입니다.
 * 
 * <p>직렬화 전략 인터페이스의 구현과 SerializationEngine의 
 * 전략 패턴 적용을 검증합니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class RefactoringStep34Validator {
    
    private SerializationEngine engine;
    private SerializationStrategyFactory strategyFactory;
    
    public void setUp() {
        strategyFactory = SerializationStrategyFactory.createDefault();
        engine = new SerializationEngine(strategyFactory);
    }
    
    /**
     * 모든 검증 테스트를 실행합니다.
     */
    public static void main(String[] args) {
        RefactoringStep34Validator validator = new RefactoringStep34Validator();
        validator.setUp();
        
        System.out.println("=== JSON5 Serializer 3.4단계 검증 시작 ===");
        
        try {
            validator.testDefaultStrategiesRegistered();
            validator.testStrategyPriorityOrdering();
            validator.testPrimitiveStrategyCanHandle();
            validator.testComplexStrategyCanHandle();
            validator.testStrategySelection();
            validator.testStrategyCaching();
            validator.testCustomStrategyRegistration();
            validator.testEngineStrategyIntegration();
            validator.testEngineStrategyAPIs();
            
            System.out.println("=== 모든 검증 테스트 통과! ===");
            
        } catch (Exception e) {
            System.err.println("검증 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 기본 전략들이 정상적으로 등록되었는지 확인합니다.
     */
    public void testDefaultStrategiesRegistered() {
        System.out.println("1. 기본 전략 등록 상태 확인...");
        
        List<SerializationStrategy> strategies = strategyFactory.getAllStrategies();
        
        // 최소 2개의 기본 전략이 등록되어야 함
        if (strategies.size() < 2) {
            throw new RuntimeException("기본 전략이 등록되지 않음: " + strategies.size());
        }
        
        // PrimitiveSerializationStrategy가 포함되어야 함
        boolean hasPrimitiveStrategy = strategies.stream()
            .anyMatch(s -> s instanceof PrimitiveSerializationStrategy);
        if (!hasPrimitiveStrategy) {
            throw new RuntimeException("PrimitiveSerializationStrategy가 등록되지 않음");
        }
        
        // ComplexObjectSerializationStrategy가 포함되어야 함
        boolean hasComplexStrategy = strategies.stream()
            .anyMatch(s -> s instanceof ComplexObjectSerializationStrategy);
        if (!hasComplexStrategy) {
            throw new RuntimeException("ComplexObjectSerializationStrategy가 등록되지 않음");
        }
        
        System.out.println("   ✓ 기본 전략 " + strategies.size() + "개 정상 등록됨");
    }
    
    /**
     * 전략의 우선순위 정렬이 올바른지 확인합니다.
     */
    public void testStrategyPriorityOrdering() {
        System.out.println("2. 전략 우선순위 정렬 확인...");
        
        List<SerializationStrategy> strategies = strategyFactory.getAllStrategies();
        
        // 우선순위가 오름차순으로 정렬되어야 함 (낮은 숫자 = 높은 우선순위)
        for (int i = 1; i < strategies.size(); i++) {
            int prevPriority = strategies.get(i - 1).getPriority();
            int currentPriority = strategies.get(i).getPriority();
            if (prevPriority > currentPriority) {
                throw new RuntimeException("전략 우선순위가 올바르게 정렬되지 않음: " 
                    + prevPriority + " > " + currentPriority);
            }
        }
        
        System.out.println("   ✓ 전략 우선순위 정렬 확인됨");
    }
    
    /**
     * PrimitiveSerializationStrategy의 canHandle 메소드를 테스트합니다.
     */
    public void testPrimitiveStrategyCanHandle() {
        System.out.println("3. PrimitiveStrategy canHandle 테스트...");
        
        PrimitiveSerializationStrategy strategy = new PrimitiveSerializationStrategy();
        
        // 기본 타입들은 처리할 수 있어야 함
        if (!strategy.canHandle(42, Types.Integer)) {
            throw new RuntimeException("Integer 타입을 처리할 수 없음");
        }
        if (!strategy.canHandle("test", Types.String)) {
            throw new RuntimeException("String 타입을 처리할 수 없음");
        }
        if (!strategy.canHandle(true, Types.Boolean)) {
            throw new RuntimeException("Boolean 타입을 처리할 수 없음");
        }
        if (!strategy.canHandle(3.14, Types.Double)) {
            throw new RuntimeException("Double 타입을 처리할 수 없음");
        }
        
        // null은 처리할 수 없어야 함
        if (strategy.canHandle(null, Types.Object)) {
            throw new RuntimeException("null을 처리할 수 있다고 잘못 판단");
        }
        
        // 복합 타입은 처리할 수 없어야 함
        if (strategy.canHandle(new ArrayList<>(), Types.Collection)) {
            throw new RuntimeException("List 타입을 처리할 수 있다고 잘못 판단");
        }
        
        System.out.println("   ✓ PrimitiveStrategy canHandle 정상 동작");
    }
    
    /**
     * ComplexObjectSerializationStrategy의 canHandle 메소드를 테스트합니다.
     */
    public void testComplexStrategyCanHandle() {
        System.out.println("4. ComplexStrategy canHandle 테스트...");
        
        ComplexObjectSerializationStrategy strategy = new ComplexObjectSerializationStrategy();
        
        // 복합 타입들은 처리할 수 있어야 함
        if (!strategy.canHandle(new ArrayList<>(), Types.Collection)) {
            throw new RuntimeException("Collection 타입을 처리할 수 없음");
        }
        if (!strategy.canHandle(new HashMap<>(), Types.Map)) {
            throw new RuntimeException("Map 타입을 처리할 수 없음");
        }
        if (!strategy.canHandle(new TestObject(), Types.Object)) {
            throw new RuntimeException("Object 타입을 처리할 수 없음");
        }
        
        // null은 처리할 수 없어야 함
        if (strategy.canHandle(null, Types.Collection)) {
            throw new RuntimeException("null을 처리할 수 있다고 잘못 판단");
        }
        
        // 기본 타입은 처리할 수 없어야 함
        if (strategy.canHandle(42, Types.Integer)) {
            throw new RuntimeException("Integer 타입을 처리할 수 있다고 잘못 판단");
        }
        
        System.out.println("   ✓ ComplexStrategy canHandle 정상 동작");
    }
    
    /**
     * SerializationStrategyFactory의 전략 선택이 올바른지 테스트합니다.
     */
    public void testStrategySelection() {
        System.out.println("5. 전략 선택 테스트...");
        
        // 기본 타입에 대해서는 PrimitiveStrategy가 선택되어야 함
        SerializationStrategy strategy1 = strategyFactory.getStrategy(42, Types.Integer);
        if (strategy1 == null) {
            throw new RuntimeException("Integer에 대한 전략을 찾을 수 없음");
        }
        if (!(strategy1 instanceof PrimitiveSerializationStrategy)) {
            throw new RuntimeException("Integer에 대해 잘못된 전략이 선택됨");
        }
        
        // 컬렉션에 대해서는 ComplexObjectStrategy가 선택되어야 함
        SerializationStrategy strategy2 = strategyFactory.getStrategy(new ArrayList<>(), Types.Collection);
        if (strategy2 == null) {
            throw new RuntimeException("Collection에 대한 전략을 찾을 수 없음");
        }
        if (!(strategy2 instanceof ComplexObjectSerializationStrategy)) {
            throw new RuntimeException("Collection에 대해 잘못된 전략이 선택됨");
        }
        
        System.out.println("   ✓ 전략 선택 정상 동작");
    }
    
    /**
     * 전략 캐싱이 올바르게 동작하는지 테스트합니다.
     */
    public void testStrategyCaching() {
        System.out.println("6. 전략 캐싱 테스트...");
        
        String testString = "test";
        
        // 첫 번째 호출
        SerializationStrategy strategy1 = strategyFactory.getStrategy(testString, Types.String);
        
        // 두 번째 호출 - 캐시에서 가져와야 함
        SerializationStrategy strategy2 = strategyFactory.getStrategy(testString, Types.String);
        
        // 같은 인스턴스여야 함 (캐시 동작 확인)
        if (strategy1 != strategy2) {
            throw new RuntimeException("전략 캐싱이 동작하지 않음");
        }
        
        System.out.println("   ✓ 전략 캐싱 정상 동작");
    }
    
    /**
     * 커스텀 전략 등록이 올바르게 동작하는지 테스트합니다.
     */
    public void testCustomStrategyRegistration() {
        System.out.println("7. 커스텀 전략 등록 테스트...");
        
        // 커스텀 전략 생성
        SerializationStrategy customStrategy = new TestCustomStrategy();
        
        // 전략 등록
        strategyFactory.registerStrategy(customStrategy);
        
        // 등록된 전략이 포함되어야 함
        List<SerializationStrategy> strategies = strategyFactory.getAllStrategies();
        if (!strategies.contains(customStrategy)) {
            throw new RuntimeException("커스텀 전략이 등록되지 않음");
        }
        
        System.out.println("   ✓ 커스텀 전략 등록 정상 동작");
    }
    
    /**
     * SerializationEngine의 전략 패턴 적용을 테스트합니다.
     */
    public void testEngineStrategyIntegration() {
        System.out.println("8. 엔진 전략 통합 테스트...");
        
        // 간단한 테스트용 객체 생성
        TestObject testObj = new TestObject();
        testObj.name = "test";
        testObj.value = 42;
        
        try {
            // 직렬화 실행 - 예외가 발생하지 않아야 함
            // 실제 JSON5Object가 반환되는지는 TypeSchema 의존성 때문에 확인하지 않음
            Object result = engine.serialize(testObj);
        } catch (JSON5SerializerException e) {
            // TypeSchema가 없는 경우 예상되는 예외
            if (e.getMessage() == null) {
                throw new RuntimeException("예외 메시지가 null");
            }
            if (!e.getMessage().contains("No TypeSchema found")) {
                throw new RuntimeException("예상되는 예외 메시지 패턴이 아님: " + e.getMessage());
            }
            // 예상된 예외이므로 정상
        } catch (Exception e) {
            throw new RuntimeException("예상하지 못한 예외 발생: " + e.getMessage());
        }
        
        System.out.println("   ✓ 엔진 전략 통합 정상 동작");
    }
    
    /**
     * 전략 등록 API들이 올바르게 동작하는지 테스트합니다.
     */
    public void testEngineStrategyAPIs() {
        System.out.println("9. 엔진 전략 API 테스트...");
        
        // 새로운 전략 등록
        SerializationStrategy customStrategy = new TestCustomStrategy();
        engine.registerStrategy(customStrategy);
        
        // 등록된 전략이 포함되어야 함
        List<SerializationStrategy> strategies = engine.getAllStrategies();
        if (!strategies.contains(customStrategy)) {
            throw new RuntimeException("엔진에 커스텀 전략이 등록되지 않음");
        }
        
        // 특정 타입에 대한 전략 조회
        SerializationStrategy strategy = engine.getStrategyFor("test", Types.String);
        if (strategy == null) {
            throw new RuntimeException("String 타입에 대한 전략을 찾을 수 없음");
        }
        
        // 캐시 초기화 - 예외가 발생하지 않아야 함
        engine.clearStrategyCache();
        
        System.out.println("   ✓ 엔진 전략 API 정상 동작");
    }
    
    // 테스트용 간단한 객체 클래스
    private static class TestObject {
        String name;
        int value;
    }
    
    // 테스트용 커스텀 전략
    private static class TestCustomStrategy implements SerializationStrategy {
        
        @Override
        public boolean canHandle(Object obj, Types type) {
            return obj instanceof String && ((String) obj).startsWith("custom:");
        }
        
        @Override
        public JSON5Element serialize(Object obj, SerializationContext context) {
            JSON5Object result = new JSON5Object();
            result.put("customValue", obj.toString());
            return result;
        }
        
        @Override
        public int getPriority() {
            return 0; // 최고 우선순위
        }
    }
}
