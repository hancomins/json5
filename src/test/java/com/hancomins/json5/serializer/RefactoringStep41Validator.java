package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;

import java.util.*;

/**
 * TypeHandler 시스템(4.1 단계)의 기능을 검증하는 클래스입니다.
 * 
 * @author ice3x2
 * @version 1.2
 * @since 2.0
 */
public class RefactoringStep41Validator {
    
    private TypeHandlerRegistry registry;
    private SerializationContext serializationContext;
    private DeserializationContext deserializationContext;
    
    public void setUp() {
        registry = TypeHandlerFactory.createDefaultRegistry();
        serializationContext = new SerializationContext("test", null);
        serializationContext.setTypeHandlerRegistry(registry);
        deserializationContext = new DeserializationContext("test", new JSON5Object(), null);
        deserializationContext.setTypeHandlerRegistry(registry);
    }
    
    // 간단한 assertion 메소드들
    private void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion failed: " + message);
    }
    
    private void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Assertion failed: " + message);
    }
    
    private void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Assertion failed: " + message + " Expected: " + expected + ", Actual: " + actual);
        }
    }
    
    private void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError("Assertion failed: " + message);
    }
    
    private void assertNull(Object obj, String message) {
        if (obj != null) throw new AssertionError("Assertion failed: " + message);
    }
    
    private void assertInstanceOf(Class<?> expected, Object actual, String message) {
        if (actual == null || !expected.isInstance(actual)) {
            throw new AssertionError("Assertion failed: " + message + " Expected instance of: " + expected.getName() + 
                                   ", Actual: " + (actual == null ? "null" : actual.getClass().getName()));
        }
    }
    
    public void testPrimitiveTypeHandler() {
        PrimitiveTypeHandler handler = new PrimitiveTypeHandler();
        
        assertTrue(handler.canHandle(Types.Integer, Integer.class), "Should handle Integer");
        assertTrue(handler.canHandle(Types.String, String.class), "Should handle String");
        assertFalse(handler.canHandle(Types.Collection, List.class), "Should not handle Collection");
        
        try {
            assertEquals(42, handler.handleSerialization(42, serializationContext), "Integer serialization");
            assertEquals("test", handler.handleSerialization("test", serializationContext), "String serialization");
            assertNull(handler.handleSerialization(null, serializationContext), "Null serialization");
        } catch (Exception e) {
            throw new RuntimeException("Primitive serialization failed", e);
        }
        
        assertEquals(TypeHandler.TypeHandlerPriority.HIGHEST, handler.getPriority(), "Priority should be HIGHEST");
    }
    
    public void testCollectionTypeHandler() {
        CollectionTypeHandler handler = new CollectionTypeHandler();
        
        assertTrue(handler.canHandle(Types.Collection, List.class), "Should handle Collection");
        assertFalse(handler.canHandle(Types.String, String.class), "Should not handle String");
        
        try {
            List<String> testList = Arrays.asList("a", "b", "c");
            Object result = handler.handleSerialization(testList, serializationContext);
            assertNotNull(result, "Collection serialization result should not be null");
        } catch (Exception e) {
            throw new RuntimeException("Collection serialization failed", e);
        }
        
        assertEquals(TypeHandler.TypeHandlerPriority.HIGH, handler.getPriority(), "Priority should be HIGH");
    }
    
    public void testMapTypeHandler() {
        MapTypeHandler handler = new MapTypeHandler();
        
        assertTrue(handler.canHandle(Types.Map, Map.class), "Should handle Map");
        assertFalse(handler.canHandle(Types.Collection, List.class), "Should not handle List");
        
        try {
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("key1", "value1");
            testMap.put("key2", 42);
            
            Object result = handler.handleSerialization(testMap, serializationContext);
            assertNotNull(result, "Map serialization result should not be null");
        } catch (Exception e) {
            throw new RuntimeException("Map serialization failed", e);
        }
        
        assertEquals(TypeHandler.TypeHandlerPriority.HIGH, handler.getPriority(), "Priority should be HIGH");
    }
    
    public void testObjectTypeHandler() {
        ObjectTypeHandler handler = new ObjectTypeHandler();
        
        assertTrue(handler.canHandle(Types.Object, Object.class), "Should handle Object");
        assertFalse(handler.canHandle(Types.Integer, Integer.class), "Should not handle primitive");
        
        assertEquals(TypeHandler.TypeHandlerPriority.NORMAL, handler.getPriority(), "Priority should be NORMAL");
    }
    
    public void testGenericTypeHandler() {
        GenericTypeHandler handler = new GenericTypeHandler();
        
        assertTrue(handler.canHandle(Types.GenericType, Object.class), "Should handle GenericType");
        assertTrue(handler.canHandle(Types.AbstractObject, Object.class), "Should handle AbstractObject");
        assertFalse(handler.canHandle(Types.Integer, Integer.class), "Should not handle primitive");
        
        assertEquals(TypeHandler.TypeHandlerPriority.LOW, handler.getPriority(), "Priority should be LOW");
    }
    
    public void testTypeHandlerRegistry() {
        TypeHandlerRegistry testRegistry = new TypeHandlerRegistry();
        
        PrimitiveTypeHandler primitiveHandler = new PrimitiveTypeHandler();
        testRegistry.registerHandler(primitiveHandler);
        
        TypeHandler foundHandler = testRegistry.getHandler(Types.Integer, Integer.class);
        assertNotNull(foundHandler, "Should find handler for Integer");
        assertInstanceOf(PrimitiveTypeHandler.class, foundHandler, "Found handler should be PrimitiveTypeHandler");
        
        TypeHandler notFoundHandler = testRegistry.getHandler(Types.Collection, List.class);
        assertNull(notFoundHandler, "Should not find handler for Collection");
        
        assertEquals(1, testRegistry.getHandlerCount(), "Registry should have 1 handler");
        
        List<TypeHandler> allHandlers = testRegistry.getAllHandlers();
        assertEquals(1, allHandlers.size(), "Should return 1 handler");
        
        testRegistry.getHandler(Types.Integer, Integer.class);
        assertTrue(testRegistry.getCacheSize() > 0, "Cache should contain entry");
        
        testRegistry.clearCache();
        assertEquals(0, testRegistry.getCacheSize(), "Cache should be empty after clear");
    }
    
    public void testTypeHandlerPriority() {
        TypeHandlerRegistry testRegistry = new TypeHandlerRegistry();
        
        testRegistry.registerHandler(new GenericTypeHandler()); // LOW
        testRegistry.registerHandler(new ObjectTypeHandler());  // NORMAL
        testRegistry.registerHandler(new PrimitiveTypeHandler()); // HIGHEST
        
        List<TypeHandler> handlers = testRegistry.getAllHandlers();
        assertEquals(3, handlers.size(), "Should have 3 handlers");
        
        assertInstanceOf(PrimitiveTypeHandler.class, handlers.get(0), "First handler should be PrimitiveTypeHandler");
        assertInstanceOf(GenericTypeHandler.class, handlers.get(2), "Last handler should be GenericTypeHandler");
    }
    
    public void testTypeHandlerFactory() {
        TypeHandlerRegistry defaultRegistry = TypeHandlerFactory.createDefaultRegistry();
        assertNotNull(defaultRegistry, "Default registry should not be null");
        assertTrue(defaultRegistry.getHandlerCount() > 0, "Default registry should have handlers");
        
        TypeHandlerRegistry newRegistry = TypeHandlerFactory.createNewRegistry();
        assertNotNull(newRegistry, "New registry should not be null");
        assertTrue(newRegistry.getHandlerCount() > 0, "New registry should have handlers");
        
        TypeHandlerRegistry emptyRegistry = TypeHandlerFactory.createEmptyRegistry();
        assertNotNull(emptyRegistry, "Empty registry should not be null");
        assertEquals(0, emptyRegistry.getHandlerCount(), "Empty registry should have no handlers");
        
        TypeHandler customHandler = TypeHandlerFactory.createCustomHandler(
            String.class,
            (value, context) -> "custom_" + value,
            (element, targetType, context) -> element.toString().replace("custom_", "")
        );
        
        assertNotNull(customHandler, "Custom handler should not be null");
        assertTrue(customHandler.canHandle(Types.String, String.class), "Custom handler should handle String");
    }
    
    public void testTypesEnumExtensions() {
        assertTrue(Types.isComplexType(Types.Object), "Object should be complex type");
        assertTrue(Types.isComplexType(Types.Collection), "Collection should be complex type");
        assertTrue(Types.isComplexType(Types.Map), "Map should be complex type");
        assertFalse(Types.isComplexType(Types.Integer), "Integer should not be complex type");
        assertFalse(Types.isComplexType(Types.String), "String should not be complex type");
        
        assertTrue(Types.requiresSpecialHandling(Types.AbstractObject), "AbstractObject should require special handling");
        assertTrue(Types.requiresSpecialHandling(Types.GenericType), "GenericType should require special handling");
        assertTrue(Types.requiresSpecialHandling(Types.JSON5Element), "JSON5Element should require special handling");
        assertFalse(Types.requiresSpecialHandling(Types.Integer), "Integer should not require special handling");
        
        assertEquals(Types.TypeCategory.PRIMITIVE, Types.getCategory(Types.Integer), "Integer should be PRIMITIVE");
        assertEquals(Types.TypeCategory.WRAPPER, Types.getCategory(Types.String), "String should be WRAPPER");
        assertEquals(Types.TypeCategory.COLLECTION, Types.getCategory(Types.Collection), "Collection should be COLLECTION");
        assertEquals(Types.TypeCategory.MAP, Types.getCategory(Types.Map), "Map should be MAP");
        assertEquals(Types.TypeCategory.OBJECT, Types.getCategory(Types.Object), "Object should be OBJECT");
        assertEquals(Types.TypeCategory.SPECIAL, Types.getCategory(Types.JSON5Element), "JSON5Element should be SPECIAL");
    }
    
    public void testContextTypeHandlerIntegration() {
        SerializationContext serCtx = new SerializationContext("test", null);
        assertNull(serCtx.getTypeHandlerRegistry(), "Initial registry should be null");
        
        TypeHandlerRegistry testRegistry = TypeHandlerFactory.createDefaultRegistry();
        serCtx.setTypeHandlerRegistry(testRegistry);
        assertNotNull(serCtx.getTypeHandlerRegistry(), "Registry should be set");
        assertEquals(testRegistry, serCtx.getTypeHandlerRegistry(), "Should return same registry");
        
        DeserializationContext deserCtx = new DeserializationContext("test", new JSON5Object(), null);
        assertNull(deserCtx.getTypeHandlerRegistry(), "Initial registry should be null");
        
        deserCtx.setTypeHandlerRegistry(testRegistry);
        assertNotNull(deserCtx.getTypeHandlerRegistry(), "Registry should be set");
        assertEquals(testRegistry, deserCtx.getTypeHandlerRegistry(), "Should return same registry");
    }
    
    public void testIntegrationScenario() {
        TypeHandlerRegistry customRegistry = TypeHandlerFactory.createEmptyRegistry();
        
        customRegistry.registerHandler(new PrimitiveTypeHandler());
        customRegistry.registerHandler(new CollectionTypeHandler());
        customRegistry.registerHandler(new MapTypeHandler());
        customRegistry.registerHandler(new ObjectTypeHandler());
        customRegistry.registerHandler(new GenericTypeHandler());
        
        TypeHandler intHandler = customRegistry.getHandler(Types.Integer, Integer.class);
        assertInstanceOf(PrimitiveTypeHandler.class, intHandler, "Integer should use PrimitiveTypeHandler");
        
        TypeHandler listHandler = customRegistry.getHandler(Types.Collection, List.class);
        assertInstanceOf(CollectionTypeHandler.class, listHandler, "List should use CollectionTypeHandler");
        
        TypeHandler mapHandler = customRegistry.getHandler(Types.Map, Map.class);
        assertInstanceOf(MapTypeHandler.class, mapHandler, "Map should use MapTypeHandler");
        
        TypeHandler objHandler = customRegistry.getHandler(Types.Object, Object.class);
        assertInstanceOf(ObjectTypeHandler.class, objHandler, "Object should use ObjectTypeHandler");
        
        TypeHandler abstractHandler = customRegistry.getHandler(Types.AbstractObject, Object.class);
        assertInstanceOf(GenericTypeHandler.class, abstractHandler, "AbstractObject should use GenericTypeHandler");
    }
    
    public void printSuccessMessage() {
        System.out.println("\n=== TypeHandler 시스템 (4.1 단계) 검증 완료 ===");
        System.out.println("✅ TypeHandler 인터페이스 및 구현체들 정상 동작");
        System.out.println("✅ TypeHandlerRegistry 등록/조회/우선순위 처리 완료");
        System.out.println("✅ TypeHandlerFactory 팩토리 메소드들 정상 동작");
        System.out.println("✅ Types enum 확장 메소드들 정상 동작");
        System.out.println("✅ Context 클래스들의 TypeHandler 통합 완료");
        System.out.println("✅ 통합 시나리오 테스트 통과");
        System.out.println("\n🎉 4.1 단계 TypeHandler 시스템 구축이 성공적으로 완료되었습니다!");
    }
    
    public static void main(String[] args) {
        RefactoringStep41Validator validator = new RefactoringStep41Validator();
        
        try {
            validator.setUp();
            
            validator.testPrimitiveTypeHandler();
            validator.testCollectionTypeHandler();
            validator.testMapTypeHandler();
            validator.testObjectTypeHandler();
            validator.testGenericTypeHandler();
            validator.testTypeHandlerRegistry();
            validator.testTypeHandlerPriority();
            validator.testTypeHandlerFactory();
            validator.testTypesEnumExtensions();
            validator.testContextTypeHandlerIntegration();
            validator.testIntegrationScenario();
            
            validator.printSuccessMessage();
            
        } catch (Exception e) {
            System.err.println("❌ 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
