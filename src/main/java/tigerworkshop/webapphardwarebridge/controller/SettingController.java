package tigerworkshop.webapphardwarebridge.controller;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tigerworkshop.webapphardwarebridge.GUI;
import tigerworkshop.webapphardwarebridge.Server;
import tigerworkshop.webapphardwarebridge.responses.Setting;
import tigerworkshop.webapphardwarebridge.services.SettingService;
import tigerworkshop.webapphardwarebridge.utils.ObservableStringPair;

import javax.imageio.ImageIO;
import javax.print.PrintService;
import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static com.sun.javafx.scene.control.skin.Utils.getResource;
import static okhttp3.internal.Internal.logger;

@SuppressWarnings("Duplicates")
public class SettingController implements Initializable {
    private static SettingController instance = new SettingController();
    public boolean[] devices=new boolean[7];

    public static SettingController getInstance() {
        return instance;
    }

    public String authToken = null;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private HBox signin;
    @FXML
    private HBox signout;
    @FXML
    private TableView<DeviceTable> deviceID;
    @FXML
    private TableColumn<DeviceTable  , String> name;
    @FXML
    private TableColumn<DeviceTable  , String> port;
    @FXML
    private TableColumn<DeviceTable  , String> path;
    @FXML
    private TableColumn<DeviceTable  , String> source;
    @FXML
    private TableColumn<DeviceTable  , String> equipmentId;

    @FXML
    private TitledPane hide1;
    @FXML
    private TitledPane hide2;

    @FXML
    private Label signoutText;
    @FXML
    private Label device01;
    @FXML
    private Label device02;
    @FXML
    private Label device03;
    @FXML
    private Label device04;
    @FXML
    private Label device05;
    @FXML
    private Label device06;
    @FXML
    private Label device07;
    @FXML
    private TextField textBind;
    @FXML
    private TextField textAddress;
    @FXML
    private TextField textPort;

    @FXML
    private CheckBox checkboxCloudProxyEnabled;
    @FXML
    private TextField textCloudProxyUrl;
    @FXML
    private TextField textCloudProxyTimeout;

    @FXML
    private CheckBox checkboxTlsEnabled;
    @FXML
    private CheckBox checkboxTLSSelfSigned;
    @FXML
    private TextField textTLSCert;
    @FXML
    private TextField textTLSKey;
    @FXML
    private TextField textTLSCaBundle;

    @FXML
    private CheckBox checkboxAuthenticationEnabled;
    @FXML
    private TextField textAuthenticationToken;

    @FXML
    private TableView<ObservableStringPair> tableSerial;
    @FXML
    private TableColumn<ObservableStringPair, String> columnSerialType;
    @FXML
    private TableColumn<ObservableStringPair, String> columnPort;

    @FXML
    private TableView<ObservableStringPair> tablePrinter;
    @FXML
    private TableColumn<ObservableStringPair, String> columnPrintType;
    @FXML
    private TableColumn<ObservableStringPair, String> columnPrinter;
    @FXML
    private CheckBox checkboxFallbackToDefaultPrinter;

    @FXML
    private Button buttonLog;
    //    @FXML
//    private Button buttonSave;
    @FXML
    private Button buttonSaveAndClose;
    @FXML
    private Button buttonLoadDefault;
    @FXML
    private Button buttonReset;
    @FXML
    private Button login;
    @FXML
    private Button logout;

    private ObservableList<ObservableStringPair> printerMappingList = FXCollections.observableArrayList();
    private ObservableList<ObservableStringPair> serialMappingList = FXCollections.observableArrayList();

    private SettingService settingService = SettingService.getInstance();
    private Server server=Server.getInstance();
    private HashMap<Integer,String > devicesname=new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        buttonSave.setVisible(false);

        deviceID.setPlaceholder(new Label("Not signed-in or no device is connected..."));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        port.setCellValueFactory(new PropertyValueFactory<>("port"));
        port.setCellFactory(TextFieldTableCell.forTableColumn());
        path.setCellValueFactory(new PropertyValueFactory<>("path"));
        path.setCellFactory(TextFieldTableCell.forTableColumn());
        source.setCellValueFactory(new PropertyValueFactory<>("source"));
        source.setCellFactory(TextFieldTableCell.forTableColumn());
        equipmentId.setCellValueFactory(new PropertyValueFactory<>("equipmentId"));
        equipmentId.setCellFactory(TextFieldTableCell.forTableColumn());
        try {
            Integer num;
            devicesname = settingService.getDevicesName();

            for (HashMap.Entry<Integer, String> entry : devicesname.entrySet()){
                num=entry.getKey();
                String DEVname = entry.getValue();
//                logger.info("........................Device name:   "+DEVname);
                switch (num){
                    case 1: device01.setText(DEVname);  device01.setVisible(true);  break;
                    case 2: device02.setText(DEVname);  device02.setVisible(true);  break;
                    case 3: device03.setText(DEVname);  device03.setVisible(true);   break;
                    case 4: device04.setText(DEVname);  device04.setVisible(true);   break;
                    case 5: device05.setText(DEVname);  device05.setVisible(true);   break;
                    case 6: device06.setText(DEVname);  device06.setVisible(true);   break;
                    case 7: device07.setText(DEVname);  device07.setVisible(true);   break;
                }
                DeviceTable DevTab = new DeviceTable(DEVname);
                DevTab.SetFirst();
                deviceID.getItems().add(DevTab);
            }
        }
        catch(Exception e){e.printStackTrace();}

        // Printer List
        hide1.setVisible(false);
        hide2.setVisible(false);
        if(!server.getIsSigned()) logout();
        else{  signin.setVisible(false);
            signout.setVisible(true);     }
        //initialize active devices list
        GUIconnected(0);
        ObservableList<String> printerList = FXCollections.observableArrayList();
        printerList.addAll(listPrinters());
        tablePrinter.getSelectionModel().setCellSelectionEnabled(true);
        columnPrintType.setCellValueFactory(new PropertyValueFactory<>("left"));
        columnPrintType.setCellFactory(TextFieldTableCell.forTableColumn());
        columnPrinter.setCellValueFactory(new PropertyValueFactory<>("right"));
        columnPrinter.setCellFactory(ComboBoxTableCell.forTableColumn(printerList));

        MenuItem addItemPrinter = new MenuItem("Add");
        addItemPrinter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                printerMappingList.add(ObservableStringPair.of("KEY", "Select Printer"));
            }
        });
        tablePrinter.setRowFactory(
                new Callback<TableView<ObservableStringPair>, TableRow<ObservableStringPair>>() {
                    @Override
                    public TableRow<ObservableStringPair> call(TableView<ObservableStringPair> tableView) {
                        final TableRow<ObservableStringPair> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();

                        MenuItem removeItemPrinter = new MenuItem("Delete");
                        removeItemPrinter.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                tablePrinter.getItems().remove(row.getItem());
                            }
                        });
                        rowMenu.getItems().addAll(addItemPrinter, removeItemPrinter);

                        final ContextMenu emptyMenu = new ContextMenu();
                        emptyMenu.getItems().addAll(addItemPrinter);

                        // only display context menu for non-null items:
                        row.contextMenuProperty().bind(
                                Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                        .then(rowMenu)
                                        .otherwise(emptyMenu));
                        return row;
                    }
                }
        );

        tablePrinter.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                ContextMenu contextMenu = new ContextMenu();
                contextMenu.getItems().add(addItemPrinter);
                if (t.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(tableSerial, t.getScreenX(), t.getScreenY());
                }
            }
        });
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DeviceTable, String>>() {
                    @Override public void handle(TableColumn.CellEditEvent<DeviceTable, String> t) {
                        ((DeviceTable)t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setName(t.getNewValue());
                    }
                });
        port.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DeviceTable, String>>() {
                    @Override public void handle(TableColumn.CellEditEvent<DeviceTable, String> t) {
                        ((DeviceTable)t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setPort(t.getNewValue());
                    }
                });
        path.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DeviceTable, String>>() {
                    @Override public void handle(TableColumn.CellEditEvent<DeviceTable, String> t) {
                        ((DeviceTable)t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setPath(t.getNewValue());
                    }
                });
        source.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DeviceTable, String>>() {
                    @Override public void handle(TableColumn.CellEditEvent<DeviceTable, String> t) {
                        ((DeviceTable)t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setSource(t.getNewValue());
                    }
                });
        equipmentId.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DeviceTable, String>>() {
                    @Override public void handle(TableColumn.CellEditEvent<DeviceTable, String> t) {
                        ((DeviceTable)t.getTableView().getItems().get(
                                t.getTablePosition().getRow())).setEquipmentId(t.getNewValue());
                    }
                });
        // Serial List
        ObservableList<String> serialList = FXCollections.observableArrayList();
        serialList.addAll(listSerials());

        tableSerial.getSelectionModel().setCellSelectionEnabled(true);
        columnSerialType.setCellValueFactory(new PropertyValueFactory<>("left"));
        columnSerialType.setCellFactory(TextFieldTableCell.forTableColumn());
        columnPort.setCellValueFactory(new PropertyValueFactory<>("right"));
        columnPort.setCellFactory(ComboBoxTableCell.forTableColumn(serialList));

        MenuItem addItemSerial = new MenuItem("Add");
        addItemSerial.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                serialMappingList.add(ObservableStringPair.of("KEY", "Select Port"));
            }
        });
        deviceID.setRowFactory(
                new Callback<TableView<DeviceTable>, TableRow<DeviceTable>>() {
                    @Override
                    public TableRow<DeviceTable> call(TableView<DeviceTable> tableView) {
                        final TableRow<DeviceTable> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();
                        return row;
                    }
                });

        tableSerial.setRowFactory(
                new Callback<TableView<ObservableStringPair>, TableRow<ObservableStringPair>>() {
                    @Override
                    public TableRow<ObservableStringPair> call(TableView<ObservableStringPair> tableView) {
                        final TableRow<ObservableStringPair> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();

                        MenuItem removeItemSerial = new MenuItem("Delete");
                        removeItemSerial.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                tableSerial.getItems().remove(row.getItem());
                            }
                        });
                        rowMenu.getItems().addAll(addItemSerial, removeItemSerial);

                        final ContextMenu emptyMenu = new ContextMenu();
                        emptyMenu.getItems().addAll(addItemSerial);

                        // only display context menu for non-null items:
                        row.contextMenuProperty().bind(
                                Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                        .then(rowMenu)
                                        .otherwise(emptyMenu));
                        return row;
                    }
                }
        );

        tableSerial.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                ContextMenu contextMenu = new ContextMenu();
                contextMenu.getItems().add(addItemSerial);
                if (t.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(tableSerial, t.getScreenX(), t.getScreenY());
                }
            }
        });

        // Other controls
        buttonLog.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Desktop.getDesktop().open(new File("log"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                login();
            }
        });
        logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                logout();
            }
        });
        password.setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode.equals(KeyCode.ENTER))  login();
        });
        username.setOnKeyPressed(ke -> {
            KeyCode keyCode = ke.getCode();
            if (keyCode.equals(KeyCode.ENTER))  login();
        });

        // Save Values
        buttonSaveAndClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{

                    logout();
                    TimeUnit.SECONDS.sleep(2);
                    saveValues();
                    Stage stage = (Stage) buttonSaveAndClose.getScene().getWindow();
                    stage.close();}
                catch(Exception s){s.printStackTrace();}
            }
        });

//        // Save Values
//        buttonSave.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                saveValues();
//            }
//        });

        // Default Values
        buttonLoadDefault.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadDefaultValues();
            }
        });

        // Reset Values
        buttonReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadCurrentValues();
            }
        });

        loadValues();
// GUI Control........
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                try {
                    boolean[] getDev=settingService.getDevice();
                    for (int j = 0; j < 7; j++)
                        if (getDev[j]) GUIconnected(j + 1);
                    if(!server.getIsSigned()) GUIconnected(0);
                }
                catch(Exception c){c.printStackTrace();}}
        },0,2000);
    }

    private void loadCurrentValues() {
        try {
            settingService.loadCurrent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadValues();
    }

    private void loadDefaultValues() {
        try {
            settingService.loadDefault();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadValues();
    }

    private ArrayList<String> listPrinters() {
        ArrayList<String> printerList = new ArrayList<>();
        PrintService[] printServices = PrinterJob.lookupPrintServices();
        for (PrintService printService : printServices) {
            printerList.add(printService.getName());
        }
        return printerList;
    }

    private ArrayList<String> listSerials() {
        ArrayList<String> portList = new ArrayList<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            portList.add(port.getSystemPortName());
        }
        return portList;
    }

    private void loadValues() {
        Setting setting = settingService.getSetting();

        // General
        textBind.setText(setting.getBind());
        textAddress.setText(setting.getAddress());
        textPort.setText(Integer.toString(setting.getPort()));

        // Cloud Proxy
        checkboxCloudProxyEnabled.setSelected(setting.getCloudProxyEnabled());
        textCloudProxyUrl.setText(setting.getCloudProxyUrl());
        textCloudProxyTimeout.setText(Double.toString(setting.getCloudProxyTimeout()));

        // TLS
        checkboxTlsEnabled.setSelected(setting.getTLSEnabled());
        checkboxTLSSelfSigned.setSelected(setting.getTLSSelfSigned());
        textTLSCert.setText(setting.getTLSCert());
        textTLSKey.setText(setting.getTLSKey());
        textTLSCaBundle.setText(setting.getTLSCaBundle());

        // Authentication
        checkboxAuthenticationEnabled.setSelected(setting.getAuthenticationEnabled());
        textAuthenticationToken.setText(setting.getAuthenticationToken());

        // Printers
        printerMappingList.clear();
        HashMap<String, String> printerHashMap = setting.getPrinters();
        for (Map.Entry<String, String> mapEntry : printerHashMap.entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            printerMappingList.add(ObservableStringPair.of(key, value));
        }
        tablePrinter.setItems(printerMappingList);
        checkboxFallbackToDefaultPrinter.setSelected(setting.getFallbackToDefaultPrinter());

        // Serials
        serialMappingList.clear();
        HashMap<String, String> portHashMap = setting.getSerials();
        for (Map.Entry<String, String> mapEntry : portHashMap.entrySet()) {
            String key = mapEntry.getKey();
            String value = mapEntry.getValue();
            serialMappingList.add(ObservableStringPair.of(key, value));
        }
        tableSerial.setItems(serialMappingList);
    }

    private void saveValues(){
        Setting setting = settingService.getSetting();
        // General
        setting.setAddress(textAddress.getText());
        setting.setBind(textBind.getText());
        setting.setPort(Integer.parseInt(textPort.getText()));

        // Cloud Proxy
        setting.setCloudProxyEnabled(checkboxCloudProxyEnabled.isSelected());
        setting.setCloudProxyUrl(textCloudProxyUrl.getText());
        setting.setCloudProxyTimeout(Double.parseDouble(textCloudProxyTimeout.getText()));

        // TLS
        setting.setTLSEnabled(checkboxTlsEnabled.isSelected());
        setting.setTLSSelfSigned(checkboxTLSSelfSigned.isSelected());
        setting.setTLSCert(textTLSCert.getText());
        setting.setTLSKey(textTLSKey.getText());
        setting.setTLSCaBundle(textTLSCaBundle.getText());

        // Authentication
        setting.setAuthenticationEnabled(checkboxAuthenticationEnabled.isSelected());
        setting.setAuthenticationToken(textAuthenticationToken.getText());

        // Printers
        HashMap<String, String> printerHashMap = new HashMap<>();
        ObservableList<ObservableStringPair> printerList = tablePrinter.getItems();
        for (ObservableStringPair pair : printerList) {
            printerHashMap.put(pair.getLeft(), pair.getRight());
        }
        setting.setPrinters(printerHashMap);
        setting.setFallbackToDefaultPrinter(checkboxFallbackToDefaultPrinter.isSelected());

        // Serials
        HashMap<String, String> serialHashMap = new HashMap<>();
        ObservableList<ObservableStringPair> serialList = tableSerial.getItems();
        for (ObservableStringPair pair : serialList) {
            serialHashMap.put(pair.getLeft(), pair.getRight());
        }
        setting.setSerials(serialHashMap);
        //devices
        List<String> eqIds = new ArrayList<>();
        ArrayList<HashMap<String,String>> dev = new ArrayList<>();
        try{
            for(int i = 0 ; i< deviceID.getItems().size() ; i++){
                DeviceTable list;
                HashMap<String,String> each=new HashMap<>();
                list = deviceID.getItems().get(i);
                each.put("name",list.getName());
                each.put("port",list.getPort());
                each.put("sourceFolder",list.getSource());
                each.put("path",list.getPath());
                String n = Integer.toString(i+1);
                each.put("deviceId",n );
                each.put("equipmentId",list.getEquipmentId());
                dev.add(each);
                eqIds.add(list.getEquipmentId());
            }
        }
        catch (Exception e){e.printStackTrace();}
        setting.setDevices(dev);
        setting.setEquipmentIds(eqIds);
        settingService.save();
    }
    private void login(){
        try{
            String credential = Credentials.basic(username.getText(), password.getText());
//            String origin = "http://sima.sajayanegar.ir";
            String origin = settingService.getSimaUrl();
            System.out.println("login url: "+origin);
            Request request = new Request.Builder()
                    .url(origin + "/rest/api_key")
                    .addHeader("Accept", "text/plain")
                    .header("Authorization", credential)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                authToken = response.body().string();
                settingService.setAuthToken(authToken);
                server.setAuthToken(authToken);

                signin.setVisible(false);
                signout.setVisible(true);
                signoutText.setStyle("-fx-text-fill: #3BAE3A; -fx-font-size: 15 px;");
//                    dbWorker.updateState(Long.parseLong(barcode), "finished");
                settingService.setIsSigned(true);
                server.setIsSigned(true);
            }
            else {
                JOptionPane.showMessageDialog( null, "Wrong password or Mismatch","Authorization Problem", JOptionPane.PLAIN_MESSAGE );
                password.setText(null);
//                System.out.print(response);
                settingService.setIsSigned(false);
                server.setIsSigned(false);
            }
        }
        catch(Exception e){}
    }

    private void logout(){
        settingService.setAuthToken(null);
        server.setAuthToken(null);
        signin.setVisible(true);
        signout.setVisible(false);
        username.setText(null);
        password.setText(null);
        settingService.setDevice(100);
        GUIconnected(0);
        settingService.setIsSigned(false);
        server.setIsSigned(false);
        for(int i=0; i<7 ;i++) devices[i] = false;
    }

    public void GUIconnected(int deviceId) {
        //red E83F16
        //green 09EE2C
        switch (deviceId) {
            case 0:
                device01.setStyle("-fx-background-color: #E83F16");
                device02.setStyle("-fx-background-color: #E83F16");
                device03.setStyle("-fx-background-color: #E83F16");
                device04.setStyle("-fx-background-color: #E83F16");
                device05.setStyle("-fx-background-color: #E83F16");
                device06.setStyle("-fx-background-color: #E83F16");
                device07.setStyle("-fx-background-color: #E83F16");
                break;
            case 1:
                device01.setStyle("-fx-background-color: #09EE2C");
                break;
            case 2:
                device02.setStyle("-fx-background-color: #09EE2C");
                break;
            case 3:
                device03.setStyle("-fx-background-color: #09EE2C");
                break;
            case 4:
                device04.setStyle("-fx-background-color: #09EE2C");
                break;
            case 5:
                device05.setStyle("-fx-background-color: #09EE2C");
                break;
            case 6:
                device06.setStyle("-fx-background-color: #09EE2C");
                break;
            case 7:
                device07.setStyle("-fx-background-color: #09EE2C");
                break;
        }}
}

