package com.hancomins.json5;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IteratorTest (성공)")
public class IteratorTest {
    @Test
    public void csonArrayIteratorTest() {
        ArrayList<Object> list = new ArrayList<>();
        // make random value in list
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 1000; i++) {
            // type 0 : int, 1 : float, 2 : string, 3 : boolean, 4: JSON5Object, 5: JSON5Array
            int type = random.nextInt(7);
            switch (type) {
                case 0:
                    list.add(random.nextInt());
                    break;
                case 1:
                    list.add(random.nextFloat());
                    break;
                case 2:
                    list.add(random.nextInt() + "");
                    break;
                case 3:
                    list.add(random.nextBoolean());
                    break;
                case 4:
                    list.add(new JSON5Object());
                    break;
                case 5:
                    list.add(new JSON5Array());
                    break;
                case 6:
                    list.add(null);
                    break;
                default:
                    break;
            }
        }

        JSON5Array JSON5Array = new JSON5Array(list);
        JSON5Object json5Object = new JSON5Object();
        int no = 0;
        for(Object obj : JSON5Array) {
            json5Object.put(++no + "", obj);
        }

        Iterator<Object> iteratorArray = JSON5Array.iterator();
        while(iteratorArray.hasNext()) {
            Object obj = iteratorArray.next();
            System.out.println(obj);
            assertTrue(json5Object.containsValue(obj));

        }

        Iterator<Object> iteratorMap = json5Object.iterator();
        while(iteratorMap.hasNext()) {
            Object obj = iteratorMap.next();
            assertTrue(JSON5Array.contains(obj));
            iteratorMap.remove();
        }


        assertEquals(0, json5Object.size());

        byte[] bytes = new byte[random.nextInt(1000) + 100];
        random.nextBytes(bytes);
        JSON5Array.put(bytes);
        json5Object.put("bytes", bytes);

        String stringBytes = json5Object.getString("bytes");
        assertFalse(json5Object.containsValue(stringBytes));
        assertFalse(JSON5Array.contains(stringBytes));


        assertTrue(json5Object.containsValueNoStrict(stringBytes));
        assertTrue(JSON5Array.containsNoStrict(stringBytes));





    }

}
