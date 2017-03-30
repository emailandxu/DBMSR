/*
 * Created on 2005-1-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package buffer;

import java.util.Date;

import diskaccess.DiskManagement;

/**
 * @author zh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class Block {
	public static final int META_BLOCK = 1;
	public static final int SCHEMA_BLOCK = 2;
	public static final int INDEX_BLOCK = 3;
	public static final int DATA_BLOCK = 4;
	public static final int INDEX_INFO_BLOCK = 5;
	
	/**
	 * 数据数组
	 */
	private byte[] data;
	
	/**
	 * 指明该块的地址，用来操作对象池的hashmap
	 */
	private IDEntry idEntry;
	
	/**
	 * 是否修改过,判断是否需要写入文件
	 * 由于data[]是private，因此DataBlock中只需在以下的setXXX方法中修改
	 * 但在MetaBlock,SchemaBlock,IndexBlock中，由于数据使用率极高，
	 * 出于效率考虑，内部还需要其他数据结构，而只在写回磁盘时使用data数组，因此不能为private
	 */
	protected boolean modified = false;
	
	Block(byte[] buffer, IDEntry id) {
		data = buffer;
		idEntry = id;
	}
	
	/**
	 * 每个块使用前都要初始化,为防止多次初始化,必须先判断
	 * 在meta, index, indexinfo, schema中此判断正确
	 * 在data中须覆盖
	 * @return
	 */
	protected boolean initialized() {
        return data == null;
    }
	
	/**
	 * 释放空间，有必要时将变化的块写入磁盘
	 * @param disk
	 */
	protected abstract void releaseMemory(DiskManagement disk);
	
	/**
	 * 写入磁盘
	 * 应该继承该方法
	 * @return
	 */
	void writeBlock(DiskManagement disk) {
	    disk.writeToDisk(
	            idEntry.getFileID(),
	            idEntry.getBlockID(),
	            data);
	    data = null;
	}
	
	/**
	 * 为节省内存,必要时,在读入数据之后,可以置为null
	 * @param dt
	 */
	void setData(byte[] dt) {
	    data = dt;
	}
	
	/**
	 * 只在reload中用到
	 * @return
	 */
    byte[] getData() {
        return data;
    }
    
	/**
     * @return Returns the idEntry.
     */
    IDEntry getIdEntry() {
        return idEntry;
    }
	
	/**
	 * 以下函数为存取字段
	 * @param offset 在data[]中的起始位置
	 * @param value
	 */
	// byte
	protected void setByte(int offset, byte value) {
		data[offset] = value;
	}
    protected byte getByte(int offset) {
		return data[offset];
	}
	
	// short
	protected void setShort(int offset, short value) {
		data[offset] = (byte)(value >> 8);
		data[offset+1] = (byte)(value >> 0);
	}
	protected short getShort(int offset) {
		return (short)((data[offset] << 8) |
					(data[offset+1] & 0xff));
	}
	
	// int
	protected void setInt(int offset, int value) {
		data[offset] = (byte)(value >> 24);
		data[offset+1] = (byte)(value >> 16);
		data[offset+2] = (byte)(value >> 8);
		data[offset+3] = (byte)(value >> 0);
	}
	protected int getInt(int offset) {
		return (int)(((data[offset] & 0xff) << 24) |
			      ((data[offset+1] & 0xff) << 16) |
			      ((data[offset+2] & 0xff) <<  8) |
			      ((data[offset+3] & 0xff) <<  0));
	}
	
	// long
	protected void setLong(int offset, long value) {
		data[offset] = (byte)(value >> 56);
		data[offset+1] = (byte)(value >> 48);
		data[offset+2] = (byte)(value >> 40);
		data[offset+3] = (byte)(value >> 32);
		data[offset+4] = (byte)(value >> 24);
		data[offset+5] = (byte)(value >> 16);
		data[offset+6] = (byte)(value >> 8);
		data[offset+7] = (byte)(value >> 0);
	}
	protected long getLong(int offset) {
		return ((((long)data[offset] & 0xff) << 56) |
				(((long)data[offset+1] & 0xff) << 48) |
				(((long)data[offset+2] & 0xff) << 40) |
				(((long)data[offset+3] & 0xff) << 32) |
				(((long)data[offset+4] & 0xff) << 24) |
				(((long)data[offset+5] & 0xff) << 16) |
				(((long)data[offset+6] & 0xff) <<  8) |
				(((long)data[offset+7] & 0xff) <<  0));
	}
	
	// char
	protected void setChar(int offset, char value) {
		data[offset] = (byte)(value >> 8);
		data[offset+1] = (byte)(value >> 0);
	}
	protected char getChar(int offset) {
		return (char)((data[offset] << 8) |
					(data[offset+1] & 0xff));
	}
	
	// boolean
	protected void setBoolean(int offset, boolean value) {
		data[offset] = value ? (byte)1 : (byte)0;
	}
	protected boolean getBoolean(int offset) {
		return data[offset] == 1 ?
				true : false;
	}
	
	// double
	protected void setDouble(int offset, double value) {
		setLong(offset, Double.doubleToRawLongBits(value));
	}
	protected double getDouble(int offset) {
		return Double.longBitsToDouble(getLong(offset));
	}
	
	// time
	protected void setTime(int offset, Date value) {
		setLong( offset, value.getTime());
	}
	protected Date getTime(int offset) {
		return new Date(getLong(offset));
	}
	
	// string
	protected void setString(int offset, String value, int maxLength) {
		char[] chr = value.toCharArray();
		
		int i = 0;
		for ( ; i < chr.length && i < maxLength; i++) {
			setChar(offset, chr[i]);
			offset += 2;
		}
		if (i<maxLength)
			for ( ; i < maxLength ; i++) {
				setChar(offset, (char)0);
				offset += 2;
			}
	}
	protected String getString(int offset, int maxLength) {
		char[] chr = new char[maxLength];
		
		int i = 0;
		for ( ; i < maxLength; i++) {
			chr[i] = getChar(offset);
			if (chr[i] == 0) break;
			offset += 2;
		}
		return new String(chr,0,i);
	}
}
