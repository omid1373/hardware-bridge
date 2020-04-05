package tigerworkshop.webapphardwarebridge.websocketservices;

//import gui.GUITimerActionListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import com.sun.org.apache.xpath.internal.operations.Bool;
import okhttp3.*;
import okio.Timeout;
import tigerworkshop.webapphardwarebridge.DBwork;

import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServerInterface;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServiceInterface;
import tigerworkshop.webapphardwarebridge.services.SettingService;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;


import static okhttp3.internal.Internal.logger;

public class getBarcodesFromServer extends Thread {
    private String authToken = null;
    private String port;
    private String Url;
    private String eqIds;
    private int ThreadNumber;
    private SettingService settingService;
    private Integer requestSleep = 30;
    public getBarcodesFromServer(String port, String urlpath, Integer threadNumber, String equipmentIds){
        this.port = port;
        this.Url = urlpath;
        this.ThreadNumber = threadNumber;
        this.eqIds = equipmentIds;
    }
    @Override
    public void run() {
//        logger.info("....................Barcode .........runnable.................");
        settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        authToken = null;
        while (authToken == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) { }
        }
        while (true) {
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(new File("getBarcode.txt"), true));
                if(!server.getIsSigned()){
                    break;
                }
                settingService.setDevice(ThreadNumber);
                receiveBarcodes(20);
                TimeUnit.SECONDS.sleep(requestSleep);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void receiveBarcodes(Integer listSize) throws IOException {
        JSONObject jsonObject = firstRequest(listSize);
        if (jsonObject != null){
            //parse the response
            String barcodeList = jsonObject.get("barcodeList").toString();
            JSONArray barcodeListArray = new JSONArray(barcodeList);
            int count = 0;
            if(barcodeListArray.length() > 0){
                this.requestSleep = 0;
            }
            else {
                this.requestSleep = 30;
            }
            addToDB(barcodeListArray);
        }
    }
    private JSONObject firstRequest(Integer listSize) throws IOException{
        JSONArray jsonArr = new JSONArray();
        JSONObject Lsize = new JSONObject();
        for(String id : eqIds.split(","))
            jsonArr.put(Integer.parseInt(id));
        Lsize.put("listSize", listSize);
        Lsize.put("equipmentId",jsonArr);
        String Lmedia = Lsize.toString();

        MediaType LmediaType = MediaType.parse("application/json");
        RequestBody Lbody = RequestBody.create(LmediaType, Lmedia );
        String origin = settingService.getSimaUrl() + Url;
//        System.out.println(origin);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(origin)
                .post(Lbody)
                .addHeader("api_key", authToken)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()){
            JSONArray barArr = new JSONArray();
            //parse the response
            String MyResult = response.body().string();
            JSONObject jsonObject = new JSONObject(MyResult);
            response.body().close();
            return jsonObject;
        }
        response.body().close();
        return null;
    }
    private void secondRequest( JSONArray barcodeArr) throws IOException{
            JSONObject jobj = new JSONObject();
            jobj.put("barcodeList",barcodeArr);
            String media=jobj.toString();
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody Rbody = RequestBody.create(mediaType,media );
            String Origin2 = settingService.getSimaUrl()+ "/rest/s1/simaEquipmentInterOperation/updateSampleState";
            Request request = new Request.Builder()
                    .url(Origin2)
                    .put(Rbody)
                    .addHeader("api_key", authToken)
                    .build();
            Response response = client.newCall(request).execute();
            response.body().close();
    }
    private void addToDB(JSONArray barcodeListArray) throws IOException{
        long millis = System.currentTimeMillis();
        java.sql.Date todayDate = new java.sql.Date(millis);
        JSONArray barArr = new JSONArray();
        int count = 0;
        for (int i = 0; i < barcodeListArray.length(); i++){
            Long barcode = barcodeListArray.getJSONObject(i).getLong("barcode");
            String equipmentId = barcodeListArray.getJSONObject(i).getString("equipmentId");
            String testIds = barcodeListArray.getJSONObject(i).toString();
            Integer reception =(int) (barcode % 100000) ;  //  5-digit reception
            JSONObject jOBJ = new JSONObject(testIds);
            JSONArray testArray = jOBJ.getJSONArray("testId");
            String testsStr = null;
            if(testArray.length() > 0){
                testsStr = testArray.getString(0);
                for (int j = 1; j < testArray.length(); j++){
                    testsStr = testsStr + "," + testArray.get(j).toString();
                }
            }
            // insert to DB
            DBwork app = new DBwork();
            String insertState = app.insert(barcode, equipmentId, testsStr, "Received", todayDate.toString(),reception);
            if (insertState == "ok") {
                barArr.put(barcode);
                count++;
            }
        }
        if(barcodeListArray.length() == count) {
            secondRequest(barArr);
        }
    }
}