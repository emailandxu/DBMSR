/*
 * Created on 2005-3-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.AbstractTableModel;

import recordmanagement.Tuple;

import compiler.Attribute;

import executionengine.Operation;

/**
 * @author vole
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResultFrame extends JInternalFrame {

    private Operation operation;
    
    private Attribute[] attributes;
    
	private JPanel jContentPane = null;

	private JScrollPane jScrollPane = null;
	
	private JTable jTable = null;

	private ResultTableModel resultTableModel;
	
	private Thread thread = null;
	
	private boolean isDisposed = false;
	
    /**
     * 存放tuple
     */
    private List result;
    
	public ResultFrame(Operation operation) {
		super("查询结果", true, true, true, true);
		this.operation = operation;
		this.attributes = operation.getAttributeList();
		initialize();
	}
	
	private void initialize() {
	    this.result = new ArrayList();
		this.setBackground(Color.white);
		this.setSize(400,250);
		this.setContentPane(getJContentPane());
		this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                isDisposed = true;
                System.gc();
            }
        });
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
			jScrollPane = new JScrollPane(getJTable());
		}
		return jScrollPane;
	}
	
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(getTableModel());
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.setRowHeight(20);
			jTable.getTableHeader().setFont(new Font("Arial", Font.PLAIN, 12));
			jTable.getTableHeader().setPreferredSize(new Dimension(100, 24));
		}
		return jTable;
	}
	
	private ResultTableModel getTableModel() {
	    if (resultTableModel == null) {
            resultTableModel = new ResultTableModel();
        }
        return resultTableModel;
	}
	
	public void execute() {
        thread = new Thread() {
            public void run() {
                operation.open();
                Tuple tuple = null;
                while ( ! isDisposed && operation.hasNext()) {
                    tuple = operation.getNext();
                    getTableModel().addTuple(tuple);
                }
                operation.close();
            }
        };
        thread.start();
    }
	
	private class ResultTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return attributes.length;
        }

        public int getRowCount() {
            return result.size();
        }
        
        public String getColumnName(int column) {
            byte index = attributes[column].attIndex;
            return attributes[column].table.getSchema().getAttributeName(index);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Tuple tuple = (Tuple) result.get(rowIndex);
            return tuple.getItem((byte)columnIndex);
        }
	    
        /**
         * 加入新元组，更新表
         * @param tuple
         */
        public void addTuple(Tuple tuple) {
            result.add(tuple);
            fireTableRowsInserted(getTableModel().getRowCount(),
                    getTableModel().getRowCount());
        }
	}
}
