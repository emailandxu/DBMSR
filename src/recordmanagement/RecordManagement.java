package recordmanagement;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import buffer.Block;
import buffer.BufferAccess;
import buffer.DataBlock;
import buffer.IndexBlock;
import buffer.IndexInfoBlock;
import buffer.MetaBlock;
import buffer.SchemaBlock;

public class RecordManagement {
	private static final String CONFIG_FILE = "dbms.config";
	private static final String DEFAULT_DIRECTORY = "";
	
	private static final String KEY_DB_DIRECTORY = "dbDir";
	private static final String KEY_TEMP_DIRECTORY = "tempDir";
	
	private static final String DB_FILE_EXTENSION = ".vl";
	private static final String TABLE_FILE_EXTENSION = ".table";
	
	/**
	 * ��dbms.config�ж�ȡ������
	 */
	private Properties dbProperties;
	
	/**
	 * ���ݿ�����Ŀ¼,��ʱ�ļ�����Ŀ¼
	 */
	private String dbDirectory, tempDirectory;
	
    private BufferAccess bufferAccess;
    
    private String dbName;
    
    private Integer dbFileID;
 
    private MetaBlock dbMetaBlock;
    
    /**
     * ��database�еı�
     */
	private List tableList;
	 
	/**
	 * temp table�ƺ����ô�Ϊ��create��ֱ��ʹ��
	 * private List tempTableList;
	 */
	
	private static RecordManagement recordManagementInstance;
	
	/**
	 * private constructor
	 */
	private RecordManagement() {
	    bufferAccess = BufferAccess.getInstance();
	    tableList = new ArrayList();
	    loadConfig();
    }
	
	/**
	 * �����Ѿ����ڵ����ݿ�
	 * @param dbName
	 */
	public void loadDB(String dbName) {
	    dbFileID = bufferAccess.loadFile(dbDirectory + dbName + DB_FILE_EXTENSION);
	    this.dbName = dbName;
	    dbMetaBlock = (MetaBlock)bufferAccess
	                    .getLockedBlock(dbFileID, 0, Block.META_BLOCK);
	    dbMetaBlock.initialize(false);
	    
	    // �������ݿ��еı�
	    for (byte i = 0; i < dbMetaBlock.getTableSize(); i++) {
	        Table table = loadTable(dbMetaBlock.getSchemaBlockID(i));
	        tableList.add(table);
        }
	}
	
	/**
	 * ����һ����database
	 * @param dbName
	 */
    public void createDB(String dbName) {
        this.dbName = dbName;
        dbFileID = bufferAccess.createFile(dbDirectory + dbName + DB_FILE_EXTENSION);
        int metaID = bufferAccess.getNewBlockID(dbFileID);
        dbMetaBlock = (MetaBlock)bufferAccess
                        .getLockedBlock(dbFileID, metaID, Block.META_BLOCK);
        dbMetaBlock.initialize(true);
    }
    
    /**
     * �ж����ݿ��Ƿ��Ѿ���
     * @return
     */
    public boolean isDBConnected() {
        return dbFileID != null;
    }
    
    /**
     * �ر�database
     */
    public void closeDB() {
        for (Iterator iter = tableList.iterator(); iter.hasNext();) {
            closeTable((Table) iter.next());
            iter.remove();
        }
        bufferAccess.closeDBFile(dbFileID);
        dbName = null;
        dbFileID = null;
        dbMetaBlock = null;
    }
	 
	/**
	 * �����Ѿ����ڵı�
	 * @param schemaBlock
	 * @return
	 */
	private Table loadTable(int schemaBlockID) {
        SchemaBlock schemaBlock = 
	        (SchemaBlock)bufferAccess.getLockedBlock(dbFileID,
	                schemaBlockID, Block.SCHEMA_BLOCK);
        schemaBlock.initialize();
        
	    byte attributeSize = schemaBlock.getAttributeSize();
	    
	    Index[] attributeIndex = new Index[attributeSize];
	    for (byte i = 0; i < attributeIndex.length; i++) {
            if (schemaBlock.getAttributeIndexBlockID(i) != 0) {
                IndexInfoBlock indexInfoBlock = 
                    (IndexInfoBlock)bufferAccess.getLockedBlock(
                            dbFileID, schemaBlock.getAttributeIndexBlockID(i),
                            Block.INDEX_INFO_BLOCK);
                indexInfoBlock.initialize(0);
                
                attributeIndex[i] = new Index( AttributeType.decodeType(
                                schemaBlock.getAttributeTypeCode(i)),
                        indexInfoBlock);
            }
        }
	    DefaultSchema schema = new DefaultSchema(schemaBlock, attributeIndex);
	    Integer tableFile =
	        bufferAccess.loadFile(dbDirectory + dbName + "." + schema.getSchemaName()
                    + TABLE_FILE_EXTENSION);
	    return new Table(schemaBlockID, tableFile, schema);
	}
	 
	/**
	 * �ڱ�database�д���һ���±�
	 * @param schemaName
	 * @param attributeSize
	 * @param keyAttribute -1 ������
	 * @param attributeName.length = attributeSize
	 * @param attributeType.length = attributeSize
	 * @return
	 */
	public Table createTable(String schemaName, byte attributeSize, byte keyAttribute,
            String[] attributeName, AttributeType[] attributeType) {
	    if (checkDuplicate(schemaName))
	        throw new IllegalArgumentException("duplicate table name");
	    
	    int schemaBlockID = bufferAccess.getNewBlockID(dbFileID);
	    SchemaBlock newSchemaBlock = (SchemaBlock)bufferAccess
	                    .getLockedBlock(dbFileID, schemaBlockID, Block.SCHEMA_BLOCK);
	    newSchemaBlock.initialize(schemaName, attributeSize, keyAttribute,
	            attributeName, attributeType);

	    Index[] attributeIndex = new Index[attributeSize];
	    DefaultSchema defaultSchema = new DefaultSchema(newSchemaBlock, attributeIndex);
	    Table newTable =  new Table(schemaBlockID, 
	            bufferAccess.createFile(dbDirectory + dbName + "." + schemaName
	                    + TABLE_FILE_EXTENSION), defaultSchema);

	    // �½���ֻ����һ��������������
	    if (keyAttribute != -1) {
	        newTable.createIndex(keyAttribute);
        }
	    
	    // ��meta block�м����±�
	    tableList.add(newTable);
	    dbMetaBlock.addTable(schemaBlockID);
	    return newTable;
	}
	
	/**
	 * ɾ��һ��table������temp table
	 * @param tableName
	 */
	public void removeTable(String tableName) {
        for (Iterator iter = tableList.iterator(); iter.hasNext();) {
            Table table = (Table) iter.next();
            if (tableName.equalsIgnoreCase(table.getTableName())) {
                iter.remove();
                dbMetaBlock.removeTable(table.getSchemaBlockID());
                bufferAccess.removeTableFile(table.getFileID());
                return;
            }
        }
    }
	
	/**
	 * ɾ��һ��temp table,����table
	 * @param tableName
	 */
	public void removeTempTable(Table table) {
        bufferAccess.removeTableFile(table.getFileID());
    }
	 
	/**
     * �ڱ�database�д���һ����ʱ�����ô���dbFile
     * 
     * @param schema
     * @return
     */
	public Table createTempTable(Schema schema) {
	    return new Table( 0,
	            bufferAccess.createTempFile(tempDirectory, dbName), schema);
	}
	
	/**
	 * ���ݱ����õ�һ����
	 * @param tableName
	 * @return
	 */
	public Table getTable(String tableName) {
	    for (Iterator iter = tableList.iterator(); iter.hasNext();) {
            Table table = (Table) iter.next();
            if (tableName.equalsIgnoreCase(table.getTableName())) {
                return table;
            }
        }
	    return null;
    }
	
	/**
	 * ����λ�õõ�һ����
	 * @param order
	 * @return
	 */
	public Table getTable(int order) {
	    return (Table)tableList.get(order);
	}
	
	/**
	 * @return ����
	 */
	public int getTableSize() {
        return tableList.size();
    }
	
	/**
	 * �رձ�
	 * @param table
	 */
	public void closeTable(Table table) {
	    bufferAccess.closeTableFile(table.getFileID());
    }
	
	/**
	 * �����±�ʱ����Ƿ����ظ�����
	 * @param tableName
	 * @return true �ظ�
	 */
	private boolean checkDuplicate(String tableName) {
        for (Iterator iter = tableList.iterator(); iter.hasNext();) {
            Table table = (Table) iter.next();
            if (tableName.equalsIgnoreCase(table.getTableName())) {
                return true;
            }
        }
        return false;
    }
	
	/**
	 * ����IndexBlock�������DB File��id��
	 * ����meta, schema�����⿪��
	 * @param blockID
	 * @return
	 */
	IndexBlock getIndexBlock(int blockID) {
	    return (IndexBlock)bufferAccess
	    		.getLockedBlock(dbFileID, blockID, Block.INDEX_BLOCK);
	}
	
	/**
	 * ����IndexIndoBlock�������DB File��id
	 * @param blockID
	 * @return
	 */
	IndexInfoBlock getIndexInfoBlock(int blockID) {
	    return (IndexInfoBlock)bufferAccess
	    		.getLockedBlock(dbFileID, blockID, Block.INDEX_INFO_BLOCK);
	}
	
	/**
	 * �����ṩDataBlock
	 * @param fileID
	 * @param blockID
	 * @return
	 */
	DataBlock getDataBlock(Integer fileID, int blockID) {
	    return bufferAccess.getBlock(fileID, blockID);
	}
	
	/**
	 * �õ��µ����ݿ����ļ���id
	 * @return
	 */
	int getNewDBBlockID() {
	    return bufferAccess.getNewBlockID(dbFileID);
	}
	
	/**
	 * �õ��µı��ļ���id
	 * @param fileID
	 * @return
	 */
	int getNewDataBlockID(Integer fileID) {
	    return bufferAccess.getNewBlockID(fileID);
	}
	
	/**
	 * �õ�ָ����Ŀ���
	 * @param fileID
	 * @return
	 */
	int getTableBlockSize(Integer fileID) {
		return bufferAccess.getBlockSize(fileID);
	}
	
    Integer getDbFileID() {
        return dbFileID;
    }
	
	public static RecordManagement getInstance() {
	    if (recordManagementInstance == null) {
            recordManagementInstance = new RecordManagement();
        }
        return recordManagementInstance;
	}
	
    public String toString() {
        return dbName;
    }
    
    /**
     * ��ȡ���ݿ�����
     */
    private void loadConfig() {
	    InputStream in = null;
	    dbProperties = new Properties();
	    try {
            in = new FileInputStream(CONFIG_FILE);
            dbProperties.load(in);
        } catch (FileNotFoundException e) {
            // ��һ�γ�ʼ��
            dbDirectory = DEFAULT_DIRECTORY;
            tempDirectory = DEFAULT_DIRECTORY;
            dbProperties.setProperty(KEY_DB_DIRECTORY, DEFAULT_DIRECTORY);
            dbProperties.setProperty(KEY_TEMP_DIRECTORY, DEFAULT_DIRECTORY);
            saveConfig();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        dbDirectory = dbProperties.getProperty(KEY_DB_DIRECTORY, DEFAULT_DIRECTORY);
        tempDirectory = dbProperties.getProperty(KEY_TEMP_DIRECTORY, DEFAULT_DIRECTORY);
    }
    
    /**
     * �������ݿ�����
     */
    public void saveConfig() {
        OutputStream out = null;
        try {
            out = new FileOutputStream(CONFIG_FILE);
            dbProperties.store(out, "Oracle 12v Config");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
        }
    }
    
    public String getDbDirectory() {
        return dbDirectory;
    }
    public String getTempDirectory() {
        return tempDirectory;
    }
    public void setDbDirectory(String dbDirectory) {
        this.dbDirectory = dbDirectory;
        dbProperties.setProperty(KEY_DB_DIRECTORY, dbDirectory);
    }
    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
        dbProperties.setProperty(KEY_TEMP_DIRECTORY, tempDirectory);
    }
}
 
