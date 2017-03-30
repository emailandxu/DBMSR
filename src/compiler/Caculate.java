
package compiler;


import java.util.LinkedList;
import java.util.List;

import recordmanagement.Table;
import recordmanagement.Tuple;
import dataitem.DataItem;

public class Caculate {
	private Object obj1 = null;
	private Object obj2 = null;
	private byte opr = 0;
	//private int hierarchy;
	public Caculate(int h){
		
	}
	public Caculate(Object o1, Object o2, byte op){
		obj1 = o1;
		obj2 = o2;
		opr = op;
	}
	public Object getObj1(){
		return obj1;
	}
	public Object getObj2(){
		return obj2;
	}
	public byte getOpr(){
		return opr;
	}
	/**
	 * 解析算术表达式,末尾要求有一个空格
	 * @param forChange
	 * @return
	 */
	public static Object compileFunction(String forChange){
		char[] c = forChange.toCharArray();
		List tempStringList = new LinkedList();
		int i,j,k;
		for (i = 0,j = 0,k = 0; i<c.length; i++){
			switch(c[i]){
				case ' ':{
					if (i>j){
						tempStringList.add(k,String.valueOf(c,j,i-j));
						k++;
					}
					j = i+1;
					break;
				}
				case '+':case '-':case '*':case '/':case '(':case ')':
				{
					if (i>j){
						tempStringList.add(k,String.valueOf(c,j,i-j));
						k++;
						tempStringList.add(k,String.valueOf(c,i,1));
						k++;
					}else{
						tempStringList.add(k,String.valueOf(c,i,1));
						k++;
					}
					j = i+1;
					break;
				}
				default:{
					
				}
			}//endswitch
		}//endfor
		tempStringList.add(k,"#");
		String[] stringList = (String[])tempStringList.toArray(new String[0]);
		//两个栈，一个放操作数，一个放操作符
		ObjectStack OPTR = new ObjectStack(50);
		ObjectStack OPND = new ObjectStack(50);
		OPTR.push("#");
		i = 0;
		String tempC = stringList[i++];
		try{
			while(tempC != "#" || !(OPTR.getTop().equals("#"))){
				if(!isOP(tempC)){
					OPND.push(tempC);
					tempC = stringList[i++];
				}else{
					switch(precede((String)OPTR.getTop(),tempC)){
						case '<':{
							OPTR.push(tempC);
							tempC = stringList[i++];
							break;
						}
						case '=':{
							OPTR.pop();
							tempC = stringList[i++];
							break;
						}
						case '>':{
							String theta = (String)OPTR.pop();
							Object b = OPND.pop();
							Object a = OPND.pop();
							byte op = StringtoByteOpr(theta);
							Caculate ca = new Caculate(a,b,op);
							OPND.push(ca);
							break;
						}
					}//switch
				}//if			
			}//while
		}catch(Exception e){
			StringParser.DisplayError("不正确的算术表达式!");
		}
		return OPND.getTop();				
	}
	/**
	 * 对Caculate树做一些处理，型别鉴定
	 * @param con
	 * @param superCon
	 * @param left
	 * @return
	 * @throws Exception
	 */
	public static void OprtCaculate(Caculate cal, Caculate superCal, boolean left, Table[] tableList, Attribute attribute) throws Exception{
		Caculate p,q;
		p = cal;
		q = superCal;
		//判断是否需要消除空子树
		if (p.obj1 != null && p.obj2 == null && p.opr == 0){
			if (left){
				q.obj1 = p.obj1;
			}else{
				q.obj2 = p.obj1;
			}
			p = (Caculate)p.obj1;
		}
		//访问左子树
		if (p.obj1 != null && (p.obj1 instanceof Caculate)){//还有子树，递归调用
			OprtCaculate((Caculate)p.obj1,p,true, tableList, attribute);
		}else if (p.obj1 != null ){// p.con1 instanceof String，到叶子节点了
			//以下进行型别判定和转化
			try{
				if (attribute.table.getSchema().getAttributeType(attribute.attIndex).getType() == 7){
					//型别为String的时候要判断首尾是否为"'"
					if(((String)p.obj1).startsWith("'") && ((String)p.obj1).endsWith("'")){
						//p.con2 = ((String)p.con2).substring(1, ((String)p.con2).length() - 1);
						p.obj1 = StringParser.StringToData((String)(p.obj1),attribute);
					}else{
						throw new Exception(p.obj1 + "不是字符串！");
					}
				}else{
					p.obj1 = StringParser.StringToData((String)p.obj1, attribute);
				}
			}catch(Exception e){
				//判断p.con2是否也是Attribute
				Attribute[] temp1 = StringParser.StringToAtt((String)p.obj1,tableList);
				if (temp1 != null){
					p.obj1 = temp1[0];
					//p.con1 与 p.con2的型别不同
					if(attribute.table.getSchema().getAttributeType(attribute.attIndex).getType() != 
						((Attribute)p.obj1).table.getSchema().getAttributeType(((Attribute)p.obj1).attIndex).getType()){
						throw new Exception(attribute.toString() + "与" + ((Attribute)p.obj1).toString() + "是不同的型别！");
					}
				}else{
					//p.con2不是Attribute，重掷异常
					throw e;
				}
			}
		}
		//访问右子树
		if (p.obj2 != null && (p.obj2 instanceof Caculate)){
			OprtCaculate((Caculate)p.obj2,p,false, tableList, attribute);
		}else if (p.obj2 != null ){
			try{
				if (attribute.table.getSchema().getAttributeType(attribute.attIndex).getType() == 7){
					//型别为String的时候要判断首尾是否为"'"
					if(((String)p.obj2).startsWith("'") && ((String)p.obj2).endsWith("'")){
						//p.con2 = ((String)p.con2).substring(1, ((String)p.con2).length() - 1);
						p.obj2 = StringParser.StringToData((String)(p.obj2),attribute);
					}else{
						throw new Exception(p.obj2 + "不是字符串！");
					}
				}else{
					p.obj2 = StringParser.StringToData((String)p.obj2, attribute);
				}
			}catch(Exception e){
				//判断p.con2是否也是Attribute
				Attribute[] temp2 = StringParser.StringToAtt((String)p.obj2,tableList);
				if (temp2 != null){
					p.obj2 = temp2[0];
					//p.con1 与 p.con2的型别不同
					if(attribute.table.getSchema().getAttributeType(attribute.attIndex).getType() != 
						((Attribute)p.obj2).table.getSchema().getAttributeType(((Attribute)p.obj2).attIndex).getType()){
						throw new Exception(attribute.toString() + "与" + ((Attribute)p.obj2).toString() + "是不同的型别！");
					}
				}else{
					//p.con2不是Attribute，重掷异常
					throw e;
				}
			}
		}//结束访问右子树		
	}
	public DataItem getResult(Tuple tuple){
		
		DataItem item1 = null;
		DataItem item2 = null;
		//访问左子树
		if (this.getObj1() != null && (this.getObj1() instanceof Caculate)){
			item1 = ((Caculate)this.getObj1()).getResult(tuple);
		}else if (this.getObj1() != null && this.getObj1() instanceof Attribute){
			item1 = tuple.getItem(((Attribute)this.getObj1()).attIndex);
		}else if (this.getObj1() != null){
			item1 = (DataItem)this.getObj1();
		}
		//访问右子树
		if (this.getObj2() != null && (this.getObj2() instanceof Caculate)){
			item2 = ((Caculate)this.getObj2()).getResult(tuple);
		}else if (this.getObj2() != null && this.getObj2() instanceof Attribute){
			item2 = tuple.getItem(((Attribute)this.getObj2()).attIndex);
		}else if (this.getObj2() != null){
			item2 = (DataItem)this.getObj2();
		}
		//计算结果
		if (item1 != null && item2 != null){
			switch(this.getOpr()){
				case Constant.PLUS:{
					return item1.add(item2);
				}
				case Constant.MINUS:{
					return item1.substract(item2);
				}
				case Constant.MULTIPLY:{
					return item1.multiply(item2);
				}
				case Constant.DIVIDE:{
					return item1.divide(item2);
				}
			}
		}
		return null;
	}
	
	private static byte StringtoByteOpr(String tempC){
		if (tempC.length() != 1){
			throw new IllegalArgumentException();
		}
		char c = tempC.charAt(0);
		switch(c){
			case '+':{
				return Constant.PLUS;
			}
			case '-':{
				return Constant.MINUS;
			}
			case '*':{
				return Constant.MULTIPLY;
			}
			case '/':{
				return Constant.DIVIDE;
			}
			case '(':{
				return Constant.LBRACKET;
			}
			case ')':{
				return Constant.RBRACKET;
			}
		}
		throw new IllegalArgumentException();
	}
	/**
	 * 算符间的优先关系
	 * @param optr
	 * @param tempC
	 * @return
	 */
	private static char precede(String optr, String tempC){
		char op = optr.charAt(0);
		char c = tempC.charAt(0);
		switch(op){
			case '+':{
				switch(c){
					case '+':case '-':case ')':case '#':{
						return '>';
					}
					case '*':case '/':case '(':{
						return '<';
					}
				}
				break;
			}
			case '-':{
				switch(c){
					case '+':case '-':case ')':case '#':{
						return '>';
					}
					case '*':case '/':case '(':{
						return '<';
					}
				}
				break;
			}
			case '*':{
				switch(c){
					case '+':case '-':case '*':case '/':case ')':case '#':{
						return '>';
					}
					case '(':{
						return '<';
					}
				}
				break;
			}
			case '/':{
				switch(c){
					case '+':case '-':case '*':case '/':case ')':case '#':{
						return '>';
					}
					case '(':{
						return '<';
					}
				}
				break;
			}
			case '(':{
				switch(c){
					case '+':case '-':case '*':case '/':case '(':{
						return '<';
					}
					case ')':{
						return '=';
					}
					case '#':{
						throw new IllegalArgumentException();
					}
				}
				break;
			}
			case ')':{
				switch(c){
					case '+':case '-':case '*':case '/':case ')':case '#':{
						return '>';
					}
					case '(':{
						throw new IllegalArgumentException();
					}
				}
				break;
			}
			case '#':{
				switch(c){
					case '+':case '-':case '*':case '/':case '(':{
						return '<';
					}
					case ')':{
						throw new IllegalArgumentException();
					}
					case '#':{
						return '=';
					}
				}
				break;
			}
		}
		throw new IllegalArgumentException();	
	}
	/**
	 * 判断是否是操作符
	 * @param tempC
	 * @return
	 */
	private static boolean isOP(String tempC){
		char[] c = tempC.toCharArray();
		if (c.length != 1){
			return false;
		}
		switch(c[0]){
			case '+':case '-':case '*':case '/':case '(':case ')':case '#':{
				return true;
			}
			default:{
				return false;
			}
		}
	}
	public String toString(){
		return this.obj1.toString() + this.opr + this.obj2.toString();
	}
	public boolean equals(Object obj){
		if(!(obj instanceof Caculate)){
			return false;
		}
		if (this == obj){
			return true;
		}else{
			return false;
		}
	}
	public static void main(String[] args) {
		//String sql = "1 + 2 * (9*8+1)*6 +(4+ 5*3) ";
		String sql = "1 + 5/7 ";
		//Caculate ca = Caculate.compileFunction(sql);
		System.out.println();
	}
}
