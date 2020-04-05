package tigerworkshop.webapphardwarebridge.parser;

import java.util.ArrayList;
import java.util.List;

public class order {
    private String seqNum= null; //1
    private String specimenId= null; //2
    private String instSpecimenId= null; //3
    private String uniTestId= null; //4
    private String priority= null; //5
    private String dateTime= null; //6
    private String speDateTime= null; //7
    private String endTime= null; //8
    private String volume= null; //9
    private String collectorId= null; //10
    private String actionCode= null; //11
    private String dangerCode= null; //12
    private String clinicInfo= null; //13
    private String dateTimeRecieved= null; //14
    private String descriptor= null; //15
    private String speType= null; //16
    private String physician= null; //17
    private String physicianTel= null; //18
    private String userField1= null; //19
    private String userField2= null; //20
    private String labField1= null; //21
    private String labField2= null; //22
    private String dateTimeResult= null; //23
    private String instCharge= null; //24
    private String instSecId= null; //25
    private String reportType= null; //26
    private String reserved= null; //27
    private String speLocation= null; //28
    private String nosocomial= null; //29
    private String speService= null; //30
    private String speInst= null; //31
    private List<result> resultList = new ArrayList<>();

    public void setResultList(List<result> list){ this.resultList = list;}
    public void addResult(result result){ this.resultList.add(result);}
    public List<result> getResultList(){ return resultList;}
    public String getSeqNum(){ return seqNum;} //1
    public void setSeqNum(String seqNum) { this.seqNum = seqNum; }
    public String getSpecimenId(){ return specimenId;} //2
    public void setSpecimenId(String specimenId) { this.specimenId = specimenId; }
    public String getInstSpecimenId(){ return instSpecimenId;} //3
    public void setInstSpecimenId(String instSpecimenId) { this.instSpecimenId = instSpecimenId; }
    public String getUniTestId(){ return uniTestId;} //4
    public void setUniTestId(String uniTestId) { this.uniTestId = uniTestId; }
    public String getPriority(){ return priority;} //5
    public void setPriority(String priority) { this.priority = priority; }
    public String getDateTime(){ return dateTime;} //6
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public String getSpeDateTime(){ return speDateTime;} //7
    public void setSpeDateTime(String speDateTime) { this.speDateTime = speDateTime; }
    public String getEndTime(){ return endTime;} //8
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getVolume(){ return volume;} //9
    public void setVolume(String volume) { this.volume = volume; }
    public String getCollectorId(){ return collectorId;} //10
    public void setCollectorId(String collectorId) { this.collectorId = collectorId; }
    public String getActionCode(){ return actionCode;} //11
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }
    public String getDangerCode(){ return dangerCode;} //12
    public void setDangerCode(String dangerCode) { this.dangerCode = dangerCode; }
    public String getClinicInfo(){ return clinicInfo;} //13
    public void setClinicInfo(String clinicInfo) { this.clinicInfo = clinicInfo; }
    public String getDateTimeRecieved(){ return dateTimeRecieved;} //14
    public void setDateTimeRecieved(String dateTimeRecieved) { this.dateTimeRecieved = dateTimeRecieved; }
    public String getDescriptor(){ return descriptor;} //15
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }
    public String getSpeType(){ return speType;} //16
    public void setSpeType(String speType) { this.speType = speType; }
    public String getPhysician(){ return physician;} //17
    public void setPhysician(String physician) { this.physician = physician; }
    public String getPhysicianTel(){ return physicianTel;} //18
    public void setPhysicianTel(String physicianTel) { this.physicianTel = physicianTel; }
    public String getUserField1(){ return userField1;} //19
    public void setUserField1(String userField1) { this.userField1 = userField1; }
    public String getUserField2(){ return userField2;} //20
    public void setUserField2(String userField2) { this.userField2 = userField2; }
    public String getLabField1(){ return labField1;} //21
    public void setLabField1(String labField1) { this.labField1 = labField1; }
    public String getLabField2(){ return labField2;} //22
    public void setLabField2(String labField2) { this.labField2 = labField2; }
    public String getDateTimeResult(){ return dateTimeResult;} //23
    public void setDateTimeResult(String dateTimeResult) { this.dateTimeResult = dateTimeResult; }
    public String getInstCharge(){ return instCharge;} //24
    public void setInstCharge(String instCharge) { this.instCharge = instCharge; }
    public String getInstSecId(){ return instSecId;} //25
    public void setInstSecId(String instSecId) { this.instSecId = instSecId; }
    public String getReportType(){ return reportType;} //26
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getReserved(){ return reserved;} //27
    public void setReserved(String reserved) { this.reserved = reserved; }
    public String getSpeLocation(){ return speLocation;} //28
    public void setSpeLocation(String speLocation) { this.speLocation = speLocation; }
    public String getNosocomial(){ return nosocomial;} //29
    public void setNosocomial(String nosocomial) { this.nosocomial = nosocomial; }
    public String getSpeService(){ return speService;} //30
    public void setSpeService(String speService) { this.speService = speService; }
    public String getSpeInst(){ return speInst;} //31
    public void setSpeInst(String speInst) { this.speInst = speInst; }
}
