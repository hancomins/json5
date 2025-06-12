package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("호환성 테스트 디버깅")
class CompatibilityTestDebugTest {
    
    @Test
    @DisplayName("shouldSupportDirectArrayAccess 메서드 정확히 복사해서 디버깅")
    void debugDirectArrayAccess() {
        JSON5Object root = new JSON5Object();
        JSON5Array numbers = new JSON5Array();
        numbers.add(10);  // add 메서드 사용
        numbers.add(20);
        numbers.add(30);
        root.put("numbers", numbers);
        
        // 배열 요소 직접 접근 - PathItem 방식 (numbers[0])
        Object result1 = JSON5PathExtractor.extractValue(root, "numbers[0]");
        Object result2 = JSON5PathExtractor.extractValue(root, "numbers[1]");
        Object result3 = JSON5PathExtractor.extractValue(root, "numbers[2]");
        
        System.out.println("numbers[0] result: " + result1 + " (expected: 10)");
        System.out.println("numbers[1] result: " + result2 + " (expected: 20)");
        System.out.println("numbers[2] result: " + result3 + " (expected: 30)");
        
        assertEquals(10, result1);
        assertEquals(20, result2);
        assertEquals(30, result3);
        
        // 존재하지 않는 인덱스
        Object missingResult1 = JSON5PathExtractor.extractValue(root, "numbers[3]");
        Object missingResult2 = JSON5PathExtractor.extractValue(root, "numbers[-1]");
        
        System.out.println("numbers[3] result: " + missingResult1 + " (should be missing)");
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(missingResult1));
        System.out.println("numbers[-1] result: " + missingResult2 + " (should be missing)");
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(missingResult2));
        
        assertTrue(JSON5PathExtractor.isMissingValue(missingResult1));
        assertTrue(JSON5PathExtractor.isMissingValue(missingResult2));
        
        // 경로 존재 확인 - PathItem 방식
        boolean exists0 = JSON5PathExtractor.pathExists(root, "numbers[0]");
        boolean exists2 = JSON5PathExtractor.pathExists(root, "numbers[2]");
        boolean exists3 = JSON5PathExtractor.pathExists(root, "numbers[3]");
        
        System.out.println("numbers[0] exists: " + exists0 + " (expected: true)");
        System.out.println("numbers[2] exists: " + exists2 + " (expected: true)");
        System.out.println("numbers[3] exists: " + exists3 + " (expected: false)");
        
        // 이것이 141번째 줄이었을 것:
        System.out.println("About to test: assertTrue(exists0) - this is line that fails");
        assertTrue(exists0);
        assertTrue(exists2);
        assertFalse(exists3);
        
        System.out.println("All assertions passed!");
    }
}