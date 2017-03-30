
package compiler;

/**
 * Sigma Ñ¡Ôñ£¬Ìõ¼þ
 */
public class Sigma implements Algebra {
	private Condition condition = null;
	private Algebra belowNode = null;
	
	public Sigma(){
	}
	/**
	 * 
	 * @param con
	 * @param node
	 */
	public Sigma(Condition con, Algebra node){
		condition = con;
		belowNode = node;
	}
	public Condition GetCondition(){
		return condition;
	}
	public Algebra GetBelowNode(){
		return belowNode;
	}
}
