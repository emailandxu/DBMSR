/*
 * Created on 2005-3-18
 *
 */
package buffer;

import diskaccess.DiskManagement;

/**
 * @author zh
 */
public class IndexInfoBlock extends Block{
    private static final int TREE_LEVEL_OFFSET = 0;
    private static final int ROOT_ID_OFFSET = 4;
    
    /**
     * 从0开始，根节点为0层
     */
    private int level;

    private int root;
    
    IndexInfoBlock(byte[] buffer, IDEntry id) {
        super(buffer, id);
    }
    
    /**
     * @param root 0 用data数据初始化，
     *               否则用外部的root
     */
    public void initialize(int root) {
        if (initialized()) {
            if (root != 0) {
                throw new IllegalArgumentException("new block has an error");
            }
            return;
        }
        
        if (root != 0) {
            setData(null);
            level = 0;
            this.root = root;
            modified = true;
            return;
        }
        
        this.level = getInt(TREE_LEVEL_OFFSET);
        this.root = getInt(ROOT_ID_OFFSET);

        // 不需要如此大的空间了
		setData(null);
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getRoot() {
        return root;
    }
    
    public void plusLevel() {
        level++;
        modified = true;
    }
    
    public void setRoot(int root) {
        this.root = root;
        modified = true;
    }

    protected void releaseMemory(DiskManagement disk) {
        if (modified) {
            int size = ROOT_ID_OFFSET + 4;
            byte[] dt = new byte[size];
            setData(dt);
            
            setInt(TREE_LEVEL_OFFSET, level);
            setInt(ROOT_ID_OFFSET, root);
            
            modified = false;
            super.writeBlock(disk);
        }
    }
    
}
