package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.options.WritingOptions;

import java.util.function.Predicate;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 역직렬화 빌더 클래스입니다.
 * 
 * <p>Fluent API를 통해 역직렬화 옵션을 설정하고 실행할 수 있습니다.
 * 고급 체인 옵션을 통해 조건부 설정, 배치 처리, 유효성 검사 등을 지원합니다.</p>
 * 
 * <h3>기본 사용법:</h3>
 * <pre>{@code
 * MyClass obj = serializer.forDeserialization()
 *     .ignoreErrors()
 *     .withDefaultValue(new MyClass())
 *     .deserialize(json, MyClass.class);
 * }</pre>
 * 
 * <h3>고급 체인 옵션:</h3>
 * <pre>{@code
 * // 조건부 설정
 * MyClass obj = serializer.forDeserialization()
 *     .when(json -> json.has("version"))
 *     .then(builder -> builder.enableStrictValidation())
 *     .deserialize(json, MyClass.class);
 * 
 * // 배치 처리
 * List<MyClass> results = serializer.forDeserialization()
 *     .ignoreErrors()
 *     .deserializeMultiple(jsonArray, MyClass.class);
 * 
 * // 유효성 검사
 * MyClass obj = serializer.forDeserialization()
 *     .validateWith(this::isValidObject)
 *     .deserialize(json, MyClass.class);
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class DeserializationBuilder {
    
    private final JSON5Serializer serializer;
    private boolean ignoreError = false;
    private boolean strictTypeChecking = true;
    private Object defaultValue = null;
    private WritingOptions writingOptions;
    private List<Predicate<Object>> validators = new ArrayList<>();
    private Predicate<JSON5Object> condition;
    private Function<DeserializationBuilder, DeserializationBuilder> conditionalConfigurer;
    private boolean enableStrictValidation = false;
    private Map<String, Object> fieldDefaults = new HashMap<>();
    
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
    
    // =========================
    // 고급 체인 옵션들
    // =========================
    
    /**
     * 조건부 설정을 시작합니다.
     * JSON 구조에 따라 조건적으로 설정을 적용할 수 있습니다.
     * 
     * @param condition 적용 조건
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder when(Predicate<JSON5Object> condition) {
        this.condition = condition;
        return this;
    }
    
    /**
     * when 조건이 true일 때 실행할 설정을 정의합니다.
     * 
     * @param configurer Builder 설정 함수
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder then(Function<DeserializationBuilder, DeserializationBuilder> configurer) {
        if (condition != null) {
            this.conditionalConfigurer = configurer;
        }
        return this;
    }
    
    /**
     * 역직렬화 결과에 대한 유효성 검사를 추가합니다.
     * 
     * @param validator 유효성 검사 함수
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder validateWith(Predicate<Object> validator) {
        this.validators.add(validator);
        return this;
    }
    
    /**
     * 엄격한 유효성 검사를 활성화합니다.
     * 모든 필드가 JSON에 존재해야 하며, 타입이 정확히 일치해야 합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder enableStrictValidation() {
        this.enableStrictValidation = true;
        this.strictTypeChecking = true;
        return this;
    }
    
    /**
     * 특정 필드에 대한 기본값을 설정합니다.
     * JSON에 해당 필드가 없을 때 사용될 값을 지정합니다.
     * 
     * @param fieldName 필드명
     * @param defaultValue 기본값
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder withFieldDefault(String fieldName, Object defaultValue) {
        this.fieldDefaults.put(fieldName, defaultValue);
        return this;
    }
    
    /**
     * 안전한 역직렬화 모드를 활성화합니다.
     * 오류를 무시하고 가능한 한 안전하게 역직렬화를 수행합니다.
     * 
     * @return 이 Builder 인스턴스
     */
    public DeserializationBuilder enableSafeMode() {
        this.ignoreError = true;
        this.strictTypeChecking = false;
        return this;
    }
    
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
     * 설정된 옵션으로 JSON5Object를 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz) {
        // 조건부 설정 적용
        if (condition != null && conditionalConfigurer != null && condition.test(json5Object)) {
            conditionalConfigurer.apply(this);
        }
        
        // DeserializationContext 생성 및 설정
        TypeSchema tempTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        Object tempRootObject = tempTypeSchema != null ? tempTypeSchema.newInstance() : null;
        DeserializationContext context = new DeserializationContext(tempRootObject, json5Object, tempTypeSchema);
        
        // 기본 옵션 적용
        context.setIgnoreError(ignoreError);
        context.setStrictTypeChecking(strictTypeChecking);
        context.setDefaultValue(defaultValue);
        if (writingOptions != null) {
            context.setWritingOptions(writingOptions);
        }
        
        // 고급 옵션 적용
        if (enableStrictValidation) {
            context.setStrictValidation(enableStrictValidation);
        }
        if (!fieldDefaults.isEmpty()) {
            context.setFieldDefaults(fieldDefaults);
        }
        
        // 역직렬화 실행 - 컴텍스트와 함께
        T result = serializer.deserialize(json5Object, clazz, context);
        
        // fieldDefaults 적용 - 역직렬화 후 기본값 설정
        if (result != null && !fieldDefaults.isEmpty()) {
            applyFieldDefaults(result, json5Object, fieldDefaults);
        }
        
        // 유효성 검사 수행
        if (result != null && !validators.isEmpty()) {
            for (Predicate<Object> validator : validators) {
                if (!validator.test(result)) {
                    if (ignoreError) {
                        return (T) defaultValue;
                    } else {
                        throw new JSON5SerializerException("유효성 검사 실패: " + result);
                    }
                }
            }
        }
        
        return result;
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
        // 조건부 설정 적용
        if (condition != null && conditionalConfigurer != null && condition.test(json5Object)) {
            conditionalConfigurer.apply(this);
        }
        
        // 기본 역직렬화 실행 - 기존 객체에 역직렬화
        T result = serializer.deserialize(json5Object, targetObject);
        
        // 유효성 검사 수행
        if (result != null && !validators.isEmpty()) {
            for (Predicate<Object> validator : validators) {
                if (!validator.test(result)) {
                    if (ignoreError) {
                        return targetObject;
                    } else {
                        throw new JSON5SerializerException("유효성 검사 실패: " + result);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 여러 JSON5Object를 배치로 역직렬화합니다.
     * 
     * @param jsonObjects 역직렬화할 JSON5Object들
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체들의 리스트
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> List<T> deserializeMultiple(List<JSON5Object> jsonObjects, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        for (JSON5Object json : jsonObjects) {
            try {
                T result = deserialize(json, clazz);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                if (!ignoreError) {
                    throw e;
                }
                // 오류 무시 모드일 때 기본값 추가
                if (defaultValue != null) {
                    results.add((T) defaultValue);
                }
            }
        }
        return results;
    }
    
    /**
     * JSON5Array를 리스트로 역직렬화합니다.
     * 
     * @param jsonArray 역직렬화할 JSON5Array
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체들의 리스트
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> List<T> deserializeFromArray(JSON5Array jsonArray, Class<T> clazz) {
        List<JSON5Object> jsonObjects = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSON5Object jsonObj = jsonArray.getJSON5Object(i);
            if (jsonObj != null) {
                jsonObjects.add(jsonObj);
            }
        }
        return deserializeMultiple(jsonObjects, clazz);
    }
    
    /**
     * 필드 기본값을 적용합니다.
     * JSON에 해당 필드가 없으면 기본값을 설정합니다.
     * 
     * @param result 대상 객체
     * @param json5Object 원본 JSON
     * @param fieldDefaults 필드 기본값 맵
     */
    private void applyFieldDefaults(Object result, JSON5Object json5Object, Map<String, Object> fieldDefaults) {
        try {
            Class<?> resultClass = result.getClass();
            
            for (Map.Entry<String, Object> entry : fieldDefaults.entrySet()) {
                String fieldName = entry.getKey();
                Object defaultValue = entry.getValue();
                
                // JSON에 해당 필드가 없으면 기본값 설정
                if (!json5Object.has(fieldName)) {
                    try {
                        // setter 메서드 찾기
                        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        java.lang.reflect.Method setter = findSetter(resultClass, setterName, defaultValue);
                        
                        if (setter != null) {
                            setter.setAccessible(true);
                            setter.invoke(result, defaultValue);
                        } else {
                            // 필드 직접 접근
                            java.lang.reflect.Field field = findField(resultClass, fieldName);
                            if (field != null) {
                                field.setAccessible(true);
                                field.set(result, defaultValue);
                            }
                        }
                    } catch (Exception e) {
                        if (!ignoreError) {
                            throw new JSON5SerializerException("필드 기본값 설정 실패: " + fieldName, e);
                        }
                        // 무시
                    }
                }
            }
        } catch (Exception e) {
            if (!ignoreError) {
                throw new JSON5SerializerException("필드 기본값 적용 실패", e);
            }
        }
    }
    
    /**
     * setter 메서드를 찾습니다.
     */
    private java.lang.reflect.Method findSetter(Class<?> clazz, String setterName, Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            // 정확한 타입으로 시도
            return clazz.getMethod(setterName, value.getClass());
        } catch (NoSuchMethodException e) {
            // 모든 setter 메서드를 찾아서 매칭
            for (java.lang.reflect.Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName) && 
                    method.getParameterCount() == 1 &&
                    method.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
                    return method;
                }
            }
        }
        return null;
    }
    
    /**
     * 필드를 찾습니다.
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}