package com.hancomins.json5;

import com.hancomins.json5.options.JSON5WriterOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JSON5WriterTest {

    @Test
    public void simpleWrite() {
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("key", "value");
        json5Object.put("emptyObject", new JSON5Object());
        json5Object.put("emptyArray", new JSON5Array());
        json5Object.put("array", new JSON5Array().put("value1").put("value2").put("value3"));
        json5Object.put("object", new JSON5Object().put("key1", "value1").put("key2", "value2").put("key3", "value3"));

        System.out.println(json5Object.toString(JSON5WriterOption.json()));
        System.out.println(json5Object.toString(JSON5WriterOption.prettyJson()));
        System.out.println(json5Object.toString(JSON5WriterOption.prettyJson().setUnprettyArray(true)));
        System.out.println(json5Object.toString(JSON5WriterOption.json5()));
    }

    @Test
    public void withComment() {
        JSON5Object json5Object = new JSON5Object();
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


        JSON5Object parseredJSON5Object = new JSON5Object(json5Object.toString(JSON5WriterOption.json5()));
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
        parseredJSON5Object = new JSON5Object(json5Object.toString(JSON5WriterOption.prettyJson5()));
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

    }
}
