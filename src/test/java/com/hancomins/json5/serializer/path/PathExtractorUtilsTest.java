package com.hancomins.json5.serializer.path;

import com.hancomins.json5.PathItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathExtractorUtils 테스트")
class PathExtractorUtilsTest {
    
    @Test
    @DisplayName("표준 경로 파싱 테스트")
    void shouldParseStandardPath() {
        // 단순 경로
        List<PathItem> simple = PathExtractorUtils.parseStandardPath("name");
        assertEquals(1, simple.size());
        assertEquals("name", simple.get(0).getName());
        
        // 중첩 경로
        List<PathItem> nested = PathExtractorUtils.parseStandardPath("user.profile.email");
        assertEquals(3, nested.size());
        assertEquals("user", nested.get(0).getName());
        assertEquals("profile", nested.get(1).getName());
        assertEquals("email", nested.get(2).getName());
        
        // 배열 인덱스 경로
        List<PathItem> array = PathExtractorUtils.parseStandardPath("users[0].name");
        assertEquals(2, array.size());
        assertEquals("users", array.get(0).getName());
        assertTrue(array.get(0).isArrayValue());
        assertEquals("name", array.get(1).getName());
    }
    
    @Test
    @DisplayName("잘못된 경로 파싱 예외 테스트")
    void shouldThrowExceptionForInvalidPaths() {
        // null 경로
        assertThrows(IllegalArgumentException.class, () -> {
            PathExtractorUtils.parseStandardPath(null);
        });
        
        // 빈 경로
        assertThrows(IllegalArgumentException.class, () -> {
            PathExtractorUtils.parseStandardPath("");
        });
        
        // 공백 경로
        assertThrows(IllegalArgumentException.class, () -> {
            PathExtractorUtils.parseStandardPath("   ");
        });
    }
    
    @Test
    @DisplayName("경로 유효성 검증 테스트")
    void shouldValidatePaths() {
        // 유효한 경로들
        assertTrue(PathExtractorUtils.isValidPath("name"));
        assertTrue(PathExtractorUtils.isValidPath("user.name"));
        assertTrue(PathExtractorUtils.isValidPath("users[0].name"));
        assertTrue(PathExtractorUtils.isValidPath("companies[0].departments[0].employees[0].name"));
        
        // 무효한 경로들
        assertFalse(PathExtractorUtils.isValidPath(null));
        assertFalse(PathExtractorUtils.isValidPath(""));
        assertFalse(PathExtractorUtils.isValidPath("   "));
    }
    
    @Test
    @DisplayName("경로 정규화 테스트")
    void shouldNormalizePaths() {
        // 공백 제거
        assertEquals("user.name", PathExtractorUtils.normalizePath("  user.name  "));
        
        // 연속된 점 제거
        assertEquals("user.name", PathExtractorUtils.normalizePath("user..name"));
        assertEquals("user.profile.email", PathExtractorUtils.normalizePath("user...profile..email"));
        
        // 시작/끝 점 제거
        assertEquals("user.name", PathExtractorUtils.normalizePath(".user.name"));
        assertEquals("user.name", PathExtractorUtils.normalizePath("user.name."));
        assertEquals("user.name", PathExtractorUtils.normalizePath(".user.name."));
        
        // 복합 정규화
        assertEquals("user.profile.email", PathExtractorUtils.normalizePath("  .user..profile...email.  "));
        
        // null 처리
        assertEquals("", PathExtractorUtils.normalizePath(null));
        
        // 빈 문자열 처리
        assertEquals("", PathExtractorUtils.normalizePath(""));
        assertEquals("", PathExtractorUtils.normalizePath("   "));
    }
    
    @Test
    @DisplayName("경로 타입 분석 테스트")
    void shouldAnalyzePathTypes() {
        // SIMPLE 타입
        assertEquals(PathExtractorUtils.PathType.SIMPLE, PathExtractorUtils.analyzePathType("name"));
        assertEquals(PathExtractorUtils.PathType.SIMPLE, PathExtractorUtils.analyzePathType("age"));
        
        // NESTED 타입
        assertEquals(PathExtractorUtils.PathType.NESTED, PathExtractorUtils.analyzePathType("user.name"));
        assertEquals(PathExtractorUtils.PathType.NESTED, PathExtractorUtils.analyzePathType("user.profile.email"));
        assertEquals(PathExtractorUtils.PathType.NESTED, PathExtractorUtils.analyzePathType("settings.theme.color"));
        
        // COMPLEX 타입 (배열 인덱스 포함)
        assertEquals(PathExtractorUtils.PathType.COMPLEX, PathExtractorUtils.analyzePathType("users[0]"));
        assertEquals(PathExtractorUtils.PathType.COMPLEX, PathExtractorUtils.analyzePathType("users[0].name"));
        assertEquals(PathExtractorUtils.PathType.COMPLEX, PathExtractorUtils.analyzePathType("companies[0].departments[0].employees[0].name"));
        
        // INVALID 타입
        assertEquals(PathExtractorUtils.PathType.INVALID, PathExtractorUtils.analyzePathType(null));
        assertEquals(PathExtractorUtils.PathType.INVALID, PathExtractorUtils.analyzePathType(""));
        assertEquals(PathExtractorUtils.PathType.INVALID, PathExtractorUtils.analyzePathType("   "));
    }
    
    @Test
    @DisplayName("경로 타입별 처리 일관성 테스트")
    void shouldHandlePathTypesConsistently() {
        // SIMPLE 경로들
        String[] simplePaths = {"name", "age", "status"};
        for (String path : simplePaths) {
            assertEquals(PathExtractorUtils.PathType.SIMPLE, PathExtractorUtils.analyzePathType(path));
            assertTrue(PathExtractorUtils.isValidPath(path));
        }
        
        // NESTED 경로들
        String[] nestedPaths = {"user.name", "profile.email", "settings.theme.color"};
        for (String path : nestedPaths) {
            assertEquals(PathExtractorUtils.PathType.NESTED, PathExtractorUtils.analyzePathType(path));
            assertTrue(PathExtractorUtils.isValidPath(path));
        }
        
        // COMPLEX 경로들
        String[] complexPaths = {"users[0]", "items[1].name", "data[0].values[1].amount"};
        for (String path : complexPaths) {
            assertEquals(PathExtractorUtils.PathType.COMPLEX, PathExtractorUtils.analyzePathType(path));
            assertTrue(PathExtractorUtils.isValidPath(path));
        }
    }
    
    @Test
    @DisplayName("정규화와 타입 분석 조합 테스트")
    void shouldCombineNormalizationAndTypeAnalysis() {
        // 정규화 후 타입 분석
        String messyPath = "  .user..profile...email.  ";
        String normalizedPath = PathExtractorUtils.normalizePath(messyPath);
        assertEquals("user.profile.email", normalizedPath);
        assertEquals(PathExtractorUtils.PathType.NESTED, PathExtractorUtils.analyzePathType(normalizedPath));
        
        // 배열 경로 정규화는 영향 없음
        String arrayPath = "  users[0].name  ";
        String normalizedArrayPath = PathExtractorUtils.normalizePath(arrayPath);
        assertEquals("users[0].name", normalizedArrayPath);
        assertEquals(PathExtractorUtils.PathType.COMPLEX, PathExtractorUtils.analyzePathType(normalizedArrayPath));
    }
}
