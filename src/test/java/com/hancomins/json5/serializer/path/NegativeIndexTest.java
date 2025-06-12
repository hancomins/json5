package com.hancomins.json5.serializer.path;

import com.hancomins.json5.PathItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

@DisplayName("음수 인덱스 파싱 테스트")
class NegativeIndexTest {
    
    @Test
    @DisplayName("음수 인덱스 PathItem 파싱 확인")
    void testNegativeIndexParsing() {
        List<PathItem> items = PathItem.parseMultiPath2("numbers[-1]");
        
        System.out.println("numbers[-1] 파싱 결과:");
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
    }
}