package com.hancomins.json5.serializer;

import com.hancomins.json5.options.WritingOptions;

/**
 * JSON5Serializer 인스턴스를 구성하는 Builder 클래스입니다.
 * 
 * <p>이 클래스는 JSON5Serializer의 설정을 단계별로 구성할 수 있게 해주는
 * 빌더 패턴을 구현합니다. SerializerConfiguration을 내부적으로 사용하여
 * 다양한 설정 옵션들을 제공합니다.</p>
 * 
 * <h3>사용 예제:</h3>
 * <pre>{@code
 * // 기본 serializer
 * JSON5Serializer serializer = JSON5Serializer.builder().build();
 * 
 * // 개발환경용 설정
 * JSON5Serializer devSerializer = JSON5Serializer.builder()
 *     .forDevelopment()
 *     .build();
 * 
 * // 커스텀 설정
 * JSON5Serializer customSerializer = JSON5Serializer.builder()
 *     .ignoreUnknownProperties()
 *     .enableSchemaCache()
 *     .withCustomTypeHandler(myHandler)
 *     .build();
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class JSON5SerializerBuilder {
    
    private SerializerConfiguration.Builder configBuilder;
    
    /**
     * 새로운 JSON5SerializerBuilder 인스턴스를 생성합니다.
     */
    public JSON5SerializerBuilder() {
        this.configBuilder = SerializerConfiguration.builder();
    }
    
    // =========================
    // 환경별 설정 메소드들
    // =========================
    
    /**
     * 개발 환경에 최적화된 설정을 적용합니다.
     * Pretty Print, 엄격한 검증, 성능 모니터링 등을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder forDevelopment() {
        this.configBuilder = SerializerConfiguration.forDevelopment().toBuilder();
        return this;
    }
    
    /**
     * 운영 환경에 최적화된 설정을 적용합니다.
     * 성능 최적화, 보안 설정, 캐싱 등을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder forProduction() {
        this.configBuilder = SerializerConfiguration.forProduction().toBuilder();
        return this;
    }
    
    /**
     * 테스트 환경에 최적화된 설정을 적용합니다.
     * 일관된 결과, 순서 보장 등을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder forTesting() {
        this.configBuilder = SerializerConfiguration.forTesting().toBuilder();
        return this;
    }
    
    // =========================
    // 기본 동작 설정 메소드들
    // =========================
    
    /**
     * 알 수 없는 속성을 무시하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder ignoreUnknownProperties() {
        configBuilder.ignoreUnknownProperties();
        return this;
    }
    
    /**
     * 빈 객체에 대해 실패하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder failOnEmptyBeans() {
        configBuilder.failOnEmptyBeans();
        return this;
    }
    
    /**
     * 필드 가시성을 사용하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder useFieldVisibility() {
        configBuilder.useFieldVisibility();
        return this;
    }
    
    /**
     * null 값을 포함하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder includeNullValues() {
        configBuilder.includeNullValues();
        return this;
    }
    
    // =========================
    // 성능 최적화 설정 메소드들
    // =========================
    
    /**
     * Schema 캐싱을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableSchemaCache() {
        configBuilder.enableSchemaCache();
        return this;
    }
    
    /**
     * 지연 로딩을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableLazyLoading() {
        configBuilder.enableLazyLoading();
        return this;
    }
    
    /**
     * 커스텀 Schema 캐시를 설정합니다.
     * 
     * @param cache 사용할 SchemaCache 인스턴스
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withSchemaCache(SchemaCache cache) {
        configBuilder.withSchemaCache(cache);
        return this;
    }
    
    // =========================
    // 보안 설정 메소드들
    // =========================
    
    /**
     * 민감한 필드 마스킹을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder maskSensitiveFields() {
        configBuilder.maskSensitiveFields();
        return this;
    }
    
    /**
     * 민감한 필드를 추가합니다.
     * 
     * @param fieldNames 민감한 필드명들
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder addSensitiveField(String... fieldNames) {
        configBuilder.addSensitiveField(fieldNames);
        return this;
    }
    
    /**
     * 필드명 검증을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableFieldNameValidation() {
        configBuilder.enableFieldNameValidation();
        return this;
    }
    
    // =========================
    // 고급 설정 메소드들
    // =========================
    
    /**
     * 기본 WritingOptions를 설정합니다.
     * 
     * @param options 사용할 WritingOptions
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withDefaultWritingOptions(WritingOptions options) {
        configBuilder.withDefaultWritingOptions(options);
        return this;
    }
    
    /**
     * 최대 깊이를 설정합니다.
     * 
     * @param maxDepth 최대 중첩 깊이
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withMaxDepth(int maxDepth) {
        configBuilder.withMaxDepth(maxDepth);
        return this;
    }
    
    /**
     * 순환 참조 감지를 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableCircularReferenceDetection() {
        configBuilder.enableCircularReferenceDetection();
        return this;
    }
    
    /**
     * 엄격한 타입 검사를 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableStrictTypeChecking() {
        configBuilder.enableStrictTypeChecking();
        return this;
    }
    
    /**
     * 직렬화 타임아웃을 설정합니다.
     * 
     * @param timeoutMs 타임아웃 시간 (밀리초)
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withSerializationTimeout(long timeoutMs) {
        configBuilder.withSerializationTimeout(timeoutMs);
        return this;
    }
    
    /**
     * 성능 모니터링을 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enablePerformanceMonitoring() {
        configBuilder.enablePerformanceMonitoring();
        return this;
    }
    
    /**
     * 최대 문자열 길이를 설정합니다.
     * 
     * @param maxLength 최대 문자열 길이
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withMaxStringLength(int maxLength) {
        configBuilder.withMaxStringLength(maxLength);
        return this;
    }
    
    /**
     * 필드 순서를 보장하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder preserveFieldOrder() {
        configBuilder.preserveFieldOrder();
        return this;
    }
    
    // =========================
    // 타입 핸들러 설정 메소드들
    // =========================
    
    /**
     * 커스텀 타입 핸들러를 추가합니다.
     * 
     * @param handler 추가할 TypeHandler
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withCustomTypeHandler(TypeHandler handler) {
        configBuilder.withCustomTypeHandler(handler);
        return this;
    }
    
    /**
     * 커스텀 TypeHandlerRegistry를 설정합니다.
     * 
     * @param registry 사용할 TypeHandlerRegistry
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withTypeHandlerRegistry(TypeHandlerRegistry registry) {
        configBuilder.withTypeHandlerRegistry(registry);
        return this;
    }
    
    // =========================
    // 편의 메소드들
    // =========================
    
    /**
     * 모든 보안 기능을 활성화합니다.
     * 민감한 필드 마스킹, 필드명 검증, 문자열 길이 제한 등을 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableAllSecurityFeatures() {
        configBuilder.enableAllSecurityFeatures();
        return this;
    }
    
    /**
     * 모든 성능 최적화를 활성화합니다.
     * Schema 캐싱, 지연 로딩, 최적화된 설정 등을 적용합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableAllPerformanceOptimizations() {
        configBuilder.enableAllPerformanceOptimizations();
        return this;
    }
    
    /**
     * 모든 개발 도구를 활성화합니다.
     * 엄격한 검증, 성능 모니터링, 순환 참조 감지 등을 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableAllDevelopmentTools() {
        configBuilder.enableAllDevelopmentTools();
        return this;
    }
    
    // =========================
    // 설정 메소드들
    // =========================
    
    /**
     * 환경 프로파일을 설정합니다.
     * 
     * @param profile 환경 프로파일
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withEnvironmentProfile(SerializerConfiguration.EnvironmentProfile profile) {
        configBuilder.withEnvironmentProfile(profile);
        return this;
    }
    
    /**
     * 기존 설정을 기반으로 Builder를 초기화합니다.
     * 
     * @param config 기반이 될 SerializerConfiguration
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder basedOn(SerializerConfiguration config) {
        this.configBuilder = config.toBuilder();
        return this;
    }
    
    // =========================
    // 빌드 메소드
    // =========================
    
    /**
     * 설정된 옵션으로 JSON5Serializer 인스턴스를 생성합니다.
     * 
     * @return 새로운 JSON5Serializer 인스턴스
     * @throws IllegalArgumentException 잘못된 설정이 있는 경우
     */
    public JSON5Serializer build() {
        SerializerConfiguration config = configBuilder.build();
        return new JSON5Serializer(config);
    }
    
    /**
     * 현재 설정을 기반으로 SerializerConfiguration을 생성합니다.
     * JSON5Serializer를 생성하지 않고 설정만 필요한 경우 사용합니다.
     * 
     * @return 새로운 SerializerConfiguration 인스턴스
     */
    public SerializerConfiguration buildConfiguration() {
        return configBuilder.build();
    }
}
