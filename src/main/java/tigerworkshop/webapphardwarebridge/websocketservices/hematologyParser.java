package tigerworkshop.webapphardwarebridge.websocketservices;

import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class hematologyParser {
    static String startOfMessage = Character.toString((char) 11);
    static String endOfMessage = Character.toString((char) 28) + Character.toString((char) 13);
    static String carriageReturn = Character.toString((char) 13);
    logger logFile = new logger();
    private String barcode;
    List<String> queryList;
    List<String> resultList;
    DBwork dbWorker ;
    String equipmentId;
    public List messageParser(String received,String eqId) throws IOException {
        logFile.log("modularlog",received);
        queryList = new ArrayList<>();
        resultList = new ArrayList<>();
        dbWorker = new DBwork();
        if (received.indexOf("ORC") > 0) {
            String[] tempD = received.split("ORC");
            this.barcode = tempD[1];
            tempD = barcode.split("\\|");
            this.barcode = tempD[3];
        }
        this.equipmentId = eqId;
        if (received.indexOf("ORM^O01") >= 0 && received.indexOf("ORU^R01") < 0) {
            parseQuery(received);
            return queryList;
        } else if (received.indexOf("ORU^R01") >= 0) {
            parseResult(received);
            return resultList;
        }
        else if (received.equals("Exit")) {
        }
        return null;
    }
    private void parseQuery(String received) throws IOException{
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
        this.queryList.add(dataToSendBc6800);
    }
    private void parseResult(String received){
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
//            System.out.println("barcode" + barcode);
            if (barcode.indexOf("Invalid") < 0) {
                DBwork app = new DBwork();
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
//                        System.out.println("result len " + hlResult.length);
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
//                                        JSON
//                                        Array TestList = new JSONArray();
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

                    app.update(Long.parseLong(barcode),equipmentId, finalBodyString , "processed");
                    this.resultList.add(this.sendAck());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private String sendAck(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
         String messageAck = this.startOfMessage + "MSH|^~\\&|BC-6800|Mindray|||"+dtf.format(now)
                +"||ACK^R01|5|P|2.3.1||||||UNICODE\r" +"MSA|AA|1\r" + this.endOfMessage;
        //"Message Accepted|||0|\rERR|0|\rQAK|SR|OK|\r" + this.endOfMessage;
        return messageAck;
    }
}
