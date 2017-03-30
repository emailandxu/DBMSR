package recordmanagement;

import buffer.IndexBlock;
import buffer.IndexInfoBlock;
import dataitem.DataItem;


/**
 * B+��
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
     * ����ֱ��Ҷ�ڵ㣬���ؼ���ָ�Ŀ�ĵ�ַ
     * @param key
     * @return -1 ������
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
     * �����¼�
     * @param key
     * @param pointer
     */
    public void insert(DataItem key, int pointer) {
        IndexBlock rootNode = getNode(indexInfoBlock.getRoot());
        insertRecursive(key, pointer, rootNode, 0);
        if (rootNode.isBeyondSpace()) {
            int newNodePointer = getNextNewNode();
            IndexBlock newNode = getNode(newNodePointer);
            // ��ָ��ڵ�
            DataItem splitKey =
                indexInfoBlock.getLevel() == 0 ?
                rootNode.splitLeafNode(newNode, newNodePointer) : // ���ڵ�Ҳ��Ҷ�ڵ�
                rootNode.splitInternalNode(newNode);
                
            int newRootNodePointer = getNextNewNode();
            IndexBlock newRootNode = getNode(newRootNodePointer);
            newRootNode.initializeRootNode(splitKey,
                    indexInfoBlock.getRoot(), newNodePointer);
            
            // �޸�������
            indexInfoBlock.setRoot(newRootNodePointer);
            indexInfoBlock.plusLevel();
        }
    }
    
    /**
     * �ݹ����
     * @param key
     * @param pointer
     * @param thisNode
     * @param thisLevel
     * @return -1 ��thisNode���������룬���򷵻ز����Ҷ�ڵ��ַ
     */
    private int insertRecursive(DataItem key, int pointer, IndexBlock thisNode, int thisLevel) {
        if (thisLevel < indexInfoBlock.getLevel()) {
            int nextNodeloc = thisNode.locateInternalItem(key);
            IndexBlock nextNode = getNode(thisNode.getPointerAt(nextNodeloc));
            // �ݹ�
            int loc = insertRecursive(key, pointer, nextNode, thisLevel + 1);
            
            // Ѱ�Ҳ���λ��
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
                    
                // ��ֺ��´���ԭ�ڵ�ļ�Ҳ�����仯,��һ������
                if (nextNodeloc != 0)
                    thisNode.setKeyAt(nextNodeloc - 1,
                        nextNode.getRepresentKey(
                                thisNode.getKeyAt(nextNodeloc - 1)));
                thisNode.insertInternalItem(splitKey, newNodePointer, nextNodeloc);
            }
            return -1;
        }
        else {
            // ��˳������Ѱ�Ҳ����
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
                
                // �ҵ���
                if (findLeaf(key, loc, nextNode, thisLevel + 1)) {
                    if (nextNode.isBeyondSpace()) {
                        int newNodePointer = getNextNewNode();
                        IndexBlock newNode = getNode(newNodePointer);
                        
                        DataItem splitKey = nextNode.splitInternalNode(newNode);
                            
                        // ��ֺ��´���ԭ�ڵ�ļ�Ҳ�����仯,��һ������
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
            // ���ļ�ֵ,����һ����
            if (at != 0)
                thisNode.setKeyAt(at - 1, key);
            
            IndexBlock nextNode = getNode(loc);
            if (nextNode.isBeyondSpace()) {
                int newNodePointer = getNextNewNode();
                IndexBlock newNode = getNode(newNodePointer);
                
                DataItem splitKey = nextNode.splitLeafNode(newNode, newNodePointer);
                    
                // ��ֺ��´���ԭ�ڵ�ļ�Ҳ�����仯,��һ������
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
     * ɾ����
     * @param key
     * @return ָ��Ŀ�ĵ�ַ
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
     * �õ��¿�,����ʼ��,�Ա�����
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
 
