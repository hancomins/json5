package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;


public class JSON5TypeSerializerTest {

     @JSON5Type(explicit = true)
    static class TestClassC {
        @JSON5Value
        private String name = "C";



    }

     @JSON5Type(explicit = true)
    public static class TestClassB {
        @JSON5Value
        private String name = "B";

        @JSON5Value
        private TestClassC testC = new TestClassC();

    }

     @JSON5Type(explicit = true)
    public static class TestClassA {
        @JSON5Value
        private String name = "A";

        @JSON5Value("testB.testC.float")
        private float pi = 3.14f;

        @JSON5Value("testB.testB.testC.pi")
        private float pi2 = 3.14f;
        @JSON5Value("value.int")
        private int value = 1;

        @JSON5Value
        private TestClassB testB = new TestClassB();

        @JSON5Value("testB.testB")
        private TestClassB testBInTestB = new TestClassB();

        @JSON5Value("testB.testB.testC.nullValue")
        private String nullValue = null;

        @JSON5Value
        private ArrayList<String> strArray = new ArrayList<>();
        @JSON5Value
        private ArrayList<LinkedList<Deque<String>>> strArraySet = new ArrayList<>();

        @JSON5Value
        private ArrayList<TestClassB> testBArray = new ArrayList<>();
        @JSON5Value
        private Deque<ArrayList<TestClassB>> testBInTestBArray = new ArrayDeque<>();

    }



    private static String makeRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            sb.append((char) (Math.random() * 26 + 'a'));
        }
        return sb.toString();
    }


    public void fillRandomValues(ArrayList<LinkedList<Deque<String>>> strArraySet) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int numSets = random.nextInt(5) + 1; // 랜덤한 개수의 HashSet 추가
        for (int i = 0; i < numSets; i++) {
            LinkedList<Deque<String>> hashSet = new LinkedList<>();
            int numDeques = random.nextInt(5) + 1; // 랜덤한 개수의 Deque 추가
            for (int j = 0; j < numDeques; j++) {
                Deque<String> deque = new LinkedList<>();
                int numStrings = random.nextInt(5) + 1; // 랜덤한 개수의 문자열 추가
                for (int k = 0; k < numStrings; k++) {
                    deque.add("Value" + random.nextInt(100)); // 랜덤한 문자열 값 추가
                }
                hashSet.add(deque); // Deque를 HashSet에 추가
            }
            strArraySet.add(hashSet); // HashSet를 ArrayList에 추가
        }
    }

    public void fillRandomTestBCalss(Collection<TestClassB> testBObjectList) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int numSets = random.nextInt(5,10); // 랜덤한 개수의 HashSet 추가
            for (int i = 0; i < numSets; i++) {
                TestClassB testClassB = new TestClassB();
                testClassB.name = makeRandomString(ThreadLocalRandom.current().nextInt(1,50));
                if(random.nextBoolean()) {
                    testClassB.testC = null;
                } else {
                    testClassB.testC = new TestClassC();
                    testClassB.testC.name = makeRandomString(ThreadLocalRandom.current().nextInt(1,50));
                }
                testBObjectList.add(testClassB);
            }
    }

    @Test
    public void serializeTest() {

        String line = "     documentPageCountWrite(config, key, 0, false, log);";
        System.out.println(line.matches("^[\t|' ']{1,}documentPageCountWrite.*"));

        TestClassA testClassA = new TestClassA();

        ArrayList<String> strArray = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            testClassA.strArray.add(makeRandomString(ThreadLocalRandom.current().nextInt(1,50)));
            strArray.add(makeRandomString(ThreadLocalRandom.current().nextInt(1,50)));
        }

        fillRandomTestBCalss(testClassA.testBArray);
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(5,10); i < n; ++i) {
            ArrayList<TestClassB> testBInTestBArray = new ArrayList<>();
            fillRandomTestBCalss(testBInTestBArray);
            testClassA.testBInTestBArray.add(testBInTestBArray);
        }

        this.fillRandomValues(testClassA.strArraySet);

        testClassA.testBInTestB.name="BInB";
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(testClassA);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals("A", json5Object.get("name"));
        assertEquals(1, json5Object.getJSON5Object("value").get("int"));
        assertEquals("B", json5Object.getJSON5Object("testB").get("name"));
        assertEquals("BInB", json5Object.getJSON5Object("testB").getJSON5Object("testB").get("name"));
        assertEquals("C", json5Object.getJSON5Object("testB").getJSON5Object("testB").getJSON5Object("testC").get("name"));
        assertEquals(3.14f, json5Object.getJSON5Object("testB").getJSON5Object("testC").get("float"));
        assertEquals(3.14f, json5Object.getJSON5Object("testB").getJSON5Object("testB").getJSON5Object("testC").get("pi"));
        assertEquals(null, json5Object.getJSON5Object("testB").getJSON5Object("testB").getJSON5Object("testC").get("nullValue"));


        for(int i = 0; i < 10; i++) {
            assertEquals(testClassA.strArray.get(i), json5Object.getJSON5Array("strArray").get(i));
        }

        for(int i = 0; i < testClassA.strArraySet.size(); i++) {
            LinkedList<Deque<String>> linkedList = testClassA.strArraySet.get(i);
            JSON5Array JSON5Array = json5Object.getJSON5Array("strArraySet").getJSON5Array(i);
            assertEquals(linkedList.size(), JSON5Array.size());
            Iterator<Object> json5ArrayIter = JSON5Array.iterator();
            for(Deque<String> deque : linkedList) {
                JSON5Array array2 = (JSON5Array)json5ArrayIter.next();
                assertEquals(deque.size(), array2.size());
                Iterator<Object> array2Iter = array2.iterator();
                for(String str : deque) {
                    assertEquals(str, array2Iter.next());
                }
            }
        }

        assertEquals(testClassA.testBArray.size(), json5Object.getJSON5Array("testBArray").size());

        for(int i = 0; i < testClassA.testBArray.size(); i++) {
            TestClassB testClassB = testClassA.testBArray.get(i);
            JSON5Object json5Object1 = json5Object.getJSON5Array("testBArray").getJSON5Object(i);
            assertEquals(testClassB.name, json5Object1.get("name"));
            if(testClassB.testC == null) {
                assertNull(json5Object1.get("testC"));
            } else {
                assertEquals(testClassB.testC.name, json5Object1.getJSON5Object("testC").get("name"));
            }
        }

        assertEquals(testClassA.testBInTestBArray.size(), json5Object.getJSON5Array("testBInTestBArray").size());

        Iterator<ArrayList<TestClassB>> iter = testClassA.testBInTestBArray.iterator();
        for(int i = 0, n = testClassA.testBInTestBArray.size(); i< n; ++i) {
            ArrayList<TestClassB> testBInTestBArray = iter.next();
            JSON5Array JSON5Array = json5Object.getJSON5Array("testBInTestBArray").getJSON5Array(i);
            assertEquals(testBInTestBArray.size(), JSON5Array.size());
            Iterator<Object> json5ArrayIter = JSON5Array.iterator();
            for(TestClassB testClassB : testBInTestBArray) {
                JSON5Object json5Object1 = (JSON5Object)json5ArrayIter.next();
                assertEquals(testClassB.name, json5Object1.get("name"));
                if(testClassB.testC == null) {
                    assertNull(json5Object1.get("testC"));
                } else {
                    assertEquals(testClassB.testC.name, json5Object1.getJSON5Object("testC").get("name"));
                }
            }

        }
    }








     @JSON5Type(explicit = true)
    private static class TestClassNull {

        @JSON5Value
        private TestClassA testClassA0 = null;
        @JSON5Value
        private TestClassB testClassB1 = new TestClassB();

        @JSON5Value("testClassB1.testC.name")
        private String classCName = "nameC";

        @JSON5Value("testClassB1.testC.int")
        private int d = 2000;


        @JSON5Value("testClassB1.testClassA1")
        private TestClassA testClassA1 = null;

        @JSON5Value("testClassB1.testClassA2")
        private TestClassA testClassA2 = new TestClassA();

    }

    @Test
    public void nullObjectSerializeTest() {

        TestClassNull testClassNull = new TestClassNull();

        testClassNull.testClassB1.testC = null;
        testClassNull.testClassA2.testB = null;
        testClassNull.testClassA2.pi2 = 41.3f;
        testClassNull.testClassA2.testBInTestB = null;

        JSON5Object json5Object = JSON5Serializer.toJSON5Object(testClassNull);
        System.out.println(json5Object.toString(WritingOptions.json5()));

        assertNotNull(json5Object.getJSON5Object("testClassB1").getJSON5Object("testC"));
        assertEquals("nameC", json5Object.getJSON5Object("testClassB1").getJSON5Object("testC").getString("name"));
        assertEquals(2000, json5Object.getJSON5Object("testClassB1").getJSON5Object("testC").getInt("int"));
        assertEquals(2000, json5Object.getJSON5Object("testClassB1").getJSON5Object("testC").getInt("int"));

        assertNull(json5Object.getJSON5Object("testClassB1").get("testClassA1"));
        assertEquals(3.14f, json5Object.getJSON5Object("testClassB1").getJSON5Object("testClassA2").getJSON5Object("testB").getJSON5Object("testC").getFloat("float"));


        assertNull(json5Object.get("testClassA0"));




        //assertNull(json5Object.getObject("testClassB1").get("testC"));
    }


     @JSON5Type(explicit = true)
    public static class Item {
        @JSON5Value
        private String name = "item";
        @JSON5Value
        private int value = 1;
    }

     @JSON5Type(explicit = true)
    public static class ArrayTestClass {
        @JSON5Value("array[10]")
        int array10 = 10;


        @JSON5Value("arrayInArray[10].[3]")
        int array10array3 = 3;

        @JSON5Value("arrayInArray[10][2]")
        int array10array2 = 2;

        @JSON5Value("arrayInArray[10][1].name")
        String name = "name";

        @JSON5Value("arrayInArray[10][0]")
        Item item = new Item();

        @JSON5Value("arrayInArray[10][0].itemInItem")
        Item itemInItem = new Item();

        @JSON5Value("arrayInArray[10][0].stringValue")
        String strValue = "1";


    }

    @Test
    public void arraySerializeTest() {
        ArrayTestClass arrayTestClass = new ArrayTestClass();
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(arrayTestClass);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals(11, json5Object.getJSON5Array("array").size());
        assertEquals(10, json5Object.getJSON5Array("array").getInt(10));

        assertEquals(11, json5Object.getJSON5Array("arrayInArray").size());
        assertEquals(4, json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).size());
        assertEquals(3, json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getInt(3));
        assertEquals(2, json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getInt(2));

        assertEquals("name", json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getJSON5Object(1).getString("name"));
        assertEquals("item", json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getJSON5Object(0).getString("name"));
        assertEquals("item", json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getJSON5Object(0).getJSON5Object("itemInItem").getString("name"));
        assertEquals("1", json5Object.getJSON5Array("arrayInArray").getJSON5Array(10).getJSON5Object(0).getString("stringValue"));



    }


    @JSON5Type(comment = "루트 코멘트", commentAfter = "루트 코멘트 끝.")
    public static class SimpleComment {
        @JSON5Value(key = "key1", comment = "comment1", commentAfterKey = "commentAfterKey1")
        String key1 = "value1";
        @JSON5Value(key = "key2", comment = "comment2", commentAfterKey = "")
        String key2 = "value2";


        @JSON5Value(key = "key3[0]", comment = "comment3", commentAfterKey = "commentAfter3")
        String key3InArray = "value3";
        @JSON5Value(key = "key3[1]", comment = "comment4", commentAfterKey = "commentAfter4")
        String key4InArray = "value4";

        @JSON5Value(key = "key3[2]", comment = "comment5", commentAfterKey = "commentAfter5")
        String key5InArray = null;


        @JSON5Value(key = "key4", comment = "comment6", commentAfterKey = "commentAfter6")
        ArrayList<String> key4 = new ArrayList<>();


    }


    @Test
    public void commentInArrayTest() {

        String obj = "{key3:['value3',\n" +
                "      /* comment4 */'value4'/* commentAfter4 */,\n" +
                "      /* commentNull */null    /* commentAfterNull */,/* comment5 */]/* commentAfter5 */, \n}";

        JSON5Object json5Object = new JSON5Object(obj, WritingOptions.json5());
        System.out.println(json5Object.toString(WritingOptions.json5()));

        JSON5Array JSON5Array = json5Object.getJSON5Array("key3");
        assertEquals(3, JSON5Array.size());
        assertEquals("value3", JSON5Array.get(0));
        assertEquals(" comment4 ", JSON5Array.getCommentForValue(1));
        assertEquals(" commentAfter4 ", JSON5Array.getCommentAfterValue(1));
        assertEquals(" commentNull ", JSON5Array.getCommentForValue(2));
        assertEquals(" commentAfterNull ", JSON5Array.getCommentAfterValue(2));



        assertEquals("value4", JSON5Array.get(1));
        assertNull(JSON5Array.get(2));


        json5Object = new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5());
        JSON5Array = json5Object.getJSON5Array("key3");
        assertEquals(3, JSON5Array.size());
        assertEquals("value3", JSON5Array.get(0));
        assertEquals(" comment4 ", JSON5Array.getCommentForValue(1));
        assertEquals(" commentAfter4 ", JSON5Array.getCommentAfterValue(1));
        assertEquals(" commentNull ", JSON5Array.getCommentForValue(2));
        assertEquals(" commentAfterNull ", JSON5Array.getCommentAfterValue(2));



        assertEquals("value4", JSON5Array.get(1));
        assertNull(JSON5Array.get(2));



    }

    @Test
    public void rootCommentParseTest() {
        String json = "/* root comment */\n" +
                "{}" +
                "/* root comment end */";
        JSON5Object json5Object = new JSON5Object(json, WritingOptions.json5());
        assertEquals(" root comment ", json5Object.getHeaderComment());
        assertEquals(" root comment end ", json5Object.getFooterComment());
    }


    @Test
    public void simpleCommentTest() {
        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array.addAll(new Object[]{"value3", "value4", new JSON5Object()});
        //System.out.println(JSON5Array.toString(WritingOptions.json5()));

        SimpleComment simpleComment = new SimpleComment();
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(simpleComment);
        System.out.println(json5Object.toString(WritingOptions.json5()));

        int referenceNumber = System.identityHashCode(json5Object);

        // 출력
        System.out.println("객체의 레퍼런스 번호: " + referenceNumber);

        assertEquals("루트 코멘트", json5Object.getHeaderComment());
        assertEquals("루트 코멘트 끝.", json5Object.getFooterComment());



        assertEquals(json5Object.getJSON5Array("key3").size(), 3);

        json5Object.put("key5", new String[]{"value3", "value4", null});

        System.out.println("----------------------------");
         referenceNumber = System.identityHashCode(json5Object);

        // 출력
        System.out.println("객체의 레퍼런스 번호: " + referenceNumber);
        // json5 Object 의 레퍼런스
        System.out.println(json5Object.toString(WritingOptions.json5()));


        assertEquals("comment1", new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()) .getCommentForKey("key1"));
        assertEquals("commentAfterKey1", new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()) .getCommentAfterKey("key1"));
        assertEquals(null, new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()) .getCommentAfterKey("key2"));
        System.out.println(json5Object.toString(WritingOptions.json5()));
        System.out.println(new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()));
        assertEquals(json5Object.toString(WritingOptions.json5()), new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()).toString(WritingOptions.json5()));

    }


     @JSON5Type(explicit = true)
    public static class ByteArray {
        @JSON5Value
        byte[] bytes = new byte[]{1,2,3,4,5,6,7,8,9,10};
    }

    @Test
    public void byteArrayTest() {
        ByteArray byteArray = new ByteArray();
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(byteArray);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        byte[] buffer = json5Object.getByteArray("bytes");
        assertEquals(10, buffer.length);
        for(int i = 0; i < 10; i++) {
            assertEquals(i + 1, buffer[i]);
        }
        byteArray.bytes = new byte[]{5,4,3,2,1,0};
        json5Object = JSON5Serializer.toJSON5Object(byteArray);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        buffer = json5Object.getByteArray("bytes");
        for(int i = 0; i < 6; i++) {
            assertEquals(byteArray.bytes[i], buffer[i]);
        }

        ByteArray bu = JSON5Serializer.fromJSON5Object(json5Object, ByteArray.class);
    }



     @JSON5Type(explicit = true)
    public static class MapClassTest {
        @JSON5Value
        private HashMap<String, String> map = new HashMap<>();

        @JSON5Value
        private HashMap<String, SimpleComment> commentMap = new HashMap<>();



    }



    @Test
    public void mapClassTest() {
        MapClassTest mapClassTest = new MapClassTest();
        mapClassTest.map.put("key1", "value1");
        mapClassTest.map.put("key2", "value2");
        mapClassTest.map.put("keyNull", null);
        HashMap subMap = new HashMap<>();
        subMap.put("key1", new ByteArray());
        subMap.put("key2", new ByteArray());




        Map<String, Integer> maps = new HashMap<>();
        maps.put("key1", 1);
        maps.put("key2", 2);
        maps.put("key3", 3);
        mapClassTest.commentMap.put("key1", new SimpleComment());
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(mapClassTest);


        System.out.println(json5Object.toString(WritingOptions.json5()));

        MapClassTest mapClassTest1 = JSON5Serializer.fromJSON5Object(json5Object, MapClassTest.class);




        assertEquals(mapClassTest.map.size(), mapClassTest1.map.size());

        assertEquals(json5Object.toString(WritingOptions.json5()), JSON5Serializer.toJSON5Object(mapClassTest1).toString(WritingOptions.json5()));




    }

     @JSON5Type(explicit = true)
    public static class GenericClass<T> {
        @JSON5Value
        private String value = "value";

    }

     @JSON5Type(explicit = true)
    public static class Sim {
        @JSON5Value
        Collection<GenericClass<String>> collection = new ArrayList<>();
    }

    @Test
    public void genericClassTest() {
        Sim genericClass = new Sim();
        genericClass.collection.add(new GenericClass<>());
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(genericClass);
        System.out.println(json5Object.toString(WritingOptions.json5()));
    }



    public static class TestSuperClass {
        @JSON5Value
        private String name = "name";

        public String getName() {
            return name;
        }
    }
     @JSON5Type(explicit = true)
    static class TestChildClass extends TestSuperClass {

    }

    @Test
    public void extendsTest() {
        TestChildClass testChildClass = new TestChildClass();
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(testChildClass);
        assertEquals("name", json5Object.get("name"));
        json5Object.put("name", "name2");
        testChildClass = JSON5Serializer.fromJSON5Object(json5Object, TestChildClass.class);
        assertEquals("name2", testChildClass.getName());




    }





     @JSON5Type(explicit = true)
    public static class TestClassY {
        @JSON5Value
        private int age = 29;
    }

     @JSON5Type(explicit = true)
    public static class TestClassP {
        @JSON5Value("ageReal")
        private int age = 27;
    }

     @JSON5Type(explicit = true)
    public static class TestClassX {
        @JSON5Value("nickname.key[10]")
        private String name = "name";

        @JSON5Value(value = "nickname", comment = "닉네임 오브젝트.", commentAfterKey = "닉네임 오브젝트 끝.")
        TestClassY testClassY = new TestClassY();

        @JSON5Value(value = "nickname")
        TestClassP testClassP = new TestClassP();


        @JSON5Value(key="list", comment = "닉네임을 입력합니다.", commentAfterKey = "닉네임 입력 끝.")
        ArrayList<List<TestClassY>> testClassYArrayList = new ArrayList<>();


    }

    @Test
    public void testClassX() {
        TestClassX testClassX = new TestClassX();

        testClassX.testClassYArrayList.add(new ArrayList<>());
        testClassX.testClassYArrayList.get(0).add(new TestClassY());


        
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(testClassX);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals(27, json5Object.getJSON5Object("nickname").getInt("ageReal"));
        assertEquals(29, json5Object.getJSON5Object("nickname").getInt("age"));
        assertEquals(json5Object.getCommentForKey("nickname"), "닉네임 오브젝트.");
        assertEquals(json5Object.getCommentAfterKey("nickname"), "닉네임 오브젝트 끝.");


        String json5 = json5Object.toString(WritingOptions.json5());

        System.out.println(json5);

    }



     @JSON5Type(explicit = true)
    class NestedValueClass {
        @JSON5Value
        private String name = "name";

        private String name2 = "name2";
    }




     @JSON5Type(explicit = true)
    public static class NestedObjectClass {
        @JSON5Value(key =  "ages", comment = "닉네임 오브젝트:testClassP", commentAfterKey = "닉네임 오브젝트 끝:testClassP")
        private TestClassP testClassP = new TestClassP();

        @JSON5Value(key = "ages", comment = "닉네임 오브젝트:testClassB", commentAfterKey = "닉네임 오브젝트 끝:testClassB")
        private TestClassB testClassB = new TestClassB();

        @JSON5Value("name3")
        private String name2 = "name2";

        @JSON5Value("name3")
        private String name3 = "name3";


    }

    @Test
    public void  nestedValuesTest() {

        NestedObjectClass nestedObjectClass = new NestedObjectClass();
        nestedObjectClass.testClassB.testC.name = "adsfadsfadsf";
        nestedObjectClass.testClassB.name = "123123";
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(nestedObjectClass);
        System.out.println(json5Object.toString(WritingOptions.json5()));


        NestedObjectClass nestedObjectClassCopied = JSON5Serializer.fromJSON5Object(json5Object, NestedObjectClass.class);
        assertEquals(nestedObjectClass.testClassP.age, nestedObjectClassCopied.testClassP.age);
        assertEquals(nestedObjectClass.testClassB.name, nestedObjectClassCopied.testClassB.name);
        assertEquals(nestedObjectClass.testClassB.testC.name , nestedObjectClassCopied.testClassB.testC.name);
        assertNotEquals(nestedObjectClass.name2, nestedObjectClass.name3);
        assertEquals(nestedObjectClassCopied.name2, nestedObjectClassCopied.name3);


    }

    @JSON5Type(explicit = true)
   public static class SetterGetterTestClass {

        Map<String, String> nameAgeMap = null;
        Map<String, Integer> nameAgeMapInteger = null;





       Collection<ArrayList<LinkedList<String>>> nameList = null;
       ArrayDeque<LinkedList<HashSet<Integer>>> nameTypeChangedList = null;

       @JSON5Value("nameList")
       Collection<Collection<Collection<Short>>> nameShortList = null;

       @JSON5ValueGetter
       public Collection<ArrayList<LinkedList<String>>> getNameList() {
           int x = 0;
           ArrayList result = new ArrayList();
            for(int i = 0; i < 10; i++) {
               ArrayList<LinkedList<String>> arrayList = new ArrayList<>();
               for(int j = 0; j < 10; j++) {
                   LinkedList<String> list = new LinkedList<>();
                   for(int k = 0; k < 10; k++) {
                       ++x;
                       list.add( x + "");
                   }
                   arrayList.add(list);
               }
               result.add(arrayList);
           }
            return result;

       }




       @JSON5ValueSetter
       public void setNameList(Collection<ArrayList<LinkedList<String>>> names) {
           nameList = names;

       }


       @JSON5ValueSetter(key = "nameList")
       public void setNameHashSet(ArrayDeque<LinkedList<HashSet<Integer>>> names) {
           nameTypeChangedList = names;
       }


       @JSON5ValueGetter
       public Map<String, String> getNameAgeMap() {
              Map<String, String> map = new HashMap<>();
              for(int i = 0; i < 10; i++) {
                  map.put("name" + i, i + "");
              }
              return map;
       }

       @JSON5ValueSetter(key = "nameAgeMap")
       public String setNameAgeMapInteger(Map<String, Integer> map) {
           nameAgeMapInteger = map;
           return "OK" ;
       }

       @JSON5ValueSetter(key = "nameAgeMap")
       public String setNameAgeMap(Map<String, String> map) {
          nameAgeMap = map;
          return "OK" ;
       }

       String inputName = "name";
       @JSON5ValueGetter
       public String getName() {
           return "name";
       }
       @JSON5ValueSetter
       public void setName(String name) {
           this.inputName = name;
       }


   }


   @Test
    public void setterGetterTest() {

        // You can change the default options. (It will be applied to all JSON5Object and CONSArray)
        // JSON5Object.setDefaultStringFormatOption(WritingOptions.json5());
        for(int count = 0; count < 1; ++count) {


            SetterGetterTestClass setterGetterTestClass = new SetterGetterTestClass();

            JSON5Object json5Object = JSON5Serializer.toJSON5Object(setterGetterTestClass);
            System.out.println(json5Object.toString(WritingOptions.json5()));

            setterGetterTestClass = JSON5Serializer.fromJSON5Object(json5Object, SetterGetterTestClass.class);

            assertEquals(setterGetterTestClass.nameList.size(), 10);
            Iterator<ArrayList<LinkedList<String>>> iter = setterGetterTestClass.nameList.iterator();
            int x = 0;
            for (int i = 0; i < 10; i++) {
                ArrayList<LinkedList<String>> arrayList = iter.next();
                assertEquals(arrayList.size(), 10);
                Iterator<LinkedList<String>> iter2 = arrayList.iterator();
                for (int j = 0; j < 10; j++) {
                    LinkedList<String> list = iter2.next();
                    assertEquals(list.size(), 10);
                    Iterator<String> iter3 = list.iterator();
                    for (int k = 0; k < 10; k++) {
                        ++x;
                        assertEquals(x + "", iter3.next());
                    }
                }
            }

            assertTrue(setterGetterTestClass.nameTypeChangedList instanceof  ArrayDeque);
            assertEquals(setterGetterTestClass.nameTypeChangedList.size(), 10);
            Iterator<LinkedList<HashSet<Integer>>> iter4 = setterGetterTestClass.nameTypeChangedList.iterator();
            x = 0;
            for (int i = 0; i < 10; i++) {
                LinkedList<HashSet<Integer>> arrayList = iter4.next();
                assertEquals(arrayList.size(), 10);
                Iterator<HashSet<Integer>> iter5 = arrayList.iterator();
                for (int j = 0; j < 10; j++) {
                    HashSet<Integer> list = iter5.next();
                    assertEquals(list.size(), 10);
                    SortedSet<Integer> sortedSet = new TreeSet<>(list);
                    Iterator<Integer> iter6 = sortedSet.iterator();
                    for (int k = 0; k < 10; k++) {
                        ++x;
                        assertEquals(Integer.valueOf(x), iter6.next());
                    }
                }
            }

            assertEquals(setterGetterTestClass.nameAgeMap.size(), 10);
            for(int i = 0; i < 10; i++) {
                assertEquals(setterGetterTestClass.nameAgeMap.get("name" + i), i + "");
                assertEquals(setterGetterTestClass.nameAgeMapInteger.get("name" + i),Integer.valueOf(i) );
            }


            System.out.println(json5Object.toString(WritingOptions.json5()));
            assertEquals("name", json5Object.get("name"));
            json5Object.put("name", "1213123");

            setterGetterTestClass = JSON5Serializer.fromJSON5Object(json5Object, SetterGetterTestClass.class);
            assertEquals("1213123", setterGetterTestClass.inputName);


        }
   }



    @JSON5Type(explicit = true)
   public static class Addr {
        @JSON5Value
        private String city;
        @JSON5Value
        private String zipCode;
   }


    @JSON5Type(explicit = true)
   public static class User {
       @JSON5Value
       private String name;
       @JSON5Value
       private int age;
       @JSON5Value
       private List<String> friends;
       @JSON5Value
       private Addr addr;

       private User() {}
       public User(String name, int age, String... friends) {
           this.name = name;
           this.age = age;
           this.friends = Arrays.asList(friends);
       }

       public void setAddr(String city, String zipCode) {
          this.addr = new Addr();
          this.addr.city = city;
          this.addr.zipCode = zipCode;
       }

   }

   @JSON5Type(comment = "Users", commentAfter = "Users end.")
   public static class Users {
        @JSON5Value(key = "users", comment = "key: user id, value: user object")
        private HashMap<String, User> idUserMap = new HashMap<>();
   }


   @Test
   public void exampleTest() {
       Users users = new Users();

       User user1 = new User("MinJun", 28, "YoungSeok", "JiHye", "JiHyeon", "MinSu");
       user1.setAddr("Seoul", "04528");
       User user2 = new User("JiHye", 27, "JiHyeon","Yeongseok","Minseo");
       user2.setAddr("Cheonan", "31232");
       users.idUserMap.put("qwrd", user1);
       users.idUserMap.put("ffff", user2);

       JSON5Object json5Object = JSON5Serializer.toJSON5Object(users);

       System.out.println(json5Object);
       // Output
       /*
            //Users
            {
                //key: user id, value: user object
                users:{
                    qwrd:{
                        name:'MinJun',
                        age:28,
                        friends:['YoungSeok','JiHye','JiHyeon','MinSu'],
                        addr:{
                            city:'Seoul',
                            zipCode:'04528'
                        }
                    },
                    ffff:{
                        name:'JiHye',
                        age:27,
                        friends:['JiHyeon','Yeongseok','Minseo'],
                        addr:{
                            city:'Cheonan',
                            zipCode:'31232'
                        }
                    }
                }
            }
            //Users end.
        */


       //  Parse JSON5Object to Users
       // Option 1.
       Users parsedUsers = JSON5Serializer.fromJSON5Object(json5Object, Users.class);

       // Option 2. Can be used even without a default constructor.
       //Users parsedUsers = new Users();
       //JSON5Serializer.fromJSON5Object(json5Object, parsedUsers);


   }

    @JSON5Type(explicit = true)
    public static class JSON5ElementInClass {
        @JSON5Value
        private ArrayList<JSON5Array> JSON5ArrayInList = new ArrayList<>();


       @JSON5Value
       private Map<String, JSON5Object> json5ObjectInMap = new HashMap<>();


       @JSON5Value
       private JSON5Object json5Object = new JSON5Object();


       @JSON5Value("ok[2]")
       private JSON5Object json5ObjectInArray = new JSON5Object();


       private JSON5Array consObjectBySetterGetter = new JSON5Array();

       @JSON5ValueSetter("consObjectBySetterGetter")
       public void setConsObjectBySetterGetter(JSON5Array json5ObjectInArray) {
           consObjectBySetterGetter = (JSON5Array)json5ObjectInArray;
       }

       @JSON5ValueGetter("consObjectBySetterGetter")
       public JSON5Array getConsObjectBySetterGetter() {
            return new JSON5Array().put(1).put(2);
       }


       List<JSON5Object> json5ObjectInArrayBySetter = null;

       @JSON5ValueSetter("json5ObjectInArrayBySetter")
       public void setJSON5ObjectInList(List<JSON5Object> json5ObjectInArray) {
           this.json5ObjectInArrayBySetter = json5ObjectInArray;
       }

       @JSON5ValueGetter("json5ObjectInArrayBySetter")
       public List<JSON5Object> getJSON5ObjectInList() {
           if(this.json5ObjectInArrayBySetter == null) {
                this.json5ObjectInArrayBySetter = new ArrayList<>();
                this.json5ObjectInArrayBySetter.add(new JSON5Object().put("random",Math.random() + ""));
           }
           return this.json5ObjectInArrayBySetter;
       }

    }

    @Test
    public void json5ElementInClassTest() {
        JSON5ElementInClass json5ElementInClass = new JSON5ElementInClass();

        json5ElementInClass.JSON5ArrayInList.add(new JSON5Array().put(new JSON5Object().put("name1", "name1")));

        json5ElementInClass.json5ObjectInMap.put("name2", new JSON5Object().put("name2", "name2"));
        //json5ElementInClass.consObjectBySetterGetter = new JSON5Object().put("1234", "5678");

        json5ElementInClass.json5Object.put("name", "name");
        json5ElementInClass.json5ObjectInArray.put("name3", "name3");
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(json5ElementInClass);
        System.out.println(json5Object.toString(WritingOptions.json5()));

        assertEquals(json5Object.getJSON5Array("consObjectBySetterGetter"), new JSON5Array().put(1,2));


        JSON5ElementInClass parsedJSON5Object = JSON5Object.toObject(new JSON5Object(json5Object.toString()), JSON5ElementInClass.class);

        assertEquals(json5ElementInClass.json5Object,  parsedJSON5Object.json5Object);


        assertEquals(json5Object.toString(), JSON5Object.fromObject(parsedJSON5Object).toString());


        json5Object.optJSON5Array("consObjectBySetterGetter").put(1,2);
        parsedJSON5Object = JSON5Object.toObject(new JSON5Object(json5Object.toString()), JSON5ElementInClass.class);

        assertEquals( new JSON5Array().put(1,2,1,2),parsedJSON5Object.consObjectBySetterGetter);

    }


     @JSON5Type(explicit = true)
    public static interface InterfaceTest {
        @JSON5ValueGetter
        String getName();

        @JSON5ValueSetter
        void setName(String name);

    }

     @JSON5Type(explicit = true)
    public static interface InterfaceTestText extends InterfaceTest {

    }

     @JSON5Type(explicit = true)
    public static class GenericClassTest<T extends  InterfaceTest> {
        @JSON5Value
        private T value;

        private String namename;

        @ObtainTypeValue
        public T getValue(JSON5Object all, JSON5Object value) {
            return (T) new InterfaceTest() {
                @Override
                public String getName() {
                    return System.currentTimeMillis() + "";
                }

                @Override
                public void setName(String name) {
                    namename = name;

                }
            };
        }


        private String name = "";
    }





     @JSON5Type(explicit = true)
    public static class ObjGenericClassTest<TV , IV> {

        @JSON5Value
        public TV value;
        @JSON5Value
        public List<HashSet<TV>> values = new ArrayList<>();
        @JSON5Value
        public Map<String, TV> Maps = new HashMap<>();
        public List<String> setNames = new ArrayList<>();
        @JSON5Value
        public Map<String, IV> intMaps = new HashMap<>();

        IV okValue;

        Map<String,TV> okValueMap;
        List<TV> okValueList;

        @JSON5ValueSetter
        public void setOK(IV ok) {
            okValue = ok;
        }

        @JSON5ValueGetter
        public IV getOK() {
            return (IV)(Integer)10000;
        }


        @JSON5ValueSetter
        public void setOKList(List<TV> ok) {
            okValueList = ok;
        }

        @JSON5ValueGetter
        public List<TV> getOKList() {
            if(this.okValueList != null) {
                return this.okValueList;
            }

            List<TV> list = new ArrayList<>();
            list.add((TV) new InterfaceTest() {
                String name = Math.random() + "";

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void setName(String name) {
                    this.name = name;
                }
            });
            return list;
        }

        @JSON5ValueSetter
        public void setOKMap(Map<String,TV> ok) {
            okValueMap = ok;
        }

        @JSON5ValueGetter
        public Map<String,TV> getOKMap() {
            if(this.okValueMap != null) {
                return this.okValueMap;
            }

            Map<String,TV> map = new HashMap<>();
            map.put("1", (TV) new InterfaceTest() {
                String name = Math.random() + "";

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void setName(String name) {
                    this.name = name;
                }
            });
            return map;
        }

        @ObtainTypeValue(fieldNames =  {"intMaps"})
        public int getIntValue(JSON5Object json5Element, JSON5Object value) {
            return json5Element.getInt("$value");

        }
        @ObtainTypeValue(fieldNames = {"Maps", "values"}, setterMethodNames = {"setOK","setOKMap", "setOKList"})
        public TV getValue(JSON5Object all, JSON5Object value) {
            return (TV) new InterfaceTest() {
                String name = System.currentTimeMillis() + "";

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void setName(String name) {
                    this.name = name;
                }
            };
        }



        @ObtainTypeValue(fieldNames = {"value"})
        public TV getValue1(JSON5Object all, JSON5Object value) {
            return (TV) new InterfaceTest() {
                String name = System.currentTimeMillis() + "";

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void setName(String name) {
                    this.name = name;
                }
            };
        }

        private String name = "";
    }


    @Test
    public void objGenericClassTest1() {
        ObjGenericClassTest<InterfaceTestText, Integer> genericClassTest = new ObjGenericClassTest<>();
        genericClassTest.value = new InterfaceTestText() {
            @Override
            public String getName() {
                return System.currentTimeMillis() + "";
            }

            @Override
            public void setName(String name) {
                genericClassTest.name = name;
            }
        };

        for(int i = 0; i < 5; ++i) {
            final int count = i;
            genericClassTest.intMaps.put(i + "", i);
            InterfaceTestText text = new InterfaceTestText() {
                @Override
                public String getName() {
                    return count + "";
                }

                @Override
                public void setName(String name) {
                    genericClassTest.setNames.add(name);
                }

            };
            HashSet<InterfaceTestText> set = new HashSet<>();
            set.add(text);
            genericClassTest.values.add(set);
            genericClassTest.Maps.put(i + "", text);
        }

        JSON5Object json5Object = JSON5Serializer.toJSON5Object(genericClassTest);
        System.out.println(json5Object.toString());

        ObjGenericClassTest<InterfaceTest, Integer> parsertObject = JSON5Serializer.fromJSON5Object(json5Object, ObjGenericClassTest.class);
        assertNotNull(parsertObject.value);

        assertEquals( json5Object.toString(WritingOptions.json5()), JSON5Object.fromObject(parsertObject).toString(WritingOptions.json5()));

        System.out.println(JSON5Object.fromObject(parsertObject));


    }

    @Test
    public void genericClassTest2() {
        GenericClassTest<InterfaceTest> genericClassTest = new GenericClassTest<>();
        genericClassTest.value = new InterfaceTest() {
            @Override
            public String getName() {
                return System.currentTimeMillis() + "";
            }

            @Override
            public void setName(String name) {
                genericClassTest.name = name;
            }
        };
        JSON5Object json5Object = JSON5Serializer.toJSON5Object(genericClassTest);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        GenericClassTest<InterfaceTest> parsertObject = JSON5Serializer.fromJSON5Object(json5Object, GenericClassTest.class);
    }



     @JSON5Type(explicit = true)
    public static class ResponseMessage<T> {
        @JSON5Value
        private boolean success = false;
        @JSON5Value
        private int code = 0;

        @JSON5Value
        private List<T> data = new ArrayList<>();


        public static ResponseMessage<FileInfo> createFileInfoMessage(List<FileInfo> fileInfos) {
            ResponseMessage<FileInfo> responseMessage = new ResponseMessage<>();
            responseMessage.success = true;
            responseMessage.code = 200;
            responseMessage.data = fileInfos;
            return responseMessage;
        }


        private ResponseMessage() {
            this.data = new ArrayList<>();
        }
    }

     @JSON5Type(explicit = true)
    public static class FileInfo {
        @JSON5Value
        private String name = "name";
        @JSON5Value
        private boolean isDirectory = false;
        @JSON5Value
        private long size = 0;

    }

    private static FileInfo makeRandomFileInfo() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.name = System.currentTimeMillis() + "";
        fileInfo.isDirectory = Math.random() > 0.5;
        fileInfo.size = (long)(Math.random() * 1000000);
        return fileInfo;
    }

    @Test
    public void genericClassTest3() {
        List<FileInfo> fileInfos = new ArrayList<>();
        for(int i = 0; i < 5; ++i) {
            fileInfos.add(makeRandomFileInfo());
        }
        ResponseMessage<FileInfo> responseMessage = ResponseMessage.createFileInfoMessage(fileInfos);

        JSON5Object json5Object = JSON5Object.fromObject(responseMessage);
        System.out.println(json5Object.toString(WritingOptions.jsonPretty()));

    }



    public static class A1 {
        @JSON5Value
        private String name = "name";

        @JSON5ValueSetter("name")
        public void setName(String name) {
            this.name = name;
        }


    }



     @JSON5Type(explicit = true)
    public static class A2 extends A1 {
        @JSON5Value
        private String name = "name2";

        private String value = "value";
        @JSON5ValueSetter("name")
        public void setName(String name2) {
            this.value = name2 + "by A2";
        }
    }


    @Test
    public void extendsTest2() {
        A2 a2 = new A2();
        JSON5Object json5Object = JSON5Object.fromObject(a2);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        A2 a2_1 = JSON5Object.toObject(json5Object, A2.class);

        assertEquals(a2_1.name, "name2");
        assertEquals(a2_1.value, "name2by A2");

    }


    public static enum ValueEnum {
        VALUE1, VALUE2, VALUE3, VALUE4
    }

     @JSON5Type(explicit = true)
    public static class EnumClass<T> {
        @JSON5Value
        private ValueEnum valueEnum = ValueEnum.VALUE1;

        private ValueEnum valueEnum2 = null;
        @JSON5Value
        private ValueEnum valueEnum3 = ValueEnum.VALUE3;

        @JSON5Value

        private T valueEnum4 = null;

        @ObtainTypeValue(fieldNames = {"valueEnum4"}, deserializeAfter = false)
        public T getValueEnum4(JSON5Object json5Element, JSON5Object root) {
            return (T) ValueEnum.VALUE4;
        }


        @JSON5ValueSetter
        public void setValueEnum2(ValueEnum valueEnum2) {
            this.valueEnum2 = valueEnum2;
        }

        @JSON5ValueGetter
        public ValueEnum getValueEnum2() {
            if(valueEnum2 == null) {
                valueEnum2 = ValueEnum.VALUE2;
            }
            return valueEnum2;
        }
    }

     @JSON5Type(explicit = true)
    public static interface I1 {
        @JSON5ValueGetter
        String getName();

        @JSON5ValueSetter
        void setName(String name);
    }

     @JSON5Type(explicit = true)
    public static class I1Impl implements  I1 {

        private String name = "name";

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name + " by I1Impl";

        }
    }

     @JSON5Type(explicit = true)
    public static class I2 {
        @JSON5Value
        private I1Impl i1 = new I1Impl();
        @JSON5Value
        private I1 i1i = new I1Impl();

        @JSON5Value
        private List<I1> i1iList = new ArrayList<>();

        @JSON5Value
        private Map<String, I1> i1iMap = new HashMap<>();

        private I1 i1iInSetter = new I1Impl();
        private List<I1> i1iListInSetter = new ArrayList<>();
        private Map<String, I1> i1iMapInSetter = new HashMap<>();



        @JSON5Value
        private static String staticValue = "staticValue";

        @ObtainTypeValue(fieldNames = {"i1i","i1iList", "i1iMap" }, setterMethodNames = {"setI1inMethod", "setI1iListInSetter", "setI1iMapInSetter"}, deserializeAfter = true)
        public I1 getI1(JSON5Object json5Element, JSON5Object root) {
            return new I1Impl();
        }

        @JSON5ValueSetter
        public void setI1inMethod(I1 i1) {
            this.i1iInSetter = i1;
        }

        @JSON5ValueGetter
        public I1 getI1inMethod() {
            return this.i1i;
        }

        @JSON5ValueSetter
        public void setI1iListInSetter(List<I1> i1iListInSetter) {
            this.i1iListInSetter = i1iListInSetter;
        }

        @JSON5ValueGetter
        public List<I1> getI1iListInSetter() {
            List<I1> list = new ArrayList<>();
            list.add(new I1Impl());
            return list;
        }

        @JSON5ValueSetter
        public void setI1iMapInSetter(Map<String, I1> i1iMapInSetter) {
            this.i1iMapInSetter = i1iMapInSetter;
        }

        @JSON5ValueGetter
        public Map<String, I1> getI1iMapInSetter() {
            Map<String, I1> map = new HashMap<>();
            map.put("1", new I1Impl());
            return map;
        }
    }

    @Test
    public void interfaceAndImplClassTest() {
        I2 i2 = new I2();
        i2.i1iList = new ArrayList<>();
        i2.i1iList.add(new I1Impl());
        i2.i1iMap.put("1", new I1Impl());
        JSON5Object json5Object = JSON5Object.fromObject(i2);

        System.out.println(json5Object.toString(WritingOptions.json5()));

        I2 i2_1 = JSON5Object.toObject(json5Object, I2.class);
        assertEquals(i2_1.i1.getName(), "name by I1Impl");
        assertEquals(i2_1.i1i.getName(), "name by I1Impl");
        assertEquals(i2_1.i1iList.get(0).getName(), "name by I1Impl");
        assertEquals(i2_1.i1iMap.get("1").getName(), "name by I1Impl");
        assertEquals(i2_1.i1iInSetter.getName(), "name by I1Impl");
        assertEquals(i2_1.i1iListInSetter.get(0).getName(), "name by I1Impl");
        assertEquals(i2_1.i1iMapInSetter.get("1").getName(), "name by I1Impl");

    }







    @Test
    public void enumTest() {
        EnumClass<ValueEnum> enumClass = new EnumClass<ValueEnum>();
        JSON5Object json5Object = JSON5Object.fromObject(enumClass);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        json5Object.put("valueEnum3", "sdafdasfadsf");



        EnumClass enumClass1 = JSON5Object.toObject(json5Object, EnumClass.class);
        assertEquals(enumClass.valueEnum, enumClass1.valueEnum);
        assertEquals(enumClass1.valueEnum2, ValueEnum.VALUE2);

        assertEquals(enumClass1.valueEnum3, null);
        assertEquals(enumClass1.valueEnum4, ValueEnum.VALUE4);

        System.out.println(JSON5Object.fromObject(enumClass1));
    }



     @JSON5Type(explicit = true)
    public interface ResultMessage<T> {

        @JSON5ValueGetter
        T getData();
    }

     @JSON5Type(explicit = true)
    public class ResultMessageImpl<T> implements  ResultMessage<T>{

        T data = null;

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }


    @Test
    public void interfaceTest() {
        ResultMessageImpl<JSON5Object> resultMessage = new ResultMessageImpl<>();
        resultMessage.setData(new JSON5Object().put("time", 123123));
        JSON5Object json5Object = JSON5Object.fromObject(resultMessage);
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals(json5Object.getJSON5Object("data").getLong("time"), 123123);

        JSON5Object parsedObject = JSON5Object.fromObject(resultMessage);

        System.out.println(parsedObject.toString(WritingOptions.json5()));

        assertEquals(json5Object.toString(WritingOptions.json5()), parsedObject.toString(WritingOptions.json5()));


    }




}