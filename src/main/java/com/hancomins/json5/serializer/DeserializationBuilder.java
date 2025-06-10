package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.options.WritingOptions;

/**
 * 역직렬화 빌더 클래스입니다.
 * 
 * <p>Fluent API를 통해 역직렬화 옵션을 설정하고 실행할 수 있습니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class DeserializationBuilder {
    
    private final JSON5Serializer serializer;
    private boolean ignoreError = false;
    private boolean strictTypeChecking = true;
    private Object defaultValue = null;
    private WritingOptions writingOptions;
    
    /**
     * DeserializationBuilder를 생성합니다.
     * 
     * @param serializer 사용할 JSON5Serializer 인스턴스
     */
    public DeserializationBuilder(JSON5Serializer serializer) {
        this.serializer = serializer;
    }
    
    /**
     * 오류를 무시하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder ignoreErrors() {
        this.ignoreError = true;
        return this;
    }
    
    /**
     * 엄격한 타입 검사를 설정합니다.
     * 
     * @param strict true이면 엄격한 타입 검사 수행
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder withStrictTypeChecking(boolean strict) {
        this.strictTypeChecking = strict;
        return this;
    }
    
    /**
     * 기본값을 설정합니다.
     * 
     * @param defaultValue 사용할 기본값
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder withDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    /**
     * WritingOptions을 설정합니다.
     * 
     * @param options 사용할 WritingOptions
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder withWritingOptions(WritingOptions options) {
        this.writingOptions = options;
        return this;
    }
    
    /**
     * 설정된 옵션으로 JSON5Object를 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz) {
        // DeserializationContext 생성 및 설정 - 임시로 기본값 사용
        TypeSchema tempTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object tempRootObject = tempTypeSchema != null ? tempTypeSchema.newInstance() : null;
        DeserializationContext context = new DeserializationContext(tempRootObject, json5Object, tempTypeSchema);
        
        context.setIgnoreError(ignoreError);
        context.setStrictTypeChecking(strictTypeChecking);
        context.setDefaultValue(defaultValue);
        if (writingOptions != null) {
            context.setWritingOptions(writingOptions);
        }
        
        // 역직렬화 실행
        return serializer.deserialize(json5Object, clazz, context);
    }
    
    /**
     * 설정된 옵션으로 JSON5Object를 기존 객체에 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject) {
        // DeserializationContext 생성 및 설정 - 임시로 기본값 사용
        TypeSchema tempTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(targetObject.getClass());
        DeserializationContext context = new DeserializationContext(targetObject, json5Object, tempTypeSchema);
        
        context.setIgnoreError(ignoreError);
        context.setStrictTypeChecking(strictTypeChecking);
        context.setDefaultValue(defaultValue);
        if (writingOptions != null) {
            context.setWritingOptions(writingOptions);
        }
        
        // 역직렬화 실행
        return serializer.deserialize(json5Object, targetObject, context);
    }
}