package tigerworkshop.webapphardwarebridge.websocketservices;


import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static okhttp3.internal.Internal.logger;

public class ImmuliteParser {
    private List<String> writeMessage;
    private String buffer = null;
    private String barcodeBuffer = null;
//    private logger logFile;
//--------------------------------------Query-----Query-----Query-------------------------------
    public List<String> query(List<String> msg){
        String barcode ;
        String messageBuffer;
        Integer count;
        writeMessage = new ArrayList<>();
        if(msg.size() < 1) return null;
        enquiry();
        header();
        messageBuffer = msg.get(0);
        barcode = messageBuffer.split("\\|")[2].substring(1);
        patient(barcode);
        count = orders(barcode);
        last(count);
        ending();
//        System.out.println("writeMessage:  "+writeMessage.size());
        return writeMessage;
    }
    private String ch(String input){
        switch(input){
            case "STX":
                return Character.toString((char)2);
            case "ETX":
                return Character.toString((char)3);
            case "EOT":
                return Character.toString((char)4);
            case "ENQ":
                return Character.toString((char)5);
            case "ACK":
                return Character.toString((char)6);
            case "LF":
                return Character.toString((char)10);
            case "CR":
                return Character.toString((char)13);
            case "NAK":
                return Character.toString((char)21);
            default:
                return "";
        }
    }
    private String Checksum(String message){
        char[] charArr= message.toCharArray();
        Integer length = 0;
        for(int i=0 ; i< charArr.length; i++)
            length += (int) charArr[i];
        length -= 2;
        length %= 256;
        String hex=Integer.toHexString(length).toUpperCase();
        if(hex.length() == 1) {
            hex = (char) 48 + hex;
        }
//        char [] ch= hex.toCharArray();
//        if(ch.length < 2)
//            hex = Character.toString((char)48)+hex;
        return hex;
    }
    public String sendACK(){
        return ch("ACK");
    }
    private void enquiry(){
        writeMessage.add(ch("ENQ"));
    }
    private void header(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();// add Date/Time to header
        buffer = ch("STX") + "1H|^&||DPC|Receiver||||N81|Sender||P|1|" + dtf.format(now) + ch("CR") + ch("ETX");
        writeMessage.add(buffer + Checksum(buffer) + ch("CR") + ch("LF"));
    }
    private void patient(String barcode){
        buffer = ch("STX") + "2P|1|" + barcode + "|||||||||||" + ch("CR") + ch("ETX");
        writeMessage.add(buffer + Checksum(buffer) + ch("CR") + ch("LF"));
    }
    private Integer orders(String barcode){
        DBwork app = new DBwork();
        String testIds = app.selectTestIds(Long.parseLong(barcode));
        int testCount = 3;
        for (String test : testIds.split(",")){
            buffer = ch("STX") + (testCount % 8) + "O|" + (testCount - 2) + "|" + barcode + "||^^^"+ test +"|||||||||" + ch("CR") + ch("ETX");
            writeMessage.add(buffer + Checksum(buffer) + ch("CR") + ch("LF"));
            testCount++;
        }
        testCount %= 8;
        return testCount;
    }
    private void last(Integer testCount){
        buffer = ch("STX")+testCount+"L|1|N"+ch("CR")+ch("ETX");
        writeMessage.add(buffer+Checksum(buffer)+ch("CR")+ch("LF"));
    }
    private void ending(){
        writeMessage.add(ch("EOT"));
    }

//---------------------------------------Result----Result----Result------------------------------
    public void resultParse(List<String> msg, String eqId){
        mapTojson( ListTomap(msg,eqId) ,eqId);
    }
   private HashMap<String,List<HashMap<String,String>>> ListTomap(List<String> list, String eqId) {
       HashMap<String,List<HashMap<String,String>>> map = new HashMap<>();
       String messageBuffer;
       String barcode = "";
       String testId;
       String result;
       for(int i = 0 ; i < list.size() ; i++){
           messageBuffer = list.get(i);
           switch (messageBuffer.charAt(0)){
               case 'O':
                   List<HashMap<String,String>> buffList = new ArrayList<>();
                   barcode = messageBuffer.split("\\|")[2];
                   map.put(barcode,buffList);
                   break;
               case 'R':
                   HashMap<String,String> resultMap = new HashMap<>();
                   String[] resultArray = messageBuffer.split("\\|");
                   testId = resultArray[2].substring(3);
                   result = resultArray[3];
                   resultMap.put("testId",testId);
                   resultMap.put("reTest", "0");
                   resultMap.put("state","urtProcessed");
                   resultMap.put("type","T");
                   resultMap.put("result", result );
                   resultMap.put("equipmentId", eqId );
                   map.get(barcode).add(resultMap);
                   break;
           }
       }
        return map;
   }
   private void mapTojson(HashMap<String,List<HashMap<String,String>>> map, String eqId){
       DBwork dbWorker = new DBwork();
       for (Map.Entry<String,List<HashMap<String,String>>> main : map.entrySet()) {
           JSONObject obj = new JSONObject();
           String barcode = main.getKey();
           obj.put("barcode", barcode);
           if (barcode.length() < 1) {
               barcode = "-1";
           }
           String resultString = dbWorker.selectResult(Long.parseLong(barcode));
           JSONObject finalFinal;
           JSONArray mapResult = new JSONArray();
           JSONArray TestList = new JSONArray();
           if(resultString!=null && resultString.length()>0 && resultString.charAt(0)=='{') {
           finalFinal = new JSONObject(resultString);
           mapResult = finalFinal.getJSONArray("mapResult");
           JSONObject TestListObj = mapResult.getJSONObject(0);
           TestList = TestListObj.getJSONArray("TestList");
           }
           else{
               finalFinal = new JSONObject();
           }
             List<HashMap<String, String>> list = main.getValue();
           for(int j = 0 ; j< list.size() ; j++){
               if (!TestList.equals(null)) {
                   for (int i = 0; i < TestList.length(); i++) {
                       JSONObject eachResultJson = TestList.getJSONObject(i);
                       if (eachResultJson.getString("testId").equals(list.get(j).get("testId"))) {
                            TestList.remove(i);
                           eachResultJson = null;
                       }
                   }
               }
                   JSONObject jobj = new JSONObject();
                   for(Map.Entry<String,String> entry : list.get(j).entrySet())
                      jobj.put(entry.getKey(),entry.getValue());
                   TestList.put(jobj);
            }
           obj.put("TestList",TestList);
           if(mapResult.length()>0)
                mapResult.remove(0);
           mapResult.put(obj);
           finalFinal.put("mapResult", mapResult);
           dbWorker.update(Long.parseLong(main.getKey()),eqId, finalFinal.toString(),"processed");
       }
   }
}