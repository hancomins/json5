package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;

/**
 * 다형성 역직렬화 기능의 데모 프로그램입니다.
 */
public class PolymorphicDemo {
    
    public static void main(String[] args) {
        System.out.println("=== JSON5 Serializer 다형성 역직렬화 데모 ===\n");
        
        // 1. TypeInfoAnalyzer 테스트
        System.out.println("1. TypeInfoAnalyzer 테스트");
        testTypeInfoAnalyzer();
        
        // 2. SubTypeRegistry 테스트  
        System.out.println("\n2. SubTypeRegistry 테스트");
        testSubTypeRegistry();
        
        // 3. PolymorphicDeserializer 테스트
        System.out.println("\n3. PolymorphicDeserializer 테스트");
        testPolymorphicDeserializer();
        
        // 4. 통합 테스트
        System.out.println("\n4. JSON5Serializer 통합 테스트");
        testIntegration();
        
        System.out.println("\n=== 데모 완료 ===");
    }
    
    private static void testTypeInfoAnalyzer() {
        TypeInfoAnalyzer analyzer = new TypeInfoAnalyzer();
        
        System.out.println("- Animal 클래스 다형성 여부: " + analyzer.isPolymorphicType(Animal.class));
        System.out.println("- Dog 클래스 다형성 여부: " + analyzer.isPolymorphicType(Dog.class));
        System.out.println("- String 클래스 다형성 여부: " + analyzer.isPolymorphicType(String.class));
        
        TypeInfo typeInfo = analyzer.analyzeTypeInfo(Animal.class);
        if (typeInfo != null) {
            System.out.println("- Animal TypeInfo: " + typeInfo);
            System.out.println("- 지원되는 타입 이름들: " + typeInfo.getSupportedTypeNames());
        }
    }
    
    private static void testSubTypeRegistry() {
        SubTypeRegistry registry = new SubTypeRegistry();
        
        TypeInfo typeInfo = registry.getTypeInfo(Animal.class);
        System.out.println("- Animal TypeInfo 조회: " + (typeInfo != null ? "성공" : "실패"));
        
        Class<?> dogType = registry.resolveConcreteType(Animal.class, "dog");
        Class<?> catType = registry.resolveConcreteType(Animal.class, "cat");
        Class<?> unknownType = registry.resolveConcreteType(Animal.class, "unknown");
        
        System.out.println("- 'dog' 타입 해결: " + (dogType == Dog.class ? "성공" : "실패"));
        System.out.println("- 'cat' 타입 해결: " + (catType == Cat.class ? "성공" : "실패"));
        System.out.println("- 'unknown' 타입 해결: " + (unknownType == null ? "성공 (null 반환)" : "실패"));
        
        System.out.println("- 캐시 크기: " + registry.getCacheSize());
    }
    
    private static void testPolymorphicDeserializer() {
        PolymorphicDeserializer deserializer = new PolymorphicDeserializer();
        
        // Dog JSON 생성
        JSON5Object dogJson = new JSON5Object();
        dogJson.put("type", "dog");
        dogJson.put("name", "Buddy");
        dogJson.put("breed", "Golden Retriever");
        
        // Cat JSON 생성
        JSON5Object catJson = new JSON5Object();
        catJson.put("type", "cat");
        catJson.put("name", "Whiskers");
        catJson.put("indoor", true);
        
        try {
            Animal dog = deserializer.deserialize(dogJson, Animal.class);
            System.out.println("- Dog 역직렬화: " + (dog instanceof Dog ? "성공" : "실패"));
            if (dog instanceof Dog) {
                System.out.println("  이름: " + dog.name + ", 품종: " + ((Dog) dog).breed);
            }
        } catch (Exception e) {
            System.out.println("- Dog 역직렬화 실패: " + e.getMessage());
        }
        
        try {
            Animal cat = deserializer.deserialize(catJson, Animal.class);
            System.out.println("- Cat 역직렬화: " + (cat instanceof Cat ? "성공" : "실패"));
            if (cat instanceof Cat) {
                System.out.println("  이름: " + cat.name + ", 실내: " + ((Cat) cat).indoor);
            }
        } catch (Exception e) {
            System.out.println("- Cat 역직렬화 실패: " + e.getMessage());
        }
    }
    
    private static void testIntegration() {
        // DeserializationEngine 통합 테스트
        DeserializationEngine engine = new DeserializationEngine();
        
        JSON5Object dogJson = new JSON5Object();
        dogJson.put("type", "dog");
        dogJson.put("name", "Rex");
        dogJson.put("breed", "German Shepherd");
        
        try {
            Animal result = engine.deserialize(dogJson, Animal.class);
            System.out.println("- DeserializationEngine 테스트: " + (result instanceof Dog ? "성공" : "실패"));
            if (result instanceof Dog) {
                System.out.println("  이름: " + result.name + ", 품종: " + ((Dog) result).breed);
            }
        } catch (Exception e) {
            System.out.println("- DeserializationEngine 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 테스트용 클래스들
    @JSON5TypeInfo(property = "type")
    @JSON5SubType(value = Dog.class, name = "dog")
    @JSON5SubType(value = Cat.class, name = "cat")
    public abstract static class Animal {
        @JSON5Value
        protected String name;
        
        public abstract void makeSound();
    }
    
    @JSON5Type
    public static class Dog extends Animal {
        @JSON5Value
        private String breed;
        
        @Override
        public void makeSound() {
            System.out.println("Woof!");
        }
    }
    
    @JSON5Type
    public static class Cat extends Animal {
        @JSON5Value
        private boolean indoor;
        
        @Override
        public void makeSound() {
            System.out.println("Meow!");
        }
    }
}
