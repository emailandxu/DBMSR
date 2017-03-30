package executionengine;

import java.util.Date;

import recordmanagement.Tuple;

import compiler.Attribute;

import dataitem.DataItem;

/**
 *ͶӰ
 *
 */
public class Project implements Operation {
	private Attribute[] attributeList = null;
	private Operation belowOpr = null;
	private int[] projectIndex = null;//ͶӰ���������ԭ�����б��е�λ��
	//private Tuple tuple;
	
	public Project(Attribute[] l, Operation opr){
		attributeList = l;
		belowOpr = opr;
		Attribute[] belowAtt = belowOpr.getAttributeList();
		if (attributeList[0].table == null && attributeList[0].attIndex == -1 ){
			// * �����
			projectIndex = new int[belowAtt.length];
			for (int i = 0; i<projectIndex.length; i++){
				projectIndex[i] = i;
			}
			//����attributeList��Ϊ���ϲ�ĵ��÷���
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
				//i��ԭʼ��attributeListѭ����j��Table.*��ʽ������ѭ����k��tempAtt,projectIndexѭ��
				if (attributeList[i].attIndex == -1 ){
					//Table.*�����
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
					//Table.Attribute�����
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
 
