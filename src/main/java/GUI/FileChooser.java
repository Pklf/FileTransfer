package GUI;

import Util.Util;

import javax.swing.*;
import java.io.File;
import java.util.Set;


public class FileChooser {
    public static JFileChooser chooser;

    public static void fileChooser(String directoryPath, JFrame frame, Set<String> selected_directory_set, Set<String> selected_file_edited_set){
        chooser = new JFileChooser(directoryPath);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);
        int flag = chooser.showOpenDialog(frame);
        // if click confirm
        if (flag == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File file : files){
                if (file.isDirectory()){
                    selected_directory_set.add(file.getAbsolutePath());
                }else if(file.isFile()){
                    selected_file_edited_set.add(Util.getParentPathAndFile(file.getAbsolutePath(),file.getName()));
                }
            }
        }
    }

    public static String pathChooser(String directoryPath, JFrame frame){
        chooser = new JFileChooser(directoryPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        int flag = chooser.showOpenDialog(frame);
        if (flag == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().toString();
        }
        return null;
    }
}
