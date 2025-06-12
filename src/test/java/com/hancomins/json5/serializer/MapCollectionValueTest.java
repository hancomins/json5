package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Map Collection 값 지원 테스트")
class MapCollectionValueTest {
    
    @JSON5Type
    static class SimpleContainer {
        @JSON5Value
        private Map<String, List<String>> stringListMap = new HashMap<>();
        
        public SimpleContainer() {}
        
        public Map<String, List<String>> getStringListMap() {
            return stringListMap;
        }
        
        public void setStringListMap(Map<String, List<String>> stringListMap) {
            this.stringListMap = stringListMap;
        }
    }
    
    @Test
    @DisplayName("Map<String, List<String>> 직렬화/역직렬화")
    void testMapWithListValue() {
        // Given
        SimpleContainer container = new SimpleContainer();
        Map<String, List<String>> testMap = new HashMap<>();
        testMap.put("admin", Arrays.asList("read", "write", "delete"));
        testMap.put("user", Arrays.asList("read"));
        container.setStringListMap(testMap);
        
        try {
            // When - 직렬화
            JSON5Object serialized = JSON5Serializer.toJSON5Object(container);
            System.out.println("직렬화 결과: " + serialized.toString());
            
            // Then - 직렬화 검증
            assertTrue(serialized.has("stringListMap"));
            
            // When - 역직렬화  
            SimpleContainer deserialized = JSON5Serializer.fromJSON5Object(serialized, SimpleContainer.class);
            
            // Then - 역직렬화 검증
            assertNotNull(deserialized.getStringListMap());
            assertEquals(2, deserialized.getStringListMap().size());
            
            // 더 자세한 검증을 위해 타입 확인
            System.out.println("역직렬화된 Map: " + deserialized.getStringListMap());
            System.out.println("admin 값 타입: " + deserialized.getStringListMap().get("admin").getClass().getName());
            
        } catch (Exception e) {
            System.err.println("테스트 실패: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            fail("테스트 중 예외 발생: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Map Collection 생성 가능 여부 확인")
    void testCollectionTypeSupport() {
        // ISchemaMapValue.assertCollectionOrMapValue가 Collection을 허용하는지 확인
        try {
            ISchemaMapValue.assertCollectionOrMapValue(List.class, "test.path");
            // 예외가 발생하지 않으면 성공
            assertTrue(true, "Collection 타입이 Map 값으로 허용됨");
        } catch (Exception e) {
            fail("Collection 타입이 여전히 거부됨: " + e.getMessage());
        }
    }
}
