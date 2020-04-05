package tigerworkshop.webapphardwarebridge.websocketservices;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import static okhttp3.internal.Internal.logger;


public class picSender extends Thread{
    private String AuthToken=null;
    private String port;
    private String Url;
    private String SourceFolder;
    private boolean signed=false;
    private int ThreadNumber;


    public picSender(String port, String urlpath, String source, Integer threadNumber){
        this.port=port;
        this.Url=urlpath;
        this.SourceFolder=source;
        this.ThreadNumber=threadNumber;
    }

    SettingService settingService = SettingService.getInstance();
    Server server=Server.getInstance();

    void startWatching(ActionListener l, String dictionary) {
        try {
            FileAlterationObserver observer = new FileAlterationObserver(dictionary);
            FileAlterationMonitor monitor = new FileAlterationMonitor(3000);

            FileAlterationListener listener = new FileAlterationListenerAdaptor() {
                private ArrayList<String> files = new ArrayList<>();

                @Override
                public void onFileCreate(File file) {
                    if (file.getName().contains(".fuse_hidden")
                            || file.getName().contains(".nfs"))
                        return;
                    if (file.isDirectory()) {
                        return;
                    }
                    String absolutePath = file.getAbsolutePath();
                    for (String s : files) {
                        if (s.equals(absolutePath))
                            return;
                    }
                    onFileChange(file);
//                    files.add(absolutePath);
                }

                private boolean checkFile(File file) {
                    final long s = file.getUsableSpace();
                    try {
                        long l1 = file.length();
                        Thread.sleep(1000); //60000
                        long l2 = file.length();
                        if (l1 != l2)
                            throw new Exception("EX: File is in process");
//                        System.out.println(file.getName() +"File is out of process");
                        return true;
                    } catch (Exception ex) {
//                        System.out.println(file.getName() + " is copying");
                        return false;
                    }
                }

                @Override
                public void onFileDelete(File file) {
                    // code for processing deletion event
                }

                @Override
                public void onFileChange(final File file) {

                    if (file.getName().contains(".fuse_hidden")
                            || file.getName().contains(".nfs"))
                        return;

                    String cur = null;
                    String newFileName = file.getAbsolutePath();
                    for (int i = 0; i < files.size(); i++) {
                        String s = files.get(i);
                        if (s.equals(newFileName)) {
                            cur = s;
                            break;
                        }
                    }
                    if (cur == null){
                        files.add(newFileName);
//                        System.out.println("new file added to queue: " + newFileName);
                    }
                    new Thread() {
                        public void run() {
                            if (checkFile(file)) {
                                synchronized (files) {
                                    int con = containsFile(file);
                                    if (con >= 0) {
                                        files.remove(con);
                                    } else {
//                                        System.out.println("file has been added before: "
//                                                + file.getAbsolutePath());
                                        return;
                                    }
                                }
                                l.actionPerformed(new ActionEvent(
                                        file, (int) (Math.random() * 10000), "change"));
                            }
                        }
                    }.start();
                }

                private int containsFile(File f) {
                    String path = f.getAbsolutePath();
                    for (int i = 0; i < files.size(); i++) {
                        String s = files.get(i);
                        if (path.equals(s))
                            return i;
                    }
                    return -1;
                }
            };
            observer.addListener(listener);
            monitor.addObserver(observer);
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    try{
                        if(signed!=server.getIsSigned()) {
//                            logger.info("............PicSender............get signed in :   "+settingService.getIsSigned());
                            signed = server.getIsSigned();
                            if (server.getIsSigned()) {
                                monitor.start();
                                settingService.setDevice(ThreadNumber);
                            } else {
                                monitor.stop();
                                settingService.setDevice(ThreadNumber+100);
                            }
                        }
                    }
                    catch(Exception c){c.printStackTrace();}}
            },0,2000);
//            monitor.start();

//            System.out.println("Watching started");
//            l.actionPerformed(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean uploadFile(File f) {
//        AuthToken=settingService.getAuthToken();
        AuthToken=server.getAuthToken();
        String url =  settingService.getSimaUrl()+ Url;

        OkHttpClient client = new OkHttpClient();

        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", f.getName(), RequestBody.create(MEDIA_TYPE_PNG, f))
                .addFormDataPart("name", FilenameUtils.removeExtension(f.getName()))
                .build();

        Request request = new Request.Builder().url(url).addHeader("api_key", AuthToken)
                .post(requestBody).build();

        Response response;
        try {
            response = client.newCall(request).execute();
//            System.out.println(response.body().string());
            if (!response.isSuccessful()) {
//                System.out.println("Response was not 200 OK.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

        @Override
        public void run() {
            while(AuthToken == null){
                try{
                    TimeUnit.SECONDS.sleep(1);
//                    AuthToken = settingService.getAuthToken();
                    AuthToken = server.getAuthToken();
                }
                catch(Exception e){ }
            }
            startWatching(e -> {
                File f = (File) e.getSource();
//                System.out.println("file     : " + f.getAbsoluteFile());

                if (f.isDirectory()) {
                    return;
                }
                boolean success = uploadFile(f);
                if (success){
                    f.delete();
                }
            }, SourceFolder);
    }
}
