package com.hancomins.json5.serializer.path;

import com.hancomins.json5.PathItem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 경로 처리 관련 공통 유틸리티 클래스
 * JSON5Path, NodePath, JSON5PathExtractor 간의 공통 로직을 제공합니다.
 * 성능 최적화를 위한 캐싱 기능도 포함합니다.
 */
public class PathExtractorUtils {
    
    /**
     * PathItem 파싱 결과 캐시 (성능 최적화)
     * 동일한 경로를 반복적으로 파싱하는 비용을 절약합니다.
     */
    private static final Map<String, List<PathItem>> PATH_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 캐시 최대 크기 (메모리 사용량 제한)
     */
    private static final int MAX_CACHE_SIZE = 1000;
    
    /**
     * 표준 경로 파싱 방법 (캐싱 적용)
     * 모든 경로 처리 클래스에서 동일한 파싱 로직을 사용하도록 보장합니다.
     * 동일한 경로의 반복 파싱을 방지하여 성능을 최적화합니다.
     */
    public static List<PathItem> parseStandardPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        // 캐시에서 먼저 확인
        List<PathItem> cached = PATH_CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        
        try {
            List<PathItem> result = PathItem.parseMultiPath2(path);
            
            // 캐시 크기 제한 확인 후 저장
            if (PATH_CACHE.size() < MAX_CACHE_SIZE) {
                PATH_CACHE.put(path, result);
            }
            
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format: " + path, e);
        }
    }
    
    /**
     * 캐싱 없이 직접 파싱 (캐시 우회 필요 시 사용)
     */
    @SuppressWarnings("UnusedReturnValue")
    public static List<PathItem> parsePathDirect(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        try {
            return PathItem.parseMultiPath2(path);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path format: " + path, e);
        }
    }
    
    /**
     * 경로 유효성 검증
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        try {
            PathItem.parseMultiPath2(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 경로 정규화 (앞뒤 공백 제거, 연속된 점 제거 등)
     */
    public static String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        
        return path.trim()
                   .replaceAll("\\.{2,}", ".")  // 연속된 점 제거
                   .replaceAll("^\\.", "")      // 시작 점 제거
                   .replaceAll("\\.$", "");     // 끝 점 제거
    }
    
    /**
     * 경로 타입 분석
     */
    public static PathType analyzePathType(String path) {
        if (path == null || path.trim().isEmpty()) {
            return PathType.INVALID;
        }
        
        if (path.contains("[") && path.contains("]")) {
            return PathType.COMPLEX; // 배열 인덱스 포함
        } else if (path.contains(".")) {
            return PathType.NESTED;  // 중첩 객체
        } else {
            return PathType.SIMPLE;  // 단순 키
        }
    }
    
    /**
     * 경로 타입 열거형
     */
    public enum PathType {
        SIMPLE,   // "name"
        NESTED,   // "user.name"
        COMPLEX,  // "users[0].name"
        INVALID   // null, 빈 문자열 등
    }
    
    /**
     * 캐시 상태 정보를 반환합니다.
     */
    public static String getCacheStats() {
        return String.format("PathExtractorUtils Cache: %d/%d entries", 
                           PATH_CACHE.size(), MAX_CACHE_SIZE);
    }
    
    /**
     * 캐시를 지웁니다. (테스트 용도 또는 메모리 절약)
     */
    public static void clearCache() {
        PATH_CACHE.clear();
    }
    
    /**
     * 캐시 크기를 반환합니다.
     */
    public static int getCacheSize() {
        return PATH_CACHE.size();
    }
    
    /**
     * 경로가 캐시되어 있는지 확인합니다.
     */
    public static boolean isCached(String path) {
        return PATH_CACHE.containsKey(path);
    }
}
