package recordmanagement;

import java.util.Date;

import dataitem.DataItem;

/**
 * @author vole
 *
 * TODO
 */
public interface Tuple {
    
    public abstract Date getLastModified();
	
	/*public abstract void setTime(byte position, Date value);
	public abstract Date getTime(byte position);
	
	public abstract void setDouble(byte position, double value);
	public abstract double getDouble(byte position);
	
	public abstract void setLong(byte position, long value);
	public abstract long getLong(byte position);
	
	public abstract void setChar(byte position, char value);
	public abstract char getChar(byte position);
	
	public abstract void setBoolean(byte position, boolean value);
	public abstract boolean getBoolean(byte position);
	
	public abstract void setString(byte position, String value);
	public abstract String getString(byte position);
	
	public abstract void setInt(byte position, int value);
	public abstract int getInt(byte position);*/
	
	public DataItem getItem(byte position);
	
	public void setItem(byte position, DataItem item);
}
