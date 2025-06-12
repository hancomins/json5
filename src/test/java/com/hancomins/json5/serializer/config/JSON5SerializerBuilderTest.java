package com.hancomins.json5.serializer.config;

import com.hancomins.json5.serializer.*;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON5SerializerBuilder 클래스의 테스트입니다.
 * Builder 패턴을 통한 JSON5Serializer 구성을 검증합니다.
 */
@DisplayName("JSON5SerializerBuilder 테스트")
public class JSON5SerializerBuilderTest {

    @Nested
    @DisplayName("기본 Builder 동작 테스트")
    class BasicBuilderTest {

        @Test
        @DisplayName("기본 Builder 생성 및 빌드 테스트")
        void shouldCreateDefaultSerializerWithBuilder() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder().build();

            // Then
            assertNotNull(serializer);
            assertNotNull(serializer.getConfiguration());
        }

        @Test
        @DisplayName("설정만 빌드하기 테스트")
        void shouldBuildConfigurationOnly() {
            // When
            SerializerConfiguration config = JSON5Serializer.builder()
                .ignoreUnknownProperties()
                .enableSchemaCache()
                .buildConfiguration();

            // Then
            assertNotNull(config);
            assertTrue(config.isIgnoreUnknownProperties());
            assertTrue(config.isEnableSchemaCache());
        }
    }

    @Nested
    @DisplayName("환경별 설정 테스트")
    class EnvironmentConfigurationTest {

        @Test
        @DisplayName("개발 환경 설정 Builder 테스트")
        void shouldConfigureForDevelopment() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .forDevelopment()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.DEVELOPMENT, config.getEnvironmentProfile());
            assertTrue(config.isEnableStrictTypeChecking());
            assertTrue(config.isEnablePerformanceMonitoring());
            assertTrue(config.isEnableCircularReferenceDetection());
        }

        @Test
        @DisplayName("운영 환경 설정 Builder 테스트")
        void shouldConfigureForProduction() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .forProduction()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.PRODUCTION, config.getEnvironmentProfile());
            assertTrue(config.isEnableSchemaCache());
            assertTrue(config.isEnableLazyLoading());
            assertTrue(config.isMaskSensitiveFields());
            assertEquals(5000, config.getSerializationTimeoutMs());
        }

        @Test
        @DisplayName("테스트 환경 설정 Builder 테스트")
        void shouldConfigureForTesting() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .forTesting()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.TEST, config.getEnvironmentProfile());
            assertTrue(config.isIncludeNullValues());
            assertTrue(config.isPreserveFieldOrder());
            assertTrue(config.isIgnoreUnknownProperties());
        }
    }

    @Nested
    @DisplayName("기본 동작 설정 테스트")
    class BasicBehaviorTest {

        @Test
        @DisplayName("기본 동작 설정 Builder 테스트")
        void shouldConfigureBasicBehavior() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .ignoreUnknownProperties()
                .failOnEmptyBeans()
                .useFieldVisibility()
                .includeNullValues()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertTrue(config.isIgnoreUnknownProperties());
            assertTrue(config.isFailOnEmptyBeans());
            assertTrue(config.isUseFieldVisibility());
            assertTrue(config.isIncludeNullValues());
        }
    }

    @Nested
    @DisplayName("보안 설정 테스트")
    class SecurityConfigurationTest {

        @Test
        @DisplayName("보안 설정 Builder 테스트")
        void shouldConfigureSecurityFeatures() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .maskSensitiveFields()
                .addSensitiveField("password", "secret", "token")
                .enableFieldNameValidation()
                .withMaxStringLength(5000)
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertTrue(config.isMaskSensitiveFields());
            assertTrue(config.isSensitiveField("password"));
            assertTrue(config.isSensitiveField("secret"));
            assertTrue(config.isSensitiveField("token"));
            assertTrue(config.isEnableFieldNameValidation());
            assertEquals(5000, config.getMaxStringLength());
        }
    }

    @Nested
    @DisplayName("편의 메소드 테스트")
    class ConvenienceMethodTest {

        @Test
        @DisplayName("모든 보안 기능 활성화 테스트")
        void shouldEnableAllSecurityFeatures() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .enableAllSecurityFeatures()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertTrue(config.isMaskSensitiveFields());
            assertTrue(config.isEnableFieldNameValidation());
            assertTrue(config.isSensitiveField("password"));
            assertTrue(config.isSensitiveField("secret"));
            assertTrue(config.isSensitiveField("token"));
            assertEquals(10000, config.getMaxStringLength());
        }

        @Test
        @DisplayName("모든 성능 최적화 활성화 테스트")
        void shouldEnableAllPerformanceOptimizations() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .enableAllPerformanceOptimizations()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertTrue(config.isEnableSchemaCache());
            assertTrue(config.isEnableLazyLoading());
            assertEquals(32, config.getMaxDepth());
            assertEquals(10000, config.getSerializationTimeoutMs());
        }

        @Test
        @DisplayName("모든 개발 도구 활성화 테스트")
        void shouldEnableAllDevelopmentTools() {
            // When
            JSON5Serializer serializer = JSON5Serializer.builder()
                .enableAllDevelopmentTools()
                .build();

            SerializerConfiguration config = serializer.getConfiguration();

            // Then
            assertTrue(config.isEnableStrictTypeChecking());
            assertTrue(config.isEnablePerformanceMonitoring());
            assertTrue(config.isEnableCircularReferenceDetection());
            assertTrue(config.isPreserveFieldOrder());
            assertNotNull(config.getDefaultWritingOptions());
        }
    }
}
