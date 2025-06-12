package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("배열 접근 문제 디버깅")
class ArrayAccessDebugTest {
    
    @Test
    @DisplayName("배열 접근 디버깅")
    void debugArrayAccess() {
        JSON5Object root = new JSON5Object();
        JSON5Array numbers = new JSON5Array();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        root.put("numbers", numbers);
        
        // numbers[0] 접근 테스트
        Object result1 = JSON5PathExtractor.extractValue(root, "numbers[0]");
        System.out.println("numbers[0] result: " + result1);
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(result1));
        
        // numbers.0 접근 테스트 (fallback)
        Object result2 = JSON5PathExtractor.extractValue(root, "numbers.0");
        System.out.println("numbers.0 result: " + result2);
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(result2));
        
        // pathExists 테스트
        boolean exists1 = JSON5PathExtractor.pathExists(root, "numbers[0]");
        boolean exists2 = JSON5PathExtractor.pathExists(root, "numbers.0");
        System.out.println("numbers[0] exists: " + exists1);
        System.out.println("numbers.0 exists: " + exists2);
        
        // 실제 값 확인
        assertNotNull(result1);
        assertNotNull(result2);
    }
}
