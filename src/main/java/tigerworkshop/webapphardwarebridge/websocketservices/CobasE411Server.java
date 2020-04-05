package tigerworkshop.webapphardwarebridge.websocketservices;

import com.fazecast.jSerialComm.SerialPort;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;
import tigerworkshop.webapphardwarebridge.utils.ThreadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;

public class CobasE411Server extends Thread{
    private String authToken;
    private Thread readThread;
    private byte[] writeBuffer = {};
    private String messageStr = "";
    private Thread writeThread;
    private final SerialPort serialPort;
    private String Serialport;
    private String path;
    public CobasE411Parser parse = new CobasE411Parser();
    private Integer ThreadNumber;
    private String equipmentId;
    private List<String> message;

    public CobasE411Server(String port, String urlpath, Integer threadNumber , String eqId){
        this.Serialport = port;
        this.path = urlpath;
        this.ThreadNumber = threadNumber;
        this.serialPort = SerialPort.getCommPort(port);
        this.equipmentId = eqId;
    }

    @Override
    public void run(){
        message = new ArrayList<>();
//        logger.info("..........Cobas .........Cobas.................");
        SettingService settingService = SettingService.getInstance();
        Server server = Server.getInstance();
        authToken = null;
        while (authToken == null){
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) { }
        }
        readThread = new Thread(new Runnable(){
            @Override
            public void run(){
                while (true){
                    byte[] receivedData= new byte[1];
                    if(!server.getIsSigned()){
//                        logger.info("Serial unplugged! signed = "+server.getIsSigned());
                        serialPort.closePort();
                        break;
                    }
                    try {
                        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("Cobas.txt"), true));
                        if (serialPort.isOpen()){
                            settingService.setDevice(ThreadNumber);
                            if (serialPort.bytesAvailable() == 0){
                                TimeUnit.MICROSECONDS.sleep(500);
                                continue;
                            } else if (serialPort.bytesAvailable() == -1){
                                serialPort.closePort();
//                                System.out.println("Serial unplugged!");
                                continue;
                            } else {
                                serialPort.readBytes(receivedData,1);
                                char[] ch = new String(receivedData, StandardCharsets.UTF_8).toCharArray();
                                if((ch[0]!=(char)10) && (ch[0]!=(char)13) && (ch[0]!=(char)3) && (ch[0]!=(char)2)&& (ch[0]!=(char)4) && (ch[0]!=(char)6) && (ch[0]!=(char)5) && (ch[0]!=(char)21))
                                    messageStr += Character.toString(ch[0]);
                                communicate( ch[0], pw); // -----------Communicate with Device!!!!
                            }
                            pw.close();
                        }
                        else {
//                            System.out.println("Cobas Trying to connect the serial @ " + serialPort.getSystemPortName());
                            serialPort.openPort();
                        }
                    }
                    catch (Exception e){ e.printStackTrace(); }
                }
            }});
        writeThread = new Thread(new Runnable(){
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if (serialPort.isOpen()){
                        try {
                            if (writeBuffer.length > 0){
                                serialPort.writeBytes(writeBuffer, writeBuffer.length);
                                writeBuffer = new byte[]{};
                            }
                            ThreadUtil.silentSleep(1000);
                        } catch (Exception e){
//                            logger.info("Error:" + e.getMessage());
                            ThreadUtil.silentSleep(100);
                        }
                    }
                }
            }
        });

        readThread.start();
        writeThread.start();
    }

    private void communicate(char ch, PrintWriter pw) throws Exception {
        switch (ch){
            case (char) 10: // Carriage Return
//                System.out.println("e411: "+messageStr+"**Omid");
                pw.println(messageStr);
                if(messageStr.length()>0 &&(messageStr.charAt(1)=='O' || messageStr.charAt(1)=='R')){
//                    System.out.println("oooRRR");
                    message.add(messageStr);
                }
                writeBuffer = parse.sendACK().getBytes();
                messageStr = "";
                break;
            case (char) 4: //  EOT
                pw.println("EOT");
//                System.out.println("EOT :D");
                parse.resultParse(message , equipmentId);
                message.clear();
                break;
            case (char) 5: //  ENQ
                pw.println(messageStr + "<->ENQ");
//                System.out.println(messageStr + "<->ENQ");
                messageStr = "";
                writeBuffer = parse.sendACK().getBytes();
                TimeUnit.MICROSECONDS.sleep(100);
                break;
            case (char) 6: //  ACK
                pw.println(messageStr + "<-Ack");
//                System.out.println(messageStr + "<-Ack");
                messageStr = "";
                break;
        }
    }
}
