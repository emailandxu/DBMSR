/*
 * Created on 2005-1-21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package buffer;

/**
 * @author zh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class IDEntry {
	private Integer fileID;
	private int blockID;
	
	IDEntry (Integer fileID, int blockID) {
		this.fileID = fileID;
		this.blockID = blockID;
	}

	int getBlockID() {
		return blockID;
	}
	
	Integer getFileID() {
		return fileID;
	}
	
    public int hashCode() {
        int result = 17;
        result = 37*result + fileID.hashCode();
        result = 37*result + blockID;
        return result;
    }
	
    public boolean equals(Object obj) {
        if (obj instanceof IDEntry) {
            IDEntry id = (IDEntry) obj;
            return id.fileID.equals(fileID) && id.blockID == blockID;
        }
        return false;
    }
}
