package compiler;

public class Optimizer {
	public static Optimizer  optimizer = new Optimizer();
	public static Optimizer getInstance(){
		return optimizer;
	}
	public AlgebraPlan optimize(AlgebraPlan agbPlan) {
		Algebra root = agbPlan.GetRoot();
		Sigma tempSigma = null;
		
		tempSigma = findSigma(root);
		
		return null;
	}
	private Sigma findSigma(Algebra agb){
		if(agb instanceof Sigma){
			return (Sigma)agb;
		}else if (agb instanceof Pi){
			return findSigma (((Pi)agb).GetBelowNode());
		}
		return null;
	}
	 
}
 
