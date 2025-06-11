package com.hancomins.json5.serializer.polymorphic;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.serializer.JSON5SerializerException;
import com.hancomins.json5.serializer.JSON5Serializer;
import com.hancomins.json5.serializer.DeserializationEngine;

/**
 * 다형성 역직렬화를 수행합니다.
 * @JSON5TypeInfo와 @JSON5SubType 어노테이션을 기반으로 적절한 구체 타입을 결정하여 역직렬화합니다.
 */
public class PolymorphicDeserializer {
    private final SubTypeRegistry subTypeRegistry;
    private final TypeResolver typeResolver;
    private DeserializationEngine deserializationEngine;
    
    public PolymorphicDeserializer() {
        this.subTypeRegistry = new SubTypeRegistry();
        this.typeResolver = new TypeResolver();
    }
    
    public PolymorphicDeserializer(SubTypeRegistry subTypeRegistry) {
        this.subTypeRegistry = subTypeRegistry;
        this.typeResolver = new TypeResolver();
    }
    
    /**
     * DeserializationEngine을 설정합니다.
     * @param deserializationEngine 역직렬화 엔진
     */
    public void setDeserializationEngine(DeserializationEngine deserializationEngine) {
        this.deserializationEngine = deserializationEngine;
    }
    
    /**
     * 다형성 역직렬화를 수행합니다.
     * @param json5Object JSON 객체
     * @param baseType 기본 타입 (추상 클래스 또는 인터페이스)
     * @param <T> 반환 타입
     * @return 역직렬화된 객체
     * @throws JSON5SerializerException 역직렬화 실패 시
     */
    public <T> T deserialize(JSON5Object json5Object, Class<T> baseType) {
        if (json5Object == null) {
            return null;
        }
        
        // 1. 타입 정보 확인
        TypeInfo typeInfo = subTypeRegistry.getTypeInfo(baseType);
        if (typeInfo == null) {
            throw new JSON5SerializerException(
                "다형성 타입이 아닙니다. @JSON5TypeInfo 어노테이션이 필요합니다: " + baseType.getName()
            );
        }
        
        // 2. 구체적인 타입 결정
        Class<?> concreteType = determineConcreteType(json5Object, typeInfo);
        
        // 3. 구체적인 타입으로 역직렬화 (순환 참조 방지를 위해 직접 처리)
        try {
            // 다형성 역직렬화에서는 구체 타입이 또 다른 다형성 타입일 수 있으므로
            // 재귀적으로 처리하지 않고 기본 ObjectDeserializer를 사용
            Object result = deserializeConcreteType(json5Object, concreteType);
            return baseType.cast(result);
        } catch (ClassCastException e) {
            throw new JSON5SerializerException(
                String.format("타입 캐스팅 실패: %s를 %s로 캐스팅할 수 없습니다", 
                            concreteType.getName(), baseType.getName()), e
            );
        } catch (Exception e) {
            throw new JSON5SerializerException(
                String.format("역직렬화 실패: %s (대상 타입: %s)", 
                            e.getMessage(), concreteType.getName()), e
            );
        }
    }
    
    /**
     * 다형성 타입 여부를 확인합니다.
     * @param clazz 확인할 클래스
     * @return 다형성 타입이면 true
     */
    public boolean isPolymorphicType(Class<?> clazz) {
        return subTypeRegistry.isPolymorphicType(clazz);
    }
    
    /**
     * 서브타입 레지스트리를 반환합니다.
     * @return 서브타입 레지스트리
     */
    public SubTypeRegistry getSubTypeRegistry() {
        return subTypeRegistry;
    }
    
    /**
     * JSON에서 구체적인 타입을 결정합니다.
     * @param json5Object JSON 객체
     * @param typeInfo 타입 정보
     * @return 구체적인 클래스
     */
    private Class<?> determineConcreteType(JSON5Object json5Object, TypeInfo typeInfo) {
        return typeResolver.resolveType(json5Object, typeInfo);
    }
    
    /**
     * 구체적인 타입으로 역직렬화합니다.
     * @param json5Object JSON 객체
     * @param concreteType 구체적인 타입
     * @return 역직렬화된 객체
     */
    private Object deserializeConcreteType(JSON5Object json5Object, Class<?> concreteType) {
        // 순환 참조를 방지하기 위해 단순한 역직렬화 수행
        // 다형성 체크를 하지 않고 기본 ObjectDeserializer만 사용
        try {
            // 임시로 JSON5Serializer를 사용하지만, 실제로는 다른 방법이 필요
            // 현재는 간단한 방법으로 구현
            Object instance;
            try {
                instance = concreteType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new JSON5SerializerException(
                    String.format("기본 생성자를 찾을 수 없습니다: %s", concreteType.getName()), e
                );
            }
            
            // 간단한 필드 매핑 (실제로는 ObjectDeserializer 로직 필요)
            // 현재는 JSON5Serializer를 사용하여 처리
            return JSON5Serializer.fromJSON5Object(json5Object, concreteType);
        } catch (Exception e) {
            throw new JSON5SerializerException(
                String.format("구체 타입 역직렬화 실패: %s", e.getMessage()), e
            );
        }
    }
    
    /**
     * JSON에서 타입 값을 추출합니다.
     * @param json5Object JSON 객체
     * @param typeInfo 타입 정보
     * @return 타입 값, 없으면 null
     */
    private String extractTypeValue(JSON5Object json5Object, TypeInfo typeInfo) {
        return typeResolver.hasTypeInformation(json5Object, typeInfo) ? 
               json5Object.getString(typeInfo.getTypeProperty()) : null;
    }
}
