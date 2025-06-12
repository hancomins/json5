package com.hancomins.json5.serializer.path;

import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.PathItem;

import java.util.List;

/**
 * JSON에서 중첩된 경로의 값을 추출합니다.
 * PathItem을 활용하여 고도화된 경로 파싱을 지원합니다.
 * "user.profile.email", "users[0].name", "data.items[2].value" 같은 경로를 지원합니다.
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
     * @param path 추출할 경로 (예: "user.profile.email", "users[0].name")
     * @return 추출된 값 또는 MISSING_VALUE_MARKER
     */
    public static Object extractValue(JSON5Element json5Element, String path) {
        if (json5Element == null || path == null || path.trim().isEmpty()) {
            return MISSING_VALUE_MARKER;
        }
        
        try {
            // PathItem을 사용한 고도화된 경로 파싱
            List<PathItem> pathItems = PathItem.parseMultiPath2(path);
            Object current = json5Element;
            
            for (PathItem pathItem : pathItems) {
                if (current == null) {
                    return MISSING_VALUE_MARKER;
                }
                
                current = extractValueFromPathItem(current, pathItem);
                if (current == MISSING_VALUE_MARKER) {
                    return MISSING_VALUE_MARKER;
                }
            }
            
            return current;
        } catch (Exception e) {
            // PathItem 파싱 실패 시 기존 방식으로 fallback
            return extractValueLegacy(json5Element, path);
        }
    }
    
    /**
     * PathItem을 사용하여 값을 추출합니다.
     */
    private static Object extractValueFromPathItem(Object current, PathItem pathItem) {
        if (pathItem.isEndPoint()) {
            // 최종 경로 요소
            return extractFinalValue(current, pathItem);
        } else {
            // 중간 경로 요소
            return extractIntermediateValue(current, pathItem);
        }
    }
    
    /**
     * 최종 값을 추출합니다.
     */
    private static Object extractFinalValue(Object current, PathItem pathItem) {
        if (pathItem.isInArray()) {
            if (pathItem.isObject()) {
                // 배열 내 객체의 속성 접근: items[0].name
                if (!(current instanceof JSON5Array)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Array array = (JSON5Array) current;
                int index = pathItem.getIndex();
                if (index < 0 || index >= array.size()) {
                    return MISSING_VALUE_MARKER;
                }
                Object element = array.get(index);
                if (element == null || !(element instanceof JSON5Object)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Object obj = (JSON5Object) element;
                return obj.has(pathItem.getName()) ? obj.get(pathItem.getName()) : MISSING_VALUE_MARKER;
            } else {
                // 배열 요소 직접 접근: items[0]
                if (!(current instanceof JSON5Array)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Array array = (JSON5Array) current;
                int index = pathItem.getIndex();
                if (index < 0 || index >= array.size()) {
                    return MISSING_VALUE_MARKER;
                }
                return array.get(index);
            }
        } else if (pathItem.isArrayValue() && pathItem.getIndex() == -1) {
            // 음수 인덱스가 있는 단일 PathItem 처리: numbers[-1]
            // 이 경우 음수 인덱스는 지원하지 않으므로 MISSING_VALUE_MARKER 반환
            return MISSING_VALUE_MARKER;
        } else {
            // 일반적인 객체 속성 접근: name
            if (!(current instanceof JSON5Object)) {
                return MISSING_VALUE_MARKER;
            }
            JSON5Object obj = (JSON5Object) current;
            return obj.has(pathItem.getName()) ? obj.get(pathItem.getName()) : MISSING_VALUE_MARKER;
        }
    }
    
    /**
     * 중간 값을 추출합니다 (경로 탐색 계속).
     */
    private static Object extractIntermediateValue(Object current, PathItem pathItem) {
        if (pathItem.isInArray()) {
            if (pathItem.isObject()) {
                // 배열 내 객체 접근 후 속성 접근: users[0].profile
                if (!(current instanceof JSON5Array)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Array array = (JSON5Array) current;
                int index = pathItem.getIndex();
                if (index < 0 || index >= array.size()) {
                    return MISSING_VALUE_MARKER;
                }
                Object element = array.get(index);
                if (!(element instanceof JSON5Object)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Object obj = (JSON5Object) element;
                return obj.has(pathItem.getName()) ? obj.get(pathItem.getName()) : MISSING_VALUE_MARKER;
            } else {
                // 배열 요소 접근: users[0]
                if (!(current instanceof JSON5Array)) {
                    return MISSING_VALUE_MARKER;
                }
                JSON5Array array = (JSON5Array) current;
                int index = pathItem.getIndex();
                if (index < 0 || index >= array.size()) {
                    return MISSING_VALUE_MARKER;
                }
                return array.get(index);
            }
        } else if (pathItem.isArrayValue()) {
            // 배열 이름에 접근하여 배열 객체를 반환: users (from users[0].name)
            if (!(current instanceof JSON5Object)) {
                return MISSING_VALUE_MARKER;
            }
            JSON5Object obj = (JSON5Object) current;
            return obj.has(pathItem.getName()) ? obj.get(pathItem.getName()) : MISSING_VALUE_MARKER;
        } else {
            // 일반적인 객체 속성 접근: user
            if (!(current instanceof JSON5Object)) {
                return MISSING_VALUE_MARKER;
            }
            JSON5Object obj = (JSON5Object) current;
            return obj.has(pathItem.getName()) ? obj.get(pathItem.getName()) : MISSING_VALUE_MARKER;
        }
    }
    
    /**
     * 기존 방식의 경로 추출 (fallback용)
     * PathItem 파싱 실패 시에만 사용됩니다.
     */
    private static Object extractValueLegacy(JSON5Element json5Element, String path) {
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
     * 경로를 파싱하여 개별 키들로 분리합니다. (Legacy 지원용)
     * 
     * @param path 파싱할 경로
     * @return 분리된 키 배열
     * @deprecated PathItem.parseMultiPath2() 사용을 권장합니다.
     */
    @Deprecated
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
    
    /**
     * 경로의 복잡도를 확인합니다.
     * 
     * @param path 확인할 경로
     * @return true: 배열 인덱스 포함된 복잡한 경로, false: 단순한 점 구분 경로
     */
    public static boolean isComplexPath(String path) {
        return path != null && (path.contains("[") || path.contains("]"));
    }
    
    /**
     * PathItem 리스트를 반환합니다. (고급 사용자용)
     * 
     * @param path 파싱할 경로
     * @return PathItem 리스트
     */
    public static List<PathItem> parseToPathItems(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        
        try {
            List<PathItem> items = PathItem.parseMultiPath2(path);
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Path resulted in empty PathItem list: " + path);
            }
            return items;
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalArgumentException("Invalid path format: " + path, e);
        }
    }
}
