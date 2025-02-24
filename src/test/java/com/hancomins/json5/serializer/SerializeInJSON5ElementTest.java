package com.hancomins.json5.serializer;

import com.hancomins.json5.JSON5Array;
import com.hancomins.json5.JSON5Object;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeInJSON5ElementTest {
     @JSON5Type(explicit = true)
    public static class CSONClass {
        @JSON5Value
        String name;
        @JSON5Value
        String value;

        @JSON5Value("bundle.value")
        String data;
    }

    @Test
    public void testSerializeInCSONElement() throws Exception {

        CSONClass csonClass = new CSONClass();
        csonClass.name = "name";
        csonClass.value = "value";
        long millis = System.currentTimeMillis();
        csonClass.data = millis + "";
        JSON5Object json5Object = new JSON5Object();
        json5Object.put("csonClass", csonClass);

        JSON5Object json5Object2 = JSON5Object.fromObject(csonClass);
        System.out.println(json5Object2.toString());

        System.out.println(json5Object.toString());

        assertEquals( "name", json5Object.getJSON5Object("csonClass").getString("name"));
        assertEquals( "value", json5Object.getJSON5Object("csonClass").getString("value"));
        assertEquals( millis + "", json5Object.getJSON5Object("csonClass").getJSON5Object("bundle").getString("value"));

        CSONClass newObject = json5Object.getObject("csonClass", CSONClass.class);

        assertEquals( "name", newObject.name);
        assertEquals( "value", newObject.value);
        assertEquals( millis + "", newObject.data);


        JSON5Array JSON5Array = new JSON5Array();
        JSON5Array.add(csonClass);

        System.out.println(JSON5Array.toString());

        assertEquals( "name", JSON5Array.getJSON5Object(0).getString("name"));
        assertEquals( "value", JSON5Array.getJSON5Object(0).getString("value"));
        assertEquals( millis + "", JSON5Array.getJSON5Object(0).getJSON5Object("bundle").getString("value"));


        ArrayList<CSONClass> newArrayList = new ArrayList<>();
        newArrayList.add(csonClass);
        newArrayList.add(csonClass);

        json5Object.put("csonClassList", newArrayList);

        System.out.println(json5Object.toString());

        assertEquals( "name", json5Object.getJSON5Array("csonClassList").getJSON5Object(0).getString("name"));
        assertEquals( "value", json5Object.getJSON5Array("csonClassList").getJSON5Object(0).getString("value"));
        assertEquals( millis + "", json5Object.getJSON5Array("csonClassList").getJSON5Object(0).getJSON5Object("bundle").getString("value"));

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
    public void testGetObjectOfCSONClass() {
        JSON5Object json5Object = new JSON5Object();
        JSON5Array JSON5Array = new JSON5Array();
        for(int i = 0; i < 10; i++) {
            CSONClass csonClass = new CSONClass();
            csonClass.name = "name" + i;
            csonClass.value = "value" + i;
            csonClass.data = i + "";
            JSON5Array.put(csonClass);
        }
        json5Object.put("list", JSON5Array);
        System.out.println(json5Object.toString());
        List<CSONClass> list=  json5Object.getList("list", CSONClass.class);
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
