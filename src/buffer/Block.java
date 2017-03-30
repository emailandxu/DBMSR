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
	 * ��������
	 */
	private byte[] data;
	
	/**
	 * ָ���ÿ�ĵ�ַ��������������ص�hashmap
	 */
	private IDEntry idEntry;
	
	/**
	 * �Ƿ��޸Ĺ�,�ж��Ƿ���Ҫд���ļ�
	 * ����data[]��private�����DataBlock��ֻ�������µ�setXXX�������޸�
	 * ����MetaBlock,SchemaBlock,IndexBlock�У���������ʹ���ʼ��ߣ�
	 * ����Ч�ʿ��ǣ��ڲ�����Ҫ�������ݽṹ����ֻ��д�ش���ʱʹ��data���飬��˲���Ϊprivate
	 */
	protected boolean modified = false;
	
	Block(byte[] buffer, IDEntry id) {
		data = buffer;
		idEntry = id;
	}
	
	/**
	 * ÿ����ʹ��ǰ��Ҫ��ʼ��,Ϊ��ֹ��γ�ʼ��,�������ж�
	 * ��meta, index, indexinfo, schema�д��ж���ȷ
	 * ��data���븲��
	 * @return
	 */
	protected boolean initialized() {
        return data == null;
    }
	
	/**
	 * �ͷſռ䣬�б�Ҫʱ���仯�Ŀ�д�����
	 * @param disk
	 */
	protected abstract void releaseMemory(DiskManagement disk);
	
	/**
	 * д�����
	 * Ӧ�ü̳и÷���
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
	 * Ϊ��ʡ�ڴ�,��Ҫʱ,�ڶ�������֮��,������Ϊnull
	 * @param dt
	 */
	void setData(byte[] dt) {
	    data = dt;
	}
	
	/**
	 * ֻ��reload���õ�
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
	 * ���º���Ϊ��ȡ�ֶ�
	 * @param offset ��data[]�е���ʼλ��
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
