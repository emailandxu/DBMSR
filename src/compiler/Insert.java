
package compiler;

import java.text.ParseException;


import dataitem.DataItem;

import recordmanagement.RecordManagement;
import recordmanagement.Table;

public class Insert {
	Table table = null;
	DataItem[] items = null;
	
	public boolean FromSQL(String sql){
		String[] tempString = sql.substring(12).trim().split("(\\s*)(\\s)(V|v)(A|a)(L|l)(U|u)(E|e)(S|s)(\\s|)(\\s*)(\\()");//按Values拆分
		if(tempString.length != 2){
			StringParser.DisplayError("不正确的语法格式!");
			return false;
		}
		tempString[1] = "(" + tempString[1];
		//以下处理values前的字串
		String tempTableString = null;
		String tempAttString = null;
		int i;
		boolean found = false;
		for (i = 0; i<tempString[0].length(); i++){
			if(tempString[0].charAt(i) == '('){
				tempTableString = tempString[0].substring(0,i).trim();
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
		for (; j<tempString[0].length(); j++){
			if(found){
				StringParser.DisplayError("不正确的语法格式!");
				return false;
			}
			if (tempString[0].charAt(j) == '('){
				StringParser.DisplayError("不正确的语法格式!");
				return false;
			}
			if (tempString[0].charAt(j) == ')'){
				tempAttString = tempString[0].substring(i,j).trim();
				found = true;
			}
		}
		String[] attList = tempAttString.split("(\\s*|),(\\s*|)");
		if(attList.length != table.getSchema().getAttributeSize()){
			StringParser.DisplayError("输入的属性列表与表的属性列表不符!");
			return false;
		}
		for (i = 0; i<attList.length; i++){
			if (!attList[i].equals(table.getSchema().getAttributeName((byte)i))){
				StringParser.DisplayError("输入的属性列表与表的属性列表不符!");
				return false;
			}
		}
		//以下处理Values后的字串
		tempString[1] = tempString[1].trim().substring(1,tempString[1].length()-1);
		for(i = 0; i<tempString[1].length(); i++){
			if(tempString[1].charAt(i) == '(' || tempString[1].charAt(i) == ')'){
				StringParser.DisplayError("不正确的值列表!");
				return false;
			}
		}
		String[] valueList = tempString[1].split("(\\s*|),(\\s*|)");
		if (valueList.length != attList.length){
			StringParser.DisplayError("不正确的值列表!");
			return false;
		}
		DataItem[] tempItem = new DataItem[valueList.length];
		try{
			for (i=0; i<tempItem.length; i++){
				tempItem[i] = StringParser.StringToData(valueList[i], new Attribute(table,(byte)i));
			}
		}catch(Exception e){
			StringParser.DisplayError(e.getMessage());
			return false;
		}
		this.items = tempItem;
		return true;
	}
	public void insert(){
		table.insertTuple(items);
	}
	public static void main(String[] args) throws ParseException {
		/*
		String s = "asdf ValUes(asdf)";
		String[] ss = s.split("(\\s)(V|v)(A|a)(L|l)(U|u)(E|e)(S|s)(\\s|)(\\()");
		for (int i = 0; i<ss.length; i++){
			System.out.println(ss[i]);
		}
		*/
		
		RecordManagement recordManagement = RecordManagement.getInstance();
	    recordManagement.loadDB("lflf");
	     
	    String sql = "insert into MovieStar( name, address, gender, birthdate) values('ZhengHao', 'SJTU', 'M', 2005-3-28)";
	     
	    Insert insert = new Insert();
	    if (insert.FromSQL(sql)){
	    	insert.insert();
	    }
	    recordManagement.closeDB();
	    
	}	
}
