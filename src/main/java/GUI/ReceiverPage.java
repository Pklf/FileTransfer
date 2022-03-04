package GUI;

import GUI.NormalJTree.NormalJTree;
import GUI.NormalJTree.TreeModelUtil;
import FileTransfer.Receiver_Thread.ClientTask;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class ReceiverPage extends JFrame {
    private JPanel MainPage, leftToolBar, peerIPPanel;
    private JLabel peerIP, peerPort;
    private JButton pathChooserBtn, refreshListBtn, clearListBtn, removeBtn, showFileBtn, startReceiveBtn;
    private JScrollPane selectedFileScroll;
    private JList selectedFileList;
    private JTextField pathTextFeild;
    private ReceiverPage receiverPage;
    private ClientTask clientTask;
    private DefaultListModel listModel = new DefaultListModel();

    private Set<String> selected_file_without_parent_path_set = new HashSet<>();
    private Set<String> receiver_want_fileSet = new HashSet<>();

    public JPanel getMainPage(){ return MainPage;}

    public JButton getStartReceiveBtn(){return startReceiveBtn;}

    public Set<String> getReceiver_want_fileSet() {
        return receiver_want_fileSet;
    }

    public String getPathToSave() {
        return pathTextFeild.getText();
    }

    public Set<String> getSelected_file_without_parent_path_set() {
        return selected_file_without_parent_path_set;
    }

    public void setPeerIP(String ip){
        peerIP.setText(ip);
    }

    public void setPeerPort(String ip){ peerPort.setText(ip); }

    public ReceiverPage(Set<String> set, ClientTask clientTask) {
        selected_file_without_parent_path_set = set;
        this.clientTask = clientTask;
        init();
    }

    public void init() {
        pathChooserBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String savePath = FileChooser.pathChooser("D:", receiverPage);
                if (savePath != null) {
                    pathTextFeild.setText(savePath);
                }

            }
        });

        refreshListBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                freshJlist();
            }
        });

        clearListBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiver_want_fileSet.clear();
                listModel = new DefaultListModel();
                selectedFileList.setModel(listModel);
            }
        });

        removeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                receiver_want_fileSet.remove(selectedFileList.getSelectedValue());
                int index = selectedFileList.getSelectedIndex();
                if (index != -1) {
                    listModel.remove(index);
                }
            }
        });

        showFileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                System.out.println(selected_file_without_parent_path_set.size());
                DefaultTreeModel model = TreeModelUtil.createTreeModel("Root", getSelected_file_without_parent_path_set());
                NormalJTree jt = new NormalJTree(model);
                jt.confirmButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        jt.jtreeNodeToSet(receiver_want_fileSet);
                        freshJlist();
                    }
                });
                jt.setVisible(true);
            }
        });
    }

    public void freshJlist() {
        listModel = new DefaultListModel();
        for (String file : receiver_want_fileSet) {
            System.out.println(file);
            listModel.addElement(file);
        }
        selectedFileList.setModel(listModel);
    }
}
