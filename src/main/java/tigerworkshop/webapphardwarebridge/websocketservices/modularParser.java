package tigerworkshop.webapphardwarebridge.websocketservices;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class modularParser {
    static String startOfMessage = Character.toString((char) 11);
    static String endOfMessage = Character.toString((char) 28) + Character.toString((char) 13);
    static String carriageReturn = Character.toString((char) 13);
    logger logFile = new logger();
    List<String> queryList;
    List<String> resultList;
    DBwork dbWorker ;
    String equipmentId;

    public List messageParser(String received,String eqId) throws IOException {
//        System.out.println("------received: "+received);
        logFile.log("modularlog","(received:) "+received);
        queryList = new ArrayList<>();
        resultList = new ArrayList<>();
        dbWorker = new DBwork();
        this.equipmentId = eqId;
        if (received.indexOf("QRY^Q02") > 0) {
            parseQuery(received);
            return queryList;
        } else if (received.indexOf("ORU^R01") > 0) {
//            System.out.println("--------------------ORU");
            parseResult(received);
            return resultList;
        }
        return null;
    }
//----------------Query---------Query---------Query---------Query---------Query---------Query---------Query------------
    private void parseQuery(String received) throws IOException{
        String barcode = "";
//        logFile.log("modularlog","QRY LOG ----->"+received);
        int endBarcode = received.indexOf("|OTH|");
        int startBarcode = received.indexOf("|||RD|") + 6;
        if (startBarcode != 5 && endBarcode != -1) {
            barcode = received.substring(startBarcode, endBarcode);
        }
        if (barcode != "") {
//make HL7 query string
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime now = LocalDateTime.now();
   //read testIDs from DB
            String testIds = "";
            DBwork app = new DBwork();
            testIds = app.selectTestIds(Long.parseLong(barcode));
            int count = 29;
            if(!testIds.equals("")) {
                this.queryList.add(sendAck(true));
                String preString = startOfMessage +
                        "MSH|^~\\&|||Mindray|BS-400|" +
                        dtf.format(now) +
                        "||DSR^Q03|1|P|2.3.1||||||ASCII|||\rMSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\rQRD|20120304000000|R|D|1|||RD|0019|OTH|||T|\rQRF||||||RCT|COR|ALL||\rDSP|1||0054|||\rDSP|2||27|||\rDSP|3|||||\rDSP|4||20020304000000|||\rDSP|5||F|||\rDSP|6||O|||\rDSP|7|||||\rDSP|8|||||\rDSP|9|||||\rDSP|10|||||\rDSP|11|||||\rDSP|12|||||\rDSP|13|||||\rDSP|14|||||\rDSP|15||outpatient|||\rDSP|16|||||\rDSP|17||own|||\rDSP|18|||||\rDSP|19|||||\rDSP|20|||||\rDSP|21||" + barcode + "|||\rDSP|22||1|||\rDSP|23||20120904000000|||\rDSP|24||N|||\rDSP|25||1|||\rDSP|26||serum|||\rDSP|27|||||\rDSP|28|||||\r";
                for (String testId : testIds.split(",")) {
                    if (!testId.equals("") && !testId.equals(null)) {
                        preString = preString + "DSP|" + count + "||" + testId + "^^^|||\r";
                        count++;
                    }
                }
                    preString = preString + "DSC||\r" + Character.toString((char) 28) + Character.toString((char) 13);
                this.queryList.add(preString);
            }
            else {
                queryList.add(sendAck(false));
            }
        }
    }

//---------------------------Result---------Result---------Result---------Result---------Result-----------------------
    private void parseResult(String received) throws IOException{
        String barcode = "";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String messageSituation = startOfMessage +
                "MSH|^~\\&|||||"
                + dtf.format(now)
                + "||ACK^R01|1|P|2.3.1||||0||ASCII|||"
                + carriageReturn
                + "MSA|AA|1|Message accepted|||0|"
                + carriageReturn
                + endOfMessage;
        this.resultList.add(messageSituation);
        int barcodeStart = received.indexOf("OBR|");
        if (barcodeStart != -1) {
            barcode = received.substring(barcodeStart).split("\\|")[2];
        }
//        System.out.println("barcode\t"+barcode);
        if (barcode != null && !barcode.isEmpty()) {
            JSONArray TestList = new JSONArray();
            JSONObject finalFinal = new JSONObject();
            JSONArray mapResult = new JSONArray();
            JSONObject finalJsonObj = new JSONObject();
            String[] hlOBX = received.split("OBX");
            finalJsonObj.put("barcode", barcode);
            for (int i = 1; i < hlOBX.length; i++) {
                String[] hlResult = hlOBX[i].split("\\|");
                if (!hlResult[5].equals("")) {
                    JSONObject testJsonObj = new JSONObject();
                    if (!hlResult[3].equals("")) {
                        testJsonObj.put("testId", hlResult[3]);
                    }
                    else if(!hlResult[4].equals("")) {
                        testJsonObj.put("testId", hlResult[4]);
                    }else{
                        continue;
                    }
                    try {
                        testJsonObj.put("reTest", Integer.parseInt(hlResult[17]));
                    } catch (Exception e) {
                        testJsonObj.put("reTest", 0);
                    }
                    testJsonObj.put("state", "urtProcessed");
                    testJsonObj.put("type", "T");
                    testJsonObj.put("equipmentId", equipmentId);
                    testJsonObj.put("result", hlResult[5]);
                    TestList.put(testJsonObj);
                }
            }
            finalJsonObj.put("TestList", TestList);
            mapResult.put(finalJsonObj);
            finalFinal.put("mapResult", mapResult);
//            System.out.println("finalFinal:\n"+finalFinal);
            dbWorker.update(Long.parseLong(barcode), equipmentId, finalFinal.toString(), "processed");
        }
    }

    private String sendAck(Boolean barcodeFound){
        String messageAck = startOfMessage;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        if(barcodeFound) {
            messageAck += "MSH|^~\\&|||Mindray|BS-400|"+dtf.format(now)+"||QCK^Q02|1|P|2.3.1||||||ASCII|||\r" +
                    "MSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + endOfMessage;
        }
        else{
            messageAck += "MSH|^~\\&|||Mindray|BS-400|"+dtf.format(now)+"||QCK^Q02|1|P|2.3.1||||||ASCII|||\r" +
                    "MSA|AA|1|Message Accepted|||0|\rERR|0|\rQAK|SR|NF|\r" + endOfMessage;
        }
        return messageAck;
    }
}
