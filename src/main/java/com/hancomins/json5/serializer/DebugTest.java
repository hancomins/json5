package com.hancomins.json5.serializer;

import com.hancomins.json5.*;

/**
 * 간단한 디버깅 클래스 - 테스트 실패 원인 분석
 */
public class DebugTest {
    
    @JSON5Type(explicit = true)
    public static class SimpleClass {
        @JSON5Value
        private String name = "test";
    }
    
    @JSON5Type(explicit = true)
    public static class NullTestClass {
        @JSON5Value
        private SimpleClass child = null;
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 간단한 null 테스트 시작 ===");
            
            // 1. 기본 테스트
            SimpleClass simple = new SimpleClass();
            JSON5Object result1 = JSON5Serializer.toJSON5Object(simple);
            System.out.println("1. 기본 테스트 성공: " + result1);
            
            // 2. null 필드 테스트
            NullTestClass nullTest = new NullTestClass();
            JSON5Object result2 = JSON5Serializer.toJSON5Object(nullTest);
            System.out.println("2. null 필드 테스트 성공: " + result2);
            
            System.out.println("=== 모든 테스트 성공 ===");
            
        } catch (Exception e) {
            System.err.println("❌ 테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
