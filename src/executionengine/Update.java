package executionengine;

import compiler.Attribute;
import compiler.Caculate;
import dataitem.DataItem;


import recordmanagement.Tuple;

public class Update implements Operation {
	private Attribute[] attributeList;
	private Object[] dataList;
	private Operation belowOpr = null;
	
	public Update(Attribute[] att, Object[] obj, Operation opr){
		attributeList = att;
		dataList = obj;
		belowOpr = opr;
	}
	public void open() {
		belowOpr.open();
	}
	
	public boolean hasNext() {
		return belowOpr.hasNext();
	}
	
	public Tuple getNext() {
		Tuple tempTuple = belowOpr.getNext();
		for(int i = 0; i< dataList.length; i++){
			if(dataList[i] instanceof DataItem){
				tempTuple.setItem(attributeList[i].attIndex, (DataItem)dataList[i]);
			}else{
				DataItem tempItem = ((Caculate)dataList[i]).getResult(tempTuple);
				tempTuple.setItem(attributeList[i].attIndex, tempItem);
			}
		}
		return tempTuple;
	}

	public void close() {
	}

	public Attribute[] getAttributeList() {
		
		return belowOpr.getAttributeList();
	}
	 
}
 
