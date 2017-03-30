/*
 * Created on 2005-3-26
 *
 */
package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import recordmanagement.RecordManagement;
/**
 * @author zh
 */
public class PropertyDialog extends JDialog implements ActionListener{

	private JPanel jContentPane = null;

	private JPanel jpnlDB = null;
	private JTextField jtxtDB = null;
	private JButton jbtnDBChoose = null;
	
	private JPanel jpnlTemp = null;
	private JTextField jtxtTemp = null;
	private JButton jbtnTempChoose = null;
	
	private JPanel jpnl3 = null;
	private JButton jbtnOK = null;
	
	private Icon openIcon = Util.createImageIcon("/images/open.gif");
	
	private JFileChooser fileChooser = null;
	
	private File dbDirectory = null;
	private File tempDirectory = null;
	
	private boolean editable;
	
	public PropertyDialog(JFrame mainFrame) {
		super(mainFrame,"设置数据库目录", true);
		initialize();
	}

	private void initialize() {
	    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fileChooser = new JFileChooser();
		fileChooser.setFont(new Font("宋体", Font.PLAIN, 12));
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		RecordManagement recordManagement = RecordManagement.getInstance();
		dbDirectory = new File(recordManagement.getDbDirectory());
		tempDirectory = new File(recordManagement.getTempDirectory());
		editable = ! recordManagement.isDBConnected();
		this.setContentPane(getJContentPane());
		this.pack();

		// 将窗口放在frame的中间
        int x = getParent().getX(),
			y = getParent().getY(),
			fw = getParent().getWidth(),
			fh = getParent().getHeight();
        this.setLocation( x + (fw - this.getWidth()) / 2,
                y + (fh - this.getHeight()) / 2);
	}
	
	private JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
			jContentPane.add(getJpnlDB(), null);
			jContentPane.add(getJpnlTemp(), null);
			jContentPane.add(getJpnlOK(), null);
		}
		return jContentPane;
	}

	private JPanel getJpnlTemp() {
		if (jpnlTemp == null) {
			jpnlTemp = new JPanel(new FlowLayout(FlowLayout.CENTER));
			jpnlTemp.setBorder(BorderFactory.createTitledBorder("临时目录"));
			jpnlTemp.setPreferredSize(new Dimension(300,60));
			jpnlTemp.add(getJtxtTemp(), null);
			jpnlTemp.add(getJbtnTempChoose(), null);
		}
		return jpnlTemp;
	}

	private JPanel getJpnlDB() {
		if (jpnlDB == null) {
			jpnlDB = new JPanel(new FlowLayout(FlowLayout.CENTER));
			jpnlDB.setBorder(BorderFactory.createTitledBorder("数据库目录"));
			jpnlDB.setPreferredSize(new Dimension(300,60));
			jpnlDB.add(getJtxtDB(), null);
			jpnlDB.add(getJbtnDBChoose(), null);
		}
		return jpnlDB;
	}

	private JPanel getJpnlOK() {
		if (jpnl3 == null) {
			jpnl3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			jpnl3.setPreferredSize(new Dimension(60,34));
			jpnl3.add(getJbtnOK(), null);
		}
		return jpnl3;
	}

	private JButton getJbtnOK() {
		if (jbtnOK == null) {
			jbtnOK = new JButton();
			jbtnOK.setName("jbtnOK");
			jbtnOK.setPreferredSize(new Dimension(60,24));
			jbtnOK.setText("确定");
			jbtnOK.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			        RecordManagement recordManagement = RecordManagement.getInstance();
			        
			        String abs = dbDirectory.getAbsolutePath();
			        if ( ! abs.endsWith("\\"))
			            abs = abs + "\\";
			        System.out.println(abs);
			        recordManagement.setDbDirectory(abs);

			        String tmp = tempDirectory.getAbsolutePath();
			        if ( ! tmp.endsWith("\\"))
			            tmp = tmp + "\\";
			        System.out.println(tmp);
			        recordManagement.setTempDirectory(tmp);
			        
			        recordManagement.saveConfig();
                    PropertyDialog.this.dispose();
			    }
			});
		}
		return jbtnOK;
	}

	private JTextField getJtxtDB() {
		if (jtxtDB == null) {
			jtxtDB = new JTextField(dbDirectory.getAbsolutePath());
			jtxtDB.setEditable(false);
			jtxtDB.setPreferredSize(new Dimension(250,24));
		}
		return jtxtDB;
	}

	private JButton getJbtnDBChoose() {
		if (jbtnDBChoose == null) {
			jbtnDBChoose = new JButton(openIcon);
			jbtnDBChoose.setEnabled(editable);
			jbtnDBChoose.setPreferredSize(new Dimension(24,24));
			jbtnDBChoose.addActionListener(this);
		}
		return jbtnDBChoose;
	}

	private JTextField getJtxtTemp() {
		if (jtxtTemp == null) {
			jtxtTemp = new JTextField(tempDirectory.getAbsolutePath());
			jtxtTemp.setEditable(false);
			jtxtTemp.setPreferredSize(new Dimension(250,24));
		}
		return jtxtTemp;
	}

	private JButton getJbtnTempChoose() {
		if (jbtnTempChoose == null) {
			jbtnTempChoose = new JButton(openIcon);
			jbtnTempChoose.setEnabled(editable);
			jbtnTempChoose.setPreferredSize(new Dimension(24,24));
			jbtnTempChoose.addActionListener(this);
		}
		return jbtnTempChoose;
	}

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getJbtnDBChoose()) {
            fileChooser.setCurrentDirectory(dbDirectory);
        }
        else if (e.getSource() == getJbtnTempChoose()) {
            fileChooser.setCurrentDirectory(tempDirectory);
        }
        fileChooser.changeToParentDirectory();
        int opt = fileChooser.showSaveDialog(this);

        if (opt == JFileChooser.APPROVE_OPTION) {
            File fileName = fileChooser.getSelectedFile();
            if (e.getSource() == getJbtnDBChoose()) {
                dbDirectory = fileName;
                jtxtDB.setText(dbDirectory.getAbsolutePath());
            }
            else if (e.getSource() == getJbtnTempChoose()) {
                tempDirectory = fileName;
                jtxtTemp.setText(tempDirectory.getAbsolutePath());
            }
        }
    }
    
    public File getDbDirectory() {
        return dbDirectory;
    }
    
    public File getTempDirectory() {
        return tempDirectory;
    }
}
