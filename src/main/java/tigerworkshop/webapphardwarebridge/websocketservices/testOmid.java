package tigerworkshop.webapphardwarebridge.websocketservices;

import okhttp3.*;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class testOmid extends Thread {
    private String authToken = null;
    private int ThreadNumber;
    private String equipmentId;
    private String url;
    private String directory;

    public testOmid( String urlpath, Integer threadNumber, String eqId, String sourceDIR) {
        this.url = urlpath ;
        this.ThreadNumber = threadNumber ;
        this.equipmentId = eqId ;
        this.directory = sourceDIR;
    }

    @Override
    public void run(){
        System.out.println("---------------TestOmid------------");
        SettingService settingService = SettingService.getInstance();
        Server server=Server.getInstance();
        authToken = null;
        while(authToken == null){
            try{
                TimeUnit.SECONDS.sleep(1);
                authToken=server.getAuthToken();
            } catch(Exception e){ }
        }
        settingService.setDevice(ThreadNumber);
        File dir = new File(directory);
        try {
            omidWatcher(dir.toPath());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void omidWatcher(Path path) throws Exception{
        WatchService watcher = FileSystems.getDefault().newWatchService();
        WatchKey key =  path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        while(true) {
            try {
//            key = watcher.take();
//            key = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                key = path.register(watcher, ENTRY_CREATE);
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    Path child = path.resolve(filename);
                    try {
                        if (Files.probeContentType(child).equals("image/jpeg")) {
                            System.out.println(" Size:\t" + Files.size(child) + "\n File name:\t" + child.toFile().getName() );
                            uploadServer(child.toFile());
//                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//                                Desktop.getDesktop().browse(new URI("https://www.google.com"));
//                            }
                        } else {
                            Files.deleteIfExists(child);
                            continue;
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                    } catch (Exception x) {
//                        System.out.println("type:\t"+Files.probeContentType(child));
                        Files.deleteIfExists(child);
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
    private void uploadServer(File file) throws IOException{
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody body = RequestBody.create(mediaType, file);
        MultipartBody multiBody = new MultipartBody.Builder()
                .addFormDataPart("Content-Type", "image/jpeg")
                .addFormDataPart("omid", file.getName(), body)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("Accept", "image/jpeg")
//                .addHeader("api_key", authToken)
                .build();
        Response response = client.newCall(request).execute();
        response.body().close();
        System.out.println(request.toString());
        System.out.println(response.toString());
    }
}
