
package compiler;

import recordmanagement.Table;

public class AgbDelete implements Algebra {
	private Table table;
	private Condition condition;
	
	public AgbDelete(Table t, Condition con){
		table = t;
		condition = con;
	}
	public Table getTable(){
		return table;
	}
	public Condition getCondition(){
		return condition;
	}
}
