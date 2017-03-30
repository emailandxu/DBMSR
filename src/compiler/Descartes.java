
package compiler;

/**
 * µÑ¿¨¶û»ý
 */
public class Descartes implements Algebra {
	private Algebra agb1;
	private Algebra agb2;
	
	public Descartes(){
	}
	public Descartes(Algebra t1, Algebra t2){
		agb1 = t1;
		agb2 = t2;
	}
	public Algebra GetAgb1(){
		return agb1;
	}
	public Algebra GetAgb2(){
		return agb2;
	}
}
