# JSON5 완전 사용 설명서

## 📋 개요

JSON5 Serializer는 Java 8 이상에서 동작하는 강력한 JSON5 직렬화/역직렬화 라이브러리입니다. JSON5는 JSON의 상위집합으로, **주석 처리, 후행 쉼표, 따옴표 없는 키** 등을 지원하여 **설정 파일 작성에 매우 적합**합니다.

### ✅ 주요 장점
- **설정 파일에 최적화**: 주석 처리와 유연한 문법으로 설정 파일 작성이 쉬움
- **Jackson 수준의 고급 기능**: 생성자 기반 역직렬화, 다형성 처리, 커스텀 값 공급자, TypeReference 지원
- **복잡한 중첩 타입 지원**: `List<Map<Car, Brand>>`, `Map<String, List<Map<String, User>>>` 등 완벽 지원
- **유연한 어노테이션 모드**: 어노테이션 없이도 동작, 필요시 explicit 모드로 엄격 제어
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

### 1. 기본 어노테이션과 모드

#### 어노테이션 모드 선택
JSON5 Serializer는 Jackson과 유사하게 **어노테이션 없이도 동작**합니다.

```java
// 📝 방법 1: 어노테이션 없이 사용 (Jackson 스타일)
public class User {
    private String name;    // 자동으로 직렬화/역직렬화됨
    private int age;        // 자동으로 직렬화/역직렬화됨
    
    public User() {}
    
    // Getter/Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// 📝 방법 2: 선택적 어노테이션 사용
@JSON5Type
public class User {
    @JSON5Value(comment = "사용자 이름")
    private String name;
    
    @JSON5Value(ignore = true)
    private String password;    // 직렬화에서 제외
    
    private int age;            // 어노테이션 없어도 포함됨
}

// 📝 방법 3: 엄격 모드 (explicit = true)
@JSON5Type(explicit = true)
public class User {
    @JSON5Value
    private String name;        // 어노테이션 있음 → 포함됨
    
    private int age;            // 어노테이션 없음 → 제외됨
    private String email;       // 어노테이션 없음 → 제외됨
    
    @JSON5Value
    private boolean isActive;   // 어노테이션 있음 → 포함됨
}
```

#### @JSON5Type - 클래스 어노테이션
```java
@JSON5Type
public class User {
    // 기본 모드: 모든 필드 자동 처리
}

@JSON5Type(explicit = true)
public class User {
    // 엄격 모드: @JSON5Value가 있는 필드만 처리
}

@JSON5Type(comment = "사용자 정보", commentAfter = "사용자 정보 끝")
public class User {
    // 클래스에 주석 추가
}
```

#### @JSON5Value - 필드 어노테이션
```java
@JSON5Type
public class User {
    @JSON5Value
    private String name;
    
    @JSON5Value(key = "user_id", comment = "사용자 ID")
    private String id;
    
    @JSON5Value(ignore = true)
    private String password; // 직렬화에서 제외
    
    private String internalData; // explicit=false면 포함, explicit=true면 제외
}
```

#### @JSON5ValueGetter/@JSON5ValueSetter - 메서드 어노테이션
```java
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    private List<String> hobbies;
    
    // Getter 메서드로 가상 필드 생성
    @JSON5ValueGetter(comment = "전체 이름")
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // 커스텀 키 이름 사용
    @JSON5ValueGetter(key = "hobby_list", comment = "취미 목록")
    public List<String> getUserHobbies() {
        return hobbies;
    }
    
    // Setter 메서드로 역직렬화 처리
    @JSON5ValueSetter
    public void setFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }
    
    @JSON5ValueSetter(key = "hobby_list", ignoreError = true)
    public void setUserHobbies(List<String> hobbies) {
        this.hobbies = hobbies != null ? hobbies : new ArrayList<>();
    }
}
```

#### @ObtainTypeValue - 제네릭/추상 타입 처리
```java
@JSON5Type
public class Container<T> {
    @JSON5Value
    private T data;
    
    @JSON5Value
    private String type;
    
    // 제네릭 타입의 실제 타입을 결정
    @ObtainTypeValue(fieldNames = {"data"})
    public T obtainDataType(JSON5Object fieldJson, JSON5Object rootJson) {
        String type = rootJson.getString("type");
        switch (type) {
            case "user":
                return (T) JSON5Serializer.fromJSON5Object(fieldJson, User.class);
            case "product":
                return (T) JSON5Serializer.fromJSON5Object(fieldJson, Product.class);
            default:
                return (T) fieldJson.get("value");
        }
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

### 6. 컬렉션과 Map 고도화 처리

#### 고급 Map 기능

**다양한 Key 타입 지원**
```java
@JSON5Type
public class AdvancedMaps {
    // Enum Key 지원
    @JSON5Value
    private Map<UserRole, List<String>> enumKeyMap;
    
    // Primitive Key 지원
    @JSON5Value
    private Map<Integer, User> intKeyMap;
    
    // @JSON5ValueProvider Key 지원
    @JSON5Value
    private Map<UserId, UserProfile> customKeyMap;
}

public enum UserRole { ADMIN, USER, GUEST }

// JSON 결과
{
    "enumKeyMap": {
        "ADMIN": ["all", "read", "write"],
        "USER": ["read"]
    },
    "intKeyMap": {
        "1": {"name": "John", "age": 30},
        "2": {"name": "Jane", "age": 25}
    }
}
```

**Map 값으로 Collection 지원**
```java
@JSON5Type
public class CollectionValues {
    @JSON5Value
    private Map<String, List<String>> rolePermissions;
    
    @JSON5Value
    private Map<String, Set<Permission>> userPermissions;
    
    @JSON5Value
    private Map<UserRole, List<User>> roleUsers;
}
```

#### TypeReference를 통한 완전한 제네릭 타입 지원

**기본 TypeReference 사용법**
```java
// 기존 방식의 한계
Map<UserRole, List<String>> result1 = deserializer.deserializeWithKeyType(
    json, UserRole.class, List.class); // ❌ List의 요소 타입 정보 손실

// TypeReference로 완전한 타입 정보 보존
Map<UserRole, List<String>> result2 = deserializer.deserializeWithTypeReference(json,
    new JSON5TypeReference<Map<UserRole, List<String>>>() {}); // ✅ 완전한 타입 정보
```

**복잡한 중첩 타입 완벽 지원**
```java
// 이제 이런 복잡한 타입도 완벽하게 지원됩니다!
Map<String, List<Map<Car, Brand>>> ultraComplex;

// 사용 예제
@JSON5Type
public class ComplexContainer {
    // List<Map<String, User>> - 리스트 안에 맵
    @JSON5Value
    private List<Map<String, User>> userMaps;
    
    // Map<UserRole, List<Map<String, Permission>>> - 3단계 중첩
    @JSON5Value  
    private Map<UserRole, List<Map<String, Permission>>> complexStructure;
    
    // Map<String, Set<List<Category>>> - 모든 Collection 타입 조합
    @JSON5Value
    private Map<String, Set<List<Category>>> megaComplex;
}

// TypeReference로 직렬화/역직렬화
JSON5Serializer serializer = JSON5Serializer.getInstance();

// 직렬화
JSON5Object json = (JSON5Object) JSON5Serializer.toJSON5WithTypeReference(complexData,
    new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});

// 역직렬화 - 모든 타입 정보 완벽 보존
Map<UserRole, List<Map<String, User>>> restored = 
    JSON5Serializer.fromJSON5ObjectWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
```

**Collection TypeReference 지원**
```java
// List<Map<String, Integer>> 같은 복잡한 Collection도 지원
List<Map<String, Integer>> complexList = new ArrayList<>();
Map<String, Integer> item1 = new HashMap<>();
item1.put("score", 95);
item1.put("rank", 1);
complexList.add(item1);

// 완전한 타입 정보로 직렬화/역직렬화
JSON5Array json = (JSON5Array) JSON5Serializer.toJSON5WithTypeReference(complexList,
    new JSON5TypeReference<List<Map<String, Integer>>>() {});

List<Map<String, Integer>> restored = 
    JSON5Serializer.fromJSON5ArrayWithTypeReference(json,
        new JSON5TypeReference<List<Map<String, Integer>>>() {});
```

**통합 API 메서드들**
```java
// JSON5Object에서 TypeReference 역직렬화
Map<UserRole, List<String>> mapResult = JSON5Serializer.fromJSON5ObjectWithTypeReference(
    jsonObject, new JSON5TypeReference<Map<UserRole, List<String>>>() {});

// JSON5Array에서 TypeReference 역직렬화  
List<Map<String, User>> listResult = JSON5Serializer.fromJSON5ArrayWithTypeReference(
    jsonArray, new JSON5TypeReference<List<Map<String, User>>>() {});

// 문자열에서 직접 TypeReference 파싱
Map<String, List<Integer>> directResult = JSON5Serializer.parseWithTypeReference(
    jsonString, new JSON5TypeReference<Map<String, List<Integer>>>() {});

// TypeReference로 직렬화
Object serialized = JSON5Serializer.toJSON5WithTypeReference(complexObject,
    new JSON5TypeReference<Map<String, List<Map<String, User>>>>() {});
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

#### 복잡한 중첩 타입 실무 예제
```java
@JSON5Type
public class GameConfiguration {
    // Map<GameMode, List<Map<ItemType, ItemConfig>>>
    @JSON5Value(comment = "게임 모드별 아이템 설정")
    private Map<GameMode, List<Map<ItemType, ItemConfig>>> modeItemConfigs;
    
    // List<Map<String, List<SkillTree>>>
    @JSON5Value(comment = "캐릭터별 스킬 트리")
    private List<Map<String, List<SkillTree>>> characterSkills;
    
    // Map<String, Set<List<Achievement>>>
    @JSON5Value(comment = "카테고리별 업적 그룹")
    private Map<String, Set<List<Achievement>>> achievementGroups;
}

public enum GameMode { NORMAL, HARD, EXPERT }
public enum ItemType { WEAPON, ARMOR, CONSUMABLE }

// TypeReference로 완전한 타입 정보 보존하여 처리
GameConfiguration config = new GameConfiguration();
// ... 데이터 설정

// 직렬화 - 모든 제네릭 타입 정보 보존
JSON5Object configJson = (JSON5Object) JSON5Serializer.toJSON5WithTypeReference(config,
    new JSON5TypeReference<GameConfiguration>() {});

// JSON5 형태로 설정 파일 저장
configJson.toString(WritingOptions.json5Pretty());
/*
// 게임 설정 파일
{
    // 게임 모드별 아이템 설정
    modeItemConfigs: {
        NORMAL: [
            {
                WEAPON: {
                    damage: 100,
                    durability: 50
                },
                ARMOR: {
                    defense: 30,
                    weight: 2.5
                }
            }
        ],
        HARD: [
            // ...
        ]
    },
    // 캐릭터별 스킬 트리
    characterSkills: [
        {
            "warrior": [
                {name: "Power Strike", level: 1},
                {name: "Shield Bash", level: 2}
            ],
            "mage": [
                {name: "Fireball", level: 1},
                {name: "Ice Storm", level: 3}
            ]
        }
    ]
}
*/

// 역직렬화 - 완전한 타입 정보 복원
GameConfiguration restoredConfig = 
    JSON5Serializer.fromJSON5ObjectWithTypeReference(configJson,
        new JSON5TypeReference<GameConfiguration>() {});

// 이제 모든 제네릭 타입이 완벽하게 보존됨
Map<GameMode, List<Map<ItemType, ItemConfig>>> itemConfigs = 
    restoredConfig.getModeItemConfigs();
ItemConfig weaponConfig = itemConfigs.get(GameMode.NORMAL).get(0).get(ItemType.WEAPON);
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

// 해결책 1: 제네릭 타입 명시
@JSON5Value
private List<User> userList;

// 해결책 2: TypeReference 사용 (복잡한 중첩 타입)
Map<UserRole, List<User>> complexType = JSON5Serializer.fromJSON5ObjectWithTypeReference(
    json, new JSON5TypeReference<Map<UserRole, List<User>>>() {});
```

**4. TypeReference ClassCastException 문제**
```java
// 문제: 복잡한 중첩 타입에서 타입 캐스팅 오류
Map<UserRole, List<Map<String, User>>> complexData; // ClassCastException 발생 가능

// 해결책: TypeReference 사용으로 타입 안전성 보장
Map<UserRole, List<Map<String, User>>> safeData = 
    deserializer.deserializeWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
```

**5. 어노테이션 없는 클래스 직렬화 문제**
```java
// 문제: 어노테이션 없는 클래스
public class SimpleUser {
    private String name; // 직렬화되지 않을 것 같지만...
    private int age;
}

// 실제로는 동작함! (Jackson과 동일)
SimpleUser user = new SimpleUser();
user.setName("John");
user.setAge(30);

JSON5Object json = JSON5Serializer.toJSON5Object(user); // ✅ 정상 동작
// {"name":"John","age":30}

// 엄격한 제어가 필요하다면 explicit 모드 사용
@JSON5Type(explicit = true)
public class StrictUser {
    @JSON5Value
    private String name;     // 포함됨
    
    private int age;         // 제외됨 (어노테이션 없음)
}
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

#### 2. 복잡한 타입 처리 가이드
```java
// ✅ 좋은 예: TypeReference로 완전한 타입 안전성
Map<UserRole, List<Map<String, User>>> complexData = 
    deserializer.deserializeWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});

// ✅ 좋은 예: 구체적 타입 명시
@JSON5Value
private List<String> tags;              // 구체적 타입

@JSON5Value  
private Map<String, User> userMap;      // String 키 사용

@JSON5Value
private Map<UserRole, List<String>> enumKeyMap; // Enum 키 지원

// ❌ 나쁜 예: Raw 타입 사용
@JSON5Value
private List tags;                      // Raw 타입

@JSON5Value
private Map userMap;                    // Raw 타입

// ❌ 나쁜 예: 지원하지 않는 키 타입
@JSON5Value
private Map<CustomObject, String> invalidKeyMap; // 지원되지 않는 키 타입
```

#### 3. 메서드 어노테이션 활용
```java
// ✅ 좋은 예: Getter/Setter로 가상 필드 생성
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    
    @JSON5ValueGetter(comment = "전체 이름")
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @JSON5ValueSetter
    public void setFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : "";
    }
    
    @JSON5ValueGetter(key = "display_name")
    public String getDisplayName() {
        return "Mr/Ms. " + getFullName();
    }
}

// JSON 결과
{
    "firstName": "John",
    "lastName": "Doe", 
    "fullName": "John Doe",        // Getter로 생성된 가상 필드
    "display_name": "Mr/Ms. John Doe"  // 커스텀 키 이름
}
```

#### 4. 어노테이션 모드별 사용 전략
```java
// 상황 1: 간단한 데이터 클래스 → 어노테이션 없이 사용
public class SimpleConfig {
    private String host = "localhost";
    private int port = 8080;
    private boolean ssl = false;
    // Getter/Setter만 추가
}

// 상황 2: 일부 필드 제어 필요 → 선택적 어노테이션
@JSON5Type
public class UserConfig {
    private String username;     // 자동 포함
    private String email;        // 자동 포함
    
    @JSON5Value(ignore = true)
    private String password;     // 제외
    
    @JSON5Value(comment = "마지막 로그인")
    private LocalDateTime lastLogin; // 주석 추가
}

// 상황 3: 엄격한 제어 필요 → explicit 모드
@JSON5Type(explicit = true)
public class SecurityConfig {
    @JSON5Value
    private String publicKey;    // 명시적 포함
    
    @JSON5Value  
    private String algorithm;    // 명시적 포함
    
    private String privateKey;   // 제외 (보안)
    private String salt;         // 제외 (보안)
    private String internalConfig; // 제외 (내부용)
}
```

#### 5. 예외 처리
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

// ✅ 좋은 예: TypeReference 안전 사용
public Map<String, List<User>> parseComplexDataSafely(JSON5Object json) {
    try {
        return JSON5Serializer.fromJSON5ObjectWithTypeReference(json,
            new JSON5TypeReference<Map<String, List<User>>>() {});
    } catch (JSON5SerializerException e) {
        logger.error("복잡한 데이터 파싱 실패: " + e.getMessage());
        return new HashMap<>(); // 빈 Map 반환
    }
}
```

#### 6. 성능 최적화 팁
```java
// ✅ 좋은 예: 스키마 캐시 활용
JSON5Serializer serializer = JSON5Serializer.builder()
    .enableSchemaCache()  // 스키마 캐시 활성화
    .build();

// 반복 처리 시 성능 향상
List<User> users = getUsers();
for (User user : users) {
    JSON5Object json = serializer.serialize(user);
    // 두 번째부터는 캐시된 스키마 사용
}

// ✅ 좋은 예: 적절한 설정 조합
User user = serializer.forDeserialization()
    .ignoreErrors()                // 오류 무시
    .withStrictTypeChecking(false) // 엄격한 타입 체크 비활성화  
    .deserialize(json, User.class);
```

---

## 🎯 요약

JSON5 Serializer는 설정 파일 처리에 특화된 강력한 라이브러리입니다:

### 핵심 장점
- **JSON5 지원**: 주석, 후행 쉼표, 따옴표 없는 키 등으로 설정 파일 작성 용이
- **고급 기능**: Jackson 수준의 생성자 기반 역직렬화, 다형성 처리, 커스텀 값 공급자, TypeReference
- **복잡한 중첩 타입**: `List<Map<Car, Brand>>`, `Map<String, List<Map<String, User>>>` 등 완벽 지원
- **유연한 API**: 정적 메서드와 Fluent API 모두 지원
- **어노테이션 선택**: 어노테이션 없이도 동작, 필요시 explicit 모드로 엄격 제어

### 사용 권장 사항
- **✅ 설정 파일**: 애플리케이션 설정, 환경 설정 등
- **✅ 개발 도구**: 빌드 스크립트, 개발자 도구 설정
- **✅ 복잡한 객체 구조**: 다형성이 필요한 도메인 모델
- **❌ REST API**: 표준 JSON 사용 권장
- **❌ 시스템 간 데이터 교환**: 호환성을 위해 표준 JSON 사용

이 가이드를 통해 JSON5 Serializer의 모든 기능을 효과적으로 활용하여 유지보수하기 쉬운 설정 파일과 강력한 객체 직렬화를 구현할 수 있습니다.