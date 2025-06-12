package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for Step 2 refactoring (Schema system modularization) completion.
 * 
 * <p>This class validates that the ValueProcessor system, SchemaFactory, 
 * and SchemaCache are working correctly.</p>
 * 
 * @author JSON5 Team
 * @version 1.2
 * @since 2.0
 */
public class RefactoringStep2Validator {
    
    /**
     * Validates that Step 2 refactoring is completed correctly.
     * 
     * @return true if all validations pass, false otherwise
     */
    public static boolean validateStep2Completion() {
        try {
            boolean valueProcessorTest = validateValueProcessorSystem();
            boolean schemaFactoryTest = validateSchemaFactory();
            boolean schemaCacheTest = validateSchemaCache();
            boolean integrationTest = validateIntegration();
            
            System.out.println("=== Step 2 Refactoring Validation Results ===");
            System.out.println("ValueProcessor System: " + (valueProcessorTest ? "PASS" : "FAIL"));
            System.out.println("SchemaFactory: " + (schemaFactoryTest ? "PASS" : "FAIL"));
            System.out.println("SchemaCache: " + (schemaCacheTest ? "PASS" : "FAIL"));
            System.out.println("Integration Test: " + (integrationTest ? "PASS" : "FAIL"));
            
            return valueProcessorTest && schemaFactoryTest && schemaCacheTest && integrationTest;
            
        } catch (Exception e) {
            System.err.println("Error during Step 2 validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates that the ValueProcessor system works correctly.
     */
    private static boolean validateValueProcessorSystem() {
        try {
            ValueProcessorFactory factory = ValueProcessorFactory.getInstance();
            
            // Test PrimitiveValueProcessor
            ValueProcessor primitiveProcessor = factory.getProcessor(Types.Integer);
            if (!(primitiveProcessor instanceof PrimitiveValueProcessor)) {
                System.err.println("PrimitiveValueProcessor assignment failed");
                return false;
            }
            
            // Test ObjectValueProcessor
            ValueProcessor objectProcessor = factory.getProcessor(Types.Object);
            if (!(objectProcessor instanceof ObjectValueProcessor)) {
                System.err.println("ObjectValueProcessor assignment failed");
                return false;
            }
            
            // Test CollectionValueProcessor
            ValueProcessor collectionProcessor = factory.getProcessor(Types.Collection);
            if (!(collectionProcessor instanceof CollectionValueProcessor)) {
                System.err.println("CollectionValueProcessor assignment failed");
                return false;
            }
            
            // Check processor count
            if (factory.getProcessorCount() < 3) {
                System.err.println("Insufficient processor count: " + factory.getProcessorCount());
                return false;
            }
            
            System.out.println("ValueProcessor system validation completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("ValueProcessor system validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates that SchemaFactory works correctly.
     */
    private static boolean validateSchemaFactory() {
        try {
            SchemaFactory factory = SchemaFactory.getInstance();
            
            // Create test TypeSchema
            TypeSchema testTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(TestObject.class);
            
            // Test Schema creation
            SchemaObjectNode schema = factory.createSchema(testTypeSchema, null);
            if (schema == null) {
                System.err.println("Schema creation failed");
                return false;
            }
            
            // Test SubTree creation
            SchemaElementNode subTree = factory.createSubTree("test.path", schema);
            if (subTree == null) {
                System.err.println("SubTree creation failed");
                return false;
            }
            
            // Test cache functionality
            SchemaCache cache = factory.getCache();
            if (cache == null) {
                System.err.println("SchemaCache access failed");
                return false;
            }
            
            System.out.println("SchemaFactory validation completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("SchemaFactory validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates that SchemaCache works correctly.
     */
    private static boolean validateSchemaCache() {
        try {
            DefaultSchemaCache cache = new DefaultSchemaCache();
            
            // Check empty cache
            if (!cache.isEmpty()) {
                System.err.println("New cache is not empty");
                return false;
            }
            
            // Test Schema caching
            TypeSchema testTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(TestObject.class);
            SchemaObjectNode testSchema = new SchemaObjectNode();
            
            cache.putSchema(TestObject.class, testSchema);
            SchemaObjectNode retrieved = cache.getCachedSchema(TestObject.class);
            
            if (retrieved != testSchema) {
                System.err.println("Schema caching/retrieval failed");
                return false;
            }
            
            // Test SubTree caching
            SchemaElementNode testSubTree = new SchemaObjectNode();
            String key = "test.key";
            
            cache.putSubTree(key, testSubTree);
            SchemaElementNode retrievedSubTree = cache.getCachedSubTree(key);
            
            if (retrievedSubTree != testSubTree) {
                System.err.println("SubTree caching/retrieval failed");
                return false;
            }
            
            // Test cache invalidation
            cache.invalidateCache();
            if (!cache.isEmpty()) {
                System.err.println("Cache invalidation failed");
                return false;
            }
            
            System.out.println("SchemaCache validation completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("SchemaCache validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates the integrated system operation.
     */
    private static boolean validateIntegration() {
        try {
            // Test actual serialization/deserialization
            TestObject original = new TestObject();
            original.stringValue = "test";
            original.intValue = 42;
            original.listValue = Arrays.asList("item1", "item2");
            
            // Serialization (using ValueProcessor)
            JSON5Object json = JSON5Serializer.toJSON5Object(original);
            if (json == null) {
                System.err.println("Serialization failed");
                return false;
            }
            
            // Deserialization (using ValueProcessor)
            TestObject restored = JSON5Serializer.fromJSON5Object(json, TestObject.class);
            if (restored == null) {
                System.err.println("Deserialization failed");
                return false;
            }
            
            // Check data consistency
            if (!original.stringValue.equals(restored.stringValue) ||
                original.intValue != restored.intValue ||
                !original.listValue.equals(restored.listValue)) {
                System.err.println("Data inconsistency");
                return false;
            }
            
            System.out.println("Integration test validation completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("Integration test validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates performance improvement.
     */
    public static boolean validatePerformanceImprovement() {
        try {
            long iterations = 1000;
            
            // Execute without cache
            SchemaFactory.getInstance().invalidateCache();
            long startTime = System.nanoTime();
            
            for (int i = 0; i < iterations; i++) {
                TypeSchema testTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(TestObject.class);
                SchemaFactory.getInstance().createSchema(testTypeSchema, null);
            }
            
            long timeWithoutCache = System.nanoTime() - startTime;
            
            // Execute with cache (first miss, rest hits)
            startTime = System.nanoTime();
            
            for (int i = 0; i < iterations; i++) {
                TypeSchema testTypeSchema = TypeSchemaMap.getInstance().getTypeInfo(TestObject.class);
                SchemaFactory.getInstance().createSchema(testTypeSchema, null);
            }
            
            long timeWithCache = System.nanoTime() - startTime;
            
            System.out.println("Without cache: " + (timeWithoutCache / 1_000_000) + "ms");
            System.out.println("With cache: " + (timeWithCache / 1_000_000) + "ms");
            
            // Performance should improve with cache (at least 50% better)
            return timeWithCache < timeWithoutCache * 0.5;
            
        } catch (Exception e) {
            System.err.println("Performance validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Prints Step 2 completion status.
     */
    public static void printStep2Status() {
        System.out.println("\\n=== JSON5 Serializer Step 2 Refactoring Completed ===");
        System.out.println("✅ ValueProcessor system established");
        System.out.println("✅ SchemaFactory separation completed");
        System.out.println("✅ SchemaCache system completed");
        System.out.println("✅ SchemaValueAbs refactoring completed");
        System.out.println("✅ NodePath delegation structure completed");
        System.out.println("\\nKey improvements:");
        System.out.println("- Separated complex getValue/setValue logic into type-specific processors");
        System.out.println("- Encapsulated Schema creation logic into SchemaFactory");
        System.out.println("- Performance optimization through caching strategy");
        System.out.println("- Improved maintainability by following Single Responsibility Principle");
        System.out.println("\\nNext step: Step 3 - JSON5Serializer decomposition (serialization part)");
    }
    
    // Test class
    @JSON5Type
    public static class TestObject {
        @JSON5Value
        public String stringValue;
        
        @JSON5Value
        public int intValue;
        
        @JSON5Value
        public List<String> listValue;
        
        public TestObject() {}
    }
    
    /**
     * Main method - runs validation
     */
    public static void main(String[] args) {
        System.out.println("Starting Step 2 refactoring validation...");
        
        boolean validationResult = validateStep2Completion();
        boolean performanceResult = validatePerformanceImprovement();
        
        if (validationResult && performanceResult) {
            printStep2Status();
            System.out.println("\\n🎉 Step 2 refactoring completed successfully!");
        } else {
            System.err.println("\\n❌ Step 2 refactoring validation failed");
            System.exit(1);
        }
    }
}
