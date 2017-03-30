package buffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import diskaccess.DiskManagement;



/**
 * 块缓冲池－－采用时钟算法(LRU的近似算法)管理块
 * 原理
 * 首先构造对象池、设置池的大小
 * 当把新的对象放入到池中时候，池已经满，那把删除最久没有被使用的对象，然后放入对象
 * 当从池中读取对象时候，根据条件从池中获得对象；
 * 然后把池的指针指向该取出的对象，以表明该对象最近最短被使用过
 * @author zh
 */

public class BufferAccess {
	/**
	 * 缓冲池大小，56MB
	 */
	public static final int cacheSize = 16 * 1024 * 1024 / DiskManagement.BLOCK_SIZE;
	
	private static BufferAccess bufferAccessInstance = null;
	
    private DiskManagement diskManagement;
    
    /**
     * 锁住的关键块,只在文件打开时放入,文件关闭时取出
     * 存放mata, schema, index block, index info block
     */
    private Set LockedBlockSet = new HashSet();
    
    /**
     * 块缓冲池,load factor 0.75,确保没有rehash发生
     * 方便起见，只存DataBlock
     */
    private Map blockCache = new HashMap( (int)(cacheSize / 0.75));
    
    /**
     * 时钟算法的环中的当前元素，下一个元素才是淘汰候选对象
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
	 * 从buffer中得到一个数据块
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

	    // 内存中没有相同id的块
	    if (block == null) {
			byte[] blockByte = diskManagement.readFromDisk(id.getFileID(), id.getBlockID());
			ensureSpace();
			
			nullBlock.setData(blockByte);
			addBlock(nullBlock);
			blockCache.put(id, nullBlock);
	        return;
        }
        // 内存中存在相同id的块，需要用到identical指针
	    nullBlock.setData(block.getData());
	    nullBlock.identical = block.identical;
	    block.identical = nullBlock;
	}
	
	/**
	 * 得到一个锁住的块，该块不参与LRU算法的竞争
	 * 只可能是MetaBlock,SchemaBlock,IndexBlock
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
	 * 调度缓冲区，可能需要丢弃老的块
	 * 确保至少有一个空位置
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
	 * 在lastUsedElement后加入新元素
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
	 * 将指定的块删除
	 * 并在必要时改变lastUsedBlcok
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
	 * 关闭表文件
	 * 将block cache池中特定文件的所有块清空，有必要时写入文件
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
	 * 关闭数据库文件
	 * 将locked block池中特定文件的所有块清空，有必要时写入文件
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
 
