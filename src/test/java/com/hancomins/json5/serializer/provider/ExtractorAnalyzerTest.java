package com.hancomins.json5.serializer.provider;

import com.hancomins.json5.serializer.JSON5ValueExtractor;
import com.hancomins.json5.serializer.JSON5SerializerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("추출자 분석기 테스트")
class ExtractorAnalyzerTest {
    
    private final ExtractorAnalyzer analyzer = new ExtractorAnalyzer();
    
    @Test
    @DisplayName("정상적인 추출자 메서드를 찾아야 함")
    void shouldFindValidExtractorMethod() {
        class TestClass {
            @JSON5ValueExtractor
            public String getValue() {
                return "test";
            }
        }
        
        ExtractorInfo info = analyzer.analyzeValueExtractor(TestClass.class);
        assertNotNull(info);
        assertEquals("getValue", info.getMethod().getName());
        assertEquals(String.class, info.getReturnType());
    }
    
    @Test
    @DisplayName("void 반환 타입은 거부해야 함")
    void shouldRejectVoidReturnType() {
        class TestClass {
            @JSON5ValueExtractor
            public void getValue() {
                // void return
            }
        }
        
        assertThrows(JSON5SerializerException.class, () -> 
            analyzer.analyzeValueExtractor(TestClass.class));
    }
    
    @Test
    @DisplayName("파라미터가 있는 메서드는 거부해야 함")
    void shouldRejectMethodsWithParameters() {
        class TestClass {
            @JSON5ValueExtractor
            public String getValue(String param) {
                return param;
            }
        }
        
        assertThrows(JSON5SerializerException.class, () -> 
            analyzer.analyzeValueExtractor(TestClass.class));
    }
    
    @Test
    @DisplayName("복수의 추출자가 있으면 거부해야 함")
    void shouldRejectMultipleExtractors() {
        class TestClass {
            @JSON5ValueExtractor
            public String getValue1() {
                return "test1";
            }
            
            @JSON5ValueExtractor
            public String getValue2() {
                return "test2";
            }
        }
        
        assertThrows(JSON5SerializerException.class, () -> 
            analyzer.analyzeValueExtractor(TestClass.class));
    }
    
    @Test
    @DisplayName("추출자가 없으면 거부해야 함")
    void shouldRejectNoExtractor() {
        class TestClass {
            public String getValue() {
                return "test";
            }
        }
        
        assertThrows(JSON5SerializerException.class, () -> 
            analyzer.analyzeValueExtractor(TestClass.class));
    }
    
    @Test
    @DisplayName("지원하는 반환 타입들을 허용해야 함")
    void shouldAllowSupportedReturnTypes() {
        class StringExtractor {
            @JSON5ValueExtractor
            public String getValue() { return "test"; }
        }
        
        class IntExtractor {
            @JSON5ValueExtractor
            public int getValue() { return 42; }
        }
        
        class LongExtractor {
            @JSON5ValueExtractor
            public Long getValue() { return 123L; }
        }
        
        class BooleanExtractor {
            @JSON5ValueExtractor
            public boolean getValue() { return true; }
        }
        
        assertDoesNotThrow(() -> analyzer.analyzeValueExtractor(StringExtractor.class));
        assertDoesNotThrow(() -> analyzer.analyzeValueExtractor(IntExtractor.class));
        assertDoesNotThrow(() -> analyzer.analyzeValueExtractor(LongExtractor.class));
        assertDoesNotThrow(() -> analyzer.analyzeValueExtractor(BooleanExtractor.class));
    }
}
