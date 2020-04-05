package tigerworkshop.webapphardwarebridge;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

public class GUI extends Application {
    private static Logger logger = LoggerFactory.getLogger("GUI");
    private static Server server = new Server();

    public static void main(String args[]) {
        try {
            JUnique.acquireLock(Config.APP_ID);

        } catch (AlreadyLockedException e) {
            logger.error(Config.APP_ID + " already running");
            //System.exit(1);
        }

        // Create tray icon
        try {
            TrayIcon trayIcon = null;
            if (!SystemTray.isSupported()) {
                System.out.println("SystemTray is not supported");
                return;
            }
            final Image image = ImageIO.read(GUI.class.getResource("/logo.png"));

            final PopupMenu popup = new PopupMenu();
            trayIcon = new TrayIcon(image, Config.APP_NAME);
            final SystemTray tray = SystemTray.getSystemTray();

            // Create a pop-up menu components
            MenuItem settingItem = new MenuItem("Configurator");
            settingItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                config();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
//            settingItem.setEnabled(true);

            MenuItem logItem = new MenuItem("Log");
            logItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().open(new File("log"));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            MenuItem restartItem = new MenuItem("Restart");
            restartItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    server.restart();
                }
            });

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    server.stop();
                    System.exit(0);
                }
            });
            //Add components to pop-up menu
            popup.add(settingItem);
            popup.add(logItem);
            popup.addSeparator();
            popup.add(restartItem);
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);

            tray.add(trayIcon);
            trayIcon.displayMessage(Config.APP_NAME, "is running in background!", TrayIcon.MessageType.INFO);
//            config();
        } catch (Exception e) {
            System.out.println("TrayIcon could not be added.");
            e.printStackTrace();
        }
        server.start();
    }
    private static void config() throws Exception{
        Platform.setImplicitExit(false);
        FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/setting.fxml"));
        Stage stage = new Stage();
        Image icon = ImageIO.read(GUI.class.getResource("/logo.png"));
        stage.getIcons().add(new javafx.scene.image.Image("/logo_high.png"));
        stage.setTitle("Sima Lab Hardware");
        stage.setScene(new Scene(loader.load()));
        stage.setResizable(true);
        stage.show();
        stage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                server.restart();
            }
        });
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
//        logger.info("---------------------------------------GUI Start");
//       config();
    }
}
