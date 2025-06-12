package com.hancomins.json5.serializer.config;

import com.hancomins.json5.options.WritingOptions;
import com.hancomins.json5.serializer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SerializerConfiguration 클래스의 테스트입니다.
 * 다양한 설정 조합과 환경별 설정을 검증합니다.
 */
@DisplayName("SerializerConfiguration 테스트")
public class SerializerConfigurationTest {

    @Nested
    @DisplayName("기본 설정 테스트")
    class DefaultConfigurationTest {

        @Test
        @DisplayName("기본 설정 생성 테스트")
        void shouldCreateDefaultConfiguration() {
            // When
            SerializerConfiguration config = SerializerConfiguration.getDefault();

            // Then
            assertNotNull(config);
            assertFalse(config.isIgnoreUnknownProperties());
            assertFalse(config.isFailOnEmptyBeans());
            assertFalse(config.isUseFieldVisibility());
            assertFalse(config.isEnableSchemaCache());
            assertFalse(config.isIncludeNullValues());
            assertEquals(64, config.getMaxDepth());
            assertEquals(30000, config.getSerializationTimeoutMs());
            assertEquals(SerializerConfiguration.EnvironmentProfile.CUSTOM, config.getEnvironmentProfile());
        }

        @Test
        @DisplayName("기본 TypeHandlerRegistry 설정 테스트")
        void shouldHaveDefaultTypeHandlerRegistry() {
            // When
            SerializerConfiguration config = SerializerConfiguration.getDefault();

            // Then
            assertNotNull(config.getTypeHandlerRegistry());
        }

        @Test
        @DisplayName("기본 WritingOptions 설정 테스트")
        void shouldHaveDefaultWritingOptions() {
            // When
            SerializerConfiguration config = SerializerConfiguration.getDefault();

            // Then
            assertNotNull(config.getDefaultWritingOptions());
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("Builder로 커스텀 설정 생성 테스트")
        void shouldCreateCustomConfigurationWithBuilder() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .ignoreUnknownProperties()
                .failOnEmptyBeans()
                .useFieldVisibility()
                .includeNullValues()
                .enableSchemaCache()
                .withMaxDepth(10)
                .withSerializationTimeout(5000)
                .build();

            // Then
            assertTrue(config.isIgnoreUnknownProperties());
            assertTrue(config.isFailOnEmptyBeans());
            assertTrue(config.isUseFieldVisibility());
            assertTrue(config.isIncludeNullValues());
            assertTrue(config.isEnableSchemaCache());
            assertEquals(10, config.getMaxDepth());
            assertEquals(5000, config.getSerializationTimeoutMs());
        }

        @Test
        @DisplayName("WritingOptions 설정 테스트")
        void shouldSetWritingOptions() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .withDefaultWritingOptions(WritingOptions.json5Pretty())
                .build();

            // Then
            assertNotNull(config.getDefaultWritingOptions());
        }

        @Test
        @DisplayName("민감한 필드 설정 테스트")
        void shouldConfigureSensitiveFields() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .maskSensitiveFields()
                .addSensitiveField("password", "secret", "token")
                .build();

            // Then
            assertTrue(config.isMaskSensitiveFields());
            assertTrue(config.isSensitiveField("password"));
            assertTrue(config.isSensitiveField("secret"));
            assertTrue(config.isSensitiveField("token"));
            assertFalse(config.isSensitiveField("username"));
        }

        @Test
        @DisplayName("순환 참조 감지 설정 테스트")
        void shouldConfigureCircularReferenceDetection() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .enableCircularReferenceDetection()
                .build();

            // Then
            assertTrue(config.isEnableCircularReferenceDetection());
        }
    }

    @Nested
    @DisplayName("환경별 설정 테스트")
    class EnvironmentConfigurationTest {

        @Test
        @DisplayName("개발 환경 설정 테스트")
        void shouldCreateDevelopmentConfiguration() {
            // When
            SerializerConfiguration config = SerializerConfiguration.forDevelopment();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.DEVELOPMENT, config.getEnvironmentProfile());
            assertTrue(config.isEnableStrictTypeChecking());
            assertTrue(config.isEnablePerformanceMonitoring());
            assertTrue(config.isEnableCircularReferenceDetection());
            assertNotNull(config.getDefaultWritingOptions());
        }

        @Test
        @DisplayName("운영 환경 설정 테스트")
        void shouldCreateProductionConfiguration() {
            // When
            SerializerConfiguration config = SerializerConfiguration.forProduction();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.PRODUCTION, config.getEnvironmentProfile());
            assertTrue(config.isEnableSchemaCache());
            assertTrue(config.isEnableLazyLoading());
            assertEquals(5000, config.getSerializationTimeoutMs());
            assertTrue(config.isMaskSensitiveFields());
        }

        @Test
        @DisplayName("테스트 환경 설정 테스트")
        void shouldCreateTestConfiguration() {
            // When
            SerializerConfiguration config = SerializerConfiguration.forTesting();

            // Then
            assertEquals(SerializerConfiguration.EnvironmentProfile.TEST, config.getEnvironmentProfile());
            assertTrue(config.isIncludeNullValues());
            assertTrue(config.isPreserveFieldOrder());
            assertTrue(config.isIgnoreUnknownProperties());
        }
    }

    @Nested
    @DisplayName("편의 메소드 테스트")
    class ConvenienceMethodTest {

        @Test
        @DisplayName("모든 보안 기능 활성화 테스트")
        void shouldEnableAllSecurityFeatures() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .enableAllSecurityFeatures()
                .build();

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
            SerializerConfiguration config = SerializerConfiguration.builder()
                .enableAllPerformanceOptimizations()
                .build();

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
            SerializerConfiguration config = SerializerConfiguration.builder()
                .enableAllDevelopmentTools()
                .build();

            // Then
            assertTrue(config.isEnableStrictTypeChecking());
            assertTrue(config.isEnablePerformanceMonitoring());
            assertTrue(config.isEnableCircularReferenceDetection());
            assertTrue(config.isPreserveFieldOrder());
            assertNotNull(config.getDefaultWritingOptions());
        }
    }

    @Nested
    @DisplayName("toBuilder() 테스트")
    class ToBuilderTest {

        @Test
        @DisplayName("기존 설정에서 새 Builder 생성 테스트")
        void shouldCreateBuilderFromExistingConfiguration() {
            // Given
            SerializerConfiguration originalConfig = SerializerConfiguration.builder()
                .ignoreUnknownProperties()
                .enableSchemaCache()
                .withMaxDepth(20)
                .build();

            // When
            SerializerConfiguration newConfig = originalConfig.toBuilder()
                .failOnEmptyBeans()
                .withMaxDepth(30)
                .build();

            // Then
            assertTrue(newConfig.isIgnoreUnknownProperties()); // 기존 설정 유지
            assertTrue(newConfig.isEnableSchemaCache());        // 기존 설정 유지
            assertTrue(newConfig.isFailOnEmptyBeans());         // 새로 추가된 설정
            assertEquals(30, newConfig.getMaxDepth());          // 변경된 설정
        }
    }

    @Nested
    @DisplayName("검증 테스트")
    class ValidationTest {

        @Test
        @DisplayName("잘못된 maxDepth 설정 시 예외 발생 테스트")
        void shouldThrowExceptionForInvalidMaxDepth() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SerializerConfiguration.builder()
                    .withMaxDepth(-1)
                    .build();
            });
        }

        @Test
        @DisplayName("잘못된 timeout 설정 시 예외 발생 테스트")
        void shouldThrowExceptionForInvalidTimeout() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SerializerConfiguration.builder()
                    .withSerializationTimeout(-1)
                    .build();
            });
        }

        @Test
        @DisplayName("잘못된 maxStringLength 설정 시 예외 발생 테스트")
        void shouldThrowExceptionForInvalidMaxStringLength() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                SerializerConfiguration.builder()
                    .withMaxStringLength(-1)
                    .build();
            });
        }
    }

    @Nested
    @DisplayName("민감한 필드 테스트")
    class SensitiveFieldTest {

        @Test
        @DisplayName("민감한 필드 집합 불변성 테스트")
        void shouldReturnImmutableSensitiveFieldsSet() {
            // Given
            SerializerConfiguration config = SerializerConfiguration.builder()
                .addSensitiveField("password")
                .build();

            // When
            Set<String> sensitiveFields = config.getSensitiveFields();

            // Then
            assertThrows(UnsupportedOperationException.class, () -> {
                sensitiveFields.add("newField");
            });
        }
    }

    @Nested
    @DisplayName("환경 프로파일별 기본값 적용 테스트")
    class EnvironmentDefaultsTest {

        @Test
        @DisplayName("개발 환경에서 Pretty Print 기본 적용 테스트")
        void shouldApplyPrettyPrintInDevelopment() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .withEnvironmentProfile(SerializerConfiguration.EnvironmentProfile.DEVELOPMENT)
                .build();

            // Then
            assertNotNull(config.getDefaultWritingOptions());
            // Pretty print 옵션이 적용되었는지 간접적으로 확인
        }

        @Test
        @DisplayName("운영 환경에서 성능 최적화 기본 적용 테스트")
        void shouldApplyPerformanceOptimizationInProduction() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .withEnvironmentProfile(SerializerConfiguration.EnvironmentProfile.PRODUCTION)
                .build();

            // Then
            assertTrue(config.isEnableSchemaCache());
            assertEquals(5000, config.getSerializationTimeoutMs());
        }

        @Test
        @DisplayName("테스트 환경에서 일관성 보장 설정 기본 적용 테스트")
        void shouldApplyConsistencySettingsInTest() {
            // When
            SerializerConfiguration config = SerializerConfiguration.builder()
                .withEnvironmentProfile(SerializerConfiguration.EnvironmentProfile.TEST)
                .build();

            // Then
            assertTrue(config.isPreserveFieldOrder());
        }
    }
}
