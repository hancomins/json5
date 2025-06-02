package com.hancomins.json5;

import com.hancomins.json5.options.ParsingOptions;
import com.hancomins.json5.options.WritingOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5TypeObjectTest (성공)")
public class JSON5Test {


    @Test
    public void testA() {
        JSON5Array JSON5Array = new JSON5Array("[\"value5!\\n\\tbreak line\"\n]", ParsingOptions.json5());
        assertEquals("value5!\n\tbreak line", JSON5Array.getString(0));

        JSON5Array = new JSON5Array("[\"value5!\n\tbreak line\"]", ParsingOptions.json5().setIgnoreControlCharacters(true));
        assertEquals("value5!break line", JSON5Array.getString(0));


        JSON5Array = new JSON5Array("[/* for value */ \"value5!\n\tbreak line\" /* after value */]", ParsingOptions.json5().setIgnoreControlCharacters(true).setSkipComments(true));
        assertNull(JSON5Array.getCommentForValue(0));
        assertNull(JSON5Array.getCommentAfterValue(0));

    }


    @Test
    public void test() {
        JSON5Object json5Object = new JSON5Object("{key: \"value\", 'key 2': \"value2\", key3: 'value3'," +
                " key4: value4 ," +
                " 'key5': \"value5!\\tbreak line\", object: {key: value,}, 'byte[]': [+1,+2,+3,+4,5,6,7,8,9,10,Infinity,NaN,],  }", WritingOptions.json5());

        assertEquals("value",json5Object.get("key"));
        assertEquals("value2",json5Object.get("key 2"));
        assertEquals("value3",json5Object.get("key3"));
        assertEquals("value4",json5Object.get("key4"));
        assertEquals("value5!\tbreak line",json5Object.get("key5"));
        assertEquals(12,json5Object.getJSON5Array("byte[]").size());
        JSON5Array array =  json5Object.getJSON5Array("byte[]");
        assertTrue(Double.isInfinite(array.getDouble(10)));
        assertTrue(Double.isNaN(array.getDouble(11)));



    }

    @Test
    public void testTopComment() {
        JSON5Object json5Object = new JSON5Object("// 루트코멘트 \n { \n" +
                "// 코멘트입니다. \n " +
                " key: \"value\" }" , WritingOptions.json5());

        assertEquals(" 루트코멘트 ",json5Object.getHeaderComment());

        json5Object = new JSON5Object("// 루트코멘트 \n// 코멘트입니다222. \n" +
                "  /* 코\n멘\n트\r\n는\n\t주\n석\n\n\n\n*/" +
                "{ \n" +
                "// 코멘트입니다. \n" +
                " key: \"value\" }\n\n\n\n\n\r\n\t\t\t\t\t ", WritingOptions.json5()   );

        assertEquals(" 루트코멘트 \n 코멘트입니다222. \n 코\n" +
                "멘\n" +
                "트\r\n" +
                "는\n" +
                "\t주\n" +
                "석\n\n\n\n",json5Object.getHeaderComment());

        assertEquals("value",json5Object.get("key"));
    }

    @Test
    public void testKeyCommentSimple() {
        //JSON5Object json5Object = new JSON5Object("{key:'value', /* 여기에도 코멘트가 존재 가능*/ } /** 여기도 존재가능 **/"  );

        JSON5Object json5Object = new JSON5Object("{key:'value',} ", WritingOptions.json5());
        assertEquals("value", json5Object.get("key"));

        json5Object = new JSON5Object("{key:'value', // 코멘트X \n } /* 코멘트별*/ // 코멘트", WritingOptions.json5());
        assertEquals("value", json5Object.get("key"));
        assertEquals(" 코멘트별\n 코멘트", json5Object.getFooterComment());

        json5Object = new JSON5Object("{key:'value' } // 코멘트 "  , WritingOptions.json5());
        assertEquals(" 코멘트 ", json5Object.getFooterComment());
        json5Object = new JSON5Object("{key:'value',} // 코멘트 \n // 코멘트2" , WritingOptions.json5());
        assertEquals(" 코멘트 \n 코멘트2", json5Object.getFooterComment());
    }

    @Test
    public void testArrayComment() {

        JSON5Object jsonObject = new JSON5Object("{\"key\": \"3 \"}");
        System.out.println(jsonObject.get("key"));


        JSON5Array jsonArray = new JSON5Array("[1,,2,3,4,5,6,7,8,9,10,Infinity,NaN,]"  );
        Object obj =  jsonArray.get(1);

        JSON5Array JSON5Array = null;
        JSON5Array = new JSON5Array("[//index1\n1,2,3,4,5,6,7,8,9,10,Infinity,NaN,] // 코멘트 \n // 코멘트2" , WritingOptions.json5());
        assertEquals("index1", JSON5Array.getCommentForValue(0));


        JSON5Array = new JSON5Array("/*테*///스\n" +
                "/*트*/[//index1\n" +
                "1\n" +
                "//index1After\n" +
                ",,/* 이 곳에 주석 가능 */,\"3 \"/*index 3*/,4,5,6,7,8,9,10,11," +
                "/*오브젝트 시작*/{/*알수없는 영역*/}/*오브젝트끝*/,13,//14\n" +
" {/*123*/123:456//456\n,},/*15배열로그*/[,,]/*15after*/,[],[],-Infinity,NaN," +
                ",[{},{}],[,/*index22*/],[,/*index23*/]/*index23after*/,24,[,]//index25after\n," +
                "{1:2,//코멘트\n}//코멘트\n,] // 코멘트 \n // 코멘트2",  ParsingOptions.json5());
        System.out.println(JSON5Array);
        assertEquals("index1", JSON5Array.getCommentForValue(0));
        assertEquals("테\n스\n트", JSON5Array.getHeaderComment());
        assertEquals("index1After", JSON5Array.getCommentAfterValue(0));
        assertEquals(1, JSON5Array.getInt(0));

        assertNull(JSON5Array.get(1));
        assertNull(JSON5Array.get(2));
        assertEquals(" 이 곳에 주석 가능 ", JSON5Array.getCommentForValue(2));
        assertEquals("3 ", JSON5Array.get(3));
        assertEquals(3, JSON5Array.getInt(3));
        assertEquals("index 3", JSON5Array.getCommentAfterValue(3));

        assertEquals("오브젝트 시작", JSON5Array.getCommentForValue(12));
        assertEquals("오브젝트끝", JSON5Array.getCommentAfterValue(12));
        assertEquals("오브젝트 시작", JSON5Array.getJSON5Object(12).getHeaderComment());
        //assertEquals("알수없는 영역", JSON5Array.getJSON5Object(12).getCommentAfterElement().getBeforeComment());
        assertEquals("오브젝트끝", JSON5Array.getJSON5Object(12).getFooterComment());

        assertEquals("오브젝트끝", JSON5Array.getCommentAfterValue(12));

        assertEquals(Double.NEGATIVE_INFINITY, JSON5Array.get(18));
        assertEquals(Double.NaN, JSON5Array.get(19));


        JSON5Array idx15Array = JSON5Array.getJSON5Array(15);
        assertEquals("15배열로그", JSON5Array.getCommentForValue(15));
        assertEquals("15배열로그",idx15Array.getHeaderComment());
        assertEquals("15after", JSON5Array.getCommentAfterValue(15));
        assertNull(idx15Array.get(0));
        assertNull(idx15Array.get(1));


        assertTrue(Double.isInfinite(JSON5Array.getDouble(18)));
        assertTrue(Double.isNaN(JSON5Array.getDouble(19)));





    }


    @Test
    public void toStringTest1() {
        JSON5Object json5Object = new JSON5Object("{array:/*코멘트 값 앞*/[]/*코멘트 뒤*/,  key:/*코멘트앞*/{}/*코멘트*/} ", WritingOptions.json5());
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals("코멘트 뒤",json5Object.getCommentAfterValue("array"));
        json5Object = new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5());

        assertEquals("코멘트 값 앞",json5Object.getCommentForValue("array"));
        assertEquals("코멘트 뒤",json5Object.getCommentAfterValue("array"));
        assertEquals("코멘트",json5Object.getCommentAfterValue("key"));
        assertEquals("코멘트앞",json5Object.getCommentForValue("key"));

    }

    @Test
    public void toStringTest() {
        String jsonString =
                "//인덱스\n//야호!!여기가 처음이다." +
                        "\n{ //키1_앞\n" +
                            "키1/*키1_뒤*/: " +
                    "/*값1앞*/값1/*값1뒤*/," +
                    "키2:[/*값2[0]*/1/*2[0]값*/,/*값2[1]*/2/*2[1]값*/,/*값2[2]*/3/*2[2]값*/,/*값2[3]*/4,/*값2[4]*/{}/*[4]값2*/," +
                        "{/*키2.1*/키2.1/*2.1키*/:/*값2.1*/값2.1/*2.1값*/,키2.2 /*\\\n\t\t\t2.2키\n\t\t\ttestㅇㄴㅁㄹㅇㄴㅁㄹㄴㅇㄻㅇㄴㄹ\n\t\t\tsdafasdfadsfdasㅇㄴㅁㄹㄴㅇㄻㄴㅇㄹ\n\\*/:/*값2.2*/{키2.2.1/*2.2.1키*/:값2.2.1/*2.2.1값*/}/*2.2값*/" +
                                        ",/*키2.3*/키2.3/*2.3키*/:/*값2.3*/{/*키2.3.1*/키2.3.1/*2.3.1키*/:값2.3.1/*2.3.1값*/}/*2.3값*/}/*2[5]값*/,[],/*배열 안에 배열*/[]/*배열 안에 배열 \n끝*/,// 배열 마지막 \n]/*값2*/," +
                "}//테일. 어쩌고저쩌고";
        // array  파싱하는 과정에서 빈 공간은 건너뛰는 것 같음...


        JSON5Object json5Object = new JSON5Object(jsonString, WritingOptions.json5());

       System.out.println(json5Object.toString());

    }

    @Test
    public void testMultilineComment() {
        String json5Str = "{ \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\"//값 코멘트 뒤\n,key2: \"val/* ok */ue2\",/*array코멘트*/array:[1,2,3,4,Infinity]//array코멘트 값 뒤\n,/*코멘트array2*/array2/*코멘트array2*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/,/* 오브젝트 */ object " +
                "// 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3'" +
                " /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123 */,\"LFARRAY\":[\"sdfasdf \\\n123\"]  ,  \n /*123*/ } /* 꼬리 다음 코멘트 */";
        JSON5Object json5Object = new JSON5Object(json5Str);
        assertEquals(" 코멘트입니다. \n222 ",json5Object.getCommentForKey("key"));
        assertEquals("array코멘트", json5Object.getCommentForKey("array"));
        assertEquals("array코멘트 값 뒤",json5Object.getCommentAfterValue("array"));

        System.out.println(json5Object.toString());
        // json5Object.getCommentOfKey("key");


    }

    @Test
    public void lineCommentTest() {
        String comment = "{key: // 값 앞 코멘트 \n {}  // 값 뒤 코멘트 \n ,// key2 코멘트\n key2 : 'value2' // key2 comment \n } // 꼬리 다음 코멘트";
        JSON5Object json5Object = new JSON5Object(comment);

    }


    @Test
    public void testKeyComment() throws IOException {

        // todo "// 이곳은?" 코멘트를 무시하도록 처리해야한다.

        //String json5StrPRe = "/*루트코멘트*/{ \n//값 앞 코멘트\nkey/*키 뒤 코멘트 */: /* 오브젝트 값 이전 코멘트 */ {p: 'ok'\n, // 이곳은? \n } /* 값 뒤 코멘트 */, // key3comment \n 'key3' /*key3 다음 코멘트*/: 'value3' /*123 */ } /* 꼬리 다음 코멘트 */";

        //JSON5Object json5ObjectPre = new JSON5Object(json5StrPRe, WritingOptions.json5());


        //System.out.println(json5ObjectPre.getCommentForKey("key3"));
       // System.out.println(json5ObjectPre.toString());




        String json5StrB = "{ /*Array 키 앞*/array2/*Array 키 뒤*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/}";
        JSON5Object json5ObjectB = new JSON5Object(json5StrB, WritingOptions.json5());
        System.out.println(json5ObjectB.toString());


        // todo 제거 필요
        //if(1 < 2) return;


        String json5Str = "/** 헤더 코멘트 **/ { \n" +
                "/* 코멘트입니다. */\n //222 \n " +
                " key: /* 값 코멘트 */ \"value\"//값 코멘트 뒤\n,key2: \"val/* ok */ue2\",/*array코멘트*/array:[1,2,3,4,Infinity],/*코멘트array2*/array2/*코멘트array2*/:/*코멘트array2b*/[1,2,3,4]/*코멘트array2a*/,/* 오브젝트 */ object " +
                "// 오브젝트 코멘트 \n: /* 오브젝트 값 이전 코멘트 */ { p : 'ok' \n, // 이곳은? \n } // 오브젝트 코멘트 엔드 \n  , // key3comment \n 'key3'" +
                " /*이상한 코멘트*/: // 값 앞 코멘트 \n 'value3' // 값 뒤 코멘트 \n /*123 */,\"LFARRAY\":[\"sdfasdf \\\n123\"]  ,  \n /*123*/ } /* 꼬리 다음 코멘트 */";


        JSON5Object origin = new JSON5Object(json5Str);
        System.out.println(origin);
        assertEquals(" 오브젝트 코멘트 ",origin.getCommentAfterKey("object"));
        assertEquals(" key3comment ",origin.getCommentForKey("key3"));
        //System.out.println(origin);
        JSON5Object json5Object = new JSON5Object(json5Str);

        assertEquals(" 코멘트입니다. \n222 ",json5Object. getCommentForKey("key"));

        // ANSI escape code for green text
        String greenText = "\u001B[32m";
        // ANSI escape code to reset the color
        String resetText = "\u001B[0m";

        System.out.println(greenText + json5Object.toString() + resetText);

        StringReader stringReader = new StringReader(json5Object.toString());
        BufferedReader  bufferedReader = new BufferedReader(stringReader);
        String line = null;
        int lineNumber = 0;
        while((line = bufferedReader.readLine()) != null) {
            System.out.println(lineNumber++ + " : " + line);
        }


        
        json5Object = new JSON5Object(json5Object.toString() , WritingOptions.json5());
        // 초록섹으로 System.out.println(json5Object.toString());




        assertEquals(" 오브젝트 코멘트 ",json5Object.getCommentAfterKey("object"));
        System.out.println(json5Object.toString(WritingOptions.json5()));

        assertEquals(" 코멘트입니다. \n222 ",json5Object.getCommentForKey("key"));
        assertEquals(" 값 코멘트 ",json5Object.getCommentForValue("key"));
        assertEquals("값 코멘트 뒤",json5Object.getCommentAfterValue("key"));



        assertEquals("array코멘트",json5Object.getCommentForKey("array"));
        assertNull(json5Object.getCommentForKey("key2"));
        assertNull(json5Object.getCommentForValue("key2"));
        assertEquals("val/* ok */ue2",json5Object.getString("key2"));
        assertEquals(" key3comment ",json5Object.getCommentForKey("key3"));
        assertEquals("이상한 코멘트",json5Object.getCommentAfterKey("key3"));
        assertEquals("이상한 코멘트",json5Object.getCommentAfterKey("key3"));
        assertEquals(" 값 앞 코멘트 ",json5Object.getCommentForValue("key3"));
        assertEquals(" 값 뒤 코멘트 \n123 ",json5Object.getCommentAfterValue("key3"));
        System.out.println(json5Object.toString(WritingOptions.json5()));
        assertEquals(" 꼬리 다음 코멘트 ",json5Object.getFooterComment());


        assertEquals(" 오브젝트 코멘트 ",  json5Object.getCommentAfterKey("object"));
        //CommentObject keyCommentObject = json5Object.getCommentObjectOfKey("object");
        assertEquals(" 오브젝트 ", json5Object.getCommentForKey("object"));
        assertEquals(" 오브젝트 값 이전 코멘트 ", json5Object.getCommentForValue("object"));
        assertEquals(" 오브젝트 코멘트 엔드 ", json5Object.getCommentAfterValue("object"));
        JSON5Object subObject =  json5Object.getJSON5Object("object");
        assertEquals("ok",subObject.get("p"));

        //CommentObject valueCommentObject = json5Object.getCommentObjectOfValue("object");
        assertEquals(json5Object.getCommentForValue("object"),subObject.getHeaderComment());
        //assertEquals("이곳은?",subObject.getCommentAfterElement().getBeforeComment());
        assertEquals(" 오브젝트 코멘트 엔드 ",subObject.getFooterComment());

        //assertEquals(" 오브젝트 코멘트 ",  keyCommentObject.getTrailingComment());


        assertEquals(origin.toString(WritingOptions.json5()), new JSON5Object(json5Object.toString(WritingOptions.json5()), WritingOptions.json5()).toString(WritingOptions.json5()));




        String obj = origin.toString(WritingOptions.json());
        System.out.println(obj);
        long start = System.currentTimeMillis();
        /*start = System.currentTimeMillis();
        JSON5Object json5Objects = null;// = new JSON5Object(obj);
        for(int i = 0; i < 1000000; ++i) {
            NoSynchronizedStringReader stringReader = new NoSynchronizedStringReader(obj);
            json5Objects = (JSON5Object) PureJSONParser.parsePureJSON(stringReader);
            stringReader.close();
            //JSON5Object json5Objects = new JSON5Object(obj);
            //json5Objects.toString();
            String aaa = "";
            aaa.trim();
        }
        System.out.println("json5 : " + json5Objects.toString());

        System.out.println("json5 : " + (System.currentTimeMillis() - start));
*/





    }


    @Test
    public void objectInArrayCommentTest() {
        JSON5Array JSON5Array = new JSON5Array(WritingOptions.json5());
        JSON5Array.put(new JSON5Object());
        JSON5Array.setCommentForValue(0,"배열값0 앞 코멘트");
        JSON5Array.setCommentAfterValue(0,"배열값0 뒤 코멘트");
        JSON5Array.put(123);
        JSON5Array.setCommentForValue(1,"배열값1 앞 코멘트");
        JSON5Array.setCommentAfterValue(1,"배열값1 뒤 코멘트");
        JSON5Array.put(new JSON5Array());
        JSON5Array.setCommentForValue(2,"배열값2 앞 코멘트");
        JSON5Array.setCommentAfterValue(2,"배열값2 뒤 코멘트");

        JSON5Array.put(new JSON5Object().put("key","value").setComment("key", "키 앞 코멘트").setCommentAfterKey("key","키 뒤 코멘트"));
        JSON5Array.setCommentForValue(3,"배열값3 앞 코멘트");
        JSON5Array.setCommentAfterValue(3,"배열값3 뒤 코멘트");


        System.out.println(JSON5Array.toString());



        assertEquals("배열값0 앞 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentForValue(0));
        assertEquals("배열값0 뒤 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentAfterValue(0));
        assertEquals("배열값1 앞 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentForValue(1));
        assertEquals("배열값1 뒤 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentAfterValue(1));
        assertEquals("배열값2 앞 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentForValue(2));
        assertEquals("배열값2 뒤 코멘트", new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).getCommentAfterValue(2));

        assertEquals(JSON5Array.toString(), new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).toString());

    }

    @Test
    public void commentTest() {
        JSON5Object json5Object = new JSON5Object(WritingOptions.json5());
        json5Object.put("key","value");
        json5Object.setCommentForKey("key","키 앞 코멘트1");
        json5Object.setCommentAfterKey("key","키 뒤 코멘트1");

        json5Object.setCommentForValue("key","값 앞 코멘트1");
        json5Object.setCommentAfterValue("key","값 뒤 코멘트1");

        JSON5Array JSON5Array = new JSON5Array(WritingOptions.json5());
        json5Object.put("arrayKey", JSON5Array);
        JSON5Object objinObj = new JSON5Object(WritingOptions.json5());
        objinObj.put("key","value");
        objinObj.setCommentForKey("key","키 앞 코멘트");
        objinObj.setCommentAfterKey("key","키 뒤 코멘트");
        objinObj.setCommentForValue("key","값 앞 코멘트");
        objinObj.setCommentAfterValue("key","값 뒤 코멘트");
        assertEquals(objinObj.toString(), new JSON5Object(objinObj.toString(), WritingOptions.json5()).toString());

        JSON5Array.setCommentForValue(0,"배열값 앞 코멘트");
        JSON5Array.setCommentAfterValue(0,"배열값 뒤 코멘트");
        JSON5Array.setCommentForValue(2,"배열값 앞 코멘트2");
        JSON5Array.setCommentAfterValue(2,"배열값 뒤 코멘트2");

        JSON5Array.setCommentForValue(3,"배열값 앞 코멘트3");
        JSON5Array.setCommentAfterValue(3,"배열값 뒤 코멘트3");

        JSON5Array.put(objinObj);
        JSON5Array.put(objinObj);
        JSON5Array.put(123);
        JSON5Array.put(new JSON5Array().put(1).put(2).put(3).put(4).put(5).put(6).put(7).put(8).put(9).put(10).put(Double.POSITIVE_INFINITY).put(Double.NaN).setFooterComment("sdafasdfdasf"));
        objinObj.setFooterComment("오브젝트 뒤 코멘트");
        objinObj.setHeaderComment("오브젝트 앞 코멘트");


        JSON5Array.setHeaderComment("배열 앞 코멘트");
        JSON5Array.setFooterComment("배열 뒤 코멘트");



        assertEquals(JSON5Array.toString(), new JSON5Array(JSON5Array.toString(), WritingOptions.json5()).toString());


        json5Object.setCommentForKey("arrayKey","키 앞 코멘트");
        json5Object.setCommentAfterKey("arrayKey","키 뒤 코멘트");
        json5Object.setCommentForValue("arrayKey","값 앞 코멘트");
        json5Object.setCommentAfterValue("arrayKey","값 뒤 코멘트");

        json5Object.setCommentForKey("arrayKey","키 앞 코멘트\nok");
        json5Object.setCommentAfterKey("arrayKey","키 뒤 코멘트");
        json5Object.setCommentForValue("arrayKey","값 앞 코멘트");
        json5Object.setCommentAfterValue("arrayKey","값 뒤 코멘트");
        System.out.println(json5Object.toString());

       JSON5Object result = new JSON5Object(json5Object.toString(), WritingOptions.json5());
        System.out.println(result.toString());

        assertEquals(json5Object.toString(), result.toString());

    }




}
