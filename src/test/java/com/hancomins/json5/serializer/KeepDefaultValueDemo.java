package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;

import java.util.Arrays;
import java.util.List;

/**
 * 기본값 유지 기능의 데모 예제입니다.
 */
public class KeepDefaultValueDemo {

    public static class Person {
        @JSON5Value
        private String name = "Unknown";

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
    
    public static void main(String[] args) {
        System.out.println("=== JSON5 기본값 유지 기능 데모 ===\n");
        
        // 1. 부분적인 JSON으로 역직렬화
        System.out.println("1. 부분적인 JSON으로 역직렬화:");
        JSON5Object partialJson = new JSON5Object();
        partialJson.put("name", "John");
        partialJson.put("age", 30);
        // active와 hobbies는 JSON에 없음
        
        System.out.println("입력 JSON: " + partialJson);
        
        Person person1 = JSON5Serializer.fromJSON5Object(partialJson, Person.class);
        System.out.println("결과: " + person1);
        System.out.println("-> age, name은 JSON 값으로, active와 hobbies는 기본값 유지\n");
        
        // 2. 빈 JSON으로 역직렬화
        System.out.println("2. 빈 JSON으로 역직렬화:");
        JSON5Object emptyJson = new JSON5Object();
        System.out.println("입력 JSON: " + emptyJson);
        
        Person person2 = JSON5Serializer.fromJSON5Object(emptyJson, Person.class);
        System.out.println("결과: " + person2);
        System.out.println("-> 모든 필드가 기본값 유지\n");
        
        // 3. null 값이 포함된 JSON으로 역직렬화
        System.out.println("3. null 값이 포함된 JSON으로 역직렬화:");
        JSON5Object nullJson = new JSON5Object();
        nullJson.put("name", (String) null);
        nullJson.put("age", 25);
        // active와 hobbies는 JSON에 없음
        
        System.out.println("입력 JSON: " + nullJson);
        
        Person person3 = JSON5Serializer.fromJSON5Object(nullJson, Person.class);
        System.out.println("결과: " + person3);
        System.out.println("-> name은 null, age는 JSON 값, active와 hobbies는 기본값 유지\n");
        
        // 4. 기존 객체에 역직렬화
        System.out.println("4. 기존 객체에 역직렬화:");
        Person existingPerson = new Person();
        existingPerson.setName("ExistingName");
        existingPerson.setAge(99);
        existingPerson.setActive(false);
        existingPerson.setHobbies(Arrays.asList("gaming", "cooking"));
        
        System.out.println("기존 객체: " + existingPerson);
        
        JSON5Object updateJson = new JSON5Object();
        updateJson.put("name", "UpdatedName");
        // age, active, hobbies는 JSON에 없음
        
        System.out.println("업데이트 JSON: " + updateJson);
        
        Person updatedPerson = JSON5Serializer.fromJSON5Object(updateJson, existingPerson);
        System.out.println("결과: " + updatedPerson);
        System.out.println("-> name만 업데이트되고 나머지는 기존 값 유지\n");
        
        // 5. 전체 필드가 있는 JSON (기존 동작 확인)
        System.out.println("5. 전체 필드가 있는 JSON (기존 동작 확인):");
        JSON5Object fullJson = new JSON5Object();
        fullJson.put("name", "FullName");
        fullJson.put("age", 50);
        fullJson.put("active", false);
        fullJson.put("hobbies", Arrays.asList("travel", "photography"));
        
        System.out.println("입력 JSON: " + fullJson);
        
        Person person5 = JSON5Serializer.fromJSON5Object(fullJson, Person.class);
        System.out.println("결과: " + person5);
        System.out.println("-> 모든 필드가 JSON 값으로 설정됨 (기존 동작과 동일)\n");
        
        System.out.println("=== 데모 완료 ===");
    }
}
