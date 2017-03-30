package executionengine;

import compiler.Attribute;

import recordmanagement.Schema;
import recordmanagement.Tuple;


/**
 *Ïû³ýÖØ¸´
 *
 */
public class Distinct implements Operation {
 
	 
	public void open() {
	}
	 
	/**
	 *@see executionengine.Operation#hasNext()
	 *
	 */
	public boolean hasNext() {
		return false;
	}
	 
	/**
	 *@see executionengine.Operation#getNext()
	 *
	 */
	public Tuple getNext() {
		return null;
	}
	 
	/**
	 *@see executionengine.Operation#close()
	 *
	 */
	public void close() {
	}

	/* (non-Javadoc)
	 * @see executionengine.Operation#getSchema()
	 */
	public Schema getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see executionengine.Operation#getAttributeList()
	 */
	public Attribute[] getAttributeList() {
		// TODO Auto-generated method stub
		return null;
	}
	 
}
 
