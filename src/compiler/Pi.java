
package compiler;

/**
 * Pi ͶӰ
 */
public class Pi implements Algebra {
	private Attribute[] list = null;
	private Algebra belowNode = null;
	
	public Pi (){
	}
	public Pi (Attribute[] attList, Algebra node){
		list = attList;
		belowNode = node;
	}
	public Attribute[] GetList(){
		return list;
	}
	public Algebra GetBelowNode(){
		return belowNode;
	}
}
