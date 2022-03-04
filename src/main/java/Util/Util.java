package Util;

import GUI.SenderPage;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class Util {

    static int nthLastIndexOf(int nth, String ch, String string) {
        if (nth <= 0) return string.length();
        return nthLastIndexOf(--nth, ch, string.substring(0, string.lastIndexOf(ch)));
    }

    public static String getPathWithoutLast(String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        int last = nthLastIndexOf(endsWithSlash ? 2: 1,File.separator,fileOrDirPath);
        return fileOrDirPath.substring(0,last);
    }

    public static String getParentPathAndFile(String fileOrDirPath, String parent) {
        return fileOrDirPath.substring(parent.length(),fileOrDirPath.length());
    }

    public static String getFileExtension(String fileName) {

        final String WINDOWS_FILE_SEPARATOR = "\\";
        final String UNIX_FILE_SEPARATOR = "/";
        final String FILE_EXTENSION = ".";

        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null!");
        }

        String extension = "";

        int indexOfLastExtension = fileName.lastIndexOf(FILE_EXTENSION);

        // check last file separator, windows and unix
        int lastSeparatorPosWindows = fileName.lastIndexOf(WINDOWS_FILE_SEPARATOR);
        int lastSeparatorPosUnix = fileName.lastIndexOf(UNIX_FILE_SEPARATOR);

        // takes the greater of the two values, which mean last file separator
        int indexOflastSeparator = Math.max(lastSeparatorPosWindows, lastSeparatorPosUnix);

        // make sure the file extension appear after the last file separator
        if (indexOfLastExtension > indexOflastSeparator) {
            extension = fileName.substring(indexOfLastExtension + 1);
        }
        return extension;
    }

    public static void restartApplication() throws IOException, URISyntaxException {
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(SenderPage.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        if(currentJar.getName().endsWith(".jar")) {
            // If is jar
            // Build command: java -jar application.jar
            final ArrayList<String> command = new ArrayList<String>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        } else if (currentJar.getName().endsWith(".exe")) {
            // If is exe
            // Build command: application.exe
            final ArrayList<String> command = new ArrayList<String>();
            command.add(currentJar.getPath());

            final ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();
            System.exit(0);
        }
    }

    public static String getIPaddress(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // filter out IPv6 address
                    if (addr instanceof Inet6Address) continue;
                    String ip = addr.getHostAddress();
//                    System.out.println(iface.getDisplayName() + " " + ip);
                    return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return "Error";
    }

    public static void main(String args[]){
        System.out.println(getParentPathAndFile("D:\\AI\\AI\\BERT\\test\\test1\\hw7_data.zip", "D:\\AI"));
        System.out.println(getPathWithoutLast("D:\\AI\\AI\\BERT\\test\\test1\\hw7_data.zip"));
        System.out.println(getFileExtension(""));
    }
}
