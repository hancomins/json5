package com.hancomins.json5.serializer.constructor;

import com.hancomins.json5.serializer.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 클래스의 생성자를 분석하여 @JSON5Creator가 붙은 생성자를 찾고
 * 우선순위에 따라 정렬합니다.
 */
public class ConstructorAnalyzer {
    
    /**
     * 클래스의 모든 생성자를 분석하여 @JSON5Creator가 붙은 생성자들을 반환합니다.
     * 
     * @param clazz 분석할 클래스
     * @return @JSON5Creator가 붙은 생성자 정보 리스트 (우선순위 내림차순 정렬)
     */
    public List<ConstructorInfo> analyzeConstructors(Class<?> clazz) {
        if (clazz == null) {
            return new ArrayList<>();
        }
        
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        List<ConstructorInfo> creatorConstructors = new ArrayList<>();
        
        for (Constructor<?> constructor : constructors) {
            JSON5Creator creatorAnnotation = constructor.getAnnotation(JSON5Creator.class);
            if (creatorAnnotation != null) {
                ConstructorInfo constructorInfo = analyzeConstructor(constructor, creatorAnnotation);
                if (constructorInfo != null) {
                    creatorConstructors.add(constructorInfo);
                }
            }
        }
        
        // 우선순위 내림차순으로 정렬 (높은 우선순위가 먼저)
        creatorConstructors.sort(Comparator.comparingInt(ConstructorInfo::getPriority).reversed());
        
        return creatorConstructors;
    }
    
    /**
     * 클래스에 @JSON5Creator가 붙은 생성자가 있는지 확인합니다.
     * 
     * @param clazz 확인할 클래스
     * @return @JSON5Creator 생성자 존재 여부
     */
    public boolean hasCreatorConstructor(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(JSON5Creator.class) != null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 가장 적합한 생성자를 선택합니다.
     * 
     * @param constructors 생성자 정보 리스트
     * @return 가장 적합한 생성자 정보 (없으면 null)
     */
    public ConstructorInfo selectBestConstructor(List<ConstructorInfo> constructors) {
        if (constructors == null || constructors.isEmpty()) {
            return null;
        }
        
        // 이미 우선순위 순으로 정렬되어 있으므로 첫 번째를 반환
        return constructors.get(0);
    }
    
    /**
     * 개별 생성자를 분석하여 ConstructorInfo를 생성합니다.
     * 
     * @param constructor 분석할 생성자
     * @param creatorAnnotation @JSON5Creator 어노테이션
     * @return 생성자 정보 (분석 실패 시 null)
     */
    private ConstructorInfo analyzeConstructor(Constructor<?> constructor, JSON5Creator creatorAnnotation) {
        try {
            constructor.setAccessible(true);
            
            Parameter[] parameters = constructor.getParameters();
            List<ParameterInfo> parameterInfos = new ArrayList<>();
            
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                JSON5Property propertyAnnotation = parameter.getAnnotation(JSON5Property.class);
                
                if (propertyAnnotation == null) {
                    // @JSON5Property가 없는 파라미터는 무시하거나 예외 처리
                    throw new IllegalArgumentException(
                        "Parameter " + i + " in constructor " + constructor.getName() + 
                        " must have @JSON5Property annotation"
                    );
                }
                
                String jsonPath = propertyAnnotation.value();
                if (jsonPath == null || jsonPath.trim().isEmpty()) {
                    throw new IllegalArgumentException(
                        "JSON path in @JSON5Property must not be empty for parameter " + i
                    );
                }
                
                MissingValueStrategy missingStrategy = propertyAnnotation.onMissing();
                boolean required = propertyAnnotation.required();
                Class<?> parameterType = parameter.getType();
                
                ParameterInfo parameterInfo = new ParameterInfo(
                    jsonPath.trim(), parameterType, missingStrategy, required, i
                );
                parameterInfos.add(parameterInfo);
            }
            
            int priority = creatorAnnotation.priority();
            return new ConstructorInfo(constructor, priority, parameterInfos);
            
        } catch (Exception e) {
            // 분석 중 오류 발생 시 null 반환
            CatchExceptionProvider.getInstance().catchException("Failed to analyze constructor: " + constructor.getName() +
                             ", error: " + e.getMessage(),e);
            return null;
        }
    }
    
    /**
     * 값 공급자 생성자 검증 (기존 로직 + 단일 파라미터 검증)
     */
    public ConstructorInfo analyzeValueProviderConstructor(Class<?> clazz) {
        List<Constructor<?>> constructors = Arrays.stream(clazz.getDeclaredConstructors())
            .filter(c -> c.isAnnotationPresent(JSON5ValueConstructor.class))
            .collect(Collectors.toList());
        
        if (constructors.isEmpty()) {
            throw new JSON5SerializerException(
                "No @JSON5ValueConstructor found in class: " + clazz.getName());
        }
        
        if (constructors.size() > 1) {
            throw new JSON5SerializerException(
                "Multiple @JSON5ValueConstructor found in class: " + clazz.getName() + 
                ". Only one is allowed.");
        }
        
        Constructor<?> constructor = constructors.get(0);
        
        // 파라미터 개수 검증 (값 공급자는 정확히 1개만 허용)
        if (constructor.getParameterCount() != 1) {
            throw new JSON5SerializerException(
                "@JSON5ValueConstructor must have exactly one parameter: " + constructor);
        }
        
        // 기존 ConstructorInfo 생성 로직 활용하되 단일 파라미터로 제한
        constructor.setAccessible(true);
        return new ConstructorInfo(constructor, 0, Collections.emptyList()); // 값 공급자는 JSON5Property 불필요
    }
    
    /**
     * 값 공급자 생성자가 있는지 확인
     */
    public boolean hasValueProviderConstructor(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        
        return Arrays.stream(clazz.getDeclaredConstructors())
            .anyMatch(c -> c.isAnnotationPresent(JSON5ValueConstructor.class));
    }
}
