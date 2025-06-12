package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.options.WritingOptions;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;

/**
 * 직렬화 빌더 클래스입니다.
 * 
 * <p>Fluent API를 통해 직렬화 옵션을 설정하고 실행할 수 있습니다.
 * 고급 체인 옵션을 통해 조건부 설정, 변환 파이프라인, 배치 처리 등을 지원합니다.</p>
 * 
 * <h3>기본 사용법:</h3>
 * <pre>{@code
 * JSON5Object json = serializer.forSerialization()
 *     .withWritingOptions(WritingOptions.json5Pretty())
 *     .includeNullValues()
 *     .serialize(myObject);
 * }</pre>
 * 
 * <h3>고급 체인 옵션:</h3>
 * <pre>{@code
 * // 조건부 설정
 * JSON5Object json = serializer.forSerialization()
 *     .when(obj -> obj instanceof SensitiveData)
 *     .then(builder -> builder.ignoreFields("password", "secret"))
 *     .serialize(myObject);
 * 
 * // 배치 처리
 * List<JSON5Object> results = serializer.forSerialization()
 *     .withWritingOptions(WritingOptions.json5())
 *     .serializeMultiple(objects);
 * 
 * // 변환 파이프라인
 * JSON5Object json = serializer.forSerialization()
 *     .transform(obj -> preprocess(obj))
 *     .serialize(myObject);
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class SerializationBuilder {
    
    private final JSON5Serializer serializer;
    private WritingOptions writingOptions;
    private boolean includeNullValues = false;
    private Set<String> ignoredFields = new HashSet<>();
    private List<Function<Object, Object>> transformers = new ArrayList<>();
    private Predicate<Object> condition;
    private boolean conditionMatched = true;
    private Function<SerializationBuilder, SerializationBuilder> conditionalConfigurer;
    private Set<String> onlyFields;
    private int maxDepth = -1;
    private int maxStringLength = -1;
    
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
    
    // =========================
    // 고급 체인 옵션들
    // =========================
    
    /**
     * 조건부 설정을 시작합니다.
     * 조건이 true일 때만 다음 설정들이 적용됩니다.
     * 
     * @param condition 적용 조건
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder when(Predicate<Object> condition) {
        this.condition = condition;
        this.conditionMatched = false; // 아직 평가하지 않음
        return this;
    }
    
    /**
     * when 조건이 true일 때 실행할 설정을 정의합니다.
     * 
     * @param configurer Builder 설정 함수
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder then(Function<SerializationBuilder, SerializationBuilder> configurer) {
        if (condition != null) {
            // 조건 평가는 실제 직렬화 시점에 수행
            // 여기서는 설정만 저장
            this.conditionalConfigurer = configurer;
        }
        return this;
    }
    
    /**
     * 객체 변환 파이프라인을 추가합니다.
     * 직렬화 전에 객체를 변환하는 함수를 추가할 수 있습니다.
     * 
     * @param transformer 객체 변환 함수
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder transform(Function<Object, Object> transformer) {
        this.transformers.add(transformer);
        return this;
    }
    
    /**
     * 객체를 필터링합니다.
     * 조건에 맞지 않는 객체는 null로 변환됩니다.
     * 
     * @param filter 필터 조건
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder filter(Predicate<Object> filter) {
        this.transformers.add(obj -> filter.test(obj) ? obj : null);
        return this;
    }
    
    /**
     * 객체의 특정 필드만 유지합니다.
     * 
     * @param fieldNames 유지할 필드명들
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder onlyFields(String... fieldNames) {
        // 구현은 실제 직렬화 시점에 수행
        this.onlyFields = new HashSet<>();
        for (String fieldName : fieldNames) {
            this.onlyFields.add(fieldName);
        }
        return this;
    }
    
    /**
     * 부분 직렬화 옵션을 설정합니다.
     * 깊이 제한, 크기 제한 등을 설정할 수 있습니다.
     * 
     * @param maxDepth 최대 깊이
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder withPartialOptions(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }
    
    /**
     * 최대 문자열 길이를 제한합니다.
     * 
     * @param maxLength 최대 문자열 길이
     * @return 이 Builder 인스턴스
     */
    public SerializationBuilder withMaxStringLength(int maxLength) {
        this.maxStringLength = maxLength;
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
        // 조건부 설정 적용
        if (condition != null && conditionalConfigurer != null && condition.test(obj)) {
            conditionalConfigurer.apply(this);
        }
        
        // 대상 객체 변환 적용
        Object transformedObj = applyTransformations(obj);
        if (transformedObj == null) {
            return null;
        }
        
        // 기본 직렬화 실행
        JSON5Object result = serializer.serialize(transformedObj);
        
        // 필드 필터링 후처리 (ignoreFields, onlyFields)
        if (result != null && (!ignoredFields.isEmpty() || onlyFields != null)) {
            result = applyFieldFiltering(result);
        }
        
        // WritingOptions 적용
        if (writingOptions != null && result != null) {
            result.setWritingOptions(writingOptions);
        }
        
        return result;
    }
    
    /**
     * 결과 JSON에 필드 필터링을 적용합니다.
     */
    private JSON5Object applyFieldFiltering(JSON5Object original) {
        JSON5Object filtered = new JSON5Object();
        
        // WritingOptions 복사
        if (original.getWritingOptions() != null) {
            filtered.setWritingOptions(original.getWritingOptions());
        }
        
        // 모든 키를 검사하여 필터링
        for (String key : original.keySet()) {
            boolean shouldInclude = true;
            
            // onlyFields가 설정된 경우
            if (onlyFields != null && !onlyFields.isEmpty()) {
                shouldInclude = onlyFields.contains(key);
            }
            
            // ignoredFields 검사
            if (shouldInclude && ignoredFields.contains(key)) {
                shouldInclude = false;
            }
            
            if (shouldInclude) {
                filtered.put(key, original.get(key));
            }
        }
        
        return filtered;
    }
    
    /**
     * 여러 객체를 배치로 직렬화합니다.
     * 
     * @param objects 직렬화할 객체들
     * @return 직렬화된 JSON5Object들의 리스트
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public List<JSON5Object> serializeMultiple(List<?> objects) {
        List<JSON5Object> results = new ArrayList<>();
        for (Object obj : objects) {
            JSON5Object result = serialize(obj);
            if (result != null) {
                results.add(result);
            }
        }
        return results;
    }
    
    /**
     * 객체를 JSON5Array로 직렬화합니다.
     * 
     * @param objects 직렬화할 객체들
     * @return 직렬화된 JSON5Array
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public JSON5Array serializeToArray(List<?> objects) {
        JSON5Array array = new JSON5Array();
        if (writingOptions != null) {
            array.setWritingOptions(writingOptions);
        }
        
        for (Object obj : objects) {
            JSON5Object result = serialize(obj);
            if (result != null) {
                array.put(result);
            }
        }
        return array;
    }
    
    /**
     * 변환 파이프라인을 적용합니다.
     * 
     * @param obj 원본 객체
     * @return 변환된 객체
     */
    private Object applyTransformations(Object obj) {
        Object result = obj;
        for (Function<Object, Object> transformer : transformers) {
            result = transformer.apply(result);
            if (result == null) {
                break;
            }
        }
        return result;
    }
}