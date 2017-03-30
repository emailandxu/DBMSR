
package compiler;

import executionengine.Engine;
import executionengine.Operation;
import recordmanagement.RecordManagement;
import recordmanagement.Table;
import recordmanagement.Tuple;

//Update...Set...Where...��ʽ�����
public class USW implements Query {
	private Table table;
	private Attribute[] attList = null;
	private Object[] dataList = null;
	private Condition condition = null;
	
	public boolean FromSQL(String sql) {
		String[] tempStringList = sql.substring(7).split("(\\s*)(\\s)(((S|s)(E|e)(T|t))|((W|w)(H|h)(E|e)(R|r)(E|e)))(\\s)(\\s*)");
		//������
		tempStringList[0] = tempStringList[0].trim();
		Table tempTable = RecordManagement.getInstance().getTable(tempStringList[0]);
		if (tempTable == null){
			StringParser.DisplayError("��������Ϊ" + tempStringList[0] + "�ı�!");
			return false;
		}
		table = tempTable;
		
		//����Set��ı��ʽ
		tempStringList[1] = tempStringList[1].trim();
		String[] betweenSetWhere = tempStringList[1].split("(\\s*|),(\\s*|)");
		dataList = new Object[betweenSetWhere.length];
		attList = new Attribute[betweenSetWhere.length];
		compileSet(betweenSetWhere);
		
		//����Where��ı��ʽ
		tempStringList[2] = tempStringList[2].trim() + " ";
		if (tempStringList[2].trim()!= null){			
			tempStringList[2] = tempStringList[2].trim() + " ";
			try{
				this.condition = Condition.CompileWhere(tempStringList[2]);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}			
			try{				
				Table[] tempTableList = new Table[1];
				tempTableList[0] = this.table;
				Condition.OprtCondition(this.condition, this.condition,true, tempTableList);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
		}//endIf
		return true;
	}
	private void compileSet(String[] tempList){
		String[] tempFunction;
		Table[] tempTableList = new Table[1];
		tempTableList[0] = this.table;
		Attribute[] tempAtt;
		for (int i = 0 ; i< tempList.length; i++){
			tempFunction = tempList[i].split("(\\s*|)=(\\s*|)");
			tempFunction[0] = tempFunction[0].trim();
			tempAtt = StringParser.StringToAtt(tempFunction[0], tempTableList);
			if (tempAtt.length != 1){
				StringParser.DisplayError("����ȷ������ʽ!");
				return;
			}
			this.attList[i] = tempAtt[0];
			tempFunction[1] = tempFunction[1].trim() +" ";
			Object obj = Caculate.compileFunction(tempFunction[1]);
			if(obj instanceof String){
				this.dataList[i] = obj;
			}else{
				this.dataList[i] = obj;
			}
			try{
				//Table[] tempTableList = new Table[1];
				tempTableList[0] = this.table;
				if(dataList[i] instanceof Caculate){
					Caculate.OprtCaculate((Caculate)dataList[i], (Caculate)dataList[i],true, tempTableList, attList[i]);
				}else{
					try{
						if (attList[i].table.getSchema().getAttributeType(attList[i].attIndex).getType() == 7){
							//�ͱ�ΪString��ʱ��Ҫ�ж���β�Ƿ�Ϊ"'"
							if(((String)dataList[i]).startsWith("'") && ((String)dataList[i]).endsWith("'")){
								//p.con2 = ((String)p.con2).substring(1, ((String)p.con2).length() - 1);
								dataList[i] = StringParser.StringToData((String)(dataList[i]),attList[i]);
							}else{
								throw new Exception(dataList[i] + "�����ַ�����");
							}
						}else{
							dataList[i] = StringParser.StringToData((String)dataList[i], attList[i]);
						}
					}catch(Exception e){
						//�ж�p.con2�Ƿ�Ҳ��Attribute
						Attribute[] temp1 = StringParser.StringToAtt((String)dataList[i],tempTableList);
						if (temp1 != null){
							dataList[i] = temp1[0];
							//p.con1 �� p.con2���ͱ�ͬ
							if(attList[i].table.getSchema().getAttributeType(attList[i].attIndex).getType() != 
								((Attribute)dataList[i]).table.getSchema().getAttributeType(((Attribute)dataList[i]).attIndex).getType()){
								throw new Exception(attList[i].toString() + "��" + ((Attribute)dataList[i]).toString() + "�ǲ�ͬ���ͱ�");
							}
						}else{
							//p.con2����Attribute�������쳣
							throw e;
						}
					}
				}
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
			}
		}		
	}
	
	public AlgebraPlan ExcuteAgbPlan() {
		AgbUpdate agbUpdate= null;
		Sigma sigma = null;
		AgbTable agbTable = null;
		agbTable = new AgbTable(this.table);
		sigma = new Sigma(this.condition, agbTable);
		agbUpdate = new AgbUpdate(this.attList, this.dataList, sigma);
		
		AlgebraPlan plan = new AlgebraPlan(agbUpdate);
		return plan;
	}

	public static void main(String[] args) {
		/*
		String sql = "Update asdf set asdf = 898, wer = 9879897 where dfdcc> sfa";
		String[] tempString = sql.split("(\\s*)(\\s)(((S|s)(E|e)(T|t))|((W|w)(H|h)(E|e)(R|r)(E|e)))(\\s)(\\s*)");
		for (int i = 0; i< tempString.length; i++){
			System.out.println(tempString[i]);
		}
		*/
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("zhzh");
        String sql = "Update staff set  address = 'at' + address where name > 700 ";
        
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
		
	}
}
