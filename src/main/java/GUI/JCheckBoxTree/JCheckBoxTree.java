package GUI.JCheckBoxTree;


import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class JCheckBoxTree extends JTree implements Serializable {
    public static ImageIcon ICON_FOLDER =  new ImageIcon("src/main/img/icon_folder.png");
    public static ImageIcon ICON_EXPANDEDFOLDER =  new ImageIcon("src/main/img/icon_folder_open.png");
    public static ImageIcon ICON_FILE =  new ImageIcon("src/main/img/icon_file.png");

    JCheckBoxTree selfPointer = this;

    public static void resizeIcon(){
        Image image = ICON_EXPANDEDFOLDER.getImage(); // transform it
        Image newimg = image.getScaledInstance(20, 20,  Image.SCALE_SMOOTH); // scale it the smooth way
        ICON_EXPANDEDFOLDER = new ImageIcon(newimg);

        image = ICON_FOLDER.getImage(); // transform it
        newimg = image.getScaledInstance(20, 20,  Image.SCALE_SMOOTH); // scale it the smooth way
        ICON_FOLDER = new ImageIcon(newimg);

        image = ICON_FILE.getImage(); // transform it
        newimg = image.getScaledInstance(15, 20,  Image.SCALE_SMOOTH); // scale it the smooth way
        ICON_FILE = new ImageIcon(newimg);
    }
    // Defining data structure that will enable to fast check-indicate the state of each node
    // It totally replaces the "selection" mechanism of the JTree
    private class CheckedNode {
        boolean isSelected;
        boolean hasChildren;
        boolean allChildrenSelected;

        public CheckedNode(boolean isSelected_, boolean hasChildren_, boolean allChildrenSelected_) {
            isSelected = isSelected_;
            hasChildren = hasChildren_;
            allChildrenSelected = allChildrenSelected_;
        }
    }
    HashMap<TreePath, CheckedNode> nodesCheckingState;
    HashSet<TreePath> checkedPaths = new HashSet<TreePath>();

    // Defining a new event type for the checking mechanism and preparing event-handling mechanism
    protected EventListenerList listenerList = new EventListenerList();

    public class CheckChangeEvent extends EventObject {
        public CheckChangeEvent(Object source) {
            super(source);
        }
    }

    public interface CheckChangeEventListener extends EventListener {
        public void checkStateChanged(CheckChangeEvent event);
    }

    public void addCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.add(CheckChangeEventListener.class, listener);
    }
    public void removeCheckChangeEventListener(CheckChangeEventListener listener) {
        listenerList.remove(CheckChangeEventListener.class, listener);
    }

    void fireCheckChangeEvent(CheckChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CheckChangeEventListener.class) {
                ((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
            }
        }
    }

    // Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        resetCheckingState();
    }

    // New method that returns only the checked paths (totally ignores original "selection" mechanism)
    public TreePath[] getCheckedPaths() {
        return checkedPaths.toArray(new TreePath[checkedPaths.size()]);
    }

    // Returns true in case that the node is selected, has children but not all of them are selected
    public boolean isSelectedPartially(TreePath path) {
        CheckedNode cn = nodesCheckingState.get(path);
        return cn.isSelected && cn.hasChildren && !cn.allChildrenSelected;
    }

    private void resetCheckingState() {
        nodesCheckingState = new HashMap<TreePath, CheckedNode>();
        checkedPaths = new HashSet<TreePath>();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel().getRoot();
        if (node == null) {
            return;
        }
        addSubtreeToCheckingStateTracking(node);
    }

    // Creating data structure of the current model for the checking mechanism
    private void addSubtreeToCheckingStateTracking(DefaultMutableTreeNode node) {
        TreeNode[] path = node.getPath();
        TreePath tp = new TreePath(path);
        CheckedNode cn = new CheckedNode(false, node.getChildCount() > 0, false);
        nodesCheckingState.put(tp, cn);
        for (int i = 0 ; i < node.getChildCount() ; i++) {
            addSubtreeToCheckingStateTracking((DefaultMutableTreeNode) tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
        }
    }

    // Overriding cell renderer by a class that ignores the original "selection" mechanism
    // It decides how to show the nodes due to the checking-mechanism
    private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer {
        private static final long serialVersionUID = -7341833835878991719L;
        JCheckBox checkBox;
        JLabel label;

        public CheckBoxCellRenderer() {
            super();
            this.setLayout(new BorderLayout());
            checkBox = new JCheckBox();
            label = new JLabel();
            add(label, BorderLayout.WEST);
            add(checkBox, BorderLayout.CENTER);
            setOpaque(false);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object obj = node.getUserObject();
            TreePath tp = new TreePath(node.getPath());
            CheckedNode cn = nodesCheckingState.get(tp);
            if (cn == null) {
                return this;
            }

            if (obj instanceof IconData) {
                IconData idata = (IconData)obj;
                if (expanded) {
//                    label.setText("expanded");
                    label.setIcon(idata.getExpandedIcon());
                } else {
//                    label.setText("close");
                    label.setIcon(idata.getIcon());
                }
            }
            else {
                label.setIcon(null);
            }

            checkBox.setSelected(cn.isSelected);
            checkBox.setText(obj.toString());
            checkBox.setOpaque(cn.isSelected && cn.hasChildren && ! cn.allChildrenSelected);
            return this;
        }
    }

    static class IconData {
        protected Icon   m_icon;
        protected Icon   m_expandedIcon;
        protected Object m_data;

        public IconData(Icon icon, Object data)
        {
            m_icon = icon;
            m_expandedIcon = null;
            m_data = data;
        }

        public IconData(Icon icon, Icon expandedIcon, Object data)
        {
            m_icon = icon;
            m_expandedIcon = expandedIcon;
            m_data = data;
        }

        public Icon getIcon()
        {
            return m_icon;
        }

        public Icon getExpandedIcon()
        {
            return m_expandedIcon!=null ? m_expandedIcon : m_icon;
        }

        public Object getObject()
        {
            return m_data;
        }

        public String toString()
        {
            return m_data.toString();
        }
    }

    private static MutableTreeNode init(File node){
        resizeIcon();
        return(addNodes(null,node));
    }

    private static MutableTreeNode scan(File node)
    {
        IconData idata = null;
        if (node.isDirectory()) {
            idata = new IconData(ICON_FOLDER, ICON_EXPANDEDFOLDER, node.getName());
        }else{
            idata = new IconData(ICON_FILE, ICON_FILE, node.getName());
        }
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(idata);
        if (node.isDirectory())
            for (File child: node.listFiles())
                ret.add(scan(child));
        return ret;
    }

    static class IconSort implements Comparator<IconData> {
        // sort by icon
        public int compare(IconData a, IconData b)
        {
            return a.m_data.toString().compareTo(b.m_data.toString());
        }
    }

    static DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
        String curPath = dir.getPath();
        IconData idata = null;
        if (dir.isDirectory()) {
            idata = new IconData(ICON_FOLDER, ICON_EXPANDEDFOLDER, dir.getName());
        }else{
            idata = new IconData(ICON_FILE, ICON_FILE, dir.getName());
        }
        DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(idata);
        if (curTop != null) { // should only be null at root
            curTop.add(curDir);
        }
        Vector ol = new Vector();
        String[] tmp = dir.list();
        if (tmp != null) {
            for (int i = 0; i < tmp.length; i++) {
//            ol.addElement(tmp[i]);
                idata = new IconData(ICON_FOLDER, ICON_EXPANDEDFOLDER, tmp[i]);
                ol.addElement(idata);
            }
        }

        Collections.sort(ol, new IconSort());
        File f;
        Vector files = new Vector();
        // Make two passes, one for Dirs and one for Files. This is #1.
        for (int i = 0; i < ol.size(); i++) {
            IconData object = (IconData) ol.elementAt(i);

            String thisObject = (String) object.getObject().toString();
            String newPath;
            if (curPath.equals("."))
                newPath = thisObject;
            else
                newPath = curPath + File.separator + thisObject;
            if ((f = new File(newPath)).isDirectory())
                addNodes(curDir, f);
            else {
                idata = new IconData(ICON_FILE, ICON_FILE, thisObject);
                files.addElement(idata);
            }
        }
        // Pass two: for files.
        for (int fnum = 0; fnum < files.size(); fnum++)
            curDir.add(new DefaultMutableTreeNode(files.elementAt(fnum)));
        return curDir;
    }
    private class TreeHandleUI extends BasicTreeUI {
        ///Variables
        private JTree t = null;
        private Icon rolloverIcon = null;
        private boolean rolloverEnabled = false;
        private UpdateHandler uH = null;

        private boolean isLeftToRight(Component c) {
            return c.getComponentOrientation().isLeftToRight();
        }

        public TreeHandleUI(JTree tree) {
            t = tree;
            uH = new UpdateHandler(t);
            t.addMouseMotionListener(uH);
        }

        public void setRolloverIcon(Icon rolloverG) {
            Icon oldValue = rolloverIcon;
            rolloverIcon = rolloverG;
            setRolloverEnabled(true);
            if (rolloverIcon != oldValue) {
                t.repaint();
            }
        }

        private void setRolloverEnabled(boolean handleRolloverEnabled) {
            boolean oldValue = rolloverEnabled;
            rolloverEnabled = handleRolloverEnabled;
            if (handleRolloverEnabled != oldValue) {
                t.repaint();
            }
        }

        @Override
        protected void paintExpandControl(Graphics g,
                                          Rectangle clipBounds, Insets insets,
                                          Rectangle bounds, TreePath path,
                                          int row, boolean isExpanded,
                                          boolean hasBeenExpanded,
                                          boolean isLeaf) {
            Object value = path.getLastPathComponent();

            if (!isLeaf && (!hasBeenExpanded || treeModel.getChildCount(value) > 0)) {
                int middleXOfKnob;
                if (isLeftToRight(t)) {
                    middleXOfKnob = bounds.x - getRightChildIndent() + 1;
                } else {
                    middleXOfKnob = bounds.x + bounds.width + getRightChildIndent() - 1;
                }
                int middleYOfKnob = bounds.y + (bounds.height / 2);

                if (isExpanded) {
                    Icon expandedIcon = getExpandedIcon();
                    if (expandedIcon != null)
                        drawCentered(tree, g, expandedIcon, middleXOfKnob, middleYOfKnob);
                } else if (isLocationInExpandControl(path, uH.getXPos(), uH.getYPos()) && !isExpanded && rolloverEnabled) {
                    if (row == uH.getRow()) {
                        if (rolloverIcon != null)
                            drawCentered(tree, g, rolloverIcon, middleXOfKnob, middleYOfKnob);
                    } else {
                        Icon collapsedIcon = getCollapsedIcon();
                        if (collapsedIcon != null)
                            drawCentered(tree, g, collapsedIcon, middleXOfKnob, middleYOfKnob);
                    }
                } else {
                    Icon collapsedIcon = getCollapsedIcon();
                    if (collapsedIcon != null)
                        drawCentered(tree, g, collapsedIcon, middleXOfKnob, middleYOfKnob);
                }
            }
        }

        private class UpdateHandler extends MouseHandler {
            private JTree t = null;
            private int xPos = 0;
            private int yPos = 0;

            private boolean leftToRight(Component c) {
                return c.getComponentOrientation().isLeftToRight();
            }

            public UpdateHandler(JTree tree) {
                t = tree;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xPos = e.getX();
                yPos = e.getY();
                t.repaint();
            }

            public int getXPos() {
                return xPos;
            }

            public int getYPos() {
                return yPos;
            }

            public int getRow() {
                return getRowForPath(t, getClosestPathForLocation(t, xPos, yPos));
            }
        }
    }

    public JCheckBoxTree(String path) {
        super(init(new File(path)));

        // change handler of gui
        TreeHandleUI tUI = new TreeHandleUI(this);
        this.setUI(tUI);

        tUI.setCollapsedIcon(new ImageIcon("src/main/img/icon_collapsed.png"));
        tUI.setExpandedIcon(new ImageIcon("src/main/img/icon_expanded.png"));
        tUI.setRolloverIcon(new ImageIcon("src/main/img/icon_rollover.png"));

        this.setShowsRootHandles(true);

        // Disabling toggling by double-click
        this.setToggleClickCount(0);
        // Overriding cell renderer by new one defined above
        CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
        this.setCellRenderer(cellRenderer);

        // Overriding selection model by an empty one
        DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel() {
            private static final long serialVersionUID = -8190634240451667286L;
            // Totally disabling the selection mechanism
            public void setSelectionPath(TreePath path) {
            }
            public void addSelectionPath(TreePath path) {
            }
            public void removeSelectionPath(TreePath path) {
            }
            public void setSelectionPaths(TreePath[] pPaths) {
            }
        };
        // Calling checking mechanism on mouse click
        this.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent arg0) {
                TreePath tp = selfPointer.getPathForLocation(arg0.getX(), arg0.getY());
                if (tp == null) {
                    return;
                }
                boolean checkMode = ! nodesCheckingState.get(tp).isSelected;
                checkSubTree(tp, checkMode);
                updatePredecessorsWithCheckMode(tp, checkMode);
                // Firing the check change event
                fireCheckChangeEvent(new CheckChangeEvent(new Object()));
                // Repainting tree after the data structures were updated
                selfPointer.repaint();
            }
            public void mouseEntered(MouseEvent arg0) {
            }
            public void mouseExited(MouseEvent arg0) {
            }
            public void mousePressed(MouseEvent arg0) {
            }
            public void mouseReleased(MouseEvent arg0) {
            }
        });
        this.setSelectionModel(dtsm);
    }

    // When a node is checked/unchecked, updating the states of the predecessors
    protected void updatePredecessorsWithCheckMode(TreePath tp, boolean check) {
        TreePath parentPath = tp.getParentPath();
        // If it is the root, stop the recursive calls and return
        if (parentPath == null) {
            return;
        }
        CheckedNode parentCheckedNode = nodesCheckingState.get(parentPath);
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        parentCheckedNode.allChildrenSelected = true;
        parentCheckedNode.isSelected = false;
        for (int i = 0 ; i < parentNode.getChildCount() ; i++) {
            TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
            CheckedNode childCheckedNode = nodesCheckingState.get(childPath);
            // It is enough that even one subtree is not fully selected
            // to determine that the parent is not fully selected
            if (! childCheckedNode.allChildrenSelected) {
                parentCheckedNode.allChildrenSelected = false;
            }
            // If at least one child is selected, selecting also the parent
            if (childCheckedNode.isSelected) {
//                parentCheckedNode.isSelected = true;
            }
        }
        if (parentCheckedNode.isSelected) {
            checkedPaths.add(parentPath);
        } else {
            checkedPaths.remove(parentPath);
        }
        // Go to upper predecessor
        updatePredecessorsWithCheckMode(parentPath, check);
    }

    // Recursively checks/unchecks a subtree
    protected void checkSubTree(TreePath tp, boolean check) {
        CheckedNode cn = nodesCheckingState.get(tp);
        cn.isSelected = check;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
        for (int i = 0 ; i < node.getChildCount() ; i++) {
            checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
        }
        cn.allChildrenSelected = check;

        // if is file add to checkedPaths
        if (check && !cn.hasChildren) {
            checkedPaths.add(tp);
        } else {
            checkedPaths.remove(tp);
        }
    }

}
