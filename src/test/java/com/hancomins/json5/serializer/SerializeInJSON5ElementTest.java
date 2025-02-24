package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeInJSON5ElementTest {
     @JSON5Type(explicit = true)
    public static class JSON5Class {
        @JSON5Value
        String name;
        @JSON5Value
        String value;

        @JSON5Value("bundle.value")
        String data;
    }

    @Test
    public void testSerializeInJSON5Element() throws Exception {

        JSON5Class json5Class = new JSON5Class();
        json5Class.name = "name";
        json5Class.value = "value";
        long millis = System.currentTimeMillis();
        json5Class.data = millis + "";
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("json5Class", json5Class);

        JSON5Object json5Object2 = JSON5Object.fromObject(json5Class);
        System.out.println(json5Object2.toString());

        System.out.println(json5Object.toString());

        assertEquals( "name", json5Object.getJSON5Object("json5Class").getString("name"));
        assertEquals( "value", json5Object.getJSON5Object("json5Class").getString("value"));
        assertEquals( millis + "", json5Object.getJSON5Object("json5Class").getJSON5Object("bundle").getString("value"));

        JSON5Class newObject = json5Object.getObject("json5Class", JSON5Class.class);

        assertEquals( "name", newObject.name);
        assertEquals( "value", newObject.value);
        assertEquals( millis + "", newObject.data);


        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array.add(json5Class);

        System.out.println(JSON5Array.toString());

        assertEquals( "name", JSON5Array.getJSON5Object(0).getString("name"));
        assertEquals( "value", JSON5Array.getJSON5Object(0).getString("value"));
        assertEquals( millis + "", JSON5Array.getJSON5Object(0).getJSON5Object("bundle").getString("value"));


        ArrayList<JSON5Class> newArrayList = new ArrayList<>();
        newArrayList.add(json5Class);
        newArrayList.add(json5Class);

        json5Object.put("json5ClassList", newArrayList);

        System.out.println(json5Object.toString());

        assertEquals( "name", json5Object.getJSON5Array("json5ClassList").getJSON5Object(0).getString("name"));
        assertEquals( "value", json5Object.getJSON5Array("json5ClassList").getJSON5Object(0).getString("value"));
        assertEquals( millis + "", json5Object.getJSON5Array("json5ClassList").getJSON5Object(0).getJSON5Object("bundle").getString("value"));

    }


    @Test
    public void testGetList() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Array JSON5Array = new JSON5Array();
        for(int i = 0; i < 10; i++) {
            JSON5Array.put((i % 2 == 0 ? "+" : "-") + i + "");
        }
        json5Object.put("list", JSON5Array);
        List<Float> list=  json5Object.getList("list", Float.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( (i % 2 == 0 ? "" : "-") + i + ".0", list.get(i).toString());
        }
    }

    @Test
    public void testGetBooleanList() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Array JSON5Array = new JSON5Array();
        for(int i = 0; i < 10; i++) {
            JSON5Array.put((i % 2 == 0));
        }
        json5Object.put("list", JSON5Array);
        List<Boolean> list=  json5Object.getList("list", Boolean.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( (i % 2 == 0), list.get(i).booleanValue());
        }
    }

    @Test
    public void testGetObjectOfJSON5Class() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Array JSON5Array = new JSON5Array();
        for(int i = 0; i < 10; i++) {
            JSON5Class json5Class = new JSON5Class();
            json5Class.name = "name" + i;
            json5Class.value = "value" + i;
            json5Class.data = i + "";
            JSON5Array.put(json5Class);
        }
        json5Object.put("list", JSON5Array);
        System.out.println(json5Object.toString());
        List<JSON5Class> list=  json5Object.getList("list", JSON5Class.class);
        for(int i = 0; i < 10; i++) {
            assertEquals( "name" + i, list.get(i).name);
            assertEquals( "value" + i, list.get(i).value);
            assertEquals( i + "", list.get(i).data);
        }


        List<String> stringList=  json5Object.getList("list", String.class);
        for(int i = 0; i < 10; i++) {
            System.out.println(stringList.get(i));
        }

    }

}
