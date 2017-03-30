
package compiler;

import recordmanagement.AttributeType;
import recordmanagement.RecordManagement;
import recordmanagement.Table;

public class CrtTable {
	private String tableName;
	private String[] attNameList;
	private AttributeType[] attTypeList;
	private byte primaryKey = -1;
	public boolean FromSQL(String sql) {
		int i;
		sql = sql.trim();//去除两端的空格
		String[] tempList = null; 
		for (i = 13; i<sql.length(); i++){
			if(sql.charAt(i) == ' ')
				continue;
			else if (sql.charAt(i) == '(' ){
				if (sql.charAt(sql.length()-1) != ')'){
					StringParser.DisplayError("未匹配的括号!");
					return false;
				}
				String tempName = sql.substring(13,i).trim();
				if (StringParser.CheckTable(tempName) != null){
					StringParser.DisplayError("表" + tempName + "已存在!");
					return false;
				}
				this.tableName = tempName;
				String tempString = sql.substring(i+1, sql.length() - 1);
				tempString = tempString.trim();
				tempList = tempString.split("(\\s*|)(\\s|,)(\\s*|)");
				break;
			}
		}
		if (tempList == null){
			StringParser.DisplayError("缺少属性列表!");
			return false;
		}
		if ( (tempList.length % 2) != 0){
			StringParser.DisplayError("缺少属性名或类型!");
			return false;
		}
		boolean hasKey = false;
		for (i = 0; i<tempList.length; i+=2){
			if (tempList[i].toLowerCase().equals("primary") && tempList[i+1].toLowerCase().equals("key")){
				if(!hasKey){
					hasKey = true;
				}else{
					StringParser.DisplayError("只能有一个主键!");
					return false;
				}
			}
		}
		if(!hasKey){
			this.attNameList = new String[tempList.length / 2];
			this.attTypeList = new AttributeType[tempList.length / 2];
		}else{
			this.attNameList = new String[tempList.length / 2 - 1];
			this.attTypeList = new AttributeType[tempList.length / 2 - 1];
		}
		int j;
		for (i = 0, j = 0; i<tempList.length; i += 2, j++){
			try{
				if (tempList[i].toLowerCase().equals("primary") && tempList[i+1].toLowerCase().equals("key")){
					this.primaryKey = (byte)(--j);
				}else{
					attNameList[j] = tempList[i];
					attTypeList[j] = StringtoAttributeType(tempList[i+1]);
				}
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	private AttributeType StringtoAttributeType(String toChange) throws Exception{
		toChange = toChange.toLowerCase();
		if (toChange.equals("boolean")){
			return AttributeType.getTypeBoolean();
		}else if (toChange.equals("double")){			 
			return AttributeType.getTypeDouble();
		}else if(toChange.equals("int")){
			return AttributeType.getTypeInteger();
		}else if(toChange.equals("long")){
			return AttributeType.getTypeLong();
		}else if(toChange.equals("time")){
			return AttributeType.getTypeTime();
		}else if(toChange.startsWith("string")){
			int start = -1, end = -1;
			int i = 0;
			boolean found = false;
			for (; i<toChange.length(); i++){
				if (toChange.charAt(i) == '('){
					if (found){
						throw new Exception("多余的括号!");
					}else{
						found = true;
					}
				}
				if (toChange.charAt(i)<='9' && toChange.charAt(i)>='0'){
					if(found){
						start = i;
						break;
					}else{
						throw new Exception("缺少括号!");
					}
				}
			}
			for (; i<toChange.length(); i++){
				if (toChange.charAt(i) == ' ' || toChange.charAt(i) == ')'){
					end = i;
					break;
				}
			}
			if (start != -1 && end != -1){
				byte b = Byte.valueOf(toChange.substring(start, end)).byteValue();
				return AttributeType.getTypeString(b);
			}else{
				throw new Exception("错误的属性类型设置!");
			}			
		}else{
			throw new Exception(toChange + "是未定义的数据类型!");
		}
	}
	public Table createTable(RecordManagement recordManagement){
		
		Table table = recordManagement.createTable(tableName, (byte)attNameList.length,
                primaryKey, attNameList, attTypeList);
		return table;
	}
	public static void main (String args[]){
		/*
		String s = " abc,bde";
		s = s.trim();
		String[] ss = s.split("(\\s|)(\\s|,)(\\s|)");
		for (int i = 0; i<ss.length; i++){
			System.out.println(ss[i]);
		}
		*/
		/*
		String sql = "Create Table MovieStar(name String(30), address String(255), gender String(1), birthdate Time)";
		System.out.println(sql.substring(13,22).trim());
		*/
		
		RecordManagement recordManagement = RecordManagement.getInstance();
	    recordManagement.loadDB("lflf");
		
	    String sql = "Create Table MovieStar(name String(30), address String(50), gender String(1), birthdate Time)";
	    CrtTable crtTable = new CrtTable();
	    if(crtTable.FromSQL(sql)){
	    	crtTable.createTable(recordManagement);
	    }
	    recordManagement.closeDB();
	}
}
