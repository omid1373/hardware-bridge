package tigerworkshop.webapphardwarebridge.parser;

public class header {
    private String delimiter = null; //1
    private String controlId = null; //2
    private String password = null; //3
    private String senderId = null; //4
    private String SenderAddress = null; //5
    private String reserved = null; //6
    private String senderTel = null; //7
    private String CharfoSender = null; //8
    private String recieverId = null; //9
    private String comment = null; //10
    private String processingId = null; //11
    private String versionNum = null; //12
    private String dateTime= null; //13

  public String getDelimiter(){ return delimiter;}
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }
  public String getControlId(){ return controlId;}
    public void setControlId(String controlId) { this.controlId = controlId; }
  public String getPassword(){ return password;}
    public void setPassword(String password) { this.password = password; }
  public String getSenderId(){ return senderId;}
    public void setSenderId(String senderId) { this.senderId = senderId; }
  public String getSenderAddress(){ return SenderAddress;}
    public void setSenderAddress(String SenderAddress) { this.SenderAddress = SenderAddress; }
  public String getReserved(){ return reserved;}
    public void setReserved(String reserved) { this.reserved = reserved; }
  public String getSenderTel(){ return senderTel;}
    public void setSenderTel(String senderTel) { this.senderTel = senderTel; }
  public String getCharfoSender(){ return CharfoSender;}
    public void setCharfoSender(String CharfoSender) { this.CharfoSender = CharfoSender; }
  public String getRecieverId(){ return recieverId;}
    public void setRecieverId(String recieverId) { this.recieverId = recieverId; }
  public String getComment(){ return comment;}
    public void setComment(String comment) { this.comment = comment; }
  public String getProcessingId(){ return processingId;}
    public void setProcessingId(String processingId) { this.processingId = processingId; }
  public String getVersionNum(){ return versionNum;}
    public void setVersionNum(String versionNum) { this.versionNum = versionNum; }
  public String getDateTime(){ return dateTime;}
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
}
