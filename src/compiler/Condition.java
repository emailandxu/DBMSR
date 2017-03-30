
package compiler;

import recordmanagement.Table;

//SFW中WHERE后的Condition
public class Condition {
	Object con1;
	Object con2;
	byte opr;
	
	public Object getCon1(){
		return con1;
	}
	public Object getCon2(){
		return con2;
	}
	public byte getOpr(){
		return opr;
	}
	
	/**
	 * 处理Where后的字串,递归调用
	 * @param w
	 * @return
	 * @throws Exception
	 */
	public static Condition CompileWhere (String w) throws Exception{
		Condition p,q,r;//p指向当前操作的节点，q指向根节点，r为临时引用
		p = new Condition ();
		q = p;
		String s = null;
		boolean b = false;//false表示左子树为空
		char[] c = w.toCharArray();
		int i,j;
		for (i = 0,j = i; i < c.length; i++){
			switch(c[i]){
				case ' ':{
					if(i > j){
						s = String.valueOf(c,j,i-j);
						if (b == false){
							p.con1 = s;
							b = true;
						}else{
							p.con2 = s;
							b = false;
						}
					}
					j = i + 1;
					break;
				}
				case '>':{
					if (c[i+1] == '='){
						//>=
						p.opr = Constant.NOTSMALL;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}						
						i++;
					}else{
						//>
						p.opr = Constant.LARGER;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}												
					}
					j = i + 1;
					break;
				}
				case '<':{
					if (c[i+1] == '='){
						//<=
						p.opr = Constant.NOTLARGE;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}						
						i++;
					}else if (c[i+1] == '>'){
						//<>
						p.opr = Constant.NOTEQUAL;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}						
						i++;
					}else{
						//<
						p.opr = Constant.SMALLER;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}												
						i++;
					}
					j = i + 1;
					break;
				}
				case '=':{
					p.opr = Constant.EQUAL;
					if (b == false){
						s = String.valueOf(c,j,i-j);
						p.con1 = s;
						b = true;
					}					
					j = i + 1;
					break;
				}
				case 'L':case'l':{
					//LIKE
					if ( String.valueOf(c,i,4).toUpperCase().equals("LIKE") && (c[i+4]==' ')||(c[i+4]==',')&&(c[i-1]==' ')){
						p.opr = Constant.LIKE;
						if (b == false){
							s = String.valueOf(c,j,i-j);
							p.con1 = s;
							b = true;
						}
						i += 4;
						j = i + 1;
					}
				}
				case 'A':case 'a':{
					//AND
					if ( (c[i+1]=='n'||c[i+1]=='N')&&(c[i+2]=='d'||c[i+2]=='D')&&(c[i-1]==' ')&&(c[i+3]==' ')){
						if (b == true){
							//此时为简单操作，只需为p的右子树赋值
							p.opr = Constant.AND;
							i += 3;
							j = i + 1;
						}else{
							//此时为复杂操作，需新建一个根节点，将其左子树设为p，q指向根节点，p指向q的右子树
							r = q;
							q = new Condition();
							q.con1 = r;
							q.opr = Constant.AND;
							p = new Condition();
							q.con2 = p;
							//p = q;
							//b = true;
							i += 3;
							j = i + 1;
						}
					}
					break;
				}
				case 'O':case 'o':{
					//AND
					if (( c[i+1]=='r'||c[i+1]=='R')&&(c[i-1]==' ')&&(c[i+2]==' ')){
						if (b == true){
							//此时为简单操作，只需为p的右子树赋值
							p.opr = Constant.OR;
							i += 2;
							j = i + 1;
						}else{
							//此时为复杂操作，需新建一个根节点，将其左子树设为p，q指向根节点，p指向q的右子树
							r = q;
							q = new Condition();
							q.con1 = r;
							q.opr = Constant.OR;
							p = new Condition();
							q.con2 = p;
							//p = q;
							//b = true;
							i += 2;
							j = i + 1;
						}
					}
					break;
				}
				case '(':{
					int num = 1;
					int k = i+1;
					if (p.con1 != null && p.con2 != null){
						//StringParser.DisplayError("缺少运算符！");
						Exception e = new Exception("缺少运算符！");
						throw e;
						//break;
					}
					for (; k < c.length; k++){
						if (c[k] == ')'){
							num--;
							if(num == 0){
								String temp = String.valueOf(c,i+1,k-i-1);
								temp += " ";
								Condition con = CompileWhere(temp);
								if (p.con1 == null){
									p.con1 = con;
								}else if (p.con2 == null){
									p.con2 = con;
								}//endif
								break;
							}//endif
						}else if (c[k] == '('){
							num++;
						}
					}//endfor
					if (k == c.length){
						//StringParser.DisplayError("未匹配的括号!");
						Exception e = new Exception("未匹配的括号!");
						throw e;
					}else{
						i = k;
						j = i+1;
					}					
					break;
				}
				case ')':{
					//StringParser.DisplayError("未匹配的括号!");
					Exception e = new Exception("未匹配的括号!");
					throw e;
				}
			}//endSwitch			
		}//endFor
		return q;
	}
	
	/**
	 * 对Condition树做一些处理，型别鉴定，消除空子树
	 * @param con
	 * @param superCon
	 * @param left
	 * @return
	 * @throws Exception
	 */
	public static void OprtCondition(Condition con, Condition superCon, boolean left, Table[] tableList) throws Exception{
		Condition p,q;
		p = con;
		q = superCon;
		//判断是否需要消除空子树
		if (p.con1 != null && p.con2 == null && p.opr == 0){
			if (left){
				q.con1 = p.con1;
			}else{
				q.con2 = p.con1;
			}
			p = (Condition)p.con1;
		}
		//访问左子树
		if (p.con1 != null && (p.con1 instanceof Condition)){//还有子树，递归调用
			OprtCondition((Condition)p.con1,p,true, tableList);
		}else if (p.con1 != null ){// p.con1 instanceof String，到叶子节点了
			//以下进行型别判定和转化
			Attribute[] temp = StringParser.StringToAtt((String)p.con1,tableList);
			if(temp != null) {
			p.con1 = temp[0];
			}
		}
		//访问右子树
		if (p.con2 != null && (p.con2 instanceof Condition)){
			OprtCondition((Condition)p.con2,p,false, tableList);
		}else if (p.con2 != null ){
			try{
				if (((Attribute)p.con1).table.getSchema().getAttributeType(((Attribute)p.con1).attIndex).getType() == 7){
					//型别为String的时候要判断首尾是否为"'"
					if(((String)p.con2).startsWith("'") && ((String)p.con2).endsWith("'")){
						//p.con2 = ((String)p.con2).substring(1, ((String)p.con2).length() - 1);
						p.con2 = StringParser.StringToData((String)(p.con2),(Attribute)(p.con1));
					}else{
						throw new Exception(p.con2 + "不是字符串！");
					}
				}else{
					p.con2 = StringParser.StringToData((String)p.con2, ((Attribute)p.con1));
				}
			}catch(Exception e){
				//判断p.con2是否也是Attribute
				Attribute[] temp2 = StringParser.StringToAtt((String)p.con2,tableList);
				if (temp2 != null){
					p.con2 = temp2[0];
					//p.con1 与 p.con2的型别不同
					if(((Attribute)p.con1).table.getSchema().getAttributeType(((Attribute)p.con1).attIndex).getType() != 
						((Attribute)p.con2).table.getSchema().getAttributeType(((Attribute)p.con2).attIndex).getType()){
						throw new Exception(((Attribute)p.con1).toString() + "与" + ((Attribute)p.con2).toString() + "是不同的型别！");
					}
				}else{
					//p.con2不是Attribute，重掷异常
					throw e;
				}
			}
		}//结束访问右子树		
	}
	
}
