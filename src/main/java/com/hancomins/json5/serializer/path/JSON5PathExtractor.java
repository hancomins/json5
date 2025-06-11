package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;

/**
 * JSON에서 중첩된 경로의 값을 추출합니다.
 * "user.profile.email" 같은 경로를 파싱하여 값을 가져옵니다.
 */
public class JSON5PathExtractor {
    
    /**
     * 값이 존재하지 않음을 나타내는 마커 객체
     */
    public static final Object MISSING_VALUE_MARKER = new Object();
    
    /**
     * 경로에서 값을 추출합니다.
     * 
     * @param json5Element 루트 JSON 요소
     * @param path 추출할 경로 (예: "user.profile.email")
     * @return 추출된 값 또는 MISSING_VALUE_MARKER
     */
    public static Object extractValue(JSON5Element json5Element, String path) {
        if (json5Element == null || path == null || path.trim().isEmpty()) {
            return MISSING_VALUE_MARKER;
        }
        
        String[] pathSegments = parsePath(path);
        Object current = json5Element;
        
        for (String segment : pathSegments) {
            if (current == null) {
                return MISSING_VALUE_MARKER;
            }
            
            if (current instanceof JSON5Object) {
                JSON5Object jsonObject = (JSON5Object) current;
                if (!jsonObject.has(segment)) {
                    return MISSING_VALUE_MARKER;
                }
                current = jsonObject.get(segment);
            } else if (current instanceof JSON5Array) {
                // 배열 인덱스 접근 시도
                try {
                    int index = Integer.parseInt(segment);
                    JSON5Array jsonArray = (JSON5Array) current;
                    if (index < 0 || index >= jsonArray.size()) {
                        return MISSING_VALUE_MARKER;
                    }
                    current = jsonArray.get(index);
                } catch (NumberFormatException e) {
                    return MISSING_VALUE_MARKER;
                }
            } else {
                // 원시 값에 대한 추가 경로 접근은 불가능
                return MISSING_VALUE_MARKER;
            }
        }
        
        return current;
    }
    
    /**
     * 경로가 존재하는지 확인합니다.
     * 
     * @param json5Element 루트 JSON 요소
     * @param path 확인할 경로
     * @return 경로 존재 여부
     */
    public static boolean pathExists(JSON5Element json5Element, String path) {
        return extractValue(json5Element, path) != MISSING_VALUE_MARKER;
    }
    
    /**
     * 경로를 파싱하여 개별 키들로 분리합니다.
     * 
     * @param path 파싱할 경로
     * @return 분리된 키 배열
     */
    public static String[] parsePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return new String[0];
        }
        
        return path.trim().split("\\.");
    }
    
    /**
     * 추출된 값이 누락된 값인지 확인합니다.
     * 
     * @param value 확인할 값
     * @return 누락된 값 여부
     */
    public static boolean isMissingValue(Object value) {
        return value == MISSING_VALUE_MARKER;
    }
}
