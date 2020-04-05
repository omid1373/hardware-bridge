package tigerworkshop.webapphardwarebridge.websocketservices;

import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;

public class hematologyServer2 extends Thread {
    private String authToken = null;
    //    private WebSocketServerInterface server = null;
    private Properties p;
    public ServerSocket ss;
    private String portNumber;
    private String path;
    private int ThreadNumber;
    private String equipmentId;

    public hematologyServer2(String port,String url, Integer threadNumber, String eqId){
        this.portNumber=port;
        this.path=url;
        this.ThreadNumber=threadNumber;
        this.equipmentId = eqId;
    }

    @Override
    public void run() {
        System.out.println(".................Hemoato .........runnable.................");
        SettingService settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        try {
            ss = new ServerSocket(Integer.parseInt(portNumber));
        } catch (IOException e) {
            e.printStackTrace();
        }
        authToken = null;
        while (authToken == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
                authToken = server.getAuthToken();
            } catch (Exception e) {
               }
        }
        while (true) {
            Socket s;
            try {
                if(!server.getIsSigned()){
                    ss.close();
                    break;
                }
                ss.setSoTimeout(2000);

                // socket object to receive incoming client requests
                s = ss.accept();
                if(!server.getIsSigned()){
                    s.close(); ss.close(); break;}
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                // create a new thread object
                Thread t2 = new hematologyClientHandler2(s, ss, dis, dos, authToken, server, settingService, path, ThreadNumber, equipmentId);
                t2.start();
                // Invoking the start() method

            } catch (Exception e) {
//                s.close();
                if(!(e instanceof SocketTimeoutException) && !(e instanceof RuntimeException) && !(e instanceof  NullPointerException)){
                    e.printStackTrace();
                    if(e instanceof BindException)
                        logger.info("Shit!!  Bind Exception Hemato");
                }
            }
        }
    }
}


class hematologyClientHandler2 extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    private ServerSocket socket;
    private String authToken = null;
    private String URL = null;
    private Server server2;
    private SettingService sett;
    private String Urlpath = null;
    private int ThreadNum;
    private String equipmentId;
    static String startOfMessage = Character.toString((char) 11);
    static String endOfMessage = Character.toString((char) 28) + Character.toString((char) 13);
    static String carriageReturn = Character.toString((char) 13);

    // Constructor
    public hematologyClientHandler2(Socket s, ServerSocket soc, DataInputStream dis, DataOutputStream dos , String token, Server serv,
                                   SettingService settingservice, String url, int threadNum , String eqId) {
        this.s = s;
        this.socket=soc;
        this.dis = dis;
        this.dos = dos;
        this.authToken=token;
        this.sett=settingservice;
        this.server2=serv;
        this.URL=settingservice.getSimaUrl();
        this.Urlpath=url;
        this.Urlpath=url;
        this.ThreadNum=threadNum;
        this.equipmentId = eqId;
    }
    @Override
    public void run() {
        hematologyParser parser = new hematologyParser();
        String receivingBuffer = "";
        sett.setDevice(ThreadNum);
        String received = "";
        String barcode = "";
        try {
            InputStream is = s.getInputStream();
            OutputStream os = s.getOutputStream();

            //Receiving Data
            char eof = (char) 28;
            byte[] data = new byte[1024];
            if (is.read(data) == -1) {
                throw new EOFException();
            }
            received = new String(data);
            received = received.replaceAll("\\s", "");
            if (received != "") {
                while (received.contains("" + eof) == false) {
                    //read second set
                    byte[] sdata = new byte[1024];
                    if (is.read(sdata) == -1) {
                        throw new EOFException();
                    }
                    String sReceived = new String(sdata);
                    received = received + sReceived;
                }
                received = received.replaceAll("\\s", "");
                received = received.replace("\u0000", ""); // removes NUL chars
                received = received.replace("\\u0000", ""); // removes backslash+u0000
            }
            String newMessage = receivingBuffer.substring(0, receivingBuffer.indexOf(eof));
            receivingBuffer = receivingBuffer.replace(newMessage, "");
            if (receivingBuffer.startsWith(eof + "")) {
                receivingBuffer = receivingBuffer.replaceFirst(eof + "", "");
            }
            List<String> msgList = parser.messageParser(newMessage,equipmentId);
            for(int k = 0 ; k < msgList.size(); k++) {
                if( k > 0) TimeUnit.MILLISECONDS.sleep(300);
                sendToDevice(msgList.get(k));
            }
            if (received.equals("Exit") || !server2.getIsSigned()) {
                this.s.close();
                this.socket.close();
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    private void sendAck(){
////        System.out.println("Sending Ack to hematology");
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        LocalDateTime now = LocalDateTime.now();
//        String messageAck = this.startOfMessage + "MSH|^~\\&|BC-6800|Mindray|||"+dtf.format(now)
//                +"||ACK^R01|5|P|2.3.1||||||UNICODE\r" +"MSA|AA|1\r" + this.endOfMessage;
//        //"Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + this.endOfMessage;
//        sendToDevice(messageAck);
//        this.log(messageAck);
//    }
    private void sendToDevice(String message) {
        try {
            s.getOutputStream().write(message.getBytes());
            s.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void log(String message){
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File("hematologyResult.txt"), true));
            pw.println(message);
            pw.close();
        }
        catch(Exception e){
        }
    }

}
