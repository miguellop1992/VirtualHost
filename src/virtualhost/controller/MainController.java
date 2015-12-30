/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtualhost.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import virtualhost.main.Main;

/**
 *
 * @author Administrator
 */
public class MainController implements Initializable {

    @FXML
    private TextField txtUrl;
    @FXML
    private Button btnConect;
    @FXML
    private TextField txtCondo;
    @FXML
    private Text txtMsg;
    private String URL_HOST = "dir.host";
    private File vhost;
    private Properties properties;
    private String DNS = "dns.name";
    private String SUB_DNS = "dns.sub";
    private String indexSearch = "#condo";
    private final File store;
    private Stage stageInfo;

    /**
     *
     */
    public MainController() {
        init();
        //load properties 
        properties = new Properties();
        store = new File(System.getProperty("user.dir") + File.separator + "host.conf");
        boolean exist = store.exists();
        try {
            if (!exist) {
                store.createNewFile();
            }
            properties.load(new FileReader(store));
            //if does not exist,we create and fill
            if (!exist) {
                properties.put(URL_HOST, "C:/WINDOWS/system32/drivers/etc/hosts");
                properties.put(DNS, "");
                properties.put(SUB_DNS, "");
                store();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //load the file and fill the field
        vhost = new File(properties.getProperty(URL_HOST));
        txtUrl.setText(properties.getProperty(DNS));
        txtCondo.setText(properties.getProperty(SUB_DNS));
        //make a backup
        backup();
    }

    @FXML
    private void onActionBtnConect(ActionEvent event) throws IOException {
        try {//get data
            String dns = txtUrl.getText();
            String subDns = txtCondo.getText();
            //do ping to DNS
            InetAddress a = InetAddress.getByName(dns);
            //if ip address is different to null, we gonna write ip,dns and sub-dns
            if (a.getHostAddress() != null) {
                String line = a.getHostAddress() + " " + (subDns != null && !subDns.isEmpty() ? subDns + "." : "") + dns + " " + indexSearch;
                if (write(line)) {
                    properties.setProperty(DNS, dns);
                    if(subDns != null && !subDns.isEmpty())
                        properties.setProperty(SUB_DNS, subDns);
                    store();
                }
                txtMsg.setFill(Color.GREEN);
                txtMsg.setText("Conexion Exitosa ");
            }
        }  catch (FileNotFoundException ex) {
            txtMsg.setFill(Color.RED);
            txtMsg.setText("Ejecute la Aplicacion como administrador ");
        } catch (java.net.UnknownHostException ex) {
            txtMsg.setFill(Color.RED);
            txtMsg.setText("Conexion Fallida ");
        }

    }
    @FXML
    private void onActionBtnInfo(ActionEvent event) throws IOException {
        stageInfo.show();
    }
    
    private boolean write(String dns) throws IOException {
        String lastFile = readFile();
        FileWriter w = new FileWriter(vhost);
        BufferedWriter bw = new BufferedWriter(w);
        bw.append(lastFile + dns);
        bw.close();
        w.close();
        return true;
    }

    @SuppressWarnings("empty-statement")
    private String readFile() {
        StringBuilder l = new StringBuilder();
        try (FileReader fr = new FileReader(vhost); BufferedReader br = new BufferedReader(fr)) {
            br.lines()
                    .filter((String t) -> !t.contains(indexSearch))
                    .forEach((String line) -> {
                        l.append(line).append("\n");
                    });
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return l.toString();
    }

    private void store() throws IOException {
       properties.store(new FileWriter(store), new Date().toString());
    }
    
    
    /**
     *  Make a backup of the origin host
     */
    private void backup() {
        try {
            File backup = new File(properties.getProperty(URL_HOST)+ ".sample");
            if (!backup.exists()) {
                backup.createNewFile();
            }
            String lastFile = readFile();
            FileWriter w = new FileWriter(backup);
            BufferedWriter bw = new BufferedWriter(w);
            bw.append(lastFile);
            bw.close();
            w.close();
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void init() {
        try {
            stageInfo = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/virtualhost/view/info.fxml"));
            Scene scene = new Scene(root);
            stageInfo.getIcons().addAll(Main.ICON);
            stageInfo.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
