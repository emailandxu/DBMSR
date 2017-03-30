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
     * 在第order个属性上建立索引
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
        
        // 在default schema中加入新的索引
        ((DefaultSchema)schema).createAttributeIndex(order,
                index, indexInfoBlockID);
    }
    
    /**
     * 在第order个属性上删除索引，如果存在的话
     * 不能删除主键上的索引
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
     * 插入元组时必须调用这个函数,
     * 而不能直接调用DataBlock中的insertBlock,
	 * 因为插入位置由整个table确定
	 * @param insertItems 类型必须与schema对应，在内部不会再进行类型检查
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
        // 需要新建一个数据块
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
     * 得到指定的已经存在的块
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
	 * 得到总的块数，在遍历时用到
	 * @return
	 */
	public int getTableBlockSize() {
        return RecordManagement.getInstance().getTableBlockSize(fileID);
    }
    
	/**
	 * 得到表名
	 * @return
	 */
    public String getTableName() {
        return schema.getSchemaName();
    }
	
    /**
     * 得到表模式
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
 
