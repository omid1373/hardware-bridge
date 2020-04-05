package tigerworkshop.webapphardwarebridge;

import jdk.nashorn.internal.parser.JSONParser;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import tigerworkshop.webapphardwarebridge.websocketservices.logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static okhttp3.internal.Internal.logger;

public class DBwork {
    logger logFile;
    private Connection connect() {
        logFile = new logger();
        // SQLite connection string
        String url = "jdbc:sqlite:db/labEquipmentsDB.db";
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection("jdbc:sqlite:db/my-db.sqlite");
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            if(e instanceof SQLException)
                e.printStackTrace();
        }
        return conn;
    }

    public String insert(Long barcode, String deviceName, String tests, String state, String insertDate, Integer reception){
        String insertState = "";
        String sql = "INSERT INTO sampleTubes(barcode,deviceName,tests,result,state,insertDate,reception) VALUES(?,?,?,?,?,?,?)";
        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, barcode);
            pstmt.setString(2, deviceName);
            pstmt.setString(3, tests);
            pstmt.setString(4, "");
            pstmt.setString(5, state);
            pstmt.setString(6, insertDate);
            pstmt.setInt(7, reception);
            pstmt.executeUpdate();
            insertState = "ok";
        } catch (Exception e) {
            String sql2 = "UPDATE sampleTubes " +
                    "SET deviceName = ?, " +
                    "tests = ?, " +
                    "state = ?, " +
                    "insertDate = ?, " +
                    "reception = ? " +
                    "WHERE barcode = ?;";
            try {
                Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql2);
                pstmt.setString(1, deviceName);
                pstmt.setString(2, tests);
                pstmt.setString(3, state);
                pstmt.setString(4, insertDate);
                pstmt.setInt(5, reception);
                pstmt.setLong(6, barcode);
                pstmt.executeUpdate();
                insertState = "ok";
            } catch (Exception e2) {
//                e2.printStackTrace();
                try { logFile.log("dbLogger","(DB Work--insert) Failed to insert/update testIds for "+barcode); } catch(IOException e01){ }
            }
        }
        return insertState;
    }

//    public Boolean insertSysmex2{
//        return true;
//    }

    public Boolean insertSysmex(String response){
        String sql = "INSERT INTO sampleTubes(barcode,deviceName,tests,result,state,insertDate,reception) VALUES(?,?,?,?,?,?,?)";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");
        LocalDateTime now = LocalDateTime.now();
        long millis = System.currentTimeMillis();
        java.sql.Date todayDate = new java.sql.Date(millis);
        try {
            Connection conn = this.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1,Long.parseLong(dtf.format(now)) );
            pstmt.setString(2, "sysmex");
            pstmt.setString(3, null);
            pstmt.setString(4, response);
            pstmt.setString(5, "processed");
            pstmt.setString(6, todayDate.toString());
            pstmt.setInt(7, 0);
            pstmt.executeUpdate();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void update(Long barcode, String deviceName, String result, String state){
        String sql;
        if(barcode < 10000000) { // less than 8 digits
            sql = "UPDATE sampleTubes SET deviceName = ?,"
                    + "result = ? ,"
                    + "state = ? "
                    + "WHERE reception IN (SELECT reception " +
                    "FROM sampleTubes " +
                    " WHERE reception = ? " +
                    " ORDER BY barcode DESC LIMIT 1)";
        }else {
            sql = "UPDATE sampleTubes SET deviceName = ?,"
                    + "result = ? ,"
                    + "state = ? "
                    + "WHERE barcode = ?";
        }
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            // set the corresponding param
            pstmt.setString(1, deviceName);
            pstmt.setString(2, result);
            pstmt.setString(3, state);
            if(barcode < 10000000) {
                int receptionId;
                receptionId = (int) (barcode % 10000000);
                pstmt.setInt(4, receptionId);
            }
            else
            pstmt.setLong(4, barcode);
            // update
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
            try { logFile.log("dbLogger","(DB Work--update) Failed to update result for "+barcode); } catch(IOException e01){ }
        }
    }

    public String updateState(Long barcode, String state){
        String updateBarcodeState = "";
        String sql = "UPDATE sampleTubes SET state = ? "
                + "WHERE barcode = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // set the corresponding param
            pstmt.setString(1, state);
            pstmt.setLong(2, barcode);
            updateBarcodeState = "ok";
            // update
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
//            e.printStackTrace();
      try { logFile.log("dbLogger","(DB Work--updateState) Failed to update State for "+barcode); } catch(IOException e01){ }
        }
        return updateBarcodeState;
    }

    public String selectTestIds(Long barcode){
        String sql = "SELECT tests FROM sampleTubes WHERE barcode =" + barcode;
        String testIds ;
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
            testIds = rs.getString("tests");
            conn.close();
        } catch (SQLException e) {
            e.getSQLState();
            try { logFile.log("dbLogger","(DB Work--selectTestIds) No testId for "+barcode); } catch(IOException e01){ }
            testIds = "";
        }
        return testIds;
    }

    public List<HashMap<String,String>> selectTestIds2(String state){
        String sql = "SELECT * FROM sampleTubes WHERE state = '"+state+"'";
        List<HashMap<String,String>> list = new ArrayList<>();
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                HashMap<String,String> map = new HashMap<>();
                map.put( "testIds",rs.getString("tests"));
                Long barcode = rs.getLong("barcode");
                map.put("barcode",new String(barcode.toString()));
//                System.out.println("DB--->>>"+new String(barcode.toString())+"---"+rs.getString("tests"));
                list.add(map);
            }
            conn.close();
        } catch (SQLException e) {
            try { logFile.log("dbLogger","(DB Work--selectTestIds) No testId with state "+state); } catch(IOException e01){ }
//            e.printStackTrace();
//            e.getSQLState();
//            System.out.println("SelectTestId Error:   No test's!!!");
        }
        return list;
    }

    public List selectBarcodesWithState(String state) {
        String sql = "SELECT barcode FROM sampleTubes WHERE state ='" + state + "'";
        List barcodeList = new ArrayList();
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                barcodeList.add(rs.getString("barcode"));
            }
            conn.close();
        } catch (Exception e) {
//            e.printStackTrace();
       try { logFile.log("dbLogger","(DB Work--selectBarcodesWithState) Failed to select barcode with state: "+state); } catch(IOException e01){ }
        }
        return barcodeList;
    }

    public List<HashMap<String,String>> selectWithStateEq(String state1 , String state2 , @Nullable Integer limit){
        /*String sql = "SELECT * FROM sampleTubes WHERE state = '"+state1+"' OR state = '" + state2 + "'";*/
        String sql;
        if(limit != null) {
            sql = "SELECT * FROM sampleTubes WHERE state = '" + state1 + "' OR state = '" + state2 + "' ORDER BY insertDate DESC LIMIT " + limit;
        }else {
            sql = "SELECT * FROM sampleTubes WHERE state = '" + state1 + "' OR state = '" + state2 + "' ORDER BY insertDate DESC";
        }
        List<HashMap<String,String>> list = new ArrayList();
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                HashMap<String,String> map = new HashMap<>();
                map.put("barcode",rs.getString("barcode"));
                map.put("deviceName",rs.getString("deviceName"));
                map.put("tests",rs.getString("tests"));
                map.put("result",rs.getString("result"));
                map.put("state",rs.getString("state"));
                map.put("insertDate",rs.getString("insertDate"));
                list.add(map);
            }
            conn.close();
        } catch (Exception e) {
//            e.printStackTrace();
            try { logFile.log("dbLogger","(DB Work--selectWithStateEq) Failed to select with statea "+state1 + " & "+state2); } catch(IOException e01){ }
        }
        return list;
    }

    public List selectBarcodesWithDate(String insertDate) {
        String sql = "SELECT barcode FROM sampleTubes WHERE insertDate < date('" + insertDate + "')";
        List barcodeList = new ArrayList();

        try {
            //Class.forName("org.sqlite.JDBC");
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                barcodeList.add(rs.getString("barcode"));
            }
            conn.close();
        } catch (Exception e) {
//            e.printStackTrace();
     try { logFile.log("dbLogger","(DB Work--selectBarcodesWithDate) Failed to select barcode with date "+insertDate); } catch(IOException e01){ }
        }
        return barcodeList;
    }

    public String selectResult(Long barcode) {
        String sql = "SELECT result FROM sampleTubes WHERE barcode =" + barcode;
        String result = "";
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            result = rs.getString("result");
            conn.close();
        } catch (Exception e) {
//            e.printStackTrace();
            try { logFile.log("dbLogger","(DB Work--selectResult) Failed to select result for "+barcode); } catch(IOException e01){ }
        }
        return result;
    }

    public String selectDeviceName(Long barcode) {
        String sql = "SELECT deviceName FROM sampleTubes WHERE barcode =" + barcode;
        String deviceName = "";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            deviceName = rs.getString("deviceName");
            conn.close();
        } catch (SQLException e) {
//            e.printStackTrace();
            try { logFile.log("dbLogger","(DB Work--selectDeviceName) Failed to select device name for "+barcode); } catch(IOException e01){ }
        }
        return deviceName;
    }

    public void deleteSample(Long barcode) {
        String sql = "DELETE FROM sampleTubes WHERE barcode = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setLong(1, barcode);
            // execute the delete statement
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
//            e.printStackTrace();
        try { logFile.log("dbLogger","(DB Work--deleteSample) Failed to delete sample "+barcode); } catch(IOException e01){ }
        }
    }
}