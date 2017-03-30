package buffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import diskaccess.DiskManagement;



/**
 * �黺��أ�������ʱ���㷨(LRU�Ľ����㷨)�����
 * ԭ��
 * ���ȹ������ء����óصĴ�С
 * �����µĶ�����뵽����ʱ�򣬳��Ѿ������ǰ�ɾ�����û�б�ʹ�õĶ���Ȼ��������
 * ���ӳ��ж�ȡ����ʱ�򣬸��������ӳ��л�ö���
 * Ȼ��ѳص�ָ��ָ���ȡ���Ķ����Ա����ö��������̱�ʹ�ù�
 * @author zh
 */

public class BufferAccess {
	/**
	 * ����ش�С��56MB
	 */
	public static final int cacheSize = 16 * 1024 * 1024 / DiskManagement.BLOCK_SIZE;
	
	private static BufferAccess bufferAccessInstance = null;
	
    private DiskManagement diskManagement;
    
    /**
     * ��ס�Ĺؼ���,ֻ���ļ���ʱ����,�ļ��ر�ʱȡ��
     * ���mata, schema, index block, index info block
     */
    private Set LockedBlockSet = new HashSet();
    
    /**
     * �黺���,load factor 0.75,ȷ��û��rehash����
     * ���������ֻ��DataBlock
     */
    private Map blockCache = new HashMap( (int)(cacheSize / 0.75));
    
    /**
     * ʱ���㷨�Ļ��еĵ�ǰԪ�أ���һ��Ԫ�ز�����̭��ѡ����
     */
    private DataBlock lastUsedBlock;
	 
	private BufferAccess() {
		this.diskManagement = DiskManagement.getInstance();
	}
	
	public static BufferAccess getInstance() {
		if (bufferAccessInstance == null) {
			bufferAccessInstance = new BufferAccess();
		}
		return bufferAccessInstance;
	}
	
	/**
	 * ��buffer�еõ�һ�����ݿ�
	 * @param fileID
	 * @param blockID
	 * @return
	 */
	public DataBlock getBlock(Integer fileID, int blockID ) {
		IDEntry id = new IDEntry(fileID, blockID);
		
		DataBlock block = (DataBlock)blockCache.get(id);
		
		if (block != null) {
		    block.keepNextTurn = true;
			return block;
		}
		
		byte[] blockByte = diskManagement.readFromDisk(fileID, blockID);
		block = new DataBlock(blockByte, id);

		ensureSpace();
		addBlock(block);
		blockCache.put(id, block);
		return block;
	}
	
	void reloadDataBlock(DataBlock nullBlock) {
	    IDEntry id = nullBlock.getIdEntry();
	    DataBlock block = (DataBlock)blockCache.get(id);

	    // �ڴ���û����ͬid�Ŀ�
	    if (block == null) {
			byte[] blockByte = diskManagement.readFromDisk(id.getFileID(), id.getBlockID());
			ensureSpace();
			
			nullBlock.setData(blockByte);
			addBlock(nullBlock);
			blockCache.put(id, nullBlock);
	        return;
        }
        // �ڴ��д�����ͬid�Ŀ飬��Ҫ�õ�identicalָ��
	    nullBlock.setData(block.getData());
	    nullBlock.identical = block.identical;
	    block.identical = nullBlock;
	}
	
	/**
	 * �õ�һ����ס�Ŀ飬�ÿ鲻����LRU�㷨�ľ���
	 * ֻ������MetaBlock,SchemaBlock,IndexBlock
	 * @param fileID
	 * @param blockID
	 * @param blockType
	 * @return
	 */
	public Block getLockedBlock(Integer fileID, int blockID, int blockType) {
		IDEntry id = new IDEntry(fileID, blockID);
		
		Block block = null;
		
		byte[] blockByte = diskManagement.readFromDisk(fileID, blockID);
		switch (blockType) {
			case Block.META_BLOCK:
				block = new MetaBlock( blockByte, id);
				break;
			case Block.SCHEMA_BLOCK:
				block = new SchemaBlock( blockByte, id);
				break;
			case Block.INDEX_BLOCK:
				block = new IndexBlock( blockByte, id);
				break;
			case Block.INDEX_INFO_BLOCK:
				block = new IndexInfoBlock( blockByte, id);
				break;
			default:
				System.out.println(" illegal block type argument ");
		}
		LockedBlockSet.add( block);
		
		return block;
	}
	
	/**
	 * ���Ȼ�������������Ҫ�����ϵĿ�
	 * ȷ��������һ����λ��
	 */
	private void ensureSpace() {
		if (blockCache.size() == cacheSize) {
		    lastUsedBlock = lastUsedBlock.next;
		    while (lastUsedBlock.keepNextTurn) {
		        lastUsedBlock.keepNextTurn = false;
		        lastUsedBlock = lastUsedBlock.next;
            }
		    removeBlock(lastUsedBlock);
		}
	}
	
	/**
	 * ��lastUsedElement�������Ԫ��
	 * @param blk
	 */
	private void addBlock(DataBlock blk) {
		if (lastUsedBlock == null) {
		    blk.next = blk;
		    blk.prev = blk;
		    lastUsedBlock = blk;
		    return;
		}
		
		DataBlock next = lastUsedBlock.next;
		
		lastUsedBlock.next = blk;
		blk.next = next;
		
		blk.prev = lastUsedBlock;
		next.prev = blk;
		
		lastUsedBlock = blk;
	}
	
	/**
	 * ��ָ���Ŀ�ɾ��
	 * ���ڱ�Ҫʱ�ı�lastUsedBlcok
	 * @param block
	 */
	private void removeBlock(DataBlock block) {
	    block.prev.next = block.next;
	    block.next.prev = block.prev;
	    blockCache.remove(block.getIdEntry());

	    if (block == lastUsedBlock) {
		    lastUsedBlock = lastUsedBlock == lastUsedBlock.prev ?
		            null : lastUsedBlock.prev;
        }
	    
	    while (block != null) {
		    DataBlock identical = block.identical;
		    block.releaseMemory(diskManagement);
		    block = identical;
        }
    }
	
	/**
	 * �رձ��ļ�
	 * ��block cache�����ض��ļ������п���գ��б�Ҫʱд���ļ�
	 * @param fileID
	 */
	public void closeTableFile(Integer fileID) {
	    Set entrySet = blockCache.entrySet();
	    for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
			if (((IDEntry)entry.getKey()).getFileID().equals(fileID)) {
			    iter.remove();
			    DataBlock block = (DataBlock)entry.getValue();
				removeBlock(block);
				block.releaseMemory(diskManagement);
			}
        }
		diskManagement.closeFile(fileID);
	}
	
	/**
	 * �ر����ݿ��ļ�
	 * ��locked block�����ض��ļ������п���գ��б�Ҫʱд���ļ�
	 * @param fileID
	 */
	public void closeDBFile(Integer fileID) {
	    for (Iterator iter = LockedBlockSet.iterator(); iter.hasNext();) {
			Block block = (Block) iter.next();
			IDEntry id = block.getIdEntry();
			if (id.getFileID().equals(fileID)) {
				iter.remove();
				block.releaseMemory(diskManagement);
			}
		}
		diskManagement.closeFile(fileID);
	}
	
	/**
	 * @param fileID
	 */
	public void removeTableFile(Integer fileID) {
	    Set entrySet = blockCache.entrySet();
	    for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
			if (((IDEntry)entry.getKey()).getFileID().equals(fileID)) {
			    iter.remove();
			    DataBlock block = (DataBlock)entry.getValue();
				removeBlock(block);
				block.releaseMemory(diskManagement);
			}
        }
		diskManagement.removeFile(fileID);
	}
	
	/**
	 * @return
	 */
	public Integer createFile(String fileName) {
		return diskManagement.createFile(fileName);
	}
	
	/**
	 * @return
	 */
	public Integer createTempFile(String dir, String dbName) {
		return diskManagement.createTempFile(dir, dbName);
	}
	
	/**
	 * @param fileName
	 * @return
	 */
	public Integer loadFile(String fileName) {
		return diskManagement.loadFile(fileName);
	}
	
	/**
	 * @param fileID
	 * @return
	 */
	public int getNewBlockID(Integer fileID) {
		return diskManagement.getNewBlockID(fileID);
	}
	
	/**
	 * @param fileID
	 * @return
	 */
	public int getBlockSize(Integer fileID) {
		return diskManagement.getBlockSize(fileID);
	}
}
 
