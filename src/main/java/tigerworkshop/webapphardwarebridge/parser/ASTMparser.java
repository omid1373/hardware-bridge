package tigerworkshop.webapphardwarebridge.parser;


import com.fazecast.jSerialComm.SerialPort;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.json.JSONArray;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.DBwork;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ASTMparser {
    private List<String> writeMessage;
    private String buffer = null;
    private List<String> message = new ArrayList<>();
    private fullMessage msg;
    private patient p;
    private order o;
    private result r;
    public ASTMparser(){
        // Blah Blah
    }
    public Boolean serialReady(SerialPort serialPort){
        Boolean isOk;
        switch (serialPort.bytesAvailable()){
            case 0:  //  idle state
                isOk = false;
                break;
            case -1:
                isOk = false;
                System.out.println("Serial unplugged!");
                break;
            default: isOk = true;
        }
        return  isOk;
    }
    public void parseMessage(String message){
        message.replaceAll("\2",""); // STX
        message.replaceAll("\3",""); // ETX
        message.replaceAll("\4",""); // EOT
        message.replaceAll("\5",""); // ENQ
        message.replaceAll("\6",""); // ACK
        message.replaceAll("\10",""); // LF
        message.replaceAll("\21",""); // NAK
        String[] msgArray = message.split("\\r"); // split by CR
        for(int i = 0; i< msgArray.length ; i++){
            buffer = msgArray[i];
            if((int)buffer.charAt(0) < 65)  buffer = buffer.substring(1);
            msg = new fullMessage();
            switch (buffer.charAt(0)){
                case 'H':
                    addHead(buffer);
                    break;
                case 'Q':
                    addQuery(buffer);
                    break;
                case 'P':
                    addPatient(buffer);
                    break;
                case 'O':
                    addOrder(buffer);
                    break;
                case 'R':
                    addResult(buffer);
                    break;
                case 'L':
                    addLast(buffer);
                    break;
            }
        }
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
    private header createHeader(String st){
        header header = new header();
        String[] headerArray = st.split("\\|");
        header.setDelimiter(headerArray[1]);
        header.setControlId(headerArray[2]);
        header.setPassword(headerArray[3]);
        header.setSenderId(headerArray[4]);
        header.setSenderAddress(headerArray[5]);
        header.setReserved(headerArray[6]);
        header.setSenderTel(headerArray[7]);
        header.setCharfoSender(headerArray[8]);
        header.setRecieverId(headerArray[9]);
        header.setComment(headerArray[10]);
        header.setProcessingId(headerArray[11]);
        header.setVersionNum(headerArray[12]);
        header.setDateTime(headerArray[13]);
        return header;
    }
    private query createQuery(String st){
        query query = new query();
        String[] queryArray = st.split("\\|");
        query.setSeqNum(queryArray[1]);
        query.setStartRange(queryArray[2]);
        query.setEndRange(queryArray[3]);
        query.setUniTestId(queryArray[4]);
        query.setTimeLimits(queryArray[5]);
        query.setBeginReqRes(queryArray[6]);
        query.setEndReqRes(queryArray[7]);
        query.setPhysician(queryArray[8]);
        query.setPhysicianTel(queryArray[9]);
        query.setUserField1(queryArray[10]);
        query.setUserField2(queryArray[11]);
        query.setReqStatusCode(queryArray[12]);
        return query;
    }
    private patient createPatient(String st){
        patient patient = new patient();
        String[] patientArray = st.split("\\|");
        patient.setSeqNum(patientArray[1]);
        patient.setAssId(patientArray[2]);
        patient.setLabAssId(patientArray[3]);
        patient.setId(patientArray[4]);
        patient.setPatientName(patientArray[5]);
        patient.setMaidenName(patientArray[6]);
        patient.setBirthDay(patientArray[7]);
        patient.setSex(patientArray[8]);
        patient.setRace(patientArray[9]);
        patient.setAddress(patientArray[10]);
        patient.setREeserved(patientArray[11]);
        patient.setPhone(patientArray[12]);
        patient.setPhysicianId(patientArray[13]);
        patient.setSpecialField1(patientArray[14]);
        patient.setSpecialField2(patientArray[15]);
        patient.setHeight(patientArray[16]);
        patient.setWeight(patientArray[17]);
        patient.setDiagnosis(patientArray[18]);
        patient.setMedications(patientArray[19]);
        patient.setDiet(patientArray[20]);
        patient.setPracticeField1(patientArray[21]);
        patient.setPracticeField2(patientArray[22]);
        patient.setAddDisDate(patientArray[23]);
        patient.setAddStatus(patientArray[24]);
        patient.setLocation(patientArray[25]);
        patient.setNatureofDiag(patientArray[26]);
        patient.setAltCode(patientArray[27]);
        patient.setReligion(patientArray[28]);
        patient.setMaritalStatus(patientArray[29]);
        patient.setIsolationStatus(patientArray[30]);
        patient.setLanguage(patientArray[31]);
        patient.setHospService(patientArray[32]);
        patient.setHospInstitution(patientArray[33]);
        patient.setDosage(patientArray[34]);
        return patient;
    }
    private order createOrder(String st){
        order order = new order();
        String[] orderArray = st.split("\\|");
        order.setSeqNum(orderArray[1]);
        order.setSpecimenId(orderArray[2]);
        order.setInstSpecimenId(orderArray[3]);
        order.setUniTestId(orderArray[4]);
        order.setPriority(orderArray[5]);
        order.setDateTime(orderArray[6]);
        order.setSpeDateTime(orderArray[7]);
        order.setEndTime(orderArray[8]);
        order.setVolume(orderArray[9]);
        order.setCollectorId(orderArray[10]);
        order.setActionCode(orderArray[11]);
        order.setDangerCode(orderArray[12]);
        order.setClinicInfo(orderArray[13]);
        order.setDateTimeRecieved(orderArray[14]);
        order.setDescriptor(orderArray[15]);
        order.setSpeType(orderArray[16]);
        order.setPhysician(orderArray[17]);
        order.setPhysicianTel(orderArray[18]);
        order.setUserField1(orderArray[19]);
        order.setUserField2(orderArray[20]);
        order.setLabField1(orderArray[21]);
        order.setLabField2(orderArray[22]);
        order.setDateTimeResult(orderArray[23]);
        order.setInstCharge(orderArray[24]);
        order.setInstSecId(orderArray[25]);
        order.setReportType(orderArray[26]);
        order.setReserved(orderArray[27]);
        order.setSpeLocation(orderArray[28]);
        order.setNosocomial(orderArray[29]);
        order.setSpeService(orderArray[30]);
        order.setSpeInst(orderArray[31]);
        return order;
    }
    private result createResult(String st){
        result result = new result();
        String[] resultArray = st.split("\\|");
        result.setSeqNum(resultArray[1]);
        result.setUniTestId(resultArray[2]);
        result.setResult(resultArray[3]);
        result.setUnit(resultArray[4]);
        result.setRefRange(resultArray[5]);
        result.setAbnormality(resultArray[6]);
        result.setNatureofAb(resultArray[7]);
        result.setResultStatus(resultArray[8]);
        result.setUnitChangeDate(resultArray[9]);
        result.setOperator(resultArray[10]);
        result.setStartedDateTime(resultArray[11]);
        result.setCompletedDateTime(resultArray[12]);
        result.setInstId(resultArray[13]);
        return result;
    }
    private last createLast(String st){
        last last = new last();
        String[] lastArray = st.split("\\|");
        last.setSeqNum(lastArray[1]);
        last.setTerminationCode(lastArray[2]);
    return last;
    }
    private void addHead(String st){
        msg.setHead(createHeader(st));
    }
    private void addQuery(String st){
        msg.setQuery(createQuery(st));
    }
    private void addPatient(String st){
        if (p != null){
            o.addResult(r);
            p.addOrder(o);
            msg.addPatient(p);
        }
        p = createPatient(st);
    }
    private void addOrder(String st){
        if (o != null){
            o.addResult(r);
            p.addOrder(o);
        }
        o = createOrder(st);
    }
    private void addResult(String st){
        if (r != null){
            o.addResult(r);
        }
        r = createResult(st);
    }
    private void addLast(String st){
        if (p != null){
            o.addResult(r);
            p.addOrder(o);
            msg.addPatient(p);
        }
        msg.setLast(createLast(st));
    }
}