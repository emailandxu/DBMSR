/*
 * Created on 2005-3-22
 *
 * TODO
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import recordmanagement.RecordManagement;
/**
 * @author vole
 *
 * TODO
 */
public class MainFrame extends JFrame implements WindowListener{
    
    private RecordManagement recordManagement;

	private JPanel jContentPane = null;

	private JSplitPane jSplitPane = null;
	private JDesktopPane jDesktopPane = null;
	private DBTreePanel treePanel = null;
	
	private JMenuBar mainMenuBar = null;
	private JMenu setMenu = null;
	private JMenuItem propItem = null;

	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		recordManagement = RecordManagement.getInstance();
		initialize();
	}
	
	private void initialize() {
	    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);
		this.setSize(800,600);
		this.setJMenuBar(getMainMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Oracle 12v");
	}
	
	private JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJSplitPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
	
	private JMenuBar getMainMenuBar() {
        if (mainMenuBar == null) {
            mainMenuBar = new JMenuBar();
            setMenu = new JMenu("系统");
            propItem = new JMenuItem("设置");
            propItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PropertyDialog dialog = new PropertyDialog(MainFrame.this);
                    dialog.show();
                }
            });
            setMenu.add(propItem);
            mainMenuBar.add(setMenu);
        }
        return mainMenuBar;
    }
	
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setDividerSize(5);
			jSplitPane.setLeftComponent(getDBTreePanel());
			jSplitPane.setRightComponent(getJDesktopPane());
		}
		return jSplitPane;
	}
	
	private JDesktopPane getJDesktopPane() {
		if (jDesktopPane == null) {
			jDesktopPane = new JDesktopPane();
			jDesktopPane.setMinimumSize(new Dimension(500, 500));
			jDesktopPane.setBackground(Color.GRAY);
			
			ConsoleFrame console = new ConsoleFrame(this, getDBTreePanel(), jDesktopPane);
			console.setLocation(0, this.getHeight() - console.getHeight() - 40);
			jDesktopPane.add(console);
			console.show();
		}
		return jDesktopPane;
	}
	
	private DBTreePanel getDBTreePanel() {
        if (treePanel == null) {
            treePanel = new DBTreePanel(this, getJDesktopPane());
        }
        return treePanel;
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        if (recordManagement.isDBConnected()) {
            int option = JOptionPane.showConfirmDialog(this, "数据库连接正在使用中，要断开连接吗？",
                    "确认 - 断开连接", JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                JInternalFrame[] internal = jDesktopPane.getAllFrames();
                for (int i = 0; i < internal.length; i++) {
                    if (internal[i] instanceof SchemaFrame
                            || internal[i] instanceof ResultFrame) {
                        internal[i].dispose();
                    }
                }
                recordManagement.closeDB();
                this.dispose();
                System.exit(0);
            }
            return;
        }
        this.dispose();
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }
}
