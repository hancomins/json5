package com.hancomins.json5;


import com.hancomins.json5.options.JSON5WriterOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("JSON5TypeObjectTest (성공)")
public class JSON5TypeObjectTest {

    private JSON5Object makeCSOObject() {
        Random random = new Random(System.currentTimeMillis());
        byte[] randomBuffer = new byte[random.nextInt(64) +2];
        random.nextBytes(randomBuffer);
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("1", 1);
        json5Object.put("1.1", 1.1f);
        json5Object.put("2.2", 2.2);
        json5Object.put("333333L", Long.MIN_VALUE);
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


        JSON5Object jsonObjectX = new JSON5Object("{char:'c'}");
        System.out.println(jsonObjectX.toString());




        JSON5Object jsonObject = new JSON5Object("{\"key\": \"va \\\" \\n \\r lue\"}");

        JSON5Object json5ObjectA = new JSON5Object("{\"key\": \"va \\\" \\n \\r lue\"}");
        System.out.println(json5ObjectA.toString());
        JSON5Object jsonObjectA = new JSON5Object(json5ObjectA.toString(JSON5WriterOption.json()));
        new JSON5Object(json5ObjectA.toString(), JSON5WriterOption.json());

        System.out.println("--------------------------------------------------");

        JSON5Object json5Object = makeCSOObject();
        JSON5Object json5Object2 = json5Object.clone();
        assertEquals(json5Object, json5Object2);
        assertEquals(json5Object.toString(), json5Object2.toString());

        System.out.println(json5Object.toString());
        JSON5Object jsonObject1 = new JSON5Object(json5Object.toString(JSON5WriterOption.json()));
        assertEquals(json5Object2,new JSON5Object(json5Object.toString(JSON5WriterOption.json())));

        JSON5WriterOption json5WriterOption = JSON5WriterOption.json().setPretty(true);


        assertEquals(json5Object2.toString(json5WriterOption),new JSON5Object(json5Object.toString(json5WriterOption)).toString(json5WriterOption));
        assertEquals(json5Object2,new JSON5Object(json5Object.toString()));
    }

    @Test
    public void toJSON5AndParseTest() {

        JSON5Object json5Object = makeCSOObject();

        System.out.println(json5Object.toString());
        byte[] buffer = json5Object.getByteArray("byte[]");


        JSON5Object compareJSON5Object = new JSON5Object(json5Object.toString());

        assertEquals(1, ((Number)compareJSON5Object.get("1")).intValue());
        assertEquals(1.1f, compareJSON5Object.getFloat("1.1"), 0.0001f);
        assertEquals(2.2, compareJSON5Object.getDouble("2.2"), 0.0001);
        assertEquals(Long.MIN_VALUE, compareJSON5Object.get("333333L"));
        assertEquals(true, compareJSON5Object.get("boolean"));
        assertEquals('c', compareJSON5Object.getChar("char"));
        Object value =  compareJSON5Object.get("null");

        assertEquals(null, compareJSON5Object.get("null"));

        assertEquals((short)32000, compareJSON5Object.getShort("short"));
        assertEquals((byte)128, compareJSON5Object.getByte("byte"));
        assertEquals("stri \" \n\rng", compareJSON5Object.getString("string"));


        String aa = compareJSON5Object.getString("byte[]");

        assertArrayEquals(buffer, compareJSON5Object.getByteArray("byte[]"));

        JSON5Array JSON5Array = compareJSON5Object.getJSON5Array("array");
        assertEquals(1, JSON5Array.getInt(0));
        assertEquals(1.1f, JSON5Array.getFloat(1), 0.00001f);
        assertEquals(2.2, JSON5Array.getDouble(2), 0.00001);
        assertEquals(333333L, JSON5Array.getLong(3));
        assertEquals(true, JSON5Array.getBoolean(4));
        assertEquals('c', JSON5Array.getChar(5));
        assertEquals((short)32000, JSON5Array.getShort(6));
        assertEquals((byte)128, JSON5Array.getByte(7));
        assertEquals(null, JSON5Array.get(8));
        assertEquals("stri \" \n\rng", JSON5Array.get(9));
        assertArrayEquals("stri \" \n\rng".getBytes(StandardCharsets.UTF_8), JSON5Array.getByteArray(9));
        assertTrue(JSON5Array.get(10) instanceof JSON5Array);
        assertTrue(JSON5Array.get(11) instanceof JSON5Object);
        assertArrayEquals(buffer, JSON5Array.getByteArray(12));


    }


    @Test
    @DisplayName("비어있는 JSON5Object 와 JSON5Array 파싱 테스트")
    public void emptyJSON5ObjectAndArrayTest() {

        JSON5Object json5Object = new JSON5Object("{}", JSON5WriterOption.json());
        JSON5Array JSON5Array = new JSON5Array("[]", JSON5WriterOption.json());

        assertEquals(0, json5Object.size());
        assertEquals(0, JSON5Array.size());

        JSON5Object complexJSON5Object = new JSON5Object("{\"emptyObject\":{},\"emptyArray\":[]}");
        assertEquals(2, complexJSON5Object.size());
        assertEquals(0, complexJSON5Object.getJSON5Object("emptyObject").size());
        assertEquals(0, complexJSON5Object.getJSON5Array("emptyArray").size());

        System.out.println(complexJSON5Object);

        assertEquals("{\"emptyObject\":{},\"emptyArray\":[]}", complexJSON5Object.toString());






    }


    @Test
    public void toJsonAndParseTest() {


        JSON5Object json5Object = makeCSOObject();

        byte[] buffer = json5Object.getByteArray("byte[]");
        byte[] bufferOrigin = buffer;
        String jsonString = json5Object.toString(JSON5WriterOption.json());



        System.out.println(json5Object.get("string"));


        System.out.println(jsonString);
        String bufferBase64 = "base64," + Base64.getEncoder().encodeToString(buffer);
        JSON5Object compareJSON5Object = new JSON5Object(jsonString, JSON5WriterOption.json());


        assertEquals(1, compareJSON5Object.getInt("1"));
        assertEquals(1.1f, compareJSON5Object.getFloat("1.1"), 0.0001f);
        assertEquals(2.2, compareJSON5Object.getDouble("2.2"), 0.0001);
        assertEquals(Long.MIN_VALUE, compareJSON5Object.getLong("333333L"));
        assertEquals(true, compareJSON5Object.getBoolean("boolean"));
        assertEquals('c', compareJSON5Object.getChar("char"));
        assertEquals((short)32000, compareJSON5Object.getShort("short"));
        assertEquals((byte)128, compareJSON5Object.getByte("byte"));
        assertEquals("stri \" \n\rng", compareJSON5Object.getString("string"));


        assertEquals(bufferBase64, compareJSON5Object.getString("byte[]"));
        assertArrayEquals(bufferOrigin, compareJSON5Object.getByteArray("byte[]"));

        JSON5Array JSON5Array = compareJSON5Object.getJSON5Array("array");
        assertEquals(1, JSON5Array.get(0));
        assertEquals(1.1f, JSON5Array.getFloat(1), 0.00001f);
        assertEquals(2.2, JSON5Array.getDouble(2), 0.00001);
        assertEquals(333333L, JSON5Array.getLong(3));
        assertEquals(true, JSON5Array.getBoolean(4));
        assertEquals('c', JSON5Array.getChar(5));
        assertEquals(32000, JSON5Array.getShort(6));
        assertEquals((byte)128, JSON5Array.getByte(7));
        assertEquals(null, JSON5Array.getString(8));
        assertEquals("stri \" \n\rng", JSON5Array.getString(9));
        assertArrayEquals("stri \" \n\rng".getBytes(StandardCharsets.UTF_8), JSON5Array.getByteArray(9));
        assertTrue(JSON5Array.get(10) instanceof JSON5Array);
        assertTrue(JSON5Array.get(11) instanceof JSON5Object);
        assertArrayEquals(buffer, JSON5Array.getByteArray(12));

        System.out.println("--------------------------------------------------");
        System.out.println(jsonString);

        JSON5Object json5Object2 = new JSON5Object(jsonString);

    }

    @Test
    public void json5ArrayToStringTest() {

    }
}
