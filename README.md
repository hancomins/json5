# JSON5 User Guide

# 한국어 버전 바로가기
 - [JSON5 Serializer 사용자 가이드 (한국어)](README.KO.md)

## 📋 Overview

JSON5 Serializer is a powerful JSON5 serialization/deserialization library for Java 8+. JSON5 is a superset of JSON that supports **comments, trailing commas, and unquoted keys**, making it **ideal for configuration files**.

### ✅ Key Advantages
- **Optimized for configuration files**: Easy configuration file creation with comment support and flexible syntax
- **Jackson-level advanced features**: Constructor-based deserialization, polymorphism, custom value providers
- **XPath-style path access**: Nested path access like `users[0].profile.email`

### ⚠️ Important Notes
- **Not suitable as data format**: Standard JSON is recommended for inter-system data exchange
- **Not recommended for network APIs**: Standard JSON is more appropriate for REST APIs, etc.

---

## 🚀 Basic Setup

### Add Gradle Dependency
```groovy
dependencies {
    implementation 'io.github.hancomins:json5:1.x.x' // Replace with the latest version
}
```

### Basic Imports
```java
import com.hancomins.json5.*;
import com.hancomins.json5.serializer.*;
import com.hancomins.json5.options.*;
```

---

## 📦 JSON5Object and JSON5Array Basic Usage

### JSON5Object Basic Operations

#### 1. Object Creation and Data Addition
```java
// Create empty object
JSON5Object user = new JSON5Object();

// Add basic type data
user.put("name", "John Doe");
user.put("age", 30);
user.put("isActive", true);
user.put("score", 95.5);

// Add nested object
JSON5Object profile = new JSON5Object();
profile.put("email", "john@example.com");
profile.put("department", "Engineering");
user.put("profile", profile);

System.out.println(user);
// {"name":"John Doe","age":30,"isActive":true,"score":95.5,"profile":{"email":"john@example.com","department":"Engineering"}}
```

#### 2. Creating Object from JSON5 String
```java
// JSON5 format (supports comments, trailing commas, unquoted keys)
String json5String = """
{
    // User basic information
    name: 'John Doe',
    age: 30,
    hobbies: ['reading', 'movies', 'travel',], // trailing comma allowed
    /* Contact information */
    contact: {
        email: 'john@example.com',
        phone: '555-1234-5678'
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
boolean isActive = user.getBoolean("isActive", false); // with default value

// Nested object retrieval
JSON5Object profile = user.getJSON5Object("profile");
String email = profile.getString("email");

// Safe retrieval (null handling)
String department = user.getJSON5Object("profile").getString("department", "TBD");

// Null check
if (user.has("profile")) {
    JSON5Object userProfile = user.getJSON5Object("profile");
    // Process profile information
}
```

### JSON5Array Basic Operations

#### 1. Array Creation and Data Addition
```java
// Create empty array
JSON5Array hobbies = new JSON5Array();

// Add data
hobbies.put("reading");
hobbies.put("movies");
hobbies.put("travel");

// Add multiple data at once
hobbies.put("hiking", "cooking", "gaming");

// Add object to array
JSON5Object hobby1 = new JSON5Object();
hobby1.put("name", "reading");
hobby1.put("frequency", "daily");
hobbies.put(hobby1);
```

#### 2. Creating from JSON5 Array String
```java
String arrayString = """
[
    'reading',
    'movies',
    {
        name: 'travel',
        frequency: 'monthly',
        cost: 500
    },
    // Last item can also have comments
    'exercise'
]
""";

JSON5Array hobbies = new JSON5Array(arrayString);
```

#### 3. Data Retrieval
```java
// Retrieve by index
String firstHobby = hobbies.getString(0);
JSON5Object hobbyDetail = hobbies.getJSON5Object(2);

// Array size
int size = hobbies.size();

// Iterate through array
for (int i = 0; i < hobbies.size(); i++) {
    Object item = hobbies.get(i);
    System.out.println("Item " + i + ": " + item);
}

// Enhanced for loop
for (Object item : hobbies) {
    System.out.println("Item: " + item);
}
```

### JSON5 Advanced Features

#### 1. Comment Handling
```java
JSON5Object config = new JSON5Object();
config.put("port", 8080);
config.put("host", "localhost");

// Add comments to keys
config.setCommentForKey("port", "Server port number");
config.setCommentAfterValue("host", "Development environment host");

// Add comments to entire object
config.setHeaderComment("Server configuration file");
config.setFooterComment("End of configuration");

System.out.println(config.toString(WritingOptions.json5Pretty()));
```

#### 2. Path-based Access (XPath Style)
```java
JSON5Object data = new JSON5Object();
JSON5Array users = new JSON5Array();

JSON5Object user1 = new JSON5Object();
user1.put("name", "John Smith");
user1.put("email", "john@example.com");
users.put(user1);

data.put("users", users);

// Access values by path
String firstUserName = data.getString("$.users[0].name");
String firstUserEmail = data.getString("$.users[0].email");

// Set values by path
data.put("$.users[0].department", "Engineering");
data.put("$.users[1]", new JSON5Object().put("name", "Jane Doe"));
```

---

## 🔄 Complete Serialization/Deserialization Guide

### Basic Concepts

JSON5 Serializer supports bidirectional conversion between Java objects and JSON5. Fine-grained control is possible through annotations, providing Jackson-like advanced features.

### 1. Basic Annotations

#### @JSON5Type - Class Annotation
```java
@JSON5Type
public class User {
    // Mark class for serialization/deserialization
}

@JSON5Type(comment = "User information", commentAfter = "End of user info")
public class User {
    // Add comments to class
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
    private String password; // Exclude from serialization
    
    private String internalData; // Excluded without annotation
}
```

### 2. Basic Serialization/Deserialization

#### Using Static Methods (Legacy Way)
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
    
    // Getters/Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

// Serialization
User user = new User("John Doe", 30);
JSON5Object json = JSON5Serializer.toJSON5Object(user);

// Deserialization
User restored = JSON5Serializer.fromJSON5Object(json, User.class);
```

#### Using Fluent API (Recommended)
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
    "name": "John Doe",
    "contact": {
        "email": "john@example.com",
        "phone": "555-1234-5678"
    },
    "work": {
        "department": "Engineering",
        "position": "Senior Developer"
    },
    "address": {
        "city": "New York",
        "state": "NY"
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
        "name": "Engineering Team",
        "leader": "John Manager"
    },
    "members": [
        {"name": "Alice Developer", "role": "Senior"},
        {"name": "Bob Junior", "role": "Junior"},
        {"name": "Carol Senior", "role": "Senior"}
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

#### Basic Polymorphism
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
dogJson.put("breed", "Golden Retriever");

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
    "owner": "John Doe",
    "vehicle": {
        "type": "car",
        "brand": "Toyota",
        "model": "Camry"
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

// The status field value ("active") in JSON is used for type determination
{
    "status": "active",
    "name": "John Doe",
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

// When type info is missing or doesn't match, GenericPayment is used
{
    "amount": 100.0,
    "currency": "USD"
    // no type field -> GenericPayment used
}
```

### 5. Custom Value Provider

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
    private UserId userId;  // UserId object serialized/deserialized as String
    
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
        this.value = value != null ? value : "default";
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

### 6. Collection and Map Handling

#### List Serialization/Deserialization
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

// Usage
Team team = new Team();
team.setName("Engineering Team");
team.setMembers(Arrays.asList(
    new User("John Developer", 30),
    new User("Jane Frontend", 28)
));
team.setSkills(Arrays.asList("Java", "JavaScript", "Python"));

JSON5Object json = JSON5Serializer.toJSON5Object(team);
Team restored = JSON5Serializer.fromJSON5Object(json, Team.class);
```

#### Map Serialization/Deserialization
```java
@JSON5Type
public class UserManager {
    @JSON5Value(key = "users")
    private Map<String, User> userMap = new HashMap<>();
    
    @JSON5Value
    private Map<String, List<String>> rolePermissions = new HashMap<>();
}

// Note: Map keys must be String
// Values can be basic types, @JSON5Type classes, Lists, etc.
```

### 7. Advanced Configuration and Options

#### Advanced Configuration with Builder
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

### 8. Real-world Examples

#### Configuration File Handling
```java
@JSON5Type(comment = "Application configuration")
public class AppConfig {
    @JSON5Value(comment = "Server configuration")
    private ServerConfig server;
    
    @JSON5Value(comment = "Database configuration")
    private DatabaseConfig database;
    
    @JSON5Value(comment = "Logging configuration")
    private LoggingConfig logging;
}

@JSON5Type
public class ServerConfig {
    @JSON5Value(comment = "Server port")
    private int port = 8080;
    
    @JSON5Value(comment = "Host address")
    private String host = "localhost";
    
    @JSON5Value(comment = "SSL enabled")
    private boolean ssl = false;
}

// config.json5 file
/*
// Application configuration
{
    // Server configuration
    server: {
        // Server port
        port: 8080,
        // Host address
        host: 'localhost',
        // SSL enabled
        ssl: false
    },
    // Database configuration
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

#### Complex Business Objects
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

// Usage example
Order order = new Order();
order.setOrderId("ORD-2024-001");
order.setItems(Arrays.asList(
    new OrderItem("PROD-001", "Laptop", 1, new BigDecimal("1500.00")),
    new OrderItem("PROD-002", "Mouse", 2, new BigDecimal("25.00"))
));

JSON5Object orderJson = JSON5Serializer.toJSON5Object(order);
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
    
    // Only provide getters (immutable object)
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
    "price": 1500.00,
    "categories": ["Computer", "Gaming", "Electronics"],
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
    private User() {} // private is OK
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

// Solution: Remove one side reference or use ignore
@JSON5Type
public class Employee {
    @JSON5Value(ignore = true)
    private Department department; // Exclude from serialization
}
```

**3. Generic Type Handling Issues**
```java
// Problem: Raw type usage
@JSON5Value
private List userList; // No generic information

// Solution: Specify generic type
@JSON5Value
private List<User> userList;
```

#### Debugging Tips

**1. Check Serialization Results**
```java
JSON5Object json = JSON5Serializer.toJSON5Object(object);
System.out.println("Serialization result:");
System.out.println(json.toString(WritingOptions.json5Pretty()));
```

**2. Test with Error Ignore Mode**
```java
// Check which fields are causing problems
User user = JSON5Serializer.getInstance()
    .forDeserialization()
    .ignoreErrors() // Ignore errors and proceed
    .deserialize(json, User.class);
```

**3. Verify Type Information**
```java
// Check type information in polymorphic handling
if (json.has("type")) {
    System.out.println("Type info: " + json.getString("type"));
} else {
    System.out.println("No type info - default implementation used");
}
```

### 10. Best Practices

#### 1. Annotation Usage Guidelines
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

#### 2. Prefer Immutable Objects
```java
// ✅ Good example: Immutable object
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

#### 3. Proper Type Usage
```java
// ✅ Good example
@JSON5Value
private List<String> tags;              // Specific type

@JSON5Value  
private Map<String, User> userMap;      // String key usage

// ❌ Bad example
@JSON5Value
private List tags;                      // Raw type

@JSON5Value
private Map<User, String> reverseMap;   // Non-String key
```

#### 4. Exception Handling
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
```

---

## 🎯 Summary

JSON5 Serializer is a powerful library specialized for configuration file processing:

### Core Advantages
- **JSON5 Support**: Easy configuration file creation with comments, trailing commas, unquoted keys
- **Advanced Features**: Jackson-level constructor-based deserialization, polymorphism, custom value providers
- **Flexible API**: Support for both static methods and Fluent API

### Usage Recommendations
- **✅ Configuration files**: Application settings, environment configuration, etc.
- **✅ Development tools**: Build scripts, developer tool configurations
- **✅ Complex object structures**: Domain models requiring polymorphism
- **❌ REST APIs**: Standard JSON recommended
- **❌ Inter-system data exchange**: Use standard JSON for compatibility

This guide enables you to effectively utilize all features of JSON5 Serializer to implement maintainable configuration files and powerful object serialization.
