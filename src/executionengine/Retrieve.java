package executionengine;

import recordmanagement.Table;
import recordmanagement.Tuple;
import buffer.DataBlock;

import compiler.Attribute;

/**
 *����,������ܵĻ�ʹ������
 *
 */
public class Retrieve implements Operation {
 
	private Table table;
	private Attribute[] attributeList;
	
	private int blockSize;
	private int tupleSize;
	private int blockID;
	private short tupleID;
	private DataBlock block;
	
	public Retrieve(Table t){
		table = t;
		blockSize = table.getTableBlockSize();
		attributeList = new Attribute[table.getSchema().getAttributeSize()];
		for (int i = 0; i < attributeList.length; i++){
			attributeList[i] = new Attribute(this.table, (byte)i);
		}
	}
	/**
	 *@see executionengine.Operation#open()
	 *
	 */
	public void open() {
		blockID = 0;
		tupleID = -1;
		block = table.getBlock(blockID);
		tupleSize = block.getTupleSize();
	}
	 
	/**
	 *@see executionengine.Operation#hasNext()
	 *
	 */
	public boolean hasNext() {
		if(blockSize == 0){
			return false;
		}
		if(tupleID >= tupleSize-1 ){//������һ��Block
			blockID++;
			if (blockID >= blockSize){//û����һ��Tuple��
				return false;
			}else{//��ȡһ���µ�Block
				block = table.getBlock(blockID);
				tupleSize = block.getTupleSize();
				while(tupleSize == 0){//Block��û��Tuple
					 if (blockID + 1 >= blockSize - 1){
					 	return false;
					 }else{
					 	blockID++;
					 	block = table.getBlock(blockID);
					 	tupleSize = block.getTupleSize();
					 }
				}
				tupleID = 0;
			}
		}else{
			tupleID++;
		}
		return true;
	}
	 
	/**
	 *@see executionengine.Operation#getNext()
	 *
	 */
	public Tuple getNext() {
		return block.getTuple(tupleID);
	}
	 
	/**
	 *@see executionengine.Operation#close()
	 *
	 */
	public void close() {
	}	
	
	public Attribute[] getAttributeList() {
		return this.attributeList;
	}
	 
}
 
