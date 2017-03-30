
package compiler;

//语法树
public interface Query {
	public boolean FromSQL(String sql);	//对输入的SQL语句进行词法分析和语法分析，构建语法树
	public AlgebraPlan ExcuteAgbPlan();	//根据语法树生成逻辑查询计划
}
