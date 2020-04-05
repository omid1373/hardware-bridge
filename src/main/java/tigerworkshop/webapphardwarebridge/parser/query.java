package tigerworkshop.webapphardwarebridge.parser;

public class query {
    private String seqNum= null; //1
    private String startRange= null; //2  ->Barcode
    private String endRange= null; //3
    private String uniTestId= null; //4
    private String timeLimits= null; //5
    private String beginReqRes= null; //6
    private String endReqRes= null; //7
    private String physician= null; //8
    private String physicianTel= null; //9
    private String userField1= null; //10
    private String userField2= null; //11
    private String reqStatusCode= null; //12

    public String getSeqNum(){ return seqNum;} //1
    public void setSeqNum(String seqNum) { this.seqNum = seqNum; }
    public String getStartRange(){ return startRange;} //2
    public void setStartRange(String startRange) { this.startRange = startRange; }
    public String getEndRange(){ return endRange;} //3
    public void setEndRange(String endRange) { this.endRange = endRange; }
    public String getUniTestId(){ return uniTestId;} //4
    public void setUniTestId(String uniTestId) { this.uniTestId = uniTestId; }
    public String getTimeLimits(){ return timeLimits;} //5
    public void setTimeLimits(String timeLimits) { this.timeLimits = timeLimits; }
    public String getBeginReqRes(){ return beginReqRes;} //6
    public void setBeginReqRes(String beginReqRes) { this.beginReqRes = beginReqRes; }
    public String getEndReqRes(){ return endReqRes;} //7
    public void setEndReqRes(String endReqRes) { this.endReqRes = endReqRes; }
    public String getPhysician(){ return physician;} //8
    public void setPhysician(String physician) { this.physician = physician; }
    public String getPhysicianTel(){ return physicianTel;} //9
    public void setPhysicianTel(String physicianTel) { this.physicianTel = physicianTel; }
    public String getUserField1(){ return userField1;} //10
    public void setUserField1(String userField1) { this.userField1 = userField1; }
    public String getUserField2(){ return userField2;} //11
    public void setUserField2(String userField2) { this.userField2 = userField2; }
    public String getReqStatusCode(){ return reqStatusCode;} //12
    public void setReqStatusCode(String reqStatusCode) { this.reqStatusCode = reqStatusCode; }
}
