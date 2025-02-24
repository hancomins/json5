package com.hancomins.json5;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class JSONWriterTest {
/*
    @Test
    public void testWriteSimple() {

        JSON5Object json5Object = new JSON5Object();
        JSON5Array csonArray = new JSON5Array();
        csonArray.add("a");
        csonArray.add("b");
        csonArray.add("c");
        csonArray.add(new JSON5Object().put("ok", "123").put("array", new JSON5Array().put("a").put("11")));
        json5Object.put("array", csonArray);
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
    public void testWriteCommentInCSONArray() {


        JSON5Array csonArray = new JSON5Array();
        csonArray.put(0);
        csonArray.put(new JSON5Object());
        csonArray.put(1);
        csonArray.put(new JSON5Array());
        csonArray.setCommentForValue(0, "comment before a value at index 0");
        csonArray.setCommentAfterValue(0, "comment after a value at index 0");
        csonArray.setCommentForValue(1, "comment before a value at index 1");
        csonArray.setCommentAfterValue(1, "comment after a value at index 1");
        csonArray.setCommentForValue(3, "comment before a value at index 3");
        csonArray.setCommentAfterValue(3, "comment after a value at index 3");

        JSONWriter jsonWriter = new JSONWriter(WritingOptions.json5().setUnprettyArray(false));
        JSONWriter.writeJSONElement(csonArray, jsonWriter);
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