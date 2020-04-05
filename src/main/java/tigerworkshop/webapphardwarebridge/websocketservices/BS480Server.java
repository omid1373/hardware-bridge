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
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.Internal.logger;

public class BS480Server extends Thread{
    private String authToken = null;
    private WebSocketServerInterface server = null;
    public ServerSocket ss;
    private String portNumber;
    private String path;
    private int ThreadNumber;
    private String equipmentId;

    public BS480Server(String port, String urlpath, Integer threadNumber, String eqId) {
        this.portNumber = port;
        this.path = urlpath;
        this.ThreadNumber = threadNumber;
        this.equipmentId = eqId;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(Integer.parseInt(portNumber));
//            logger.info("BS480 ..............BS480 ................BS480 ");
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
                ss.setSoTimeout(2000);
                // socket object to receive incoming client requests
                s = ss.accept();
                if(!server.getIsSigned()){
                    s.close(); ss.close(); break;}
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                // create a new thread object
                Thread t = new ClientHandler480(s, dis, dos, authToken, server, settingService, path, ThreadNumber, equipmentId);
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
class ClientHandler480 extends Thread {
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
    InputStream deviceInputStream;
    OutputStream deviceOutputStream;

    public ClientHandler480(Socket s, DataInputStream dis, DataOutputStream dos, String token, Server serv,
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
        try{
            deviceInputStream = s.getInputStream();
            deviceOutputStream = s.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        BS480parser parser = new BS480parser();
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
//                        System.out.println("reading new buffer ...");
                        //read second set
                        byte[] sdata = new byte[1024];
                        if (deviceInputStream.read(sdata) == -1) {
                            break;
                        }
                        String newReceived = new String(sdata);
                        receivingBuffer = receivingBuffer + newReceived;
                    }
                    receivingBuffer = receivingBuffer.replaceAll("\\s", "");
                    receivingBuffer = receivingBuffer.replaceAll("\u0000", ""); // removes NULL chars
                    receivingBuffer = receivingBuffer.replaceAll("\\u0000", ""); // removes backslash+u0000
                }
                // گرفتن تستهای مربوط به دستگاه
                String newMessage = receivingBuffer.substring(0, receivingBuffer.indexOf(eof));
                receivingBuffer = receivingBuffer.replace(newMessage, "");
                if (receivingBuffer.startsWith(eof + "")) {
                    receivingBuffer = receivingBuffer.replaceFirst(eof + "", "");
                }
//                System.out.println(newMessage);
                List<String> msgList = parser.messageParser(newMessage,equipmentId);
                for(int k=0 ; k < msgList.size(); k++) {
                    sendToDevice(msgList.get(k));
                    if(k > 0){
                        try{
                            TimeUnit.MILLISECONDS.sleep(500);
                        }catch (Exception e){e.printStackTrace();}
                    }
                }

            } catch (Exception p) {
            }
        }
    }
    private void sendToDevice(String preString) {
        try {
//            System.out.println("output:\n"+preString+"\n");
            deviceOutputStream.write(preString.getBytes());
            deviceOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

