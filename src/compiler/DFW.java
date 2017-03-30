
package compiler;

import executionengine.Engine;
import executionengine.Operation;
import recordmanagement.RecordManagement;
import recordmanagement.Table;
import recordmanagement.Tuple;

//Delete...From...Where... ��ʽ����� 
public class DFW implements Query{
	private Table table;
	Condition condition;
	
	public boolean FromSQL(String sql){
		String[] tempString = sql.substring(12).trim().split("(\\s*)(\\s)(W|w)(H|h)(E|e)(R|r)(E|e)(\\s|)(\\s*)");//��Where���
		if(tempString.length != 2){
			StringParser.DisplayError("����ȷ�����!");
			return false;
		}
		tempString[0] = tempString[0].trim();
		tempString[1] = tempString[1].trim() + " ";
		Table tempTable = RecordManagement.getInstance().getTable(tempString[0]);
		if (tempTable == null){
			StringParser.DisplayError("��������Ϊ" + tempString[0] + "�ı�!");
			return false;
		}
		this.table = tempTable;
		if (tempString[1] != null){			
			try{
				this.condition = Condition.CompileWhere(tempString[1]);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
			//TraverseCondition(condition);
			try{
				//��Condition����һЩ�����ͱ����������������
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
	
	public AlgebraPlan ExcuteAgbPlan() {
		Algebra agb = new AgbDelete(this.table,this.condition);
		return new AlgebraPlan(agb);
	}
	public static void main(String[] args) {
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("zhzh");
		
        String sql = "Delete from staff where name > 450 ";
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
