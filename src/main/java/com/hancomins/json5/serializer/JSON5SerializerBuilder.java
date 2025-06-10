package com.hancomins.json5.serializer;

/**
 * JSON5Serializer Builder 클래스입니다.
 * 
 * <p>Builder 패턴을 통해 JSON5Serializer를 구성할 수 있습니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class JSON5SerializerBuilder {
    
    private SerializerConfiguration.Builder configBuilder;
    
    /**
     * JSON5SerializerBuilder를 생성합니다.
     */
    public JSON5SerializerBuilder() {
        this.configBuilder = SerializerConfiguration.builder();
    }
    
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
     * 빈 Bean에 대해 실패하도록 설정합니다.
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
     * 커스텀 TypeHandler를 추가합니다.
     * 
     * @param handler 추가할 TypeHandler
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder withCustomTypeHandler(TypeHandler handler) {
        configBuilder.withCustomTypeHandler(handler);
        return this;
    }
    
    /**
     * SchemaCache를 활성화합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public JSON5SerializerBuilder enableSchemaCache() {
        configBuilder.enableSchemaCache();
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
    
    /**
     * 설정된 옵션으로 JSON5Serializer를 빌드합니다.
     * 
     * @return 새로운 JSON5Serializer 인스턴스
     */
    public JSON5Serializer build() {
        SerializerConfiguration config = configBuilder.build();
        return new JSON5Serializer(config);
    }
}