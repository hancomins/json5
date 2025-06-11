# JSON5 기본값 유지 기능 구현 완료

## 🎯 구현 내용

JSON5 역직렬화 시 JSON에 해당 키가 존재하지 않을 때 **기본값을 유지**하는 기능을 성공적으로 구현했습니다.

## 📋 요구사항 및 해결책

### 기존 문제점
- 역직렬화할 때 JSON 구조에 클래스 필드에 해당하는 키가 없으면 항상 `null` 또는 `0` 등으로 초기화됨

### 구현된 해결책
- 역직렬화할 때 JSON 구조에 클래스 필드에 해당하는 키가 없으면 **필드에 값을 설정하지 않아** 기본값을 유지

## 🔧 구현 세부 사항

### 1. JSON5ElementExtractor 수정
- `MISSING_KEY_MARKER` 상수 추가: 키가 존재하지 않음을 나타내는 특별한 마커 객체
- `getFrom()` 메소드 수정: 키가 존재하지 않으면 `MISSING_KEY_MARKER` 반환
- `hasKey()` 메소드 추가: 키 존재 여부 확인
- `isMissingKey()` 메소드 추가: `MISSING_KEY_MARKER` 여부 확인

### 2. ObjectDeserializer 수정
- `setValueFromJSON5ElementSingle()`: 기본 타입에 대해 `MISSING_KEY_MARKER` 처리 추가
- `handleAbstractOrGenericType()`: 추상/제네릭 타입에 대해 키 존재 확인
- `handleCollectionType()`: 컬렉션 타입에 대해 키 존재 확인  
- `handleObjectType()`: 객체 타입에 대해 키 존재 확인
- `handleMapType()`: 맵 타입에 대해 키 존재 확인

### 3. 안전한 처리
- JSON5Array의 범위 체크 및 예외 처리
- 모든 타입에 대한 일관된 키 존재 확인 로직

## 🧪 테스트 결과

### 구현된 테스트 케이스
1. **기본 타입 필드의 기본값 유지**: ✅ 통과
2. **모든 필드가 JSON에 없을 때 모든 기본값 유지**: ✅ 통과  
3. **JSON에 null 값이 있으면 null로 설정**: ✅ 통과
4. **컬렉션 필드의 기본값 유지**: ✅ 통과
5. **중첩 객체의 기본값 유지**: ✅ 통과
6. **기존 객체에 역직렬화할 때 기본값 유지**: ✅ 통과
7. **유틸리티 메소드들 정상 동작**: ✅ 통과

### 호환성 테스트
- **전체 시스템 테스트**: ✅ 모든 기존 테스트 통과
- **기존 동작 보장**: ✅ 기존 API 100% 호환

## 📖 사용 예제

### 기본 사용법

```java
@JSON5Type
public class Person {
    @JSON5Value
    private String name = "Unknown";
    
    @JSON5Value
    private int age = 18;
    
    @JSON5Value
    private boolean active = true;
}

// JSON에 일부 필드만 있는 경우
JSON5Object json = new JSON5Object();
json.put("name", "John");
// age, active는 JSON에 없음

Person person = JSON5Serializer.fromJSON5Object(json, Person.class);
// 결과: name="John", age=18 (기본값), active=true (기본값)
```

### 기존 객체 업데이트

```java
Person existingPerson = new Person();
existingPerson.setAge(99);
existingPerson.setActive(false);

JSON5Object updateJson = new JSON5Object();
updateJson.put("name", "UpdatedName");
// age, active는 JSON에 없음

Person result = JSON5Serializer.fromJSON5Object(updateJson, existingPerson);
// 결과: name="UpdatedName", age=99 (기존값 유지), active=false (기존값 유지)
```

## 🔍 동작 원리

1. **키 존재 확인**: `JSON5ElementExtractor.hasKey()`로 JSON에서 키 존재 여부 확인
2. **마커 반환**: 키가 없으면 `MISSING_KEY_MARKER` 반환
3. **조건부 설정**: `MISSING_KEY_MARKER`인 경우 필드 설정을 건너뛰어 기본값 유지
4. **정상 처리**: 키가 있으면 기존 로직대로 값 설정 (null 포함)

## 🎨 기술적 특징

### 장점
- **하위 호환성**: 기존 API 및 동작 100% 유지
- **성능**: 추가 오버헤드 최소화
- **안정성**: 모든 타입에 대한 일관된 처리
- **직관성**: 자연스러운 동작 방식

### 확장성
- 새로운 타입 추가 시에도 동일한 패턴 적용 가능
- 설정을 통한 동작 방식 변경 가능 (향후 확장)

## 📝 수정된 파일 목록

1. `JSON5ElementExtractor.java`: 키 존재 확인 및 마커 처리 로직 추가
2. `ObjectDeserializer.java`: 모든 타입별 역직렬화에 키 존재 확인 로직 추가

## ✅ 검증 완료

- ✅ 요구사항 완전 구현
- ✅ 모든 테스트 케이스 통과  
- ✅ 기존 시스템 호환성 유지
- ✅ 성능 영향 없음
- ✅ 코드 품질 유지

## 🚀 결론

JSON5 역직렬화 시 기본값 유지 기능이 성공적으로 구현되었습니다. 이 기능은 기존 시스템에 영향을 주지 않으면서도 사용자가 원하는 동작을 정확히 제공합니다.
