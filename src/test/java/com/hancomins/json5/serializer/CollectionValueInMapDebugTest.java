package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

@DisplayName("Collection value in map 디버깅 테스트")
class CollectionValueInMapDebugTest {
    
    @JSON5Type
    public static class SimpleUser {
        @JSON5Value
        private String name;
        
        public SimpleUser() {}
        
        public SimpleUser(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    @Test
    @DisplayName("간단한 Map<String, List<String>> 직렬화 테스트")
    void testSimpleMapWithList() {
        System.out.println("=== Phase 1 디버깅 테스트 시작 ===");
        
        // Given
        Map<String, List<String>> testMap = new HashMap<>();
        testMap.put("fruits", Arrays.asList("apple", "banana"));
        testMap.put("colors", Arrays.asList("red", "blue", "green"));
        
        try {
            // When - 직렬화
            MapSerializer serializer = new MapSerializer();
            JSON5Object serialized = serializer.serializeMap(testMap, List.class);
            
            System.out.println("직렬화 성공: " + serialized.toString());
            
            // When - 역직렬화
            MapDeserializer deserializer = new MapDeserializer();
            @SuppressWarnings("unchecked")
            Map<String, List<String>> deserialized = (Map<String, List<String>>) 
                deserializer.deserialize(null, serialized, List.class, null, null);
                
            System.out.println("역직렬화 성공: " + deserialized);
            System.out.println("fruits 크기: " + deserialized.get("fruits").size());
            System.out.println("colors 크기: " + deserialized.get("colors").size());
            
        } catch (Exception e) {
            System.err.println("테스트 실패: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Test
    @DisplayName("Map<String, List<SimpleUser>> 테스트")
    void testMapWithUserList() {
        System.out.println("=== Map<String, List<SimpleUser>> 테스트 시작 ===");
        
        // Given
        Map<String, List<SimpleUser>> userGroups = new HashMap<>();
        List<SimpleUser> group1 = Arrays.asList(
            new SimpleUser("John"),
            new SimpleUser("Jane")
        );
        List<SimpleUser> group2 = Arrays.asList(
            new SimpleUser("Mike"),
            new SimpleUser("Sara")
        );
        userGroups.put("team1", group1);
        userGroups.put("team2", group2);
        
        try {
            // When - 직렬화
            MapSerializer serializer = new MapSerializer();
            JSON5Object serialized = serializer.serializeMap(userGroups, List.class);
            
            System.out.println("직렬화 성공: " + serialized.toString());
            
            // When - 역직렬화
            MapDeserializer deserializer = new MapDeserializer();
            @SuppressWarnings("unchecked")
            Map<String, List<SimpleUser>> deserialized = (Map<String, List<SimpleUser>>) 
                deserializer.deserialize(null, serialized, List.class, null, null);
                
            System.out.println("역직렬화 성공: " + deserialized);
            
        } catch (Exception e) {
            System.err.println("테스트 실패: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
