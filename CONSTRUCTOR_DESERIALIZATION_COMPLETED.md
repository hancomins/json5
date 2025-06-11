# 생성자 기반 역직렬화 (Constructor-based Deserialization) 구현 완료

## 📋 구현 개요

JSON5 Serializer에 Jackson의 `@JsonCreator`와 `@JsonProperty`와 유사한 생성자 기반 역직렬화 기능을 성공적으로 구현했습니다.

## ✅ 구현된 기능

### 1. 어노테이션 시스템
- **@JSON5Creator**: 역직렬화에 사용할 생성자 지정 및 우선순위 설정
- **@JSON5Property**: 생성자 파라미터와 JSON 키 매핑, 중첩 경로 지원
- **MissingValueStrategy**: 누락된 값에 대한 처리 전략 (기본값/예외)

### 2. 핵심 컴포넌트
- **JSON5PathExtractor**: 중첩 경로 값 추출 (`"user.profile.email"` 형태 지원)
- **ConstructorAnalyzer**: 생성자 분석 및 우선순위 정렬
- **ParameterValueResolver**: 파라미터 타입 변환 및 기본값 처리
- **ConstructorDeserializer**: 생성자 기반 역직렬화 수행

### 3. DeserializationEngine 통합
- 기존 JSON5Serializer와 완전 통합
- 생성자 기반 → 전략 패턴 → 기본 방식 순으로 역직렬화 시도
- 기존 기능과의 호환성 유지

## 🚀 사용 예제

### 기본 사용법
```java
@JSON5Type
public class User {
    private final String name;
    private final int age;
    
    @JSON5Creator
    public User(@JSON5Property("name") String name,
               @JSON5Property("age") int age) {
        this.name = name;
        this.age = age;
    }
    
    // getters...
}

// 사용
JSON5Object json = new JSON5Object();
json.put("name", "John Doe");
json.put("age", 30);

User user = JSON5Serializer.getInstance().deserialize(json, User.class);
```

### 중첩 경로 접근
```java
@JSON5Creator
public User(@JSON5Property("name") String name,
           @JSON5Property("profile.email") String email,
           @JSON5Property("profile.department") String department) {
    // 생성자 구현
}

// JSON 구조:
// {
//   "name": "John",
//   "profile": {
//     "email": "john@example.com",
//     "department": "Engineering"
//   }
// }
```

### 우선순위 및 필수 필드
```java
@JSON5Creator(priority = 1)
public User(@JSON5Property(value = "id", required = true) String id,
           @JSON5Property(value = "name", onMissing = MissingValueStrategy.EXCEPTION) String name) {
    // 높은 우선순위, 필수 필드
}
```

## 🧪 테스트 현황

### 구현된 테스트
- **JSON5PathExtractorTest**: 경로 추출 기능 테스트 (✅ 통과)
- **ConstructorAnalyzerTest**: 생성자 분석 기능 테스트 (✅ 통과)
- **ParameterValueResolverTest**: 파라미터 값 변환 테스트 (✅ 통과)
- **ConstructorDeserializationIntegrationTest**: 통합 테스트 (✅ 통과)

### 테스트 커버리지
- 단순 경로 추출
- 중첩 경로 추출 (3단계 깊이)
- 배열 인덱스 접근
- 생성자 우선순위 선택
- 필수 필드 검증
- 기본값 처리
- 에러 처리 및 예외 상황

## 📁 구현된 파일 구조

```
src/main/java/com/hancomins/json5/serializer/
├── JSON5Creator.java                    # 생성자 어노테이션
├── JSON5Property.java                   # 프로퍼티 매핑 어노테이션
├── MissingValueStrategy.java            # 누락 값 처리 전략
├── DeserializationStrategySelector.java # 전략 선택기
├── constructor/
│   ├── ConstructorAnalyzer.java         # 생성자 분석기
│   ├── ConstructorInfo.java             # 생성자 정보
│   ├── ParameterInfo.java               # 파라미터 정보
│   ├── ConstructorDeserializer.java     # 생성자 역직렬화기
│   └── ParameterValueResolver.java      # 파라미터 값 해석기
└── path/
    └── JSON5PathExtractor.java          # 경로 추출기

src/test/java/com/hancomins/json5/serializer/
├── constructor/
│   ├── ConstructorAnalyzerTest.java
│   └── ParameterValueResolverTest.java
├── path/
│   └── JSON5PathExtractorTest.java
└── integration/
    └── ConstructorDeserializationIntegrationTest.java
```

## 🔧 기술적 특징

### 1. 중첩 경로 지원
- `"user.profile.email"` 형태의 경로 자동 파싱
- 배열 인덱스 접근 지원 (`"items.0"`)
- 깊이 제한 없는 중첩 구조 처리

### 2. 유연한 우선순위 시스템
- `@JSON5Creator(priority = n)` 을 통한 생성자 우선순위 설정
- 동일 우선순위 시 파라미터 개수 및 복잡도 고려

### 3. 강력한 에러 처리
- 명확한 예외 메시지 (어떤 필드에서 문제 발생했는지 명시)
- 필수 필드 누락 감지
- 타입 변환 실패 시 상세한 오류 정보

### 4. 기존 시스템과의 호환성
- 기존 `@JSON5Type` 클래스와 완전 호환
- 기존 역직렬화 방식에 대한 fallback 지원
- 성능 영향 최소화

## 🎯 달성된 목표

### 기능적 요구사항 ✅
- [x] 생성자 기반 역직렬화 완전 구현
- [x] 중첩 경로 접근 지원 ("a.b.c" 형태)
- [x] 우선순위 기반 생성자 선택
- [x] 필수 필드 검증 및 기본값 처리
- [x] 기존 기능과의 완벽한 호환성

### 비기능적 요구사항 ✅
- [x] 컴파일 에러 없이 빌드 성공
- [x] 모든 신규 테스트 통과
- [x] 명확하고 직관적인 API 설계
- [x] Jackson과 유사한 사용법

### 사용성 요구사항 ✅
- [x] Jackson 사용자가 쉽게 이해할 수 있는 API
- [x] 직관적이고 일관된 어노테이션 설계
- [x] 명확하고 도움이 되는 에러 메시지

## 🚀 다음 단계 제안

### 1. 고급 기능 추가
- 복합 타입 자동 변환 (현재는 JSON5Element로 처리)
- 커스텀 컨버터 지원
- 조건부 파라미터 매핑

### 2. 성능 최적화
- 생성자 분석 결과 캐싱
- 경로 파싱 최적화
- 메모리 사용량 모니터링

### 3. 추가 테스트
- 대용량 데이터 처리 테스트
- 멀티스레드 환경 테스트
- 기존 코드와의 성능 비교

## 📝 결론

Jackson 수준의 강력한 생성자 기반 역직렬화 기능이 JSON5 Serializer에 성공적으로 구현되었습니다. 
중첩 경로 접근, 우선순위 기반 생성자 선택, 강력한 에러 처리 등 핵심 기능들이 모두 동작하며, 
기존 시스템과의 완벽한 호환성도 유지됩니다.

이제 개발자들은 불변 객체(immutable objects)를 쉽게 생성하고, 복잡한 JSON 구조를 직관적으로 매핑할 수 있습니다.
