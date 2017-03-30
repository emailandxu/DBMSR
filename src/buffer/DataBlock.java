package buffer;

import java.util.Date;

import recordmanagement.AttributeType;
import recordmanagement.Schema;
import recordmanagement.Tuple;
import dataitem.BooleanItem;
import dataitem.CharItem;
import dataitem.DataItem;
import dataitem.DoubleItem;
import dataitem.IntegerItem;
import dataitem.LongItem;
import dataitem.StringItem;
import dataitem.TimeItem;
import diskaccess.DiskManagement;


/**
 *���̿�
 */
public class DataBlock extends Block{
	private static final int TUPLE_SIZE_OFFSET = 0;
	public static final int TUPLE_OFFSET_OFFSET = 2;
	
	private static final int OFFSET_OFFSET = 2;
	
	/**
	 * �����ڿ���й���˫������ʵ��ʱ���㷨(LRU�Ľ����㷨)
	 * �������Լ��ڴ˲������ʣ�
	 * Ӧ����BufferAccess�����һ��������DataBlock�İ����࣬��Ϊ����������ԣ�
	 * �����ڴ�ϵͳЧ��������Ҫ��Ϊ�����������Ķ��󣬽�������ʡȥ
	 * package access��ֻ�ڱ�package��������
	 */
	DataBlock prev;
	DataBlock next;
	
	/**
	 * ��ͬ�������ֻ��һ���С�Ŀռ����cache��
	 * ����ԭ��
	 * ��һ��ʹ�ú�����������������ʹ����
	 * ��ͬ��ŵ���һ�����������һ������Ͷ��ʹ�ã����������ͬ��ŵĿ���
	 */
	DataBlock identical;

	/**
	 * ��������һ���иÿ��Ƿ�Ҫ�������ڴ���
	 * ��getDataBlock�������µ�getTuple��ȡ�������ݵĺ���������ʱ��
	 * ��Ӧ����Ϊtrue
	 */
	boolean keepNextTurn;
	
	/**
	 * ������ѭ��ģʽ
	 */
	private Schema schema;
		
	/**
	 * ƫ��������ı�ʱֱ��д��ײ�����
	 */
	private short[] tupleOffset;
	
	DataBlock(byte[] buffer, IDEntry id) {
		super(buffer, id);
	}

	/**
	 * ÿ����ʹ��ǰ��Ҫ��ʼ��,Ϊ��ֹ��γ�ʼ��,�������ж�
	 * @return
	 */
	protected boolean initialized() {
        return tupleOffset != null;
    }
	
	public void initialize(Schema schema, boolean byExternal) {
	    if (initialized()) {
            if (byExternal) {
                throw new IllegalArgumentException("new block has an error");
            }
            return;
        }
	    
	    this.schema = schema;
	    
	    if (byExternal) {
	        setShort(TUPLE_SIZE_OFFSET, (short)0);
            tupleOffset = new short[0];
            modified = true;
            return;
        }
	    
		short tupleSize = getShort(TUPLE_SIZE_OFFSET);
		tupleOffset = new short[tupleSize];
		for (int i = 0; i < tupleOffset.length; i++) {
            tupleOffset[i] = getShort(TUPLE_OFFSET_OFFSET + OFFSET_OFFSET * i);
        }
	}
	
	/**
	 * ������Ԫ����������ʱʹ��
	 * @return
	 */
	public short getTupleSize() {
	    ensureInMemory();
		return (short)tupleOffset.length;
	}
	
	/**
	 * �õ�ָ����Ԫ��
	 * @param tupleID
	 * @return
	 */
	public Tuple getTuple(short tupleID) {
	    ensureInMemory();
		return new DefaultTuple(tupleOffset[tupleID]);
	}
	
	/**
	 * �ϲ��ڲ���ʱ����table�ϵ�insert,���ܵ������������
	 * ֻ�ܼ������һ��Ԫ��ǰ��,
	 * ����֮ǰ��Ԫ�鱻ɾ��,�ռ�Ҳ��������
	 * @param insertItems ���ͱ�����schema��Ӧ�����ڲ������ٽ������ͼ��
	 * @return null ����ʧ��,�ռ䲻�� �� offset����ײ�
	 */
	public Tuple insertTuple(DataItem[] insertItems) {
	    ensureInMemory();
	    if (tupleOffset.length != 0 &&
	            tupleOffset[tupleOffset.length - 1]
	                == schema.firstPermittedTupleOffset())
	        return null;
	    
	    short[] newOffsetArray = new short[tupleOffset.length + 1];
	    System.arraycopy(tupleOffset, 0, newOffsetArray, 0, tupleOffset.length);
	    // ��Ԫ���ƫ����
	    short newTupleOffset = tupleOffset.length == 0 ?
	            (short)(DiskManagement.BLOCK_SIZE - schema.tupleLength()) :
	                (short)(tupleOffset[tupleOffset.length - 1] - schema.tupleLength());
	    newOffsetArray[newOffsetArray.length - 1] = newTupleOffset;
	    tupleOffset = newOffsetArray;
	    modifyDataArray();
	    
	    // ������д������
	    DefaultTuple newTuple = new DefaultTuple(newTupleOffset);
	    for (byte i = 0; i < schema.getAttributeSize(); i++) {
	        
            switch (schema.getAttributeType(i).getType()) {
            case AttributeType.BOOLEAN:
                newTuple.setBoolean(i,
                        ((BooleanItem)insertItems[i]).getData());
                break;
            case AttributeType.CHAR:
                newTuple.setChar(i,
                        ((CharItem)insertItems[i]).getData());
                break;
            case AttributeType.INTEGER:
                newTuple.setInt(i,
                        ((IntegerItem)insertItems[i]).getData());
                break;
            case AttributeType.LONG:
                newTuple.setLong(i,
                        ((LongItem)insertItems[i]).getData());
                break;
            case AttributeType.DOUBLE:
                newTuple.setDouble(i,
                        ((DoubleItem)insertItems[i]).getData());
                break;
            case AttributeType.TIME:
                newTuple.setTime(i,
                        ((TimeItem)insertItems[i]).getData());
                break;
            case AttributeType.STRING:
                newTuple.setString(i,
                        ((StringItem)insertItems[i]).getData());
                break;
            default:
                throw new IllegalArgumentException("unknown type");
            }
        }
	    return newTuple;
	}
	
	/**
	 * ɾ��ָ��λ�õ�Ԫ��
	 * @param position
	 * @return null λ�ó�����Χ
	 */
	public Tuple removeTuple(short position) {
	    ensureInMemory();
	    if (position < 0 || position >= tupleOffset.length)
            return null;
	    
	    short[] newOffsetArray = new short[tupleOffset.length - 1];
	    System.arraycopy(tupleOffset, 0, newOffsetArray, 0, position);
	    System.arraycopy(tupleOffset, position + 1, newOffsetArray,
	            position, newOffsetArray.length - position);
	    
	    Tuple deleted = new DefaultTuple(tupleOffset[position]);
	    tupleOffset = newOffsetArray;
	    modifyDataArray();
	    
	    modified = true;
	    return deleted;
	}
	
	/**
	 * �������ɾ��ʱ,��ƫ�����ı仯ֱ��д��data����
	 * ��֤��data������Եõ����µı仯
	 */
	private void modifyDataArray() {
		setShort(TUPLE_SIZE_OFFSET, (short) tupleOffset.length);
		for (int i = 0; i < tupleOffset.length; i++) {
            setShort(TUPLE_OFFSET_OFFSET + OFFSET_OFFSET * i,
                    tupleOffset[i]);
        }
    }
	
	/**
	 * initialze֮ǰ��tupleOffsetҲΪnull
	 * ����tupleOffset��initialize֮���ٴγ���nullʱ��
	 * �ض������ڸÿ鱻��������Ҫ���¶���
	 */
	private void ensureInMemory() {
        if (getData() == null) {
            BufferAccess bufferAccess = BufferAccess.getInstance();
            bufferAccess.reloadDataBlock(this);
            initialize(schema, false);
        }
    }
	
    protected void releaseMemory(DiskManagement disk) {
        if (modified) {
            modified = false;
            super.writeBlock(disk);
        }
        prev = null;
        next = null;
        identical = null;
        keepNextTurn = false;
        tupleOffset = null;
        setData(null);
    }
	 
	private class DefaultTuple implements Tuple {
        /*
         * ��Ԫ��Ļ���ƫ����
         */
        int baseOffset;

        private DefaultTuple(int offset) {
            this.baseOffset = offset;
        }

        /**
         * ʱ���
         */
        public Date getLastModified() {
    	    ensureInMemory();
            return DataBlock.this.getTime(baseOffset);
        }

        private void updateLastModified() {
    	    ensureInMemory();
            DataBlock.this.setTime(baseOffset, new Date());
            modified = true;
        }

        /**
         * ���º���Ϊ��ȡ�ֶ�
         * 
         * @param position
         *            ��position���ֶ�
         * @param value
         */
        private void setTime(byte position, Date value) {
            DataBlock.this.setTime(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private Date getTime(byte position) {
            return DataBlock.this.getTime(baseOffset
                    + schema.getAttributeOffset(position));
        }

        private void setInt(byte position, int value) {
            DataBlock.this.setInt(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private int getInt(byte position) {
            return DataBlock.this.getInt(baseOffset
                    + schema.getAttributeOffset(position));
        }

        private void setDouble(byte position, double value) {
            DataBlock.this.setDouble(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private double getDouble(byte position) {
            return DataBlock.this.getDouble(baseOffset
                    + schema.getAttributeOffset(position));
        }

        private void setChar(byte position, char value) {
            DataBlock.this.setChar(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private char getChar(byte position) {
            return DataBlock.this.getChar(baseOffset
                    + schema.getAttributeOffset(position));
        }

        private void setBoolean(byte position, boolean value) {
            DataBlock.this.setBoolean(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private boolean getBoolean(byte position) {
            return DataBlock.this.getBoolean(baseOffset
                    + schema.getAttributeOffset(position));
        }

        private void setString(byte position, String value) {
            DataBlock.this.setString(baseOffset
                    + schema.getAttributeOffset(position), value, schema
                    .getAttributeByteLength(position) / 2);
            updateLastModified();
        }

        private String getString(byte position) {
            return DataBlock.this.getString(baseOffset
                    + schema.getAttributeOffset(position), schema
                    .getAttributeByteLength(position) / 2);

        }

        private void setLong(byte position, long value) {
            DataBlock.this.setLong(baseOffset
                    + schema.getAttributeOffset(position), value);
            updateLastModified();
        }

        private long getLong(byte position) {
            return DataBlock.this.getLong(baseOffset
                    + schema.getAttributeOffset(position));
        }

        public DataItem getItem(byte position) {
    	    ensureInMemory();
    	    
            switch (schema.getAttributeType(position).getType()) {
            case AttributeType.BOOLEAN:
                return new BooleanItem(getBoolean(position));
            case AttributeType.CHAR:
                return new CharItem(getChar(position));
            case AttributeType.INTEGER:
                return new IntegerItem(getInt(position));
            case AttributeType.LONG:
                return new LongItem(getLong(position));
            case AttributeType.DOUBLE:
                return new DoubleItem(getDouble(position));
            case AttributeType.TIME:
                return new TimeItem(getTime(position));
            case AttributeType.STRING:
                return new StringItem(getString(position));
            default:
                return null;
            }
        }

        public void setItem(byte position, DataItem item) {
    	    ensureInMemory();
    	    
            switch (schema.getAttributeType(position).getType()) {
            case AttributeType.BOOLEAN:
                setBoolean(position, ((BooleanItem)item).getData());
                break;
            case AttributeType.CHAR:
                setChar(position, ((CharItem)item).getData());
                break;
            case AttributeType.INTEGER:
                setInt(position, ((IntegerItem)item).getData());
                break;
            case AttributeType.LONG:
                setLong(position, ((LongItem)item).getData());
                break;
            case AttributeType.DOUBLE:
                setDouble(position, ((DoubleItem)item).getData());
            case AttributeType.TIME:
                setTime(position, ((TimeItem)item).getData());
                break;
            case AttributeType.STRING:
                setString(position, ((StringItem)item).getData());
                break;
            default:
            }
        }

    }
		
}
 
