
package compiler;

import recordmanagement.RecordManagement;

public class ConnectDB {
	String DBName;
	public boolean FromSQL(String sql){
		sql = sql.trim();//去除两端的空格
		String[] tempString = sql.split("(\\s*)(\\s)"); 
		if (tempString[2] == null){
			StringParser.DisplayError("缺少数据库名!");
			return false;
		}
		if (tempString.length > 3){
			StringParser.DisplayError("不正确的语句格式!");
		}
		for (int i = 0; i<tempString[2].length(); i++){
			if (tempString[2].charAt(i) == '*' 
				|| tempString[2].charAt(i) == '\\'
				|| tempString[2].charAt(i) == '/'
				|| tempString[2].charAt(i) == '?'
			){
				StringParser.DisplayError(tempString[2].charAt(i) + "不能出现在数据库名中!");
				return false;
			}
		}
		DBName = tempString[2];
		return true;
	}
	public void connect(){
		RecordManagement.getInstance().loadDB(this.DBName);
	}
	public static void main(String[] args) {
		RecordManagement recordManagement = RecordManagement.getInstance();
		
		String sql = "Connect DataBase lflf ";
		ConnectDB connectDB = new ConnectDB();
		if (connectDB.FromSQL(sql)){
			connectDB.connect();
		}
		
	    recordManagement.closeDB();
	}
}
