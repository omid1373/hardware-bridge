package tigerworkshop.webapphardwarebridge.websocketservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class logger {
    private PrintWriter pw;
    public void log(String file, String message) throws IOException {
        pw = new PrintWriter(new FileOutputStream(new File("logger/"+file+".txt"), true));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        pw.println(dtf.format(now) + "----"+message);
        pw.close();
    }
}

