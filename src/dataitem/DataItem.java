package dataitem;
/*
 * Created on 2005-3-7
 *
 * TODO
 */

/**
 * @author vole
 *
 * ��װ���ݣ�ʹ��ͬ���Ϳ��Լ��׸ı�
 */
public interface DataItem extends Comparable{
    public DataItem add(DataItem d);
    
    public DataItem substract(DataItem d);
    
    public DataItem multiply(DataItem d);
    
    public DataItem divide(DataItem d);
}
