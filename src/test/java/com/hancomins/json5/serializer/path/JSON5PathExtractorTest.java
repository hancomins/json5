package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5PathExtractor 테스트")
class JSON5PathExtractorTest {
    
    @Test
    @DisplayName("단순 경로 추출 테스트")
    void testSimplePathExtraction() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John");
        json.put("age", 30);
        
        // When & Then
        Object nameValue = JSON5PathExtractor.extractValue(json, "name");
        assertFalse(JSON5PathExtractor.isMissingValue(nameValue));
        assertEquals("John", nameValue);
        
        Object ageValue = JSON5PathExtractor.extractValue(json, "age");
        assertFalse(JSON5PathExtractor.isMissingValue(ageValue));
        assertEquals(30, ageValue);
    }
    
    @Test
    @DisplayName("중첩 경로 추출 테스트")
    void testNestedPathExtraction() {
        // Given
        JSON5Object json = new JSON5Object();
        JSON5Object profile = new JSON5Object();
        profile.put("email", "john@example.com");
        profile.put("department", "Engineering");
        
        JSON5Object contact = new JSON5Object();
        contact.put("phone", "123-456-7890");
        
        json.put("profile", profile);
        json.put("contact", contact);
        
        // When & Then
        Object emailValue = JSON5PathExtractor.extractValue(json, "profile.email");
        assertFalse(JSON5PathExtractor.isMissingValue(emailValue));
        assertEquals("john@example.com", emailValue);
        
        Object phoneValue = JSON5PathExtractor.extractValue(json, "contact.phone");
        assertFalse(JSON5PathExtractor.isMissingValue(phoneValue));
        assertEquals("123-456-7890", phoneValue);
    }
    
    @Test
    @DisplayName("깊이 3단계 중첩 경로 추출 테스트")
    void testDeepNestedPathExtraction() {
        // Given
        JSON5Object json = new JSON5Object();
        JSON5Object user = new JSON5Object();
        JSON5Object settings = new JSON5Object();
        JSON5Object theme = new JSON5Object();
        
        theme.put("color", "dark");
        theme.put("fontSize", 14);
        
        settings.put("theme", theme);
        user.put("settings", settings);
        json.put("user", user);
        
        // When & Then
        Object colorValue = JSON5PathExtractor.extractValue(json, "user.settings.theme.color");
        assertFalse(JSON5PathExtractor.isMissingValue(colorValue));
        assertEquals("dark", colorValue);
        
        Object fontSizeValue = JSON5PathExtractor.extractValue(json, "user.settings.theme.fontSize");
        assertFalse(JSON5PathExtractor.isMissingValue(fontSizeValue));
        assertEquals(14, fontSizeValue);
    }
    
    @Test
    @DisplayName("존재하지 않는 경로 테스트")
    void testNonExistentPath() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("name", "John");
        
        // When & Then
        Object missingValue = JSON5PathExtractor.extractValue(json, "nonexistent");
        assertTrue(JSON5PathExtractor.isMissingValue(missingValue));
        
        Object nestedMissingValue = JSON5PathExtractor.extractValue(json, "profile.email");
        assertTrue(JSON5PathExtractor.isMissingValue(nestedMissingValue));
        
        Object deepMissingValue = JSON5PathExtractor.extractValue(json, "user.profile.settings.theme");
        assertTrue(JSON5PathExtractor.isMissingValue(deepMissingValue));
    }
    
    @Test
    @DisplayName("배열 인덱스 접근 테스트")
    void testArrayIndexAccess() {
        // Given
        JSON5Object json = new JSON5Object();
        JSON5Array items = new JSON5Array();
        items.put("item0");
        items.put("item1");
        items.put("item2");
        
        json.put("items", items);
        
        // When & Then
        Object item0 = JSON5PathExtractor.extractValue(json, "items.0");
        assertFalse(JSON5PathExtractor.isMissingValue(item0));
        assertEquals("item0", item0);
        
        Object item1 = JSON5PathExtractor.extractValue(json, "items.1");
        assertFalse(JSON5PathExtractor.isMissingValue(item1));
        assertEquals("item1", item1);
        
        // 존재하지 않는 인덱스
        Object missingItem = JSON5PathExtractor.extractValue(json, "items.5");
        assertTrue(JSON5PathExtractor.isMissingValue(missingItem));
    }
    
    @Test
    @DisplayName("null 값 처리 테스트")
    void testNullValues() {
        // Given
        JSON5Object json = new JSON5Object();
        json.put("nullValue", (String) null);
        
        // When & Then
        Object nullValue = JSON5PathExtractor.extractValue(json, "nullValue");
        assertFalse(JSON5PathExtractor.isMissingValue(nullValue));
        assertNull(nullValue);
    }
    
    @Test
    @DisplayName("경로 파싱 테스트")
    void testPathParsing() {
        // When & Then
        String[] simple = JSON5PathExtractor.parsePath("name");
        assertArrayEquals(new String[]{"name"}, simple);
        
        String[] nested = JSON5PathExtractor.parsePath("user.profile.email");
        assertArrayEquals(new String[]{"user", "profile", "email"}, nested);
        
        String[] empty = JSON5PathExtractor.parsePath("");
        assertArrayEquals(new String[0], empty);
        
        String[] nullPath = JSON5PathExtractor.parsePath(null);
        assertArrayEquals(new String[0], nullPath);
        
        String[] withSpaces = JSON5PathExtractor.parsePath("  user.profile.email  ");
        assertArrayEquals(new String[]{"user", "profile", "email"}, withSpaces);
    }
    
    @Test
    @DisplayName("경로 존재 여부 확인 테스트")
    void testPathExists() {
        // Given
        JSON5Object json = new JSON5Object();
        JSON5Object profile = new JSON5Object();
        profile.put("email", "john@example.com");
        json.put("profile", profile);
        json.put("name", "John");
        
        // When & Then
        assertTrue(JSON5PathExtractor.pathExists(json, "name"));
        assertTrue(JSON5PathExtractor.pathExists(json, "profile.email"));
        assertFalse(JSON5PathExtractor.pathExists(json, "age"));
        assertFalse(JSON5PathExtractor.pathExists(json, "profile.phone"));
        assertFalse(JSON5PathExtractor.pathExists(json, "user.settings.theme"));
    }
    
    @Test
    @DisplayName("잘못된 입력값 처리 테스트")
    void testInvalidInputs() {
        // When & Then
        Object nullElement = JSON5PathExtractor.extractValue(null, "path");
        assertTrue(JSON5PathExtractor.isMissingValue(nullElement));
        
        JSON5Object json = new JSON5Object();
        Object nullPath = JSON5PathExtractor.extractValue(json, null);
        assertTrue(JSON5PathExtractor.isMissingValue(nullPath));
        
        Object emptyPath = JSON5PathExtractor.extractValue(json, "");
        assertTrue(JSON5PathExtractor.isMissingValue(emptyPath));
        
        Object whitespacePath = JSON5PathExtractor.extractValue(json, "   ");
        assertTrue(JSON5PathExtractor.isMissingValue(whitespacePath));
    }
}
