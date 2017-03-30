package executionengine;

import java.util.*;

import compiler.Attribute;
import dataitem.DataItem;


import recordmanagement.Tuple;
//import recordmanagement.*;

/**
 *简易的笛卡尔积实现方式，不论实际缓冲区大小如何都采用一趟算法，极端情况下会造成效率极低
 *
 */
public class CrudeJoin implements Join {
	private Operation opr1;
	private Operation opr2;
	private Attribute[] attributeList;
	private Tuple[] tupleArray = null;
	private Tuple tupleofOpr2 = null;
	int leftSize;	//记录opr1中的AttributeSize
	private int tupleID;
	public CrudeJoin(Operation o1, Operation o2){
		opr1 = o1;
		opr2 = o2;
		
		//以下设置attributeList
		Attribute[] att1 = opr1.getAttributeList();
		Attribute[] att2 = opr2.getAttributeList();
		
		leftSize = att1.length;
		
		this.attributeList = new Attribute[att1.length + att2.length];
		int i;
		for (i = 0; i < att1.length; i++){
			this.attributeList[i] = att1[i]; 
		}
		for (; i < att1.length + att2.length; i++){
			this.attributeList[i] = att2[i - att1.length];
		}		
	}
	/**
	 *@see executionengine.Operation#open()
	 *
	 */
	public void open() {
		List tupleList1 = new LinkedList();
		opr1.open();
		opr2.open();
				
		while (opr1.hasNext()){
			tupleList1.add(opr1.getNext());
		}
		tupleArray = (Tuple[])tupleList1.toArray(new Tuple[0]);
		tupleID = -1;
	} 
	
	public boolean hasNext() {
		if(tupleID == tupleArray.length-1 || tupleID == -1){
			if(opr2.hasNext()){
				tupleID = 0;
				//tupleofOpr2 = opr2.getNext();
			}else{
				return false;
			}
		}else{
			tupleID++;
		}
		return true;
	}	 
	
	public Tuple getNext() {
		final Tuple tempTuple2 = opr2.getNext();
		final Tuple tempTuple1 = this.tupleArray[tupleID];
		return new Tuple(){
			
			public Date getLastModified(){
				return null;
			}
			
			public DataItem getItem(byte position) {
				
				if(position > leftSize - 1){
					return (tempTuple2.getItem((byte)(position - leftSize) ));
				}else{
					return (tempTuple1.getItem(position));
				}				
			}
			public void setItem(byte position, DataItem item) {
				
			}
		};
	}
	 
	public void close() {
	}
	
	public Attribute[] getAttributeList() {
		return this.attributeList;
	}
	 
}
 
