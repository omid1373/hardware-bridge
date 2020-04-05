package tigerworkshop.webapphardwarebridge.websocketservices;

import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServerInterface;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import javax.swing.*;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;

public class modularServer2 extends Thread {
    private String authToken = null;
    private WebSocketServerInterface server = null;
    public ServerSocket ss;
    private String portNumber;
    private String path;
    private int ThreadNumber;
    private String equipmentId;

    public modularServer2(String port, String urlpath, Integer threadNumber, String eqId) {
        this.portNumber = port ;
        this.path = urlpath ;
        this.ThreadNumber = threadNumber ;
        this.equipmentId = eqId ;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(Integer.parseInt(portNumber));
            logger.info("Modular started ..............Modular started on "+portNumber+"................Modular started ");
        }
        catch(IOException e){ e.printStackTrace(); }

        SettingService settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        authToken = null;
        while(authToken == null){
            try{
                TimeUnit.SECONDS.sleep(1);
//                authToken = settingService.getAuthToken();
                authToken=server.getAuthToken();
            }
            catch(Exception e){ }
        }
        while (true) {
            Socket s;
            try {
                if(!server.getIsSigned()){
                    ss.close();
                    break;
                }
//                ss.setSoTimeout(2000);
                // socket object to receive incoming client requests
                s = ss.accept();
//                if(!server.getIsSigned()){
//                    s.close(); ss.close(); break;}
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                // create a new thread object
                Thread t = new ClientHandlerModular(s, dis, dos, authToken, server, settingService, path, ThreadNumber, equipmentId);
                t.start();

            } catch (Exception e) {
                if(!(e instanceof SocketTimeoutException)  && !(e instanceof RuntimeException) && !(e instanceof  NullPointerException)) {
                    e.printStackTrace();
                    if(e instanceof BindException)
                        JOptionPane.showMessageDialog( null, "Port in use !!!","Port Problem", JOptionPane.PLAIN_MESSAGE );
                    break;
                }
            }
        }
    }
}
class ClientHandlerModular extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    private String authToken=null;
    private SettingService sett;
    private String URL;
    private String Urlpath = null;
    private int threadnumber;
    private Server server2;
    private String equipmentId;
    logger logFile;
    InputStream deviceInputStream;
    OutputStream deviceOutputStream;
//    private logger logFile = new logger();

    public ClientHandlerModular(Socket s, DataInputStream dis, DataOutputStream dos, String token, Server serv,
                                SettingService settingService, String path, int threadnum , String eqId) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.authToken = token;
        this.sett = settingService;
        this.URL = settingService.getSimaUrl();
        this.Urlpath = path;
        this.threadnumber = threadnum;
        this.server2 = serv;
        this.equipmentId = eqId;
         this.logFile = new logger();
        try{
            deviceInputStream = s.getInputStream();
            deviceOutputStream = s.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        modularParser parser = new modularParser();
        String receivingBuffer = "";
        byte[] data = new byte[1024];
        char eof = (char) 28;
        while (true) {
            try {
                if (deviceInputStream.read(data) == -1) {
                    throw new EOFException();
                }
                receivingBuffer = receivingBuffer + new String(data);
                if (!receivingBuffer.isEmpty()) {
                    while (!receivingBuffer.contains("" + eof)) {
                        //read second set
                        byte[] sdata = new byte[1024];
                        if (deviceInputStream.read(sdata) == -1) {
                            break;
                        }
                        String newReceived = new String(sdata);
                        receivingBuffer = receivingBuffer + newReceived;
                    }
                    logFile.log("pureLog","received:\n"+receivingBuffer);
//                    System.out.println(receivingBuffer);
                    receivingBuffer = receivingBuffer.replaceAll("\\s", "");
                    receivingBuffer = receivingBuffer.replaceAll("\u0000", ""); // removes NULL chars
                    receivingBuffer = receivingBuffer.replaceAll("\\u0000", ""); // removes backslash+u0000
                }
                // گرفتن تستهای مربوط به دستگاه
                String newMessage = receivingBuffer.substring(0, receivingBuffer.indexOf(eof));
                receivingBuffer = receivingBuffer.replace(newMessage, "");
                if (receivingBuffer.startsWith(eof + "")){
                    receivingBuffer = receivingBuffer.replaceFirst(eof + "", "");
                }
//                System.out.println("message:\n"+newMessage);
                List<String> msgList = parser.messageParser(newMessage , equipmentId);
                for(int k = 0 ; k < msgList.size(); k++) {
                    if( k > 0) TimeUnit.MILLISECONDS.sleep(250);
                    sendToDevice(msgList.get(k));
                }
            } catch (Exception p) {
            }
        }
    }
    private void sendToDevice(String preString){
        try {
            deviceOutputStream.write(preString.getBytes());
            deviceOutputStream.flush();
//            System.out.println("----------Written: "+preString);
//            System.out.println("write:\n"+preString+"\n");
            logFile.log("pureLog","Written:\n "+preString);

            // --------------------make it neat!!!
            preString = preString.replaceAll("\\s", "");
            preString = preString.replaceAll("\u0000", ""); // removes NULL chars
            logFile.log("modularlog","(Written:) "+preString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
