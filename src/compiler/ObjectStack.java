
package compiler;

public class ObjectStack {
	Object[] stack;
	int i;
	public ObjectStack(int length){
		stack = new Object[length];
		i = 0;
	}
	public Object getTop(){
		if (i == 0){
			return null;
		}
		return stack[i-1];
	}
	public Object pop(){
		if(i == 0){
			return null;
		}
		i--;
		return stack[i];
	}
	public boolean push(Object obj){
		if(i >= stack.length ){
			return false;
		}else{
			stack[i] = obj;
			i++;
			return true;
		}
	}
}
