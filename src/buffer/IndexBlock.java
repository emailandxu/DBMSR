/*
 * Created on 2005-1-24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package buffer;

import java.util.Arrays;

import recordmanagement.AttributeType;
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
 * @author zh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IndexBlock extends Block {
    private static final int NOT_NULL_OFFSET = 1;
    private static final int POINTER_OFFSET = 4;

    private static final int KEY_NUMBER_OFFSET = 0;
    private static final int LAST_POINTER_OFFSET = 2;
    private static final int STARTING_OFFSET =
        					LAST_POINTER_OFFSET + POINTER_OFFSET;
    
    /**
     * �ڵ���key�ĸ���
     */
    private short keyNum;

    /**
     * �ӽڵ��ַ
     */
    private int[] pointerArray;

    /**
     * ������
     */
    private AttributeType type;
    
    /**
     * ����
     */
    private DataItem[] keyArray;
    
	/**
	 * @param buffer
	 * @param id
	 */
	IndexBlock(byte[] buffer, IDEntry id) {
		super(buffer, id);
	}
	
	public void initialize(AttributeType type, boolean byExternal) {
	    if (initialized()) {
            if (byExternal) {
                throw new IllegalArgumentException("new block has an error");
            }
            return;
        }
	    
	    this.type = type;

	    int n = type.getIndexCapacity();
	    // �������鶼��һ��������룬ʵ����ֻ����n��ָ�룬n-1����
	    pointerArray = new int[n + 2];
        keyArray = new DataItem[n + 1];
        
	    if (byExternal) {
	        setData(null);
	        keyNum = 0;
	        
	        // Ҷ�ڵ������һ��˳��ָ���ʼ��
	        pointerArray[n + 1] = -1;
	        modified = true;
	        return;
        }
	    
	    keyNum = getShort(KEY_NUMBER_OFFSET);
	    pointerArray[n + 1] = getInt(LAST_POINTER_OFFSET);
	    
        int offset = STARTING_OFFSET;
        for (int i = 0; i <= keyNum; i++) {
            pointerArray[i] = getInt(offset);
            offset += POINTER_OFFSET; 
        }
        // �����
        readKey(offset);

		// ����Ҫ��˴�Ŀռ���
		setData(null);
	}
	
	private void readKey(int offset) {
	    int	keyByteAndNullFlagLength =
	        	type.getByteLength() + NOT_NULL_OFFSET;
	    
        switch (type.getType()) {
        case AttributeType.BOOLEAN:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new BooleanItem(getBoolean(offset
                            + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.CHAR:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new CharItem(
                            getChar(offset + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.INTEGER:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new IntegerItem(getInt(offset
                            + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.LONG:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new LongItem(
                            getLong(offset + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.DOUBLE:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new DoubleItem(getDouble(offset
                            + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.TIME:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new TimeItem(
                            getTime(offset + NOT_NULL_OFFSET));
                offset += keyByteAndNullFlagLength;
            }
            break;
        case AttributeType.STRING:
            for (int i = 0; i < keyNum; i++) {
                if (getBoolean(offset))
                    keyArray[i] = new StringItem(getString(offset
                            + NOT_NULL_OFFSET, keyByteAndNullFlagLength / 2));
                offset += keyByteAndNullFlagLength;
            }
            break;
        default:
            System.out.println("illegal argument");
            break;
        }
	}

    protected void releaseMemory(DiskManagement disk) {
        if (modified) {
            int size = STARTING_OFFSET + (keyNum + 1) * POINTER_OFFSET + keyNum
                    * (type.getByteLength() + NOT_NULL_OFFSET);
            byte[] dt = new byte[size];
            setData(dt);

            setShort(KEY_NUMBER_OFFSET, keyNum);
            setInt(LAST_POINTER_OFFSET, pointerArray[pointerArray.length - 1]);

            int offset = STARTING_OFFSET;
            for (int i = 0; i <= keyNum; i++) {
                setInt(offset, pointerArray[i]);
                offset += POINTER_OFFSET;
            }
            writeKey(offset);
            
            modified = false;
            super.writeBlock(disk);
        }
		// ����ڴ�ռ�
        pointerArray = null;
        keyArray = null;
    }
    
    private void writeKey(int offset) {
	    int	keyByteAndNullFlagLength =
	        			type.getByteLength() + NOT_NULL_OFFSET;
        switch (type.getType()) {
        case AttributeType.BOOLEAN:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setBoolean(offset + NOT_NULL_OFFSET, ((BooleanItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.CHAR:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setChar(offset + NOT_NULL_OFFSET, ((CharItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.INTEGER:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setInt(offset + NOT_NULL_OFFSET, ((IntegerItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.LONG:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setLong(offset + NOT_NULL_OFFSET, ((LongItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.DOUBLE:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setDouble(offset + NOT_NULL_OFFSET, ((DoubleItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.TIME:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setTime(offset + NOT_NULL_OFFSET, ((TimeItem)keyArray[i]).getData() );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        case AttributeType.STRING:
            for (int i = 0; i < keyNum; i++) {
                if (keyArray[i] != null) {
                    setBoolean(offset, true);
                    setString(offset + NOT_NULL_OFFSET,
                            ((StringItem)keyArray[i]).getData(),
                            keyByteAndNullFlagLength / 2 );
                }
                else {
                    setBoolean(offset, false);
                }
                offset += keyByteAndNullFlagLength; 
            }
            break;
        default:
            System.out.println("illegal argument");
            break;
        }
	}
    
    
    /**
     * ������B+���Ĳ���
     * 
     * ���ڵ����ʱ������ʼ��
     * @param key
     * @param lessPointer
     * @param greaterPointer
     */
    public void initializeRootNode(DataItem key,
            int lessPointer, int greaterPointer) {
        keyArray[0] = key;
        pointerArray[0] = lessPointer;
        pointerArray[1] = greaterPointer;
        keyNum++;
    }
    
    /**
     * ����Ҷ�ڵ�����ȵ�ֵ��λ��
     * @param key
     * @return -1 ��������ڣ���pointerArray�е�λ��
     */
    public int findLeafItem(DataItem key) {
        for (int j = 0; j < keyNum; j++) {
            if (keyArray[j].equals(key))
                return j;
        }
        return -1;
    }
    
    /**
     * �����ڲ��ڵ�����һ���ڵ��λ��
     * @param key
     * @return ��pointerArray�е�λ��
     */
    public int locateInternalItem(DataItem key) {
        boolean processNull = false;
        int d = -1;
        for (int j = 0; j < keyNum; j++) {
            if (processNull) {
                if (keyArray[j] != null) {
                    if (keyArray[j].compareTo(key) > 0)
                        return d;
                    else
                        processNull = false;
                }
            }
            else if (keyArray[j] == null) {
                d = j;
                processNull = true;
            }
            else if (keyArray[j].compareTo(key) > 0)
                return j;
        }
        return processNull ? d : keyNum;
    }
    
    /**
     * �����ڲ��ڵ�����һ���ڵ��λ��
     * @param pointer
     * @return ��pointerArray�е�λ��
     */
    public int locateInternalPointer(int pointer) {
        for (int j = 0; j <= keyNum; j++) {
            if (pointerArray[j] == pointer)
                return j;
        }
        return -1;
    }
    
    /**
     * ����ʱ�����Ƿ�����֮���ټ��
     * Ҷ�ڵ���ָ����һ�ڵ��ָ��ʼ����pointerArray�����һ��λ��
     * @param newItem
     * @return
     */
    public void insertLeafItem(DataItem newItem, int pointer) {
        keyNum++; // �ڵ�����һ
        for (int j = keyNum - 2; j > -1; j--) {
            if (keyArray[j].compareTo(newItem) > 0) {
                keyArray[j + 1] = keyArray[j];
                pointerArray[j + 1] = pointerArray[j];
            }
            else {
                keyArray[j + 1] = newItem;
                pointerArray[j + 1] = pointer;
                return;
            }
        }
        keyArray[0] = newItem;
        pointerArray[0] = pointer;
    }
    
    /**
     * ����ʱ�����Ƿ�����֮���ټ��
     * �ڲ��ڵ�ĵ�һ��ָ��ʼ�ղ���
     * @param newItem
     * @param pointer
     * @param loc ����λ�ã�֮ǰ�Ѿ�ȷ��
     */
    public void insertInternalItem(DataItem newItem, int pointer, int loc) {
        keyNum++; // �ڵ�����һ
        for (int j = keyNum - 2; j >= loc; j--) {
            keyArray[j + 1] = keyArray[j];
            pointerArray[j + 2] = pointerArray[j + 1];
        }
        keyArray[loc] = newItem;
        pointerArray[loc + 1] = pointer;
    }
    
    /**
     * ɾ��һ����
     * @param item
     * @return ����Ӧ��ָ��, -1 û�������
     */
    public int deleteItem(DataItem item) {
        int j = 0, loc = -1;
        for (; j < keyNum; j++) {
            if (keyArray[j].equals(item)) {
                loc = pointerArray[j];
                keyArray[j] = null;
                keyNum--;
                break;
            }
        }
        for (; j < keyNum; j++) {
            pointerArray[j] = pointerArray[j + 1];
            keyArray[j] = keyArray[j + 1];
        }
        if (loc != -1) {
            keyArray[j] = null;
        }
        return loc;
    }
    
    /**
     * ���Ҷ�ڵ�
     * @param newNode
     * @param newNodePointer �½ڵ��ָ��,����˳��ָ��
     * @return null �½ڵ������¼�
     */
    public DataItem splitLeafNode(IndexBlock newNode, int newNodePointer) {
        // ��������
        newNode.keyNum = (short)(keyNum / 2);
        keyNum = (short)((keyNum + 1) / 2);
        
        // ���Ƽ���ָ��
        System.arraycopy(keyArray, keyNum,
                newNode.keyArray, 0, newNode.keyNum);
        Arrays.fill(keyArray, keyNum, keyArray.length, null);
        System.arraycopy(pointerArray, keyNum,
                newNode.pointerArray, 0, newNode.keyNum);
        
        // ��Ҷ�ڵ��˳��ָ�����pointerArray�����һ��λ��
        int nextNodePointer = pointerArray.length - 1;
        newNode.pointerArray[nextNodePointer] = pointerArray[nextNodePointer];
        
        // ��˳��ָ��ָ��newNode
        pointerArray[nextNodePointer] = newNodePointer;
        
        // Ѱ���¼�
        DataItem lastItem = keyArray[keyNum - 1];
        for (int i = 0; i < newNode.keyNum; i++) {
            if ( ! newNode.keyArray[i].equals(lastItem))
                return newNode.keyArray[i];
        }
        return null;
    }
    
    /**
     * ����ڲ��ڵ�
     * @param newNode
     * @return ��ֳ��Ķ�����ֵ
     */
    public DataItem splitInternalNode(IndexBlock newNode) {
        // ���Ƽ���ָ��
        System.arraycopy(keyArray, (keyNum + 2) / 2,
                newNode.keyArray, 0, (keyNum - 1) / 2);
        System.arraycopy(pointerArray, (keyNum + 2) / 2,
                newNode.pointerArray, 0, (keyNum + 1) / 2);
        
        // ��������
        newNode.keyNum = (short)((keyNum - 1) / 2);
        keyNum = (short)(keyNum / 2);
        
        // �����ڵ�����ϲ�ڵ�,Ѱ�ҷ�null�¼�
        DataItem splitKey = keyArray[keyNum];
        Arrays.fill(keyArray, keyNum, keyArray.length, null);
        
        // ��֤��null
        if (splitKey == null) {
            for (int i = 0; i < newNode.keyNum; i++) {
                if (newNode.keyArray[i] != null)
                    return newNode.keyArray[i];
            }
        }
        return splitKey;
    }
    
    /**
     * @return ˳��ָ��,ֻ��Ҷ�ڵ���������
     */
    public int nextLeafNode() {
        return pointerArray[pointerArray.length - 1];
    }
    
    /**
     * ����B+ tree����
     * @return
     */
    public boolean isBeyondSpace() {
        return keyNum == keyArray.length;
    }
    
    /**
     * ֻ��Ҷ�ڵ�����,ȫ����С�ڵ���key
     * ����keyNum != 0
     * @param key
     * @return
     */
    public boolean isTotalLessOrEqual(DataItem key) {
        return keyArray[keyNum - 1].compareTo(key) <= 0;
    }
    
    /**
     * ֻ��Ҷ�ڵ�����,���ڼ�С��key
     * ����keyNum != 0
     * @param key
     * @return
     */
    public boolean hasLessOrEqual(DataItem key) {
        return keyArray[0].compareTo(key) <= 0;
    }
    
    public int getItemNum() {
        return keyNum;
    }
    
    public int getPointerAt(int num) {
        return pointerArray[num];
    }
    
    public void setKeyAt(int num, DataItem key) {
        keyArray[num] = key;
    }
    
    public DataItem getKeyAt(int num) {
        return keyArray[num];
    }
    
    public DataItem getRepresentKey(DataItem old) {
        if (old == null) {
            return null;
        }
        
        for (int j = 0; j < keyNum; j++) {
            if (keyArray[j] != null
                    && keyArray[j].compareTo(old) >= 0)
                return keyArray[j];
        }
        return null;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" keyNum = " + keyNum + " : ");
        for (int j = 0; j < keyNum; j++)
            sb.append(keyArray[j]);
        return sb.toString();
    }
}
