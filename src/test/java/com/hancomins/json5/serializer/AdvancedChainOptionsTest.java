package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 고급 체인 옵션들을 테스트하는 클래스입니다.
 * 
 * SerializationBuilder와 DeserializationBuilder의 고급 기능들을
 * 테스트합니다.
 */
@DisplayName("고급 체인 옵션 테스트")
public class AdvancedChainOptionsTest {
    
    private JSON5Serializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = JSON5Serializer.builder().build();
    }
    
    @Nested
    @DisplayName("SerializationBuilder 고급 옵션 테스트")
    class SerializationBuilderAdvancedOptionsTest {
        
        @Test
        @DisplayName("조건부 설정 테스트")
        void testConditionalConfiguration() {
            // Given
            TestObject obj = new TestObject("test", "secret123", 42);
            
            // When - 민감한 데이터가 있을 때 비밀번호 필드 무시
            JSON5Object result = serializer.forSerialization()
                .when(o -> o instanceof TestObject && ((TestObject) o).getPassword() != null)
                .then(builder -> builder.ignoreFields("password"))
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.has("name"));
            assertFalse(result.has("password")); // 조건에 의해 무시됨
            assertTrue(result.has("age"));
        }
        
        @Test
        @DisplayName("변환 파이프라인 테스트")
        void testTransformationPipeline() {
            // Given
            TestObject obj = new TestObject("test", "secret", 42);
            
            // When - 객체 전처리 후 직렬화
            JSON5Object result = serializer.forSerialization()
                .transform(o -> {
                    if (o instanceof TestObject) {
                        TestObject to = (TestObject) o;
                        return new TestObject(to.getName().toUpperCase(), "[MASKED]", to.getAge());
                    }
                    return o;
                })
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertEquals("TEST", result.getString("name"));
            assertEquals("[MASKED]", result.getString("password"));
        }
        
        @Test
        @DisplayName("필터링 테스트")
        void testFiltering() {
            // Given
            TestObject validObj = new TestObject("valid", "pass", 25);
            TestObject invalidObj = new TestObject("invalid", "pass", 150); // 나이가 너무 많음
            
            // When - 나이가 100 이상이면 null로 변환
            JSON5Object validResult = serializer.forSerialization()
                .filter(o -> o instanceof TestObject && ((TestObject) o).getAge() < 100)
                .serialize(validObj);
            
            JSON5Object invalidResult = serializer.forSerialization()
                .filter(o -> o instanceof TestObject && ((TestObject) o).getAge() < 100)
                .serialize(invalidObj);
            
            // Then
            assertNotNull(validResult);
            assertNull(invalidResult); // 필터링에 의해 null
        }
        
        @Test
        @DisplayName("특정 필드만 유지 테스트")
        void testOnlyFields() {
            // Given
            TestObject obj = new TestObject("test", "secret", 42);
            
            // When - name과 age 필드만 유지
            JSON5Object result = serializer.forSerialization()
                .onlyFields("name", "age")
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.has("name"));
            assertFalse(result.has("password")); // onlyFields에 없으므로 제외
            assertTrue(result.has("age"));
        }
        
        @Test
        @DisplayName("부분 직렬화 옵션 테스트")
        void testPartialSerializationOptions() {
            // Given
            TestObject obj = new TestObject("test", "secret", 42);
            
            // When - 최대 깊이와 문자열 길이 제한
            JSON5Object result = serializer.forSerialization()
                .withPartialOptions(2) // 최대 깊이 2
                .withMaxStringLength(3) // 최대 문자열 길이 3
                .serialize(obj);
            
            // Then
            assertNotNull(result);
            // 문자열이 잘려야 함
            String name = result.getString("name");
            assertTrue(name.length() <= 6); // "tes..." 형태로 잘림
        }
        
        @Test
        @DisplayName("배치 직렬화 테스트")
        void testBatchSerialization() {
            // Given
            List<TestObject> objects = Arrays.asList(
                new TestObject("obj1", "pass1", 20),
                new TestObject("obj2", "pass2", 30),
                new TestObject("obj3", "pass3", 40)
            );
            
            // When
            List<JSON5Object> results = serializer.forSerialization()
                .ignoreFields("password")
                .serializeMultiple(objects);
            
            // Then
            assertEquals(3, results.size());
            for (JSON5Object result : results) {
                assertNotNull(result);
                assertTrue(result.has("name"));
                assertFalse(result.has("password"));
                assertTrue(result.has("age"));
            }
        }
        
        @Test
        @DisplayName("배열로 직렬화 테스트")
        void testSerializeToArray() {
            // Given
            List<TestObject> objects = Arrays.asList(
                new TestObject("obj1", "pass1", 20),
                new TestObject("obj2", "pass2", 30)
            );
            
            // When
            JSON5Array result = serializer.forSerialization()
                .withWritingOptions(WritingOptions.json5Pretty())
                .serializeToArray(objects);
            
            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(WritingOptions.json5Pretty(), result.getWritingOptions());
        }
    }
    
    @Nested
    @DisplayName("DeserializationBuilder 고급 옵션 테스트")
    class DeserializationBuilderAdvancedOptionsTest {
        
        @Test
        @DisplayName("조건부 설정 테스트")
        void testConditionalConfiguration() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("version", "2.0");
            json.put("name", "test");
            json.put("password", "secret");
            json.put("age", 42);
            
            // When - 버전이 있으면 엄격한 검증 활성화
            TestObject result = serializer.forDeserialization()
                .when(j -> j.has("version"))
                .then(builder -> builder.enableStrictValidation())
                .deserialize(json, TestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
        }
        
        @Test
        @DisplayName("유효성 검사 테스트")
        void testValidation() {
            // Given
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("password", "weak"); // 약한 비밀번호
            json.put("age", 42);
            
            // When & Then - 비밀번호 강도 검사
            assertThrows(JSON5SerializerException.class, () -> {
                serializer.forDeserialization()
                    .validateWith(obj -> {
                        if (obj instanceof TestObject) {
                            TestObject to = (TestObject) obj;
                            return to.getPassword() != null && to.getPassword().length() >= 8;
                        }
                        return true;
                    })
                    .deserialize(json, TestObject.class);
            });
        }
        
        @Test
        @DisplayName("필드 기본값 테스트")
        void testFieldDefaults() {
            // Given - password 필드가 없는 JSON
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("age", 42);
            // password 필드 누락
            
            // When
            TestObject result = serializer.forDeserialization()
                .withFieldDefault("password", "default_password")
                .deserialize(json, TestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals("test", result.getName());
            assertEquals("default_password", result.getPassword()); // 기본값 적용
            assertEquals(42, result.getAge());
        }
        
        @Test
        @DisplayName("안전한 모드 테스트")
        void testSafeMode() {
            // Given - 잘못된 타입의 JSON
            JSON5Object json = new JSON5Object();
            json.put("name", "test");
            json.put("password", "secret");
            json.put("age", "not_a_number"); // 잘못된 타입
            
            // When
            TestObject result = serializer.forDeserialization()
                .enableSafeMode()
                .withDefaultValue(new TestObject("default", "default", 0))
                .deserialize(json, TestObject.class);
            
            // Then - 오류가 발생해도 기본값 반환
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("배치 역직렬화 테스트")
        void testBatchDeserialization() {
            // Given
            JSON5Object json1 = new JSON5Object();
            json1.put("name", "obj1");
            json1.put("password", "pass1");
            json1.put("age", 20);
            
            JSON5Object json2 = new JSON5Object();
            json2.put("name", "obj2");
            json2.put("password", "pass2");
            json2.put("age", 30);
            
            List<JSON5Object> jsonObjects = Arrays.asList(json1, json2);
            
            // When
            List<TestObject> results = serializer.forDeserialization()
                .ignoreErrors()
                .deserializeMultiple(jsonObjects, TestObject.class);
            
            // Then
            assertEquals(2, results.size());
            assertEquals("obj1", results.get(0).getName());
            assertEquals("obj2", results.get(1).getName());
        }
        
        @Test
        @DisplayName("배열에서 역직렬화 테스트")
        void testDeserializeFromArray() {
            // Given
            JSON5Array jsonArray = new JSON5Array();
            
            JSON5Object json1 = new JSON5Object();
            json1.put("name", "obj1");
            json1.put("password", "pass1");
            json1.put("age", 20);
            jsonArray.put(json1);
            
            JSON5Object json2 = new JSON5Object();
            json2.put("name", "obj2");
            json2.put("password", "pass2");
            json2.put("age", 30);
            jsonArray.put(json2);
            
            // When
            List<TestObject> results = serializer.forDeserialization()
                .deserializeFromArray(jsonArray, TestObject.class);
            
            // Then
            assertEquals(2, results.size());
            assertEquals("obj1", results.get(0).getName());
            assertEquals("obj2", results.get(1).getName());
        }
    }
    
    // 테스트용 클래스
    @JSON5Type
    public static class TestObject {
        @JSON5Value
        private String name;
        
        @JSON5Value
        private String password;
        
        @JSON5Value
        private int age;
        
        public TestObject() {}
        
        public TestObject(String name, String password, int age) {
            this.name = name;
            this.password = password;
            this.age = age;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "TestObject{" +
                    "name='" + name + '\'' +
                    ", password='" + password + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
