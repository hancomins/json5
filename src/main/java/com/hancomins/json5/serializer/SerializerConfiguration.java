package com.hancomins.json5.serializer;

import com.hancomins.json5.options.WritingOptions;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

/**
 * JSON5Serializer의 고급 설정을 관리하는 클래스입니다.
 * 
 * <p>이 클래스는 직렬화/역직렬화 동작을 제어하는 다양한 옵션들을 제공합니다.
 * Builder 패턴을 통해 설정을 구성할 수 있으며, 환경별 설정, 성능 최적화 옵션,
 * 보안 설정 등을 포함한 고급 설정을 지원합니다.</p>
 * 
 * <h3>주요 설정 카테고리:</h3>
 * <ul>
 *   <li><b>기본 동작 설정:</b> ignoreUnknownProperties, failOnEmptyBeans 등</li>
 *   <li><b>성능 최적화:</b> 캐싱, 타임아웃, 지연 로딩 설정</li>
 *   <li><b>보안 설정:</b> 민감한 필드 마스킹, 필드명 검증</li>
 *   <li><b>환경별 설정:</b> 개발/스테이징/운영 환경별 최적화</li>
 * </ul>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * // 기본 설정
 * SerializerConfiguration config = SerializerConfiguration.getDefault();
 * 
 * // 개발 환경 설정
 * SerializerConfiguration devConfig = SerializerConfiguration.forDevelopment();
 * 
 * // 커스텀 설정
 * SerializerConfiguration customConfig = SerializerConfiguration.builder()
 *     .ignoreUnknownProperties()
 *     .enableSchemaCache()
 *     .maskSensitiveFields()
 *     .addSensitiveField("password", "secret")
 *     .build();
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class SerializerConfiguration {
    
    // 기본 동작 설정
    private final boolean ignoreUnknownProperties;
    private final boolean failOnEmptyBeans;
    private final boolean useFieldVisibility;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final SchemaCache schemaCache;
    private final boolean enableSchemaCache;
    private final boolean includeNullValues;
    
    // 고급 설정
    private final WritingOptions defaultWritingOptions;
    private final int maxDepth;
    private final boolean enableCircularReferenceDetection;
    private final Set<String> sensitiveFields;
    private final boolean maskSensitiveFields;
    private final EnvironmentProfile environmentProfile;
    private final boolean enableStrictTypeChecking;
    private final long serializationTimeoutMs;
    private final boolean enablePerformanceMonitoring;
    private final int maxStringLength;
    private final boolean enableFieldNameValidation;
    private final boolean preserveFieldOrder;
    private final boolean enableLazyLoading;
    
    /**
     * 환경별 프로파일을 정의하는 enum입니다.
     */
    public enum EnvironmentProfile {
        /** 개발 환경 - 디버깅과 가독성 우선 */
        DEVELOPMENT,
        
        /** 스테이징 환경 - 운영과 유사하지만 디버깅 기능 포함 */
        STAGING, 
        
        /** 운영 환경 - 성능과 보안 우선 */
        PRODUCTION,
        
        /** 테스트 환경 - 일관성과 재현성 우선 */
        TEST,
        
        /** 사용자 정의 환경 */
        CUSTOM
    }
    
    private SerializerConfiguration(Builder builder) {
        // 기본 설정
        this.ignoreUnknownProperties = builder.ignoreUnknownProperties;
        this.failOnEmptyBeans = builder.failOnEmptyBeans;
        this.useFieldVisibility = builder.useFieldVisibility;
        this.typeHandlerRegistry = builder.typeHandlerRegistry;
        this.schemaCache = builder.schemaCache;
        this.enableSchemaCache = builder.enableSchemaCache;
        this.includeNullValues = builder.includeNullValues;
        
        // 고급 설정
        this.defaultWritingOptions = builder.defaultWritingOptions;
        this.maxDepth = builder.maxDepth;
        this.enableCircularReferenceDetection = builder.enableCircularReferenceDetection;
        this.sensitiveFields = new HashSet<>(builder.sensitiveFields);
        this.maskSensitiveFields = builder.maskSensitiveFields;
        this.environmentProfile = builder.environmentProfile;
        this.enableStrictTypeChecking = builder.enableStrictTypeChecking;
        this.serializationTimeoutMs = builder.serializationTimeoutMs;
        this.enablePerformanceMonitoring = builder.enablePerformanceMonitoring;
        this.maxStringLength = builder.maxStringLength;
        this.enableFieldNameValidation = builder.enableFieldNameValidation;
        this.preserveFieldOrder = builder.preserveFieldOrder;
        this.enableLazyLoading = builder.enableLazyLoading;
    }
    
    // =========================
    // 정적 팩토리 메소드들
    // =========================
    
    /**
     * 기본 설정으로 SerializerConfiguration을 생성합니다.
     * 
     * @return 기본 설정이 적용된 SerializerConfiguration
     */
    public static SerializerConfiguration getDefault() {
        return builder().build();
    }
    
    /**
     * 개발 환경에 최적화된 설정을 생성합니다.
     * 디버깅과 가독성을 위해 Pretty Print를 활성화하고 엄격한 검증을 적용합니다.
     * 
     * @return 개발 환경 설정이 적용된 SerializerConfiguration
     */
    public static SerializerConfiguration forDevelopment() {
        return builder()
            .withEnvironmentProfile(EnvironmentProfile.DEVELOPMENT)
            .withDefaultWritingOptions(WritingOptions.json5Pretty())
            .enableStrictTypeChecking()
            .enablePerformanceMonitoring()
            .enableCircularReferenceDetection()
            .enableAllDevelopmentTools()
            .build();
    }
    
    /**
     * 운영 환경에 최적화된 설정을 생성합니다.
     * 성능을 우선시하고 보안 설정을 강화합니다.
     * 
     * @return 운영 환경 설정이 적용된 SerializerConfiguration
     */
    public static SerializerConfiguration forProduction() {
        return builder()
            .withEnvironmentProfile(EnvironmentProfile.PRODUCTION)
            .withDefaultWritingOptions(WritingOptions.json5())
            .enableSchemaCache()
            .enableLazyLoading()
            .enableAllSecurityFeatures()
            .enableAllPerformanceOptimizations()
            .withSerializationTimeout(5000)  // 환경별 타임아웃을 마지막에 설정
            .build();
    }
    
    /**
     * 테스트 환경에 최적화된 설정을 생성합니다.
     * 빠른 실행과 일관된 결과를 위해 최적화됩니다.
     * 
     * @return 테스트 환경 설정이 적용된 SerializerConfiguration
     */
    public static SerializerConfiguration forTesting() {
        return builder()
            .withEnvironmentProfile(EnvironmentProfile.TEST)
            .includeNullValues()
            .preserveFieldOrder()
            .ignoreUnknownProperties()
            .build();
    }
    
    /**
     * SerializerConfiguration Builder를 생성합니다.
     * 
     * @return 새로운 Builder 인스턴스
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // =========================
    // Getter 메소드들
    // =========================
    
    // 기본 설정 getter들
    public boolean isIgnoreUnknownProperties() { return ignoreUnknownProperties; }
    public boolean isFailOnEmptyBeans() { return failOnEmptyBeans; }
    public boolean isUseFieldVisibility() { return useFieldVisibility; }
    public TypeHandlerRegistry getTypeHandlerRegistry() { return typeHandlerRegistry; }
    public SchemaCache getSchemaCache() { return schemaCache; }
    public boolean isEnableSchemaCache() { return enableSchemaCache; }
    public boolean isIncludeNullValues() { return includeNullValues; }
    
    // 고급 설정 getter들
    public WritingOptions getDefaultWritingOptions() { return defaultWritingOptions; }
    public int getMaxDepth() { return maxDepth; }
    public boolean isEnableCircularReferenceDetection() { return enableCircularReferenceDetection; }
    public Set<String> getSensitiveFields() { return Collections.unmodifiableSet(sensitiveFields); }
    public boolean isMaskSensitiveFields() { return maskSensitiveFields; }
    public EnvironmentProfile getEnvironmentProfile() { return environmentProfile; }
    public boolean isEnableStrictTypeChecking() { return enableStrictTypeChecking; }
    public long getSerializationTimeoutMs() { return serializationTimeoutMs; }
    public boolean isEnablePerformanceMonitoring() { return enablePerformanceMonitoring; }
    public int getMaxStringLength() { return maxStringLength; }
    public boolean isEnableFieldNameValidation() { return enableFieldNameValidation; }
    public boolean isPreserveFieldOrder() { return preserveFieldOrder; }
    public boolean isEnableLazyLoading() { return enableLazyLoading; }
    
    /**
     * 특정 필드가 민감한 필드인지 확인합니다.
     * 
     * @param fieldName 확인할 필드명
     * @return 민감한 필드이면 true
     */
    public boolean isSensitiveField(String fieldName) {
        return sensitiveFields.contains(fieldName);
    }
    
    /**
     * 현재 설정을 기반으로 새로운 Builder를 생성합니다.
     * 
     * @return 현재 설정이 적용된 새로운 Builder
     */
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.ignoreUnknownProperties = this.ignoreUnknownProperties;
        builder.failOnEmptyBeans = this.failOnEmptyBeans;
        builder.useFieldVisibility = this.useFieldVisibility;
        builder.typeHandlerRegistry = this.typeHandlerRegistry;
        builder.schemaCache = this.schemaCache;
        builder.enableSchemaCache = this.enableSchemaCache;
        builder.includeNullValues = this.includeNullValues;
        builder.defaultWritingOptions = this.defaultWritingOptions;
        builder.maxDepth = this.maxDepth;
        builder.enableCircularReferenceDetection = this.enableCircularReferenceDetection;
        builder.sensitiveFields.addAll(this.sensitiveFields);
        builder.maskSensitiveFields = this.maskSensitiveFields;
        builder.environmentProfile = this.environmentProfile;
        builder.enableStrictTypeChecking = this.enableStrictTypeChecking;
        builder.serializationTimeoutMs = this.serializationTimeoutMs;
        builder.enablePerformanceMonitoring = this.enablePerformanceMonitoring;
        builder.maxStringLength = this.maxStringLength;
        builder.enableFieldNameValidation = this.enableFieldNameValidation;
        builder.preserveFieldOrder = this.preserveFieldOrder;
        builder.enableLazyLoading = this.enableLazyLoading;
        return builder;
    }
    
    // =========================
    // Builder 클래스
    // =========================
    
    /**
     * SerializerConfiguration을 구성하는 Builder 클래스입니다.
     * 다양한 설정 옵션들을 fluent API 스타일로 구성할 수 있습니다.
     */
    public static class Builder {
        // 기본 설정
        private boolean ignoreUnknownProperties = false;
        private boolean failOnEmptyBeans = false;
        private boolean useFieldVisibility = false;
        private TypeHandlerRegistry typeHandlerRegistry;
        private SchemaCache schemaCache;
        private boolean enableSchemaCache = false;
        private boolean includeNullValues = false;
        
        // 고급 설정 (기본값 설정)
        private WritingOptions defaultWritingOptions = WritingOptions.json5();
        private int maxDepth = 64;
        private boolean enableCircularReferenceDetection = false;
        private Set<String> sensitiveFields = new HashSet<>();
        private boolean maskSensitiveFields = false;
        private EnvironmentProfile environmentProfile = EnvironmentProfile.CUSTOM;
        private boolean enableStrictTypeChecking = false;
        private long serializationTimeoutMs = 30000; // 30초
        private boolean enablePerformanceMonitoring = false;
        private int maxStringLength = Integer.MAX_VALUE;
        private boolean enableFieldNameValidation = false;
        private boolean preserveFieldOrder = false;
        private boolean enableLazyLoading = false;
        
        private Builder() {
            // 기본 TypeHandlerRegistry 생성
            this.typeHandlerRegistry = TypeHandlerFactory.createDefaultRegistry();
        }
        
        // =========================
        // 기본 설정 메소드들
        // =========================
        
        public Builder ignoreUnknownProperties(boolean ignore) {
            this.ignoreUnknownProperties = ignore;
            return this;
        }
        
        public Builder ignoreUnknownProperties() {
            return ignoreUnknownProperties(true);
        }
        
        public Builder failOnEmptyBeans(boolean fail) {
            this.failOnEmptyBeans = fail;
            return this;
        }
        
        public Builder failOnEmptyBeans() {
            return failOnEmptyBeans(true);
        }
        
        public Builder useFieldVisibility(boolean use) {
            this.useFieldVisibility = use;
            return this;
        }
        
        public Builder useFieldVisibility() {
            return useFieldVisibility(true);
        }
        
        public Builder withCustomTypeHandler(TypeHandler handler) {
            this.typeHandlerRegistry.registerHandler(handler);
            return this;
        }
        
        public Builder withTypeHandlerRegistry(TypeHandlerRegistry registry) {
            this.typeHandlerRegistry = registry;
            return this;
        }
        
        public Builder withSchemaCache(SchemaCache cache) {
            this.schemaCache = cache;
            this.enableSchemaCache = true;
            return this;
        }
        
        public Builder enableSchemaCache() {
            this.schemaCache = new DefaultSchemaCache();
            this.enableSchemaCache = true;
            return this;
        }
        
        public Builder includeNullValues(boolean include) {
            this.includeNullValues = include;
            return this;
        }
        
        public Builder includeNullValues() {
            return includeNullValues(true);
        }
        
        // =========================
        // 고급 설정 메소드들
        // =========================
        
        public Builder withDefaultWritingOptions(WritingOptions options) {
            this.defaultWritingOptions = options;
            return this;
        }
        
        public Builder withMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }
        
        public Builder enableCircularReferenceDetection(boolean enable) {
            this.enableCircularReferenceDetection = enable;
            return this;
        }
        
        public Builder enableCircularReferenceDetection() {
            return enableCircularReferenceDetection(true);
        }
        
        public Builder maskSensitiveFields(boolean mask) {
            this.maskSensitiveFields = mask;
            return this;
        }
        
        public Builder maskSensitiveFields() {
            return maskSensitiveFields(true);
        }
        
        public Builder addSensitiveField(String fieldName) {
            this.sensitiveFields.add(fieldName);
            return this;
        }
        
        public Builder addSensitiveField(String... fieldNames) {
            this.sensitiveFields.addAll(Arrays.asList(fieldNames));
            return this;
        }
        
        public Builder withEnvironmentProfile(EnvironmentProfile profile) {
            this.environmentProfile = profile;
            return this;
        }
        
        public Builder enableStrictTypeChecking(boolean enable) {
            this.enableStrictTypeChecking = enable;
            return this;
        }
        
        public Builder enableStrictTypeChecking() {
            return enableStrictTypeChecking(true);
        }
        
        public Builder withSerializationTimeout(long timeoutMs) {
            this.serializationTimeoutMs = timeoutMs;
            return this;
        }
        
        public Builder enablePerformanceMonitoring(boolean enable) {
            this.enablePerformanceMonitoring = enable;
            return this;
        }
        
        public Builder enablePerformanceMonitoring() {
            return enablePerformanceMonitoring(true);
        }
        
        public Builder withMaxStringLength(int maxLength) {
            this.maxStringLength = maxLength;
            return this;
        }
        
        public Builder enableFieldNameValidation(boolean enable) {
            this.enableFieldNameValidation = enable;
            return this;
        }
        
        public Builder enableFieldNameValidation() {
            return enableFieldNameValidation(true);
        }
        
        public Builder preserveFieldOrder(boolean preserve) {
            this.preserveFieldOrder = preserve;
            return this;
        }
        
        public Builder preserveFieldOrder() {
            return preserveFieldOrder(true);
        }
        
        public Builder enableLazyLoading(boolean enable) {
            this.enableLazyLoading = enable;
            return this;
        }
        
        public Builder enableLazyLoading() {
            return enableLazyLoading(true);
        }
        
        // =========================
        // 편의 메소드들
        // =========================
        
        /**
         * 모든 보안 설정을 활성화합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder enableAllSecurityFeatures() {
            return this
                .maskSensitiveFields()
                .addSensitiveField("password", "secret", "token", "key", "apiKey", "accessToken")
                .enableFieldNameValidation()
                .withMaxStringLength(10000);
        }
        
        /**
         * 모든 성능 최적화를 활성화합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder enableAllPerformanceOptimizations() {
            return this
                .enableSchemaCache()
                .enableLazyLoading()
                .withMaxDepth(32)
                .withSerializationTimeout(10000);
        }
        
        /**
         * 모든 개발 도구를 활성화합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder enableAllDevelopmentTools() {
            return this
                .enableStrictTypeChecking()
                .enablePerformanceMonitoring()
                .enableCircularReferenceDetection()
                .preserveFieldOrder()
                .withDefaultWritingOptions(WritingOptions.json5Pretty());
        }
        
        /**
         * 설정된 옵션으로 SerializerConfiguration을 빌드합니다.
         * 
         * @return 새로운 SerializerConfiguration 인스턴스
         */
        public SerializerConfiguration build() {
            // 환경 프로파일에 따른 기본 설정 조정
            applyEnvironmentDefaults();
            
            // 검증
            validateConfiguration();
            
            return new SerializerConfiguration(this);
        }
        
        /**
         * 환경 프로파일에 따른 기본 설정을 적용합니다.
         */
        private void applyEnvironmentDefaults() {
            switch (environmentProfile) {
                case DEVELOPMENT:
                    if (defaultWritingOptions == WritingOptions.json5()) {
                        defaultWritingOptions = WritingOptions.json5Pretty();
                    }
                    break;
                case PRODUCTION:
                    if (!enableSchemaCache) {
                        enableSchemaCache();
                    }
                    if (serializationTimeoutMs == 30000) {
                        serializationTimeoutMs = 5000;
                    }
                    break;
                case TEST:
                    if (!preserveFieldOrder) {
                        preserveFieldOrder = true;
                    }
                    break;
                default:
                    // STAGING, CUSTOM은 사용자 설정 그대로 유지
                    break;
            }
        }
        
        /**
         * 설정의 유효성을 검증합니다.
         */
        private void validateConfiguration() {
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive: " + maxDepth);
            }
            if (serializationTimeoutMs <= 0) {
                throw new IllegalArgumentException("serializationTimeoutMs must be positive: " + serializationTimeoutMs);
            }
            if (maxStringLength <= 0) {
                throw new IllegalArgumentException("maxStringLength must be positive: " + maxStringLength);
            }
            if (typeHandlerRegistry == null) {
                throw new IllegalArgumentException("typeHandlerRegistry cannot be null");
            }
        }
    }
}
