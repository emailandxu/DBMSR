package recordmanagement;

import buffer.IndexBlock;
import buffer.IndexInfoBlock;
import dataitem.DataItem;


/**
 * B+树
 *
 */
public class Index {
    
    private AttributeType keyType;

    private IndexInfoBlock indexInfoBlock;
    
    public Index(AttributeType keyType, IndexInfoBlock indexInfoBlock) {
        this.keyType  = keyType;
        this.indexInfoBlock = indexInfoBlock;
    }
    
	/**
     * 搜索直到叶节点，返回键所指的块的地址
     * @param key
     * @return -1 不存在
     */
    public int find(DataItem key) {
        IndexBlock curNode = getNode(indexInfoBlock.getRoot());
        for (int i = 0; i < indexInfoBlock.getLevel(); i++) {
            int loc = curNode.locateInternalItem(key);
            curNode = getNode( curNode.getPointerAt(loc) );
        }
        int d = curNode.findLeafItem(key);
        return d == -1 ?
                -1 : curNode.getPointerAt(d);
    }
    
    /**
     * 插入新键
     * @param key
     * @param pointer
     */
    public void insert(DataItem key, int pointer) {
        IndexBlock rootNode = getNode(indexInfoBlock.getRoot());
        insertRecursive(key, pointer, rootNode, 0);
        if (rootNode.isBeyondSpace()) {
            int newNodePointer = getNextNewNode();
            IndexBlock newNode = getNode(newNodePointer);
            // 拆分根节点
            DataItem splitKey =
                indexInfoBlock.getLevel() == 0 ?
                rootNode.splitLeafNode(newNode, newNodePointer) : // 根节点也是叶节点
                rootNode.splitInternalNode(newNode);
                
            int newRootNodePointer = getNextNewNode();
            IndexBlock newRootNode = getNode(newRootNodePointer);
            newRootNode.initializeRootNode(splitKey,
                    indexInfoBlock.getRoot(), newNodePointer);
            
            // 修改树参数
            indexInfoBlock.setRoot(newRootNodePointer);
            indexInfoBlock.plusLevel();
        }
    }
    
    /**
     * 递归插入
     * @param key
     * @param pointer
     * @param thisNode
     * @param thisLevel
     * @return -1 在thisNode中正常插入，否则返回插入的叶节点地址
     */
    private int insertRecursive(DataItem key, int pointer, IndexBlock thisNode, int thisLevel) {
        if (thisLevel < indexInfoBlock.getLevel()) {
            int nextNodeloc = thisNode.locateInternalItem(key);
            IndexBlock nextNode = getNode(thisNode.getPointerAt(nextNodeloc));
            // 递归
            int loc = insertRecursive(key, pointer, nextNode, thisLevel + 1);
            
            // 寻找插入位置
            if (loc != -1) {
         //       System.out.println(loc);
                return findLeaf(key, loc, thisNode, thisLevel) ?
                        -1 : loc;
            }
            if (nextNode.isBeyondSpace()) {
                int newNodePointer = getNextNewNode();
                IndexBlock newNode = getNode(newNodePointer);
                
                DataItem splitKey =
                    thisLevel + 1 == indexInfoBlock.getLevel() ?
                    nextNode.splitLeafNode(newNode, newNodePointer) :
                    nextNode.splitInternalNode(newNode);
                    
                // 拆分后导致代表原节点的键也发生变化,第一个除外
                if (nextNodeloc != 0)
                    thisNode.setKeyAt(nextNodeloc - 1,
                        nextNode.getRepresentKey(
                                thisNode.getKeyAt(nextNodeloc - 1)));
                thisNode.insertInternalItem(splitKey, newNodePointer, nextNodeloc);
            }
            return -1;
        }
        else {
            // 在顺序链中寻找插入点
            int preLoc = -1, curLoc = -1;
            IndexBlock pre = thisNode, cur = thisNode;
            while (cur.nextLeafNode() != -1) {
                if (cur.getItemNum() != 0) {
                    if (cur.isTotalLessOrEqual(key)) {
                        preLoc = curLoc;
                        pre = cur;
                    }
                    else
                        break;
                }
                curLoc = cur.nextLeafNode();
                cur = getNode(curLoc);
            }
            if (cur.getItemNum() == 0
                    || ! cur.hasLessOrEqual(key)) {
                curLoc = preLoc;
                cur = pre;
            }
            cur.insertLeafItem(key, pointer);
            return curLoc;
        }
    }
    
    private boolean findLeaf(DataItem key, int loc, IndexBlock thisNode, int thisLevel) {
        if (thisLevel + 1 < indexInfoBlock.getLevel()) {
            for (int i = 0; i <= thisNode.getItemNum(); i++) {
                int nextNodeloc = thisNode.getPointerAt(i);
                IndexBlock nextNode = getNode(nextNodeloc);
                
                // 找到了
                if (findLeaf(key, loc, nextNode, thisLevel + 1)) {
                    if (nextNode.isBeyondSpace()) {
                        int newNodePointer = getNextNewNode();
                        IndexBlock newNode = getNode(newNodePointer);
                        
                        DataItem splitKey = nextNode.splitInternalNode(newNode);
                            
                        // 拆分后导致代表原节点的键也发生变化,第一个除外
                        if (i != 0)
                            thisNode.setKeyAt(i - 1,
                                nextNode.getRepresentKey(
                                        thisNode.getKeyAt(i - 1)));
                        thisNode.insertInternalItem(splitKey, newNodePointer, i);
                    }
                    return true;
                }
            }
            return false;
        }
        else {
            int at = thisNode.locateInternalPointer(loc);
            if (at == -1) {
                return false;
            }
            // 更改键值,除第一个外
            if (at != 0)
                thisNode.setKeyAt(at - 1, key);
            
            IndexBlock nextNode = getNode(loc);
            if (nextNode.isBeyondSpace()) {
                int newNodePointer = getNextNewNode();
                IndexBlock newNode = getNode(newNodePointer);
                
                DataItem splitKey = nextNode.splitLeafNode(newNode, newNodePointer);
                    
                // 拆分后导致代表原节点的键也发生变化,第一个除外
                if (at != 0)
                    thisNode.setKeyAt(at - 1,
                        nextNode.getRepresentKey(
                                thisNode.getKeyAt(at - 1)));
                thisNode.insertInternalItem(splitKey, newNodePointer, at);
            }
            return true;
        }
    }
    
    /**
     * 删除键
     * @param key
     * @return 指向的块的地址
     */
    public int delete(DataItem key) {
        IndexBlock curNode = getNode(indexInfoBlock.getRoot());
        for (int i = 0; i < indexInfoBlock.getLevel(); i++) {
            int loc = curNode.locateInternalItem(key);
            curNode = getNode(curNode.getPointerAt(loc));
        }
        return curNode.deleteItem(key);
    }
    
    public void displayTree() {
        recDisplayTree(getNode(indexInfoBlock.getRoot()), 0);
    }

    private void recDisplayTree(IndexBlock thisNode, int thisLevel) {
        for (int i = 0; i < thisLevel; i++) {
            System.out.print("  ");
        }
        System.out.println("level = " + thisLevel + thisNode);
        
        if (thisLevel < indexInfoBlock.getLevel()) {
            int numItems = thisNode.getItemNum();
            for (int j = 0; j < numItems + 1; j++) {
                IndexBlock nextNode = getNode(thisNode.getPointerAt(j));
                recDisplayTree(nextNode, thisLevel + 1);
            }
        }
    }
    
    public void displaySequentially() {
        int cur = indexInfoBlock.getRoot();
        for (int i = 0; i < indexInfoBlock.getLevel(); i++) {
            cur = getNode(cur).getPointerAt(0);
        }
        int d = 1;
        System.out.print("nodes = " + d++);
        System.out.println(getNode(cur));
        while (getNode(cur).nextLeafNode() != -1) {
            cur = getNode(cur).nextLeafNode();
            System.out.print("nodes = " + d++);
            System.out.println(getNode(cur));
        }
    }
    
    private IndexBlock getNode(int id) {
        IndexBlock indexBlock = 
            RecordManagement.getInstance().getIndexBlock(id);
        indexBlock.initialize(keyType, false);
        return indexBlock;
    }
    
    /**
     * 得到新块,并初始化,以备后用
     * @return
     */
    private int getNextNewNode() {
        int id = RecordManagement.getInstance().getNewDBBlockID();
        IndexBlock indexBlock =
            RecordManagement.getInstance().getIndexBlock(id);
        indexBlock.initialize(keyType, true);
        return id;
    }
}
 
