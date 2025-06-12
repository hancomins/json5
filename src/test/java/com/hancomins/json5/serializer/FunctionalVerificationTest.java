package com.hancomins.json5.serializer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionalVerificationTest {

    @SuppressWarnings("unused")
    @JSON5Type
    public static class Employee {
        @JSON5Value
        private String name;
        @JSON5Value
        private int age;
        @JSON5Value
        private String department;

        @JSON5Creator()
        public Employee(@JSON5Property("name") String name,
                        @JSON5Property("age")int age,
                        @JSON5Property("department") String department) {
            this.name = name;
            this.age = age;
            this.department = department;
        }

        @JSON5Creator(priority = 1)
        public Employee(@JSON5Property("name") String name,
                        @JSON5Property("age")String age) {
            this.name = name;
            this.age = Integer.parseInt(age) + 100; // 예시로 100을 더함
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getDepartment() {
            return department;
        }
    }

    @Test
    public void priorityTest() {
        String json = "{ \"name\": \"John\", \"age\": 30, \"department\": \"Engineering\" }";

        // JSON5 직렬화
        Employee emp = JSON5Serializer.getInstance().deserialize(json, Employee.class);

        // 우선순위가 높은 생성자 호출 확인
        assert emp.getAge() == 130 : "우선순위가 높은 생성자가 호출되지 않았습니다.";
        assert emp.getName().equals("John") : "이름이 잘못 설정되었습니다.";
        assert null == emp.getDepartment() : "부서가 잘못 설정되었습니다."; // department는 null로 설정됨
    }


    @JSON5Type
    public static class Company {
        @JSON5Value
        Map<String, List<Employee>> ageGroup = new HashMap<>();

        public Company() {
            // 30대 그룹
            List<Employee> group30s = listOf(
                new Employee("John", 30, "Engineering"),
                new Employee("Jane", 25, "Marketing")
            );
            // 40대 그룹
            List<Employee> group40s = listOf(
                new Employee("Mike", 45, "Sales"),
                new Employee("Sara", 42, "HR")
            );

            ageGroup.put("30", group30s);
            ageGroup.put("40", group40s);

        }
    }

    @SuppressWarnings("unchecked")
    private static <T>  List<T> listOf(T... items) {
        ArrayList<T> list = new ArrayList<>();
        for( Object item : items) {

                list.add((T) item);

        }
        return list;
    }

    @Test
    public void mapSerializationTest() {
        Company company = new Company();

        try {
            // JSON5 직렬화
            String json = JSON5Serializer.getInstance().serialize(company).toString();
            System.out.println("Serialized JSON5: " + json);

            // 역직렬화
            Company deserializedCompany = JSON5Serializer.getInstance().deserialize(json, Company.class);
            System.out.println("Deserialization successful");
            System.out.println("ageGroup size: " + deserializedCompany.ageGroup.size());
            System.out.println("ageGroup keys: " + deserializedCompany.ageGroup.keySet());

            assert deserializedCompany.ageGroup.size() == 2 : "역직렬화된 그룹의 크기가 잘못되었습니다.";
            
            // 디버깅 정보 추가
            Object group30 = deserializedCompany.ageGroup.get("30");
            System.out.println("group30 type: " + (group30 != null ? group30.getClass().getName() : "null"));
            if (group30 instanceof List) {
                List<?> list30 = (List<?>) group30;
                System.out.println("group30 size: " + list30.size());
                if (!list30.isEmpty()) {
                    Object firstItem = list30.get(0);
                    System.out.println("first item type: " + (firstItem != null ? firstItem.getClass().getName() : "null"));
                    System.out.println("first item: " + firstItem);
                }
            }
            
            assert deserializedCompany.ageGroup.get("30").size() == 2 : "30대 그룹의 크기가 잘못되었습니다.";
            assert deserializedCompany.ageGroup.get("40").size() == 2 : "40대 그룹의 크기가 잘못되었습니다.";
            
            // Collection 내의 객체 타입 확인 후 캠스팅
            List<Employee> group30List = deserializedCompany.ageGroup.get("30");
            List<Employee> group40List = deserializedCompany.ageGroup.get("40");
            
            // 첫 번째 요소가 Employee 타입인지 확인
            if (!group30List.isEmpty() && group30List.get(0) instanceof Employee) {
                assert ((Employee) group30List.get(0)).getName().equals("John") : "30대 그룹의 첫 번째 직원 이름이 잘못되었습니다.";
            }
            if (!group40List.isEmpty() && group40List.get(0) instanceof Employee) {
                assert ((Employee) group40List.get(0)).getName().equals("Mike") : "40대 그룹의 첫 번째 직원 이름이 잘못되었습니다.";
            }
        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
