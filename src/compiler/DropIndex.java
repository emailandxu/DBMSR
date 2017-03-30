
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
