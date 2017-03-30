package dataitem;
/*
 * Created on 2005-3-7
 *
 * TODO
 */

/**
 * @author vole
 *
 * 包装数据，使不同类型可以简易改变
 */
public interface DataItem extends Comparable{
    public DataItem add(DataItem d);
    
    public DataItem substract(DataItem d);
    
    public DataItem multiply(DataItem d);
    
    public DataItem divide(DataItem d);
}
