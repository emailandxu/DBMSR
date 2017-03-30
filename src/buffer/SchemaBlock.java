/*
 * Created on 2005-1-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package buffer;

import recordmanagement.AttributeType;
import diskaccess.DiskManagement;

/**
 * @author zh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SchemaBlock extends Block{
    private static final int SCHEMA_NAME_OFFSET = 0;
    private static final int ATTRIBUTE_SIZE_OFFSET = 32;
    private static final int KET_ATTRIBUTE_OFFSET = 33;
    private static final int ATTRIBUTE_STARTING_OFFSET = 34;
    
    // 相对每个字段的开始
    private static final int ATTRIBUTE_NAME_OFFSET = 0;
    private static final int ATTRIBUTE_TYPE_OFFSET = 32;
    private static final int ATTRIBUTE_INDEX_BLOCK_ID_OFFSET = 33;
    
    // 模式名,字段名最多16个字符
    private static final int SCHEMA_NAME_LENGTH = 16;
    private static final int ATTRIBUTE_NAME_LENGTH = 16;
    
    // 字段信息的长度
    private static final int ATTRIBUTE_INFO_LENGTH = ATTRIBUTE_INDEX_BLOCK_ID_OFFSET + 4;
    
    // 每个表最大含有字段数
    private static final int MAX_ATTRIBUTE_SIZE = 
        (DiskManagement.BLOCK_SIZE - ATTRIBUTE_STARTING_OFFSET) / ATTRIBUTE_INFO_LENGTH;
    
    private String schemaName;
    private byte attributeSize;
    private byte keyAttribute;
    
    private String[] attributeName;
    private byte[] attributeType;
    private int[] attributeIndexBlockID;
    
	/**
	 * @param buffer
	 * @param id
	 */
	SchemaBlock(byte[] buffer, IDEntry id) {
		super(buffer, id);
	}
	
	/**
	 * 使用以有数据进行初始化
	 */
	public void initialize() {
	    if (initialized()) {
            return;
        }
	    
		schemaName = getString(SCHEMA_NAME_OFFSET, SCHEMA_NAME_LENGTH);
		attributeSize = getByte(ATTRIBUTE_SIZE_OFFSET);
		keyAttribute = getByte(KET_ATTRIBUTE_OFFSET);
		
		attributeName = new String[attributeSize];
		attributeType = new byte[attributeSize];
		attributeIndexBlockID = new int[attributeSize];
		
		for (int i = 0; i < attributeSize; i++) {
		    int relativeOffset = ATTRIBUTE_STARTING_OFFSET + i * ATTRIBUTE_INFO_LENGTH;
            attributeName[i] = getString(relativeOffset + ATTRIBUTE_NAME_OFFSET, ATTRIBUTE_NAME_LENGTH);
            attributeType[i] = getByte(relativeOffset + ATTRIBUTE_TYPE_OFFSET);
            attributeIndexBlockID[i] = getInt(relativeOffset + ATTRIBUTE_INDEX_BLOCK_ID_OFFSET);
        }
		
		// 不需要如此大的空间了
		setData(null);
	}
	
	/**
	 * 使用外部数据,即新建一个表,此时index必为空
	 * @param externalSchema
	 */
	public void initialize(String schemaName, byte attributeSize, byte keyAttribute,
            String[] attributeName, AttributeType[] attributeType) {
	    if (initialized()) {
            throw new IllegalArgumentException("new block has an error");
        }
	    
	    setData(null);
	    this.schemaName = schemaName;
        this.attributeSize = attributeSize;
        this.keyAttribute = keyAttribute;
        
        this.attributeName = new String[attributeSize];
        this.attributeType = new byte[attributeSize];
        this.attributeIndexBlockID = new int[attributeSize];
        for (byte i = 0; i < attributeSize; i++) {
            this.attributeName[i] = attributeName[i];
            this.attributeType[i] = attributeType[i].getEncodedType();
        }
		modified = true;
	}

	/**
	 * 表
	 */
	public String getSchemaName() {
        return schemaName;
    }
	
    public byte getAttributeSize() {
        return attributeSize;
    }
    
    public byte getKeyAttribute() {
        return keyAttribute;
    }
    
    /**
     * 改变索引
     * @param order
     * @param indexInfoBlockID 0 为删去索引
     */
    public void alterAttributeIndexBlockID(byte order, int indexInfoBlockID) {
        attributeIndexBlockID[order] = indexInfoBlockID;
        modified = true;
    }
    
    /**
     * 属性
     */
    public int getAttributeIndexBlockID(byte order) {
        return attributeIndexBlockID[order];
    }
    
    public String getAttributeName(byte order) {
        return attributeName[order];
    }
    
    public byte getAttributeTypeCode(byte order) {
        return attributeType[order];
    }
    
    protected void releaseMemory(DiskManagement disk) {
        if (modified) {
            int size = ATTRIBUTE_STARTING_OFFSET + attributeSize * ATTRIBUTE_INFO_LENGTH;
            byte[] dt = new byte[size];
            setData(dt);
            
            setString(SCHEMA_NAME_OFFSET, schemaName, SCHEMA_NAME_LENGTH);
    		setByte(ATTRIBUTE_SIZE_OFFSET, attributeSize);
    		setByte(KET_ATTRIBUTE_OFFSET, keyAttribute);
    		
    		for (int i = 0; i < attributeSize; i++) {
    		    int relativeOffset = ATTRIBUTE_STARTING_OFFSET + i * ATTRIBUTE_INFO_LENGTH;
                setString(relativeOffset + ATTRIBUTE_NAME_OFFSET, attributeName[i], ATTRIBUTE_NAME_LENGTH);
                setByte(relativeOffset + ATTRIBUTE_TYPE_OFFSET, attributeType[i]);
                setInt(relativeOffset + ATTRIBUTE_INDEX_BLOCK_ID_OFFSET, attributeIndexBlockID[i]);
            }
    		
    		modified = false;
    		super.writeBlock(disk);
        }
		// 清空内存空间
		attributeName = null;
	    attributeType = null;
	    attributeIndexBlockID = null;
    }

}
