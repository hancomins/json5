package com.hancomins.json5.serializer;

/**
 * JSON5Serializer의 설정을 관리하는 클래스입니다.
 * 
 * <p>이 클래스는 직렬화/역직렬화 동작을 제어하는 다양한 옵션들을 제공합니다.
 * Builder 패턴을 통해 설정을 구성할 수 있습니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class SerializerConfiguration {
    
    private final boolean ignoreUnknownProperties;
    private final boolean failOnEmptyBeans;
    private final boolean useFieldVisibility;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final SchemaCache schemaCache;
    private final boolean enableSchemaCache;
    private final boolean includeNullValues;
    
    private SerializerConfiguration(Builder builder) {
        this.ignoreUnknownProperties = builder.ignoreUnknownProperties;
        this.failOnEmptyBeans = builder.failOnEmptyBeans;
        this.useFieldVisibility = builder.useFieldVisibility;
        this.typeHandlerRegistry = builder.typeHandlerRegistry;
        this.schemaCache = builder.schemaCache;
        this.enableSchemaCache = builder.enableSchemaCache;
        this.includeNullValues = builder.includeNullValues;
    }
    
    /**
     * 기본 설정으로 SerializerConfiguration을 생성합니다.
     * 
     * @return 기본 설정이 적용된 SerializerConfiguration
     */
    public static SerializerConfiguration getDefault() {
        return builder().build();
    }
    
    /**
     * SerializerConfiguration Builder를 생성합니다.
     * 
     * @return 새로운 Builder 인스턴스
     */
    public static Builder builder() {
        return new Builder();
    }
    
    // Getter methods
    public boolean isIgnoreUnknownProperties() {
        return ignoreUnknownProperties;
    }
    
    public boolean isFailOnEmptyBeans() {
        return failOnEmptyBeans;
    }
    
    public boolean isUseFieldVisibility() {
        return useFieldVisibility;
    }
    
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }
    
    public SchemaCache getSchemaCache() {
        return schemaCache;
    }
    
    public boolean isEnableSchemaCache() {
        return enableSchemaCache;
    }
    
    public boolean isIncludeNullValues() {
        return includeNullValues;
    }
    
    /**
     * SerializerConfiguration을 구성하는 Builder 클래스입니다.
     */
    public static class Builder {
        private boolean ignoreUnknownProperties = false;
        private boolean failOnEmptyBeans = false;
        private boolean useFieldVisibility = false;
        private TypeHandlerRegistry typeHandlerRegistry;
        private SchemaCache schemaCache;
        private boolean enableSchemaCache = false;
        private boolean includeNullValues = false;
        
        private Builder() {
            // 기본 TypeHandlerRegistry 생성
            this.typeHandlerRegistry = TypeHandlerFactory.createDefaultRegistry();
        }
        
        /**
         * 알 수 없는 속성을 무시할지 설정합니다.
         * 
         * @param ignore true이면 알 수 없는 속성을 무시
         * @return 이 Builder 인스턴스
         */
        public Builder ignoreUnknownProperties(boolean ignore) {
            this.ignoreUnknownProperties = ignore;
            return this;
        }
        
        /**
         * 알 수 없는 속성을 무시하도록 설정합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder ignoreUnknownProperties() {
            return ignoreUnknownProperties(true);
        }
        
        /**
         * 빈 Bean에 대해 실패할지 설정합니다.
         * 
         * @param fail true이면 빈 Bean에 대해 실패
         * @return 이 Builder 인스턴스
         */
        public Builder failOnEmptyBeans(boolean fail) {
            this.failOnEmptyBeans = fail;
            return this;
        }
        
        /**
         * 빈 Bean에 대해 실패하도록 설정합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder failOnEmptyBeans() {
            return failOnEmptyBeans(true);
        }
        
        /**
         * 필드 가시성을 사용할지 설정합니다.
         * 
         * @param use true이면 필드 가시성 사용
         * @return 이 Builder 인스턴스
         */
        public Builder useFieldVisibility(boolean use) {
            this.useFieldVisibility = use;
            return this;
        }
        
        /**
         * 필드 가시성을 사용하도록 설정합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder useFieldVisibility() {
            return useFieldVisibility(true);
        }
        
        /**
         * 커스텀 TypeHandler를 추가합니다.
         * 
         * @param handler 추가할 TypeHandler
         * @return 이 Builder 인스턴스
         */
        public Builder withCustomTypeHandler(TypeHandler handler) {
            this.typeHandlerRegistry.registerHandler(handler);
            return this;
        }
        
        /**
         * TypeHandlerRegistry를 설정합니다.
         * 
         * @param registry 사용할 TypeHandlerRegistry
         * @return 이 Builder 인스턴스
         */
        public Builder withTypeHandlerRegistry(TypeHandlerRegistry registry) {
            this.typeHandlerRegistry = registry;
            return this;
        }
        
        /**
         * SchemaCache를 설정합니다.
         * 
         * @param cache 사용할 SchemaCache
         * @return 이 Builder 인스턴스
         */
        public Builder withSchemaCache(SchemaCache cache) {
            this.schemaCache = cache;
            this.enableSchemaCache = true;
            return this;
        }
        
        /**
         * 기본 SchemaCache를 활성화합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder enableSchemaCache() {
            this.schemaCache = new DefaultSchemaCache();
            this.enableSchemaCache = true;
            return this;
        }
        
        /**
         * null 값을 포함할지 설정합니다.
         * 
         * @param include true이면 null 값 포함
         * @return 이 Builder 인스턴스
         */
        public Builder includeNullValues(boolean include) {
            this.includeNullValues = include;
            return this;
        }
        
        /**
         * null 값을 포함하도록 설정합니다.
         * 
         * @return 이 Builder 인스턴스
         */
        public Builder includeNullValues() {
            return includeNullValues(true);
        }
        
        /**
         * 설정된 옵션으로 SerializerConfiguration을 빌드합니다.
         * 
         * @return 새로운 SerializerConfiguration 인스턴스
         */
        public SerializerConfiguration build() {
            return new SerializerConfiguration(this);
        }
    }
}