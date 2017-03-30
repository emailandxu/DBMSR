/*
 * Created on 2005-3-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import executionengine.Operation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position.Bias;

import compiler.StringParser;
/**
 * @author vole
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConsoleFrame extends JInternalFrame {
    private String SQL_REMINDER = "SQL >";
    
    private MainFrame mainFrame = null;
    private DBTreePanel dbTreePanel = null;
    private JDesktopPane desktopPane = null;

	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;
	
	private JTextPane jTextPane = null;
	private StyledDocument consoleDocument = null;
	
	private JTextPane jRowHeader = null;
	private StyledDocument rowDocument = null;
	
	private SimpleAttributeSet userStyle, sysStyle;
	
	private int isOuterInsert;
	
	/**
	 * 限制caret只能在本行移动,
	 * 以及记录本句语句的开始处
	 */
	private int lineRestrict, sentenceRestrict;
	
	public ConsoleFrame(MainFrame mainFrame, DBTreePanel dbTreePanel, JDesktopPane desktopPane) {
		super("Oracle 12v - SQL++", true, false, true, true);
		this.mainFrame = mainFrame;
		this.dbTreePanel = dbTreePanel;
		this.desktopPane = desktopPane;
		initialize();
	}
	
	private void initialize() {
		this.setSize(490,200);
		this.setContentPane(getJContentPane());
	}
	
	private JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJTextPane());
			jScrollPane.setRowHeaderView(getJRowHeader());
			JLabel jlbl = new JLabel();
			jlbl.setOpaque(true);
			jlbl.setBackground(Color.WHITE);
			jScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, jlbl);
		}
		return jScrollPane;
	}
	
	private JTextPane getJRowHeader() {
        if (jRowHeader == null) {
            jRowHeader = new JTextPane();
            jRowHeader.setEditable(false);
            jRowHeader.setPreferredSize(new Dimension(58, 40));
            rowDocument = jRowHeader.getStyledDocument();
            try {
                rowDocument.insertString(rowDocument.getLength(), SQL_REMINDER, sysStyle);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return jRowHeader;
    }
    
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane() {
                public boolean getScrollableTracksViewportWidth() {
                    return false;
                }
                public void setSize(Dimension d) {
                    if (d.width < getParent().getSize().width) {
                        d.width = getParent().getSize().width;
                    }
                    super.setSize(d);
                }
			};
			jTextPane.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (jTextPane.getCaretPosition() == lineRestrict
                            && jTextPane.getCaretPosition() != 0
                            && e.getKeyChar() == '\b') {
                        isOuterInsert = 2;
                        try {
                            consoleDocument.insertString(jTextPane.getCaretPosition(), "\n", userStyle);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
			
			InputMap inputMap = jTextPane.getInputMap(JComponent.WHEN_FOCUSED);
			KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_V,
                    Event.CTRL_MASK);
//			inputMap.getParent().getParent().getParent().remove(key);
			
			consoleDocument = jTextPane.getStyledDocument();
			initStyle();
			jTextPane.setParagraphAttributes(userStyle, true);
			jTextPane.setCharacterAttributes(userStyle, true);
			
			// 防止caret移到当前行之外
			jTextPane.setNavigationFilter(new NavigationFilter() {
			    public void moveDot(FilterBypass fb, int dot, Bias bias) {
			        if (dot >= lineRestrict) {
			            super.moveDot(fb, dot, bias);
                    }
                }
			    
                public void setDot(FilterBypass fb, int dot, Bias bias) {
			        if (dot >= lineRestrict) {
				        super.setDot(fb, dot, bias);
                    }
                }
			});
			consoleDocument.addDocumentListener(new DocumentListener() {

                public void changedUpdate(DocumentEvent e) {
                }

                public void insertUpdate(DocumentEvent e) {
                    if (isOuterInsert != 0) {
                        isOuterInsert--;
                        return;
                    }
                    try {
                        String insertString = consoleDocument.getText(
                                e.getOffset(), e.getLength());
                        if (insertString.length() == 1) {
                            // 换行时判断是否为命令结束符';'
                            if (insertString.charAt(0) == '\n') {
                                String line = consoleDocument.getText(lineRestrict,
                                        consoleDocument.getLength() - lineRestrict);
                                
                                lineRestrict = e.getOffset() + 1;
                                rowDocument.insertString(rowDocument.getLength(), "\n", userStyle);
                                if (line.matches(".*;\\s*")) {
                                    // 以;结束
                                    String sql = consoleDocument.getText(sentenceRestrict,
                                            consoleDocument.getLength() - sentenceRestrict);
                                    sql = sql.replace('\n', '\u0020');
                                    sql = sql.replace(';', '\u0020');
                                    
                              //      System.out.println(sql);
                                    
                                    executeSQL(sql);
                                    
                                    sentenceRestrict = consoleDocument.getLength();
                                    rowDocument.insertString(rowDocument.getLength(), SQL_REMINDER, sysStyle);
                                }
                            }
                        }
            //            else
            //                throw new IllegalStateException("only single insert is supported");
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                public void removeUpdate(DocumentEvent e) {
                }
			    
			});
		}
		return jTextPane;
	}

	/**
	 * 将console中的用户输入字体与系统提示字体区分开
	 */
	private void initStyle() {
		userStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(userStyle, Color.BLACK);
		StyleConstants.setFontFamily(userStyle, "Courier New");
		StyleConstants.setFontSize(userStyle, 16);
		
		sysStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(sysStyle, Color.BLUE);
		StyleConstants.setFontFamily(sysStyle, "Courier New");
		StyleConstants.setFontSize(sysStyle, 16);
    }
	
	/**
	 * 执行
	 * @param sql
	 */
	private void executeSQL(String sql) {
	    try {
            Operation o = StringParser.SQLtoOperation(sql);
            if (o != null) {
                ResultFrame resultFrame = new ResultFrame(o);
                desktopPane.add(resultFrame);
                resultFrame.show();
                resultFrame.execute();
            } else {
                dbTreePanel.updateTree();
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(),
                    "错误 - SQL语句", JOptionPane.WARNING_MESSAGE);
        }
	}
}
