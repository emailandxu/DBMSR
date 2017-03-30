package executionengine;

import compiler.Attribute;

import recordmanagement.*;


/**
 *所有SQL操作的interface，实现的类通过构造函数
 *完成自身数据结构的构造
 *
 */
public abstract interface Operation {
	
	public void open();
	public abstract boolean hasNext();
	public abstract Tuple getNext();
	public abstract void close();
	public Attribute[] getAttributeList();
}
 
