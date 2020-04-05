package tigerworkshop.webapphardwarebridge.websocketservices;

import okhttp3.*;
import org.java_websocket.WebSocketListener;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.DBwork;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.interfaces.WebSocketServerInterface;
import tigerworkshop.webapphardwarebridge.services.SettingService;


import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;


public class ModularServer  extends Thread{
    private String authToken = null;
    private WebSocketServerInterface server = null;
    private Properties p;
    public ServerSocket ss;
    private String portNumber;
    private String path;
    private int ThreadNumber;
    private String equipmentId;

    public ModularServer(String port, String urlpath,Integer threadNumber, String eqId){
        this.portNumber = port;
        this.path = urlpath;
        this.ThreadNumber = threadNumber;
        this.equipmentId = eqId;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(Integer.parseInt(portNumber));
            logger.info("Modular started on "+portNumber+"..............Modular started on "+portNumber+"................Modular started on "+portNumber);
        }
        catch(IOException e){ e.printStackTrace();}

        SettingService settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        authToken = null;
        while(authToken == null){
            try{
                TimeUnit.SECONDS.sleep(1);
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
                    // socket object to receive incoming client requests
                    s = ss.accept();
                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    // create a new thread object
                    Thread t = new ClientHandler(s, dis, dos, authToken, server, settingService, path, ThreadNumber, equipmentId);
                    // Invoking the start() method
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
class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    private String authToken=null;
    private SettingService sett;
    private String URL;
    private String Urlpath=null;
    private int threadnumber;
    private Server server2;
    private String equipmentId;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String token, Server serv,
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
        }

    @Override
    public void run() {

        String received = "";
        String barcode = "";
        String messageSituation;
        while (true) {
            if (!server2.getIsSigned()) break;
            else  sett.setDevice(threadnumber);
            try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(new File("modularResult.txt"), true));
                    InputStream is = s.getInputStream();
                    OutputStream os = s.getOutputStream();

                    // Receiving Data
                    char eof = (char) 28;
                    byte[] data = new byte[1024];
                    if (is.read(data) == -1) {
                        throw new EOFException();
                    }
                    received = new String(data);
                    received = received.replaceAll("\\s", "");
                    if (received != "") {
                        while (received.contains("" + eof) == false){
                            //read second set
                            byte[] sdata = new byte[1024];
                            if (is.read(sdata) == -1) {
                                throw new EOFException();
                            }
                            String sReceived = new String(sdata);
                            received = received + sReceived;
                        }
                        received = received.replaceAll("\\s",""); // remove NULL
                    }
//                System.out.println("Modular received:  "+received);

                    if (received.indexOf("QRY^Q02") > 0) {
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        LocalDateTime now = LocalDateTime.now();
//                        String messageAck = Character.toString((char) 11) + "MSH|^~\\&|||Mindray|BS-400|20120304000000||QCK^Q02|1|P|2.3.1||||||ASCII|||\rMSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + Character.toString((char) 28) + Character.toString((char) 13);
                        String messageAck = Character.toString((char) 11) + "MSH|^~\\&|||Mindray|BS-400|"+dtf.format(now)+"||QCK^Q02|1|P|2.3.1||||||ASCII|||\rMSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + Character.toString((char) 28) + Character.toString((char) 13);
                        // Sending Dat
                        os.write(messageAck.getBytes());
                        os.flush();

                        //get barcode
                        int endBarcode = received.indexOf("|OTH|");
                        int startBarcode = received.indexOf("|||RD|") + 6;
                        if (startBarcode != 5 && endBarcode != -1) {
                            barcode = received.substring(startBarcode, endBarcode);
                        }
                        if (barcode != ""){
                            //make HL7 query string
                            String preString = Character.toString((char) 11) + "MSH|^~\\&|||Mindray|BS-400|20120304000000||DSR^Q03|1|P|2.3.1||||||ASCII|||\rMSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\rQRD|20120304000000|R|D|1|||RD|0019|OTH|||T|\rQRF||||||RCT|COR|ALL||\rDSP|1||0054|||\rDSP|2||27|||\rDSP|3|||||\rDSP|4||20020304000000|||\rDSP|5||F|||\rDSP|6||O|||\rDSP|7|||||\rDSP|8|||||\rDSP|9|||||\rDSP|10|||||\rDSP|11|||||\rDSP|12|||||\rDSP|13|||||\rDSP|14|||||\rDSP|15||outpatient|||\rDSP|16|||||\rDSP|17||own|||\rDSP|18|||||\rDSP|19|||||\rDSP|20|||||\rDSP|21||" + barcode + "|||\rDSP|22||1|||\rDSP|23||20120904000000|||\rDSP|24||N|||\rDSP|25||1|||\rDSP|26||serum|||\rDSP|27|||||\rDSP|28|||||\r";
                            //read testIDs from DB
                            String testIds = "";
                            DBwork app = new DBwork();
                            testIds = app.selectTestIds(Long.parseLong(barcode));
                            //add tests to query string
                            int count = 29;
                            for (String testId : testIds.split(",")) {
                                preString = preString + "DSP|" + count + "||" + testId + "^^^|||\r";
                                count++;
                            }

                            //add footer
                            preString = preString + "DSC||\r" + Character.toString((char) 28) + Character.toString((char) 13);

                            //send final string to equipment
                            os.write(preString.getBytes());
                        }
                    } else if (received.indexOf("ORU^R01") > 0){
                        // send "message accepted" to client (equipment) with HL7 standard
                        messageSituation = ".MSH|^~\\&|||Mindray|BS-400|20111026172623||ACK^R01|1|P|2.3.1||||0||ASCII|||\\nMSA|AA|1|Message Accepted|||0|\\n.";
                        byte[] messageSituationBytes = messageSituation.getBytes();
                        os.write(messageSituationBytes);

                        int barcodeStart = received.indexOf("OBR|");
                        if (barcodeStart != -1) {
                            barcode = received.substring(barcodeStart).split("\\|")[2];
                        }
                        if (barcode != null && !barcode.isEmpty()){
                            //get results

                            // update db with the received results from device
                            DBwork dbWorker = new DBwork();
//                            parse the response
                            String[] hlOBX = received.split("OBX");
                            for (int i = 1; i < hlOBX.length; i++) {
                                String[] hlResult = hlOBX[i].split("\\|");
                                String finalBodyString = "";
                                if (!hlResult[3].equals("")){
                                    JSONObject finalJsonObj = new JSONObject();
                                    finalJsonObj.put("barcode", barcode);
                                    JSONObject testJsonObj = new JSONObject();
                                    testJsonObj.put("testId", hlResult[3]);
                                    try{
                                        testJsonObj.put("reTest", Integer.parseInt(hlResult[17]));
                                    }catch (Exception e){
                                        testJsonObj.put("reTest", 0);
                                    }
                                    testJsonObj.put("state", "urtProcessed");
                                    testJsonObj.put("type", "T");
                                    testJsonObj.put("result", hlResult[5]);
                                    testJsonObj.put("equipmentId", equipmentId );
                                    JSONArray TestList = new JSONArray();
                                    TestList.put(testJsonObj);
                                    finalJsonObj.put("TestList", TestList);
                                    JSONObject finalFinal = new JSONObject();
                                    JSONArray mapResult = new JSONArray();
                                    mapResult.put(finalJsonObj);
                                    finalFinal.put("mapResult", mapResult);
                                    finalBodyString = finalFinal.toString();
                                    dbWorker.update(Long.parseLong(barcode),"Modular", finalBodyString ,"processed");
                                }

                                // send parsed response to sima server
//                                pw.println(finalBodyString);
//                                System.out.print(finalBodyString);
                            }
                        }
                        pw.close();
                    }

                    if (received.equals("Exit") || !server2.getIsSigned()) {
                        this.s.close();
//                        logger.info("Modular :  Connection closed");
                        break;
                    }

                } catch (IOException e) {
                e.printStackTrace();
                    break;
                }
        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}