/**
 * @author zh
 */

package diskaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 *对硬盘读写块
 *块号从0开始
 */
class FileHandler {
	private static final String TEMP_TABLE_FILE_PREFIX = "~$";
	private static final String TEMP_TABLE_FILE_EXTENSION = ".tta";
	
	/**
	 * 为每个handler实例分配的id
	 */
	private static int handlerID = 0;
 
	private Integer fileID;
	private File fileName;
	/**
	 * 文件中块的数量
	 */
	private int blockSize;
	
	private RandomAccessFile file;
	
	private FileHandler (File fileName, RandomAccessFile file) {
		this.fileName = fileName;
		this.file = file;
		this.fileID = new Integer(handlerID++);
		try {
            this.blockSize = (int)(file.length() / DiskManagement.BLOCK_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * 读入数据库文件时用
	 * @param name
	 * @return
	 */
	static FileHandler getFileHandler(File name) {
	    // 必须存在
	    if ( ! name.exists())
	        throw new IllegalArgumentException("file does not exist");
	    
		RandomAccessFile rFile = null;
		
		try {
			rFile = new RandomAccessFile (name, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		return new FileHandler(name, rFile);
	}
	
	/**
	 * 创建文件时用
	 * @param baseName
	 * @return
	 */
	static FileHandler createFileHandler(String fileName) {
	    File name = new File(fileName);
	    
	    // 删除存在的文件，应该加入询问
	    if (name.exists()) {
            name.delete();
	    //    throw new IllegalArgumentException("file already exists");
        }
	    
		RandomAccessFile rFile = null;
		
		try {
			rFile = new RandomAccessFile (name, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		return new FileHandler(name, rFile);
	}
	
	/**
	 * 创建临时文件时用，为了避免文件名重复，只能在这里用序号
	 * @param dbName 以db的名字为基础
	 * @return
	 */
	static FileHandler createTempFileHandler(String dir, String dbName) {
	    File name = new File(dir + TEMP_TABLE_FILE_PREFIX
                	+ dbName + handlerID + TEMP_TABLE_FILE_EXTENSION);
	    
	    // 删除存在的文件，应该加入询问
	    if (name.exists()) {
            name.delete();
	    //    throw new IllegalArgumentException("file already exists");
        }
	    
		RandomAccessFile rFile = null;
		
		try {
			rFile = new RandomAccessFile (name, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		return new FileHandler(name, rFile);
	}
	
	/**
	 * 写入块
	 * @param blockNum
	 * @param buffer
	 * @throws IOException
	 */
	void writeBlock(int blockNum, byte[] buffer){
		try {
			file.seek( blockNum * DiskManagement.BLOCK_SIZE );
			file.write(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 读出块
	 * @param blockNum
	 * @return
	 * @throws IOException
	 */
	byte[] readBlock(int blockNum){
		byte[] buffer = new byte[DiskManagement.BLOCK_SIZE];
		
		try {
			file.seek(blockNum * DiskManagement.BLOCK_SIZE);
			file.read( buffer );
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	void close() {
		try {
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void delete() {
		try {
			file.close();
			fileName.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	 
	/**
	 * 返回系统内部的标示ID
	 * @return Returns the fileID.
	 */
	Integer getFileId() {
		return fileID;
	}
	
	/**
	 * 文件块数
	 * @return
	 */
    int getBlockSize() {
        return blockSize;
    }
    
	/**
	 * 下一块的ID,调用后新块已经创建
	 * @return Returns the nextNewBlock.
	 */
	int getNewBlockID() {
		try {
            /*
             * 在文件最后，创建一个新的块
             */
            file.setLength( (blockSize + 1) * DiskManagement.BLOCK_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blockSize++;
	}
}
 
