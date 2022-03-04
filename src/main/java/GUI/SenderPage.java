package GUI;

import FileTransfer.Receiver_Thread;
import FileTransfer.Sender_Thread;
import GUI.JCheckBoxTree.JCheckBoxTreeMain;
import Main.Main;
import Util.Util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class SenderPage extends JFrame {
    public JPanel MainPage;
    private JPanel clientIPPanel, peerIPPanel, leftToolBar, clientPortPanel;
    private JButton fileChooserButton, refreshList, clearListBtn, removeBtn, startTransferBtn;
    private JTextField peerIPTextField, peerPortTextField;
    private JLabel clientIP, clientPort;
    private JScrollPane selectedFileScroll;
    private JList selectedFileList;
    private DefaultListModel listModel = new DefaultListModel();
    private SenderPage senderPage;
    private JMenu connectMenu, aboutMenu;
    private JMenuItem portItem, aboutItem;
    private JMenuBar menuBar;

    private Set<String> selected_file_set = new HashSet<>();
    private Set<String> selected_directory_set = new HashSet<>();
    private Set<String> selected_file_without_parent_path_set = new HashSet<>();

    private String ip;
    private int port;

    public void createJMenu() {
        menuBar = new JMenuBar();
        connectMenu = new JMenu("Connect Setting");
        aboutMenu = new JMenu("About");
        aboutItem = new JMenuItem("About");
        portItem = new JMenuItem("Set Your Port (Will restart application)");
        connectMenu.add(portItem);
        aboutMenu.add(aboutItem);
        menuBar.add(connectMenu);
        menuBar.add(aboutMenu);

        portItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String input;
                String string = "";
                do {
                    input = JOptionPane.showInputDialog("Enter Your Port Number: ");
                    if (input.matches("^[0-9]*$")) {
                        string = input;
                    } else {
                        JOptionPane.showMessageDialog(null, "Please input integer only", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                } while (!input.matches("^[0-9]*$"));
                port = Integer.parseInt(string);
                try {
                    Main.updatePropsFilePort(port);

                    // Restart program after change Recevier Port
                    Util.restartApplication();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JOptionPane.showMessageDialog(null, "Code by Cheng Wong Kwan \n Free to use", "About This program", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public SenderPage(){
        // create JMenu
        createJMenu();
        setJMenuBar(menuBar);
        // Get local IP address IPV4
        ip = Util.getIPaddress();
        // Set JFrame display
        clientIP.setText(ip);
        port = Main.getPropsFilePort();
        // Start Receive (Allow listen other Sender's request)
        Receiver_Thread.startServer(port, clientPort);

        fileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected_directory_set.clear();
                FileChooser.fileChooser("C:", senderPage, selected_directory_set, selected_file_without_parent_path_set);
                for (String p : selected_directory_set) {
                    JCheckBoxTreeMain jCheckBoxTreeMain = new JCheckBoxTreeMain(p);
                    jCheckBoxTreeMain.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            selected_file_set.addAll(jCheckBoxTreeMain.getTemp_selected_file_set());
                            selected_file_without_parent_path_set.addAll(jCheckBoxTreeMain.getTemp_selected_file_without_parent_path_set());
                            freshJlist();
                        }
                    });
                    jCheckBoxTreeMain.setVisible(true);
                }
                freshJlist();
//                for (String file : selected_file_set) {
//                    System.out.println(file);
//                }
            }
        });
        refreshList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                freshJlist();
            }
        });

        clearListBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected_directory_set.clear();
                selected_file_set.clear();
                selected_file_without_parent_path_set.clear();
                listModel = new DefaultListModel();
                selectedFileList.setModel(listModel);
            }
        });

        removeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selected_file_set.remove(selectedFileList.getSelectedValue());
                int index = selectedFileList.getSelectedIndex();
                if (index != -1) {
                    listModel.remove(index);
                }
            }
        });
        startTransferBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // start send Files
                Sender_Thread.startServer(peerIPTextField.getText(), Integer.parseInt(peerPortTextField.getText()), selected_file_set);
            }
        });
    }

    public void freshJlist() {
        listModel = new DefaultListModel();
        for (String file : selected_file_without_parent_path_set) {
//            System.out.println(file);
            listModel.addElement(file);
        }
        selectedFileList.setModel(listModel);
    }
}

