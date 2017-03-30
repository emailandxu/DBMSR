
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
		boolean found = false;//�Ƿ��ҵ�'('
		int i;
		for (i = 0; i<tempString.length(); i++){
			if(tempString.charAt(i) == '('){
				tempTableString = tempString.substring(0,i).trim();
				found = true;
				break;
			}
		}
		if(!found){
			StringParser.DisplayError("����ȷ���﷨��ʽ!");
			return false;
		}
		table = RecordManagement.getInstance().getTable(tempTableString);
		if (table == null){
			StringParser.DisplayError("��������Ϊ" + tempTableString + "�ı�!");
			return false;
		}
		int j = ++i;
		found = false;
		for (; j<tempString.length(); j++){
			if(found){
				StringParser.DisplayError("����ȷ���﷨��ʽ!");
				return false;
			}
			if (tempString.charAt(j) == '('){
				StringParser.DisplayError("����ȷ���﷨��ʽ!");
				return false;
			}
			if (tempString.charAt(j) == ')'){
				tempAttString = tempString.substring(i,j).trim();
				found = true;
			}
		}
		byte b = this.table.getSchema().getAttributeIDByName(tempAttString);
		if (b == -1){
			StringParser.DisplayError("��������Ϊ" + tempAttString + "������!");
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
		if(tupleID == tupleSize-1){//������һ��Block
			blockID++;
			if (blockID == blockSize){//û����һ��Tuple��
				return false;
			}else{//��ȡһ���µ�Block
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
