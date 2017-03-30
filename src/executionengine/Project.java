package executionengine;

import java.util.Date;

import recordmanagement.Tuple;

import compiler.Attribute;

import dataitem.DataItem;

/**
 *投影
 *
 */
public class Project implements Operation {
	private Attribute[] attributeList = null;
	private Operation belowOpr = null;
	private int[] projectIndex = null;//投影后的属性在原属性列表中的位置
	//private Tuple tuple;
	
	public Project(Attribute[] l, Operation opr){
		attributeList = l;
		belowOpr = opr;
		Attribute[] belowAtt = belowOpr.getAttributeList();
		if (attributeList[0].table == null && attributeList[0].attIndex == -1 ){
			// * 的情况
			projectIndex = new int[belowAtt.length];
			for (int i = 0; i<projectIndex.length; i++){
				projectIndex[i] = i;
			}
			//重设attributeList，为了上层的调用方便
			attributeList = belowAtt;
		}else{
			int attNum = 0;
			for (int i = 0; i<attributeList.length; i++){
				if (attributeList[i].attIndex == -1 ){
					attNum += attributeList[i].table.getSchema().getAttributeSize();
				}else{
					attNum++;
				}
			}
			projectIndex = new int[attNum];
			Attribute[] tempAtt = new Attribute[attNum];
			Attribute att;
			for (int i = 0, j = 0, k = 0; i<attributeList.length; i++){
				//i对原始的attributeList循环，j对Table.*形式的属性循环，k对tempAtt,projectIndex循环
				if (attributeList[i].attIndex == -1 ){
					//Table.*的情况
					for (j = 0; j<attributeList[i].table.getSchema().getAttributeSize();j++){
						att = new Attribute(attributeList[i].table,(byte)j);
						tempAtt[k] = att;
						for (int t = 0; t < belowAtt.length; t++){
							if (att.equals(belowAtt[t])){
								projectIndex[k] = t;
								k++;
								break;
							}
						}//endfor
					}//endfor
				}else{
					//Table.Attribute的情况
					att = attributeList[i];
					tempAtt[k] = att;
					for (int t = 0; t < belowAtt.length; t++){
						if (att.equals(belowAtt[t])){
							projectIndex[k] = t;
							k++;
							break;
						}
					}//endfor
				}//endif
			}//endfor
			this.attributeList = tempAtt;
		}//endif
	}
	
	public void open() {
		belowOpr.open();		
	}
	 
	public boolean hasNext() {
		return belowOpr.hasNext();
	}
	
	public Tuple getNext() {
		final Tuple temptuple = belowOpr.getNext();
		return new Tuple(){
			public Date getLastModified() {
				return null;
			}

			public DataItem getItem(byte position) {
				return temptuple.getItem((byte)projectIndex[position]);
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
 
