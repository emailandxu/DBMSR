
package compiler;

import recordmanagement.RecordManagement;


public class CloseDB {
	public boolean FromSQL(String sql){
		sql = sql.trim();
		String[] tempString = sql.split("(\\s*)\\s");
		if (tempString.length > 2){
			StringParser.DisplayError("����ȷ���﷨��ʽ!");
		}
		return true;
	}
	public void close(){
		RecordManagement.getInstance().closeDB();
	}
	public static void main(String[] args) {
	}
}
