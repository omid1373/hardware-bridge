package tigerworkshop.webapphardwarebridge.controller;

import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.util.HashMap;

public class DeviceTable {
    private SettingService settingService = SettingService.getInstance();
    private String name = null;
    private String port=null;
    private String path=null;
    private String source=null;
    private String equipmentId=null;

    public DeviceTable(String Name){
        this.name=Name;
    }
    public void SetFirst() throws Exception{
        HashMap<String,String> devices = settingService.AllDevicesByName(name);
        this.port = devices.get("port");
        this.path = devices.get("path");
        this.source = devices.get("source");
        this.equipmentId = devices.get("equipmentId");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getPort() {
        return port;
    }

    public String getSource() {
        return source;
    }

    public String getEquipmentId() {
        return equipmentId;
    }
}
