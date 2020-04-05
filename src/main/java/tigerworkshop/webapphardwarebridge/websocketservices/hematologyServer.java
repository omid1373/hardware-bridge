package tigerworkshop.webapphardwarebridge.websocketservices;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServerInterface;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServiceInterface;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static okhttp3.internal.Internal.logger;
import tigerworkshop.webapphardwarebridge.DBwork;

import javax.swing.*;

public class hematologyServer extends Thread {
    private String authToken = null;
//    private WebSocketServerInterface server = null;
    private Properties p;
    public ServerSocket ss;
    private String portNumber;
    private String path;
    private int ThreadNumber;
    private String equipmentId;

    public hematologyServer(String port,String url, Integer threadNumber, String eqId){
        this.portNumber=port;
        this.path=url;
        this.ThreadNumber=threadNumber;
        this.equipmentId = eqId;
    }

    @Override
    public void run() {
        System.out.println("..........Hemoato .........runnable.................");
        SettingService settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        try {
            ss = new ServerSocket(Integer.parseInt(portNumber));
//            logger.info("Hemato started ..............Hemato started ..............port: "+portNumber);
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
                Thread t2 = new hematologyClientHandler(s, ss, dis, dos, authToken, server, settingService, path, ThreadNumber, equipmentId);
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


class hematologyClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    private ServerSocket socket;
    private String authToken=null;
    private String URL=null;
    private Server server2;
    private SettingService sett;
    private String Urlpath=null;
    private int ThreadNum;
    private String equipmentId;
    static String startOfMessage = Character.toString((char) 11);
    static String endOfMessage = Character.toString((char) 28) + Character.toString((char) 13);
    static String carriageReturn = Character.toString((char) 13);

    // Constructor
    public hematologyClientHandler(Socket s, ServerSocket soc, DataInputStream dis, DataOutputStream dos , String token, Server serv,
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
            sett.setDevice(ThreadNum);
            String received = "";
            String barcode = "";
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(new File("hematologyResult.txt"), true));
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();
//            GUITimerActionListener.getInstance().appendtextToWindow(2, "Thread is running");

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


                if (received.indexOf("ORC") > 0) {
                    String[] tempD = received.split("ORC");
                    barcode = tempD[1];
                    tempD = barcode.split("\\|");
                    barcode = tempD[3];
//                GUITimerActionListener.getInstance().appendtextToWindow(2, "first barcode IS: " + barcode);
                }

                if (received.indexOf("ORM^O01") >= 0 && received.indexOf("ORU^R01") < 0) {
                    String dataToSendBc6800 = Character.toString((char) 11) +
                            "MSH|^~\\\\&|LIS||||20081120174836||ORR^O02|1|P|2.3.1||||||UNICODE\r" +
                            "MSA|AA|000\rPID|1||1^^^^MR||test^test||0000/00/00|Male\r" +
                            "PV1|1|E|^^|||||||||||||||||NewCharge\r" +
                            "ORC|AF|" + barcode + "|||\r" +
                            "OBR|1|" + barcode +
                            "||||20060506||||||||20060504||||||||20080821||HM||||Validated by||||Operated by\r" +
                            "OBX|1|IS|08001^Take Mode^99MRC||A||||||F\r" +
                            "OBX|2|IS|08002^Blood Mode^99MRC||W||||||F\r" +
                            "OBX|3|IS|08003^Test Mode^99MRC||CBC+DIFF+RET+NRBC||||||F\r" +
                            "OBX|4|IS|01002^Ref Group^99MRC||\r" +
                            "OBX|5|NM|30525-0^Age^LN||1|hr|||||F\r" +
                            "OBX|6|ST|01001^Remark^99MRC||||||||F\r" +
                            Character.toString((char) 28) + Character.toString((char) 13);
                    byte[] madeQuery = dataToSendBc6800.getBytes();
                    os.write(madeQuery);

                    // دادن نتایج تستها به سرور
                }
                else if (received.indexOf("ORU^R01") >= 0) {
                    //retrieve barcode
                    String[] hlMSH = received.split("MSH");
                    for (int mscounter = 1; mscounter < hlMSH.length; mscounter++){
                        if( hlMSH[mscounter].indexOf("OBR") < 0){
                            continue;
                        }
                        String[] arrReceived = hlMSH[mscounter].split("OBR");
                        barcode = arrReceived[1];
                        arrReceived = barcode.split("\\|");
                        barcode = arrReceived[3];
//                        System.out.println("barcode" + barcode);
                        //                GUITimerActionListener.getInstance().appendtextToWindow(2, "sec Barocode is : " + barcode);

                        if (barcode.indexOf("Invalid") < 0) {
                            DBwork app = new DBwork();
                            //                    app.update(Long.parseLong(barcode), "hematology", received, "processed");
                            //                    pw.println(received);
//                        JSONObject finalJsonObj = new JSONObject();
//                        finalJsonObj.put("barcode", barcode);
                            //parse the response
                            JSONObject finalFinal = new JSONObject();
                            JSONObject mapResult = new JSONObject();
                            JSONArray TestList = new JSONArray();
                            mapResult.put("barcode", barcode);
                            String finalBodyString = "";
                            String[] hlOBX = hlMSH[mscounter].split("OBX");
                            try {
                                for (int i = 1; i < hlOBX.length; i++) {
                                    String[] hlResult = hlOBX[i].split("\\|");
//                                    System.out.println("result len " + hlResult.length);
                                    if (hlResult[2].equals("NM") || hlResult[2].equals("ED")) {
                                        JSONObject testJsonObj = new JSONObject();
                                        String[] testRes = hlResult[3].split("\\^");
                                        testJsonObj.put("testId", testRes[0]);
                                        try {
                                            testJsonObj.put("reTest", Integer.parseInt(hlResult[17]));
                                        }catch (Exception s){
                                            testJsonObj.put("reTest", 0);       }
                                        testJsonObj.put("state", "urtProcessed");
                                        String res = "";
                                        if (hlResult[2].equals("NM")) {
                                            testJsonObj.put("type", "T");
                                            res = hlResult[5];
                                        } else if (hlResult[2].equals("ED")) {
                                            testJsonObj.put("type", "P");
                                            String[] pRes = hlResult[5].split("\\^Base64\\^");
                                            res = pRes[1];
                                        }
                                        testJsonObj.put("result", res);
//                                        JSONArray TestList = new JSONArray();
                                        TestList.put(testJsonObj);
//                                        finalJsonObj.put("TestList", TestList);
                                    }
                                }
                                mapResult.put("barcode", barcode);
                                mapResult.put("TestList", TestList);
                                JSONArray mapResultList = new JSONArray();
                                mapResultList.put(mapResult);
                                finalFinal.put("mapResult", mapResultList);
                                finalBodyString = finalFinal.toString();
                                // new omid:
                                app.update(Long.parseLong(barcode),equipmentId, finalBodyString , "processed");
//                                System.out.println("sending Hematology result : " + barcode);
//                                pw.println(received);
                                pw.println(finalBodyString);
                                pw.close();
                                this.sendAck();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
                if (received.equals("Exit") || !server2.getIsSigned()) {
                    this.s.close();
                    this.socket.close();
                }

            } catch (IOException e) {
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

    private void sendAck(){
//        System.out.println("Sending Ack to hematology");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String messageAck = this.startOfMessage + "MSH|^~\\&|BC-6800|Mindray|||"+dtf.format(now)
                +"||ACK^R01|5|P|2.3.1||||||UNICODE\r" +"MSA|AA|1\r" + this.endOfMessage;
        //"Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + this.endOfMessage;
        sendToDevice(messageAck);
        this.log(messageAck);
    }
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