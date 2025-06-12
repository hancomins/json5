package com.hancomins.json5.serializer.path;

import com.hancomins.json5.PathItem;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

@DisplayName("실제 PathItem 동작 확인")
class ActualBehaviorTest {
    
    @Test
    @DisplayName("numbers[0] 파싱 결과 상세 확인")
    void testNumbersArrayParsing() {
        List<PathItem> items = PathItem.parseMultiPath2("numbers[0]");
        
        System.out.println("numbers[0] 파싱 결과:");
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
        
        // 실제 JSON5PathExtractor 동작 확인
        JSON5Object root = new JSON5Object();
        JSON5Array numbers = new JSON5Array();
        numbers.add(10);
        numbers.add(20);
        root.put("numbers", numbers);
        
        Object result = JSON5PathExtractor.extractValue(root, "numbers[0]");
        System.out.println("Extract result: " + result);
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(result));
        
        boolean exists = JSON5PathExtractor.pathExists(root, "numbers[0]");
        System.out.println("Path exists: " + exists);
    }
    
    @Test
    @DisplayName("users[0].name 파싱 결과 상세 확인")
    void testUsersArrayObjectParsing() {
        List<PathItem> items = PathItem.parseMultiPath2("users[0].name");
        
        System.out.println("users[0].name 파싱 결과:");
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
        
        // 실제 JSON5PathExtractor 동작 확인
        JSON5Object root = new JSON5Object();
        JSON5Array users = new JSON5Array();
        JSON5Object user1 = new JSON5Object();
        user1.put("name", "Alice");
        users.add(user1);
        root.put("users", users);
        
        Object result = JSON5PathExtractor.extractValue(root, "users[0].name");
        System.out.println("Extract result: " + result);
        System.out.println("Is missing: " + JSON5PathExtractor.isMissingValue(result));
        
        boolean exists = JSON5PathExtractor.pathExists(root, "users[0].name");
        System.out.println("Path exists: " + exists);
    }
}