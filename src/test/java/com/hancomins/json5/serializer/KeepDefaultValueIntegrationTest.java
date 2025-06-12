package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 기본값 유지 기능의 통합 테스트입니다.
 */
@DisplayName("기본값 유지 기능 통합 테스트")
public class KeepDefaultValueIntegrationTest {

    @JSON5Type
    public static class Person {
        @JSON5Value
        private String name = "Unknown";
        
        @JSON5Value
        private int age = 18;
        
        @JSON5Value
        private boolean active = true;
        
        @JSON5Value
        private List<String> hobbies = Arrays.asList("reading", "music");
        
        // 기본 생성자
        public Person() {}
        
        // Getter/Setter
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public List<String> getHobbies() { return hobbies; }
        public void setHobbies(List<String> hobbies) { this.hobbies = hobbies; }
        
        @Override
        public String toString() {
            return String.format("Person{name='%s', age=%d, active=%s, hobbies=%s}", 
                               name, age, active, hobbies);
        }
    }

    @Test
    @DisplayName("부분적인 JSON으로 역직렬화 시 기본값 유지")
    public void testPartialJsonKeepsDefaults() {
        // Given
        JSON5Object partialJson = new JSON5Object();
        partialJson.put("name", "John");
        partialJson.put("age", 30);
        // active와 hobbies는 JSON에 없음
        
        // When
        Person person = JSON5Serializer.fromJSON5Object(partialJson, Person.class);
        
        // Then
        assertEquals("John", person.getName()); // JSON 값
        assertEquals(30, person.getAge()); // JSON 값
        assertTrue(person.isActive()); // 기본값 유지
        assertEquals(Arrays.asList("reading", "music"), person.getHobbies()); // 기본값 유지
        
        System.out.println("부분적인 JSON 결과: " + person);
    }
    
    @Test
    @DisplayName("빈 JSON으로 역직렬화 시 모든 기본값 유지")
    public void testEmptyJsonKeepsAllDefaults() {
        // Given
        JSON5Object emptyJson = new JSON5Object();
        
        // When
        Person person = JSON5Serializer.fromJSON5Object(emptyJson, Person.class);
        
        // Then
        assertEquals("Unknown", person.getName()); // 기본값 유지
        assertEquals(18, person.getAge()); // 기본값 유지
        assertTrue(person.isActive()); // 기본값 유지
        assertEquals(Arrays.asList("reading", "music"), person.getHobbies()); // 기본값 유지
        
        System.out.println("빈 JSON 결과: " + person);
    }
    
    @Test
    @DisplayName("null 값이 포함된 JSON 처리")
    public void testNullValuesOverrideDefaults() {
        // Given
        JSON5Object nullJson = new JSON5Object();
        nullJson.put("name", (String) null);
        nullJson.put("age", 25);
        // active와 hobbies는 JSON에 없음
        
        // When
        Person person = JSON5Serializer.fromJSON5Object(nullJson, Person.class);
        
        // Then
        assertNull(person.getName()); // null로 설정
        assertEquals(25, person.getAge()); // JSON 값
        assertTrue(person.isActive()); // 기본값 유지
        assertEquals(Arrays.asList("reading", "music"), person.getHobbies()); // 기본값 유지
        
        System.out.println("null 포함 JSON 결과: " + person);
    }
    
    @Test
    @DisplayName("기존 객체에 역직렬화 시 기본값 유지")
    public void testExistingObjectKeepsDefaults() {
        // Given
        Person existingPerson = new Person();
        existingPerson.setName("ExistingName");
        existingPerson.setAge(99);
        existingPerson.setActive(false);
        existingPerson.setHobbies(Arrays.asList("gaming", "cooking"));
        
        JSON5Object updateJson = new JSON5Object();
        updateJson.put("name", "UpdatedName");
        // age, active, hobbies는 JSON에 없음
        
        // When
        Person updatedPerson = JSON5Serializer.fromJSON5Object(updateJson, existingPerson);
        
        // Then
        assertSame(existingPerson, updatedPerson); // 같은 객체 인스턴스
        assertEquals("UpdatedName", updatedPerson.getName()); // JSON 값으로 업데이트
        assertEquals(99, updatedPerson.getAge()); // 기존 값 유지
        assertFalse(updatedPerson.isActive()); // 기존 값 유지
        assertEquals(Arrays.asList("gaming", "cooking"), updatedPerson.getHobbies()); // 기존 값 유지
        
        System.out.println("기존 객체 업데이트 결과: " + updatedPerson);
    }
    
    @Test
    @DisplayName("전체 필드가 있는 JSON (기존 동작 확인)")
    public void testFullJsonWorksAsExpected() {
        // Given
        JSON5Object fullJson = new JSON5Object();
        fullJson.put("name", "FullName");
        fullJson.put("age", 50);
        fullJson.put("active", false);
        fullJson.put("hobbies", Arrays.asList("travel", "photography"));
        
        // When
        Person person = JSON5Serializer.fromJSON5Object(fullJson, Person.class);
        
        // Then
        assertEquals("FullName", person.getName()); // JSON 값
        assertEquals(50, person.getAge()); // JSON 값
        assertFalse(person.isActive()); // JSON 값
        assertEquals(Arrays.asList("travel", "photography"), person.getHobbies()); // JSON 값
        
        System.out.println("전체 JSON 결과: " + person);
    }
}
