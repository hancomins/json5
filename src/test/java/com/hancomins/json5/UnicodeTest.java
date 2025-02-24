package com.hancomins.json5;

import com.hancomins.json5.options.ParsingOptions;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UnicodeTest  (성공)")
public class UnicodeTest {

    // 리소스의 config-store-test.json5 파일을 string 으로 읽어오는 메서드.
    public String readConfigStoreTest() {
        try(InputStream stream = getClass().getClassLoader().getResourceAsStream("config-store-test.json5")) {
            byte[] buffer = new byte[stream.available()];
            stream.read(buffer);
            return new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Test
    public void testBrokenString() {
        JSON5Object json5Object = new JSON5Object("{a:'uceab'}", ParsingOptions.json5());
        JSON5Array JSON5Array = new JSON5Array("[\"uceab\"]", ParsingOptions.json5());

        assertEquals("uceab", json5Object.get("a"));
        assertEquals("uceab", JSON5Array.get(0));

    }

    @Test
    public void testHex() {
        JSON5Object json5ObjectHexString = new JSON5Object("{a:'0xceab'}", ParsingOptions.json5());
        JSON5Array JSON5Array = new JSON5Array("[0xceab]", ParsingOptions.json5());
        JSON5Array JSON5ArrayHexString = new JSON5Array("[0xceab, 0x0f]", ParsingOptions.json5());

        assertEquals('캫', json5ObjectHexString.optChar("a"));
        assertEquals(52907, JSON5Array.getInt(0));
        assertEquals(52907, JSON5ArrayHexString.optInt(0));
        assertEquals(52907, JSON5ArrayHexString.optLong(0));
        assertEquals(-12629, JSON5ArrayHexString.optShort(0));
        assertEquals(15, JSON5ArrayHexString.optByte(1));
        assertEquals( Float.valueOf( 52907), Float.valueOf( JSON5ArrayHexString.optFloat(0)));
        assertEquals( Double.valueOf( 52907), Double.valueOf( JSON5ArrayHexString.optDouble(0)));



    }

    @Test
    public void testUnicode() {
        JSON5Object json5Object = new JSON5Object("{a:'\\uD83D\\uDE0A', b: '\\uceab'}", ParsingOptions.json5());
        JSON5Array JSON5Array = new JSON5Array("['\\uD83D\\uDE0A']", ParsingOptions.json5());

        assertEquals("😊", json5Object.get("a"));
        assertEquals("😊", JSON5Array.get(0));

        System.out.println(json5Object.toString());

        json5Object = new JSON5Object(json5Object.toString(), ParsingOptions.json5());
        JSON5Array = new JSON5Array(JSON5Array.toString(), ParsingOptions.json5());

        assertEquals("😊", json5Object.get("a"));
        assertEquals("😊", JSON5Array.get(0));

        json5Object = new JSON5Object(json5Object.toString(WritingOptions.json()));
        JSON5Array = new JSON5Array(JSON5Array.toString(WritingOptions.json()));

        assertEquals("😊", json5Object.get("a"));
        assertEquals("😊", JSON5Array.get(0));

        assertEquals("캫", json5Object.get("b"));

        String pure = "{\"a\":\"하\\uD83D\\uDE0A하\", \"b\": \"\\uceab\"}";
        System.out.println(pure);
        json5Object = new JSON5Object(pure);


        assertEquals("하😊하", json5Object.get("a"));
        assertEquals("캫", json5Object.get("b"));



    }
}
