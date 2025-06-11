package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PolymorphicDeserializer의 통합 테스트 클래스입니다.
 */
@DisplayName("다형성 역직렬화 통합 테스트")
class PolymorphicDeserializerTest {
    
    private PolymorphicDeserializer deserializer;
    
    @BeforeEach
    void setUp() {
        deserializer = new PolymorphicDeserializer();
    }
    
    @Test
    @DisplayName("기본 다형성 역직렬화가 작동해야 함")
    void shouldDeserializeBasicPolymorphicType() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("type", "dog");
        json.put("name", "Buddy");
        json.put("breed", "Golden Retriever");
        
        // When
        Animal result = deserializer.deserialize(json, Animal.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Dog.class, result);
        assertEquals("Buddy", result.name);
        assertEquals("Golden Retriever", ((Dog) result).breed);
    }
    
    @Test
    @DisplayName("다른 서브타입도 올바르게 역직렬화되어야 함")
    void shouldDeserializeDifferentSubTypes() {
        // Given
        JSON5Object catJson = new JSON5Object();
        catJson.put("type", "cat");
        catJson.put("name", "Whiskers");
        catJson.put("indoor", true);
        
        // When
        Animal result = deserializer.deserialize(catJson, Animal.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Cat.class, result);
        assertEquals("Whiskers", result.name);
        assertTrue(((Cat) result).indoor);
    }
    
    @Test
    @DisplayName("중첩된 타입 경로를 처리해야 함")
    void shouldHandleNestedTypePath() {
        // Given
        JSON5Object json = new JSON5Object();
        JSON5Object vehicle = new JSON5Object();
        vehicle.put("type", "car");
        json.put("vehicle", vehicle);
        json.put("brand", "Toyota");
        json.put("doors", 4);
        
        // When
        Vehicle result = deserializer.deserialize(json, Vehicle.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Car.class, result);
        assertEquals("Toyota", ((Car) result).brand);
        assertEquals(4, ((Car) result).doors);
    }
    
    @Test
    @DisplayName("기본 구현체를 사용해야 함")
    void shouldUseDefaultImplementation() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("amount", 100.0);
        // type 정보가 없음
        
        // When
        Payment result = deserializer.deserialize(json, Payment.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(GenericPayment.class, result);
        assertEquals(100.0, ((GenericPayment) result).amount);
    }
    
    @Test
    @DisplayName("알 수 없는 타입에 대해 예외를 발생시켜야 함")
    void shouldThrowExceptionForUnknownType() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("type", "unknown");
        json.put("name", "Test");
        
        // When & Then
        assertThrows(JSON5SerializerException.class, () -> {
            deserializer.deserialize(json, Animal.class);
        });
    }
    
    @Test
    @DisplayName("다형성 타입이 아닌 클래스에 대해 예외를 발생시켜야 함")
    void shouldThrowExceptionForNonPolymorphicType() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "Test");
        
        // When & Then
        assertThrows(JSON5SerializerException.class, () -> {
            deserializer.deserialize(json, NonPolymorphicClass.class);
        });
    }
    
    @Test
    @DisplayName("null JSON 객체에 대해 null을 반환해야 함")
    void shouldReturnNullForNullJson() {
        // When
        Animal result = deserializer.deserialize(null, Animal.class);
        
        // Then
        assertNull(result);
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
    
    @JSON5TypeInfo(property = "vehicle.type")
    @JSON5SubType(value = Car.class, name = "car")
    @JSON5SubType(value = Motorcycle.class, name = "motorcycle")
    public interface Vehicle {
        void start();
    }
    
    @JSON5Type
    public static class Car implements Vehicle {
        @JSON5Value
        private String brand;
        
        @JSON5Value
        private int doors;
        
        @Override
        public void start() {
            System.out.println("Car started");
        }
    }
    
    @JSON5Type
    public static class Motorcycle implements Vehicle {
        @JSON5Value
        private String model;
        
        @Override
        public void start() {
            System.out.println("Motorcycle started");
        }
    }
    
    @JSON5TypeInfo(
        property = "type", 
        defaultImpl = GenericPayment.class,
        onMissingType = MissingTypeStrategy.DEFAULT_IMPL
    )
    @JSON5SubType(value = CreditCardPayment.class, name = "credit")
    @JSON5SubType(value = PayPalPayment.class, name = "paypal")
    public interface Payment {
        void process();
    }
    
    @JSON5Type
    public static class GenericPayment implements Payment {
        @JSON5Value
        private double amount;
        
        @Override
        public void process() {
            System.out.println("Generic payment processed");
        }
    }
    
    @JSON5Type
    public static class CreditCardPayment implements Payment {
        @JSON5Value
        private String cardNumber;
        
        @Override
        public void process() {
            System.out.println("Credit card payment processed");
        }
    }
    
    @JSON5Type
    public static class PayPalPayment implements Payment {
        @JSON5Value
        private String email;
        
        @Override
        public void process() {
            System.out.println("PayPal payment processed");
        }
    }
    
    // 다형성 타입이 아닌 클래스
    @JSON5Type
    public static class NonPolymorphicClass {
        @JSON5Value
        private String name;
    }
}
