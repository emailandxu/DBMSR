package recordmanagement;

import dataitem.DataItem;
import buffer.DataBlock;
import buffer.IndexBlock;
import buffer.IndexInfoBlock;

public class Table {
 	 
    private int schemaBlockID;
    
	private Integer fileID;
	 	 
	private Schema schema;
	
    public Table(int schemaBlockID, Integer fileID, Schema schema) {
        this.schemaBlockID = schemaBlockID;
        this.fileID = fileID;
        this.schema = schema;
    }
    
    /**
     * �ڵ�order�������Ͻ�������
     * @param order
     */
    public void createIndex(byte order) {
        if (schema.getAttributeIndex(order) != null) {
            throw new IllegalArgumentException("there is already an index on this attribute");
        }
        
        RecordManagement recordManagement =
            RecordManagement.getInstance();
        AttributeType attributeType = schema.getAttributeType(order);
        
        int indexInfoBlockID = recordManagement.getNewDBBlockID();
        IndexInfoBlock newIndexInfoBlock = (IndexInfoBlock)
        		recordManagement.getIndexInfoBlock(indexInfoBlockID);
        
        int indexBlockID = recordManagement.getNewDBBlockID();
        IndexBlock newIndexBlock = recordManagement.getIndexBlock(indexBlockID);
        newIndexBlock.initialize(attributeType, true);
        
        newIndexInfoBlock.initialize(indexBlockID);
        Index index = new Index(attributeType, newIndexInfoBlock);
        
        // ��default schema�м����µ�����
        ((DefaultSchema)schema).createAttributeIndex(order,
                index, indexInfoBlockID);
    }
    
    /**
     * �ڵ�order��������ɾ��������������ڵĻ�
     * ����ɾ�������ϵ�����
     * @param order
     */
    public void dropIndex(byte order) {
        if (schema.getAttributeIndex(order) == null)
            return;
        if (order == schema.getKeyAttribute())
            throw new IllegalArgumentException("can't drop index on premier key");
        ((DefaultSchema)schema).dropIndex(order);
    }
    
    /**
     * ����Ԫ��ʱ��������������,
     * ������ֱ�ӵ���DataBlock�е�insertBlock,
	 * ��Ϊ����λ��������tableȷ��
	 * @param insertItems ���ͱ�����schema��Ӧ�����ڲ������ٽ������ͼ��
	 * @return
	 */
	public Tuple insertTuple(DataItem[] insertItems) {
	    int size = getTableBlockSize();
	    if (size != 0) {
		    DataBlock lastBlock = getBlock(size - 1);
		    Tuple newTuple = lastBlock.insertTuple(insertItems);
		    if (newTuple != null) {
	            return newTuple;
	        }
        }

        RecordManagement recordManagement = RecordManagement.getInstance();
        // ��Ҫ�½�һ�����ݿ�
        DataBlock newBlock = recordManagement.getDataBlock(fileID,
                (recordManagement.getNewDataBlockID(fileID)));
        newBlock.initialize(schema, true);
        Tuple newTuple = newBlock.insertTuple(insertItems);
        if (newTuple != null) {
            return newTuple;
        }
	    throw new IllegalStateException("cannot insert in any block");
	}
	
    /**
     * �õ�ָ�����Ѿ����ڵĿ�
     * @param blockID
     * @return
     */
	public DataBlock getBlock(int blockID) {
		DataBlock dataBlock = RecordManagement.getInstance()
					.getDataBlock(fileID, blockID);
		dataBlock.initialize(schema, false);
		return dataBlock;
	}
	
	/**
	 * �õ��ܵĿ������ڱ���ʱ�õ�
	 * @return
	 */
	public int getTableBlockSize() {
        return RecordManagement.getInstance().getTableBlockSize(fileID);
    }
    
	/**
	 * �õ�����
	 * @return
	 */
    public String getTableName() {
        return schema.getSchemaName();
    }
	
    /**
     * �õ���ģʽ
     * @return
     */
    public Schema getSchema() {
        return schema;
    }
    
    Integer getFileID() {
        return fileID;
    }
    
    int getSchemaBlockID() {
        return schemaBlockID;
    }
    
    public String toString() {
        return getTableName();
    }
}
 
