package tigerworkshop.webapphardwarebridge.parser;

import java.util.ArrayList;
import java.util.List;

public class fullMessage {
    private header head;
    private query query;
    private List<patient> patientList = new ArrayList<>();
    private last last;

    public void setHead(header head) {
        this.head = head;
    }
    public void setLast(last last) {
        this.last = last;
    }
    public void setQuery(query query){
        this.query = query;
    }
    public void setPatientList(List<patient> list){ this.patientList = list; }
    public void addPatient(patient patient){
        this.patientList.add(patient);
    }
    public void emptyPatientList(){ this.patientList.clear(); }
    public header getHead(){ return head;}
    public query getQuery(){ return query;}
    public last getLast(){ return last;}
    public List<patient> getPatientList(){ return patientList;}
}