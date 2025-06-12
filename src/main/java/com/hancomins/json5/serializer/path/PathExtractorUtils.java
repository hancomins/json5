package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.PathItem;

import java.util.List;

/**
 * 경로 처리 관련 공통 유틸리티 클래스
 * JSON5Path, NodePath, JSON5PathExtractor 간의 공통 로직을 제공합니다.
 */
public class PathExtractorUtils {
    
    /**
     * 표준 경로 파싱 방법
     * 모든 경로 처리 클래스에서 동일한 파싱 로직을 사용하도록 보장합니다.
     */
    public static List<PathItem> parseStandardPath(String path) {
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
}
