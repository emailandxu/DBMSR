package recordmanagement;


public interface Schema {

    static final int DATE_OFFSET = 8;
    
    static final int TUPLE_OFFSET_VALUE_OFFSET = 2;
    
	public String getSchemaName();
	
	public byte getAttributeSize();

	public byte getKeyAttribute();
	
	short tupleCapacity();
	
	short tupleLength();
	
	short firstPermittedTupleOffset();
	
	/**
	 * �����������õ����Ե����
	 * @param attriName
	 * @return -1 ���Բ�����
	 */
	public byte getAttributeIDByName(String attriName);
	
	/**
	 * ��ÿ���ֶ�
	 */
	public String getAttributeName(byte order);

	public AttributeType getAttributeType(byte order);
	
	public Index getAttributeIndex(byte order);
	
	short getAttributeOffset(byte order);
	
	short getAttributeByteLength(byte order);
}
 
