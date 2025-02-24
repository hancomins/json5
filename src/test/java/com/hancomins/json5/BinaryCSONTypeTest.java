package com.hancomins.json5;


import com.hancomins.json5.options.JSON5WriterOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryCSONTypeTest {

    @Test
    public void testNumberTypes() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("int", 1);
        json5Object.put("float", 1.1f);
        json5Object.put("double", 2.2);
        json5Object.put("long", Long.MAX_VALUE);
        json5Object.put("short", Short.MAX_VALUE);
        json5Object.put("byte", (byte)128);
        json5Object.put("char", 'c');
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        System.out.println(json5Object);
        assertEquals(json5Object, readObject);
        assertEquals(json5Object.getInt("int"), readObject.getInt("int"));
        //assertInstanceOf(Integer.class, readObject.get("int"));
        assertEquals(json5Object.get("float"), readObject.get("float"));
        assertInstanceOf(Float.class, readObject.get("float"));
        assertEquals(json5Object.get("double"), readObject.get("double"));
        assertInstanceOf(Double.class, readObject.get("double"));
        assertEquals(json5Object.get("long"), readObject.get("long"));
        assertInstanceOf(Long.class, readObject.get("long"));
        assertEquals(json5Object.get("short"), readObject.get("short"));
        assertInstanceOf(Short.class, readObject.get("short"));
        assertEquals(json5Object.get("byte"), readObject.get("byte"));
        assertInstanceOf(Byte.class, readObject.get("byte"));
        assertEquals(json5Object.get("char"), readObject.get("char"));
        assertInstanceOf(Character.class, readObject.get("char"));
    }


    @Test
    public void testStringTypes() {
        JSON5Object json5Object = new JSON5Object();
        // 0bytes string
        json5Object.put("1", "");
        // 15bytes string
        json5Object.put("2", "short string");
        // 254bytes string
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 254; i++) {
            sb.append("a");
        }
        json5Object.put("3", sb.toString());
        sb.setLength(0);
        // 65533 bytes string
        for(int i = 0; i < 65533; i++) {
            sb.append("a");
        }
        json5Object.put("4", sb.toString());
        sb.setLength(0);
        // 65535 bytes string
        for(int i = 0; i < 65535; i++) {
            sb.append("a");
        }
        json5Object.put("5", sb.toString());
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        assertEquals(json5Object, readObject);
        assertEquals(json5Object.get("1"), readObject.get("1"));
        assertInstanceOf(String.class, readObject.get("1"));
        assertEquals(json5Object.get("2"), readObject.get("2"));
        assertInstanceOf(String.class, readObject.get("2"));
        assertEquals(json5Object.get("3"), readObject.get("3"));
        assertInstanceOf(String.class, readObject.get("3"));
        assertEquals(json5Object.get("4"), readObject.get("4"));
        assertInstanceOf(String.class, readObject.get("4"));
        assertEquals(json5Object.get("5"), readObject.get("5"));
        assertInstanceOf(String.class, readObject.get("5"));
    }

    @Test
    public void testByteArray() {
        JSON5Object json5Object = new JSON5Object();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 0bytes byte array
        json5Object.put("1", baos.toByteArray());
        // 254bytes byte array
        for(int i = 0; i < 254; i++) {
            baos.write(1);
        }
        json5Object.put("2", baos.toByteArray());
        baos.reset();
        // 65533 bytes byte array
        for(int i = 0; i < 65533; i++) {
            baos.write(1);
        }
        json5Object.put("3", baos.toByteArray());
        baos.reset();
        // 165535 bytes byte array
        for(int i = 0; i < 165535; i++) {
            baos.write(1);
        }
        json5Object.put("4", baos.toByteArray());
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        assertEquals(json5Object, readObject);
        assertArrayEquals((byte[])json5Object.get("1"), (byte[])readObject.get("1"));
        assertArrayEquals((byte[])json5Object.get("2"), (byte[])readObject.get("2"));
        assertArrayEquals((byte[])json5Object.get("3"), (byte[])readObject.get("3"));
        assertArrayEquals((byte[])json5Object.get("4"), (byte[])readObject.get("4"));
        System.out.println(readObject.getString("2"));


    }


    @Test
    @DisplayName("JSON5OBject")
    public void testSimpleKeyValue() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("key", "value");
        json5Object.put("key2", 123L);
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        System.out.println(json5Object);
        assertEquals(json5Object, readObject);
    }

    @Test
    public void testObjectInEmptyObject() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Object innerObject = new JSON5Object();
        json5Object.put("inner", innerObject);
        JSON5Object originJSON5Object = json5Object.clone();
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        System.out.println(json5Object);
        assertEquals(originJSON5Object, readObject);
        assertEquals(json5Object, readObject);
    }

    @Test
    public void testObjectInObject() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Object innerObject = new JSON5Object();
        innerObject.put("key", "value");
        json5Object.put("inner", innerObject);
        innerObject.put("key2", 123L);
        innerObject.put("key3", new JSON5Object());
        //innerObject.put("key4", new JSON5Object().put("key", "value").put("key2", 123L));
        json5Object.put("key2", 123L);


        JSON5Object originJSON5Object = json5Object.clone();
        System.out.println("       json5: " + json5Object);
        System.out.println("origin json5: " + originJSON5Object);
        byte[] json5Bytes = json5Object.toBytes();
        System.out.println("json5Bytes: " + json5Bytes.length);
        JSON5Object readObject = new JSON5Object(json5Bytes);
        System.out.println(readObject);
        assertEquals(originJSON5Object, readObject);
        assertEquals(json5Object, readObject);
    }

    @Test
    public void testArrayInEmptyArray() {
        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array innerArray = new JSON5Array();
        JSON5Array.add(innerArray);
        JSON5Array originJSON5Array = JSON5Array.clone();
        byte[] json5Bytes = JSON5Array.toBytes();
        JSON5Array readArray = new JSON5Array(json5Bytes);
        System.out.println(JSON5Array);
        assertEquals(originJSON5Array, readArray);
        assertEquals(JSON5Array, readArray);
    }

    @Test
    public void testObjectInArray() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array innerArray = new JSON5Array();
        innerArray.add("value");
        JSON5Array.add(innerArray);
        innerArray.add(123L);
        innerArray.add("sfdsf");
        innerArray.add(new JSON5Object());
        json5Object.put("array", JSON5Array);
        JSON5Object originJSON5 = json5Object.clone();


        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readArray = new JSON5Object(json5Bytes);
        System.out.println(json5Object);
        assertEquals(originJSON5, readArray);
        assertEquals(json5Object, readArray);

    }


    private JSON5Object makeCSOObject() {
        Random random = new Random(System.currentTimeMillis());
        byte[] randomBuffer = new byte[random.nextInt(64) +2];
        random.nextBytes(randomBuffer);
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("1", 1);
        json5Object.put("1.1", 1.1f);
        json5Object.put("2.2", 2.2);
        json5Object.put("333333L", 333333L);
        json5Object.put("boolean", true);
        json5Object.put("char", 'c');
        json5Object.put("short", (short)32000);
        json5Object.put("byte", (byte)128);
        json5Object.put("null", null);
        json5Object.put("string", "stri \" \n\rng");
        json5Object.put("this", json5Object);
        json5Object.put("byte[]", randomBuffer);
        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array.add(1);
        JSON5Array.add(1.1f);
        JSON5Array.add((double)2.2);
        JSON5Array.put(333333L);
        JSON5Array.put(true);
        JSON5Array.put('c');
        JSON5Array.add((short)32000);
        JSON5Array.add((byte)128);
        JSON5Array.add(null);
        JSON5Array.add("stri \" \n\rng");
        JSON5Array.add(JSON5Array);
        JSON5Array.add(json5Object.clone());
        JSON5Array.add(randomBuffer);
        json5Object.put("array", JSON5Array);
        json5Object.put("array2", new JSON5Array().put(new JSON5Array().put(1).put(2)).put(new JSON5Array().put(3).put(4)).put(new JSON5Array()).put(new JSON5Object()));
        json5Object.put("array3", new JSON5Array().put("").put(new JSON5Array().put(3).put(4)).put(new JSON5Array()).put(new JSON5Object()));
        json5Object.put("array4", new JSON5Array().put(new JSON5Object()).put(new JSON5Object()).put(new JSON5Array()).put(new JSON5Object().put("inArray",new JSON5Array())));
        json5Object.put("key111", new JSON5Object().put("1", new JSON5Object()));
        json5Object.put("key112", new JSON5Array().put(new JSON5Object()));



        return json5Object;
    }


    @Test
    public void cloneAndEqualsTest() throws  Exception {
        JSON5Object json5Object = makeCSOObject();
        byte[] json5Bytes = json5Object.toBytes();
        JSON5Object readObject = new JSON5Object(json5Bytes);
        System.out.println(json5Object);
        assertEquals(json5Object, readObject);
    }



    @Test
    public void withComment() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.setHeaderComment("header comment");
        json5Object.setFooterComment("footer comment");

        json5Object.put("key", "value");

        json5Object.setCommentForKey("key", " comment\n for key");
        json5Object.setCommentAfterKey("key", " comment after key ");
        json5Object.setCommentForValue("key", " comment for value ");
        json5Object.setCommentAfterValue("key", " comment after value ");


        json5Object.put("emptyObject", new JSON5Object());
        json5Object.setCommentForKey("emptyObject", " for emptyObject");
        json5Object.setCommentAfterKey("emptyObject", " after emptyObject ");
        json5Object.setCommentForValue("emptyObject", " comment for emptyObject value ");
        json5Object.setCommentAfterValue("emptyObject", " comment after emptyObject value ");

        json5Object.put("emptyArray", new JSON5Array());
        json5Object.setCommentForKey("emptyArray", " for emptyArray");
        json5Object.setCommentAfterKey("emptyArray", " after emptyArray ");
        json5Object.setCommentForValue("emptyArray", " comment for emptyArray value ");
        json5Object.setCommentAfterValue("emptyArray", " comment after emptyArray value ");


        JSON5Array valueArray = new JSON5Array().put("value1").put("value2").put("value3");
        json5Object.put("array", valueArray);
        json5Object.setCommentForKey("array", " for array");
        json5Object.setCommentAfterKey("array", " after array ");
        json5Object.setCommentForValue("array", " comment for array value ");
        json5Object.setCommentAfterValue("array", " comment after array value ");
        valueArray.setCommentForValue(0, " comment for array value 0 ");
        valueArray.setCommentForValue(1, " comment for array value 1 ");
        valueArray.setCommentForValue(2, " comment for array value 2 ");
        valueArray.setCommentAfterValue(0, " comment after array value 0 ");
        valueArray.setCommentAfterValue(1, " comment after array value 1 ");
        valueArray.setCommentAfterValue(2, " comment after array value 2 ");




        JSON5Object valueObject = new JSON5Object().put("key1", "value1").put("key2", "value2").put("key3", "value3");
        json5Object.put("object", valueObject);
        json5Object.setCommentForKey("object", " for object");
        json5Object.setCommentAfterKey("object", " after object ");
        json5Object.setCommentForValue("object", " comment for object value ");
        json5Object.setCommentAfterValue("object", " comment after object value ");
        valueObject.setCommentForKey("key1", " for key1");
        valueObject.setCommentAfterKey("key1", " after key1 ");
        valueObject.setCommentForValue("key1", " comment for key1 value ");
        valueObject.setCommentAfterValue("key1", " comment after key1 value ");
        valueObject.setCommentForKey("key2", " for key2");
        valueObject.setCommentAfterKey("key2", " after key2 ");
        valueObject.setCommentForValue("key2", " comment for key2 value ");
        valueObject.setCommentAfterValue("key2", " comment after key2 value ");
        valueObject.setCommentForKey("key3", " for key3");
        valueObject.setCommentAfterKey("key3", " after key3 ");
        valueObject.setCommentForValue("key3", " comment for key3 value ");
        valueObject.setCommentAfterValue("key3", " comment after key3 value ");


        //System.out.println(json5Object.toString(JSON5WriterOption.json5()));
        //System.out.println(json5Object.toString(JSON5WriterOption.prettyJson()));
        System.out.println(json5Object.toString(JSON5WriterOption.json5()));


        JSON5Object parseredJSON5Object = new JSON5Object(json5Object.toBytes());
        assertEquals("value", parseredJSON5Object.get("key"));
        assertEquals(" comment\n for key", parseredJSON5Object.getCommentForKey("key"));
        assertEquals(" comment after key ", parseredJSON5Object.getCommentAfterKey("key"));
        assertEquals(" comment for value ", parseredJSON5Object.getCommentForValue("key"));
        assertEquals(" comment after value ", parseredJSON5Object.getCommentAfterValue("key"));

        // Verify comments for emptyObject
        assertNotNull(parseredJSON5Object.get("emptyObject"));
        assertEquals(" for emptyObject", parseredJSON5Object.getCommentForKey("emptyObject"));
        assertEquals(" after emptyObject ", parseredJSON5Object.getCommentAfterKey("emptyObject"));
        assertEquals(" comment for emptyObject value ", parseredJSON5Object.getCommentForValue("emptyObject"));
        assertEquals(" comment after emptyObject value ", parseredJSON5Object.getCommentAfterValue("emptyObject"));

        // Verify comments for emptyArray
        assertNotNull(parseredJSON5Object.get("emptyArray"));
        assertEquals(" for emptyArray", parseredJSON5Object.getCommentForKey("emptyArray"));
        assertEquals(" after emptyArray ", parseredJSON5Object.getCommentAfterKey("emptyArray"));
        assertEquals(" comment for emptyArray value ", parseredJSON5Object.getCommentForValue("emptyArray"));
        assertEquals(" comment after emptyArray value ", parseredJSON5Object.getCommentAfterValue("emptyArray"));

        // Verify comments for array values
        JSON5Array parsedArray = parseredJSON5Object.getJSON5Array("array");
        assertEquals(" comment for array value 0 ", parsedArray.getCommentForValue(0));
        assertEquals(" comment after array value 0 ", parsedArray.getCommentAfterValue(0));
        assertEquals(" comment for array value 1 ", parsedArray.getCommentForValue(1));
        assertEquals(" comment after array value 1 ", parsedArray.getCommentAfterValue(1));
        assertEquals(" comment for array value 2 ", parsedArray.getCommentForValue(2));
        assertEquals(" comment after array value 2 ", parsedArray.getCommentAfterValue(2));

        // Verify comments for object values
        JSON5Object parsedObject = parseredJSON5Object.getJSON5Object("object");
        assertEquals(" for key1", parsedObject.getCommentForKey("key1"));
        assertEquals(" after key1 ", parsedObject.getCommentAfterKey("key1"));
        assertEquals(" comment for key1 value ", parsedObject.getCommentForValue("key1"));
        assertEquals(" comment after key1 value ", parsedObject.getCommentAfterValue("key1"));
        assertEquals(" for key2", parsedObject.getCommentForKey("key2"));
        assertEquals(" after key2 ", parsedObject.getCommentAfterKey("key2"));
        assertEquals(" comment for key2 value ", parsedObject.getCommentForValue("key2"));
        assertEquals(" comment after key2 value ", parsedObject.getCommentAfterValue("key2"));
        assertEquals(" for key3", parsedObject.getCommentForKey("key3"));
        assertEquals(" after key3 ", parsedObject.getCommentAfterKey("key3"));
        assertEquals(" comment for key3 value ", parsedObject.getCommentForValue("key3"));
        assertEquals(" comment after key3 value ", parsedObject.getCommentAfterValue("key3"));


        System.out.println(json5Object.toString(JSON5WriterOption.prettyJson5()));
        parseredJSON5Object = new JSON5Object(json5Object.toBytes());
        assertEquals("value", parseredJSON5Object.get("key"));
        assertEquals(" comment\n for key", parseredJSON5Object.getCommentForKey("key"));
        assertEquals(" comment after key ", parseredJSON5Object.getCommentAfterKey("key"));
        assertEquals(" comment for value ", parseredJSON5Object.getCommentForValue("key"));
        assertEquals(" comment after value ", parseredJSON5Object.getCommentAfterValue("key"));

        // Verify comments for emptyObject
        assertNotNull(parseredJSON5Object.get("emptyObject"));
        assertEquals(" for emptyObject", parseredJSON5Object.getCommentForKey("emptyObject"));
        assertEquals(" after emptyObject ", parseredJSON5Object.getCommentAfterKey("emptyObject"));
        assertEquals(" comment for emptyObject value ", parseredJSON5Object.getCommentForValue("emptyObject"));
        assertEquals(" comment after emptyObject value ", parseredJSON5Object.getCommentAfterValue("emptyObject"));

        // Verify comments for emptyArray
        assertNotNull(parseredJSON5Object.get("emptyArray"));
        assertEquals(" for emptyArray", parseredJSON5Object.getCommentForKey("emptyArray"));
        assertEquals(" after emptyArray ", parseredJSON5Object.getCommentAfterKey("emptyArray"));
        assertEquals(" comment for emptyArray value ", parseredJSON5Object.getCommentForValue("emptyArray"));
        assertEquals(" comment after emptyArray value ", parseredJSON5Object.getCommentAfterValue("emptyArray"));

        // Verify comments for array values
        parsedArray = parseredJSON5Object.getJSON5Array("array");
        assertEquals(" comment for array value 0 ", parsedArray.getCommentForValue(0));
        assertEquals(" comment after array value 0 ", parsedArray.getCommentAfterValue(0));
        assertEquals(" comment for array value 1 ", parsedArray.getCommentForValue(1));
        assertEquals(" comment after array value 1 ", parsedArray.getCommentAfterValue(1));
        assertEquals(" comment for array value 2 ", parsedArray.getCommentForValue(2));
        assertEquals(" comment after array value 2 ", parsedArray.getCommentAfterValue(2));

        // Verify comments for object values
        parsedObject = parseredJSON5Object.getJSON5Object("object");
        assertEquals(" for key1", parsedObject.getCommentForKey("key1"));
        assertEquals(" after key1 ", parsedObject.getCommentAfterKey("key1"));
        assertEquals(" comment for key1 value ", parsedObject.getCommentForValue("key1"));
        assertEquals(" comment after key1 value ", parsedObject.getCommentAfterValue("key1"));
        assertEquals(" for key2", parsedObject.getCommentForKey("key2"));
        assertEquals(" after key2 ", parsedObject.getCommentAfterKey("key2"));
        assertEquals(" comment for key2 value ", parsedObject.getCommentForValue("key2"));
        assertEquals(" comment after key2 value ", parsedObject.getCommentAfterValue("key2"));
        assertEquals(" for key3", parsedObject.getCommentForKey("key3"));
        assertEquals(" after key3 ", parsedObject.getCommentAfterKey("key3"));
        assertEquals(" comment for key3 value ", parsedObject.getCommentForValue("key3"));
        assertEquals(" comment after key3 value ", parsedObject.getCommentAfterValue("key3"));


        assertEquals("header comment", parseredJSON5Object.getHeaderComment());
        assertEquals("footer comment", parseredJSON5Object.getFooterComment());



    }

    @Test
    public void sizeCompare() {
        if(1 < 2) return;
        InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("large-file.json");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json = byteArrayOutputStream.toString();
        JSON5Object json5Object = new JSON5Object(json);
        byte[] json5Bytes = json5Object.toBytes();
        System.out.println("json5: " + json.getBytes().length);
        System.out.println("json5: " + json5Bytes.length);
        System.out.println( 100 - ((float)json5Bytes.length / json.getBytes().length) * 100 + "%");

        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 10; i++) {
            JSON5Object json5Object1 =  new JSON5Object(json);
            json5Object1.toString();
        }
        System.out.println("json5: " + (System.currentTimeMillis() - start) + "ms");


        start = System.currentTimeMillis();
        for(int i = 0 ; i < 10; i++) {
            JSON5Object json5Object1 =  new JSON5Object(json5Bytes);
            json5Object1.toBytes();
        }
        System.out.println("json5: " + (System.currentTimeMillis() - start) + "ms");




    }


}
