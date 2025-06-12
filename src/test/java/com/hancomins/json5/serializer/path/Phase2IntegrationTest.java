package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("페이즈2 통합 테스트: JSON5Object $. 경로와 JSON5PathExtractor 일치성 검증")
class Phase2IntegrationTest {
    
    @Test
    @DisplayName("JSON5Object $.경로와 JSON5PathExtractor.extractValue() 동일 결과 보장")
    void shouldProduceIdenticalResults() {
        // Given: 복잡한 테스트 데이터 생성
        JSON5Object testData = createComplexTestData();
        
        // When & Then: 다양한 경로에 대해 동일한 결과 검증
        
        // 1. 단순 경로
        assertEquals(
            testData.get("$.name"), 
            JSON5PathExtractor.extractValue(testData, "name"),
            "단순 경로 결과가 일치해야 함"
        );
        
        // 2. 중첩 경로
        assertEquals(
            testData.get("$.user.profile.email"), 
            JSON5PathExtractor.extractValue(testData, "user.profile.email"),
            "중첩 경로 결과가 일치해야 함"
        );
        
        // 3. 배열 인덱스 경로
        assertEquals(
            testData.get("$.users[0].name"), 
            JSON5PathExtractor.extractValue(testData, "users[0].name"),
            "배열 인덱스 경로 결과가 일치해야 함"
        );
        
        // 4. 복잡한 중첩 배열 경로
        assertEquals(
            testData.get("$.companies[0].departments[1].employees[0].name"), 
            JSON5PathExtractor.extractValue(testData, "companies[0].departments[1].employees[0].name"),
            "복잡한 중첩 배열 경로 결과가 일치해야 함"
        );
        
        // 5. 존재하지 않는 경로
        assertNull(testData.get("$.nonexistent.path"));
        assertTrue(JSON5PathExtractor.isMissingValue(
            JSON5PathExtractor.extractValue(testData, "nonexistent.path")
        ));
    }
    
    @Test
    @DisplayName("JSON5Object $.has()와 JSON5PathExtractor.pathExists() 동일 결과 보장")
    void shouldProduceIdenticalExistenceResults() {
        // Given
        JSON5Object testData = createComplexTestData();
        
        // When & Then: 존재 확인 결과 검증
        
        // 1. 존재하는 경로들
        String[] existingPaths = {
            "name",
            "user.profile.email", 
            "users[0].name",
            "companies[0].departments[1].name"
        };
        
        for (String testPath : existingPaths) {
            assertEquals(
                testData.has("$." + testPath),
                JSON5PathExtractor.pathExists(testData, testPath),
                "경로 존재 확인 결과가 일치해야 함: " + testPath
            );
            assertTrue(testData.has("$." + testPath), "경로가 존재해야 함: " + testPath);
        }
        
        // 2. 존재하지 않는 경로들
        String[] nonExistingPaths = {
            "nonexistent",
            "user.nonexistent",
            "users[99].name",
            "companies[0].departments[99].name"
        };
        
        for (String testPath : nonExistingPaths) {
            assertEquals(
                testData.has("$." + testPath),
                JSON5PathExtractor.pathExists(testData, testPath),
                "경로 존재 확인 결과가 일치해야 함: " + testPath
            );
            assertFalse(testData.has("$." + testPath), "경로가 존재하지 않아야 함: " + testPath);
        }
    }
    
    @Test
    @DisplayName("배열 인덱스 범위 외 접근 시 동일한 동작")
    void shouldHandleOutOfBoundsArrayAccess() {
        // Given
        JSON5Object root = new JSON5Object();
        JSON5Array numbers = new JSON5Array();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        root.put("numbers", numbers);
        
        // When & Then: 범위 외 접근
        assertNull(root.get("$.numbers[5]")); // 기존 JSON5Object $.경로 동작
        assertTrue(JSON5PathExtractor.isMissingValue(
            JSON5PathExtractor.extractValue(root, "numbers[5]")
        )); // JSON5PathExtractor 동작
        
        // 음수 인덱스
        assertNull(root.get("$.numbers[-1]"));
        assertTrue(JSON5PathExtractor.isMissingValue(
            JSON5PathExtractor.extractValue(root, "numbers[-1]")
        ));
    }
    
    @Test
    @DisplayName("null 값 처리 동일성 검증")
    void shouldHandleNullValuesIdentically() {
        // Given
        JSON5Object root = new JSON5Object();
        root.put("nullValue", null);
        
        JSON5Object nested = new JSON5Object();
        nested.put("innerNull", null);
        root.put("nested", nested);
        
        // When & Then
        assertNull(root.get("$.nullValue"));
        assertNull(JSON5PathExtractor.extractValue(root, "nullValue"));
        
        assertNull(root.get("$.nested.innerNull"));
        assertNull(JSON5PathExtractor.extractValue(root, "nested.innerNull"));
    }
    
    /**
     * 복잡한 테스트 데이터 생성
     */
    private JSON5Object createComplexTestData() {
        JSON5Object root = new JSON5Object();
        
        // 단순 필드
        root.put("name", "RootName");
        root.put("version", "1.0.0");
        
        // 중첩 객체
        JSON5Object user = new JSON5Object();
        JSON5Object profile = new JSON5Object();
        profile.put("email", "user@example.com");
        profile.put("age", 30);
        user.put("profile", profile);
        root.put("user", user);
        
        // 사용자 배열
        JSON5Array users = new JSON5Array();
        JSON5Object user1 = new JSON5Object();
        user1.put("name", "Alice");
        user1.put("role", "Admin");
        users.add(user1);
        
        JSON5Object user2 = new JSON5Object();
        user2.put("name", "Bob");
        user2.put("role", "User");
        users.add(user2);
        root.put("users", users);
        
        // 복잡한 중첩 구조 (회사 → 부서 → 직원)
        JSON5Array companies = new JSON5Array();
        JSON5Object company = new JSON5Object();
        company.put("name", "TechCorp");
        
        JSON5Array departments = new JSON5Array();
        
        // 첫 번째 부서
        JSON5Object dept1 = new JSON5Object();
        dept1.put("name", "Engineering");
        JSON5Array employees1 = new JSON5Array();
        JSON5Object emp1 = new JSON5Object();
        emp1.put("name", "John");
        emp1.put("position", "Senior Developer");
        employees1.add(emp1);
        dept1.put("employees", employees1);
        departments.add(dept1);
        
        // 두 번째 부서
        JSON5Object dept2 = new JSON5Object();
        dept2.put("name", "Marketing");
        JSON5Array employees2 = new JSON5Array();
        JSON5Object emp2 = new JSON5Object();
        emp2.put("name", "Sarah");
        emp2.put("position", "Marketing Manager");
        employees2.add(emp2);
        dept2.put("employees", employees2);
        departments.add(dept2);
        
        company.put("departments", departments);
        companies.add(company);
        root.put("companies", companies);
        
        return root;
    }
}
