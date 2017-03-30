/**
 * @author zh
 */

package diskaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 *��Ӳ�̶�д��
 *��Ŵ�0��ʼ
 */
class FileHandler {
	private static final String TEMP_TABLE_FILE_PREFIX = "~$";
	private static final String TEMP_TABLE_FILE_EXTENSION = ".tta";
	
	/**
	 * Ϊÿ��handlerʵ�������id
	 */
	private static int handlerID = 0;
 
	private Integer fileID;
	private File fileName;
	/**
	 * �ļ��п������
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
	 * �������ݿ��ļ�ʱ��
	 * @param name
	 * @return
	 */
	static FileHandler getFileHandler(File name) {
	    // �������
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
	 * �����ļ�ʱ��
	 * @param baseName
	 * @return
	 */
	static FileHandler createFileHandler(String fileName) {
	    File name = new File(fileName);
	    
	    // ɾ�����ڵ��ļ���Ӧ�ü���ѯ��
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
	 * ������ʱ�ļ�ʱ�ã�Ϊ�˱����ļ����ظ���ֻ�������������
	 * @param dbName ��db������Ϊ����
	 * @return
	 */
	static FileHandler createTempFileHandler(String dir, String dbName) {
	    File name = new File(dir + TEMP_TABLE_FILE_PREFIX
                	+ dbName + handlerID + TEMP_TABLE_FILE_EXTENSION);
	    
	    // ɾ�����ڵ��ļ���Ӧ�ü���ѯ��
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
	 * д���
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
	 * ������
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
	 * ����ϵͳ�ڲ��ı�ʾID
	 * @return Returns the fileID.
	 */
	Integer getFileId() {
		return fileID;
	}
	
	/**
	 * �ļ�����
	 * @return
	 */
    int getBlockSize() {
        return blockSize;
    }
    
	/**
	 * ��һ���ID,���ú��¿��Ѿ�����
	 * @return Returns the nextNewBlock.
	 */
	int getNewBlockID() {
		try {
            /*
             * ���ļ���󣬴���һ���µĿ�
             */
            file.setLength( (blockSize + 1) * DiskManagement.BLOCK_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return blockSize++;
	}
}
 
