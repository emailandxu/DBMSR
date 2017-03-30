/*
 * Created on 2005-3-19
 *
 */
package recordmanagement;

import buffer.DataBlock;
import diskaccess.DiskManagement;

/**
 * @author zh
 * 
 * 用于临时创建的表
 */
public class TempSchema implements Schema {
    
    private String schemaName;
    private byte attributeSize;
    
    private String[] attributeName;
    private AttributeType[] attributeType;
    
    /**
     * 计算得出
     */
    private short inBlockCapacity;
    private short oneTupleLength;
    private short firstPermittedTupleOffset;
    private short[] attributeOffset;
    
    /**
     * 既然是临时表就不用主键了
     * @param schemaName
     * @param attributeSize
     * @param attributeName
     * @param attributeType
     */
    public TempSchema(String schemaName, byte attributeSize,
            String[] attributeName, AttributeType[] attributeType) {
        this.schemaName = schemaName;
        this.attributeSize = attributeSize;
        
        this.attributeName = new String[attributeSize];
        this.attributeType = new AttributeType[attributeSize];
        for (byte i = 0; i < attributeSize; i++) {
            this.attributeName[i] = attributeName[i];
            this.attributeType[i] = attributeType[i];
        }

        // 初始化一些常量
        attributeOffset = new short[attributeSize];
        attributeOffset[0] = DATE_OFFSET;
        for (byte i = 1; i < attributeOffset.length; i++) {
            attributeOffset[i] = (short)(attributeOffset[i - 1]
                    + getAttributeByteLength((byte) (i - 1)));
        }
        oneTupleLength = (short) (attributeOffset[attributeOffset.length - 1]
                + getAttributeByteLength((byte) (attributeOffset.length - 1)));
        inBlockCapacity = (short) ((DiskManagement.BLOCK_SIZE - DataBlock.TUPLE_OFFSET_OFFSET)
                / (oneTupleLength + TUPLE_OFFSET_VALUE_OFFSET));
        firstPermittedTupleOffset = (short) (DiskManagement.BLOCK_SIZE
				- oneTupleLength * inBlockCapacity);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public byte getAttributeSize() {
        return attributeSize;
    }

    public byte getKeyAttribute() {
        return -1; // 无主键
    }

    public short tupleCapacity() {
        return inBlockCapacity;
    }

    public short tupleLength() {
        return oneTupleLength;
    }

    public short firstPermittedTupleOffset() {
        return 0;
    }

    public byte getAttributeIDByName(String attriName) {
        for (byte i = 0; i < attributeSize; i++) {
            if (attributeName[i].equalsIgnoreCase(attriName))
                return i;
        }
        return -1;
    }

    public String getAttributeName(byte order) {
        return attributeName[order];
    }

    public AttributeType getAttributeType(byte order) {
        return attributeType[order];
    }

    public Index getAttributeIndex(byte order) {
        return null;
    }

    public short getAttributeOffset(byte order) {
        return attributeOffset[order];
    }

    public short getAttributeByteLength(byte order) {
        return attributeType[order].getByteLength();
    }

}
