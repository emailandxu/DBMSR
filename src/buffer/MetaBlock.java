/*
 * Created on 2005-1-24
 */
package buffer;

import diskaccess.DiskManagement;

/**
 * @author zh
 */
public class MetaBlock extends Block {
    private static final int TABLE_SIZE_OFFSET = 0;
    private static final int SCHEMA_BLOCK_ID_STARTING_OFFSET = 1;
    /**
     * ���ݿ���ܱ���
     */
    private byte tableSize;
    
    /**
     * ���ģʽ���ַ
     */
    private int[] schemaBlockID;
    
    /**
	 * @param buffer
	 * @param id
	 */
	public MetaBlock(byte[] buffer, IDEntry id) {
		super(buffer, id);
	}

	/**
	 * ʹ�����ݽ��г�ʼ��
	 * @param byExternal ��һ�δ���
	 */
	public void initialize(boolean byExternal) {
	    if (initialized()) {
            if (byExternal) {
                throw new IllegalArgumentException("new block has an error");
            }
            return;
        }
	    
	    if (byExternal) {
            setData(null);
            tableSize = 0;
            schemaBlockID = new int[0];
            modified = true;
            return;
        }
	    
		tableSize = getByte(TABLE_SIZE_OFFSET);
		schemaBlockID = new int[tableSize];
		for (int i = 0; i < tableSize; i++)
            schemaBlockID[i] = getInt(SCHEMA_BLOCK_ID_STARTING_OFFSET + i*4);
		
        // ����Ҫ��˴�Ŀռ���
		setData(null);
    }
	
	/**
	 * �����±�
	 * @param newSchemaBlockID
	 */
	public void addTable(int newSchemaBlockID) {
        tableSize++;
        int[] schemaArray = new int[tableSize];
        System.arraycopy(schemaBlockID, 0, schemaArray, 0, schemaBlockID.length);
        schemaArray[tableSize - 1] = newSchemaBlockID;
        schemaBlockID = schemaArray;
        modified = true;
    }

	/**
	 * ɾ����
	 * @param oldSchemaBlockID
	 */
	public void removeTable(int oldSchemaBlockID) {
        for (int i = 0; i < schemaBlockID.length; i++) {
            if (schemaBlockID[i] == oldSchemaBlockID) {
                tableSize--;
                int[] schemaArray = new int[tableSize];
                System.arraycopy(schemaBlockID, 0, schemaArray, 0, i);
                System.arraycopy(schemaBlockID, i + 1, schemaArray, i, schemaArray.length - i);
                schemaBlockID = schemaArray;
                modified = true;
                return;
            }
        }
    }
	/**
	 * �õ���order�����ģʽ��ַ
	 * @param order
	 * @return
	 */
	public int getSchemaBlockID(byte order) {
        return schemaBlockID[order];
    }
	
	/**
	 * �õ��������
	 * @return
	 */
    public byte getTableSize() {
        return tableSize;
    }
	
    protected void releaseMemory(DiskManagement disk) {
        if (modified) {
            int size = SCHEMA_BLOCK_ID_STARTING_OFFSET + schemaBlockID.length * 4;
            byte[] dt = new byte[size];
            setData(dt);
            
            setByte(TABLE_SIZE_OFFSET, tableSize);
    		for (int i = 0; i < tableSize; i++)
                setInt(SCHEMA_BLOCK_ID_STARTING_OFFSET + i*4, schemaBlockID[i]);
            
    		modified = false;
            super.writeBlock(disk);
        }
		// ����ڴ�ռ�
		schemaBlockID = null;
    }

}
