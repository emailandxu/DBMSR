
package compiler;


public class AgbUpdate implements Algebra {
	private Attribute[] attList = null;
	private Object[] dataList = null;
	private Algebra belowNode = null;
	
	public AgbUpdate(Attribute[] att, Object[] data, Algebra agb){
		attList = att;
		dataList = data;
		belowNode = agb;
	}
	public Attribute[] getAttList(){
		return attList;
	}
	public Object[] getDataList(){
		return dataList;
	}
	public Algebra getBelowNode(){
		return belowNode;
	}
}
