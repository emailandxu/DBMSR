/*
 * Created on 2005-3-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import recordmanagement.AttributeType;
import recordmanagement.Schema;
/**
 * @author vole
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SchemaFrame extends JInternalFrame {

    private Schema schema;
    
	private JPanel jContentPane = null;

	private JTable jTable = null;
	private JScrollPane jScrollPane = null;
	
	public SchemaFrame(Schema schema) {
		super("", true, true, true, true);
		this.schema = schema;
		initialize();
	}
	
	private void initialize() {
	    this.setTitle("Schema - " + schema.getSchemaName());
		this.setBackground(Color.white);
		this.setSize(400,250);
		this.setContentPane(getJContentPane());
	}
	
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}
	
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane(getJTable());
		}
		return jScrollPane;
	}
	
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(new DBSchemaTableModel());
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.setShowGrid(false);
			jTable.setRowHeight(20);
		}
		return jTable;
	}
	
	private class DBSchemaTableModel extends AbstractTableModel {
	    private String[] tableTitle = new String[] {
	            "主键", "名称", "类型", "长度", "存在索引"
	    };

        public int getColumnCount() {
            return tableTitle.length;
        }

        public int getRowCount() {
            return schema.getAttributeSize();
        }
        
        public String getColumnName(int column) {
            return tableTitle[column];
        }
        
        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return AttributeType.class;
            case 3:
                return Integer.class;
            case 4:
                return Boolean.class;
            default:
                return null;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            
            switch (columnIndex) {
            case 0:
                return schema.getKeyAttribute() == rowIndex ?
                        Boolean.TRUE : Boolean.FALSE;
            case 1:
                return schema.getAttributeName((byte)rowIndex);
            case 2:
                return schema.getAttributeType((byte)rowIndex);
            case 3:
                AttributeType type = schema.getAttributeType((byte)rowIndex);
                byte length = type.getByteLength();
                return new Integer( type.getType() == AttributeType.STRING ?
                        length / 2 : length );
            case 4:
                return schema.getAttributeIndex((byte)rowIndex) == null ?
                        Boolean.FALSE : Boolean.TRUE;
            default:
                return null;
            }
        }
	    
	}
}
