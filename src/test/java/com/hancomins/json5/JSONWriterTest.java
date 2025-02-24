package com.hancomins.json5;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class JSONWriterTest {
/*
    @Test
    public void testWriteSimple() {

        JSON5Object json5Object = new JSON5Object();
        JSON5Array json5Array = new JSON5Array();
        json5Array.add("a");
        json5Array.add("b");
        json5Array.add("c");
        json5Array.add(new JSON5Object().put("ok", "123").put("array", new JSON5Array().put("a").put("11")));
        json5Object.put("array", json5Array);
        //json5Object.setCommentAfterValue("array", "comment after array");

        json5Object.put("emptyArray", new JSON5Array());
        json5Object.put("emptyObject", new JSON5Object());
        JSONWriter jsonWriter = new JSONWriter(WritingOptions.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(json5Object, jsonWriter);

        System.out.println(jsonWriter.toString());

        JSON5Object json5Object2 = new JSON5Object(jsonWriter.toString(), WritingOptions.json5().setPretty(true).setUnprettyArray(false));
        System.out.println(json5Object2.toString());

        assertEquals(json5Object, json5Object2);
        assertEquals(json5Object.toString(WritingOptions.json5().setPretty(true).setUnprettyArray(false)), json5Object2.toString());
    }

    @Test
    public void testWriteCommentInJSON5Array() {


        JSON5Array json5Array = new JSON5Array();
        json5Array.put(0);
        json5Array.put(new JSON5Object());
        json5Array.put(1);
        json5Array.put(new JSON5Array());
        json5Array.setCommentForValue(0, "comment before a value at index 0");
        json5Array.setCommentAfterValue(0, "comment after a value at index 0");
        json5Array.setCommentForValue(1, "comment before a value at index 1");
        json5Array.setCommentAfterValue(1, "comment after a value at index 1");
        json5Array.setCommentForValue(3, "comment before a value at index 3");
        json5Array.setCommentAfterValue(3, "comment after a value at index 3");

        JSONWriter jsonWriter = new JSONWriter(WritingOptions.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(json5Array, jsonWriter);
        System.out.println(jsonWriter.toString());

    }

    @Test
    public void testWriteComment() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("a", "b");
        json5Object.setCommentForKey("a", "comment for a");
        json5Object.setCommentAfterKey("a", "comment after a");
        json5Object.setCommentForValue("a", "comment before a");
        json5Object.setCommentAfterValue("a", "comment after a");

        JSONWriter jsonWriter = new JSONWriter(WritingOptions.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(json5Object, jsonWriter);
        System.out.println(jsonWriter.toString());



        json5Object = new JSON5Object();
        json5Object.put("a", new JSON5Object());
        json5Object.setCommentForKey("a", "comment for a key");
        json5Object.setCommentAfterKey("a", "comment after a key");
        json5Object.setCommentForValue("a", "comment before a value");
        json5Object.setCommentAfterValue("a", "comment after a value");

        jsonWriter = new JSONWriter(WritingOptions.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(json5Object, jsonWriter);
        System.out.println(jsonWriter.toString());



    }

*/
}