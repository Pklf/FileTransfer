package FileTransfer;

import GUI.ProgressBar;
import GUI.ReceiverPage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver_Thread {
    public static void startServer(int port, JLabel clientPort) {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    System.out.println(String.format("listening to %s:%d", serverSocket.getInetAddress().toString(), serverSocket.getLocalPort()));
                    clientPort.setText(String.valueOf(serverSocket.getLocalPort()));
                    System.out.println("Waiting Sender send Files...");
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println(String.format("Connected from %s:%d", clientSocket.getLocalAddress().toString(), clientSocket.getPort()));
                        clientProcessingPool.submit(new ClientTask(clientSocket));
                    }
                } catch (IOException e) {
                    System.err.println("Unable to process Sender request");
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    public static class ClientTask implements Runnable {
        private final Socket clientSocket;
        private static ObjectInputStream objectInputStream = null;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("Got a Sender !");
            try {
                // get Files set from Sender
                Set<String> fileSet = receiveFileSet();
                // create receiverPage
                ReceiverPage receiverPage = new ReceiverPage(fileSet, this);
                receiverPage.setTitle("Receiver");
                receiverPage.setContentPane(receiverPage.getMainPage());
                receiverPage.setVisible(true);
                receiverPage.pack();
                // set receiverPage display
                receiverPage.setPeerIP(clientSocket.getInetAddress().toString());
                receiverPage.setPeerPort(Integer.toString(clientSocket.getPort()));
//                System.out.println(fileSet.size());
                receiverPage.getStartReceiveBtn().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            // get Files set that Receiver want
                            Set<String> receiver_want_fileSet = receiverPage.getReceiver_want_fileSet();
                            // get path to save from Receiver
                            String pathToSave = receiverPage.getPathToSave();

                            // set ProgressBar
                            float totol_files_num = (float) receiver_want_fileSet.size();
                            ProgressBar progressBar = new ProgressBar(totol_files_num, 0f);
                            for (int i = 0; i < fileSet.size(); i++) {
                                receiveFile(pathToSave, receiver_want_fileSet);
                                // update ProgressBar value
                                progressBar.updateProgres(totol_files_num, totol_files_num - receiver_want_fileSet.size());
                            }

                            // remind Receiver, he already gets all Files he want
                            int res = JOptionPane.showOptionDialog(progressBar, "You recevice All files from Sender", "Recevicer Message", JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null, null, null);
                            // close JFrame
                            if (res == 0) {
                                progressBar.dispose();
                                receiverPage.dispose();
                            }
                            // close Stream and Socket, Sender will notice it and end its socket too
                            objectInputStream.close();
                            clientSocket.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static Set<String> receiveFileSet() throws Exception {
            return (Set<String>) objectInputStream.readObject();
        }

        public static void receiveFile(String pathToSave, Set<String> receiver_want_fileSet) throws Exception {
            int fileNameSize = objectInputStream.readInt(); // read fileName size
//            System.out.println("fileNameSize->" + fileNameSize);
            if (fileNameSize != -1) { // Income file, (This value can be used for other purpose, such as control what type of file is)
                String fileName = objectInputStream.readUTF();
                System.out.println(fileName);
                boolean inSet = false;
                // Filter files that Receiver want
                for (Iterator<String> iterator = receiver_want_fileSet.iterator(); iterator.hasNext(); ) {
                    String receiver_want_file = iterator.next();
                    if (fileName.indexOf(receiver_want_file) != -1) {
                        inSet = true;
                        // remove the file in set
                        iterator.remove();
                        receiver_want_fileSet.remove(receiver_want_file);

                        fileName = pathToSave + receiver_want_file;
//                        System.out.println("receiver_want_file" + receiver_want_file);
                        System.out.println("fileName before created:" + fileName);

                        File createFile = new File(fileName);
                        createFile.getParentFile().mkdirs();  // create all parent directories
                        createFile.createNewFile(); // create file
                        FileOutputStream fileOutputStream = new FileOutputStream(fileName, false);

                        int bytes = 0;
                        long size = objectInputStream.readLong();     // read file size
                        byte[] buffer = new byte[4 * 1024];
                        while (size > 0 && (bytes = objectInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                            fileOutputStream.write(buffer, 0, bytes);
                            size -= bytes;      // read up to file size
                        }
                        fileOutputStream.close();
                        break;
                    }
                }
                if (!inSet) { // Receiver don't want coming file
                    int bytes = 0;
                    long size = objectInputStream.readLong();     // read file size
                    byte[] buffer = new byte[4 * 1024];
                    while (size > 0 && (bytes = objectInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                        size -= bytes;      // read up to file size
                    }
                }
            }
        }
    }
}
