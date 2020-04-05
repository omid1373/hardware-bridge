package tigerworkshop.webapphardwarebridge.parser;

import java.util.ArrayList;
import java.util.List;

public class patient {
    private String seqNum= null; //1
    private String assId= null; //2
    private String labAssId= null; //3
    private String id= null; //4
    private String patientName= null; //5
    private String maidenName= null; //6
    private String birthDay= null; //7
    private String sex= null; //8
    private String race= null; //9
    private String address= null; //10
    private String reserved= null; //11
    private String phone= null; //12
    private String physicianId= null; //13
    private String specialField1= null; //14
    private String specialField2= null; //15
    private String height= null; //16
    private String weight= null; //17
    private String diagnosis= null; //18
    private String medications= null; //19
    private String diet= null; //20
    private String practiceField1= null; //21
    private String practiceField2= null; //22
    private String addDisDate= null; //23
    private String addStatus= null; //24
    private String location= null; //25
    private String natureofDiag= null; //26
    private String altCode= null; //27
    private String religion= null; //28
    private String maritalStatus= null; //29
    private String isolationStatus= null; //30
    private String language= null; //31
    private String hospService= null; //32
    private String hospInstitution= null; //33
    private String dosage= null; //34
    private List<order> orderList = new ArrayList<>();

    public void setOrderList(List<order> list){ this.orderList = list;}
    public void addOrder(order order){ this.orderList.add(order);}
    public List<order> getOrderList(){ return orderList;}
    public String getSeqNum(){ return seqNum;} //1
    public void setSeqNum(String seqNum) { this.seqNum = seqNum; }
    public String getAssId(){ return assId;} //2
    public void setAssId(String assId) { this.assId = assId; }
    public String getLabAssId(){ return labAssId;} //3
    public void setLabAssId(String labAssId) { this.labAssId = labAssId; }
    public String getId(){ return id;} //4
    public void setId(String id) { this.id = id; }
    public String getPatientName(){ return patientName;} //5
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getMaidenName(){ return maidenName;} //6
    public void setMaidenName(String maidenName) { this.maidenName = maidenName; }
    public String getBirthDay(){ return birthDay;} //7
    public void setBirthDay(String birthDay) { this.birthDay = birthDay; }
    public String getSex(){ return sex;} //8
    public void setSex(String sex) { this.sex = sex; }
    public String getRace(){ return race;} //9
    public void setRace(String race) { this.race = race; }
    public String getAddress(){ return address;} //10
    public void setAddress(String address) { this.address = address; }
    public String getReserved(){ return reserved;} //11
    public void setREeserved(String reserved) { this.reserved = reserved; }
    public String getPhone(){ return phone;} //12
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhysicianId(){ return physicianId;} //13
    public void setPhysicianId(String physicianId) { this.physicianId = physicianId; }
    public String getSpecialField1(){ return specialField1;} //14
    public void setSpecialField1(String specialField1) { this.specialField1 = specialField1; }
    public String getSpecialField2(){ return specialField2;} //15
    public void setSpecialField2(String specialField2) { this.specialField2 = specialField2; }
    public String getHeight(){ return height;} //16
    public void setHeight(String height) { this.height = height; }
    public String getWeight(){ return weight;} //17
    public void setWeight(String weight) { this.weight = weight; }
    public String getDiagnosis(){ return diagnosis;} //18
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getMedications(){ return medications;} //19
    public void setMedications(String medications) { this.medications = medications; }
    public String getDiet(){ return diet;} //20
    public void setDiet(String diet) { this.diet = diet; }
    public String getPracticeField1(){ return practiceField1;} //21
    public void setPracticeField1(String practiceField1) { this.practiceField1 = practiceField1; }
    public String getPracticeField2(){ return practiceField2;} //21
    public void setPracticeField2(String practiceField2) { this.practiceField2 = practiceField2; }
    public String getAddDisDate(){ return addDisDate;} //23
    public void setAddDisDate(String addDisDate) { this.addDisDate = addDisDate; }
    public String getAddStatus(){ return addStatus;} //24
    public void setAddStatus(String addStatus) { this.addStatus = addStatus; }
    public String getLocation(){ return location;} //25
    public void setLocation(String location) { this.location = location; }
    public String getNatureofDiag(){ return natureofDiag;} //26
    public void setNatureofDiag(String natureofDiag) { this.natureofDiag = natureofDiag; }
    public String getAltCode(){ return altCode;} //27
    public void setAltCode(String altCode) { this.altCode = altCode; }
    public String getReligion(){ return religion;} //28
    public void setReligion(String religion) { this.religion = religion; }
    public String getMaritalStatus(){ return maritalStatus;} //29
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getIsolationStatus(){ return isolationStatus;} //30
    public void setIsolationStatus(String isolationStatus) { this.isolationStatus = isolationStatus; }
    public String getLanguage(){ return language;} //31
    public void setLanguage(String language) { this.language = language; }
    public String getHospService(){ return hospService;} //32
    public void setHospService(String hospService) { this.hospService = hospService; }
    public String getHospInstitution(){ return hospInstitution;} //33
    public void setHospInstitution(String hospInstitution) { this.hospInstitution = hospInstitution; }
    public String getDosage(){ return dosage;} //34
    public void setDosage(String dosage) { this.dosage = dosage; }
}
