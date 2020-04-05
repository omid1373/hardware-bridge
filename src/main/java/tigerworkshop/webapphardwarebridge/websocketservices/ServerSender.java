package tigerworkshop.webapphardwarebridge.websocketservices;

import com.fazecast.jSerialComm.SerialPort;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static okhttp3.internal.Internal.logger;

public class ServerSender extends Thread {
    private String authToken;
    private String barcodeBuffer;
    private String resultBuffer;
    private logger logFile;

    private SettingService settingService;
    private List<String> barcodes;
    @Override
    public void run(){
        this.logFile = new logger();
        barcodes = new ArrayList();
        Server server = Server.getInstance();
        settingService = SettingService.getInstance();
        authToken = null;
        while (authToken == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) { }
        }
        while(true){
            try {
                if(!server.getIsSigned())
                    break;
                DBwork dbWorker = new DBwork();
                List<HashMap<String,String>> list = dbWorker.selectWithStateEq("failed","processed",null);
                ServerManipulate(list);
                TimeUnit.SECONDS.sleep(30);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    void ServerManipulate(List<HashMap<String,String>> list) throws IOException{
        DBwork dbWorker= new DBwork();
        for(int i=0 ; i< list.size(); i++){
            HashMap<String,String> map = list.get(i);
            this.barcodeBuffer = map.get("barcode");
            this.resultBuffer = map.get("result");
//            System.out.println(barcodeBuffer+"...ready to send");
            if(RequestToServer(resultBuffer, map.get("deviceName"))){
                dbWorker.updateState(Long.parseLong(barcodeBuffer), "finished");
                System.out.println(barcodeBuffer+" sent to server successfully");
            }
            else{
                dbWorker.updateState(Long.parseLong(barcodeBuffer), "failed");
            }
        }
    }

    Boolean RequestToServer(String json, String deviceName) throws IOException {
        String url;
        if(deviceName.equals("sysmex")) {
            url = settingService.getSimaUrl() + "/rest/s1/simaEquipmentInterOperation/parseSysmex";
        }else {
            url = settingService.getSimaUrl() + "/rest/s1/simaEquipmentInterOperation/testResult";
        }
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api_key", authToken)
                .build();
        Response response = client.newCall(request).execute();
        if(!response.isSuccessful())
            System.out.println("\n----------JSON:\n\t"+json+"\nServerSender Response:\n\t"+response);

        logFile.log("serverSent",json+"\t\t"+response);
        Boolean responseOk =  response.isSuccessful();
        response.body().close();
        return responseOk;
    }
}
