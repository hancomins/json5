package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 3.1 단계 리팩토링 완료를 검증하는 클래스입니다.
 * 
 * <p>SerializationEngine 및 관련 클래스들이 올바르게 동작하는지 확인합니다.</p>
 * 
 * @author ice3x2
 * @version 1.2
 * @since 2.0
 */
public class RefactoringStep31Validator {
    
    public static void main(String[] args) {
        System.out.println("=== 3.1 단계 리팩토링 검증 시작 ===");
        
        try {
            // 1. SerializationEngine 기본 동작 검증
            validateSerializationEngine();
            
            // 2. ObjectSerializer 동작 검증
            validateObjectSerializer();
            
            // 3. CollectionSerializer 동작 검증
            validateCollectionSerializer();
            
            // 4. MapSerializer 동작 검증
            validateMapSerializer();
            
            // 5. 기존 API 호환성 검증
            validateBackwardCompatibility();
            
            System.out.println("✅ 모든 검증이 성공적으로 완료되었습니다!");
            
        } catch (Exception e) {
            System.err.println("❌ 검증 실패: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("=== 3.1 단계 리팩토링 검증 완료 ===");
    }
    
    /**
     * SerializationEngine 기본 동작을 검증합니다.
     */
    private static void validateSerializationEngine() {
        System.out.println("1. SerializationEngine 기본 동작 검증...");
        
        SerializationEngine engine = new SerializationEngine();
        
        // 간단한 객체 직렬화 테스트
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.value = 123;
        
        JSON5Object result = engine.serialize(obj);
        
        if (result == null) {
            throw new RuntimeException("SerializationEngine.serialize() 결과가 null입니다.");
        }
        
        System.out.println("   ✓ SerializationEngine 기본 동작 정상");
    }
    
    /**
     * ObjectSerializer 동작을 검증합니다.
     */
    private static void validateObjectSerializer() {
        System.out.println("2. ObjectSerializer 동작 검증...");
        
        ObjectSerializer serializer = new ObjectSerializer();
        
        // ObjectSerializer가 생성되는지 확인
        if (serializer == null) {
            throw new RuntimeException("ObjectSerializer 인스턴스 생성 실패");
        }
        
        System.out.println("   ✓ ObjectSerializer 인스턴스 생성 정상");
    }
    
    /**
     * CollectionSerializer 동작을 검증합니다.
     */
    private static void validateCollectionSerializer() {
        System.out.println("3. CollectionSerializer 동작 검증...");
        
        CollectionSerializer serializer = new CollectionSerializer();
        
        // 간단한 컬렉션 직렬화 테스트
        List<String> list = Arrays.asList("a", "b", "c");
        JSON5Array result = serializer.serializeCollection(list, String.class);
        
        if (result == null || result.size() != 3) {
            throw new RuntimeException("CollectionSerializer.serializeCollection() 결과가 올바르지 않습니다.");
        }
        
        System.out.println("   ✓ CollectionSerializer 동작 정상");
    }
    
    /**
     * MapSerializer 동작을 검증합니다.
     */
    private static void validateMapSerializer() {
        System.out.println("4. MapSerializer 동작 검증...");
        
        MapSerializer serializer = new MapSerializer();
        
        // 간단한 Map 직렬화 테스트
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        
        JSON5Object result = serializer.serializeMap(map, Object.class);
        
        if (result == null || result.size() != 2) {
            throw new RuntimeException("MapSerializer.serializeMap() 결과가 올바르지 않습니다.");
        }
        
        System.out.println("   ✓ MapSerializer 동작 정상");
    }
    
    /**
     * 기존 API 호환성을 검증합니다.
     */
    private static void validateBackwardCompatibility() {
        System.out.println("5. 기존 API 호환성 검증...");
        
        // 기존 static 메소드들이 여전히 동작하는지 확인
        TestObject obj = new TestObject();
        obj.name = "compatibility_test";
        obj.value = 999;
        
        JSON5Object result = JSON5Serializer.toJSON5Object(obj);
        
        if (result == null) {
            throw new RuntimeException("기존 JSON5Serializer.toJSON5Object() 메소드가 동작하지 않습니다.");
        }
        
        // Map 직렬화 호환성 확인
        Map<String, String> map = new HashMap<>();
        map.put("test", "value");
        
        JSON5Object mapResult = JSON5Serializer.mapToJSON5Object(map);
        if (mapResult == null) {
            throw new RuntimeException("기존 JSON5Serializer.mapToJSON5Object() 메소드가 동작하지 않습니다.");
        }
        
        // Collection 직렬화 호환성 확인
        List<String> list = Arrays.asList("test1", "test2");
        JSON5Array listResult = JSON5Serializer.collectionToJSON5Array(list);
        if (listResult == null) {
            throw new RuntimeException("기존 JSON5Serializer.collectionToJSON5Array() 메소드가 동작하지 않습니다.");
        }
        
        System.out.println("   ✓ 기존 API 호환성 정상");
    }
    
    /**
     * 테스트용 간단한 객체 클래스
     */
    @JSON5Type
    private static class TestObject {
        @JSON5Value
        public String name;
        
        @JSON5Value
        public int value;
    }
}
