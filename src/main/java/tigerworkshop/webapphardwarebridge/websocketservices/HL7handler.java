package tigerworkshop.webapphardwarebridge.websocketservices;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HL7handler {

    public void modularParser(Long barcode) {
        String finalBodyString = "";
        DBwork app = new DBwork();
        String barcodeResult = app.selectResult(barcode);
        String[] hlOBX = barcodeResult.split("OBX");
        try{
            for (int i=1; i < hlOBX.length; i++){
                String[] hlResult = hlOBX[i].split("\\|");
                if (!hlResult[3].equals("")){
                    JSONObject finalJsonObj = new JSONObject();
                    finalJsonObj.put("barcode", barcode);
                    JSONObject testJsonObj = new JSONObject();
                    testJsonObj.put("testId", hlResult[3]);
                    if (hlResult.length >= 17){
                        testJsonObj.put("reTest", Integer.parseInt(hlResult[17]));
                    }
                    else{
                        testJsonObj.put("reTest", 0);
                    }
                    testJsonObj.put("state", "urtProcessed");
                    testJsonObj.put("type", "T");
                    testJsonObj.put("result", hlResult[5]);
                    JSONArray TestList = new JSONArray();
                    TestList.put(testJsonObj);
                    finalJsonObj.put("TestList", TestList);
                    JSONObject finalFinal = new JSONObject();
                    JSONArray mapResult = new JSONArray();
                    mapResult.put(finalJsonObj);
                    finalFinal.put("mapResult", mapResult);
                    finalBodyString = finalFinal.toString();
                }
                // send parsed response to sima server
                System.out.print(finalBodyString);
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, finalBodyString);

                Properties p = new Properties();
                InputStream df = new FileInputStream("dataConfig.properties");
                p.load(df);
                String origin = p.getProperty("simaServerUrl");
                String authToken = p.getProperty("authorizationToken");

                Request request = new Request.Builder()
                        .url(origin + "/rest/s1/simaEquipmentInterOperation/testResult")
                        .put(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("authorization", authToken)
                        .build();
                Response response = client.newCall(request).execute();
                if(response.isSuccessful()){
                    System.out.print("Done");
                    app.updateState(barcode,"finished");
                }
                else {
                    System.out.print(response);
                    app.updateState(barcode,"failed");
                }
//                response.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void hematologyParser(Long barcode) {
        String finalBodyString = "";
        DBwork app = new DBwork();
        String barcodeResult = app.selectResult(barcode);
        //parse the response
        String[] hlOBX = barcodeResult.split("OBX");
        try {
            for (int i = 1; i < hlOBX.length; i++) {
                String[] hlResult = hlOBX[i].split("\\|");
                if (hlResult[2].equals("NM") || hlResult[2].equals("ED")) {
                    JSONObject finalJsonObj = new JSONObject();
                    finalJsonObj.put("barcode", barcode);
                    JSONObject testJsonObj = new JSONObject();
                    String[] testRes = hlResult[3].split("\\^");
                    testJsonObj.put("testId", testRes[0]);
                    if (hlResult.length >= 17){
                        testJsonObj.put("reTest", Integer.parseInt(hlResult[17]));
                    }
                    else{
                        testJsonObj.put("reTest", 0);
                    }
                    testJsonObj.put("state", "urtProcessed");
                    String res = "";
                    if (hlResult[2].equals("NM")) {
                        testJsonObj.put("type", "T");
                        res = hlResult[5];
                    } else if (hlResult[2].equals("ED")){
                        testJsonObj.put("type", "P");
                        String[] pRes = hlResult[5].split("\\^Base64\\^");
                        res = pRes[1];
                    }
                    testJsonObj.put("result", res);
                    JSONArray TestList = new JSONArray();
                    TestList.put(testJsonObj);
                    finalJsonObj.put("TestList", TestList);
                    JSONObject finalFinal = new JSONObject();
                    JSONArray mapResult = new JSONArray();
                    mapResult.put(finalJsonObj);
                    finalFinal.put("mapResult", mapResult);
                    finalBodyString = finalFinal.toString();

                    System.out.print(finalBodyString);
                    OkHttpClient client = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, finalBodyString);
                    Properties p = new Properties();
                    InputStream isis = new FileInputStream("dataConfig.properties");
                    p.load(isis);
                    String origin = p.getProperty("simaServerUrl");
                    String authToken = p.getProperty("authorizationToken");
                    Request request = new Request.Builder()
                            .url(origin + "/rest/s1/simaEquipmentInterOperation/testResult")
                            .put(body)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("cache-control", "no-cache")
                            .addHeader("authorization", authToken)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        System.out.println("Done");
                        app.updateState(barcode,"finished");
                    } else {
                        System.out.print(response);
                        app.updateState(barcode,"failed");
                    }
//                    response.close();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
