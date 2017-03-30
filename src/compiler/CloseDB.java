
package compiler;

import recordmanagement.RecordManagement;


public class CloseDB {
	public boolean FromSQL(String sql){
		sql = sql.trim();
		String[] tempString = sql.split("(\\s*)\\s");
		if (tempString.length > 2){
			StringParser.DisplayError("不正确的语法格式!");
		}
		return true;
	}
	public void close(){
		RecordManagement.getInstance().closeDB();
	}
	public static void main(String[] args) {
	}
}
