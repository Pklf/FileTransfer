package Main;

import GUI.SenderPage;

import javax.swing.*;
import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

public class Main {
    private static Properties prop = new Properties();
    private static OutputStream output;
    private static File propFile;

    public static void main(String args[]) {
        // load Properties file
        loadPropsFile();
        // Create Sender JFrame
        SenderPage senderPage = new SenderPage();
        senderPage.setTitle("Sender");
        senderPage.setContentPane(senderPage.MainPage);
        senderPage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        senderPage.setVisible(true);
        senderPage.pack();
    }

    public static void loadPropsFile() {
        try{
            // Get this file directory
            String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            System.out.println(decodedPath);
            propFile = new File(decodedPath + "config.properties");

            // create prop file if not exists
            propFile.createNewFile();
            InputStream input = new FileInputStream(propFile);
            prop = new Properties();
            // load a properties file
            prop.load(input);
            // if it is a newly created prop file
            if (prop.getProperty("port") == null){
                updatePropsFilePort(0);
                System.out.println(prop);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void updatePropsFilePort(int port) throws IOException {
        output = new FileOutputStream(propFile, false);
        prop.setProperty("port", Integer.toString(port));
        prop.store(output, null);
    }

    public static int getPropsFilePort(){
        return Integer.parseInt(prop.getProperty("port"));
    }

}
