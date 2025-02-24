package com.hancomins.json5;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5Array 테스트 (성공)")
public class JSON5ArrayTest {

    @Test
    public void set() {
        JSON5Array json5Array = new JSON5Array();
        json5Array.set(100, 123);
        for(int i = 0; i < 100; i++) {
            assertNull(json5Array.get(i));
        }
        assertEquals(123, json5Array.get(100));
        assertEquals(101, json5Array.size());

        json5Array.set(50, "hahaha");



        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("1", 123);
        //jsonObject.getString("1");



    }
}