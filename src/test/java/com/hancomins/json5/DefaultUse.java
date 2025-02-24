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

        JSON5Object parsedCsonObject = new JSON5Object(bytes);
        assertEquals(json5Object.get("number"), parsedCsonObject.get("number"));
        assertEquals((short)1000, parsedCsonObject.get("number"));

        assertEquals(10000000000L, parsedCsonObject.getLong("bigNumber"));


    }

    @Test
    public void escapeSequenceTest() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("string", "Hello\\nWorld");
        json5Object.put("string2", "Hello\\World");
        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array.add("Hello\\nWorld");
        JSON5Array.add("Hello\\Wor\"ld");
        json5Object.put("array", JSON5Array);
        String jsonString = json5Object.toString(WritingOptions.jsonPretty().setUnprettyArray(true));
        System.out.println(jsonString);
        JSON5Object csonObjetPure = new JSON5Object(jsonString);
        JSON5Object json5ObjectJson = new JSON5Object(jsonString);
        assertEquals("Hello\\World",json5ObjectJson.get("string2"));
        assertEquals(jsonString, csonObjetPure.toString(WritingOptions.jsonPretty().setUnprettyArray(true)));
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
}
