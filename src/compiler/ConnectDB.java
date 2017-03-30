
package compiler;

import recordmanagement.RecordManagement;

public class ConnectDB {
	String DBName;
	public boolean FromSQL(String sql){
		sql = sql.trim();//ȥ�����˵Ŀո�
		String[] tempString = sql.split("(\\s*)(\\s)"); 
		if (tempString[2] == null){
			StringParser.DisplayError("ȱ�����ݿ���!");
			return false;
		}
		if (tempString.length > 3){
			StringParser.DisplayError("����ȷ������ʽ!");
		}
		for (int i = 0; i<tempString[2].length(); i++){
			if (tempString[2].charAt(i) == '*' 
				|| tempString[2].charAt(i) == '\\'
				|| tempString[2].charAt(i) == '/'
				|| tempString[2].charAt(i) == '?'
			){
				StringParser.DisplayError(tempString[2].charAt(i) + "���ܳ��������ݿ�����!");
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
