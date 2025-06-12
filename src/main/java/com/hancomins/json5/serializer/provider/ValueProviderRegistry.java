package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5ValueProvider;
import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.constructor.ConstructorAnalyzer;
import com.hancomins.json5.serializer.constructor.ConstructorInfo;
import com.hancomins.json5.util.DataConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 값 공급자 정보를 등록하고 관리합니다.
 * SubTypeRegistry 패턴을 완전 복사하여 구현합니다.
 */
public class ValueProviderRegistry {
    private final Map<Class<?>, ValueProviderInfo> providerMap = new ConcurrentHashMap<>();
    private final ExtractorAnalyzer extractorAnalyzer = new ExtractorAnalyzer();
    private final ConstructorAnalyzer constructorAnalyzer = new ConstructorAnalyzer();
    
    /**
     * 클래스를 값 공급자로 등록
     */
    public void registerValueProvider(Class<?> clazz) {
        if (isValueProvider(clazz)) {
            ValueProviderInfo info = analyzeValueProvider(clazz);
            providerMap.put(clazz, info);
        }
    }
    
    /**
     * 값 공급자 정보 조회 (없으면 분석하여 캐싱)
     */
    public ValueProviderInfo getValueProviderInfo(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        
        return providerMap.computeIfAbsent(clazz, this::analyzeAndCache);
    }
    
    /**
     * 클래스가 값 공급자인지 확인
     */
    public boolean isValueProvider(Class<?> clazz) {
        return clazz != null && clazz.isAnnotationPresent(JSON5ValueProvider.class);
    }
    
    /**
     * 캐시를 초기화합니다.
     */
    public void clearCache() {
        providerMap.clear();
    }
    
    /**
     * 캐시된 값 공급자 정보의 개수를 반환합니다.
     */
    public int getCacheSize() {
        return providerMap.size();
    }
    
    /**
     * 분석하여 캐시에 저장
     */
    private ValueProviderInfo analyzeAndCache(Class<?> clazz) {
        if (isValueProvider(clazz)) {
            return analyzeValueProvider(clazz);
        }
        return null; // null도 캐싱하여 반복 분석 방지
    }
    
    /**
     * 값 공급자 정보 분석
     */
    private ValueProviderInfo analyzeValueProvider(Class<?> clazz) {
        JSON5ValueProvider annotation = clazz.getAnnotation(JSON5ValueProvider.class);
        
        ExtractorInfo extractor = extractorAnalyzer.analyzeValueExtractor(clazz);
        ConstructorInfo constructor = constructorAnalyzer.analyzeValueProviderConstructor(clazz);
        
        Class<?> targetType = determineTargetType(annotation, extractor);
        
        // 타입 호환성 검사
        validateTypeCompatibility(extractor, constructor, annotation.strictTypeMatching());
        
        return new ValueProviderInfo(clazz, targetType, extractor, constructor, annotation);
    }
    
    /**
     * 대상 타입 결정
     */
    private Class<?> determineTargetType(JSON5ValueProvider annotation, ExtractorInfo extractor) {
        if (annotation.targetType() != Void.class) {
            return annotation.targetType();
        }
        
        return extractor.getReturnType();
    }
    
    /**
     * 타입 호환성 검증
     */
    private void validateTypeCompatibility(ExtractorInfo extractor, ConstructorInfo constructor, 
                                         boolean strictMode) {
        Class<?> extractorReturnType = extractor.getReturnType();
        Class<?> constructorParamType = constructor.getConstructor().getParameterTypes()[0];
        
        if (strictMode) {
            if (!extractorReturnType.equals(constructorParamType)) {
                throw new JSON5SerializerException(
                    String.format("Type mismatch in strict mode: extractor returns %s but constructor expects %s",
                        extractorReturnType.getName(), constructorParamType.getName()));
            }
        } else {
            // 느슨한 모드에서는 DataConverter로 변환 가능한지 확인
            if (!DataConverter.canConvert(extractorReturnType, constructorParamType)) {
                throw new JSON5SerializerException(
                    String.format("Cannot convert from %s to %s using DataConverter",
                        extractorReturnType.getName(), constructorParamType.getName()));
            }
        }
    }
}
