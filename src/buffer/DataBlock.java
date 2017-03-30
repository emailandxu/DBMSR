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
 *磁盘块
 */
public class DataBlock extends Block{
	private static final int TUPLE_SIZE_OFFSET = 0;
	public static final int TUPLE_OFFSET_OFFSET = 2;
	
	private static final int OFFSET_OFFSET = 2;
	
	/**
	 * 用于在块池中构建双向环链表，实现时钟算法(LRU的近似算法)
	 * 此两属性加于此并不合适，
	 * 应该在BufferAccess中添加一个内隐的DataBlock的包裹类，作为包裹类的属性，
	 * 但由于此系统效率至关重要，为避免产生过多的对象，将包裹类省去
	 * package access，只在本package中有意义
	 */
	DataBlock prev;
	DataBlock next;
	
	/**
	 * 相同块的链，只有一块大小的空间存在cache中
	 * 产生原因：
	 * 第一块使用后被抛弃，但引用仍在使用中
	 * 相同块号的另一块产生，随后第一块重新投入使用，便产生了相同块号的快链
	 */
	DataBlock identical;

	/**
	 * 表明在下一轮中该块是否要保存在内存中
	 * 当getDataBlock，及以下的getTuple等取快中数据的函数被调用时，
	 * 都应该设为true
	 */
	boolean keepNextTurn;
	
	/**
	 * 块所遵循的模式
	 */
	private Schema schema;
		
	/**
	 * 偏移量数组改变时直接写入底层数组
	 */
	private short[] tupleOffset;
	
	DataBlock(byte[] buffer, IDEntry id) {
		super(buffer, id);
	}

	/**
	 * 每个块使用前都要初始化,为防止多次初始化,必须先判断
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
	 * 包含的元组数，遍历时使用
	 * @return
	 */
	public short getTupleSize() {
	    ensureInMemory();
		return (short)tupleOffset.length;
	}
	
	/**
	 * 得到指定的元组
	 * @param tupleID
	 * @return
	 */
	public Tuple getTuple(short tupleID) {
	    ensureInMemory();
		return new DefaultTuple(tupleOffset[tupleID]);
	}
	
	/**
	 * 上层在插入时调用table上的insert,不能调用这个函数，
	 * 只能加在最后一个元组前面,
	 * 就算之前的元组被删除,空间也不能利用
	 * @param insertItems 类型必须与schema对应，在内部不会再进行类型检查
	 * @return null 插入失败,空间不足 或 offset到达底部
	 */
	public Tuple insertTuple(DataItem[] insertItems) {
	    ensureInMemory();
	    if (tupleOffset.length != 0 &&
	            tupleOffset[tupleOffset.length - 1]
	                == schema.firstPermittedTupleOffset())
	        return null;
	    
	    short[] newOffsetArray = new short[tupleOffset.length + 1];
	    System.arraycopy(tupleOffset, 0, newOffsetArray, 0, tupleOffset.length);
	    // 新元组的偏移量
	    short newTupleOffset = tupleOffset.length == 0 ?
	            (short)(DiskManagement.BLOCK_SIZE - schema.tupleLength()) :
	                (short)(tupleOffset[tupleOffset.length - 1] - schema.tupleLength());
	    newOffsetArray[newOffsetArray.length - 1] = newTupleOffset;
	    tupleOffset = newOffsetArray;
	    modifyDataArray();
	    
	    // 将数据写入数组
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
	 * 删除指定位置的元组
	 * @param position
	 * @return null 位置超出范围
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
	 * 当插入或删除时,将偏移量的变化直接写入data数组
	 * 保证从data数组可以得到最新的变化
	 */
	private void modifyDataArray() {
		setShort(TUPLE_SIZE_OFFSET, (short) tupleOffset.length);
		for (int i = 0; i < tupleOffset.length; i++) {
            setShort(TUPLE_OFFSET_OFFSET + OFFSET_OFFSET * i,
                    tupleOffset[i]);
        }
    }
	
	/**
	 * initialze之前，tupleOffset也为null
	 * 但当tupleOffset被initialize之后，再次出现null时，
	 * 必定是由于该块被丢弃，需要重新读入
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
         * 本元组的基本偏移量
         */
        int baseOffset;

        private DefaultTuple(int offset) {
            this.baseOffset = offset;
        }

        /**
         * 时间戳
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
         * 以下函数为存取字段
         * 
         * @param position
         *            第position个字段
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
 
