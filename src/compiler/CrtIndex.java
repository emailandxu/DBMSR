
package compiler;

import buffer.DataBlock;
import recordmanagement.RecordManagement;
import recordmanagement.Table;
import recordmanagement.Tuple;

public class CrtIndex {
	private Table table = null;
	private byte order;
	
	private int blockSize;
	private int tupleSize;
	private int blockID;
	private short tupleID;
	private DataBlock block;
	
	public boolean FromSQL(String sql){
		sql = sql.trim();
		String tempString = sql.substring(16).trim();
		String tempTableString = null;
		String tempAttString = null;
		boolean found = false;//是否找到'('
		int i;
		for (i = 0; i<tempString.length(); i++){
			if(tempString.charAt(i) == '('){
				tempTableString = tempString.substring(0,i).trim();
				found = true;
				break;
			}
		}
		if(!found){
			StringParser.DisplayError("不正确的语法格式!");
			return false;
		}
		table = RecordManagement.getInstance().getTable(tempTableString);
		if (table == null){
			StringParser.DisplayError("不存在名为" + tempTableString + "的表!");
			return false;
		}
		int j = ++i;
		found = false;
		for (; j<tempString.length(); j++){
			if(found){
				StringParser.DisplayError("不正确的语法格式!");
				return false;
			}
			if (tempString.charAt(j) == '('){
				StringParser.DisplayError("不正确的语法格式!");
				return false;
			}
			if (tempString.charAt(j) == ')'){
				tempAttString = tempString.substring(i,j).trim();
				found = true;
			}
		}
		byte b = this.table.getSchema().getAttributeIDByName(tempAttString);
		if (b == -1){
			StringParser.DisplayError("不存在名为" + tempAttString + "的属性!");
			return false;
		}
		this.order = b;
		return true;
	}
	public void create(){
		table.createIndex(order);
		this.open();
		if (this.hasNext()){
			Tuple tuple = this.getNext();
			table.getSchema().getAttributeIndex(this.order).insert(tuple.getItem(this.order),this.blockID);
		}
	}	
	private void open() {
		blockID = 0;
		tupleID = -1;
		block = table.getBlock(blockID);
		tupleSize = block.getTupleSize();
	}
	private boolean hasNext() {
		if(blockSize == 0){
			return false;
		}
		if(tupleID == tupleSize-1){//读完了一个Block
			blockID++;
			if (blockID == blockSize){//没有下一个Tuple了
				return false;
			}else{//读取一个新的Block
				block = table.getBlock(blockID);
				tupleSize = block.getTupleSize();
				tupleID = 0;
			}
		}else{
			tupleID++;
		}
		return true;
	}
	private Tuple getNext() {
		return block.getTuple(tupleID);
	}
	public static void main(String[] args) {		
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("lflf");
        
        String sql = "Create Index On MovieStar ( address ) ";
        
        CrtIndex crtIndex = new CrtIndex();
        if (crtIndex.FromSQL(sql)){
        	crtIndex.create();
        }
        
        recordManagement.closeDB();
	}
}
