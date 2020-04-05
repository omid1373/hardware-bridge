package tigerworkshop.webapphardwarebridge.services;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.controller.SettingController;
import tigerworkshop.webapphardwarebridge.responses.Setting;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class SettingService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SettingService.class.getName());
    private static final String SETTING_FILENAME = "setting.json";
    private static final String SETTING_FALLBACK_FILENAME = "setting.default.json";
    private static SettingService instance = new SettingService();
    public SettingController ui;
    private Setting setting = null;
    private String authToken = null;
    private Boolean isSigned = false;
    private boolean[] device = new boolean[7];
    private String simaUrl = null;
    private String sourcefloder = null;
    private String equipmentIds = null;

    private SettingService() {
        ui = new SettingController();
        load();
    }

    public static SettingService getInstance() {
        return instance;
    }

    public void load() {
        try {
            loadCurrent();
        } catch (Exception e) {
            try {
                loadDefault();
            } catch (Exception ex) {
                setting = new Setting();
            }
        }
    }

    public void loadCurrent() throws IOException {
        loadFile(SETTING_FILENAME);
    }

    public void loadDefault() throws IOException {
        loadFile(SETTING_FALLBACK_FILENAME);
    }

    private void loadFile(String filename) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(filename));
        Gson gson = new Gson();
        setting = gson.fromJson(reader, Setting.class);
        reader.close();
    }

    public void setAuthToken(String token) {
        authToken = token;
    }
    public String getAuthToken() {
        return authToken;
    }
    public Setting getSetting() {
        return setting;
    }
    public void setDevice(int deviceId){
        if(deviceId > 100)
            this.device[deviceId-101]=false;
        else if(deviceId==100)  for(int i=0; i < 7 ; i++) this.device[i]=false;
        else this.device[deviceId-1]=true;
    }
    public boolean[] getDevice(){
        return device;
    }
    public void save() {
        try {
            Writer writer = new FileWriter(SETTING_FILENAME);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(setting, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void setIsSigned(Boolean b){ this.isSigned = b; }
    public Boolean getIsSigned(){return isSigned;}

    public String getSimaUrl() {
        return simaUrl;
    }
    public String equipmentIds(){
//        List<String> eqIds = new ArrayList<>();
//        for (String id : equipmentIds.split(","))
//            eqIds.add(id);
//        return eqIds;
        return this.equipmentIds;
    }

    public HashMap<Integer,String> getDevicesName() throws Exception {
        HashMap<Integer, String> devices = new HashMap<>();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(new FileReader(SETTING_FILENAME));
        JsonArray devArray = (JsonArray) jsonObject.get("devices");
        for (JsonElement dev : devArray) {
            JsonObject eachDevice = dev.getAsJsonObject();
            String name = eachDevice.get("name").getAsString();
            Integer ThreadId = eachDevice.get("deviceId").getAsInt();
            devices.put(ThreadId,name);
        }
        return devices;
    }
    public HashMap<String,String> AllDevicesByName(String Name) throws Exception{
        List<HashMap<String,String>> list = this.AllDevices();
        HashMap<String,String> map;
        for (int i=0 ; i < list.size() ; i++){
            map = list.get(i);
            if(map.get("name").equals(Name))
                return map;
        }
        return null;
    }
    public List<HashMap<String,String>> AllDevices() throws Exception{
        List<HashMap<String,String>> list =new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(new FileReader(SETTING_FILENAME));
        this.simaUrl = jsonObject.get("simaServerUrl").getAsString();
        this.equipmentIds = jsonObject.get("equipmentIds").getAsString();
        JsonArray devArray =(JsonArray) jsonObject.get("devices");
        for (JsonElement dev : devArray) {
            HashMap<String,String> map = new HashMap<>();
            JsonObject eachDevice = dev.getAsJsonObject();
            String   name = eachDevice.get("name").getAsString();
            String   port = eachDevice.get("port").getAsString();
            String   path = eachDevice.get("path").getAsString();
            String   source = eachDevice.get("sourceFolder").getAsString();
            String   Id = eachDevice.get("deviceId").getAsString();
            String   eqId =  eachDevice.get("equipmentId").getAsString();
            map.put("name",name);
            map.put("port",port);
            map.put("path",path);
            map.put("source",source);
            map.put("id",Id);
            map.put("equipmentId",eqId);
            list.add(map);
        }
        return list;
    }
    public void setDevices(ArrayList<HashMap<String, String>> arr ){
//        setting.setDevices(arr);
    }
}