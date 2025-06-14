# Complete JSON5 Serializer

# 한글 가이드 바로가기
 - [JSON5 한글 가이드](README.KO.md)

## 📋 Overview

JSON5 Serializer is a powerful JSON5 serialization/deserialization library that works with Java 8 and above. JSON5 is a superset of JSON that supports **comments, trailing commas, and unquoted keys**, making it **highly suitable for configuration files**.

### ✅ Key Advantages
- **Optimized for configuration files**: Easy configuration file creation with comments and flexible syntax
- **Advanced features comparable to Jackson**: Constructor-based deserialization, polymorphism handling, custom value providers, TypeReference support
- **Complex nested type support**: Perfect support for `List<Map<Car, Brand>>`, `Map<String, List<Map<String, User>>>`, etc.
- **Flexible annotation mode**: Works without annotations, with strict control using explicit mode when needed
- **XPath-style path access**: Supports nested path access like `users[0].profile.email`

### ⚠️ Cautions
- **Inappropriate as a data format**: Standard JSON is recommended for data exchange between systems
- **Not recommended for network APIs**: Standard JSON is more suitable for REST APIs

---

## 🚀 Basic Setup

### Adding Gradle Dependency
```groovy
dependencies {
    implementation 'io.github.hancomins:json5:1.x.x'
}
```

### Basic Imports
```java
import com.hancomins.json5.*;
import com.hancomins.json5.serializer.*;
import com.hancomins.json5.options.*;
```

---

## 📦 Basic Usage of JSON5Object and JSON5Array

### Basic JSON5Object Operations

#### 1. Creating Objects and Adding Data
```java
// Create empty object
JSON5Object user = new JSON5Object();

// Add basic type data
user.put("name", "Hong Gil-dong");
user.put("age", 30);
user.put("isActive", true);
user.put("score", 95.5);

// Add nested object
JSON5Object profile = new JSON5Object();
profile.put("email", "hong@example.com");
profile.put("department", "Development Team");
user.put("profile", profile);

System.out.println(user);
// {"name":"Hong Gil-dong","age":30,"isActive":true,"score":95.5,"profile":{"email":"hong@example.com","department":"Development Team"}}
```

#### 2. Creating Object from JSON5 String
```java
// JSON5 format (supports comments, trailing commas, unquoted keys)
String json5String = """
{
    // Basic user information
    name: 'Hong Gil-dong',
    age: 30,
    hobbies: ['Reading', 'Watching movies', 'Travel',], // trailing commas allowed
    /* Contact information */
    contact: {
        email: 'hong@example.com',
        phone: '010-1234-5678'
    }
}
""";

JSON5Object user = new JSON5Object(json5String);
```

#### 3. Data Retrieval
```java
// Basic retrieval
String name = user.getString("name");
int age = user.getInt("age");
boolean isActive = user.getBoolean("isActive", false); // specify default value

// Nested object retrieval
JSON5Object profile = user.getJSON5Object("profile");
String email = profile.getString("email");

// Safe retrieval (null handling)
String department = user.getJSON5Object("profile").getString("department", "Unspecified");

// Null check
if (user.has("profile")) {
    JSON5Object userProfile = user.getJSON5Object("profile");
    // Process profile information
}
```

### Basic JSON5Array Operations

#### 1. Creating Arrays and Adding Data
```java
// Create empty array
JSON5Array hobbies = new JSON5Array();

// Add data
hobbies.put("Reading");
hobbies.put("Watching movies");
hobbies.put("Travel");

// Add multiple data at once
hobbies.put("Hiking", "Cooking", "Gaming");

// Add objects to array
JSON5Object hobby1 = new JSON5Object();
hobby1.put("name", "Reading");
hobby1.put("frequency", "Daily");
hobbies.put(hobby1);
```

#### 2. Creating from JSON5 Array String
```java
String arrayString = """
[
    'Reading',
    'Watching movies',
    {
        name: 'Travel',
        frequency: 'Once a month',
        cost: 50000
    },
    // Comments can be used for the last item too
    'Exercise'
]
""";

JSON5Array hobbies = new JSON5Array(arrayString);
```

#### 3. Data Retrieval
```java
// Retrieval by index
String firstHobby = hobbies.getString(0);
JSON5Object hobbyDetail = hobbies.getJSON5Object(2);

// Array size
int size = hobbies.size();

// Iteration
for (int i = 0; i < hobbies.size(); i++) {
    Object item = hobbies.get(i);
    System.out.println("Item " + i + ": " + item);
}

// Using enhanced for loop
for (Object item : hobbies) {
    System.out.println("Item: " + item);
}
```

### Advanced JSON5 Features

#### 1. Using Comments
```java
JSON5Object config = new JSON5Object();
config.put("port", 8080);
config.put("host", "localhost");

// Add comments to keys
config.setCommentForKey("port", "Server port number");
config.setCommentAfterValue("host", "Host for development environment");

// Add comments to the entire object
config.setHeaderComment("Server configuration file");
config.setFooterComment("End of configuration");

System.out.println(config.toString(WritingOptions.json5Pretty()));
```

#### 2. Path-based Access (XPath style)
```java
JSON5Object data = new JSON5Object();
JSON5Array users = new JSON5Array();

JSON5Object user1 = new JSON5Object();
user1.put("name", "Kim Chul-soo");
user1.put("email", "kim@example.com");
users.put(user1);

data.put("users", users);

// Access values using paths
String firstUserName = data.getString("$.users[0].name");
String firstUserEmail = data.getString("$.users[0].email");

// Set values using paths
data.put("$.users[0].department", "Development Team");
data.put("$.users[1]", new JSON5Object().put("name", "Lee Young-hee"));
```

---

## 🔄 Complete Serialization/Deserialization Guide

### Basic Concepts

JSON5 Serializer supports bidirectional conversion between Java objects and JSON5. It provides fine-grained control through annotations and offers advanced features similar to Jackson.

### 1. Basic Annotations and Modes

#### Selecting Annotation Mode
JSON5 Serializer works **without annotations** similar to Jackson.

```java
// 📝 Method 1: Use without annotations (Jackson style)
public class User {
    private String name;    // Automatically serialized/deserialized
    private int age;        // Automatically serialized/deserialized
    
    public User() {}
    
    // Getter/Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// 📝 Method 2: Using optional annotations
@JSON5Type
public class User {
    @JSON5Value(comment = "User name")
    private String name;
    
    @JSON5Value(ignore = true)
    private String password;    // Excluded from serialization
    
    private int age;            // Included even without annotation
}

// 📝 Method 3: Strict mode (explicit = true)
@JSON5Type(explicit = true)
public class User {
    @JSON5Value
    private String name;        // Has annotation → included
    
    private int age;            // No annotation → excluded
    private String email;       // No annotation → excluded
    
    @JSON5Value
    private boolean isActive;   // Has annotation → included
}
```

#### @JSON5Type - Class Annotation
```java
@JSON5Type
public class User {
    // Default mode: All fields automatically processed
}

@JSON5Type(explicit = true)
public class User {
    // Strict mode: Only fields with @JSON5Value are processed
}

@JSON5Type(comment = "User information", commentAfter = "End of user information")
public class User {
    // Add comments to the class
}
```

#### @JSON5Value - Field Annotation
```java
@JSON5Type
public class User {
    @JSON5Value
    private String name;
    
    @JSON5Value(key = "user_id", comment = "User ID")
    private String id;
    
    @JSON5Value(ignore = true)
    private String password; // Excluded from serialization
    
    private String internalData; // Included if explicit=false, excluded if explicit=true
}
```

#### @JSON5ValueGetter/@JSON5ValueSetter - Method Annotations
```java
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    private List<String> hobbies;
    
    // Create virtual field with Getter method
    @JSON5ValueGetter(comment = "Full name")
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Use custom key name
    @JSON5ValueGetter(key = "hobby_list", comment = "Hobby list")
    public List<String> getUserHobbies() {
        return hobbies;
    }
    
    // Handle deserialization with Setter method
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

#### @ObtainTypeValue - Handling Generic/Abstract Types
```java
@JSON5Type
public class Container<T> {
    @JSON5Value
    private T data;
    
    @JSON5Value
    private String type;
    
    // Determine the actual type of generic type
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

### 2. Basic Serialization/Deserialization

#### Using Static Methods (Traditional Approach)
```java
@JSON5Type
public class User {
    @JSON5Value
    private String name;
    
    @JSON5Value
    private int age;
    
    // Default constructor required
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

// Serialization
User user = new User("Hong Gil-dong", 30);
JSON5Object json = JSON5Serializer.toJSON5Object(user);

// Deserialization
User restored = JSON5Serializer.fromJSON5Object(json, User.class);
```

#### Using Fluent API (Recommended Approach)
```java
// Basic usage
JSON5Serializer serializer = JSON5Serializer.builder().build();

// Serialization
JSON5Object json = serializer.forSerialization()
    .withWritingOptions(WritingOptions.json5Pretty())
    .includeNullValues()
    .serialize(user);

// Deserialization
User restored = serializer.forDeserialization()
    .ignoreErrors()
    .withStrictTypeChecking(false)
    .deserialize(json, User.class);
```

### 3. Constructor-based Deserialization

#### Basic Usage
```java
@JSON5Type
public class User {
    private final String name;
    private final int age;
    private final String email;
    
    // Specify constructor with @JSON5Creator
    @JSON5Creator
    public User(@JSON5Property("name") String name,
                @JSON5Property("age") int age,
                @JSON5Property("email") String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }
    
    // Only getters needed (immutable object)
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
}
```

#### Nested Path Access
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

// JSON structure
{
    "name": "Hong Gil-dong",
    "contact": {
        "email": "hong@example.com",
        "phone": "010-1234-5678"
    },
    "work": {
        "department": "Development Team",
        "position": "Senior Developer"
    },
    "address": {
        "city": "Seoul",
        "district": "Gangnam-gu"
    }
}
```

#### Array Index Access
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

// JSON structure
{
    "team": {
        "name": "Development Team",
        "leader": "Kim Team Lead"
    },
    "members": [
        {"name": "Lee Developer", "role": "Senior"},
        {"name": "Park Newcomer", "role": "Junior"},
        {"name": "Choi Experienced", "role": "Senior"}
    ]
}
```

#### Constructor Priority and Required Fields
```java
@JSON5Type
public class FlexibleUser {
    private final String name;
    private final int age;
    private final String type;
    
    // Default constructor (lower priority)
    @JSON5Creator
    public FlexibleUser(@JSON5Property("name") String name,
                       @JSON5Property("age") int age) {
        this.name = name;
        this.age = age;
        this.type = "basic";
    }
    
    // Higher priority constructor
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

### 4. Polymorphic Deserialization

#### Basic Polymorphism Handling
```java
// Parent class/interface
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

// Implementation classes
@JSON5Type
public class Dog extends Animal {
    @JSON5Value
    private String breed;
    
    @Override
    public void makeSound() { System.out.println("Woof!"); }
    
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
}

@JSON5Type
public class Cat extends Animal {
    @JSON5Value
    private boolean indoor;
    
    @Override
    public void makeSound() { System.out.println("Meow!"); }
    
    public boolean isIndoor() { return indoor; }
    public void setIndoor(boolean indoor) { this.indoor = indoor; }
}

// Usage example
JSON5Object dogJson = new JSON5Object();
dogJson.put("type", "dog");
dogJson.put("name", "Buddy");
dogJson.put("breed", "Jindo");

Animal animal = JSON5Serializer.fromJSON5Object(dogJson, Animal.class);
// Result: Dog instance is created
```

#### Nested Type Information
```java
@JSON5TypeInfo(property = "vehicle.type")
@JSON5SubType(value = Car.class, name = "car")
@JSON5SubType(value = Motorcycle.class, name = "motorcycle")
public interface Vehicle {
    void start();
}

// JSON structure
{
    "owner": "Hong Gil-dong",
    "vehicle": {
        "type": "car",
        "brand": "Hyundai",
        "model": "Sonata"
    }
}
```

#### Using Existing Property as Type Information
```java
@JSON5TypeInfo(property = "status", include = TypeInclusion.EXISTING_PROPERTY)
@JSON5SubType(value = ActiveUser.class, name = "active")
@JSON5SubType(value = InactiveUser.class, name = "inactive")
@JSON5SubType(value = PendingUser.class, name = "pending")
public abstract class User {
    @JSON5Value
    protected String status; // This field value is also used for type determination
    
    @JSON5Value
    protected String name;
}

@JSON5Type
public class ActiveUser extends User {
    @JSON5Value
    private String lastLoginDate;
}

// The value of the status field ("active") in JSON is used for type determination
{
    "status": "active",
    "name": "Hong Gil-dong",
    "lastLoginDate": "2024-01-15"
}
```

#### Specifying Default Implementation
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

// If type information is missing or doesn't match, deserialize as GenericPayment
{
    "amount": 10000,
    "currency": "KRW"
    // No type field -> Uses GenericPayment
}
```

### 5. Custom Value Providers

#### Basic Usage
```java
@JSON5ValueProvider
public class UserId {
    private final String id;
    
    // Deserialization: String → UserId
    @JSON5ValueConstructor
    public UserId(String id) {
        this.id = id;
    }
    
    // Serialization: UserId → String
    @JSON5ValueExtractor
    public String getId() {
        return id;
    }
}

// Usage class
@JSON5Type
public class User {
    @JSON5Value
    private UserId userId;  // UserId object is serialized/deserialized as String
    
    @JSON5Value
    private String name;
}
```

#### Complex Type Conversion
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

#### Null Handling Configuration
```java
@JSON5ValueProvider
public class SafeWrapper {
    private String value;
    
    @JSON5ValueConstructor(onNull = NullHandling.EMPTY_OBJECT)
    public SafeWrapper(String value) {
        this.value = value != null ? value : "default value";
    }
    
    @JSON5ValueExtractor(onNull = NullHandling.EXCEPTION)
    public String getValue() {
        if (value == null) {
            throw new JSON5SerializerException("Value cannot be null");
        }
        return value;
    }
}
```

### 6. Advanced Collection and Map Handling

#### Advanced Map Features

**Support for Various Key Types**
```java
@JSON5Type
public class AdvancedMaps {
    // Enum Key support
    @JSON5Value
    private Map<UserRole, List<String>> enumKeyMap;
    
    // Primitive Key support
    @JSON5Value
    private Map<Integer, User> intKeyMap;
    
    // @JSON5ValueProvider Key support
    @JSON5Value
    private Map<UserId, UserProfile> customKeyMap;
}

public enum UserRole { ADMIN, USER, GUEST }

// JSON result
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

**Collection Support as Map Values**
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

#### Complete Generic Type Support with TypeReference

**Basic TypeReference Usage**
```java
// Limitation of traditional approach
Map<UserRole, List<String>> result1 = deserializer.deserializeWithKeyType(
    json, UserRole.class, List.class); // ❌ Type information of List elements is lost

// Preserving complete type information with TypeReference
Map<UserRole, List<String>> result2 = deserializer.deserializeWithTypeReference(json,
    new JSON5TypeReference<Map<UserRole, List<String>>>() {}); // ✅ Complete type information
```

**Perfect Support for Complex Nested Types**
```java
// Now even complex types like this are fully supported!
Map<String, List<Map<Car, Brand>>> ultraComplex;

// Usage example
@JSON5Type
public class ComplexContainer {
    // List<Map<String, User>> - List containing maps
    @JSON5Value
    private List<Map<String, User>> userMaps;
    
    // Map<UserRole, List<Map<String, Permission>>> - 3-level nesting
    @JSON5Value  
    private Map<UserRole, List<Map<String, Permission>>> complexStructure;
    
    // Map<String, Set<List<Category>>> - All Collection type combinations
    @JSON5Value
    private Map<String, Set<List<Category>>> megaComplex;
}

// Serialization/Deserialization with TypeReference
JSON5Serializer serializer = JSON5Serializer.getInstance();

// Serialization
JSON5Object json = (JSON5Object) JSON5Serializer.toJSON5WithTypeReference(complexData,
    new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});

// Deserialization - Preserving all type information perfectly
Map<UserRole, List<Map<String, User>>> restored = 
    JSON5Serializer.fromJSON5ObjectWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
```

**Collection TypeReference Support**
```java
// Support for complex Collections like List<Map<String, Integer>>
List<Map<String, Integer>> complexList = new ArrayList<>();
Map<String, Integer> item1 = new HashMap<>();
item1.put("score", 95);
item1.put("rank", 1);
complexList.add(item1);

// Serialization/Deserialization with complete type information
JSON5Array json = (JSON5Array) JSON5Serializer.toJSON5WithTypeReference(complexList,
    new JSON5TypeReference<List<Map<String, Integer>>>() {});

List<Map<String, Integer>> restored = 
    JSON5Serializer.fromJSON5ArrayWithTypeReference(json,
        new JSON5TypeReference<List<Map<String, Integer>>>() {});
```

**Unified API Methods**
```java
// TypeReference deserialization from JSON5Object
Map<UserRole, List<String>> mapResult = JSON5Serializer.fromJSON5ObjectWithTypeReference(
    jsonObject, new JSON5TypeReference<Map<UserRole, List<String>>>() {});

// TypeReference deserialization from JSON5Array  
List<Map<String, User>> listResult = JSON5Serializer.fromJSON5ArrayWithTypeReference(
    jsonArray, new JSON5TypeReference<List<Map<String, User>>>() {});

// Direct TypeReference parsing from string
Map<String, List<Integer>> directResult = JSON5Serializer.parseWithTypeReference(
    jsonString, new JSON5TypeReference<Map<String, List<Integer>>>() {});

// Serialization with TypeReference
Object serialized = JSON5Serializer.toJSON5WithTypeReference(complexObject,
    new JSON5TypeReference<Map<String, List<Map<String, User>>>>() {});
```

### 7. Advanced Settings and Options

#### Advanced Configuration Using Builder
```java
JSON5Serializer serializer = JSON5Serializer.builder()
    .ignoreUnknownProperties()     // Ignore unknown properties
    .enableSchemaCache()           // Use schema cache
    .withErrorHandling(true)       // Enable error handling
    .build();
```

#### Serialization Options
```java
JSON5Object json = serializer.forSerialization()
    .withWritingOptions(WritingOptions.json5Pretty())  // Pretty output
    .includeNullValues()                               // Include null values
    .ignoreFields("password", "internalId")            // Ignore specific fields
    .serialize(user);
```

#### Deserialization Options
```java
User user = serializer.forDeserialization()
    .ignoreErrors()                 // Ignore errors
    .withStrictTypeChecking(false)  // Disable strict type checking
    .withDefaultValue(new User())   // Set default value
    .deserialize(json, User.class);
```

### 8. Practical Usage Examples

#### Configuration File Handling
```java
@JSON5Type(comment = "Application Configuration")
public class AppConfig {
    @JSON5Value(comment = "Server settings")
    private ServerConfig server;
    
    @JSON5Value(comment = "Database settings")
    private DatabaseConfig database;
    
    @JSON5Value(comment = "Logging settings")
    private LoggingConfig logging;
}

@JSON5Type
public class ServerConfig {
    @JSON5Value(comment = "Server port")
    private int port = 8080;
    
    @JSON5Value(comment = "Host address")
    private String host = "localhost";
    
    @JSON5Value(comment = "Use SSL")
    private boolean ssl = false;
}

// config.json5 file
/*
// Application Configuration
{
    // Server settings
    server: {
        // Server port
        port: 8080,
        // Host address
        host: 'localhost',
        // Use SSL
        ssl: false
    },
    // Database settings
    database: {
        url: 'jdbc:mysql://localhost:3306/mydb',
        username: 'user',
        password: 'pass',
        // Connection pool settings
        pool: {
            minSize: 5,
            maxSize: 20,
        }
    }
}
*/
```

#### API Response Handling
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

#### Complex Nested Type Practical Example
```java
@JSON5Type
public class GameConfiguration {
    // Map<GameMode, List<Map<ItemType, ItemConfig>>>
    @JSON5Value(comment = "Item configuration by game mode")
    private Map<GameMode, List<Map<ItemType, ItemConfig>>> modeItemConfigs;
    
    // List<Map<String, List<SkillTree>>>
    @JSON5Value(comment = "Skill trees by character")
    private List<Map<String, List<SkillTree>>> characterSkills;
    
    // Map<String, Set<List<Achievement>>>
    @JSON5Value(comment = "Achievement groups by category")
    private Map<String, Set<List<Achievement>>> achievementGroups;
}

public enum GameMode { NORMAL, HARD, EXPERT }
public enum ItemType { WEAPON, ARMOR, CONSUMABLE }

// Process with TypeReference to preserve complete type information
GameConfiguration config = new GameConfiguration();
// ... set data

// Serialization - preserving all generic type information
JSON5Object configJson = (JSON5Object) JSON5Serializer.toJSON5WithTypeReference(config,
    new JSON5TypeReference<GameConfiguration>() {});

// Save configuration file in JSON5 format
configJson.toString(WritingOptions.json5Pretty());
/*
// Game configuration file
{
    // Item configuration by game mode
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
    // Skill trees by character
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

// Deserialization - restoring complete type information
GameConfiguration restoredConfig = 
    JSON5Serializer.fromJSON5ObjectWithTypeReference(configJson,
        new JSON5TypeReference<GameConfiguration>() {});

// Now all generic types are perfectly preserved
Map<GameMode, List<Map<ItemType, ItemConfig>>> itemConfigs = 
    restoredConfig.getModeItemConfigs();
ItemConfig weaponConfig = itemConfigs.get(GameMode.NORMAL).get(0).get(ItemType.WEAPON);
```

#### Constructor-based Immutable Object Pattern
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
    
    // Only getters provided (immutable object)
    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public List<String> getCategories() { return new ArrayList<>(categories); }
    public ProductDetails getDetails() { return details; }
}

// JSON structure
{
    "id": "PROD-001",
    "name": "Gaming Laptop",
    "price": 1500000,
    "categories": ["Computers", "Gaming", "Electronics"],
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

### 9. Troubleshooting Guide

#### Common Errors and Solutions

**1. "No default constructor found" Error**
```java
// Problem: No default constructor
@JSON5Type
public class User {
    public User(String name) { ... } // Only parameterized constructor exists
}

// Solution 1: Add default constructor
@JSON5Type
public class User {
    private User() {} // Private is also acceptable
    public User(String name) { ... }
}

// Solution 2: Use @JSON5Creator
@JSON5Type
public class User {
    @JSON5Creator
    public User(@JSON5Property("name") String name) { ... }
}
```

**2. "Circular reference detected" Error**
```java
// Problem: Circular reference
@JSON5Type
public class Department {
    @JSON5Value
    private List<Employee> employees;
}

@JSON5Type  
public class Employee {
    @JSON5Value
    private Department department; // Circular reference!
}

// Solution: Remove one reference or use ignore
@JSON5Type
public class Employee {
    @JSON5Value(ignore = true)
    private Department department; // Excluded from serialization
}
```

**3. Generic Type Handling Issues**
```java
// Problem: Using raw type
@JSON5Value
private List userList; // No generic information

// Solution 1: Specify generic type
@JSON5Value
private List<User> userList;

// Solution 2: Use TypeReference (for complex nested types)
Map<UserRole, List<User>> complexType = JSON5Serializer.fromJSON5ObjectWithTypeReference(
    json, new JSON5TypeReference<Map<UserRole, List<User>>>() {});
```

**4. TypeReference ClassCastException Issues**
```java
// Problem: Type casting errors in complex nested types
Map<UserRole, List<Map<String, User>>> complexData; // ClassCastException possible

// Solution: Ensure type safety with TypeReference
Map<UserRole, List<Map<String, User>>> safeData = 
    deserializer.deserializeWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});
```

**5. Class Serialization Issues Without Annotations**
```java
// Problem: Class without annotations
public class SimpleUser {
    private String name; // Seems like it won't be serialized but...
    private int age;
}

// It actually works! (Same as Jackson)
SimpleUser user = new SimpleUser();
user.setName("John");
user.setAge(30);

JSON5Object json = JSON5Serializer.toJSON5Object(user); // ✅ Works fine
// {"name":"John","age":30}

// For strict control, use explicit mode
@JSON5Type(explicit = true)
public class StrictUser {
    @JSON5Value
    private String name;     // Included
    
    private int age;         // Excluded (no annotation)
}
```

#### Debugging Tips

**1. Check Serialization Results**
```java
JSON5Object json = JSON5Serializer.toJSON5Object(object);
System.out.println("Serialization result:");
System.out.println(json.toString(WritingOptions.json5Pretty()));
```

**2. Test with Error-Ignore Mode**
```java
// Identify which field is causing issues
User user = JSON5Serializer.getInstance()
    .forDeserialization()
    .ignoreErrors() // Ignore errors and proceed
    .deserialize(json, User.class);
```

**3. Check Type Information**
```java
// Check type information in polymorphic handling
if (json.has("type")) {
    System.out.println("Type info: " + json.getString("type"));
} else {
    System.out.println("No type info - default implementation will be used");
}
```

### 10. Best Practices

#### 1. Annotation Usage Guide
```java
// ✅ Good example
@JSON5Type(comment = "User information")
public class User {
    @JSON5Value(comment = "User ID")
    private String id;
    
    @JSON5Value(key = "user_name", comment = "User name")
    private String name;
    
    @JSON5Value(ignore = true)
    private String password; // Exclude sensitive information
}

// ❌ Bad example
public class User {
    private String id; // Missing annotation
    @JSON5Value
    private String password; // Including sensitive information
}
```

#### 2. Complex Type Handling Guide
```java
// ✅ Good example: Complete type safety with TypeReference
Map<UserRole, List<Map<String, User>>> complexData = 
    deserializer.deserializeWithTypeReference(json,
        new JSON5TypeReference<Map<UserRole, List<Map<String, User>>>>() {});

// ✅ Good example: Specific type specification
@JSON5Value
private List<String> tags;              // Specific type

@JSON5Value  
private Map<String, User> userMap;      // String key usage

@JSON5Value
private Map<UserRole, List<String>> enumKeyMap; // Enum key support

// ❌ Bad example: Raw type usage
@JSON5Value
private List tags;                      // Raw type

@JSON5Value
private Map userMap;                    // Raw type

// ❌ Bad example: Unsupported key type
@JSON5Value
private Map<CustomObject, String> invalidKeyMap; // Unsupported key type
```

#### 3. Method Annotation Usage
```java
// ✅ Good example: Creating virtual fields with Getter/Setter
@JSON5Type
public class User {
    private String firstName;
    private String lastName;
    
    @JSON5ValueGetter(comment = "Full name")
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

// JSON result
{
    "firstName": "John",
    "lastName": "Doe", 
    "fullName": "John Doe",        // Virtual field created by Getter
    "display_name": "Mr/Ms. John Doe"  // Custom key name
}
```

#### 4. Annotation Mode Usage Strategies
```java
// Scenario 1: Simple data class → Use without annotations
public class SimpleConfig {
    private String host = "localhost";
    private int port = 8080;
    private boolean ssl = false;
    // Just add Getters/Setters
}

// Scenario 2: Need to control some fields → Selective annotations
@JSON5Type
public class UserConfig {
    private String username;     // Automatically included
    private String email;        // Automatically included
    
    @JSON5Value(ignore = true)
    private String password;     // Excluded
    
    @JSON5Value(comment = "Last login")
    private LocalDateTime lastLogin; // Added comment
}

// Scenario 3: Need strict control → Explicit mode
@JSON5Type(explicit = true)
public class SecurityConfig {
    @JSON5Value
    private String publicKey;    // Explicitly included
    
    @JSON5Value  
    private String algorithm;    // Explicitly included
    
    private String privateKey;   // Excluded (security)
    private String salt;         // Excluded (security)
    private String internalConfig; // Excluded (internal use)
}
```

#### 5. Exception Handling
```java
// ✅ Good example: Safe deserialization
public User parseUserSafely(String jsonString) {
    try {
        JSON5Object json = new JSON5Object(jsonString);
        return JSON5Serializer.fromJSON5Object(json, User.class);
    } catch (JSON5SerializerException e) {
        logger.error("User parsing failed: " + e.getMessage());
        return new User(); // Return default value
    }
}

// ✅ Good example: Safe TypeReference usage
public Map<String, List<User>> parseComplexDataSafely(JSON5Object json) {
    try {
        return JSON5Serializer.fromJSON5ObjectWithTypeReference(json,
            new JSON5TypeReference<Map<String, List<User>>>() {});
    } catch (JSON5SerializerException e) {
        logger.error("Complex data parsing failed: " + e.getMessage());
        return new HashMap<>(); // Return empty Map
    }
}
```

#### 6. Performance Optimization Tips
```java
// ✅ Good example: Utilize schema cache
JSON5Serializer serializer = JSON5Serializer.builder()
    .enableSchemaCache()  // Enable schema cache
    .build();

// Performance improvement for repeated processing
List<User> users = getUsers();
for (User user : users) {
    JSON5Object json = serializer.serialize(user);
    // Uses cached schema from second iteration
}

// ✅ Good example: Appropriate configuration combination
User user = serializer.forDeserialization()
    .ignoreErrors()                // Ignore errors
    .withStrictTypeChecking(false) // Disable strict type checking  
    .deserialize(json, User.class);
```

---

## 🎯 Summary

JSON5 Serializer is a powerful library specialized for configuration file handling:

### Key Advantages
- **JSON5 Support**: Easy configuration file creation with comments, trailing commas, unquoted keys
- **Advanced Features**: Jackson-level constructor-based deserialization, polymorphism handling, custom value providers, TypeReference
- **Complex Nested Type Support**: Perfect support for `List<Map<Car, Brand>>`, `Map<String, List<Map<String, User>>>`, etc.
- **Flexible API**: Support for both static methods and Fluent API
- **Annotation Choice**: Works without annotations, with strict control using explicit mode when needed

### Usage Recommendations
- **✅ Configuration Files**: Application settings, environment configurations
- **✅ Development Tools**: Build scripts, developer tool settings
- **✅ Complex Object Structures**: Domain models requiring polymorphism
- **❌ REST APIs**: Standard JSON recommended
- **❌ System Data Exchange**: Use standard JSON for compatibility

This guide helps you effectively utilize all features of JSON5 Serializer to implement maintainable configuration files and powerful object serialization.
