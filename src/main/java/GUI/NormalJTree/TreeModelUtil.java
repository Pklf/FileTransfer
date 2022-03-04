package GUI.NormalJTree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.util.Enumeration;
import java.util.Set;

public class TreeModelUtil {
    public static DefaultTreeModel createTreeModel(String rootPath, Set<String> selected_file_set){
        // create JTree without full-path
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode (rootPath);
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        for (String file : selected_file_set){
            // get path without first directory
            int seploc;
            int secseploc;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getRoot();
            while ((seploc = file.indexOf(File.separator)) != -1){
                if ((secseploc = file.indexOf(File.separator,seploc+1))==-1){
                    secseploc= file.length();
                }
                String parentPath = file.substring(seploc+1,secseploc);
                node = createNode(parentPath, node);
                file = file.substring(secseploc);
            }
        }
        return model;
    }

    public static DefaultMutableTreeNode createNode(String path, DefaultMutableTreeNode root){
        // create node
        // if parent directory not exist, create it, otherwise add under it
        Enumeration children = root.children();
        boolean found = false;
        while(children.hasMoreElements()){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            if(node.toString().equals(path)){
                found = true;
                return node;
            }
        }
        if (!found) {
            DefaultMutableTreeNode cuckoo = new DefaultMutableTreeNode(path);
            root.add(cuckoo);
        }
        return (DefaultMutableTreeNode) root.getLastChild();
    }
}
