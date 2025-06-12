package com.hancomins.json5.serializer;

/**
 * TypeHandler와 TypeHandlerRegistry를 생성하는 팩토리 클래스입니다.
 * 
 * <p>이 클래스는 다음 기능을 제공합니다:</p>
 * <ul>
 *   <li>기본 TypeHandlerRegistry 생성</li>
 *   <li>커스텀 TypeHandler 생성</li>
 *   <li>미리 정의된 설정의 TypeHandlerRegistry 제공</li>
 * </ul>
 * 
 * @author ice3x2
 * @version 1.1
 * @since 2.0
 */
public class TypeHandlerFactory {
    
    private static volatile TypeHandlerRegistry defaultRegistry;
    private static final Object LOCK = new Object();
    
    /**
     * 기본 TypeHandler들이 등록된 TypeHandlerRegistry를 반환합니다.
     * Singleton 패턴으로 구현되어 한 번만 생성됩니다.
     * 
     * @return 기본 TypeHandlerRegistry
     */
    public static TypeHandlerRegistry createDefaultRegistry() {
        if (defaultRegistry == null) {
            synchronized (LOCK) {
                if (defaultRegistry == null) {
                    defaultRegistry = new TypeHandlerRegistry.Builder()
                        .withDefaultHandlers()
                        .build();
                }
            }
        }
        return defaultRegistry;
    }
    
    /**
     * 새로운 TypeHandlerRegistry를 생성합니다.
     * 기본 핸들러들이 포함됩니다.
     * 
     * @return 새로운 TypeHandlerRegistry
     */
    public static TypeHandlerRegistry createNewRegistry() {
        return new TypeHandlerRegistry.Builder()
            .withDefaultHandlers()
            .build();
    }
    
    /**
     * 빈 TypeHandlerRegistry를 생성합니다.
     * 사용자가 직접 핸들러를 등록해야 합니다.
     * 
     * @return 빈 TypeHandlerRegistry
     */
    public static TypeHandlerRegistry createEmptyRegistry() {
        return new TypeHandlerRegistry();
    }
    
    /**
     * 특정 타입을 위한 커스텀 TypeHandler를 생성합니다.
     * 
     * @param targetType 대상 타입
     * @param serializationLogic 직렬화 로직
     * @param deserializationLogic 역직렬화 로직
     * @return 커스텀 TypeHandler
     */
    public static TypeHandler createCustomHandler(
            Class<?> targetType,
            SerializationFunction serializationLogic,
            DeserializationFunction deserializationLogic) {
        
        return createCustomHandler(targetType, serializationLogic, deserializationLogic, TypeHandler.TypeHandlerPriority.NORMAL);
    }
    
    /**
     * 특정 타입을 위한 커스텀 TypeHandler를 생성합니다.
     * 
     * @param targetType 대상 타입
     * @param serializationLogic 직렬화 로직
     * @param deserializationLogic 역직렬화 로직
     * @param priority 우선순위
     * @return 커스텀 TypeHandler
     */
    public static TypeHandler createCustomHandler(
            Class<?> targetType,
            SerializationFunction serializationLogic,
            DeserializationFunction deserializationLogic,
            TypeHandler.TypeHandlerPriority priority) {
        
        return new TypeHandler() {
            @Override
            public boolean canHandle(Types type, Class<?> clazz) {
                return clazz != null && targetType.isAssignableFrom(clazz);
            }
            
            @Override
            public Object handleSerialization(Object value, SerializationContext context) throws SerializationException {
                try {
                    return serializationLogic.serialize(value, context);
                } catch (Exception e) {
                    throw new SerializationException("Custom serialization failed for type: " + targetType.getName(), e);
                }
            }
            
            @Override
            public Object handleDeserialization(Object element, Class<?> targetType, 
                                              DeserializationContext context) throws DeserializationException {
                try {
                    return deserializationLogic.deserialize(element, targetType, context);
                } catch (Exception e) {
                    throw new DeserializationException("Custom deserialization failed for type: " + targetType.getName(), e);
                }
            }
            
            @Override
            public TypeHandlerPriority getPriority() {
                return priority;
            }
        };
    }
    
    /**
     * 기본 TypeHandlerRegistry의 복사본을 생성합니다.
     * 추가적인 핸들러를 등록할 수 있습니다.
     * 
     * @return 기본 핸들러들이 포함된 새로운 TypeHandlerRegistry
     */
    public static TypeHandlerRegistry copyDefaultRegistry() {
        TypeHandlerRegistry newRegistry = createEmptyRegistry();
        TypeHandlerRegistry defaultReg = createDefaultRegistry();
        
        // 기본 핸들러들을 새 레지스트리에 복사
        for (TypeHandler handler : defaultReg.getAllHandlers()) {
            newRegistry.registerHandler(handler);
        }
        
        return newRegistry;
    }
    
    /**
     * 직렬화를 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface SerializationFunction {
        Object serialize(Object value, SerializationContext context) throws Exception;
    }
    
    /**
     * 역직렬화를 위한 함수형 인터페이스입니다.
     */
    @FunctionalInterface
    public interface DeserializationFunction {
        Object deserialize(Object element, Class<?> targetType, DeserializationContext context) throws Exception;
    }
    
    /**
     * 기본 TypeHandlerRegistry를 재설정합니다.
     * 테스트나 특수한 상황에서 사용됩니다.
     */
    public static void resetDefaultRegistry() {
        synchronized (LOCK) {
            defaultRegistry = null;
        }
    }
}
