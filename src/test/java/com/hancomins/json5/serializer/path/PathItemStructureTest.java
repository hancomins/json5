package com.hancomins.json5.serializer.path;

import com.hancomins.json5.PathItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

@DisplayName("다양한 PathItem 구조 분석")
class PathItemStructureTest {
    
    @Test
    @DisplayName("여러 패턴의 PathItem 구조 비교")
    void testVariousPathItemStructures() {
        String[] testPaths = {
            "numbers[0]",      // 단일 배열 접근
            "numbers[-1]",     // 음수 인덱스 배열 접근
            "users[0].name",   // 배열 + 객체 속성 접근
            "users[0]",        // 단순 배열 접근
            "user.name"        // 단순 객체 속성 접근
        };
        
        for (String path : testPaths) {
            System.out.println("=== " + path + " ===");
            List<PathItem> items = PathItem.parseMultiPath2(path);
            System.out.println("Total items: " + items.size());
            
            for (int i = 0; i < items.size(); i++) {
                PathItem item = items.get(i);
                System.out.println(String.format("Item %d:", i));
                System.out.println("  name: '" + item.getName() + "'");
                System.out.println("  index: " + item.getIndex());
                System.out.println("  isEndPoint: " + item.isEndPoint());
                System.out.println("  isObject: " + item.isObject());
                System.out.println("  isArrayValue: " + item.isArrayValue());
                System.out.println("  isInArray: " + item.isInArray());
                System.out.println();
            }
            System.out.println();
        }
    }
}