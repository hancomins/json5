package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Path;
import com.hancomins.json5.PathItem;


import com.hancomins.json5.serializer.path.JSON5PathExtractor;
import com.hancomins.json5.serializer.path.PathExtractorUtils;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON5PathExtractor 통합 페이즈3: 최적화 및 정리 테스트
 * 
 * 이 테스트는 다음을 검증합니다:
 * 1. PathExtractorUtils 캐싱 성능 최적화
 * 2. 모든 경로 처리 클래스 간 일관성
 * 3. NodePath와 JSON5PathExtractor 통합
 * 4. 성능 향상 검증
 */
@DisplayName("페이즈3: 최적화 및 정리 테스트")
class Phase3OptimizationTest {
    
    private JSON5Object testData;
    
    @BeforeEach
    void setUp() {
        // 복합 테스트 데이터 생성
        testData = new JSON5Object();
        
        // 사용자 정보
        JSON5Object user = new JSON5Object();
        user.put("name", "John Doe");
        user.put("age", 30);
        
        // 프로필 정보
        JSON5Object profile = new JSON5Object();
        profile.put("email", "john@example.com");
        profile.put("department", "Engineering");
        user.put("profile", profile);
        
        // 연락처 배열
        JSON5Array contacts = new JSON5Array();
        JSON5Object contact1 = new JSON5Object();
        contact1.put("type", "home");
        contact1.put("number", "123-456-7890");
        contacts.add(contact1);
        
        JSON5Object contact2 = new JSON5Object();
        contact2.put("type", "work");
        contact2.put("number", "098-765-4321");
        contacts.add(contact2);
        
        user.put("contacts", contacts);
        testData.put("user", user);
        
        // 설정 정보
        JSON5Object settings = new JSON5Object();
        settings.put("theme", "dark");
        settings.put("language", "en");
        testData.put("settings", settings);
        
        // 캐시 초기화
        JSON5PathExtractor.clearCache();
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 캐시 정리
        JSON5PathExtractor.clearCache();
    }
    
    @Test
    @DisplayName("PathExtractorUtils 캐싱 성능 테스트")
    void shouldDemonstrateCachingPerformance() {
        String complexPath = "user.contacts[0].number";
        
        // 첫 번째 파싱 (캐시 미스)
        Assertions.assertFalse(PathExtractorUtils.isCached(complexPath));
        
        long startTime = System.nanoTime();
        List<PathItem> firstParse = PathExtractorUtils.parseStandardPath(complexPath);
        long firstParseTime = System.nanoTime() - startTime;
        
        // 캐시 확인
        assertTrue(PathExtractorUtils.isCached(complexPath));
        assertEquals(1, PathExtractorUtils.getCacheSize());
        
        // 두 번째 파싱 (캐시 히트)
        startTime = System.nanoTime();
        List<PathItem> secondParse = PathExtractorUtils.parseStandardPath(complexPath);
        long secondParseTime = System.nanoTime() - startTime;
        
        // 결과 동일성 확인
        assertEquals(firstParse.size(), secondParse.size());
        
        // 캐시된 파싱이 더 빠른지 확인 (일반적으로 그래야 함)
        System.out.println("첫 번째 파싱: " + firstParseTime + "ns");
        System.out.println("두 번째 파싱(캐시됨): " + secondParseTime + "ns");
        System.out.println("캐시 상태: " + PathExtractorUtils.getCacheStats());
        
        // 캐시 효과 검증 (두 번째가 더 빠르거나 비슷해야 함)
        assertTrue(secondParseTime <= firstParseTime * 2, "캐시된 파싱이 너무 느림");
    }
    
    @Test
    @DisplayName("JSON5Path와 JSON5PathExtractor 일관성 테스트")
    void shouldMaintainConsistencyBetweenJSON5PathAndExtractor() {
        JSON5Path jsonPath = new JSON5Path(testData);
        
        String[] testPaths = {
            "user.name",
            "user.profile.email",
            "user.contacts[0].type",
            "user.contacts[1].number",
            "settings.theme",
            "user.age"
        };
        
        for (String path : testPaths) {
            Object pathResult = jsonPath.get(path);
            Object extractorResult = JSON5PathExtractor.extractValue(testData, path);
            
            // MISSING_VALUE_MARKER 처리
            if (JSON5PathExtractor.isMissingValue(extractorResult)) {
                assertNull(pathResult, "Path: " + path + " - JSON5Path should return null when extractor returns MISSING_VALUE_MARKER");
            } else {
                assertEquals(pathResult, extractorResult, "Path: " + path + " - Results should be identical");
            }
            
            // has() 메서드 일관성 확인
            boolean pathExists = jsonPath.has(path);
            boolean extractorExists = JSON5PathExtractor.pathExists(testData, path);
            assertEquals(pathExists, extractorExists, "Path: " + path + " - has() methods should be consistent");
        }
    }
    
    @Test
    @DisplayName("NodePath 최적화 테스트")
    void shouldTestOptimizedNodePath() {
        // NodePath 테스트를 위한 간단한 스키마 생성
        // 실제 NodePath는 스키마 기반이므로 간단한 테스트만 수행
        
        // null/empty 경로 처리 테스트
        SchemaObjectNode testNode = new SchemaObjectNode();
        NodePath nodePath = new NodePath(testNode) {};
        
        assertNull(nodePath.get(null));
        assertNull(nodePath.get(""));
        assertNull(nodePath.get("   "));
        
        assertFalse(nodePath.has(null));
        assertFalse(nodePath.has(""));
        assertFalse(nodePath.has("   "));
    }
    
    @Test
    @DisplayName("복합 경로 파싱 성능 비교")
    void shouldCompareComplexPathParsingPerformance() {
        String[] complexPaths = {
            "user.profile.email",
            "user.contacts[0].type",
            "user.contacts[1].number",
            "settings.theme",
            "user.profile.department"
        };
        
        // 여러 번 반복하여 캐싱 효과 측정
        int iterations = 100;
        
        long totalDirectTime = 0;
        long totalCachedTime = 0;
        
        // 먼저 캐시 워밍업
        for (String path : complexPaths) {
            PathExtractorUtils.parseStandardPath(path);
        }
        
        // 직접 파싱 시간 측정
        for (int i = 0; i < iterations; i++) {
            for (String path : complexPaths) {
                long startTime = System.nanoTime();
                PathExtractorUtils.parsePathDirect(path);
                totalDirectTime += System.nanoTime() - startTime;
            }
        }
        
        // 캐시된 파싱 시간 측정
        for (int i = 0; i < iterations; i++) {
            for (String path : complexPaths) {
                long startTime = System.nanoTime();
                PathExtractorUtils.parseStandardPath(path);
                totalCachedTime += System.nanoTime() - startTime;
            }
        }
        
        double avgDirectTime = (double) totalDirectTime / (iterations * complexPaths.length);
        double avgCachedTime = (double) totalCachedTime / (iterations * complexPaths.length);
        
        System.out.println("평균 직접 파싱 시간: " + avgDirectTime + "ns");
        System.out.println("평균 캐시 파싱 시간: " + avgCachedTime + "ns");
        System.out.println("성능 향상 비율: " + (avgDirectTime / avgCachedTime) + "x");
        System.out.println("최종 캐시 상태: " + JSON5PathExtractor.getPerformanceStats());
        
        // 캐시된 파싱이 직접 파싱보다 빠르거나 비슷해야 함
        assertTrue(avgCachedTime <= avgDirectTime * 1.5, "캐시된 파싱이 예상보다 느림");
    }
    
    @Test
    @DisplayName("PathExtractorUtils 타입 분석 테스트")
    void shouldAnalyzePathTypes() {
        assertEquals(PathExtractorUtils.PathType.SIMPLE, 
                    PathExtractorUtils.analyzePathType("name"));
        assertEquals(PathExtractorUtils.PathType.NESTED, 
                    PathExtractorUtils.analyzePathType("user.name"));
        assertEquals(PathExtractorUtils.PathType.COMPLEX, 
                    PathExtractorUtils.analyzePathType("users[0].name"));
        assertEquals(PathExtractorUtils.PathType.COMPLEX, 
                    PathExtractorUtils.analyzePathType("data.items[2].value"));
        assertEquals(PathExtractorUtils.PathType.INVALID, 
                    PathExtractorUtils.analyzePathType(null));
        assertEquals(PathExtractorUtils.PathType.INVALID, 
                    PathExtractorUtils.analyzePathType(""));
    }
    
    @Test
    @DisplayName("경로 정규화 테스트")
    void shouldNormalizePaths() {
        assertEquals("", PathExtractorUtils.normalizePath(null));
        assertEquals("", PathExtractorUtils.normalizePath(""));
        assertEquals("user.name", PathExtractorUtils.normalizePath("  user.name  "));
        assertEquals("user.name", PathExtractorUtils.normalizePath(".user.name."));
        assertEquals("user.name", PathExtractorUtils.normalizePath("user..name"));
        assertEquals("user.profile.email", 
                    PathExtractorUtils.normalizePath("..user...profile..email.."));
    }
    
    @Test
    @DisplayName("캐시 관리 기능 테스트")
    void shouldManageCache() {
        // 초기 상태 확인
        assertEquals(0, PathExtractorUtils.getCacheSize());
        
        // 몇 개의 경로 파싱
        String[] paths = {
            "user.name",
            "user.profile.email",
            "user.contacts[0].type"
        };
        
        for (String path : paths) {
            PathExtractorUtils.parseStandardPath(path);
        }
        
        // 캐시 크기 확인
        assertEquals(paths.length, PathExtractorUtils.getCacheSize());
        
        // 각 경로가 캐시되었는지 확인
        for (String path : paths) {
            assertTrue(PathExtractorUtils.isCached(path));
        }
        
        // 캐시 통계 문자열 확인
        String stats = PathExtractorUtils.getCacheStats();
        assertTrue(stats.contains("PathExtractorUtils Cache"));
        assertTrue(stats.contains(String.valueOf(paths.length)));
        
        // 캐시 지우기
        PathExtractorUtils.clearCache();
        assertEquals(0, PathExtractorUtils.getCacheSize());
        
        // 지워진 후 캐시 상태 확인
        for (String path : paths) {
            assertFalse(PathExtractorUtils.isCached(path));
        }
    }
    
    @Test
    @DisplayName("통합 기능 검증: 모든 경로 처리 방식 동일 결과")
    void shouldProduceIdenticalResultsAcrossAllPathProcessors() {
        String[] testPaths = {
            "user.name",
            "user.profile.email",
            "settings.theme"
        };
        
        JSON5Path jsonPath = new JSON5Path(testData);
        
        for (String path : testPaths) {
            // JSON5Path 결과
            Object pathResult = jsonPath.get(path);
            
            // JSON5PathExtractor 결과
            Object extractorResult = JSON5PathExtractor.extractValue(testData, path);
            if (JSON5PathExtractor.isMissingValue(extractorResult)) {
                extractorResult = null;
            }
            
            // PathExtractorUtils를 통한 파싱 결과 확인
            List<PathItem> pathItems = PathExtractorUtils.parseStandardPath(path);
            assertNotNull(pathItems);
            assertFalse(pathItems.isEmpty());
            
            // 결과 일치 확인
            assertEquals(pathResult, extractorResult, 
                        "Path: " + path + " - All path processors should produce identical results");
            
            System.out.println("Path: " + path + " -> Result: " + pathResult + 
                             " (PathItems: " + pathItems.size() + ")");
        }
    }
    
    @Test
    @DisplayName("복잡한 배열 경로 처리 검증")
    void shouldHandleComplexArrayPaths() {
        String[] arrayPaths = {
            "user.contacts[0].type",
            "user.contacts[0].number",
            "user.contacts[1].type",
            "user.contacts[1].number"
        };
        
        String[] expectedResults = {
            "home",
            "123-456-7890",
            "work",
            "098-765-4321"
        };
        
        JSON5Path jsonPath = new JSON5Path(testData);
        
        for (int i = 0; i < arrayPaths.length; i++) {
            String path = arrayPaths[i];
            String expected = expectedResults[i];
            
            // JSON5Path와 JSON5PathExtractor 모두 동일한 결과 반환 확인
            Object pathResult = jsonPath.get(path);
            Object extractorResult = JSON5PathExtractor.extractValue(testData, path);
            
            assertEquals(expected, pathResult, "JSON5Path result for: " + path);
            assertEquals(expected, extractorResult, "JSON5PathExtractor result for: " + path);
            
            // 경로 존재 확인
            assertTrue(jsonPath.has(path));
            assertTrue(JSON5PathExtractor.pathExists(testData, path));
        }
    }
    
    @Test
    @DisplayName("에러 상황 처리 일관성 테스트")
    void shouldHandleErrorConsistently() {
        String[] invalidPaths = {
            "user.nonexistent",
            "user.contacts[5].type",  // 인덱스 범위 초과
            "settings.nonexistent.deep"
        };
        
        JSON5Path jsonPath = new JSON5Path(testData);
        
        for (String path : invalidPaths) {
            // JSON5Path는 null 반환
            Object pathResult = jsonPath.get(path);
            assertNull(pathResult, "JSON5Path should return null for invalid path: " + path);
            
            // JSON5PathExtractor는 MISSING_VALUE_MARKER 반환
            Object extractorResult = JSON5PathExtractor.extractValue(testData, path);
            assertTrue(JSON5PathExtractor.isMissingValue(extractorResult), 
                      "JSON5PathExtractor should return MISSING_VALUE_MARKER for invalid path: " + path);
            
            // 둘 다 경로가 존재하지 않는다고 보고해야 함
            assertFalse(jsonPath.has(path));
            assertFalse(JSON5PathExtractor.pathExists(testData, path));
        }
    }
    
    @Test
    @DisplayName("페이즈3 통합 완료 검증")
    void shouldVerifyPhase3Integration() {
        System.out.println("=== 페이즈3: 최적화 및 정리 통합 검증 ===");
        
        // 1. 캐싱 기능 확인
        JSON5PathExtractor.clearCache();
        assertEquals(0, PathExtractorUtils.getCacheSize());
        
        String testPath = "user.profile.email";
        JSON5PathExtractor.extractValue(testData, testPath);
        assertTrue(PathExtractorUtils.getCacheSize() > 0, "캐싱이 작동해야 함");
        
        // 2. 모든 경로 처리 방식의 일관성
        JSON5Path jsonPath = new JSON5Path(testData);
        Object pathResult = jsonPath.get(testPath);
        Object extractorResult = JSON5PathExtractor.extractValue(testData, testPath);
        assertEquals(pathResult, extractorResult, "모든 경로 처리 방식이 동일한 결과를 반환해야 함");
        
        // 3. 성능 통계 정보 가용성
        String stats = JSON5PathExtractor.getPerformanceStats();
        assertNotNull(stats);
        assertTrue(stats.contains("Cache"), "성능 통계에 캐시 정보가 포함되어야 함");
        
        // 4. NodePath 개선 확인 (null 안전성)
        SchemaObjectNode testNode = new SchemaObjectNode();
        NodePath nodePath = new NodePath(testNode) {};
        assertNull(nodePath.get(null));
        assertFalse(nodePath.has(null));
        
        System.out.println("✅ 캐싱 기능: 정상 작동");
        System.out.println("✅ 일관성 보장: 모든 경로 처리 방식 동일 결과");
        System.out.println("✅ 성능 최적화: " + stats);
        System.out.println("✅ NodePath 개선: null 안전성 확보");
        System.out.println("🎉 페이즈3: 최적화 및 정리 완료!");
    }
}
