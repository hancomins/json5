package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5ValueProvider;
import com.hancomins.json5.serializer.NullHandling;
import com.hancomins.json5.serializer.constructor.ConstructorInfo;

/**
 * 값 공급자 메타데이터를 저장하는 클래스
 */
public class ValueProviderInfo {
    private final Class<?> providerClass;
    private final Class<?> targetType;
    private final ExtractorInfo extractor;
    private final ConstructorInfo constructor;
    private final JSON5ValueProvider annotation;
    
    public ValueProviderInfo(Class<?> providerClass, Class<?> targetType,
                           ExtractorInfo extractor, ConstructorInfo constructor,
                           JSON5ValueProvider annotation) {
        this.providerClass = providerClass;
        this.targetType = targetType;
        this.extractor = extractor;
        this.constructor = constructor;
        this.annotation = annotation;
    }
    
    public Class<?> getProviderClass() {
        return providerClass;
    }
    
    public Class<?> getTargetType() {
        return targetType;
    }
    
    public ExtractorInfo getExtractor() {
        return extractor;
    }
    
    public ConstructorInfo getConstructor() {
        return constructor;
    }
    
    public boolean canSerialize() {
        return extractor != null;
    }
    
    public boolean canDeserialize() {
        return constructor != null;
    }
    
    public boolean isStrictTypeMatching() {
        return annotation.strictTypeMatching();
    }
    
    public NullHandling getNullHandling() {
        return annotation.nullHandling();
    }
    
    @Override
    public String toString() {
        return "ValueProviderInfo{" +
                "providerClass=" + providerClass.getSimpleName() +
                ", targetType=" + targetType.getSimpleName() +
                ", canSerialize=" + canSerialize() +
                ", canDeserialize=" + canDeserialize() +
                ", strictTypeMatching=" + isStrictTypeMatching() +
                '}';
    }
}
