package com.hancomins.json5;

import com.hancomins.json5.options.JsonParsingOptions;
import com.hancomins.json5.options.ParsingOptions;
import com.hancomins.json5.options.WritingOptions;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON5ParserTest (성공)")
public class JSON5ParserTest  {




    @Test
    public void testUnquoted() {

        char q = '\t';
        System.out.println(Character.isWhitespace(q));

        String jsonKeyUnquoted = "{\n" +
                "  unquoted: 'and you can quote me on that\"',\n" +
                "}";



        JSON5Object json5Object = new JSON5Object(jsonKeyUnquoted, ParsingOptions.json5());
        System.out.println(json5Object.toString());

        assertEquals(json5Object.get("unquoted"), "and you can quote me on that\"");


        String jsonValueUnquoted = "{\n" +
                "  unquoted: 'and you can quote\\\n me on that\"',\n" +
                " unquoted_integer: 123.0\n" +
                "}";



        json5Object = new JSON5Object(jsonValueUnquoted, ParsingOptions.json5());
        System.out.println(json5Object.toString());
        assertEquals(json5Object.get("unquoted"), "and you can quote\n me on that\"");
        assertEquals(Double.valueOf(json5Object.getDouble("unquoted_integer")),  Double.valueOf( 123.0));


        String jsonValueSingleUnquoted = "{\n" +
                "  'singleQuoted': and_you_can_quote_me_on_that\"," +
                " 'singleQuoted_float': 123.0" +
                "}";



        System.out.println(jsonValueSingleUnquoted);


        json5Object = new JSON5Object(jsonValueSingleUnquoted, ParsingOptions.json5());
        System.out.println(json5Object.toString());
        assertEquals(json5Object.get("singleQuoted"), "and_you_can_quote_me_on_that\"");
        assertEquals(Double.valueOf(json5Object.getDouble("singleQuoted_float")),  Double.valueOf( 123.0));





    }

    @Test
    public void testConsecutiveCommas() {

            String json = "{\n" +
                    "  \"consecutiveCommas\": \"are just fine\",,,\n" +
                    " nullValue : /* 이 값은 어디에 */  ,\n" +
                    " arrays: [1,2,,3,],\n" +
                    "}";


            JSON5Object json5Object = new JSON5Object(json, ParsingOptions.json5().setAllowConsecutiveCommas(true));
            System.out.println(json5Object);
            assertEquals("are just fine", json5Object.optString("consecutiveCommas"));
            assertNull(json5Object.get("nullValue"));
            assertEquals(" 이 값은 어디에 ", json5Object.getCommentForValue("nullValue"));
            assertEquals(4, json5Object.optJSON5Array("arrays").size());
            assertEquals(1, json5Object.optJSON5Array("arrays").getInt(0));
            assertEquals(2, json5Object.optJSON5Array("arrays").getInt(1));
            assertNull(json5Object.optJSON5Array("arrays").get(2));
            assertEquals(3, json5Object.optJSON5Array("arrays").getInt(3));

            try {
                json5Object = new JSON5Object(json, ParsingOptions.json5().setAllowConsecutiveCommas(false));
                assertEquals("are just fine", json5Object.optString("consecutiveCommas"));
                fail();
            } catch (Exception e) {

            }
    }

    @Test
    public void testLineBreak() {

        String json = "{\n" +
                //"  // comments\n" +
                "  lineBreaks: \"Look, Mom!\\\nNo \\\\nnewlines!\",\n" +
                "}";



        JSON5Object json5Object = new JSON5Object(json, ParsingOptions.json5());
        assertEquals("Look, Mom!\nNo \\nnewlines!", json5Object.optString("lineBreaks"));

    }

    @Test
    public void testSimpleJson5Parse() {

         String json = "{\n" +
         //"  // comments\n" +
         "  unquoted: 'and you can quote me on that',\n" +
         "  singleQuotes: 'I can use \"double quotes\" here',\n" +
         "  lineBreaks: \"Look, Mom!\\\nNo \\\\nnewlines!\",\n" +
         "  hexadecimal: 0xdecaf,\n" +
         "  leadingDecimalPoint: .8675309, andTrailing: 8675309.,\n" +
         "  positiveSign: +1,\n" +
         "  trailingComma: 'in objects', andIn: ['arrays',],\n" +
         "  \"backwardsCompatible\": \"with JSON\",\n" +
         "}";
         JSON5Object json5Object = new JSON5Object(json, ParsingOptions.json5());
         assertEquals("and you can quote me on that", json5Object.optString("unquoted"));
         assertEquals("I can use \"double quotes\" here", json5Object.optString("singleQuotes"));
         assertEquals("Look, Mom!\nNo \\nnewlines!", json5Object.optString("lineBreaks"));
         assertEquals(0xdecaf, json5Object.optInt("hexadecimal"));
         assertEquals(0.8675309, json5Object.optDouble("leadingDecimalPoint"));
         assertEquals(8675309.0, json5Object.optDouble("andTrailing"));
         assertEquals(1, json5Object.optInt("positiveSign"));
         assertEquals("in objects", json5Object.optString("trailingComma"));
         assertEquals("with JSON", json5Object.optString("backwardsCompatible"));
    }

    @Test
    public void testTrailingComma() {

            String json = "{\n" +
                    "  \"trailingComma\": \"in objects\",\n" +
                    "  \"andIn\": [\"arrays\",],\n" +
                    "}";


            JSON5Object json5Object = new JSON5Object(json, ParsingOptions.json5());
            assertEquals("in objects", json5Object.optString("trailingComma"));
            assertEquals("arrays", json5Object.optJSON5Array("andIn").optString(0));

    }

    @Test
    public void testJSON5Array() {
        String array = "[1,2,3.3,4,5.5]";
        JSON5Array JSON5Array = new JSON5Array(array, ParsingOptions.json5());
        assertEquals(1, JSON5Array.getInt(0));
        assertEquals(2, JSON5Array.getInt(1));
        assertEquals(3.3, JSON5Array.getDouble(2), 0.0001);
        assertEquals(4, JSON5Array.getInt(3));
        assertEquals(5.5, JSON5Array.getDouble(4), 0.0001);

        System.out.println(JSON5Array.toString());

        JSON5Array = new JSON5Array(JSON5Array.toString(), ParsingOptions.json5());
        assertEquals(1, JSON5Array.getInt(0));
        assertEquals(2, JSON5Array.getInt(1));
        assertEquals(3.3, JSON5Array.getDouble(2), 0.0001);
        assertEquals(4, JSON5Array.getInt(3));
        assertEquals(5.5, JSON5Array.getDouble(4), 0.0001);


    }

    @Test
    public void testWeirdString() {
        JSON5Object json5Object = new JSON5Object(WritingOptions.json5());
        json5Object.put("weirdString", "stri \" \n\rng");
        assertEquals("stri \" \n\rng", json5Object.optString("weirdString"));

        System.out.println(json5Object.toString());
        json5Object = new JSON5Object(json5Object.toString(), ParsingOptions.json5());
        System.out.println(json5Object.toString());


    }

    @Test
    public void testNullValue() {
        String complexJson5 = "{\n" +
                "  nullValue: \n\n null\n\n,\n" +
                " okValue: \"ok\",\n" +
                "}";
        JSON5Object json5Object = new JSON5Object(complexJson5, ParsingOptions.json5());
        assertNull(json5Object.opt("nullValue"));
        assertEquals("ok", json5Object.optString("okValue"));
    }

    @Test
    public void testCommentInObject() {
        String complexJson5 = "{\n" +
                "  // This is a comment before key\n" +
                " \"comment\"" +
                " // This is a comment after key\n" +
                ":" +
                "// Comment before value \n" +
                " \"value\" \n" +
                "  // Comment after value\n" +
                "}";

        JsonParsingOptions jsonParsingOptions = ParsingOptions.json5();

        JSON5Object json5Object = new JSON5Object(complexJson5, jsonParsingOptions);

        System.out.println(json5Object);

        assertEquals(" This is a comment before key", json5Object.getCommentForKey("comment"));
        assertEquals(" This is a comment after key", json5Object.getCommentAfterKey("comment"));
        assertEquals(" Comment before value ", json5Object.getCommentForValue("comment"));
        assertEquals(" Comment after value", json5Object.getCommentAfterValue("comment"));

        System.out.println(json5Object);

        json5Object = new JSON5Object(json5Object.toString(), ParsingOptions.json5());

        System.out.println(json5Object);


        assertEquals(" This is a comment before key", json5Object.getCommentForKey("comment"));
        assertEquals(" This is a comment after key", json5Object.getCommentAfterKey("comment"));
        assertEquals(" Comment before value ", json5Object.getCommentForValue("comment"));
        assertEquals(" Comment after value", json5Object.getCommentAfterValue("comment"));

        complexJson5 = "{\n" +
                "  // This is a comment before key\n" +
                " \"comment\"" +
                " // This is a comment after key\n" +
                ":" +
                "// Comment before value \n" +
                " value //Comment after value\n" +
                "}";


        json5Object = new JSON5Object(complexJson5, ParsingOptions.json5());

        System.out.println(json5Object);

        assertEquals(" This is a comment before key", json5Object.getCommentForKey("comment"));
        assertEquals(" This is a comment after key", json5Object.getCommentAfterKey("comment"));
        assertEquals(" Comment before value ", json5Object.getCommentForValue("comment"));
        assertEquals("Comment after value", json5Object.getCommentAfterValue("comment"));

        complexJson5 = "{\n" +
                "  // This is a comment before key\n" +
                " \"comment\"" +
                " // This is a comment after key\n" +
                ":" +
                "// Comment before value \n" +
                " value //Comment after value\n" +
                "}";

        json5Object = new JSON5Object(complexJson5, ParsingOptions.json5());

        System.out.println(json5Object);


        assertEquals(" This is a comment before key", json5Object.getCommentForKey("comment"));
        assertEquals(" This is a comment after key", json5Object.getCommentAfterKey("comment"));
        assertEquals(" Comment before value ", json5Object.getCommentForValue("comment"));
        assertEquals("Comment after value", json5Object.getCommentAfterValue("comment"));

        complexJson5 = "{\n" +
                "  // This is a comment before key\n" +
                " \"comment\"" +
                " // This is a comment after key\n" +
                ":" +
                "// Comment before value \n" +
                " value /*Comment after value*/"
                + " // Comment after value2\n" +
                "}";

        json5Object = new JSON5Object(complexJson5, ParsingOptions.json5());

        System.out.println(json5Object);

        assertEquals(" This is a comment before key", json5Object.getCommentForKey("comment"));
        assertEquals(" This is a comment after key", json5Object.getCommentAfterKey("comment"));
        assertEquals(" Comment before value ", json5Object.getCommentForValue("comment"));
        assertEquals("Comment after value\n Comment after value2", json5Object.getCommentAfterValue("comment"));



    }


    @Test
    public void testComplexJson5Parsing() {
        String complexJson5 = "{\n" +
                "  unquotedKey: 'unquoted string value',\n" +
                "  'singleQuotes': \"can use double quotes inside\",\n" +
                "  nestedObject: {\n" +
                "    array: [1, 2, 3\n, { nestedKey: 'nestedValue' }, ['nested', 'array']],\n" +
                "    boolean: true,\n" +
                "  },\n" +
                "  nullValue: null,\n" +
                //"  // This is a comment\n" +
                "  trailingComma: \n'this is fine',\n" +
                "  trailing1Comma: 'this is fine',\n" +
                "}";

        JSON5Object json5Object = new JSON5Object(complexJson5);

        // Assert basic values
        assertEquals("unquoted string value", json5Object.optString("unquotedKey"));
        assertEquals("can use double quotes inside", json5Object.optString("singleQuotes"));
        assertNull(json5Object.opt("nullValue"));
        assertTrue(json5Object.optJSON5Object("nestedObject").optBoolean("boolean"));

        // Assert nested object and array
        JSON5Object nestedObject = json5Object.optJSON5Object("nestedObject");
        assertNotNull(nestedObject);
        assertEquals(3, nestedObject.optJSON5Array("array").getInt(2));

        // Assert nested array within an array
        JSON5Array nestedArray = nestedObject.optJSON5Array("array").optJSON5Array(4);
        assertEquals("nested", nestedArray.optString(0));

        // Assert nested object within an array
        JSON5Object nestedObjectInArray = nestedObject.optJSON5Array("array").optJSON5Object(3);
        assertEquals("nestedValue", nestedObjectInArray.optString("nestedKey"));

        System.out.println(json5Object);
    }


    @Test
    public void testPerformance() throws IOException {

        if(true) return;
        //String speedTest = "{\"name\":\"John Doe\",\"age\":30,\"isEmployed\":true,\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"},\"phoneNumbers\":[{\"type\":\"home\",\"number\":\"555-555-5555\"},{\"type\":\"work\",\"number\":\"555-555-5556\"}],\"email\":\"johndoe@example.com\",\"website\":\"http://www.johndoe.com\",\"children\":[{\"name\":\"Jane Doe\",\"age\":10,\"school\":{\"name\":\"Elementary School\",\"address\":{\"street\":\"456 School St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"}}},{\"name\":\"Jim Doe\",\"age\":8,\"school\":{\"name\":\"Elementary School\",\"address\":{\"street\":\"456 School St\",\"city\":\"Anytown\",\"state\":\"CA\",\"postalCode\":\"12345\"}}}],\"hobbies\":[\"reading\",\"hiking\",\"coding\"],\"education\":{\"highSchool\":{\"name\":\"Anytown High School\",\"yearGraduated\":2005},\"university\":{\"name\":\"State University\",\"yearGraduated\":2009,\"degree\":\"Bachelor of Science\",\"major\":\"Computer Science\"}},\"workExperience\":[{\"company\":\"Tech Corp\",\"position\":\"Software Engineer\",\"startDate\":\"2010-01-01\",\"endDate\":\"2015-01-01\",\"responsibilities\":[\"Developed web applications\",\"Led a team of 5 developers\",\"Implemented new features\"]},{\"company\":\"Web Solutions\",\"position\":\"Senior Developer\",\"startDate\":\"2015-02-01\",\"endDate\":\"2020-01-01\",\"responsibilities\":[\"Architected software solutions\",\"Mentored junior developers\",\"Managed project timelines\"]}],\"skills\":[{\"name\":\"Java\",\"level\":\"expert\"},{\"name\":\"JavaScript\",\"level\":\"advanced\"},{\"name\":\"Python\",\"level\":\"intermediate\"}],\"certifications\":[{\"name\":\"Certified Java Developer\",\"issuedBy\":\"Oracle\",\"date\":\"2012-06-01\"},{\"name\":\"Certified Scrum Master\",\"issuedBy\":\"Scrum Alliance\",\"date\":\"2014-09-01\"}],\"languages\":[{\"name\":\"English\",\"proficiency\":\"native\"},{\"name\":\"Spanish\",\"proficiency\":\"conversational\"}],\"projects\":[{\"name\":\"Project Alpha\",\"description\":\"A web application for managing tasks\",\"technologies\":[\"Java\",\"Spring Boot\",\"React\"],\"role\":\"Lead Developer\",\"startDate\":\"2018-01-01\",\"endDate\":\"2019-01-01\"},{\"name\":\"Project Beta\",\"description\":\"A mobile app for tracking fitness\",\"technologies\":[\"Kotlin\",\"Android\",\"Firebase\"],\"role\":\"Developer\",\"startDate\":\"2019-02-01\",\"endDate\":\"2020-01-01\"}]}";


        String sampleName = "sample1.json";
        String testData;
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(sampleName);  ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if(inputStream == null) {
                throw new RuntimeException("Cannot find sample data: " + sampleName);
            }
            byte[] buffer = new byte[1024];

            int read = 0;
            while((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            testData = new String(outputStream.toByteArray());
        }

        //testData = testData.replace(" ", "").replace("\n", "").replace("\r", "");


        //String speedTest = new String(Files.readAllBytes(new File("C:\\Work\\git\\_StockMind_Hive\\StockMindCentral\\resources\\conf\\FS.json5").toPath()));



                /*"{\n" +
                "  unquoted: and you can quote me on that," +
                " unquoted_integer: 123" +
                "}";*/

        JsonParsingOptions jsonOption = ParsingOptions.json5();
        //jsonOption.setAllowConsecutiveCommas(false);
        //jsonOption.setAllowTrailingComma(true);
        //jsonOption.setAllowUnquoted(true);

        final int testCaseInCycle = 10000;
        final int preheatCycle = 50;
        final int cycle = 20 + preheatCycle;
        boolean json5Test = false;
        long totalCount = 0;
        long totalTime = 0;
        int preheatCount = preheatCycle;

        long start = 0;
        for(int c = 0; c < cycle; ++c) {

            long time = 0;

            if(json5Test) {
                start = System.currentTimeMillis();
                for (int i = 0; i < testCaseInCycle; i++) {
                    JSON5Object json5Object1 = new JSON5Object(testData, ParsingOptions.json5());
                }
                time = System.currentTimeMillis() - start;
                System.out.println("JSON5Type: " + time);
            } else {
                start = System.currentTimeMillis();
                for (int i = 0; i < testCaseInCycle; i++) {
                    JSONObject jsonObject = new JSONObject(testData);
                }
                time = System.currentTimeMillis() - start;
                System.out.println("org.json5: " + time);

            }
            if(c >= preheatCycle) {
                totalTime += time;
                totalCount += testCaseInCycle;
            } else {
                System.out.println("Preheat: " + --preheatCount);
            }
        }

        System.out.println("Average: " + (totalTime / (double)totalCount) + "ms");
        System.out.println("Total: " + totalTime + "ms");
    }




}