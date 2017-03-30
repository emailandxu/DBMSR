
package compiler;

import recordmanagement.RecordManagement;
import recordmanagement.Table;

public class DropTable {
	
	Table table;
	public boolean FromSQL(String sql){
		String tempString = sql.substring(10).trim();
		table = StringParser.CheckTable(tempString);
		if (table == null){
			StringParser.DisplayError("不存在名为" + tempString + "的表!");
			return false;
		}
		return true;
	}
	public void drop(){
		RecordManagement.getInstance().removeTable(table.getTableName());
	}
	public static void main(String[] args) {
		RecordManagement recordManagement = RecordManagement.getInstance();
	    recordManagement.loadDB("zhzh");
		
	    String sql = "Drop Table MovieStar";
	    DropTable dropTable = new DropTable();
	    if(dropTable.FromSQL(sql)){
	    	dropTable.drop();
	    }
	    recordManagement.closeDB();
	}
}
