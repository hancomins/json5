# JSON5 Serializer 완전 사용 설명서

## 📋 개요

JSON5 Serializer는 Java 8 이상에서 동작하는 강력한 JSON5 직렬화/역직렬화 라이브러리입니다. JSON5는 JSON의 상위집합으로, **주석 처리, 후행 쉼표, 따옴표 없는 키** 등을 지원하여 **설정 파일 작성에 매우 적합**합니다.

### ✅ 주요 장점
- **설정 파일에 최적화**: 주석 처리와 유연한 문법으로 설정 파일 작성이 쉬움
- **Jackson 수준의 고급 기능**: 생성자 기반 역직렬화, 다형성 처리, 커스텀 값 공급자 지원
- **XPath 스타일 경로 접근**: `users[0].profile.email` 같은 중첩 경로 접근 지원

### ⚠️ 주의사항
- **데이터 포맷으로는 부적절**: 시스템 간 데이터 교환용으로는 표준 JSON 사용 권장
- **네트워크 API에는 비추천**: REST API 등에서는 표준 JSON이 더 적합

---

## 🚀 기본 설정

### Gradle 의존성 추가
```groovy
dependencies {
    implementation 'io.github.hancomins:json5:1.x.x'
}
```

### 기본 import
```java
import com.hancomins.json5.*;
import com.hancomins.json5.serializer.*;
import com.hancomins.json5.options.*;
```

---

## 📦 JSON5Object와 JSON5Array 기본 사용법

### JSON5Object 기본 조작

#### 1. 객체 생성과 데이터 추가
```java
// 빈 객체 생성
JSON5Object user = new JSON5Object();

// 기본 타입 데이터 추가
user.put("name", "홍길동");
user.put("age", 30);
user.put("isActive", true);
user.put("score", 95.5);

// 중첩 객체 추가
JSON5Object profile = new JSON5Object();
profile.put("email", "hong@example.com");
profile.put("department", "개발팀");
user.put("profile", profile);

System.out.println(user);
// {"name":"홍길동","age":30,"isActive":true,"score":95.5,"profile":{"email":"hong@example.com","department":"개발팀"}}
```

#### 2. JSON5 문자열에서 객체 생성
```java
// JSON5 형식 (주석, 후행 쉼표, 따옴표 없는 키 지원)
String json5String = """
{
    // 사용자 기본 정보
    name: '홍길동',
    age: 30,
    hobbies: ['독서', '영화감상', '여행',], // 후행 쉼표 허용
    /* 연락처 정보 */
    contact: {
        email: 'hong@example.com',
        phone: '010-1234-5678'
    }
}
""";

JSON5Object user = new JSON5Object(json5String);
```

#### 3. 데이터 조회
```java
// 기본 조회
String name = user.getString("name");
int age = user.getInt("age");
boolean isActive = user.getBoolean("isActive", false); // 기본값 지정

// 중첩 객체 조회
JSON5Object profile = user.getJSON5Object("profile");
String email = profile.getString("email");

// 안전한 조회 (null 처리)
String department = user.getJSON5Object("profile").getString("department", "미정");

// null 체크
if (user.has("profile")) {
    JSON5Object userProfile = user.getJSON5Object("profile");
    // 프로필 정보 처리
}
```

### JSON5Array 기본 조작

#### 1. 배열 생성과 데이터 추가
```java
// 빈 배열 생성
JSON5Array hobbies = new JSON5Array();

// 데이터 추가
hobbies.put("독서");
hobbies.put("영화감상");
hobbies.put("여행");

// 여러 데이터 한 번에 추가
hobbies.put("등산", "요리", "게임");

// 객체를 배열에 추가
JSON5Object hobby1 = new JSON5Object();
hobby1.put("name", "독서");
hobby1.put("frequency", "매일");
hobbies.put(hobby1);
```

#### 2. JSON5 배열 문자열에서 생성
```java
String arrayString = """
[
    '독서',
    '영화감상',
    {
        name: '여행',
        frequency: '월 1회',
        cost: 50000
    },
    // 마지막 항목도 주석 가능
    '운동'
]
""";

JSON5Array hobbies = new JSON5Array(arrayString);
```

#### 3. 데이터 조회
```java
// 인덱스로 조회
String firstHobby = hobbies.getString(0);
JSON5Object hobbyDetail = hobbies.getJSON5Object(2);

// 배열 크기
int size = hobbies.size();

// 반복 처리
for (int i = 0; i < hobbies.size(); i++) {
    Object item = hobbies.get(i);
    System.out.println("항목 " + i + ": " + item);
}

// Enhanced for 사용
for (Object item : hobbies) {
    System.out.println("항목: " + item);
}
```

### JSON5 고급 기능 활용

#### 1. 주석 처리
```java
JSON5Object config = new JSON5Object();
config.put("port", 8080);
config.put("host", "localhost");

// 키에 주석 추가
config.setCommentForKey("port", "서버 포트 번호");
config.setCommentAfterValue("host", "개발 환경용 호스트");

// 객체 전체에 주석 추가
config.setHeaderComment("서버 설정 파일");
config.setFooterComment("설정 끝");

System.out.println(config.toString(WritingOptions.json5Pretty()));
```

#### 2. 경로 기반 접근 (XPath 스타일)
```java
JSON5Object data = new JSON5Object();
JSON5Array users = new JSON5Array();

JSON5Object user1 = new JSON5Object();
user1.put("name", "김철수");
user1.put("email", "kim@example.com");
users.put(user1);

data.put("users", users);

// 경로로 값 접근
String firstUserName = data.getString("$.users[0].name");
String firstUserEmail = data.getString("$.users[0].email");

// 경로로 값 설정
data.put("$.users[0].department", "개발팀");
data.put("$.users[1]", new JSON5Object().put("name", "이영희"));
```

---

## 🔄 직렬화/역직렬화 완전 가이드

### 기본 개념

JSON5 Serializer는 Java 객체와 JSON5 간의 양방향 변환을 지원합니다. 어노테이션을 통해 세밀한 제어가 가능하며, Jackson과 유사한 고급 기능들을 제공합니다.

### 1. 모든 어노테이션 완전 가이드

JSON5 Serializer는 다양한 어노테이션을 제공하여 세밀한 직렬화/역직렬화 제어가 가능합니다.

#### 클래스 레벨 어노테이션

##### @JSON5Type - 기본 클래스 마킹
```java
@JSON5Type
public class User {
    // 직렬화/역직렬화 대상 클래스 표시
}

@JSON5Type(comment = "사용자 정보", commentAfter = "사용자 정보 끝", explicit = false)
public class User {
    // comment: 클래스 앞에 추가할 주석
    // commentAfter: 클래스 뒤에 추가할 주석  
    // explicit: 명시적 모드 (false가 기본값)
}
```

##### @JSON5TypeInfo - 다형성 타입 정보
```java
@JSON5TypeInfo(
    property = "type",                              // 타입 결정 키
    include = TypeInclusion.PROPERTY,               // 포함 방식
    defaultImpl = DefaultUser.class,                // 기본 구현체
    onMissingType = MissingTypeStrategy.DEFAULT_IMPL // 누락 시 동작
)
public abstract class User { }
```

**TypeInclusion 옵션:**
- `PROPERTY`: 별도 속성으로 타입 정보 포함
- `EXISTING_PROPERTY`: 기존 속성을 타입 정보로 활용

**MissingTypeStrategy 옵션:**
- `DEFAULT_IMPL`: 기본 구현체 사용
- `EXCEPTION`: 예외 발생

##### @JSON5SubType - 서브타입 정의
```java
@JSON5SubType(value = AdminUser.class, name = "admin")
@JSON5SubType(value = RegularUser.class, name = "regular")
@JSON5SubType(value = GuestUser.class, name = "guest")
public abstract class User { }

// 또는 @JSON5SubTypes 사용
@JSON5SubTypes({
    @JSON5SubType(value = AdminUser.class, name = "admin"),
    @JSON5SubType(value = RegularUser.class, name = "regular"),
    @JSON5SubType(value = GuestUser.class, name = "guest")
})
public abstract class User { }
```

##### @JSON5ValueProvider - 커스텀 값 공급자
```java
@JSON5ValueProvider(
    targetType = String.class,           // 대상 타입
    nullHandling = NullHandling.DEFAULT, // null 처리 방식
    strictTypeMatching = true            // 엄격한 타입 매칭
)
public class UserId { }
```

**NullHandling 옵션:**
- `DEFAULT`: null을 그대로 유지
- `EXCEPTION`: 예외 발생
- `EMPTY_OBJECT`: 기본값으로 초기화

#### 필드 레벨 어노테이션

##### @JSON5Value - 기본 필드 마킹
```java
@JSON5Type
public class User {
    @JSON5Value
    private String name; // 기본 사용법
    
    @JSON5Value(key = "user_name", comment = "사용자 이름")
    private String name; // 커스텀 키와 주석
    
    @JSON5Value(value = "email_address", comment = "이메일", commentAfterKey = "필수 항목")
    private String email; // value는 key와 동일
    
    @JSON5Value(ignore = true)
    private String password; // 직렬화에서 제외
}
```

#### 메서드 레벨 어노테이션

##### @JSON5ValueGetter - 커스텀 Getter
```java
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    
    // 기본 사용법 (메서드 이름에서 키 추출)
    @JSON5ValueGetter
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // 커스텀 키 지정
    @JSON5ValueGetter(key = "display_name", comment = "화면 표시용 이름")
    public String getDisplayName() {
        return firstName + " " + lastName;
    }
    
    // value와 key는 동일한 의미
    @JSON5ValueGetter(value = "full_name", commentAfterKey = "전체 이름")
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // 오류 무시 옵션
    @JSON5ValueGetter(key = "risky_value", ignoreError = true)
    public String getRiskyValue() {
        // 예외가 발생할 수 있는 로직
        return someRiskyOperation();
    }
}
```

##### @JSON5ValueSetter - 커스텀 Setter
```java
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    
    // 기본 사용법 (메서드 이름에서 키 추출)
    @JSON5ValueSetter
    public void setFullName(String fullName) {
        String[] parts = fullName.split(" ");
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }
    
    // 커스텀 키 지정
    @JSON5ValueSetter(key = "display_name")
    public void setDisplayName(String displayName) {
        setFullName(displayName);
    }
    
    // value와 key는 동일한 의미
    @JSON5ValueSetter(value = "user_name")
    public void setUserName(String userName) {
        setFullName(userName);
    }
    
    // 오류 무시 옵션
    @JSON5ValueSetter(key = "optional_field", ignoreError = true)
    public void setOptionalField(String value) {
        // 실패해도 무시되는 설정
        someOptionalOperation(value);
    }
}
```

#### 생성자 레벨 어노테이션

##### @JSON5Creator - 생성자 지정
```java
@JSON5Type
public class User {
    private final String name;
    private final int age;
    
    // 기본 우선순위 (0)
    @JSON5Creator
    public User(@JSON5Property("name") String name) {
        this.name = name;
        this.age = 0;
    }
    
    // 높은 우선순위 (1)
    @JSON5Creator(priority = 1)
    public User(@JSON5Property("name") String name,
                @JSON5Property("age") int age) {
        this.name = name;
        this.age = age;
    }
}
```

#### 파라미터 레벨 어노테이션

##### @JSON5Property - 생성자 파라미터 매핑
```java
@JSON5Creator
public User(
    @JSON5Property("name") String name,
    @JSON5Property(value = "age", required = true) int age,
    @JSON5Property(value = "email", onMissing = MissingValueStrategy.EXCEPTION) String email,
    @JSON5Property("profile.department") String department
) {
    // value: JSON 키 (경로 지원)
    // required: 필수 여부
    // onMissing: 누락 시 동작
}
```

**MissingValueStrategy 옵션:**
- `DEFAULT_VALUE`: 기본값 사용 (null, 0, false 등)
- `EXCEPTION`: 예외 발생

##### @JSON5ValueConstructor - 값 공급자 생성자
```java
@JSON5ValueProvider
public class UserId {
    @JSON5ValueConstructor(onNull = NullHandling.EMPTY_OBJECT)
    public UserId(String id) {
        // onNull: null 입력 시 동작
    }
}
```

##### @JSON5ValueExtractor - 값 공급자 추출자
```java
@JSON5ValueProvider
public class UserId {
    @JSON5ValueExtractor(onNull = NullHandling.EXCEPTION)
    public String getId() {
        // onNull: null 반환 시 동작
        return id;
    }
}
```

#### 고급 어노테이션

##### @ObtainTypeValue - 제네릭 타입 처리
```java
@JSON5Type
public class Container<T> {
    @JSON5Value
    private T data;
    
    // 필드 기반 타입 해석 (역직렬화 전)
    @ObtainTypeValue(fieldNames = {"data"}, deserializeAfter = false)
    public Class<?> getDataType(JSON5Object fieldObject, JSON5Object rootObject) {
        String typeHint = rootObject.getString("dataType");
        switch (typeHint) {
            case "string": return String.class;
            case "number": return Integer.class;
            case "user": return User.class;
            default: return Object.class;
        }
    }
    
    // 세터 기반 타입 해석 (역직렬화 후)
    @ObtainTypeValue(setterMethodNames = {"setData"}, deserializeAfter = true)
    public Object transformData(JSON5Object fieldObject, JSON5Object rootObject) {
        // 역직렬화된 데이터를 추가 변환
        return processedData;
    }
    
    // 오류 무시 옵션
    @ObtainTypeValue(fieldNames = {"data"}, ignoreError = true)
    public Class<?> getDataTypeSafely(JSON5Object fieldObject, JSON5Object rootObject) {
        try {
            return determineType(fieldObject);
        } catch (Exception e) {
            return Object.class; // 기본 타입 반환
        }
    }
}
```

**@ObtainTypeValue 상세 옵션:**
- `fieldNames`: 대상 필드명 배열
- `setterMethodNames`: 대상 세터 메서드명 배열
- `deserializeAfter`: 역직렬화 후 실행 여부
- `ignoreError`: 오류 무시 여부

### 2. 어노테이션 조합 패턴

#### 완전한 커스텀 객체
```java
@JSON5Type(comment = "사용자 관리 클래스")
public class UserManager {
    private Map<String, User> users = new HashMap<>();
    private int totalCount;
    
    // Getter로 계산된 값 제공
    @JSON5ValueGetter(key = "user_count", comment = "총 사용자 수")
    public int getUserCount() {
        return users.size();
    }
    
    // Setter로 복잡한 데이터 처리
    @JSON5ValueSetter(key = "users_data")
    public void setUsersData(List<User> userList) {
        this.users.clear();
        for (User user : userList) {
            this.users.put(user.getId(), user);
        }
    }
    
    // 기본 필드
    @JSON5Value(key = "total", comment = "전체 등록 수")
    private int totalCount;
}
```

#### 다형성 + 생성자 기반
```java
@JSON5TypeInfo(property = "type")
@JSON5SubType(value = EmailNotification.class, name = "email")
@JSON5SubType(value = SmsNotification.class, name = "sms")
public abstract class Notification {
    @JSON5Value
    protected String type;
    
    @JSON5Value
    protected String message;
}

@JSON5Type
public class EmailNotification extends Notification {
    private final String email;
    private final String subject;
    
    @JSON5Creator
    public EmailNotification(@JSON5Property("recipient.email") String email,
                            @JSON5Property("email.subject") String subject,
                            @JSON5Property("message") String message) {
        this.email = email;
        this.subject = subject;
        this.type = "email";
        this.message = message;
    }
}
```

### 3. 실무 활용 예제

#### 설정 파일 클래스
```java
@JSON5Type(comment = "애플리케이션 설정 파일")
public class AppConfig {
    
    @JSON5Value(comment = "서버 포트")
    private int port = 8080;
    
    @JSON5Value(key = "db_config", comment = "데이터베이스 설정")
    private DatabaseConfig database;
    
    // 환경변수로부터 값 조합
    @JSON5ValueGetter(key = "jdbc_url", comment = "JDBC 연결 URL")
    public String getJdbcUrl() {
        return "jdbc:mysql://" + database.getHost() + ":" + database.getPort() + "/" + database.getName();
    }
    
    // 복잡한 설정 파싱
    @JSON5ValueSetter(key = "logging_levels")
    public void setLoggingLevels(Map<String, String> levels) {
        for (Map.Entry<String, String> entry : levels.entrySet()) {
            Logger.getLogger(entry.getKey()).setLevel(Level.parse(entry.getValue()));
        }
    }
}
```

#### API 응답 클래스
```java
@JSON5TypeInfo(property = "status")
@JSON5SubType(value = SuccessResponse.class, name = "success")
@JSON5SubType(value = ErrorResponse.class, name = "error")
public abstract class ApiResponse {
    @JSON5Value
    protected String status;
    
    @JSON5ValueGetter(key = "timestamp")
    public long getTimestamp() {
        return System.currentTimeMillis();
    }
}

@JSON5Type
public class SuccessResponse extends ApiResponse {
    @JSON5Value
    private Object data;
    
    @JSON5ValueGetter(key = "data_size", comment = "데이터 크기")
    public int getDataSize() {
        if (data instanceof Collection) {
            return ((Collection<?>) data).size();
        }
        return data != null ? 1 : 0;
    }
}
```

### 2. 기본 직렬화/역직렬화

#### 정적 메서드 사용 (기존 방식)
```java
@JSON5Type
public class User {
    @JSON5Value
    private String name;
    
    @JSON5Value
    private int age;
    
    // 기본 생성자 필요
    public User() {}
    
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    // Getter/Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// 직렬화
User user = new User("홍길동", 30);
JSON5Object json = JSON5Serializer.toJSON5Object(user);

// 역직렬화
User restored = JSON5Serializer.fromJSON5Object(json, User.class);
```

#### Fluent API 사용 (권장 방식)
```java
// 기본 사용
JSON5Serializer serializer = JSON5Serializer.builder().build();

// 직렬화
JSON5Object json = serializer.forSerialization()
    .withWritingOptions(WritingOptions.json5Pretty())
    .includeNullValues()
    .serialize(user);

// 역직렬화
User restored = serializer.forDeserialization()
    .ignoreErrors()
    .withStrictTypeChecking(false)
    .deserialize(json, User.class);
```

### 3. 생성자 기반 역직렬화

#### 기본 사용법
```java
@JSON5Type
public class User {
    private final String name;
    private final int age;
    private final String email;
    
    // @JSON5Creator로 생성자 지정
    @JSON5Creator
    public User(@JSON5Property("name") String name,
                @JSON5Property("age") int age,
                @JSON5Property("email") String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }
    
    // Getter만 필요 (불변 객체)
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
}
```

#### 중첩 경로 접근
```java
@JSON5Type
public class UserProfile {
    private final String name;
    private final String email;
    private final String department;
    private final String city;
    
    @JSON5Creator
    public UserProfile(@JSON5Property("name") String name,
                      @JSON5Property("contact.email") String email,
                      @JSON5Property("work.department") String department,
                      @JSON5Property("address.city") String city) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.city = city;
    }
}

// JSON 구조
{
    "name": "홍길동",
    "contact": {
        "email": "hong@example.com",
        "phone": "010-1234-5678"
    },
    "work": {
        "department": "개발팀",
        "position": "시니어 개발자"
    },
    "address": {
        "city": "서울",
        "district": "강남구"
    }
}
```

#### 배열 인덱스 접근
```java
@JSON5Type
public class TeamInfo {
    private final String teamName;
    private final String leader;
    private final String firstMember;
    private final String secondMember;
    
    @JSON5Creator
    public TeamInfo(@JSON5Property("team.name") String teamName,
                   @JSON5Property("team.leader") String leader,
                   @JSON5Property("members[0].name") String firstMember,
                   @JSON5Property("members[1].name") String secondMember) {
        this.teamName = teamName;
        this.leader = leader;
        this.firstMember = firstMember;
        this.secondMember = secondMember;
    }
}

// JSON 구조
{
    "team": {
        "name": "개발팀",
        "leader": "김팀장"
    },
    "members": [
        {"name": "이개발", "role": "시니어"},
        {"name": "박신입", "role": "주니어"},
        {"name": "최경력", "role": "시니어"}
    ]
}
```

#### 생성자 우선순위와 필수 필드
```java
@JSON5Type
public class FlexibleUser {
    private final String name;
    private final int age;
    private final String type;
    
    // 기본 생성자 (우선순위 낮음)
    @JSON5Creator
    public FlexibleUser(@JSON5Property("name") String name,
                       @JSON5Property("age") int age) {
        this.name = name;
        this.age = age;
        this.type = "basic";
    }
    
    // 우선순위 높은 생성자
    @JSON5Creator(priority = 1)
    public FlexibleUser(@JSON5Property("name") String name,
                       @JSON5Property(value = "age", required = true) int age,
                       @JSON5Property(value = "type", onMissing = MissingValueStrategy.EXCEPTION) String type) {
        this.name = name;
        this.age = age;
        this.type = type;
    }
}
```

### 4. 다형성 역직렬화

#### 기본 다형성 처리
```java
// 부모 클래스/인터페이스
@JSON5TypeInfo(property = "type")
@JSON5SubType(value = Dog.class, name = "dog")
@JSON5SubType(value = Cat.class, name = "cat")
@JSON5SubType(value = Bird.class, name = "bird")
public abstract class Animal {
    @JSON5Value
    protected String name;
    
    public abstract void makeSound();
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

// 구현 클래스들
@JSON5Type
public class Dog extends Animal {
    @JSON5Value
    private String breed;
    
    @Override
    public void makeSound() { System.out.println("멍멍!"); }
    
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
}

@JSON5Type
public class Cat extends Animal {
    @JSON5Value
    private boolean indoor;
    
    @Override
    public void makeSound() { System.out.println("야옹!"); }
    
    public boolean isIndoor() { return indoor; }
    public void setIndoor(boolean indoor) { this.indoor = indoor; }
}

// 사용 예제
JSON5Object dogJson = new JSON5Object();
dogJson.put("type", "dog");
dogJson.put("name", "멍멍이");
dogJson.put("breed", "진돗개");

Animal animal = JSON5Serializer.fromJSON5Object(dogJson, Animal.class);
// 결과: Dog 인스턴스가 생성됨
```

#### 중첩된 타입 정보
```java
@JSON5TypeInfo(property = "vehicle.type")
@JSON5SubType(value = Car.class, name = "car")
@JSON5SubType(value = Motorcycle.class, name = "motorcycle")
public interface Vehicle {
    void start();
}

// JSON 구조
{
    "owner": "홍길동",
    "vehicle": {
        "type": "car",
        "brand": "현대",
        "model": "소나타"
    }
}
```

#### 기존 속성을 타입 정보로 활용
```java
@JSON5TypeInfo(property = "status", include = TypeInclusion.EXISTING_PROPERTY)
@JSON5SubType(value = ActiveUser.class, name = "active")
@JSON5SubType(value = InactiveUser.class, name = "inactive")
@JSON5SubType(value = PendingUser.class, name = "pending")
public abstract class User {
    @JSON5Value
    protected String status; // 이 필드 값이 타입 결정에도 사용됨
    
    @JSON5Value
    protected String name;
}

@JSON5Type
public class ActiveUser extends User {
    @JSON5Value
    private String lastLoginDate;
}

// JSON에서 status 필드의 값("active")이 타입 결정에 사용됨
{
    "status": "active",
    "name": "홍길동",
    "lastLoginDate": "2024-01-15"
}
```

#### 기본 구현체 지정
```java
@JSON5TypeInfo(
    property = "type",
    defaultImpl = GenericPayment.class,
    onMissingType = MissingTypeStrategy.DEFAULT_IMPL
)
@JSON5SubType(value = CreditCardPayment.class, name = "credit")
@JSON5SubType(value = PayPalPayment.class, name = "paypal")
public interface Payment {
    void process();
}

// type 정보가 없거나 매칭되지 않으면 GenericPayment로 역직렬화
{
    "amount": 10000,
    "currency": "KRW"
    // type 필드 없음 -> GenericPayment 사용
}
```

### 5. 커스텀 값 공급자 (Value Provider)

#### 기본 사용법
```java
@JSON5ValueProvider
public class UserId {
    private final String id;
    
    // 역직렬화: String → UserId
    @JSON5ValueConstructor
    public UserId(String id) {
        this.id = id;
    }
    
    // 직렬화: UserId → String
    @JSON5ValueExtractor
    public String getId() {
        return id;
    }
}

// 사용 클래스
@JSON5Type
public class User {
    @JSON5Value
    private UserId userId;  // UserId 객체가 String으로 직렬화/역직렬화됨
    
    @JSON5Value
    private String name;
}
```

#### 복잡한 타입 변환
```java
@JSON5ValueProvider(targetType = JSON5Object.class, strictTypeMatching = false)
public class ConnectionConfig {
    private String host;
    private int port;
    
    @JSON5ValueConstructor
    public ConnectionConfig(JSON5Object config) {
        this.host = config.getString("host");
        this.port = config.getInt("port");
    }
    
    @JSON5ValueExtractor
    public JSON5Object getConfig() {
        JSON5Object obj = new JSON5Object();
        obj.put("host", host);
        obj.put("port", port);
        return obj;
    }
}
```

#### Null 처리 설정
```java
@JSON5ValueProvider
public class SafeWrapper {
    private String value;
    
    @JSON5ValueConstructor(onNull = NullHandling.EMPTY_OBJECT)
    public SafeWrapper(String value) {
        this.value = value != null ? value : "기본값";
    }
    
    @JSON5ValueExtractor(onNull = NullHandling.EXCEPTION)
    public String getValue() {
        if (value == null) {
            throw new JSON5SerializerException("값이 null일 수 없습니다");
        }
        return value;
    }
}
```

### 6. 컬렉션과 Map 처리

#### List 직렬화/역직렬화
```java
@JSON5Type
public class Team {
    @JSON5Value
    private String name;
    
    @JSON5Value
    private List<User> members;
    
    @JSON5Value
    private List<String> skills;
}

// 사용
Team team = new Team();
team.setName("개발팀");
team.setMembers(Arrays.asList(
    new User("김개발", 30),
    new User("이프론트", 28)
));
team.setSkills(Arrays.asList("Java", "JavaScript", "Python"));

JSON5Object json = JSON5Serializer.toJSON5Object(team);
Team restored = JSON5Serializer.fromJSON5Object(json, Team.class);
```

#### Map 직렬화/역직렬화
```java
@JSON5Type
public class UserManager {
    @JSON5Value(key = "users")
    private Map<String, User> userMap = new HashMap<>();
    
    @JSON5Value
    private Map<String, List<String>> rolePermissions = new HashMap<>();
}

// 주의: Map의 키는 반드시 String이어야 함
// 값으로는 기본 타입, @JSON5Type 클래스, List 등 사용 가능
```

### 7. 고급 설정과 옵션

#### 빌더를 통한 고급 설정
```java
JSON5Serializer serializer = JSON5Serializer.builder()
    .ignoreUnknownProperties()     // 알 수 없는 속성 무시
    .enableSchemaCache()           // 스키마 캐시 사용
    .withErrorHandling(true)       // 오류 처리 활성화
    .build();
```

#### 직렬화 옵션
```java
JSON5Object json = serializer.forSerialization()
    .withWritingOptions(WritingOptions.json5Pretty())  // 예쁜 출력
    .includeNullValues()                               // null 값 포함
    .ignoreFields("password", "internalId")            // 특정 필드 무시
    .serialize(user);
```

#### 역직렬화 옵션
```java
User user = serializer.forDeserialization()
    .ignoreErrors()                 // 오류 무시
    .withStrictTypeChecking(false)  // 엄격한 타입 체크 비활성화
    .withDefaultValue(new User())   // 기본값 설정
    .deserialize(json, User.class);
```

### 8. 실무 활용 예제

#### 설정 파일 처리
```java
@JSON5Type(comment = "애플리케이션 설정")
public class AppConfig {
    @JSON5Value(comment = "서버 설정")
    private ServerConfig server;
    
    @JSON5Value(comment = "데이터베이스 설정")
    private DatabaseConfig database;
    
    @JSON5Value(comment = "로깅 설정")
    private LoggingConfig logging;
}

@JSON5Type
public class ServerConfig {
    @JSON5Value(comment = "서버 포트")
    private int port = 8080;
    
    @JSON5Value(comment = "호스트 주소")
    private String host = "localhost";
    
    @JSON5Value(comment = "SSL 사용 여부")
    private boolean ssl = false;
}

// config.json5 파일
/*
// 애플리케이션 설정
{
    // 서버 설정
    server: {
        // 서버 포트
        port: 8080,
        // 호스트 주소
        host: 'localhost',
        // SSL 사용 여부
        ssl: false
    },
    // 데이터베이스 설정
    database: {
        url: 'jdbc:mysql://localhost:3306/mydb',
        username: 'user',
        password: 'pass',
        // 연결 풀 설정
        pool: {
            minSize: 5,
            maxSize: 20,
        }
    }
}
*/
```

#### API 응답 처리
```java
@JSON5TypeInfo(property = "status")
@JSON5SubType(value = SuccessResponse.class, name = "success")
@JSON5SubType(value = ErrorResponse.class, name = "error")
public abstract class ApiResponse {
    @JSON5Value
    protected String status;
    
    @JSON5Value
    protected String message;
}

@JSON5Type
public class SuccessResponse extends ApiResponse {
    @JSON5Value
    private Object data;
    
    @JSON5Value
    private int totalCount;
}

@JSON5Type
public class ErrorResponse extends ApiResponse {
    @JSON5Value
    private String errorCode;
    
    @JSON5Value
    private List<String> details;
}
```

#### 복잡한 비즈니스 객체
```java
@JSON5Type
public class Order {
    @JSON5Value
    private String orderId;
    
    @JSON5Value
    private List<OrderItem> items;
    
    @JSON5Value
    private Customer customer;
    
    @JSON5Value
    private Payment payment;
    
    @JSON5Value
    private OrderStatus status;
    
    @JSON5Value
    private LocalDateTime createdAt;
}

@JSON5Type
public class OrderItem {
    @JSON5Value
    private String productId;
    
    @JSON5Value
    private String productName;
    
    @JSON5Value
    private int quantity;
    
    @JSON5Value
    private BigDecimal price;
}

// 사용 예제
Order order = new Order();
order.setOrderId("ORD-2024-001");
order.setItems(Arrays.asList(
    new OrderItem("PROD-001", "노트북", 1, new BigDecimal("1500000")),
    new OrderItem("PROD-002", "마우스", 2, new BigDecimal("25000"))
));

JSON5Object orderJson = JSON5Serializer.toJSON5Object(order);
```

#### 생성자 기반 불변 객체 패턴
```java
@JSON5Type
public class ImmutableProduct {
    private final String id;
    private final String name;
    private final BigDecimal price;
    private final List<String> categories;
    private final ProductDetails details;
    
    @JSON5Creator
    public ImmutableProduct(
        @JSON5Property("id") String id,
        @JSON5Property("name") String name,
        @JSON5Property("price") BigDecimal price,
        @JSON5Property("categories") List<String> categories,
        @JSON5Property("details.weight") double weight,
        @JSON5Property("details.dimensions.width") double width,
        @JSON5Property("details.dimensions.height") double height,
        @JSON5Property("details.dimensions.depth") double depth
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.categories = new ArrayList<>(categories);
        this.details = new ProductDetails(weight, width, height, depth);
    }
    
    // Getter만 제공 (불변 객체)
    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public List<String> getCategories() { return new ArrayList<>(categories); }
    public ProductDetails getDetails() { return details; }
}

// JSON 구조
{
    "id": "PROD-001",
    "name": "게이밍 노트북",
    "price": 1500000,
    "categories": ["컴퓨터", "게임", "전자제품"],
    "details": {
        "weight": 2.5,
        "dimensions": {
            "width": 35.0,
            "height": 2.5,
            "depth": 25.0
        },
        "manufacturer": "TechCorp"
    }
}
```

### 9. 문제 해결 가이드

#### 일반적인 오류와 해결책

**1. "No default constructor found" 오류**
```java
// 문제: 기본 생성자 없음
@JSON5Type
public class User {
    public User(String name) { ... } // 매개변수 있는 생성자만 존재
}

// 해결책 1: 기본 생성자 추가
@JSON5Type
public class User {
    private User() {} // private도 가능
    public User(String name) { ... }
}

// 해결책 2: @JSON5Creator 사용
@JSON5Type
public class User {
    @JSON5Creator
    public User(@JSON5Property("name") String name) { ... }
}
```

**2. "Circular reference detected" 오류**
```java
// 문제: 순환 참조
@JSON5Type
public class Department {
    @JSON5Value
    private List<Employee> employees;
}

@JSON5Type  
public class Employee {
    @JSON5Value
    private Department department; // 순환 참조!
}

// 해결책: 한쪽 참조 제거 또는 ignore 사용
@JSON5Type
public class Employee {
    @JSON5Value(ignore = true)
    private Department department; // 직렬화에서 제외
}
```

**3. 제네릭 타입 처리 문제**
```java
// 문제: Raw 타입 사용
@JSON5Value
private List userList; // 제네릭 정보 없음

// 해결책: 제네릭 타입 명시
@JSON5Value
private List<User> userList;
```

#### 디버깅 팁

**1. 직렬화 결과 확인**
```java
JSON5Object json = JSON5Serializer.toJSON5Object(object);
System.out.println("직렬화 결과:");
System.out.println(json.toString(WritingOptions.json5Pretty()));
```

**2. 오류 무시 모드로 테스트**
```java
// 어떤 필드에서 문제가 발생하는지 확인
User user = JSON5Serializer.getInstance()
    .forDeserialization()
    .ignoreErrors() // 오류 무시하고 진행
    .deserialize(json, User.class);
```

**3. 타입 정보 확인**
```java
// 다형성 처리에서 타입 정보 확인
if (json.has("type")) {
    System.out.println("타입 정보: " + json.getString("type"));
} else {
    System.out.println("타입 정보 없음 - 기본 구현체 사용됨");
}
```

### 10. 모범 사례 (Best Practices)

#### 1. 어노테이션 사용 가이드
```java
// ✅ 좋은 예
@JSON5Type(comment = "사용자 정보")
public class User {
    @JSON5Value(comment = "사용자 ID")
    private String id;

    @JSON5Value(key = "user_name", comment = "사용자 이름")
    private String name;

    @JSON5Value(ignore = true)
    private String password; // 민감 정보 제외
}

// ❌ 나쁜 예
public class User {
    private String id; // 어노테이션 누락
    @JSON5Value
    private String password; // 민감 정보 포함
}
```

#### 2. 불변 객체 선호
```java
// ✅ 좋은 예: 불변 객체
@JSON5Type
public class ImmutableUser {
    private final String name;
    private final int age;

    @JSON5Creator
    public ImmutableUser(@JSON5Property("name") String name,
                         @JSON5Property("age") int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
}
```

#### 3. 적절한 타입 사용
```java
// ✅ 좋은 예
@JSON5Value
private List<String> tags;              // 구체적 타입

@JSON5Value
private Map<String, User> userMap;      // String 키 사용

// ❌ 나쁜 예
@JSON5Value
private List tags;                      // Raw 타입

@JSON5Value
private Map<User, String> reverseMap;   // 비String 키
```

#### 4. 예외 처리
```java
// ✅ 좋은 예: 안전한 역직렬화
public User parseUserSafely(String jsonString) {
    try {
        JSON5Object json = new JSON5Object(jsonString);
        return JSON5Serializer.fromJSON5Object(json, User.class);
    } catch (JSON5SerializerException e) {
        logger.error("사용자 파싱 실패: " + e.getMessage());
        return new User(); // 기본값 반환
    }
}
```

---

## 🎯 요약

JSON5 Serializer는 설정 파일 처리에 특화된 강력한 라이브러리입니다:

### 핵심 장점
- **JSON5 지원**: 주석, 후행 쉼표, 따옴표 없는 키 등으로 설정 파일 작성 용이
- **고급 기능**: Jackson 수준의 생성자 기반 역직렬화, 다형성 처리, 커스텀 값 공급자
- **유연한 API**: 정적 메서드와 Fluent API 모두 지원

### 사용 권장 사항
- **✅ 설정 파일**: 애플리케이션 설정, 환경 설정 등
- **✅ 개발 도구**: 빌드 스크립트, 개발자 도구 설정
- **✅ 복잡한 객체 구조**: 다형성이 필요한 도메인 모델
- **❌ REST API**: 표준 JSON 사용 권장
- **❌ 시스템 간 데이터 교환**: 호환성을 위해 표준 JSON 사용

