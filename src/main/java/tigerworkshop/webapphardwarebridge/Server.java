package tigerworkshop.webapphardwarebridge;

import com.sun.management.OperatingSystemMXBean;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigerworkshop.webapphardwarebridge.controller.SettingController;
import tigerworkshop.webapphardwarebridge.responses.Setting;
import tigerworkshop.webapphardwarebridge.services.SettingService;
import tigerworkshop.webapphardwarebridge.utils.CertificateGenerator;
import tigerworkshop.webapphardwarebridge.utils.TLSUtil;
import tigerworkshop.webapphardwarebridge.websocketservices.*;

import java.lang.management.ManagementFactory;
import java.util.*;

public class Server {

    private static Logger logger = LoggerFactory.getLogger("Server");
    private static Server server = new Server();
    private BridgeWebSocketServer bridgeWebSocketServer;
    private boolean shouldRestart = false;
    private boolean shouldStop = false;
    private String authToken="";
    private SettingService settingService = SettingService.getInstance();
    private Boolean signed=false;
    private Boolean IsSigned=false;
    private static Server instance = new Server();

    public static Server getInstance() {
        return instance;
    }

    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getAuthToken(){ return this.authToken; }
    public void setIsSigned(boolean tf){ this.IsSigned=tf; }
    public boolean getIsSigned() { return this.IsSigned; }

    public static void main(String[] args) {
        try {
            JUnique.acquireLock(Config.APP_ID);
        } catch (AlreadyLockedException e) {
            logger.error(Config.APP_ID + " already running");
            //System.exit(1);
        }
        server.start();
    }

    public void start() {
        while (!shouldStop) {
            shouldRestart = false;

            logger.info("Application Started");
            logger.info("Program Version: " + Config.VERSION);

            logger.debug("OS Name: " + System.getProperty("os.name"));
            logger.debug("OS Version: " + System.getProperty("os.version"));
            logger.debug("OS Architecture: " + System.getProperty("os.arch"));

            logger.debug("Java Version: " + System.getProperty("java.version"));
            logger.debug("Java Vendor: " + System.getProperty("java.vendor"));

            logger.debug("Available processors (cores): " + Runtime.getRuntime().availableProcessors());
            logger.debug("JVM Maximum memory (bytes): " + Runtime.getRuntime().maxMemory());
            logger.debug("System memory (bytes): " + ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize());

            //   SettingService settingService = SettingService.getInstance();
            Setting setting = settingService.getSetting();
//            setting.setDevices();

            try {
                // Create WebSocket Server
                bridgeWebSocketServer = new BridgeWebSocketServer(setting.getBind(), setting.getPort());
                bridgeWebSocketServer.setReuseAddr(true);
                bridgeWebSocketServer.setConnectionLostTimeout(3);
                settingService.setIsSigned(false);
//                server.setIsSigned(false);
//                ThreadManipulate(settingService.AllDevices());
//                settingService.AllDevices();

                new Timer().scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run(){
                        try{
                            if(signed!=instance.getIsSigned()){
                                signed = instance.getIsSigned();
                                if(instance.getIsSigned()) {
//                                    ThreadManipulate(settingService.AllDevices());
                                }
                            }
                        }
                        catch(Exception c){c.printStackTrace();}
                    }
                },0,3000);
                Thread test = new testOmid();
                   test.start();

                // Add Serial Services
//                HashMap<String, String> serials = setting.getSerials();
//                for (Map.Entry<String, String> elem : serials.entrySet()) {
//                    SerialWebSocketService serialWebSocketService = new SerialWebSocketService(elem.getValue(), elem.getKey());
//                    serialWebSocketService.setServer(bridgeWebSocketServer);
//                    serialWebSocketService.start();
//                }

                // Add Printer Service
                PrinterWebSocketService printerWebSocketService = new PrinterWebSocketService();
                printerWebSocketService.setServer(bridgeWebSocketServer);
                printerWebSocketService.start();

                // Add Cloud Proxy Client Service
                if (setting.getCloudProxyEnabled()) {
                    CloudProxyClientWebSocketService cloudProxyClientWebSocketService = new CloudProxyClientWebSocketService();
                    cloudProxyClientWebSocketService.setServer(bridgeWebSocketServer);
                    cloudProxyClientWebSocketService.start();
                }

                // WSS/TLS Options
                if (setting.getTLSEnabled()) {
                    if (setting.getTLSSelfSigned()) {
                        logger.info("TLS Enabled with self-signed certificate");
                        CertificateGenerator.generateSelfSignedCertificate(setting.getAddress(), setting.getTLSCert(), setting.getTLSKey());
                        logger.info("For first time setup, open in browser and trust the certificate: " + setting.getUri().replace("wss", "https"));
                    }
                    bridgeWebSocketServer.setWebSocketFactory(TLSUtil.getSecureFactory(setting.getTLSCert(), setting.getTLSKey(), setting.getTLSCaBundle()));
                }

                // Start WebSocket Server
//                bridgeWebSocketServer.start();

                logger.info("WebSocket started on " + setting.getUri());

                while (!shouldRestart && !shouldStop) {
                    Thread.sleep(100);
                }

                bridgeWebSocketServer.close();
                bridgeWebSocketServer.stop();


            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void stop() {
        shouldStop = true;
    }

    public void restart() {
        shouldRestart = true;
    }

    public void ThreadManipulate(List<HashMap<String, String>> devices) {
        String device;
        String port;
        String path;
        String source;
        Integer threadNumber;
        String equipmentId;
        logger.info("...........Thraed manipulate.......");
        for(int i =0 ; i< devices.size() ; i++){
            HashMap<String,String> map;
            map = devices.get(i);
            device = map.get("name");
            port = map.get("port");
            path = map.get("path");
            source = map.get("source");
            threadNumber = Integer.parseInt(map.get("id"));
            equipmentId = map.get("equipmentId");
            switch(device){
                case "Modular":
//                    Thread modularServer = new ModularServer(port,path,threadNumber,equipmentId);
                    Thread modularServer = new modularServer2(port,path,threadNumber,equipmentId);
//                    modularServer.start();
                    break;
                case "sysmex":
                    Thread sys = new sysmex(port,path,threadNumber, equipmentId);
                    sys.start();
                    break;
                case "BS480":
                    Thread BS480 = new BS480Server(port,path,threadNumber,equipmentId);
//                    BS480.start();
                    break;
                case "Hemato":
//                    Thread hemato = new hematologyServer(port,path,threadNumber,equipmentId);
                    Thread hemato = new hematologyServer2(port,path,threadNumber,equipmentId);
//                    hemato.start();
                    break;
                case "Immulite":
                    Thread Immulite = new ImmuliteServer2(port,path,threadNumber,equipmentId);
//                    Immulite.start();
                    break;
                case "CobasE411":
                    Thread e411 = new CobasE411Server(port,path,threadNumber,equipmentId);
//                    e411.start();
                    break;
                case "Abbott":
                    Thread Abbott = new AbbottServer(port,path,threadNumber,equipmentId);
//                    Abbott.start();
                    break;
                case "Barcode":
                    Thread bar = new getBarcodesFromServer(port,path,threadNumber, settingService.equipmentIds());
                    bar.start();
                    break;
                case "Picsender":
//                    Thread pic = new picSender(port, path, source, threadNumber);
//                    pic.start();
                    break;
            }
        }
        Thread ServerSender = new ServerSender();
        ServerSender.start();
//        Thread tcp = new TCPport();
//        tcp.start();
    }
}
