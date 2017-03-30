package diskaccess;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DiskManagement {
	/*
	 * ���̿��С�����̲����ĵ�λ,2k
	 */
	public static final int BLOCK_SIZE = 1024;// * 2;
	
	private static DiskManagement diskManagementInstance= null;
	
    /*
     * ��ǰ�򿪵��ļ�����
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
	 * �������ݿ��ļ�
	 * ����meta file��data file
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
	 * �ر��ļ�
	 * @param fileID
	 */
	public void closeFile(Integer fileID) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.remove(fileID);
		fHandler.close();
	}
	
	/**
	 * ɾ���ļ�
	 * @param fileID
	 */
	public void removeFile(Integer fileID) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.remove(fileID);
		fHandler.delete();
	}
	
	/**
	 * �����ļ�
	 * @return
	 */
	public Integer createFile(String fileName) {
		FileHandler fHandler = FileHandler.createFileHandler(fileName);
		fileHandlerSet.put(fHandler.getFileId(), fHandler);
		return fHandler.getFileId();
	}
	
	/**
	 * �����ļ�
	 * @return
	 */
	public Integer createTempFile(String dir, String dbName) {
		FileHandler fHandler = FileHandler.createTempFileHandler(dir, dbName);
		fileHandlerSet.put(fHandler.getFileId(), fHandler);
		return fHandler.getFileId();
	}
	
	/**
	 * �����µĿ�ʱ���ȵõ����
	 * @param fileID
	 * @return
	 */
	public int getNewBlockID(Integer fileID) {
		FileHandler file = (FileHandler)fileHandlerSet.get(fileID);
		return file.getNewBlockID();
	}
	
	/**
	 * �õ��ļ��������ܿ���,ֻ��table file�л����
	 * @param fileID
	 * @return
	 */
	public int getBlockSize(Integer fileID) {
	    FileHandler file = (FileHandler)fileHandlerSet.get(fileID);
		return file.getBlockSize();
	}
	
	/**
	 * �Ӵ��̶����
	 */
	public byte[] readFromDisk(Integer fileID, int blockNum) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.get(fileID);
		return fHandler.readBlock(blockNum);
		
	}
	 
	/**
	 *�����Ķ�������д�����
	 */
	public void writeToDisk(Integer fileID, int blockNum, byte[] buffer) {
		FileHandler fHandler = (FileHandler)fileHandlerSet.get(fileID);
		fHandler.writeBlock(blockNum, buffer);
	}
	 
}
 
