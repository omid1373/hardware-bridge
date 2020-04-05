package tigerworkshop.webapphardwarebridge.websocketservices;

import com.fazecast.jSerialComm.SerialPort;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class sysmex extends Thread {

    private String authToken = null;
    private String Serialport;
    private final SerialPort serialPort;
    private String path;
    private int ThreadNumber;
    private byte[] writeBuffer = {};
    private String messageStr = null;
    private Thread readThread;
    private Thread writeThread;
    public ImmuliteParser parse = new ImmuliteParser();
    private List<String> message;
    private String equipmentId;
    private logger logFile;

    public sysmex(String port, String urlpath, Integer threadNumber, String eqId) {
        this.Serialport = port;
        this.path = urlpath;
        this.ThreadNumber = threadNumber;
        this.serialPort = SerialPort.getCommPort(port);
        this.equipmentId = eqId;
        this.logFile = new logger();
    }
    @Override
    public void run(){
        System.out.println("----------------------Sysmex is running--------------");
        DBwork dBwork = new DBwork();
        SettingService settingService = SettingService.getInstance();
        Server server = Server.getInstance();
        authToken = null;
        String received = "";
        while (authToken == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) { }
        }
        Boolean finished = false;
        while (true) {
            byte[] receivedData = new byte[1];
            if (!server.getIsSigned()) {
                serialPort.closePort();
                break;
            }
            try{
                if (serialPort.isOpen()){
                    settingService.setDevice(ThreadNumber);
                    if (serialPort.bytesAvailable() == 0){
                        if(received.length()>0 && finished){
                            received = received.replace('\3', '#').
                                    replace('\2', '#').replace("#", "");
                            received = "sysmex-1#"+ received;
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("response",received);
                            dBwork.insertSysmex(responseJson.toString());
                            finished = false;
                            System.out.println("received:\n"+received);
                            logFile.log("sysmexLog",received);
                            received = "";
                        }
                        finished = true;
//                        received = "";
                        TimeUnit.MILLISECONDS.sleep(1000);
                        continue;
                    } else if (serialPort.bytesAvailable() == -1){
                        serialPort.closePort();
                        continue;
                    } else {
                        serialPort.readBytes(receivedData,1);
                       received += new String(receivedData, StandardCharsets.UTF_8);
                       System.out.print(new String(receivedData, StandardCharsets.UTF_8));
                    }
                } else {
                serialPort.openPort();
                 }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
