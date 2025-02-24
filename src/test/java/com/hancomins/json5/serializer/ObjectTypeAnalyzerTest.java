package com.hancomins.json5.serializer;

import org.junit.jupiter.api.Test;

class ObjectTypeAnalyzerTest {


     @JSON5Type(explicit = true)
    public static class User {
        @JSON5Value
        private String name;
        @JSON5Value
        private int age;
        @JSON5Value("detail.isAdult")
        private boolean isAdult;
        @JSON5Value("detail.isAdult")
        private double height;
        @JSON5Value("detail.weight")
        private float weight;
    }

    @Test
    public void test() {
            User user = new User();
        user.name = "John";
        user.age = 30;
        user.isAdult = true;
        user.height = 180.5;
        user.weight = 70.5f;

        Class<?> clazz = User.class;
        TypeSchema typeSchema = TypeSchemaMap.getInstance().getTypeInfo(clazz);
        SchemaObjectNode schemaObjectNode = typeSchema.getSchemaObjectNode();

        JSON5Serializer.toJSON5Object(user);




    }

}