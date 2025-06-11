package com.hancomins.json5.serializer.integration;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 다형성 역직렬화 기능의 통합 테스트입니다.
 * JSON5Serializer의 메인 API를 통해 다형성 역직렬화가 올바르게 작동하는지 검증합니다.
 */
@DisplayName("다형성 역직렬화 통합 테스트")
class PolymorphicIntegrationTest {
    
    @BeforeEach
    void setUp() {
        // 필요 시 설정 초기화
    }
    
    @Test
    @DisplayName("JSON5Serializer.fromJSON5Object()를 통한 다형성 역직렬화")
    void shouldDeserializePolymorphicTypeViaMainAPI() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("type", "dog");
        json.put("name", "Buddy");
        json.put("breed", "Golden Retriever");
        
        // When
        Animal result = JSON5Serializer.fromJSON5Object(json, Animal.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(Dog.class, result);
        assertEquals("Buddy", result.name);
        assertEquals("Golden Retriever", ((Dog) result).breed);
    }
    
    @Test
    @DisplayName("복잡한 중첩 구조에서의 다형성 역직렬화")
    void shouldHandleComplexNestedPolymorphicStructure() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "Pet Store");
        
        // animals 배열 생성
        com.hancomins.json5.JSON5Array animalsArray = new com.hancomins.json5.JSON5Array();
        
        // Dog 객체
        JSON5Object dogJson = new JSON5Object();
        dogJson.put("type", "dog");
        dogJson.put("name", "Buddy");
        dogJson.put("breed", "Golden Retriever");
        animalsArray.add(dogJson);
        
        // Cat 객체
        JSON5Object catJson = new JSON5Object();
        catJson.put("type", "cat");
        catJson.put("name", "Whiskers");
        catJson.put("indoor", true);
        animalsArray.add(catJson);
        
        json.put("animals", animalsArray);
        
        // When
        PetStore result = JSON5Serializer.fromJSON5Object(json, PetStore.class);
        
        // Then
        assertNotNull(result);
        assertEquals("Pet Store", result.name);
        assertEquals(2, result.animals.size());
        
        // 첫 번째 동물 (Dog)
        assertInstanceOf(Dog.class, result.animals.get(0));
        Dog dog = (Dog) result.animals.get(0);
        assertEquals("Buddy", dog.name);
        assertEquals("Golden Retriever", dog.breed);
        
        // 두 번째 동물 (Cat)
        assertInstanceOf(Cat.class, result.animals.get(1));
        Cat cat = (Cat) result.animals.get(1);
        assertEquals("Whiskers", cat.name);
        assertTrue(cat.indoor);
    }
    
    @Test
    @DisplayName("기존 속성을 타입 정보로 활용하는 경우")
    void shouldHandleExistingPropertyAsTypeInfo() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("status", "active");
        json.put("name", "John Doe");
        json.put("lastLoginDate", "2023-01-01");
        
        // When
        User result = JSON5Serializer.fromJSON5Object(json, User.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(ActiveUser.class, result);
        assertEquals("active", result.status);
        assertEquals("John Doe", result.name);
        assertNotNull(((ActiveUser) result).lastLoginDate);
    }
    
    @Test
    @DisplayName("다중 레벨 다형성 처리")
    void shouldHandleMultiLevelPolymorphism() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("type", "premium");
        json.put("name", "Premium Account");
        json.put("tier", "gold");
        json.put("benefits", "All features included");
        
        // When
        Account result = JSON5Serializer.fromJSON5Object(json, Account.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(PremiumAccount.class, result);
        assertEquals("Premium Account", result.name);
        assertEquals("gold", ((PremiumAccount) result).tier);
        assertEquals("All features included", ((PremiumAccount) result).benefits);
    }
    
    @Test
    @DisplayName("기본 구현체 사용 시나리오")
    void shouldUseDefaultImplementationWhenTypeInfoMissing() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("amount", 250.0);
        json.put("currency", "USD");
        // type 정보 누락
        
        // When
        Payment result = JSON5Serializer.fromJSON5Object(json, Payment.class);
        
        // Then
        assertNotNull(result);
        assertInstanceOf(GenericPayment.class, result);
        assertEquals(250.0, ((GenericPayment) result).amount);
        assertEquals("USD", ((GenericPayment) result).currency);
    }
    
    @Test
    @DisplayName("중첩된 다형성 객체 처리")
    void shouldHandleNestedPolymorphicObjects() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("orderId", "ORDER-123");
        
        // payment 객체 (다형성)
        JSON5Object paymentJson = new JSON5Object();
        paymentJson.put("type", "credit");
        paymentJson.put("cardNumber", "1234-5678-9012-3456");
        paymentJson.put("amount", 100.0);
        json.put("payment", paymentJson);
        
        // customer 객체 (다형성)
        JSON5Object customerJson = new JSON5Object();
        customerJson.put("type", "premium");
        customerJson.put("name", "Premium Customer");
        customerJson.put("tier", "platinum");
        customerJson.put("benefits", "VIP support");
        json.put("customer", customerJson);
        
        // When
        Order result = JSON5Serializer.fromJSON5Object(json, Order.class);
        
        // Then
        assertNotNull(result);
        assertEquals("ORDER-123", result.orderId);
        
        // Payment 검증
        assertInstanceOf(CreditCardPayment.class, result.payment);
        CreditCardPayment creditPayment = (CreditCardPayment) result.payment;
        assertEquals("1234-5678-9012-3456", creditPayment.cardNumber);
        assertEquals(100.0, creditPayment.amount);
        
        // Customer 검증
        assertInstanceOf(PremiumAccount.class, result.customer);
        PremiumAccount premiumCustomer = (PremiumAccount) result.customer;
        assertEquals("Premium Customer", premiumCustomer.name);
        assertEquals("platinum", premiumCustomer.tier);
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
        public void makeSound() { }
    }
    
    @JSON5Type
    public static class Cat extends Animal {
        @JSON5Value
        private boolean indoor;
        
        @Override
        public void makeSound() { }
    }
    
    @JSON5Type
    public static class PetStore {
        @JSON5Value
        public String name;
        
        @JSON5Value
        public java.util.List<Animal> animals;
    }
    
    @JSON5TypeInfo(property = "status", include = TypeInclusion.EXISTING_PROPERTY)
    @JSON5SubType(value = ActiveUser.class, name = "active")
    @JSON5SubType(value = InactiveUser.class, name = "inactive")
    public abstract static class User {
        @JSON5Value
        protected String status;
        
        @JSON5Value
        protected String name;
    }
    
    @JSON5Type
    public static class ActiveUser extends User {
        @JSON5Value
        private String lastLoginDate;
    }
    
    @JSON5Type
    public static class InactiveUser extends User {
        @JSON5Value
        private String deactivationReason;
    }
    
    @JSON5TypeInfo(property = "type")
    @JSON5SubType(value = BasicAccount.class, name = "basic")
    @JSON5SubType(value = PremiumAccount.class, name = "premium")
    public abstract static class Account {
        @JSON5Value
        protected String name;
    }
    
    @JSON5Type
    public static class BasicAccount extends Account {
        @JSON5Value
        private String limitations;
    }
    
    @JSON5Type
    public static class PremiumAccount extends Account {
        @JSON5Value
        private String tier;
        
        @JSON5Value
        private String benefits;
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
        
        @JSON5Value
        private String currency;
        
        @Override
        public void process() { }
    }
    
    @JSON5Type
    public static class CreditCardPayment implements Payment {
        @JSON5Value
        private String cardNumber;
        
        @JSON5Value
        private double amount;
        
        @Override
        public void process() { }
    }
    
    @JSON5Type
    public static class PayPalPayment implements Payment {
        @JSON5Value
        private String email;
        
        @JSON5Value
        private double amount;
        
        @Override
        public void process() { }
    }
    
    @JSON5Type
    public static class Order {
        @JSON5Value
        public String orderId;
        
        @JSON5Value
        public Payment payment;
        
        @JSON5Value
        public Account customer;
    }
}
