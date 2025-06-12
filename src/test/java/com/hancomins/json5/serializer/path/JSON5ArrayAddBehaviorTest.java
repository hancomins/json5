package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5Array의 add 메서드 동작 확인")
class JSON5ArrayAddBehaviorTest {
    
    @Test
    @DisplayName("add로 추가한 배열에서의 문자열 키 접근 테스트")
    void testAddArrayStringKeyAccess() {
        JSON5Object root = new JSON5Object();
        JSON5Array numbers = new JSON5Array();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        root.put("numbers", numbers);
        
        // JSON5Array의 내부 구조 확인
        System.out.println("=== JSON5Array 상태 확인 ===");
        System.out.println("Size: " + numbers.size());
        for (int i = 0; i < numbers.size(); i++) {
            System.out.println("Index " + i + ": " + numbers.get(i));
        }
        
        // pathExists 동작 확인
        System.out.println("=== pathExists 테스트 ===");
        boolean exists0 = JSON5PathExtractor.pathExists(root, "numbers[0]");
        boolean exists0Dot = JSON5PathExtractor.pathExists(root, "numbers.0");
        System.out.println("numbers[0] exists: " + exists0);
        System.out.println("numbers.0 exists: " + exists0Dot);
        
        // extractValue 동작 확인
        System.out.println("=== extractValue 테스트 ===");
        Object value0 = JSON5PathExtractor.extractValue(root, "numbers[0]");
        Object value0Dot = JSON5PathExtractor.extractValue(root, "numbers.0");
        System.out.println("numbers[0] value: " + value0 + " (Missing: " + JSON5PathExtractor.isMissingValue(value0) + ")");
        System.out.println("numbers.0 value: " + value0Dot + " (Missing: " + JSON5PathExtractor.isMissingValue(value0Dot) + ")");
        
        // numbers 객체 자체의 내부 구조 확인
        System.out.println("=== numbers 객체 toString ===");
        System.out.println("numbers.toString(): " + numbers.toString());
    }
}
