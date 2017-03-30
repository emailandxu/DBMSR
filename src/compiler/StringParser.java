package compiler;
import java.text.ParseException;
import dataitem.*;
import recordmanagement.*;
import executionengine.*;

public class StringParser {
	
	private static StringParser  INSTANCE = new StringParser();
	//private Query quety = null;
	private StringParser()
	{}
	
	/**
	 * ��̬��������Singleton����
	 */
	public StringParser GetInstance()
	{
		return INSTANCE;
	}	 
	/**
	 * ����SQL�������������ѯ�ƻ�
	 * @param command
	 * @return
	 */
	public ExecutionPlan parse(String command) {
		return null;
	}
	/**
	 * ����Ƿ��������ΪtableName�ı�
	 * @param tableName
	 * @return
	 */
	public static Table CheckTable(String tableName){
		Table temp = RecordManagement.getInstance().getTable(tableName);
		return temp;
	}
	/**
	 * �������Ƿ��������ΪattName������
	 * @param attName
	 * @param tableList
	 * @return �����������ڱ�����
	 */
	public static byte CheckAttribute(String attName,Table[] tableList){
		Table tempTable;
		RecordManagement manager = RecordManagement.getInstance();
		byte id = -1;
		int i;
		for (i = 0; i<tableList.length; i++){
			tempTable = tableList[i];
			id = tempTable.getSchema().getAttributeIDByName(attName);
			if (id != -1) break;
		}
		if(i == tableList.length) i = -1;
		return (byte)i;
	}
	/*
	/**
	 * ���ַ���ת��ΪDataItem�����Table�����Attribute����
	 * @param s
	 * @return
	 
	public static Object StringToData(String s){
		Object data = null;
		char[] c = s.toCharArray();
		if (c[0] == '_' || c[0] == '\\' || c[0] == '/' || c[0] =='.'){
			//�Ƿ�����ʼ�ַ�
			return null;
		}else if ((c[0] >= '0' && c[0] <= '9')||c[0]=='-'){
			boolean isNumber = true;
			boolean hasDot = false;
			for (int i = 1; i<c.length; i++){
				if ((c[i] > '9' || c[i] < '0')&& c[i]!= '.'){
					isNumber = false;
					break;
				}
				if (c[i] == '.'){
					if (hasDot == false){
						hasDot = true;
					}else{
						DisplayError("�ж��С����");
						return null;
					}					
				}
			}//endfor
			if (isNumber){
				data = StringToNumber(c, hasDot);
			}else{
				DisplayError("���ֲ�����Ϊ����ֵ�����Ŀ�ͷ");
				return null;
			}
		}else{
			if (c[0] == '\'' && c[c.length-1]=='\''){
				data = new StringItem(String.valueOf(c,1,c.length-2));
			}else{
				boolean hasDot = false;
				int i;
				for ( i = 1; i<c.length; i++){					
					if (c[i] == '.'){
						if (hasDot == false){
							hasDot = true;
							break;
						}else{
							DisplayError("�ж��'.'");
							return null;
						}					
					}
				}//endfor
				if (hasDot){
					//Table tempTable = CheckTable(String.valueOf(c,0,i));
					
				}
			}
		}
		return data;
	}
	*/
	
	/**
	 * ���ַ���ת��Ϊ��ֵ,�����ڲ�����
	 * @param c
	 * @param hasDot
	 * @return
	 */
	private static DataItem StringToNumber(char[] c,boolean hasDot){
		DataItem data = null;
		if (!hasDot){
		//������
			boolean underZero;
			long tempNumber = 0;
			int i;
			if (c[0] == '-'){
			//����
				underZero = true;
				i = 1;
			}else{
				underZero = false;
				i = 0;
			}
			for(; i<c.length; i++){
				tempNumber *= 10;
				tempNumber += (c[i] - '0');
			}
			if (underZero){
				tempNumber = -tempNumber;
			}
			if (tempNumber > 2147483647 || tempNumber < -2147483648){
				data = new LongItem(tempNumber);
			}else{
				data = new IntegerItem((int)tempNumber);
			}
		}else{
			boolean underZero;
			double tempNumber = 0;
			int i;
			if (c[0] == '-'){
			//����
				underZero = true;
				i = 1;
			}else{
				underZero = false;
				i = 0;
			}
			for(; c[i] != '.'; i++){
				tempNumber *= 10;
				tempNumber += (c[i] - '0');
			}
			double k = 1;
			for (i++; i<c.length; i++){
				k /= 10;
				tempNumber += ((c[i] - '0')/k);
			}
			if (underZero){
				tempNumber = -tempNumber;
			}
			data = new DoubleItem(tempNumber);
		}
		return data;
	}
	
	/**
	 * ��Stringת��ΪAttribute���飬��*��R.*, R.a���ֺϷ������
	 * @param betweenSF
	 * @return
	 */
	public static Attribute[] StringToAtt(String betweenSF, Table[] fromList){
		String[] sf = betweenSF.split("(\\s*|),(\\s*|)");
		String[] temp = null;//����Table.Attribute�����
		Attribute[] tempAtt = new Attribute[sf.length];
		int i;
		for (i = 0; i<sf.length; i++){
			 temp = sf[i].split("\\.");
			 if (temp.length == 1){//ֱ��д������
			 	if(temp[0].equals("*")){//*
			 		if (sf.length == 1){
			 			tempAtt = new Attribute[1];
			 			tempAtt[0] = Attribute.GetInstanceAll();
			 		}else{
			 			StringParser.DisplayError("* ����������������һ����֣�");
			 			return null;
			 		}				 			
			 	}else if(StringParser.CheckAttribute(temp[0],fromList)== -1){
			 		StringParser.DisplayError("����������Ϊ" + temp[0] + "������!");
			 		return null;
			 	}else{//���Դ���
			 		byte tableIndex = StringParser.CheckAttribute(temp[0],fromList);
			 		byte attIndex = fromList[tableIndex].getSchema().getAttributeIDByName(temp[0]);
			 		tempAtt[i] = new Attribute(fromList[tableIndex], attIndex);
			 	}
			 }else if (temp.length == 2){//Table.Attribute�����
			 	if(temp[0].equals("*")){
			 		StringParser.DisplayError("* ���ܳ����ڱ�����λ���ϣ�");
			 		return null;
			 	}else{//Table.Attribute�����
			 		int j;
			 		for (j = 0 ; j<fromList.length; j++){//
			 			if (temp[0].equals(fromList[j].getTableName())) break;
			 		}
			 		if (j == fromList.length){
			 			StringParser.DisplayError("��" + temp[0] + "����From�б��У�");
			 			return null;
			 		}
			 		if (temp[1] != "*"){//a.b
			 			byte b = StringParser.CheckAttribute(temp[1],fromList);
			 			if (b == -1){
			 				StringParser.DisplayError("����������Ϊ" + temp[1] + "������!");
					 		return null;
			 			}else{
			 				tempAtt[i] = new Attribute(fromList[j],fromList[j].getSchema().getAttributeIDByName(temp[1]));
			 			}
			 		}else{//a.*
			 			tempAtt[i] = Attribute.GetInstanceAllinTable(fromList[j]);
			 		}
			 	}
			 }else{//�����ŹֵĻ�����û�뵽�����
			 	StringParser.DisplayError("���Ϸ����б�!");
			 	return null;
			 }
		}//endfor
		return tempAtt;
	}
	
	/**
	 * ��String����ת��ΪDataItem����
	 * @param forChange
	 * @param att
	 * @return
	 * @throws ParseException
	 */
	public static DataItem StringToData (String forChange, Attribute att) throws ParseException{
		DataItem data = null;
		byte attType = att.table.getSchema().getAttributeType(att.attIndex).getType();
		switch(attType){
			case 1:{//BOOLEAN
				try{
					data = new BooleanItem(forChange);
					break;
				}catch (Exception e) {
		            throw new ParseException(forChange + " is not boolean value", 0);
		        }
			}
			case 2:{//CHAR
				try{
					data = new CharItem(forChange);
					break;
				}catch (Exception e) {
		            throw new ParseException(forChange + " is not boolean value", 0);
		        }
			}
			case 3:{//INTEGER
				try{
					data = new IntegerItem(forChange);
					break;
				}catch(Exception e){
					throw new ParseException (forChange + "is not integer value", 0);
				}
			}
			case 4:{//LONG
				try{
					data = new LongItem(forChange);
					break;
				}catch(Exception e){
					throw new ParseException (forChange + "is not long value", 0);
				}
			}
			case 5:{//DOUBLE
				try{
					data = new DoubleItem(forChange);
					break;
				}catch(Exception e){
					throw new ParseException (forChange + "is not double value", 0);
				}
			}
			case 6:{//TIME
				try{
					data = new TimeItem(forChange);
					break;
				}catch(Exception e){
					throw new ParseException (forChange + "is not time value", 0);
				}
			}
			case 7:{//STRING
				try{
					forChange = forChange.trim();
					if (forChange.charAt(0) != '\'' || forChange.charAt(forChange.length()-1) != '\''){
						throw new Exception();
					}
					data = new StringItem(forChange.substring(1,forChange.length()-1));
					break;
				}catch(Exception e){
					throw new ParseException (forChange + "is not string value", 0);
				}
			}
		}//endSwitch
		return data;
	}
	public static void DisplayError(String error){
		//System.out.println(error);
		throw new IllegalArgumentException(error);
	}
	
	public static Operation SQLtoOperation(String sql){
		String tempSql = sql.trim().toLowerCase();
		
		if(tempSql.startsWith("create database ")){
			CrtDB crtDB = new CrtDB();
			if (crtDB.FromSQL(sql)){
				crtDB.create();
			}
			return null;
		}else if(tempSql.startsWith("connect database ")){
			ConnectDB connectDB = new ConnectDB();
			if (connectDB.FromSQL(sql)){
				connectDB.connect();
			}
			return null;
		}else{
			if (!RecordManagement.getInstance().isDBConnected()){
				StringParser.DisplayError("���ݿ�δ����!");
			}
			if (tempSql.startsWith("select ")){
				SFW sfw = new SFW();
				
				boolean success = sfw.FromSQL(sql + " ");
				AlgebraPlan agbPlan = null;
				Engine engine = null;
				Operation operation = null;
				if(success){
					agbPlan = sfw.ExcuteAgbPlan();
					engine = new Engine();
					try{
						operation = engine.createOperation(agbPlan);
					}catch(Exception e){
						StringParser.DisplayError(e.getMessage());
					}
				}
				return operation;
			}else if(tempSql.startsWith("update ")){
				USW usw = new USW();		
				
				usw.FromSQL(sql);
				
				AlgebraPlan agbPlan = usw.ExcuteAgbPlan();
				Engine engine = new Engine();
				Operation operation = null;
				try{
					operation = engine.createOperation(agbPlan);
				}catch(Exception e){
					System.out.println(e.getMessage());
				}
				return operation;
			}else if(tempSql.startsWith("delete from ")){
				DFW dfw = new DFW();
				
				boolean success = dfw.FromSQL(sql + " ");
				AlgebraPlan agbPlan = null;
				Engine engine = null;
				Operation operation = null;
				if(success){
					agbPlan = dfw.ExcuteAgbPlan();
					engine = new Engine();
					try{
						operation = engine.createOperation(agbPlan);
					}catch(Exception e){
						StringParser.DisplayError(e.getMessage());
					}
				}
				return operation;
			}else if(tempSql.startsWith("create table ")){
				CrtTable crtTable = new CrtTable();
			    if(crtTable.FromSQL(sql)){
			    	crtTable.createTable(RecordManagement.getInstance());
			    }
				return null;
			}else if(tempSql.startsWith("create index ")){
				CrtIndex crtIndex = new CrtIndex();
				if (crtIndex.FromSQL(sql)){
					crtIndex.create();
				}
				return null;
			}else if(tempSql.startsWith("drop table ")){
				DropTable dropTable = new DropTable();
				if(dropTable.FromSQL(sql)){
					dropTable.drop();
				}
				return null;
			}else if(tempSql.startsWith("close database")){
				CloseDB closeDB = new CloseDB();
				if(closeDB.FromSQL(sql)){
					closeDB.close();
				}
				return null;
			}else if(tempSql.startsWith("drop index ")){
				DropIndex dropIndex = new DropIndex();
				if (dropIndex.FromSQL(sql)){
					dropIndex.drop();
				}
				return null;
			}else if(tempSql.startsWith("insert into ")){
				Insert insert = new Insert();
				if (insert.FromSQL(sql)){
					insert.insert();
				}
				return null;
			}
			else{
				throw new IllegalArgumentException("δ֪������ʽ");
			}		
		}
	}
	public static void main (String args[])
	{	
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("lflf");
        
		SFW sfw = new SFW();
		String sql = "Select * From MovieStar where name = 'ZhengHao' ";
		//String sql = "Select * From staff ";
		boolean success = sfw.FromSQL(sql);
		AlgebraPlan agbPlan = null;
		Engine engine = null;
		Operation operation = null;
		if(success){
			agbPlan = sfw.ExcuteAgbPlan();
			engine = new Engine();
			try{
				operation = engine.createOperation(agbPlan);
			}catch(Exception e){
				System.out.println(e.getMessage());
			}
		}
		
		operation.open();
		
		int i=0;
		Tuple tuple = null;
		while(operation.hasNext()){
			tuple = operation.getNext();
	        for (int j = 0; j<operation.getAttributeList().length; j++){
	        	System.out.print(" " + tuple.getItem((byte)j));
	        }
	        System.out.println();
	        i++;
		}
		System.out.println(i);
		recordManagement.closeDB();
		
		System.out.println("Guten Tag, Welt!");	
		
	}
}