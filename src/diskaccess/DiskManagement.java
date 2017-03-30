package diskaccess;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DiskManagement {
	/*
	 * 磁盘块大小，磁盘操作的单位,2k
	 */
	public static final int BLOCK_SIZE = 1024;// * 2;
	
	private static DiskManagement diskManagementInstance= null;
	
    /*
     * 当前打开的文件集合
     */
	private Map fileHandlerSet = new HashMap();
		
	private DiskManagement() {
	}
	
	public static DiskManagement getInstance() {
		if (diskManagementInstance == null) {
			diskManagementInstance = new DiskManagement();
		}
		return diskManagementInstance;
	}
	
	/**
	 * 读入数据库文件
	 * 包括meta file和data file
	 * @param schemaName
	 * @return
	 */
	public Integer loadFile(String fileName) {
		File f = new File(fileName);
		FileHandler fHandler = FileHandler.getFileHandler(f);
		fileHandlerSet.put(fHandler.getFileId(), fHandler);
		return fHandler.getFileId();
	}
	
	/**
	 * 关闭文件
	 * @param fileID
	 */
	public void closeFile(Integer fileID) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.remove(fileID);
		fHandler.close();
	}
	
	/**
	 * 删除文件
	 * @param fileID
	 */
	public void removeFile(Integer fileID) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.remove(fileID);
		fHandler.delete();
	}
	
	/**
	 * 创建文件
	 * @return
	 */
	public Integer createFile(String fileName) {
		FileHandler fHandler = FileHandler.createFileHandler(fileName);
		fileHandlerSet.put(fHandler.getFileId(), fHandler);
		return fHandler.getFileId();
	}
	
	/**
	 * 创建文件
	 * @return
	 */
	public Integer createTempFile(String dir, String dbName) {
		FileHandler fHandler = FileHandler.createTempFileHandler(dir, dbName);
		fileHandlerSet.put(fHandler.getFileId(), fHandler);
		return fHandler.getFileId();
	}
	
	/**
	 * 创建新的块时，先得到块号
	 * @param fileID
	 * @return
	 */
	public int getNewBlockID(Integer fileID) {
		FileHandler file = (FileHandler)fileHandlerSet.get(fileID);
		return file.getNewBlockID();
	}
	
	/**
	 * 得到文件包含的总块数,只在table file中会调用
	 * @param fileID
	 * @return
	 */
	public int getBlockSize(Integer fileID) {
	    FileHandler file = (FileHandler)fileHandlerSet.get(fileID);
		return file.getBlockSize();
	}
	
	/**
	 * 从磁盘读入块
	 */
	public byte[] readFromDisk(Integer fileID, int blockNum) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.get(fileID);
		return fHandler.readBlock(blockNum);
		
	}
	 
	/**
	 *如果块改动过，则写入磁盘
	 */
	public void writeToDisk(Integer fileID, int blockNum, byte[] buffer) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.get(fileID);
		fHandler.writeBlock(blockNum, buffer);
	}
	 
}
 
