package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5ValueExtractor;
import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @JSON5ValueExtractor 메서드를 분석합니다.
 */
public class ExtractorAnalyzer {
    
    /**
     * 값 추출자 메서드를 찾고 검증합니다.
     */
    public ExtractorInfo analyzeValueExtractor(Class<?> clazz) {
        List<Method> methods = ReflectionUtils.getAllInheritedMethods(clazz);
        
        List<Method> extractors = methods.stream()
            .filter(method -> method.isAnnotationPresent(JSON5ValueExtractor.class))
            .collect(Collectors.toList());
        
        if (extractors.isEmpty()) {
            throw new JSON5SerializerException(
                "No @JSON5ValueExtractor method found in class: " + clazz.getName());
        }
        
        if (extractors.size() > 1) {
            throw new JSON5SerializerException(
                "Multiple @JSON5ValueExtractor methods found in class: " + clazz.getName() + 
                ". Only one is allowed.");
        }
        
        Method method = extractors.get(0);
        
        // 반환값 검증
        if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            throw new JSON5SerializerException(
                "Extractor method cannot have void return type: " + method.getName());
        }
        
        // 파라미터 검증
        if (method.getParameterCount() != 0) {
            throw new JSON5SerializerException(
                "Extractor method must have no parameters: " + method.getName());
        }
        
        // 변환 가능한 타입인지 검증
        if (!isConvertibleType(method.getReturnType())) {
            throw new JSON5SerializerException(
                "Extractor method return type is not convertible: " + method.getReturnType().getName());
        }
        
        method.setAccessible(true);
        return new ExtractorInfo(method);
    }
    
    /**
     * 변환 가능한 타입인지 확인
     */
    private boolean isConvertibleType(Class<?> type) {
        return type == String.class || 
               type == Integer.class || type == int.class ||
               type == Long.class || type == long.class ||
               type == Double.class || type == double.class ||
               type == Float.class || type == float.class ||
               type == Boolean.class || type == boolean.class ||
               type == Byte.class || type == byte.class ||
               type == Short.class || type == short.class ||
               type == Character.class || type == char.class ||
               type == byte[].class ||
               type.getName().contains("JSON5Object") ||  // JSON5Object 클래스 확인
               type.getName().contains("JSON5Array");     // JSON5Array 클래스 확인
    }
}
