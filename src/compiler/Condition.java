
package compiler;

import recordmanagement.Table;

//SFW��WHERE���Condition
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
	 * ����Where����ִ�,�ݹ����
	 * @param w
	 * @return
	 * @throws Exception
	 */
	public static Condition CompileWhere (String w) throws Exception{
		Condition p,q,r;//pָ��ǰ�����Ľڵ㣬qָ����ڵ㣬rΪ��ʱ����
		p = new Condition ();
		q = p;
		String s = null;
		boolean b = false;//false��ʾ������Ϊ��
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
							//��ʱΪ�򵥲�����ֻ��Ϊp����������ֵ
							p.opr = Constant.AND;
							i += 3;
							j = i + 1;
						}else{
							//��ʱΪ���Ӳ��������½�һ�����ڵ㣬������������Ϊp��qָ����ڵ㣬pָ��q��������
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
							//��ʱΪ�򵥲�����ֻ��Ϊp����������ֵ
							p.opr = Constant.OR;
							i += 2;
							j = i + 1;
						}else{
							//��ʱΪ���Ӳ��������½�һ�����ڵ㣬������������Ϊp��qָ����ڵ㣬pָ��q��������
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
						//StringParser.DisplayError("ȱ���������");
						Exception e = new Exception("ȱ���������");
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
						//StringParser.DisplayError("δƥ�������!");
						Exception e = new Exception("δƥ�������!");
						throw e;
					}else{
						i = k;
						j = i+1;
					}					
					break;
				}
				case ')':{
					//StringParser.DisplayError("δƥ�������!");
					Exception e = new Exception("δƥ�������!");
					throw e;
				}
			}//endSwitch			
		}//endFor
		return q;
	}
	
	/**
	 * ��Condition����һЩ�����ͱ����������������
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
		//�ж��Ƿ���Ҫ����������
		if (p.con1 != null && p.con2 == null && p.opr == 0){
			if (left){
				q.con1 = p.con1;
			}else{
				q.con2 = p.con1;
			}
			p = (Condition)p.con1;
		}
		//����������
		if (p.con1 != null && (p.con1 instanceof Condition)){//�����������ݹ����
			OprtCondition((Condition)p.con1,p,true, tableList);
		}else if (p.con1 != null ){// p.con1 instanceof String����Ҷ�ӽڵ���
			//���½����ͱ��ж���ת��
			Attribute[] temp = StringParser.StringToAtt((String)p.con1,tableList);
			if(temp != null) {
			p.con1 = temp[0];
			}
		}
		//����������
		if (p.con2 != null && (p.con2 instanceof Condition)){
			OprtCondition((Condition)p.con2,p,false, tableList);
		}else if (p.con2 != null ){
			try{
				if (((Attribute)p.con1).table.getSchema().getAttributeType(((Attribute)p.con1).attIndex).getType() == 7){
					//�ͱ�ΪString��ʱ��Ҫ�ж���β�Ƿ�Ϊ"'"
					if(((String)p.con2).startsWith("'") && ((String)p.con2).endsWith("'")){
						//p.con2 = ((String)p.con2).substring(1, ((String)p.con2).length() - 1);
						p.con2 = StringParser.StringToData((String)(p.con2),(Attribute)(p.con1));
					}else{
						throw new Exception(p.con2 + "�����ַ�����");
					}
				}else{
					p.con2 = StringParser.StringToData((String)p.con2, ((Attribute)p.con1));
				}
			}catch(Exception e){
				//�ж�p.con2�Ƿ�Ҳ��Attribute
				Attribute[] temp2 = StringParser.StringToAtt((String)p.con2,tableList);
				if (temp2 != null){
					p.con2 = temp2[0];
					//p.con1 �� p.con2���ͱ�ͬ
					if(((Attribute)p.con1).table.getSchema().getAttributeType(((Attribute)p.con1).attIndex).getType() != 
						((Attribute)p.con2).table.getSchema().getAttributeType(((Attribute)p.con2).attIndex).getType()){
						throw new Exception(((Attribute)p.con1).toString() + "��" + ((Attribute)p.con2).toString() + "�ǲ�ͬ���ͱ�");
					}
				}else{
					//p.con2����Attribute�������쳣
					throw e;
				}
			}
		}//��������������		
	}
	
}
