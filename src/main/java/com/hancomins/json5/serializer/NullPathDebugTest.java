package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

/**
 * TestClassNull 구조 재현 테스트
 */
public class NullPathDebugTest {
    
    @JSON5Type(explicit = true)
    public static class TestClassC {
        @JSON5Value
        private String name = "C";
    }

    @JSON5Type(explicit = true)
    public static class TestClassB {
        @JSON5Value
        private String name = "B";
        @JSON5Value
        private TestClassC testC = new TestClassC();
    }

    @JSON5Type(explicit = true)
    public static class TestClassA {
        @JSON5Value
        private String name = "A";
        @JSON5Value
        private TestClassB testB = new TestClassB();
    }

    @JSON5Type(explicit = true)
    public static class TestClassNull {
        @JSON5Value
        private TestClassA testClassA0 = null;
        @JSON5Value
        private TestClassB testClassB1 = new TestClassB();

        @JSON5Value("testClassB1.testC.name")
        private String classCName = "nameC";

        @JSON5Value("testClassB1.testC.int") 
        private int d = 2000;

        @JSON5Value("testClassB1.testClassA1")
        private TestClassA testClassA1 = null;

        @JSON5Value("testClassB1.testClassA2")
        private TestClassA testClassA2 = new TestClassA();
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TestClassNull 구조 재현 테스트 ===");
            
            TestClassNull testClassNull = new TestClassNull();
            
            // 이 부분이 문제를 일으킬 수 있음
            testClassNull.testClassB1.testC = null;  // null로 설정했는데
            // @JSON5Value("testClassB1.testC.name")와 @JSON5Value("testClassB1.testC.int")가 있음
            
            System.out.println("testClassB1.testC를 null로 설정함");
            
            JSON5Object result = JSON5Serializer.toJSON5Object(testClassNull);
            System.out.println("직렬화 성공: " + result);
            
        } catch (Exception e) {
            System.err.println("❌ 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
