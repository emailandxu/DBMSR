package executionengine;

import compiler.Attribute;

import recordmanagement.*;


/**
 *����SQL������interface��ʵ�ֵ���ͨ�����캯��
 *����������ݽṹ�Ĺ���
 *
 */
public abstract interface Operation {
	
	public void open();
	public abstract boolean hasNext();
	public abstract Tuple getNext();
	public abstract void close();
	public Attribute[] getAttributeList();
}
 
