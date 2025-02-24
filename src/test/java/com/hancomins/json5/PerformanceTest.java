package com.hancomins.json5;



import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PerformanceTest {

    public static void main(String[] args) {



        String simpleJSON = "{key: [1234]} ";
        JSON5Object json5Object1 = new JSON5Object(simpleJSON);









        //String json5 = "{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\n\"333333L\":3333e+33,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-1212312321783912389123871289371231231231238,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\",\"array\":[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\",[1,1.1,2.2,333333,true,\"c\",32000,-128,null,\"stri \\\" \\n\\rng\"],{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\",\"this\":{\"1\":1,\"1.1\":1.1,\"2.2\":2.2,\"333333L\":333333,\"boolean\":true,\"char\":\"c\",\"short\":32000,\"byte\":-128,\"null\":null,\"string\":\"stri \\\" \\n\\rng\"},\"byte[]\":\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"},\"base64,SBWP065+Pl0BSofgTP1Xg7GqUa3TkQvjl4i/bJRRVwveruL0Ql2PpP540++s0fc=\"],\"array2\":[[1,2],[3,4],[],{}],\"array3\":[\"\",[3,4],[],{}],\"array4\":[{},{},[],{\"inArray\":[]}],\"key111\":{\"1\":{}},\"key112\":[{}]}";

        String json = "";
        InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("large-file.json");
        //InputStream inputStream = PerformanceTest.class.getClassLoader().getResourceAsStream("sample1.json5");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            json = byteArrayOutputStream.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }


        long startCreate = System.currentTimeMillis();

        for (int i = 0; i < 11189; i++) {


           //JSONObject jsonObject = new JSONObject();
            //JSON5Object json5Object = new JSON5Object();

        }
        System.out.println("Time Create: " + (System.currentTimeMillis() - startCreate) + "ms");

        JSON5Object jsonObject = new JSON5Object(json);



        byte[] sss = jsonObject.toBytes();
        for(int count = 0; count < 100; ++count) {
            long start = System.currentTimeMillis();
            //

            for (int i = 0; i < 10; i++) {

                //JSONArray jsonArray = new JSONArray(json5);
                //JSON5Array json5Array = new JSON5Array(json5);

                //JSON5Object jj = new JSON5Object(sss);

                //jsonObject1 = JSONB.parse(bufferts);


                //JSON5Object json5Object = new JSON5Object(json5);
                //json5Object.toString();
                /*if(i == 0 && count == 0) {
                    System.out.println(json5Object.toString());
                }*/

                /*if(i == 0 && count == 0) {
                    byte[] bufferA = json5Object.toString().getBytes(StandardCharsets.UTF_8);
                    // bufferA 를 MegaByte 단위로 출력
                    System.out.println("bufferA.length: " + bufferA.length / 1024 / 1024 + "MB");
                }*/



            }
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start) + "ms");
        }





    }
}
