package GUI.NormalJTree;

import Util.Util;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

public class NormalJTree extends JFrame {
    private JPanel MainPanel;
    private JTree jtree;
    private JButton removeButton;
    public JButton confirmButton;

    public NormalJTree(DefaultTreeModel model) {
        setSize(500, 500);
        jtree.setModel(model);
        this.setContentPane(MainPanel);

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jtree.getSelectionPath().getLastPathComponent();

                if(selectedNode != jtree.getModel().getRoot()) {
                    DefaultTreeModel model = (DefaultTreeModel) jtree.getModel();
                    model.removeNodeFromParent(selectedNode);
                }
            }
        });
    }

    public DefaultMutableTreeNode getJTreeRoot(){
        return (DefaultMutableTreeNode) jtree.getModel().getRoot();
    }

    public void printall(DefaultMutableTreeNode root){
        Enumeration children = root.children();
        System.out.println(children.toString());
        while(children.hasMoreElements()){
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            System.out.println(child.toString());
            printall(child);
        }
    }

    public void jtreeNodeToSet(Set<String> selected_file_edited_set) {
        List<Object[]> level = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) jtree.getModel().getRoot();

        int index = 0;
        int pos = 0;

        level.add(new Object[] { root, "", (int) 0 });
        index++;

        while (level.size() != 0) {
            DefaultMutableTreeNode no;
            do {
                Object[] currentLevel = level.get(pos);
                no = (DefaultMutableTreeNode) currentLevel[0];
//                System.out.printf("%s (%d)\n", currentLevel[1], currentLevel[2]);

                Enumeration subLevel = no.children();

                if (!subLevel.hasMoreElements()) {
                    // If the file don't have extension, THIS program don't count it is a file
                    if (!Util.getFileExtension(currentLevel[1].toString()).equals("")) {
//                    System.out.println(currentLevel[1].toString());
                        selected_file_edited_set.add(currentLevel[1].toString());
                    }
                }
                while (subLevel.hasMoreElements()) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) subLevel.nextElement();
                    level.add(index++, new Object[] { child, currentLevel[1] + File.separator+
                            child.toString(),  (int) currentLevel[2] + 1});
                }
                level.remove(currentLevel);
                if (no.getChildCount() != 0) {
                    index -= no.getChildCount();
                } else {
                    index = 1;
                }
            } while (no != null && no.getChildCount() != 0);
        }
    }
}

