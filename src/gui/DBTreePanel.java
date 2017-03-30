/*
 * Created on 2005-3-22
 *
 * TODO
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import recordmanagement.RecordManagement;
import recordmanagement.Schema;
import recordmanagement.Table;

/**
 * @author vole
 *
 * TODO
 */
public class DBTreePanel extends JPanel{
    private static final String root = "Oracle 12v";
    
    private static final String indexOn = "INDEX_ON_";
    
    private MainFrame mainFrame;
    private RecordManagement recordManagement;
    
    private JDesktopPane jDesktopPane;
    
	private JScrollPane jScrollPane = null;
	private JTree jTree = null;
	
	/**
	 * oracle 菜单
	 */
	private JPopupMenu oraclePopupMenu = null;
	private JMenuItem connectItem = null;
	private JMenuItem disconnectItem = null;
	
	/**
	 * table 菜单
	 */
	private JPopupMenu tablePopupMenu = null;
    
	/**
	 * index 菜单
	 */
	private JPopupMenu indexPopupMenu = null;
	
    public DBTreePanel(MainFrame mainFrame, JDesktopPane jDesktopPane) {
        this.mainFrame = mainFrame;
        this.recordManagement = RecordManagement.getInstance();
        this.jDesktopPane = jDesktopPane;
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(200, 200));
        this.add(getJScrollPane(), BorderLayout.CENTER);
    }
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTree());
		}
		return jScrollPane;
	}
	
	private JPopupMenu getTablePopupMenu() {
        if (tablePopupMenu == null) {
            tablePopupMenu = new JPopupMenu();
            JMenuItem showSchemaItem = new JMenuItem("显示表模式");
            JMenuItem dropTableItem = new JMenuItem("删除表");
            showSchemaItem.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e) {
                    Object node = jTree.getLastSelectedPathComponent();
                    if (node instanceof Table) {
                        SchemaFrame schemaFrame =
                            new SchemaFrame(((Table)node).getSchema());
                        jDesktopPane.add(schemaFrame, 1);
                        schemaFrame.show();
                    }
                }
                
            });
            dropTableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Object node = jTree.getLastSelectedPathComponent();
                    if (node instanceof Table) {
                        recordManagement.removeTable(((Table)node).getTableName());
                        jTree.updateUI();
                    }
                }
            });
            tablePopupMenu.add(showSchemaItem);
            tablePopupMenu.add(dropTableItem);
        }
        return tablePopupMenu;
    }
	
	private JPopupMenu getOraclePopupMenu() {
        if (oraclePopupMenu == null) {
            oraclePopupMenu = new JPopupMenu();
            connectItem = new JMenuItem("连接数据库");
            disconnectItem = new JMenuItem("断开数据库");
            
            connectItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    String name = (String)JOptionPane.showInputDialog(mainFrame,
                            "请输入数据库名称:", "打开数据库", JOptionPane.OK_CANCEL_OPTION,
                            null, null, "zhzh");
                    try {
                        recordManagement.loadDB(name);
                        updateTree();
                    } catch (IllegalArgumentException ie) {
                        JOptionPane.showMessageDialog(mainFrame, "目标数据库不存在",
                                "警告 - 数据库", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            disconnectItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JInternalFrame[] internal = jDesktopPane.getAllFrames();
                    for (int i = 0; i < internal.length; i++) {
                        if (internal[i] instanceof SchemaFrame
                                || internal[i] instanceof ResultFrame) {
                            internal[i].dispose();
                        }
                    }
                    recordManagement.closeDB();
                    getJTree().updateUI();
                }
            });
            oraclePopupMenu.add(connectItem);
            oraclePopupMenu.add(disconnectItem);
        }
        if (recordManagement.isDBConnected()) {
            connectItem.setEnabled(false);
            disconnectItem.setEnabled(true);
        }
        else {
            connectItem.setEnabled(true);
            disconnectItem.setEnabled(false);
        }
        return oraclePopupMenu;
    }
    
    private JPopupMenu getIndexPopupMenu() {
        if (indexPopupMenu == null) {
            indexPopupMenu = new JPopupMenu();
            JMenuItem dropItem = new JMenuItem("删除索引");
            dropItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    TreePath path = jTree.getSelectionPath();
                    Table table = (Table)path.getPathComponent(2);
                    Object node = path.getPathComponent(3);
                    if (node instanceof String) {
                        String attributeName = ((String)node).substring(indexOn.length());
                        Schema schema = table.getSchema();
                        byte order = schema.getAttributeIDByName(attributeName);
                        try {
                            table.dropIndex(order);
                        } catch (IllegalArgumentException ie) {
                            JOptionPane.showMessageDialog(mainFrame, "不能删除主键上的索引",
                                    "警告 - 数据库", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            });
            indexPopupMenu.add(dropItem);
        }
        return indexPopupMenu;
    }
    
	private JTree getJTree() {
		if (jTree == null) {
			jTree = new JTree(new DBTreeModel()) {
                public Insets getInsets() {
            		return new Insets(5,5,5,5);
                }
			};
			jTree.setCellRenderer(new DBTreeCellRenderer());
			jTree.getSelectionModel().setSelectionMode(
			        TreeSelectionModel.SINGLE_TREE_SELECTION);
			jTree.addMouseListener(new MouseAdapter() {
			    
			    public void mouseReleased(MouseEvent e) {
                    TreePath treePath = jTree.getPathForLocation(e.getX(), e.getY());
                    if (treePath != null) {
                        jTree.setSelectionPath(treePath);
                        if (e.isPopupTrigger()) {
                            Object node = jTree.getLastSelectedPathComponent();
                            if (node == root) {
                                JPopupMenu menu = getOraclePopupMenu();
                                menu.show(e.getComponent(), e.getX(), e.getY());
                            }
                            else if (node instanceof Table) {
                                JPopupMenu menu = getTablePopupMenu();
                                menu.show(e.getComponent(), e.getX(), e.getY());
                            }
                            else if (node instanceof String) {
                                JPopupMenu menu = getIndexPopupMenu();
                                menu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    }
                }
			});
		}
		return jTree;
	}
	
	void updateTree() {
	    getJTree().updateUI();
	}
	
	class DBTreeModel implements TreeModel {

        public Object getRoot() {
            return root;
        }

        public int getChildCount(Object parent) {
            if (parent == root) {
                return recordManagement.isDBConnected() ? 1 : 0;
            }
            if (parent == recordManagement) {
                return recordManagement.getTableSize();
            }
            if (parent instanceof Table) {
                Schema schema = ((Table)parent).getSchema();
                int count = 0;
                for (byte i = 0; i < schema.getAttributeSize(); i++) {
                    if (schema.getAttributeIndex(i) != null) {
                        count++;
                    }
                }
                return count;
            }
            return 0;
        }

        public Object getChild(Object parent, int index) {
            if (parent == root) {
                return recordManagement;
            }
            if (parent == recordManagement) {
                return recordManagement.getTable(index);
            }
            if (parent instanceof Table) {
                Schema schema = ((Table)parent).getSchema();
                int count = 0;
                for (byte i = 0; i < schema.getAttributeSize(); i++) {
                    if (schema.getAttributeIndex(i) != null) {
                        if (count == index)
                            return indexOn + schema.getAttributeName(i);
                        count++;
                    }
                }
            }
            return null;
        }

        public int getIndexOfChild(Object parent, Object child) {
            if (parent == root) {
                return 0;
            }
            if (parent == recordManagement) {
                for (byte i = 0; i < recordManagement.getTableSize(); i++) {
                    if (recordManagement.getTable(i) == child)
                        return i;
                }
            }
            if (parent instanceof Table) {
                String attributeName = ((String)child).substring(indexOn.length());
                Schema schema = ((Table)parent).getSchema();
                byte index = schema.getAttributeIDByName(attributeName);
                
                int count = 0;
                for (byte i = 0; i < schema.getAttributeSize(); i++) {
                    if (schema.getAttributeIndex(i) != null) {
                        if (count == index)
                            return count;
                        count++;
                    }
                }
            }
            return -1;
        }

        public boolean isLeaf(Object node) {
            return node instanceof String && node != root;
        }

        public void addTreeModelListener(TreeModelListener l) {
        }

        public void removeTreeModelListener(TreeModelListener l) {
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }
	    
	}

	private class DBTreeCellRenderer extends DefaultTreeCellRenderer {
	    private Icon oracleIcon = Util.createImageIcon("/images/oracle.png");
	    private Icon dbIcon = Util.createImageIcon("/images/db.png");
	    private Icon tableIcon = Util.createImageIcon("/images/table.png");
	    private Icon indexIcon = Util.createImageIcon("/images/index.png");
	    
	    public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
            
            Icon iIcon = null;
            if (value == root) {
                iIcon = oracleIcon;
            }
            else if (value == recordManagement) {
                iIcon = dbIcon;
            }
            else if (value instanceof Table) {
                iIcon = tableIcon;
            }
            else {
                iIcon = indexIcon;
            }
            setIcon(iIcon);
            setDisabledIcon(iIcon);
            setLeafIcon(iIcon);
            setOpenIcon(iIcon);
            setClosedIcon(iIcon);
            
            return this;
        }
	}
}
