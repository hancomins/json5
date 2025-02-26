package com.hancomins.json5;

import com.hancomins.json5.options.WritingOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("JSON5TypeTest (성공)")
public class JSON5TypeTest {




    @Test
    public void jsonStringParsingTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("11111",1)
        .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSONObject(jsonObject.toString());

        JSON5Object json5Object = new JSON5Object(new JSON5Object(jsonObject.toString()).toString(WritingOptions.json()));
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> json5KeySet =json5Object.keySet();
        assertEquals(originalKeySet, json5KeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(jsonObject.get(oriKey),json5Object.get(oriKey) );
        }

        numberJsonArray = jsonObject.getJSONArray("AN");
        JSON5Array numberJSON5Array = json5Object.getJSON5Array("AN");
        assertEquals(numberJsonArray.length(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = jsonObject.getJSONArray("AO");
        JSON5Array objectJSON5Array = json5Object.getJSON5Array("AO");
        assertEquals(objectJsonArray.length(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            JSONObject jao = objectJsonArray.getJSONObject(i);
            JSON5Object cao = objectJSON5Array.getJSON5Object(i);
            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getInt("random"), cao.getInteger("random"));
        }
    }

    @Test
    public void jsonAndCSonParsingTest() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("11111",1)
                .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSONObject(jsonObject.toString());
        System.out.println(jsonObject);

        JSON5Object json5Object = new JSON5Object(new JSON5Object(jsonObject.toString()).toBytes());
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> json5KeySet =json5Object.keySet();
        assertEquals(originalKeySet, json5KeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(Integer.getInteger(jsonObject.get(oriKey)  + "") ,Integer.getInteger( json5Object.get(oriKey) + ""));
        }

        numberJsonArray = jsonObject.getJSONArray("AN");
        JSON5Array numberJSON5Array = json5Object.getJSON5Array("AN");
        assertEquals(numberJsonArray.length(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = jsonObject.getJSONArray("AO");
        JSON5Array objectJSON5Array = json5Object.getJSON5Array("AO");
        assertEquals(objectJsonArray.length(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            JSONObject jao = objectJsonArray.getJSONObject(i);
            JSON5Object cao = objectJSON5Array.getJSON5Object(i);
            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getInt("random"), cao.getInteger("random"));
            assertEquals(Double.valueOf(jao.getDouble("float")),Double.valueOf(cao.getDouble("float")));
            assertEquals(Float.valueOf(jao.getFloat("float")),Float.valueOf(cao.getFloat("float")));
            assertEquals(jao.getInt("float"), cao.getInteger("float"));
        }
    }

    @Test
    public void overBufferTest() {
        JSON5Object json5Object = new JSON5Object().put("sdfasdf",213123).put("sdf2w123", 21311).put("key", "name");
        byte[] buffer = json5Object.toBytes();
        ArrayList<Byte> list = new ArrayList<>();
        for(byte b : buffer) {
            list.add(b);
        }
        Random rand = new Random(System.currentTimeMillis());
        for(int i = 0; i < 1000; ++i) {
            list.add((byte)rand.nextInt(255));
        }
        byte[] overBuffer = new byte[list.size()];
        for(int i = 0, n = list.size(); i < n; ++i) {
            overBuffer[i] = list.get(i);
        }
        JSON5Object fromOverBuffer = new JSON5Object(overBuffer);
        assertArrayEquals(json5Object.toBytes(), fromOverBuffer.toBytes());

    }


    @Test
    public void jsonArrayAndCSonParsingTest() {
        JSONArray numberJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        JSONArray objectJsonArray = new JSONArray();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSONObject().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }

        numberJsonArray = new JSONArray(numberJsonArray.toString());
        JSON5Array numberJSON5Array = new JSON5Array(new JSON5Array(numberJsonArray.toString()).toBytes());
        assertEquals(numberJsonArray.length(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.length(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = new JSONArray(objectJsonArray.toString());
        System.out.println(objectJsonArray.toString());
        JSON5Array objectJSON5Array = new JSON5Array(new JSON5Array(objectJsonArray.toString()).toBytes());


        assertEquals(objectJsonArray.length(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.length(); i < n; ++i) {
            System.out.println(i);
            JSONObject jao = objectJsonArray.getJSONObject(i);
            JSON5Object cao = objectJSON5Array.getJSON5Object(i);
            System.out.println(jao.toString());
            System.out.println(cao.toString());

            assertEquals(jao.getString("str"), cao.getString("str"));
            assertEquals(jao.optString("true"), cao.getString("true"));
            assertEquals(jao.getBoolean("true"), cao.getBoolean("true"));
            assertEquals(jao.getBoolean("false"), cao.getBoolean("false"));
            assertEquals(jao.getLong("random"), cao.getLong("random"));
            assertEquals(Double.valueOf(jao.getDouble("float")),Double.valueOf(cao.getDouble("float")));
            assertEquals(Float.valueOf(jao.getFloat("float")),Float.valueOf(cao.getFloat("float")));
            assertEquals(jao.getInt("float"), cao.getInteger("float"));
        }
    }


    @Test
    public void booleanInArray() {
        JSON5Array JSON5Array = new JSON5Array(WritingOptions.json5().setPretty(false));
        JSON5Array.add(true);
        JSON5Array.add(false);
        JSON5Array.add(true);
        assertEquals(JSON5Array.toString(), "[true,false,true]");

        JSON5Object json5Object = new JSON5Object(WritingOptions.json().setPretty(false));
        json5Object.put("true", true);
        assertEquals(json5Object.toString(), "{\"true\":true}");





    }






}