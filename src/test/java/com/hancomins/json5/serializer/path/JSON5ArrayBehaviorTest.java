package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5Array 동작 확인")
class JSON5ArrayBehaviorTest {
    
    @Test
    @DisplayName("JSON5Array의 put vs add 방식 확인")
    void testJSON5ArrayBehavior() {
        // 첫 번째 방식: put 메서드 사용
        JSON5Array items1 = new JSON5Array();
        items1.put("item0");
        items1.put("item1");
        items1.put("item2");
        
        // 두 번째 방식: add 메서드 사용
        JSON5Array items2 = new JSON5Array();
        items2.add("item0");
        items2.add("item1");
        items2.add("item2");
        
        System.out.println("=== PUT 방식 ===");
        System.out.println("Size: " + items1.size());
        for (int i = 0; i < items1.size(); i++) {
            System.out.println("Index " + i + ": " + items1.get(i));
        }
        
        System.out.println("=== ADD 방식 ===");
        System.out.println("Size: " + items2.size());
        for (int i = 0; i < items2.size(); i++) {
            System.out.println("Index " + i + ": " + items2.get(i));
        }
        
        // 문자열 키로 접근 테스트는 제외 (has 메서드가 없음)
        
        // 실제 접근 테스트
        JSON5Object root1 = new JSON5Object();
        root1.put("items", items1);
        
        JSON5Object root2 = new JSON5Object();
        root2.put("items", items2);
        
        System.out.println("=== 경로 접근 테스트 ===");
        Object putResult0 = JSON5PathExtractor.extractValue(root1, "items.0");
        Object addResult0 = JSON5PathExtractor.extractValue(root2, "items.0");
        System.out.println("PUT items.0: " + putResult0 + " (Missing: " + JSON5PathExtractor.isMissingValue(putResult0) + ")");
        System.out.println("ADD items.0: " + addResult0 + " (Missing: " + JSON5PathExtractor.isMissingValue(addResult0) + ")");
        
        Object putResult0Bracket = JSON5PathExtractor.extractValue(root1, "items[0]");
        Object addResult0Bracket = JSON5PathExtractor.extractValue(root2, "items[0]");
        System.out.println("PUT items[0]: " + putResult0Bracket + " (Missing: " + JSON5PathExtractor.isMissingValue(putResult0Bracket) + ")");
        System.out.println("ADD items[0]: " + addResult0Bracket + " (Missing: " + JSON5PathExtractor.isMissingValue(addResult0Bracket) + ")");
    }
}
