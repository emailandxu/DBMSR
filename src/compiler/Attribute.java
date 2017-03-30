
package compiler;
import recordmanagement.*;
public class Attribute {
	public Table table;
	public byte attIndex;
	public Attribute(Table t, byte b){
		table = t;
		attIndex = b;
	}
	private static Attribute All = new Attribute(null, (byte)-1);
	//返回Singleton对象， *
	public static Attribute GetInstanceAll(){
		return Attribute.All;
	}
	//返回对象Table.*
	public static Attribute GetInstanceAllinTable(Table table){
		return new Attribute(table, (byte)-1);
	}
	public String toString(){
		if(table == null && attIndex == (byte)-1){
			return "*";
		}else if (table != null && attIndex == (byte)-1){
			return (table.getTableName() + ".*" );
		}else{
			return (table.getTableName() + "." + table.getSchema().getAttributeName(attIndex));
		}
		
	}
	public boolean equals(Object att){
		if (att instanceof Attribute){
			if ( ((Attribute)att).table == this.table  && ((Attribute)att).attIndex == this.attIndex){
				return true;
			}
		}
		return false;
	}
}
