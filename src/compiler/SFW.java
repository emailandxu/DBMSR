
package compiler;
import recordmanagement.*;
//import dataitem.*;

//SELECT...FROM...WHERE �͵Ĳ�ѯ���
public class SFW implements Query {
	private Attribute[] selList = null;		
	private Table[] fromList = null;		
	private Condition condition = null;
	
	public SFW(){	
	}
	
//	��ɴʷ��������﷨����
	public boolean FromSQL(String sql){
		String betweenSF = null;
		String betweenFW = null;
		String afterW = null;
	//���ȷ������ӽṹ�Ƿ����SELECT...FROM...WHERE...��ʽ���ֱ�ȡ��'...'���ڵ��ִ�
		int index = 7;
		int i;
		//ȡ��SELECT��FROM֮����ִ�
		i = index;
		if(sql.length()-i > 6){
			for (; i < sql.length()-6; i++){
				if(sql.substring(i,i + 6).toUpperCase().equals(" FROM ")){
					betweenSF = sql.substring(7,i);
					break;
				}
			}
		}
		index = i + 6;
		//����û��FROM�ؼ��ֵ����
		if (betweenSF == null){
			StringParser.DisplayError("ȱ��SELECT�б��ؼ��� 'FROM' !");
			return false;
		}
		//ȡ��FROM��WHERE֮����ִ���WHERE����ִ�
		i = index;
		if(sql.length()-i > 7){
			for (; i < (sql.length()-6); i++){
				if(sql.substring(i,i + 7).toUpperCase().equals(" WHERE ")){						
					betweenFW = sql.substring(index,i);
					afterW = sql.substring(i + 7, sql.length());
					break;
				}
			}
		}else{
			//����û��WHERE�����
			betweenFW = sql.substring(index,sql.length()-1);
		}		
		//����û��WHERE�����
		if(i == sql.length()-6){
			betweenFW = sql.substring(index,sql.length()-1);
		}
		if(betweenFW == null){
			StringParser.DisplayError("ȱ��FROM�б�");
			return false;
		}		

	//�ֱ�������ִ����з���
		//����From...WHERE֮����ִ�
		if (betweenFW != null){
			betweenFW = betweenFW.trim();
			String[] fw = betweenFW.split("(\\s*|),(\\s*|)");
			Table[] t = new Table[fw.length];
			for (i = 0; i<fw.length; i++){
				if (StringParser.CheckTable(fw[i]) == null){
					StringParser.DisplayError("����������Ϊ" + fw[i] + "�ı�!");
					return false;
				}else{
					t[i] = StringParser.CheckTable(fw[i]);
				}
			}
			this.fromList = t;
		}else{
			StringParser.DisplayError("ȱ��From�б�!");
			return false;
		}
		//����SELECT...FROM֮����ִ�
		if (betweenSF != null){
			Attribute[] tempAtt = StringParser.StringToAtt(betweenSF, this.fromList);
			if (tempAtt != null)
				this.selList = tempAtt;
			else
				return false;
		}
		//����WHERE֮����ִ�,����Condition��
		if (afterW != null){			
			try{
				this.condition = Condition.CompileWhere(afterW);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
			//TraverseCondition(condition);
			try{
				//��Condition����һЩ�����ͱ����������������
				Condition.OprtCondition(this.condition, this.condition,true, this.fromList);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
		}//endIf
		
		
		//TraverseCondition(this.condition);
		return true;
	}
	
	//���º�������Condition��
	//�������Condition��
	private void TraverseCondition(Condition con){
		if (con.con1 != null && (con.con1 instanceof Condition)){
			TraverseCondition((Condition)(con.con1));
		}else if (con.con1 != null ){
			System.out.println((con.con1).toString());
		}
		System.out.println(con.opr);
		if (con.con2 != null && (con.con2 instanceof Condition)){
			TraverseCondition((Condition)con.con2);
		}else if (con.con2 != null ){
			System.out.println((con.con2).toString());
		}
	}
	//���Ϻ�������Condition��
	
		
	/**
	 * ��SQL��������߼���ѯ�ƻ�
	 * @param Sql
	 * @return
	 */
	public AlgebraPlan ExcuteAgbPlan() {
		Pi pi = null;
		Sigma sigma = null;
		Algebra agb = null;
	
		//���õѿ�����
		if(this.fromList.length > 1){//��Ҫ��������
			Descartes p,q;
			p = null; q = null;
			p = new Descartes(new AgbTable(fromList[0]),new AgbTable(fromList[1]));
			q = p;
			for (int i = 2; i < fromList.length; i++){
				q = new Descartes(p, new AgbTable(fromList[i]));
				p = q;
			}
			agb = q;
		}else{
			agb = new AgbTable(fromList[0]);
		}
		
		if (this.condition != null){
			//��������ѡ��
			sigma = new Sigma(this.condition, agb);
			//����ͶӰ
			pi = new Pi(this.selList, sigma);
		}else{
			pi = new Pi(this.selList, agb);
		}
		
		AlgebraPlan plan = new AlgebraPlan(pi);
		return plan;
	}
}
