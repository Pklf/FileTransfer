package GUI;

import javax.swing.*;
import java.awt.*;


public class ProgressBar extends JFrame{
    JProgressBar jProgressBar = new JProgressBar();

    public ProgressBar(float totol_files_num, float files_num) {
        add(jProgressBar, BorderLayout.CENTER);
        jProgressBar.setPreferredSize(new Dimension(200, 50));
        jProgressBar.setStringPainted(true);
        updateProgres(totol_files_num, files_num);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public void updateProgres(float totol_files_num, float files_num){
        jProgressBar.setString(String.format("%d/%d", (int)files_num, (int)totol_files_num));
        jProgressBar.setValue((int) ((files_num/totol_files_num)*100));
    }

}