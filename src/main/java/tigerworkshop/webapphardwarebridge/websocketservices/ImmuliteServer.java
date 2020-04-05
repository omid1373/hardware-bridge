package tigerworkshop.webapphardwarebridge.websocketservices;

import com.fazecast.jSerialComm.SerialPort;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServerInterface;
import tigerworkshop.webapphardwarebridge.services.SettingService;
import tigerworkshop.webapphardwarebridge.utils.ThreadUtil;

import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;

public class ImmuliteServer extends Thread {
    private String authToken = null;
    private WebSocketServerInterface server = null;
//    private Properties p;
    public ServerSocket ss;

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

    public ImmuliteServer(String port, String urlpath, Integer threadNumber, String eqId) {
        this.Serialport = port;
        this.path = urlpath;
        this.ThreadNumber = threadNumber;
        this.serialPort = SerialPort.getCommPort(port);
        this.equipmentId = eqId;
        this.logFile = new logger();
    }

    @Override
    public void run() {
         message = new ArrayList<>();
//        logger.info("..........Immulite .........Immulite.................");
        SettingService settingService = SettingService.getInstance();
        Server server = Server.getInstance();
        authToken = null;
        while (authToken == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) {
          }
        }
        readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        byte[] receivedData= new byte[1];
                        if(!server.getIsSigned()){
                            serialPort.closePort();
                            break;
                        }
                    try {
                        if (serialPort.isOpen()){
                            settingService.setDevice(ThreadNumber);
                            if (serialPort.bytesAvailable() == 0){
                                TimeUnit.MICROSECONDS.sleep(500);
                                continue;
                            } else if (serialPort.bytesAvailable() == -1){
                                serialPort.closePort();
                                continue;
                            } else {
                                serialPort.readBytes(receivedData,1);
                                char[] ch = new String(receivedData, StandardCharsets.UTF_8).toCharArray();
                           if((ch[0]!=(char)10) && (ch[0]!=(char)13) && (ch[0]!=(char)3) && (ch[0]!=(char)2)&& (ch[0]!=(char)4) && (ch[0]!=(char)6) && (ch[0]!=(char)5) && (ch[0]!=(char)21)) {
                              if(messageStr == null)
                               messageStr = Character.toString(ch[0]);
                              else
                               messageStr += Character.toString(ch[0]);
                           }
                                communicate( ch[0] ); // -----------Communicate with Device!!!!
                            }
                        } else {
                            serialPort.openPort();
                        }
                    } catch (Exception e) {
                            e.printStackTrace();
                        }
                  }
                }
            });
        writeThread = new Thread(new Runnable() {
            @Override
            public void run(){
                while (!Thread.interrupted()){
                    if (serialPort.isOpen()){
                        try {
                            //omid :
                            if (writeBuffer.length > 0){
                                serialPort.writeBytes(writeBuffer, writeBuffer.length);
                                writeBuffer = new byte[]{};
                            }
                            ThreadUtil.silentSleep(500);
                        } catch (Exception e){
//                            System.out.println("Error: " + e.getMessage());
                            ThreadUtil.silentSleep(10);
                        }
                    }
                }
            }
        });
        readThread.start();
        writeThread.start();
      }
      void parseMessage(List<String> msg) throws Exception{
        List<String> writeMessage;
       if(msg.size() < 1) return;
       if (msg.get(0).charAt(0)=='Q'){ //Query from Device
           writeMessage = parse.query( msg );
             for(int i=0 ; i< writeMessage.size(); i++){
                 writeBuffer = (writeMessage.get(i)).getBytes();
//                 System.out.println(writeMessage.get(i));
                 logFile.log("ImmuliteLog_give","We give to device:\n"+writeMessage.get(i));
                 TimeUnit.MILLISECONDS.sleep(1000);
             }
        }
        else{ // Answer from Device
              parse.resultParse(msg,equipmentId);
              for(int k = 0 ; k< msg.size(); k++)
                 logFile.log("ImmuliteLog_result","result msg:---"+msg.get(k));
          }
          msg.clear();
      }
      void communicate(char ch) throws Exception{
          switch (ch){
              case (char) 10: // Line Feed
                   if(messageStr.length()>0 && messageStr.charAt(1)=='Q'){
                      message.add(messageStr.substring(1));
                       logFile.log("ImmuliteLog_get","received:\n"+messageStr);
//                       System.out.println("From device-----"+messageStr);
                  }
                  else if(messageStr.length()>0 &&(messageStr.charAt(1)=='O' || messageStr.charAt(1)=='R'|| messageStr.charAt(1)=='P')){
                      message.add(messageStr.substring(1));
                       System.out.println(messageStr);
                  }
                  messageStr = "";
                  writeBuffer = parse.sendACK().getBytes();
                  break;
              case (char) 4: //  EOT
                  parseMessage(message);
                  message.clear();
                  messageStr = "";
                  break;
              case (char) 5: //  ENQ
                  messageStr = "";
                  writeBuffer = parse.sendACK().getBytes();
                  TimeUnit.MICROSECONDS.sleep(100);
                  break;
              case (char) 6: //  ACK
                  messageStr = "";
                  break;
          }
      }
 }
