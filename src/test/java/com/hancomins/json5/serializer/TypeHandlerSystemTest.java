package com.hancomins.json5.serializer;

import com.hancomins.json5.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * TypeHandler 시스템 통합 테스트
 * 
 * 5.1단계와 5.2단계에서 구현된 TypeHandler 시스템의 전체 기능을 검증합니다.
 */
@DisplayName("TypeHandler 시스템 통합 테스트")
class TypeHandlerSystemTest {
    
    private TypeHandlerRegistry typeHandlerRegistry;
    
    @BeforeEach
    void setUp() {
        typeHandlerRegistry = TypeHandlerFactory.createDefaultRegistry();
    }
    
    @Nested
    @DisplayName("TypeHandlerRegistry 기능 테스트")
    class TypeHandlerRegistryTests {
        
        @Test
        @DisplayName("기본 TypeHandler들이 올바르게 등록되어야 함")
        void shouldRegisterDefaultTypeHandlers() {
            List<TypeHandler> handlers = typeHandlerRegistry.getAllHandlers();
            
            assertNotNull(handlers);
            assertFalse(handlers.isEmpty());
            assertTrue(handlers.size() >= 5);
            
            Set<Class<?>> handlerTypes = new HashSet<>();
            for (TypeHandler handler : handlers) {
                handlerTypes.add(handler.getClass());
            }
            
            assertTrue(handlerTypes.contains(PrimitiveTypeHandler.class));
            assertTrue(handlerTypes.contains(CollectionTypeHandler.class));
            assertTrue(handlerTypes.contains(MapTypeHandler.class));
            assertTrue(handlerTypes.contains(ObjectTypeHandler.class));
            assertTrue(handlerTypes.contains(GenericTypeHandler.class));
        }
        
        @Test
        @DisplayName("커스텀 TypeHandler 등록이 작동해야 함")
        void shouldRegisterCustomTypeHandler() {
            CustomStringHandler customHandler = new CustomStringHandler();
            
            typeHandlerRegistry.registerHandler(customHandler);
            
            // 우선순위가 HIGHEST이므로 커스텀 핸들러가 선택되어야 함
            TypeHandler foundHandler = typeHandlerRegistry.getHandler(Types.String, String.class);
            assertTrue(foundHandler instanceof CustomStringHandler || 
                      foundHandler.getPriority() == TypeHandler.TypeHandlerPriority.HIGHEST);
        }
        
        @Test
        @DisplayName("TypeHandler 제거가 작동해야 함")
        void shouldRemoveTypeHandler() {
            CustomStringHandler customHandler = new CustomStringHandler();
            typeHandlerRegistry.registerHandler(customHandler);
            
            boolean removed = typeHandlerRegistry.removeHandler(customHandler);
            
            assertTrue(removed);
            TypeHandler foundHandler = typeHandlerRegistry.getHandler(Types.String, String.class);
            assertNotEquals(customHandler, foundHandler);
        }
        
        @Test
        @DisplayName("TypeHandler 캐싱이 작동해야 함")
        void shouldCacheTypeHandlers() {
            TypeHandler handler1 = typeHandlerRegistry.getHandler(Types.String, String.class);
            TypeHandler handler2 = typeHandlerRegistry.getHandler(Types.String, String.class);
            
            assertSame(handler1, handler2);
            assertTrue(typeHandlerRegistry.getCacheSize() > 0);
        }
    }
    
    @Nested
    @DisplayName("기본형 타입 처리 테스트")
    class PrimitiveTypeHandlerTests {
        
        @Test
        @DisplayName("기본형 타입들이 올바르게 처리되어야 함")
        void shouldHandlePrimitiveTypes() {
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.Integer, Integer.class);
            
            assertNotNull(handler);
            assertInstanceOf(PrimitiveTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.Integer, Integer.class));
        }
        
        @Test
        @DisplayName("enum 타입이 올바르게 처리되어야 함")
        void shouldHandleEnumTypes() {
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.String, TestEnum.class);
            
            assertNotNull(handler);
            assertInstanceOf(PrimitiveTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.String, TestEnum.class));
        }
    }
    
    @Nested
    @DisplayName("컬렉션 타입 처리 테스트")
    class CollectionTypeHandlerTests {
        
        @Test
        @DisplayName("List 타입이 올바르게 처리되어야 함")
        void shouldHandleListTypes() {
            List<String> list = Arrays.asList("a", "b", "c");
            
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.Collection, list.getClass());
            
            assertNotNull(handler);
            assertInstanceOf(CollectionTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.Collection, list.getClass()));
        }
        
        @Test
        @DisplayName("Set 타입이 올바르게 처리되어야 함")
        void shouldHandleSetTypes() {
            Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
            
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.Collection, set.getClass());
            
            assertNotNull(handler);
            assertInstanceOf(CollectionTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.Collection, set.getClass()));
        }
    }
    
    @Nested
    @DisplayName("Map 타입 처리 테스트")
    class MapTypeHandlerTests {
        
        @Test
        @DisplayName("HashMap 타입이 올바르게 처리되어야 함")
        void shouldHandleHashMapTypes() {
            Map<String, Object> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", 123);
            
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.Map, map.getClass());
            
            assertNotNull(handler);
            assertInstanceOf(MapTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.Map, map.getClass()));
        }
    }
    
    @Nested
    @DisplayName("제네릭 타입 처리 테스트")
    class GenericTypeHandlerTests {
        
        @Test
        @DisplayName("제네릭 타입이 올바르게 식별되어야 함")
        void shouldIdentifyGenericTypes() {
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.GenericType, Object.class);
            
            assertNotNull(handler);
            assertInstanceOf(GenericTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.GenericType, Object.class));
        }
        
        @Test
        @DisplayName("추상 클래스가 올바르게 처리되어야 함")
        void shouldHandleAbstractClasses() {
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.AbstractObject, AbstractTestClass.class);
            
            assertNotNull(handler);
            assertInstanceOf(GenericTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.AbstractObject, AbstractTestClass.class));
        }
        
        @Test
        @DisplayName("인터페이스가 올바르게 처리되어야 함")
        void shouldHandleInterfaces() {
            TypeHandler handler = typeHandlerRegistry.getHandler(Types.AbstractObject, TestInterface.class);
            
            assertNotNull(handler);
            assertInstanceOf(GenericTypeHandler.class, handler);
            assertTrue(handler.canHandle(Types.AbstractObject, TestInterface.class));
        }
    }
    
    // 테스트용 헬퍼 클래스들
    enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }
    
    static abstract class AbstractTestClass {
        public abstract void abstractMethod();
    }
    
    interface TestInterface {
        void interfaceMethod();
    }
    
    // 테스트용 커스텀 TypeHandler
    static class CustomStringHandler implements TypeHandler {
        @Override
        public boolean canHandle(Types type, Class<?> clazz) {
            return type == Types.String;
        }
        
        @Override
        public Object handleSerialization(Object value, SerializationContext context) {
            return "CUSTOM:" + value;
        }
        
        @Override
        public Object handleDeserialization(Object element, Class<?> targetType, DeserializationContext context) {
            String str = element.toString();
            return str.startsWith("CUSTOM:") ? str.substring(7) : str;
        }
        
        @Override
        public TypeHandlerPriority getPriority() {
            return TypeHandlerPriority.HIGHEST;
        }
    }
}
