
package compiler;

import recordmanagement.Table;
public class AgbTable implements Algebra{
	private Table table = null;
	
	public AgbTable(Table t){
		table = t;
	}
	public Table GetTable(){
		return table;
	}
}
