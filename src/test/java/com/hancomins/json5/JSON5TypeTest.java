package com.hancomins.json5;

import com.hancomins.json5.options.WritingOptions;
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
        JSON5Object jsonObject = new JSON5Object();
        jsonObject.put("11111",1)
        .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSON5Array numberJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSON5Array objectJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSON5Object().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSON5Object(jsonObject.toString());

        JSON5Object json5Object = new JSON5Object(new JSON5Object(jsonObject.toString()).toString(WritingOptions.json()));
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> json5KeySet =json5Object.keySet();
        assertEquals(originalKeySet, json5KeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(jsonObject.get(oriKey),json5Object.get(oriKey) );
        }

        numberJsonArray = jsonObject.getJSON5Array("AN");
        JSON5Array numberJSON5Array = json5Object.getJSON5Array("AN");
        assertEquals(numberJsonArray.size(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.size(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = jsonObject.getJSON5Array("AO");
        JSON5Array objectJSON5Array = json5Object.getJSON5Array("AO");
        assertEquals(objectJsonArray.size(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.size(); i < n; ++i) {
            JSON5Object jao = objectJsonArray.getJSON5Object(i);
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
        JSON5Object jsonObject = new JSON5Object();
        jsonObject.put("11111",1)
                .put("22222",1L).put("33333","3").put("00000", true).put("44444", 4.4f).put("55555", 5.5).put("66666", '6');
        JSON5Array numberJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        jsonObject.put("AN",numberJsonArray);

        JSON5Array objectJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSON5Object().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }
        jsonObject.put("AO",objectJsonArray);


        jsonObject = new JSON5Object(jsonObject.toString());
        System.out.println(jsonObject);

        JSON5Object json5Object = new JSON5Object(new JSON5Object(jsonObject.toString()).toString());
        Set<String> originalKeySet = jsonObject.keySet();
        Set<String> json5KeySet =json5Object.keySet();
        assertEquals(originalKeySet, json5KeySet);
        for(String oriKey : originalKeySet) {
            if(oriKey.startsWith("A")) continue;
            assertEquals(Integer.getInteger(jsonObject.get(oriKey)  + "") ,Integer.getInteger( json5Object.get(oriKey) + ""));
        }

        numberJsonArray = jsonObject.getJSON5Array("AN");
        JSON5Array numberJSON5Array = json5Object.getJSON5Array("AN");
        assertEquals(numberJsonArray.size(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.size(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = jsonObject.getJSON5Array("AO");
        JSON5Array objectJSON5Array = json5Object.getJSON5Array("AO");
        assertEquals(objectJsonArray.size(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.size(); i < n; ++i) {
            JSON5Object jao = objectJsonArray.getJSON5Object(i);
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
    public void jsonArrayAndCSonParsingTest() {
        JSON5Array numberJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            numberJsonArray.put(n);
        }
        JSON5Array objectJsonArray = new JSON5Array();
        for(int i = 0, n = ThreadLocalRandom.current().nextInt(100) + 1; i < n; ++i) {
            objectJsonArray.put(new JSON5Object().put("str", "str").put("true", true).put("false", false).put("random", ThreadLocalRandom.current().nextLong()).put("float", ThreadLocalRandom.current().nextDouble()));
        }

        numberJsonArray = new JSON5Array(numberJsonArray.toString());
        JSON5Array numberJSON5Array = new JSON5Array(new JSON5Array(numberJsonArray.toString()).toString());
        assertEquals(numberJsonArray.size(), numberJSON5Array.size());
        for(int i = 0, n = numberJsonArray.size(); i < n; ++i) {
            assertEquals(numberJsonArray.optString(i), numberJSON5Array.getString(i));
        }

        objectJsonArray = new JSON5Array(objectJsonArray.toString());
        System.out.println(objectJsonArray.toString());
        JSON5Array objectJSON5Array = new JSON5Array(new JSON5Array(objectJsonArray.toString()).toString());


        assertEquals(objectJsonArray.size(), objectJSON5Array.size());
        for(int i = 0, n = objectJsonArray.size(); i < n; ++i) {
            System.out.println(i);
            JSON5Object jao = objectJsonArray.getJSON5Object(i);
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