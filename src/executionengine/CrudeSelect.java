package executionengine;

//import recordmanagement.Schema;
import recordmanagement.Tuple;
import compiler.*;
import dataitem.BooleanItem;
import dataitem.DataItem;

/**
 *蛮力选择，没有使用索引
 *
 */
public class CrudeSelect implements Select {
	private Condition condition = null;
	private Operation belowOpr = null;
	private Attribute[] attributeList = null;
	
	private Tuple tuple = null;
	
	public CrudeSelect(Condition con, Operation opr){
		condition = con;
		belowOpr = opr;
		
		attributeList = belowOpr.getAttributeList();
	}
	
	public void open() {
		belowOpr.open();
	}	 
	
	public boolean hasNext() {
		while (belowOpr.hasNext()){
			Tuple tempTuple = belowOpr.getNext();
			if (this.passCondition(this.condition, tempTuple).getData()){
				tuple = tempTuple;
				return true;
			}
		}
		tuple = null;
		return false;
	}	
	public Tuple getNext() {
		Tuple tempTuple = this.tuple;
		return tempTuple;
	}	 
	
	public void close() {
	}
	
	public Attribute[] getAttributeList() {		
		return this.attributeList;
	}
	
	/**
	 * 计算条件是否满足,内部递归调用
	 * @param con
	 * @param tempTuple
	 * @return
	 */
	private BooleanItem passCondition(Condition con, Tuple tempTuple){
		if(con == null){
			return BooleanItem.getTrue();
		}
		DataItem item1 = null;
		DataItem item2 = null;
		//访问左子树
		if (con.getCon1() != null && (con.getCon1() instanceof Condition)){
			item1 = passCondition((Condition)(con.getCon1()), tempTuple);
		}else if (con.getCon1() != null ){
			item1 = ObjtoData(con.getCon1(), tempTuple);
		}
		//访问右子树
		if (con.getCon2() != null && (con.getCon2() instanceof Condition)){
			item2 = passCondition((Condition)con.getCon2(), tempTuple);
		}else if (con.getCon2() != null ){
			item2 = ObjtoData(con.getCon2(), tempTuple);
		}
		//计算结果
		if (item1 != null && item2 != null){
			switch(con.getOpr()){
				case Constant.LARGER:{
					return item1.compareTo(item2)>0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.NOTSMALL:{
					return item1.compareTo(item2)>=0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.EQUAL:{
					return item1.equals(item2) ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.NOTLARGE:{
					return item1.compareTo(item2)<=0 ? BooleanItem.getTrue() : BooleanItem.getFalse();
				}
				case Constant.SMALLER:{
					return item1.compareTo(item2)<0 ? BooleanItem.getTrue() : BooleanItem.getFalse(); 
				}
				case Constant.NOTEQUAL:{
					return item1.equals(item2) ? BooleanItem.getFalse() : BooleanItem.getTrue();
				}
				case Constant.AND:{
					if (((BooleanItem)item1).getData() && ((BooleanItem)item2).getData()){
						return BooleanItem.getTrue();
					}else{
						return BooleanItem.getFalse();
					}
				}
				case Constant.OR:{
					if (((BooleanItem)item1).getData() || ((BooleanItem)item2).getData()){
						return BooleanItem.getTrue();
					}else{
						return BooleanItem.getFalse();
					}
				}
				case Constant.LIKE:{
					return BooleanItem.getFalse();
				}
			}
		}
		return null;
	}
	private DataItem ObjtoData(Object obj, Tuple tempTuple){
		if (obj instanceof DataItem){
			//数据常量
			return (DataItem)obj;
		}else{
			//属性变量
			for (int i = 0; i< this.attributeList.length; i++){
				if ( ((Attribute)obj).equals(attributeList[i])){
					return tempTuple.getItem((byte)i);
				}
			}		
		}
		return null;
	}
}
 
