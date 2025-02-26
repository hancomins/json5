package com.hancomins.json5;


import com.hancomins.json5.options.ParsingOptions;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("DefaultUse  (성공)")
public class DefaultUse {

    @Test
    public void toBinaryArrayTest() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("number", (short)1000).put("bigNumber", new BigDecimal(10000000000L));

        byte[] bytes = json5Object.toBytes();

        JSON5Object parsedJSON5Object = new JSON5Object(bytes);
        assertEquals(json5Object.get("number"), parsedJSON5Object.get("number"));
        assertEquals((short)1000, parsedJSON5Object.get("number"));

        assertEquals(10000000000L, parsedJSON5Object.getLong("bigNumber"));


    }

    @Test
    public void escapeSequenceTest() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("string", "Hello\\nWorld");
        json5Object.put("string2", "Hello\\World");
        JSON5Array json5Array = new JSON5Array();
        json5Array.add("Hello\\nWorld");
        json5Array.add("Hello\\Wor\"ld");
        json5Object.put("array", json5Array);
        String jsonString = json5Object.toString(WritingOptions.jsonPretty().setUnprettyArray(true));
        System.out.println(jsonString);
        JSON5Object json5ObjetPure = new JSON5Object(jsonString);
        JSON5Object json5ObjectJson = new JSON5Object(jsonString);
        assertEquals("Hello\\World", json5ObjectJson.get("string2"));
        assertEquals(jsonString, json5ObjetPure.toString(WritingOptions.jsonPretty().setUnprettyArray(true)));
        assertEquals(jsonString, json5ObjectJson.toString(WritingOptions.jsonPretty().setUnprettyArray(true)));
        json5ObjectJson.put("string3", "Hello/World");
        json5ObjectJson = new JSON5Object(json5ObjectJson.toString());
        assertEquals("Hello/World", json5ObjectJson.get("string3"));


        json5Object = new JSON5Object();
        json5Object.put("st\"ring'4", "Hello\"World");
        System.out.println(json5Object.toString(WritingOptions.json5().setValueQuote("\"")));

        JSON5Object json5 = new JSON5Object(json5Object.toString(WritingOptions.json5()), ParsingOptions.json5());
        assertEquals("Hello\"World", json5.get("st\"ring'4"));
    }

        @Test
        public void escapeSequenceTest2() {
            JSON5Object json5Object = new JSON5Object();
            json5Object.put("st\"ri\nng'4", "H\nello\"World");
            System.out.println(json5Object.toString());
        }


}
