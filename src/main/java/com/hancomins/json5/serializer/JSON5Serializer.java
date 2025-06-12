package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Element;
import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.util.DataConverter;

import java.util.*;

/**
 * JSON5 직렬화/역직렬화를 위한 메인 클래스입니다.
 * 
 * <p>이 클래스는 Java 객체와 JSON5 형식 간의 변환을 담당합니다.
 * 기존의 static 메소드 기반 API와 새로운 인스턴스 기반 Fluent API를 모두 제공합니다.</p>
 * 
 * <h3>기본 사용법:</h3>
 * <pre>{@code
 * // 기존 방식 (하위 호환성 유지)
 * JSON5Object json = JSON5Serializer.toJSON5Object(myObject);
 * MyClass obj = JSON5Serializer.fromJSON5Object(json, MyClass.class);
 * 
 * // 새로운 Fluent API
 * JSON5Serializer serializer = JSON5Serializer.builder()
 *     .ignoreUnknownProperties()
 *     .enableSchemaCache()
 *     .build();
 * 
 * JSON5Object json = serializer.forSerialization()
 *     .includeNullValues()
 *     .serialize(myObject);
 * }</pre>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 1.0
 */
public class JSON5Serializer {



    // 기본 인스턴스 (singleton)
    private static final JSON5Serializer DEFAULT_INSTANCE = new JSON5Serializer(SerializerConfiguration.getDefault());
    
    // 인스턴스 필드들
    private final SerializationEngine serializationEngine;
    private final DeserializationEngine deserializationEngine;
    private final SerializerConfiguration configuration;

    /**
     * 기본 생성자 (내부 사용용 - 하위 호환성)
     */
    private JSON5Serializer() {
        this(SerializerConfiguration.getDefault());
    }


    public static void addOnCatchExceptionListener(OnCatchExceptionListener listener) {
        CatchExceptionProvider.getInstance().addOnCatchException(listener);
    }

    /**
     * 설정을 받는 생성자
     * 
     * @param configuration 직렬화 설정
     */
    public JSON5Serializer(SerializerConfiguration configuration) {
        this.configuration = configuration;
        this.serializationEngine = new SerializationEngine(configuration);
        this.deserializationEngine = new DeserializationEngine(configuration);
    }

    /**
     * 기본 인스턴스를 반환합니다.
     * 
     * @return 기본 JSON5Serializer 인스턴스
     */
    public static JSON5Serializer getInstance() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Builder를 통해 JSON5Serializer를 생성합니다.
     * 
     * @return 새로운 JSON5SerializerBuilder 인스턴스
     */
    public static JSON5SerializerBuilder builder() {
        return new JSON5SerializerBuilder();
    }

    /**
     * 직렬화를 위한 Builder를 반환합니다.
     * 
     * @return SerializationBuilder 인스턴스
     */
    public SerializationBuilder forSerialization() {
        return new SerializationBuilder(this);
    }

    /**
     * 역직렬화를 위한 Builder를 반환합니다.
     * 
     * @return DeserializationBuilder 인스턴스
     */
    public DeserializationBuilder forDeserialization() {
        return new DeserializationBuilder(this);
    }

    // ============== 인스턴스 메소드들 (새로운 API) ==============

    /**
     * 객체를 JSON5Object로 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public JSON5Object serialize(Object obj) {
        Objects.requireNonNull(obj, "obj is null");
        return serializationEngine.serialize(obj);
    }

    /**
     * 컨텍스트와 함께 객체를 직렬화합니다.
     * 
     * @param obj 직렬화할 객체
     * @param context 직렬화 컨텍스트
     * @return 직렬화된 JSON5Object
     * @throws JSON5SerializerException 직렬화 중 오류가 발생한 경우
     */
    public JSON5Object serialize(Object obj, SerializationContext context) {
        Objects.requireNonNull(obj, "obj is null");
        return serializationEngine.serialize(obj, context);
    }

    /**
     * JSON5Object를 지정된 클래스의 객체로 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz) {
        return deserializationEngine.deserialize(json5Object, clazz);
    }

    /**
     * 컨텍스트와 함께 JSON5Object를 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param context 역직렬화 컨텍스트
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> clazz, DeserializationContext context) {
        return deserializationEngine.deserialize(json5Object, clazz, context);
    }

    /**
     * JSON5Object를 기존 객체에 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject) {
        return deserializationEngine.deserialize(json5Object, targetObject);
    }

    /**
     * 컨텍스트와 함께 JSON5Object를 기존 객체에 역직렬화합니다.
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param context 역직렬화 컨텍스트
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 중 오류가 발생한 경우
     */
    public <T> T deserialize(JSON5Object json5Object, T targetObject, DeserializationContext context) {
        return deserializationEngine.deserialize(json5Object, targetObject, context);
    }

    // ============== Static 메소드들 (기존 API - 하위 호환성 유지) ==============

    /**
     * 클래스가 직렬화 가능한지 확인합니다.
     * 
     * @param clazz 확인할 클래스
     * @return 직렬화 가능하면 true
     */
    public static boolean serializable(Class<?> clazz) {
        if(TypeSchemaMap.getInstance().hasTypeInfo(clazz)) {
            return true;
        }
        // enum 타입일 경우 true 반환
        if(clazz.isEnum()) {
            return true;
        }
        return !(clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()));
    }

    /**
     * 객체를 JSON5Object로 직렬화합니다. (기존 API)
     * 
     * @param obj 직렬화할 객체
     * @return 직렬화된 JSON5Object
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다: {@code JSON5Serializer.getInstance().serialize(obj)}
     */
    @Deprecated
    public static JSON5Object toJSON5Object(Object obj) {
        return getInstance().serialize(obj);
    }

    /**
     * TypeSchema 기반 직렬화 (기존 API)
     * 
     * @param typeSchema 타입 스키마
     * @param rootObject 루트 객체
     * @return 직렬화된 JSON5Object
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static JSON5Object serializeTypeElement(TypeSchema typeSchema, final Object rootObject) {
        return getInstance().serializationEngine.serializeTypeElement(typeSchema, rootObject);
    }

    /**
     * Map을 JSON5Object로 변환합니다. (기존 API)
     * 
     * @param map 변환할 Map
     * @return 변환된 JSON5Object
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static JSON5Object mapToJSON5Object(Map<String, ?> map) {
        return getInstance().serializationEngine.serializeMap(map, null);
    }

    /**
     * Collection을 JSON5Array로 변환합니다. (기존 API)
     * 
     * @param collection 변환할 Collection
     * @return 변환된 JSON5Array
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static JSON5Array collectionToJSON5Array(Collection<?> collection) {
        return getInstance().serializationEngine.serializeCollection(collection, null);
    }

    /**
     * JSON5Array를 List로 변환합니다. (기존 API)
     * 
     * @param json5Array 변환할 JSON5Array
     * @param valueType 값 타입
     * @param <T> 값 타입
     * @return 변환된 List
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType) {
        return json5ArrayToList(json5Array, valueType, null, false, null);
    }

    /**
     * JSON5Array를 List로 변환합니다. (기존 API)
     * 
     * @param json5Array 변환할 JSON5Array
     * @param valueType 값 타입
     * @param ignoreError 오류 무시 여부
     * @param <T> 값 타입
     * @return 변환된 List
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType, boolean ignoreError) {
        return json5ArrayToList(json5Array, valueType, null, ignoreError, null);
    }

    /**
     * JSON5Array를 List로 변환합니다. (기존 API)
     * 
     * @param json5Array 변환할 JSON5Array
     * @param valueType 값 타입
     * @param writingOptions WritingOptions
     * @param ignoreError 오류 무시 여부
     * @param defaultValue 기본값
     * @param <T> 값 타입
     * @return 변환된 List
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    public static <T> List<T> json5ArrayToList(JSON5Array json5Array, Class<T> valueType, WritingOptions writingOptions, boolean ignoreError, T defaultValue) {
        return getInstance().deserializationEngine.deserializeToList(json5Array, valueType, writingOptions, ignoreError, defaultValue);
    }

    /**
     * JSON5Object를 Map으로 변환합니다. (기존 API)
     * 
     * @param json5Object 변환할 JSON5Object
     * @param valueType 값 타입
     * @param <T> 값 타입
     * @return 변환된 Map
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다
     */
    @Deprecated
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Map<String, T> fromJSON5ObjectToMap(JSON5Object json5Object, Class<T> valueType) {
        return getInstance().deserializationEngine.deserializeToMap(json5Object, valueType);
    }

    /**
     * JSON5Object를 객체로 역직렬화합니다. (기존 API)
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param clazz 대상 클래스
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다: {@code JSON5Serializer.getInstance().deserialize(json5Object, clazz)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static<T> T fromJSON5Object(JSON5Object json5Object, Class<T> clazz) {
        return getInstance().deserialize(json5Object, clazz);
    }

    /**
     * JSON5Object를 기존 객체에 역직렬화합니다. (기존 API)
     * 
     * @param json5Object 역직렬화할 JSON5Object
     * @param targetObject 대상 객체
     * @param <T> 대상 타입
     * @return 역직렬화된 객체
     * @deprecated 새로운 인스턴스 기반 API 사용을 권장합니다: {@code JSON5Serializer.getInstance().deserialize(json5Object, targetObject)}
     */
    @Deprecated
    public static<T> T fromJSON5Object(final JSON5Object json5Object, T targetObject) {
        return getInstance().deserialize(json5Object, targetObject);
    }

    // ============== Getter 메소드들 ==============

    /**
     * 현재 설정을 반환합니다.
     * 
     * @return SerializerConfiguration
     */
    public SerializerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * SerializationEngine을 반환합니다.
     * 
     * @return SerializationEngine
     */
    public SerializationEngine getSerializationEngine() {
        return serializationEngine;
    }

    /**
     * DeserializationEngine을 반환합니다.
     * 
     * @return DeserializationEngine
     */
    public DeserializationEngine getDeserializationEngine() {
        return deserializationEngine;
    }
    
    // ============== 값 공급자 관련 메서드들 ==============
    
    /**
     * 값 공급자 수동 등록 (선택사항)
     */
    public static void registerValueProvider(Class<?> providerClass) {
        SerializationEngine.registerValueProvider(providerClass);
        // DeserializationEngine은 동일한 레지스트리 인스턴스 공유
    }
    
    /**
     * 값 공급자 여부 확인
     */
    public static boolean isValueProvider(Class<?> clazz) {
        try {
            Class<?> registryClass = Class.forName("com.hancomins.json5.serializer.provider.ValueProviderRegistry");
            Object registry = registryClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method isValueProviderMethod = registryClass.getMethod("isValueProvider", Class.class);
            return (Boolean) isValueProviderMethod.invoke(registry, clazz);
        } catch (Exception e) {
            return false;
        }
    }

    // ============== 기존 내부 클래스들 (하위 호환성을 위해 유지) ==============

    @SuppressWarnings("rawtypes")
    private static class ArraySerializeDequeueItem {
        Iterator<?> iterator;
        JSON5Array JSON5Array;

        Collection collectionObject;
        int index = 0;
        int arraySize = 0;
        private ArraySerializeDequeueItem(Iterator<?> iterator, JSON5Array JSON5Array) {
            this.iterator = iterator;
            this.JSON5Array = JSON5Array;
        }

        private ArraySerializeDequeueItem(JSON5Array JSON5Array, Collection collection) {
            this.JSON5Array = JSON5Array;
            this.arraySize = JSON5Array.size();
            this.collectionObject = collection;
        }

        private int getEndIndex() {
            return arraySize - 1;
        }

        /**
         * 역직렬화에서 사용됨.
         */
        private void setArrayIndex(int index) {
            this.index = index;
        }
    }

    private static class ObjectSerializeDequeueItem {
        Iterator<Object> keyIterator;
        ISchemaNode ISchemaNode;
        JSON5Element resultElement;

        private ObjectSerializeDequeueItem(Iterator<Object> keyIterator, ISchemaNode ISchemaNode, JSON5Element resultElement) {
            this.keyIterator = keyIterator;
            this.ISchemaNode = ISchemaNode;
            this.resultElement = resultElement;
        }
    }
}