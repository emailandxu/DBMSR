package test;
import gui.MainFrame;

import java.awt.Font;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import recordmanagement.RecordManagement;

/*
 * Created on 2005-3-22
 *
 * TODO
 */

/**
 * @author vole
 *
 * TODO
 */
public class TestGUI {

    public static void main(String[] args) {
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){}
		

		Font engFont = new Font("Arial", Font.PLAIN, 12),
		    chinaFont = new Font("ËÎÌו", Font.PLAIN, 12);
		UIDefaults ui = UIManager.getDefaults();
		ui.put("Tree.font", engFont);
		ui.put("Table.font", engFont);
		ui.put("Label.font", engFont);
		
		ui.put("InternalFrame.titleFont", chinaFont);
		ui.put("PopupMenu.font", chinaFont);
		ui.put("OptionPane.messageFont", chinaFont);
		ui.put("List.font", chinaFont);
		ui.put("OptionPane.buttonFont", chinaFont);
		ui.put("OptionPane.font", chinaFont);
		ui.put("ToggleButton.font", chinaFont);
		ui.put("ComboBox.font", chinaFont);
		ui.put("Button.font", chinaFont);
		ui.put("TextField.font", chinaFont);
		ui.put("TitledBorder.font", chinaFont);
		ui.put("Button.font", chinaFont);
		ui.put("TableHeader.font", chinaFont);

		final RecordManagement recordManagement = RecordManagement.getInstance();
        
        MainFrame mainFrame = new MainFrame();
        mainFrame.show();
        
    }
}
