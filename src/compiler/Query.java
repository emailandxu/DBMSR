
package compiler;

//�﷨��
public interface Query {
	public boolean FromSQL(String sql);	//�������SQL�����дʷ��������﷨�����������﷨��
	public AlgebraPlan ExcuteAgbPlan();	//�����﷨�������߼���ѯ�ƻ�
}
