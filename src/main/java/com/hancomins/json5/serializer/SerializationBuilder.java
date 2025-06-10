package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.options.WritingOptions;

import java.util.Set;
import java.util.HashSet;

/**
 * 직렬화 빌더 클래스입니다.
 * 
 * <p>Fluent API를 통해 직렬화 옵션을 설정하고 실행할 수 있습니다.</p>
 * 
 * @author JSON5 팀
 * @version 2.0
 * @since 2.0
 */
public class SerializationBuilder {
    
    private final JSON5Serializer serializer;
    private WritingOptions writingOptions;
    private boolean includeNullValues = false;
    private Set<String> ignoredFields = new HashSet<>();
    
    /**
     * SerializationBuilder를 생성합니다.
     * 
     * @param serializer 사용할 JSON5Serializer 인스턴스
     */
    public SerializationBuilder(JSON5Serializer serializer) {
        this.serializer = serializer;
    }
    
    /**
     * WritingOptions을 설정합니다.
     * 
     * @param options 사용할 WritingOptions
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder withWritingOptions(WritingOptions options) {
        this.writingOptions = options;
        return this;
    }
    
    /**
     * null 값을 포함하도록 설정합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder includeNullValues() {
        this.includeNullValues = true;
        return this;
    }
    
    /**
     * 무시할 필드들을 설정합니다.
     * 
     * @param fieldNames 무시할 필드명들
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder ignoreFields(String... fieldNames) {
        for (String fieldName : fieldNames) {
            this.ignoredFields.add(fieldName);
        }
        return this;
    }
    
    /**
     * 설정된 옵션으로 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public JSON5Object serialize(Object obj) {
        // SerializationContext 생성 및 설정 - 임시로 기본값 사용
        Object tempRootObject = obj;
        TypeSchema tempTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(obj.getClass());
        SerializationContext context = new SerializationContext(tempRootObject, tempTypeSchema);
        
        if (writingOptions != null) {
            context.setWritingOptions(writingOptions);
        }
        context.setIncludeNullValues(includeNullValues);
        context.setIgnoredFields(ignoredFields);
        
        // 직렬화 실행
        JSON5Object result = serializer.serialize(obj, context);
        
        // WritingOptions 적용
        if (writingOptions != null && result != null) {
            result.setWritingOptions(writingOptions);
        }
        
        return result;
    }
}