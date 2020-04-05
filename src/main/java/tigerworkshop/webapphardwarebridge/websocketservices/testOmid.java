package tigerworkshop.webapphardwarebridge.websocketservices;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

public class testOmid extends Thread {
    public static void watchDirectoryPath(Path path) {
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path
                        + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }
        System.out.println("Watching path: " + path);
        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();
        try (WatchService service = fs.newWatchService()) {
            // We register the path to the service
            // We watch for creation events
//            path.register(service, ENTRY_CREATE);
//            path.register(service, ENTRY_MODIFY);
//            path.register(service, ENTRY_DELETE);
            path.register(service, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();
                // Dequeueing events
                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue; // loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        // Output
                        System.out.println("New path created: " + newPath);
                    } else if (ENTRY_MODIFY == kind) {
                        // modified
                        Path newPath = ((WatchEvent<Path>) watchEvent)
                                .context();
                        // Output
                        System.out.println("New path modified: " + newPath);
                    }
                }
                if (!key.reset()) {
                    break; // loop
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
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
                        if (Files.probeContentType(child).equals("text/plain")) {
                            System.out.println(" Size:\t" + Files.size(child) + "\n File name:\t" + filename);
                        } else {
                            Files.deleteIfExists(child);
                            continue;
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }

                    } catch (Exception x) {
                        Files.deleteIfExists(child);
                        continue;
                    }
                }
            } catch (Exception e) {
//                    Files.deleteIfExists(child);
                continue;
            }
        }

    }
    @Override
    public void run(){
        System.out.println("---------------TestOmid------------");
        File dir = new File("E:\\zzz");
        try {
            omidWatcher(dir.toPath());
        }catch (Exception e){
            e.printStackTrace();
        }
//        watchDirectoryPath(dir.toPath());
//       DBwork dbWork = new DBwork();
//        String barcodeBuffer;
//        String resultBuffer;
//        List<HashMap<String,String>> list = dbWork.selectWithStateEq("failed","processed",null);
//        for(int i=0 ; i< list.size(); i++) {
//            HashMap<String, String> map = list.get(i);
//            barcodeBuffer = map.get("barcode");
//            resultBuffer = map.get("result");
//            resultBuffer = resultBuffer.replace('\3', '#').
//                    replace('\2', '#').replace("#", "");
//            String device_type = "sysmex-1";
//            resultBuffer = device_type + "#" + resultBuffer;
//
//            System.out.println("barcode-->"+barcodeBuffer+"\nresult:\n"+resultBuffer);
        }

//        String barcode = "10031550";
//        System.out.println("-------------TestJson--------------");
//        Boolean thereExist;
//        while(true){
//            JSONObject finalFinal;
//            JSONArray mapResult = new JSONArray();
//             JSONObject TestListObj ;
//            JSONArray TestList = new JSONArray();
//        try{
//            JSONObject obj = new JSONObject();
//            String resultString = dbWork.selectResult(Long.parseLong(barcode));
//            List<HashMap<String,String>> list = listGenerator(3);
//            finalFinal = new JSONObject(resultString);
//             mapResult = finalFinal.getJSONArray("mapResult");
//             TestListObj = mapResult.getJSONObject(0);
//             TestList = TestListObj.getJSONArray("TestList");
//            thereExist = false;
//            for(int j = 0 ; j< list.size() ; j++){
//                if (!TestList.equals(null)) {
//                    for (int i = 0; i < TestList.length(); i++) {
//                        JSONObject eachResultJson = TestList.getJSONObject(i);
//                        System.out.println("\nJson" + eachResultJson.getString("testId") + "\nList" + list.get(j).get("testId"));
//                        if (eachResultJson.getString("testId").equals(list.get(j).get("testId"))) {
//                            thereExist = true;
//                            System.out.println("testId repetitive!!!  " + list.get(j).get("testId"));
//                            eachResultJson = null;
//                        }
//                    }
//                }
//                if (! thereExist){
//                    JSONObject jobj = new JSONObject();
//                    for(Map.Entry<String,String> entry : list.get(j).entrySet())
//                        jobj.put(entry.getKey(),entry.getValue());
//                    TestList.put(jobj);
//                }
//            }
//            obj.put("TestList",TestList);
//            mapResult.remove(0);
//            mapResult.put(obj);
//            finalFinal.put("mapResult", mapResult);
//            System.out.println(finalFinal);
//            dbWork.update(Long.parseLong(barcode),"20", finalFinal.toString(),"processed");
//            TimeUnit.SECONDS.sleep(20);
//        }catch (Exception e){
//            e.printStackTrace();
//                }
//        }
//    }
//    private List listGenerator(Integer n){
//        List<HashMap<String,String>> list = new ArrayList<>();
//        for(int i=0 ;i<n ; i++){
//        HashMap<String,String> resultMap = new HashMap<>();
//        resultMap.put("testId","40"+i);
//        resultMap.put("reTest", "0");
//        resultMap.put("state","omid");
//        resultMap.put("type","T");
//        resultMap.put("result","358"+i );
//        resultMap.put("equipmentId", "001" );
//        list.add(resultMap);
//        }
//        return list;
//    }
}
