package FileTransfer;

import GUI.ProgressBar;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Sender_Thread {

    public static void startServer(String ip, int port, Set<String> selected_file_set) {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(String.valueOf(ip), port);
                    System.out.println(String.format("Connect to port: %s:%d", ip, socket.getLocalPort()));
                    clientProcessingPool.submit(new ClientTask(socket, selected_file_set));
                } catch (IOException e) {
                    System.err.println("Unable to process Sender request");
                    JOptionPane.showMessageDialog(null, "Unable to process Sender request", "Sender Message", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    public static class ClientTask implements Runnable {
        private final Socket clientSocket;
        private static DataInputStream dataInputStream = null;
        private static ObjectOutputStream objectOutputStream = null;

        private Set<String> selected_file_set = new HashSet<>();

        private ClientTask(Socket clientSocket, Set<String> selected_file_set) {
            this.clientSocket = clientSocket;
            try {
                this.dataInputStream = new DataInputStream(clientSocket.getInputStream());
                this.objectOutputStream = new ObjectOutputStream((clientSocket.getOutputStream()));
                this.selected_file_set = selected_file_set;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            System.out.println("Start Send File !");
            try {
                // send Files set to Receiver
                sendFileSet(selected_file_set);
                // set ProgressBar
                float totol_files_num = (float) selected_file_set.size();
                float sent_files_num = 0f;
                ProgressBar progressBar = new ProgressBar(totol_files_num, sent_files_num);
                for (String file : selected_file_set) {
                    sendFile(file);
                    sent_files_num += 1;
                    // update ProgressBar value
                    progressBar.updateProgres(totol_files_num, sent_files_num);
                }

                // remind Sender, he already sends all Files
                int res = JOptionPane.showOptionDialog(progressBar, "You sended all the files to recevicer. Now wait for recevicer recevice", "Sender Message", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, null, null);
                // close JFrame
                if (res == 0){
                    progressBar.dispose();
                }

                while (dataInputStream.read() != -1){
                    // Receiver not yet disconnect
                }

                // remind Sender, Receiver disconnect
                JOptionPane.showMessageDialog(null, "Recevicer receviced All files from You", "Sender Message", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void sendFileSet(Set<String> selected_file_set) throws Exception {
            objectOutputStream.writeObject(selected_file_set);
        }

        private static void sendFile(String path) throws Exception {
            File file = new File(path);
            if (file.isFile()) {
                String fileName = file.getAbsolutePath();
                System.out.println("sendfile->" + fileName);
                FileInputStream fileInputStream = new FileInputStream(file);

                // send pathname size
                objectOutputStream.writeInt(fileName.length());
                // send pathname
                objectOutputStream.writeUTF(fileName);
                // send file size
                objectOutputStream.writeLong(file.length());
                // break file into chunks
                int bytes = 0;
                byte[] buffer = new byte[4 * 1024];
                while ((bytes = fileInputStream.read(buffer)) != -1) {
                    objectOutputStream.write(buffer, 0, bytes);
                    objectOutputStream.flush();
                }
                fileInputStream.close();
            } else {
                // This is double confirm, the set should not contain non-file path
                // send pathname size
                objectOutputStream.writeInt(-1);
            }
        }
    }
}
