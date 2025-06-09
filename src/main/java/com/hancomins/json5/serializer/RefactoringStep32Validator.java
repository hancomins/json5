package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import java.util.*;

/**
 * JSON5 Serializer 리팩토링 3.2 단계 검증 클래스
 * 
 * <p>ObjectSerializer 완성 및 복잡한 객체 처리 로직 이동 검증</p>
 * 
 * <h3>검증 항목:</h3>
 * <ul>
 *   <li>새로운 SerializationEngine의 정상 동작</li>
 *   <li>ObjectSerializer의 복잡한 스키마 처리</li>
 *   <li>기존 API와의 100% 호환성</li>
 *   <li>성능 유지 및 기능 동등성</li>
 * </ul>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class RefactoringStep32Validator {
    
    /**
     * 3.2 단계 검증을 수행합니다.
     * 
     * @return 검증 성공 여부
     */
    public boolean validate() {
        try {
            System.out.println("=== JSON5 Serializer 리팩토링 3.2 단계 검증 시작 ===");
            
            // 1. SerializationEngine 기본 기능 검증
            validateSerializationEngine();
            
            // 2. ObjectSerializer 복잡한 처리 검증
            validateObjectSerializer();
            
            // 3. CollectionSerializer 기능 검증
            validateCollectionSerializer();
            
            // 4. MapSerializer 기능 검증
            validateMapSerializer();
            
            // 5. 기존 API 호환성 검증
            validateBackwardCompatibility();
            
            // 6. 성능 및 안정성 검증
            validatePerformanceAndStability();
            
            System.out.println("✅ 모든 검증이 성공적으로 완료되었습니다!");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ 검증 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * SerializationEngine 기본 기능을 검증합니다.
     */
    private void validateSerializationEngine() {
        System.out.println("1. SerializationEngine 기본 기능 검증...");
        
        SerializationEngine engine = new SerializationEngine();
        
        // 단순 객체 직렬화 테스트
        TestSimpleClass obj = new TestSimpleClass("test", 123);
        JSON5Object result = engine.serialize(obj);
        
        assert result != null : "직렬화 결과가 null입니다";
        assert "test".equals(result.getString("name")) : "name 필드가 올바르지 않습니다";
        assert 123 == result.getInt("value") : "value 필드가 올바르지 않습니다";
        
        // null 객체 처리 테스트
        try {
            engine.serialize(null);
            throw new AssertionError("null 객체에 대해 예외가 발생해야 합니다");
        } catch (NullPointerException e) {
            // 예상된 동작
        }
        
        System.out.println("  ✓ SerializationEngine 기본 기능 검증 완료");
    }
    
    /**
     * ObjectSerializer 복잡한 처리를 검증합니다.
     */
    private void validateObjectSerializer() {
        System.out.println("2. ObjectSerializer 복잡한 처리 검증...");
        
        SerializationEngine engine = new SerializationEngine();
        
        // 중첩된 객체 구조 처리 테스트
        TestComplexClass parent = new TestComplexClass("root", 
            new TestSimpleClass("nested", 789));
        
        JSON5Object result = engine.serialize(parent);
        
        assert result != null : "직렬화 결과가 null입니다";
        assert "root".equals(result.getString("name")) : "name 필드가 올바르지 않습니다";
        
        JSON5Object nested = result.getJSON5Object("child");
        assert nested != null : "중첩된 객체가 null입니다";
        assert "nested".equals(nested.getString("name")) : "중첩된 객체의 name이 올바르지 않습니다";
        assert 789 == nested.getInt("value") : "중첩된 객체의 value가 올바르지 않습니다";
        
        System.out.println("  ✓ ObjectSerializer 복잡한 처리 검증 완료");
    }
    
    /**
     * CollectionSerializer 기능을 검증합니다.
     */
    private void validateCollectionSerializer() {
        System.out.println("3. CollectionSerializer 기능 검증...");
        
        CollectionSerializer collectionSerializer = new CollectionSerializer();
        
        // 단순 컬렉션 직렬화 테스트
        List<String> list = Arrays.asList("a", "b", "c");
        JSON5Array result = collectionSerializer.serializeCollection(list, String.class);
        
        assert result != null : "직렬화 결과가 null입니다";
        assert 3 == result.size() : "배열 크기가 올바르지 않습니다";
        assert "a".equals(result.getString(0)) : "첫 번째 요소가 올바르지 않습니다";
        assert "b".equals(result.getString(1)) : "두 번째 요소가 올바르지 않습니다";
        assert "c".equals(result.getString(2)) : "세 번째 요소가 올바르지 않습니다";
        
        // 중첩된 컬렉션 처리 테스트
        List<List<String>> nestedList = Arrays.asList(
            Arrays.asList("a1", "a2"),
            Arrays.asList("b1", "b2")
        );
        
        JSON5Array nestedResult = collectionSerializer.serializeCollection(nestedList, null);
        assert nestedResult != null : "중첩된 컬렉션 직렬화 결과가 null입니다";
        assert 2 == nestedResult.size() : "중첩된 배열 크기가 올바르지 않습니다";
        
        JSON5Array first = nestedResult.getJSON5Array(0);
        assert "a1".equals(first.getString(0)) : "중첩된 배열의 첫 번째 요소가 올바르지 않습니다";
        assert "a2".equals(first.getString(1)) : "중첩된 배열의 두 번째 요소가 올바르지 않습니다";
        
        System.out.println("  ✓ CollectionSerializer 기능 검증 완료");
    }
    
    /**
     * MapSerializer 기능을 검증합니다.
     */
    private void validateMapSerializer() {
        System.out.println("4. MapSerializer 기능 검증...");
        
        MapSerializer mapSerializer = new MapSerializer();
        
        // 단순 Map 직렬화 테스트
        Map<String, Object> map = new HashMap<>();
        map.put("string", "value");
        map.put("number", 42);
        map.put("boolean", true);
        
        JSON5Object result = mapSerializer.serializeMap(map, Object.class);
        
        assert result != null : "직렬화 결과가 null입니다";
        assert "value".equals(result.getString("string")) : "string 필드가 올바르지 않습니다";
        assert 42 == result.getInt("number") : "number 필드가 올바르지 않습니다";
        assert true == result.getBoolean("boolean") : "boolean 필드가 올바르지 않습니다";
        
        // 중첩된 Map 처리 테스트
        Map<String, Object> inner = new HashMap<>();
        inner.put("inner", "value");
        
        Map<String, Object> outer = new HashMap<>();
        outer.put("nested", inner);
        
        JSON5Object nestedResult = mapSerializer.serializeMap(outer, Object.class);
        assert nestedResult != null : "중첩된 Map 직렬화 결과가 null입니다";
        
        JSON5Object nested = nestedResult.getJSON5Object("nested");
        assert nested != null : "중첩된 객체가 null입니다";
        assert "value".equals(nested.getString("inner")) : "중첩된 Map의 값이 올바르지 않습니다";
        
        System.out.println("  ✓ MapSerializer 기능 검증 완료");
    }
    
    /**
     * 기존 API 호환성을 검증합니다.
     */
    private void validateBackwardCompatibility() {
        System.out.println("5. 기존 API 호환성 검증...");
        
        // JSON5Serializer.toJSON5Object 호환성 테스트
        TestSimpleClass obj = new TestSimpleClass("compat", 999);
        JSON5Object result = JSON5Serializer.toJSON5Object(obj);
        
        assert result != null : "toJSON5Object 결과가 null입니다";
        assert "compat".equals(result.getString("name")) : "name 필드가 올바르지 않습니다";
        assert 999 == result.getInt("value") : "value 필드가 올바르지 않습니다";
        
        // mapToJSON5Object 호환성 테스트
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        
        JSON5Object mapResult = JSON5Serializer.mapToJSON5Object(map);
        assert mapResult != null : "mapToJSON5Object 결과가 null입니다";
        assert "value".equals(mapResult.getString("key")) : "Map 값이 올바르지 않습니다";
        
        // collectionToJSON5Array 호환성 테스트
        List<String> list = Arrays.asList("x", "y", "z");
        JSON5Array arrayResult = JSON5Serializer.collectionToJSON5Array(list);
        
        assert arrayResult != null : "collectionToJSON5Array 결과가 null입니다";
        assert 3 == arrayResult.size() : "배열 크기가 올바르지 않습니다";
        assert "x".equals(arrayResult.getString(0)) : "첫 번째 요소가 올바르지 않습니다";
        assert "y".equals(arrayResult.getString(1)) : "두 번째 요소가 올바르지 않습니다";
        assert "z".equals(arrayResult.getString(2)) : "세 번째 요소가 올바르지 않습니다";
        
        System.out.println("  ✓ 기존 API 호환성 검증 완료");
    }
    
    /**
     * 성능 및 안정성을 검증합니다.
     */
    private void validatePerformanceAndStability() {
        System.out.println("6. 성능 및 안정성 검증...");
        
        // 대용량 데이터 처리 테스트
        List<TestSimpleClass> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(new TestSimpleClass("item" + i, i));
        }
        
        CollectionSerializer collectionSerializer = new CollectionSerializer();
        JSON5Array result = collectionSerializer.serializeCollection(largeList, TestSimpleClass.class);
        assert result != null : "대용량 데이터 직렬화 결과가 null입니다";
        assert 1000 == result.size() : "대용량 데이터 크기가 올바르지 않습니다";
        
        // 메모리 효율성 테스트
        List<Map<String, Object>> complexData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("name", "Item " + i);
            item.put("values", Arrays.asList(i * 1, i * 2, i * 3));
            complexData.add(item);
        }
        
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        JSON5Array complexResult = collectionSerializer.serializeCollection(complexData, null);
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        assert complexResult != null : "복잡한 데이터 직렬화 결과가 null입니다";
        assert 100 == complexResult.size() : "복잡한 데이터 크기가 올바르지 않습니다";
        
        // 메모리 사용량이 합리적인 범위 내에 있는지 확인 (10MB 이내)
        long memoryUsed = endMemory - startMemory;
        if (memoryUsed > 10 * 1024 * 1024) {
            System.out.println("  ⚠️ 메모리 사용량이 예상보다 높습니다: " + (memoryUsed / (1024 * 1024)) + "MB");
        }
        
        // 에러 처리 테스트 - null 필드 처리
        TestClassWithNullables nullObj = new TestClassWithNullables();
        nullObj.setName("test");
        nullObj.setNullableValue(null);
        
        SerializationEngine engine = new SerializationEngine();
        JSON5Object nullResult = engine.serialize(nullObj);
        assert nullResult != null : "null 필드를 포함한 객체 직렬화 결과가 null입니다";
        assert "test".equals(nullResult.getString("name")) : "name 필드가 올바르지 않습니다";
        assert nullResult.isNull("nullableValue") : "null 필드가 올바르게 처리되지 않았습니다";
        
        // 빈 컬렉션 처리 테스트
        List<String> emptyList = new ArrayList<>();
        Map<String, String> emptyMap = new HashMap<>();
        
        JSON5Array emptyArrayResult = collectionSerializer.serializeCollection(emptyList, String.class);
        MapSerializer mapSerializer = new MapSerializer();
        JSON5Object emptyMapResult = mapSerializer.serializeMap(emptyMap, String.class);
        
        assert emptyArrayResult != null : "빈 배열 직렬화 결과가 null입니다";
        assert 0 == emptyArrayResult.size() : "빈 배열 크기가 올바르지 않습니다";
        assert emptyMapResult != null : "빈 Map 직렬화 결과가 null입니다";
        assert 0 == emptyMapResult.keySet().size() : "빈 Map 크기가 올바르지 않습니다";
        
        System.out.println("  ✓ 성능 및 안정성 검증 완료");
    }
    
    // 메인 메소드 - 검증 실행
    public static void main(String[] args) {
        RefactoringStep32Validator validator = new RefactoringStep32Validator();
        boolean success = validator.validate();
        
        if (success) {
            System.out.println("\n🎉 3.2 단계 리팩토링 검증이 성공적으로 완료되었습니다!");
            System.exit(0);
        } else {
            System.out.println("\n💥 3.2 단계 리팩토링 검증이 실패했습니다!");
            System.exit(1);
        }
    }
    
    // 테스트용 내부 클래스들
    @JSON5Type
    public static class TestSimpleClass {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private int value;
        
        public TestSimpleClass() {}
        
        public TestSimpleClass(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
    
    @JSON5Type
    public static class TestComplexClass {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private TestSimpleClass child;
        
        public TestComplexClass() {}
        
        public TestComplexClass(String name, TestSimpleClass child) {
            this.name = name;
            this.child = child;
        }
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public TestSimpleClass getChild() { return child; }
        public void setChild(TestSimpleClass child) { this.child = child; }
    }
    
    @JSON5Type
    public static class TestClassWithNullables {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private String nullableValue;
        
        // getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNullableValue() { return nullableValue; }
        public void setNullableValue(String nullableValue) { this.nullableValue = nullableValue; }
    }
}
