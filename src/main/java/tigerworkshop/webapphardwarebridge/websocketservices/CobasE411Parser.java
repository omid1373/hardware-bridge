package tigerworkshop.webapphardwarebridge.websocketservices;

import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CobasE411Parser {
    private List<String> writeMessage;
    private String buffer = null;
    private String barcodeBuffer = null;
    private List<String> barcodes;

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
        }
        return "";
    }
    private String Checksum(String message){
        char[] charArr= message.toCharArray();
        Integer length=0;
        for(int i=0; i< charArr.length; i++)
            length += (int) charArr[i];
        length -= 2;
        length %= 256;
        String hex=Integer.toHexString(length).toUpperCase();
        char [] ch=hex.toCharArray();
        if(ch.length < 2)
            hex = Character.toString((char)48)+hex;
        return hex;
    }
    public String sendACK(){
        return ch("ACK");
    }
    //---------------------------------------Result----Result----Result------------------------------
    public void resultParse(List<String> msg ,String eqId){
        barcodes = new ArrayList<>();
        mapTojson( ListTomap(msg ,eqId) , eqId);
    }
    private HashMap<String,List<HashMap<String,String>>> ListTomap(List<String> list ,String eqId){
        HashMap<String,List<HashMap<String,String>>> map = new HashMap<>();
        String messageBuffer;
        String barcode = "";
        String testId;
        String result;
        String[] resultBuffer;
        for(int i = 0 ; i < list.size() ; i++){
            messageBuffer = list.get(i);
            switch (messageBuffer.charAt(1)){
                case 'O':
                    List<HashMap<String,String>> buffList = new ArrayList<>();
                    barcode = messageBuffer.split("\\|")[2];
                    map.put(barcode,buffList);
                    break;
                case 'R':
//                    System.out.println("res: "+messageBuffer);
                    HashMap<String,String> resultMap = new HashMap<>();
                    String[] resultArray = messageBuffer.split("\\|");
//                    String testIdBufferr = resultArray[2].split("\\^")[3];
//                    testId = testIdBufferr.split("\\/")[0];
                    testId = resultArray[2].split("\\^")[3];
                    resultBuffer = resultArray[3].split("\\^");
                    // Qualitative and Quantitative results:
                    if(resultBuffer.length > 1 && resultBuffer[1].length()>1) {
                        result = resultBuffer[0] + "-" + resultBuffer[1];
                    }
                    else{
                        result = resultBuffer[0];
                    }
                    resultMap.put("testId",testId);
                    resultMap.put("reTest", "0");
                    resultMap.put("state","urtProcessed");
                    resultMap.put("type","T");
                    resultMap.put("result",result);
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
//               }
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