
package compiler;

import recordmanagement.RecordManagement;
import recordmanagement.Table;

public class DropIndex {
	private Table table = null;
	private byte order;
	
	public boolean FromSQL(String sql){
		sql = sql.trim();
		String tempString = sql.substring(14).trim();
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
	public void drop(){
		table.dropIndex(this.order);
	}
	public static void main(String[] args) {
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("lflf");
        
        String sql = "Drop Index On MovieStar ( address ) ";
        
        DropIndex dropIndex = new DropIndex();
        if (dropIndex.FromSQL(sql)){
        	dropIndex.drop();
        }
        
        recordManagement.closeDB();
	}
}
