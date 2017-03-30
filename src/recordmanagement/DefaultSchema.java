/*
 * Created on 2005-3-18
 *
 */
package recordmanagement;

import buffer.DataBlock;
import buffer.SchemaBlock;
import diskaccess.DiskManagement;

/**
 * @author zh
 */
public class DefaultSchema implements Schema {
    private SchemaBlock schemaBlock;

    private AttributeType[] attributeType;
    private Index[] attributeIndex;
    
    /**
     * 计算得出
     */
    private short inBlockCapacity;
    private short oneTupleLength;
    private short firstPermittedTupleOffset;
    private short[] attributeOffset;
	
    public DefaultSchema(SchemaBlock schemaBlock, Index[] attributeIndex) {
        byte attributeSize = schemaBlock.getAttributeSize();
        this.schemaBlock = schemaBlock;
        this.attributeIndex = attributeIndex;
        
        // 类型解码
        attributeType = new AttributeType[attributeSize];
        for (byte i = 0; i < attributeIndex.length; i++) {
            attributeType[i] = AttributeType.decodeType(
                    schemaBlock.getAttributeTypeCode(i));
        }

        // 初始化一些常量
        attributeOffset = new short[attributeSize];
        attributeOffset[0] = DATE_OFFSET;
        for (byte i = 1; i < attributeOffset.length; i++) {
            attributeOffset[i] = (short) (attributeOffset[i - 1]
                                 + getAttributeByteLength((byte)(i - 1)));
        }
        oneTupleLength = (short) (attributeOffset[attributeOffset.length - 1]
                    + getAttributeByteLength((byte)(attributeOffset.length - 1)));
        inBlockCapacity = (short)( (DiskManagement.BLOCK_SIZE - DataBlock.TUPLE_OFFSET_OFFSET)
                    / (oneTupleLength + TUPLE_OFFSET_VALUE_OFFSET) );
        firstPermittedTupleOffset = (short) (DiskManagement.BLOCK_SIZE
					- oneTupleLength * inBlockCapacity);
    }
    
	public String getSchemaName() {
	    return schemaBlock.getSchemaName();
	}
	
	public byte getAttributeSize() {
        return schemaBlock.getAttributeSize();
    }

	public byte getKeyAttribute() {
        return schemaBlock.getKeyAttribute();
    }
	
	public short tupleCapacity() {
	    return inBlockCapacity;
	}
	
	public short tupleLength() {
	    return oneTupleLength;
	}

    public short firstPermittedTupleOffset() {
        return firstPermittedTupleOffset;
    }

    public byte getAttributeIDByName(String attriName) {
        byte size = schemaBlock.getAttributeSize();
        for (byte i = 0; i < size; i++) {
            if (schemaBlock.getAttributeName(i).equalsIgnoreCase(attriName))
                return i;
        }
        return -1;
    }
	
	/**
	 * 对每个字段
	 */
	public String getAttributeName(byte order) {
        return schemaBlock.getAttributeName(order);
    }

	public AttributeType getAttributeType(byte order) {
        return attributeType[order];
    }
	
	public Index getAttributeIndex(byte order) {
        return attributeIndex[order];
    }
	
	public short getAttributeOffset(byte order) {
	    return attributeOffset[order];
	}
	
	public short getAttributeByteLength(byte order) {
	    return attributeType[order].getByteLength();
	}
	
	/**
	 * 建立新索引
	 * @param order
	 * @param index
	 */
	public void createAttributeIndex(byte order, Index index, int indexInfoBlockID) {
	    attributeIndex[order] = index;
	    schemaBlock.alterAttributeIndexBlockID(order, indexInfoBlockID);
	}
	
	/**
	 * 删去索引
	 * @param order
	 */
	public void dropIndex(byte order) {
        attributeIndex[order] = null;
        schemaBlock.alterAttributeIndexBlockID(order, 0);
    }
	
}
