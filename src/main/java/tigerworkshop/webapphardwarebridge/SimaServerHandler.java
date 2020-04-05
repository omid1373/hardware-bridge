package tigerworkshop.webapphardwarebridge;

import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import tigerworkshop.webapphardwarebridge.services.SettingService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimaServerHandler {

    private File db_file = new File("failed_results_db");

    SimaServerHandler() {
        readFailedResultsFromFile();
        timerService.setRepeats(true);
        timerService.start();
    }
    private List<String> failedResults = new ArrayList<>();
    private void readFailedResultsFromFile() {
        try {
            failedResults = FileUtils.readLines(db_file, "UTF-8");
            failedResults.remove("");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private Timer timerService = new Timer(30000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            List successfullySent = new ArrayList();
            for (String s : failedResults) {
                boolean success = sendToSimaServer(s);
                if (success)
                    successfullySent.add(s);
            }
            synchronized (failedResults) {
                failedResults.removeAll(successfullySent);
            }
            // Write to file
            addToFailedResults(null);
        }
    });

    boolean sendToSimaServer(String theMessage) {
        if (theMessage.isEmpty() || theMessage == null)
            return true;
        if (!theMessage.startsWith("{\"response"))
            theMessage = "{\"response\":\"" + theMessage + "\"}";
        System.out.println("sending to the server: ");
        System.out.println(theMessage);
        String serverUrl = SettingService.getInstance().getSetting().getSimaServerUrl()
                + "/rest/s1/simaEquipmentInterOperation/parseSysmex";
        String username = SettingService.getInstance().getSetting().getSimaUsername();
        String password = SettingService.getInstance().getSetting().getSimaPassword();

        OkHttpClient client = new OkHttpClient();

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, theMessage);
        //new okhttp3.RequestBody(theMessage, JSON);

        String credential = Credentials.basic(username, password);
        Request request = new Request.Builder().url(serverUrl).addHeader("Authorization", credential)
                .post(requestBody).build();

        Response response;
        try {
            response = client.newCall(request).execute();
            System.out.println(response.body().string());
            if (!response.isSuccessful()) {
                System.out.println("Response was not 200 OK.");
                addToFailedResults(theMessage);
                return false;
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            addToFailedResults(theMessage);
            return false;
        }
    }

    private void addToFailedResults(String theMessage) {

        if (theMessage != null && !failedResults.contains(theMessage)) {
            synchronized (failedResults) {
                failedResults.add(theMessage);
            }
        }
        try {
            FileUtils.writeLines(db_file, failedResults, false);
        } catch (IOException e) {
            System.out.println("Error saving failed results");
        }
    }
}
