package executionengine;

import buffer.DataBlock;
import compiler.Attribute;
import compiler.Condition;
import compiler.Constant;
import compiler.StringParser;
import dataitem.BooleanItem;
import dataitem.DataItem;

import recordmanagement.Table;
import recordmanagement.Tuple;

public class Delete implements Operation {
	private Table table;
	private Attribute[] attributeList;
	private Condition condition;
	
	private int blockSize;
	private int tupleSize;
	private int blockID;
	private short tupleID;
	private DataBlock block;
	
	private Tuple tuple;
	
	public Delete(Table t, Condition con){
		table = t;
		condition = con;
		blockSize = table.getTableBlockSize();
		attributeList = new Attribute[table.getSchema().getAttributeSize()];
		for (int i = 0; i < attributeList.length; i++){
			attributeList[i] = new Attribute(this.table, (byte)i);
		}
	}

	public void open() {
		if(blockSize == 0){
			StringParser.DisplayError("表为空!");
		}
		blockID = 0;
		tupleID = 0;
		//block = table.getBlock(blockID);
		//tupleSize = block.getTupleSize();
	}	 

	public boolean hasNext() {
		if(block != null){
			this.tupleSize = block.getTupleSize();
		}
		Tuple tempTuple = null;
		int i, j;
		for (i = blockID; i<blockSize; i++){
			block = table.getBlock(i);
			//tupleID = 0;
			tupleSize = block.getTupleSize();
			if (block.getTupleSize() == 0){
				tupleID = 0;
				continue;
			}
			for (j = tupleID; j<tupleSize; j++){
				tempTuple = block.getTuple((short)j);
				if (this.passCondition(this.condition, tempTuple).getData()){
					this.tuple = tempTuple;
					blockID = i;
					tupleID = (short)j;
					return true;
				}
			}
			tupleID = 0;
		}
		return false;
	}
		/*
		if(block != null){
			this.tupleSize = block.getTupleSize();
		}
		Tuple tempTuple;
		while(hasNextTuple()){
			tempTuple = block.getTuple(tupleID);
			if (this.passCondition(this.condition, tempTuple).getData()){
				tuple = tempTuple;
				return true;
			}
		}
		tuple = null;
		return false;		
	}
	
	private boolean hasNextTuple(){
		if(blockSize == 0){
			return false;
		}
		if(tupleID >= tupleSize-1){//读完了一个Block或Block中没有Tuple
			blockID++;
			if (blockID >= blockSize){//没有下一个Tuple了
				return false;
			}else{//读取一个新的Block
				block = table.getBlock(blockID);
				this.tupleSize = block.getTupleSize();
				while(tupleSize == 0){//Block中没有Tuple
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
	*/
	public Tuple getNext() {
		Tuple tempTuple = this.tuple;
		block.removeTuple(this.tupleID);
		return tempTuple;
	}
	
	public void close() {
	}	
	
	public Attribute[] getAttributeList() {
		return this.attributeList;
	}
	/**
	 * 计算条件是否满足,内部递归调用
	 * @param con
	 * @param tempTuple
	 * @return
	 */
	private BooleanItem passCondition(Condition con, Tuple tempTuple){
		if(con == null){
			return BooleanItem.getTrue();
		}
		DataItem item1 = null;
		DataItem item2 = null;
		//访问左子树
		if (con.getCon1() != null && (con.getCon1() instanceof Condition)){
			item1 = passCondition((Condition)(con.getCon1()), tempTuple);
		}else if (con.getCon1() != null ){
			item1 = ObjtoData(con.getCon1(), tempTuple);
		}
		//访问右子树
		if (con.getCon2() != null && (con.getCon2() instanceof Condition)){
			item2 = passCondition((Condition)con.getCon2(), tempTuple);
		}else if (con.getCon2() != null ){
			item2 = ObjtoData(con.getCon2(), tempTuple);
		}
		//计算结果
		if (item1 != null && item2 != null){
			switch(con.getOpr()){
				case Constant.LARGER:{
					return item1.compareTo(item2)>0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.NOTSMALL:{
					return item1.compareTo(item2)>=0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.EQUAL:{
					return item1.equals(item2) ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.NOTLARGE:{
					return item1.compareTo(item2)<=0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.SMALLER:{
					return item1.compareTo(item2)<0 ? BooleanItem.getTrue() : BooleanItem.getFalse(); 
				}
				case Constant.NOTEQUAL:{
					return item1.equals(item2) ? BooleanItem.getFalse() : BooleanItem.getTrue();
				}
				case Constant.AND:{
					if (((BooleanItem)item1).getData() && ((BooleanItem)item2).getData()){
						return BooleanItem.getTrue();
					}else{
						return BooleanItem.getFalse();
					}
				}
				case Constant.OR:{
					if (((BooleanItem)item1).getData() || ((BooleanItem)item2).getData()){
						return BooleanItem.getTrue();
					}else{
						return BooleanItem.getFalse();
					}
				}
				case Constant.LIKE:{
					return BooleanItem.getFalse();
				}
			}
		}
		return null;
	}
	private DataItem ObjtoData(Object obj, Tuple tempTuple){
		if (obj instanceof DataItem){
			//数据常量
			return (DataItem)obj;
		}else{
			//属性变量
			for (int i = 0; i< this.attributeList.length; i++){
				if ( ((Attribute)obj).equals(attributeList[i])){
					return tempTuple.getItem((byte)i);
				}
			}		
		}
		return null;
	}
}
 
