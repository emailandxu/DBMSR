
package compiler;
import recordmanagement.*;
//import dataitem.*;

//SELECT...FROM...WHERE 型的查询语句
public class SFW implements Query {
	private Attribute[] selList = null;		
	private Table[] fromList = null;		
	private Condition condition = null;
	
	public SFW(){	
	}
	
//	完成词法分析、语法分析
	public boolean FromSQL(String sql){
		String betweenSF = null;
		String betweenFW = null;
		String afterW = null;
	//首先分析句子结构是否符合SELECT...FROM...WHERE...格式，分别取出'...'所在的字串
		int index = 7;
		int i;
		//取出SELECT与FROM之间的字串
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
		//处理没有FROM关键字的情况
		if (betweenSF == null){
			StringParser.DisplayError("缺少SELECT列表或关键字 'FROM' !");
			return false;
		}
		//取出FROM与WHERE之间的字串和WHERE后的字串
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
			//处理没有WHERE的情况
			betweenFW = sql.substring(index,sql.length()-1);
		}		
		//处理没有WHERE的情况
		if(i == sql.length()-6){
			betweenFW = sql.substring(index,sql.length()-1);
		}
		if(betweenFW == null){
			StringParser.DisplayError("缺少FROM列表！");
			return false;
		}		

	//分别对三个字串进行分析
		//处理From...WHERE之间的字串
		if (betweenFW != null){
			betweenFW = betweenFW.trim();
			String[] fw = betweenFW.split("(\\s*|),(\\s*|)");
			Table[] t = new Table[fw.length];
			for (i = 0; i<fw.length; i++){
				if (StringParser.CheckTable(fw[i]) == null){
					StringParser.DisplayError("不存在名字为" + fw[i] + "的表!");
					return false;
				}else{
					t[i] = StringParser.CheckTable(fw[i]);
				}
			}
			this.fromList = t;
		}else{
			StringParser.DisplayError("缺少From列表!");
			return false;
		}
		//处理SELECT...FROM之间的字串
		if (betweenSF != null){
			Attribute[] tempAtt = StringParser.StringToAtt(betweenSF, this.fromList);
			if (tempAtt != null)
				this.selList = tempAtt;
			else
				return false;
		}
		//处理WHERE之后的字串,生成Condition树
		if (afterW != null){			
			try{
				this.condition = Condition.CompileWhere(afterW);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
			//TraverseCondition(condition);
			try{
				//对Condition树做一些处理，型别鉴定，消除空子树
				Condition.OprtCondition(this.condition, this.condition,true, this.fromList);
			}catch(Exception e){
				StringParser.DisplayError(e.getMessage());
				return false;
			}
		}//endIf
		
		
		//TraverseCondition(this.condition);
		return true;
	}
	
	//以下函数调试Condition用
	//中序遍历Condition树
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
	//以上函数调试Condition用
	
		
	/**
	 * 从SQL语句生成逻辑查询计划
	 * @param Sql
	 * @return
	 */
	public AlgebraPlan ExcuteAgbPlan() {
		Pi pi = null;
		Sigma sigma = null;
		Algebra agb = null;
	
		//设置笛卡尔积
		if(this.fromList.length > 1){//需要多重连接
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
			//设置条件选择
			sigma = new Sigma(this.condition, agb);
			//设置投影
			pi = new Pi(this.selList, sigma);
		}else{
			pi = new Pi(this.selList, agb);
		}
		
		AlgebraPlan plan = new AlgebraPlan(pi);
		return plan;
	}
}
