package GUI.JCheckBoxTree;

import Util.Util;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class JCheckBoxTreeMain extends JFrame {

    private Set<String> temp_selected_file_set = new HashSet<>();
    private Set<String> temp_selected_file_without_parent_path_set = new HashSet<>();

    public Set<String> getTemp_selected_file_set() {
        return temp_selected_file_set;
    }

    public Set<String> getTemp_selected_file_without_parent_path_set() {
        return temp_selected_file_without_parent_path_set;
    }

    public JCheckBoxTreeMain(String path) {
        super();
        setSize(500, 500);
        this.getContentPane().setLayout(new BorderLayout());
        final JCheckBoxTree cbt = new JCheckBoxTree(path);
        this.getContentPane().add(new JScrollPane(cbt));
        cbt.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
                System.out.println("=================event=============");
                // Get all checked treepath
                TreePath[] paths = cbt.getCheckedPaths();
                // clear Sets
                temp_selected_file_without_parent_path_set.clear();
                temp_selected_file_set.clear();
                // Get full path of checked treepath and add them to set
                for (TreePath tp : paths) {
                    Path filePath = Paths.get(Util.getPathWithoutLast(path));
                    // System.out.println("path->" + path);
                    for (Object pathPart : tp.getPath()) {
                        if (filePath != null) {
                            filePath = Paths.get(filePath.toString(), pathPart.toString());
                            System.out.println(filePath);
                        } else {
                            filePath = Paths.get(pathPart.toString());
                        }
                        if (Files.isRegularFile(filePath)) {
                            temp_selected_file_without_parent_path_set.add(Util.getParentPathAndFile(filePath.toString(), path));
                            temp_selected_file_set.add(filePath.toString());
                        }
                    }
                }
            }
        });
    }
}