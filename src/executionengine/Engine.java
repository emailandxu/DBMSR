package executionengine;

import recordmanagement.RecordManagement;

import compiler.*;
public class Engine {
 
	private RecordManagement recordManagement = RecordManagement.getInstance();
	 
	private Operation preparedOperation;
	
	//private Operation operation;
	 
	private Operation AgbToOpr(Algebra agb){
		if( agb instanceof AgbTable){//遍历
			return new Retrieve( ((AgbTable)agb).GetTable());
		}else if(agb instanceof Descartes){//连接
			Operation opr1 = AgbToOpr(((Descartes)agb).GetAgb1());
			Operation opr2 = AgbToOpr(((Descartes)agb).GetAgb2());
			return new CrudeJoin(opr1, opr2);
		}else if(agb instanceof Sigma){//选择
			return new CrudeSelect(((Sigma)agb).GetCondition(), AgbToOpr(((Sigma)agb).GetBelowNode() ));
		}else if(agb instanceof Pi){//投影
			return new Project( ((Pi)agb).GetList(), AgbToOpr( ((Pi)agb).GetBelowNode()));
		}else if(agb instanceof AgbDelete){//删除
			return new Delete ( ((AgbDelete)agb).getTable(), ((AgbDelete)agb).getCondition());
		}else if(agb instanceof AgbUpdate){//更新
			return new Update ( ((AgbUpdate)agb).getAttList(), ((AgbUpdate)agb).getDataList(), AgbToOpr( ((AgbUpdate)agb).getBelowNode()));
		}
		return null;
	}
	public Operation createOperation(AlgebraPlan exePlan) throws Exception {
		Algebra root = exePlan.GetRoot();
		Operation tempOpr = AgbToOpr(root);
		if (tempOpr != null){
			this.preparedOperation = tempOpr;
		}else{
			throw new Exception("空的物理查询计划!");
		}
		return this.preparedOperation;
	}	
	public Operation GetPreparedOperation(){
		return preparedOperation;
	}
	 
}
 
