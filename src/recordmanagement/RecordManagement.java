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
	 * 从dbms.config中读取的设置
	 */
	private Properties dbProperties;
	
	/**
	 * 数据库所在目录,临时文件所在目录
	 */
	private String dbDirectory, tempDirectory;
	
    private BufferAccess bufferAccess;
    
    private String dbName;
    
    private Integer dbFileID;
 
    private MetaBlock dbMetaBlock;
    
    /**
     * 本database中的表
     */
	private List tableList;
	 
	/**
	 * temp table似乎不用存为表，create后直接使用
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
	 * 读入已经存在的数据库
	 * @param dbName
	 */
	public void loadDB(String dbName) {
	    dbFileID = bufferAccess.loadFile(dbDirectory + dbName + DB_FILE_EXTENSION);
	    this.dbName = dbName;
	    dbMetaBlock = (MetaBlock)bufferAccess
	                    .getLockedBlock(dbFileID, 0, Block.META_BLOCK);
	    dbMetaBlock.initialize(false);
	    
	    // 读入数据库中的表
	    for (byte i = 0; i < dbMetaBlock.getTableSize(); i++) {
	        Table table = loadTable(dbMetaBlock.getSchemaBlockID(i));
	        tableList.add(table);
        }
	}
	
	/**
	 * 创建一个新database
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
     * 判读数据库是否已经打开
     * @return
     */
    public boolean isDBConnected() {
        return dbFileID != null;
    }
    
    /**
     * 关闭database
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
	 * 读入已经存在的表
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
	 * 在本database中创建一个新表
	 * @param schemaName
	 * @param attributeSize
	 * @param keyAttribute -1 无主键
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

	    // 新建表只能有一个索引，即主键
	    if (keyAttribute != -1) {
	        newTable.createIndex(keyAttribute);
        }
	    
	    // 在meta block中加入新表
	    tableList.add(newTable);
	    dbMetaBlock.addTable(schemaBlockID);
	    return newTable;
	}
	
	/**
	 * 删除一个table，不是temp table
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
	 * 删除一个temp table,不是table
	 * @param tableName
	 */
	public void removeTempTable(Table table) {
        bufferAccess.removeTableFile(table.getFileID());
    }
	 
	/**
     * 在本database中创建一个临时表，不用存入dbFile
     * 
     * @param schema
     * @return
     */
	public Table createTempTable(Schema schema) {
	    return new Table( 0,
	            bufferAccess.createTempFile(tempDirectory, dbName), schema);
	}
	
	/**
	 * 根据表名得到一个表
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
	 * 根据位置得到一个表
	 * @param order
	 * @return
	 */
	public Table getTable(int order) {
	    return (Table)tableList.get(order);
	}
	
	/**
	 * @return 表数
	 */
	public int getTableSize() {
        return tableList.size();
    }
	
	/**
	 * 关闭表
	 * @param table
	 */
	public void closeTable(Table table) {
	    bufferAccess.closeTableFile(table.getFileID());
    }
	
	/**
	 * 加入新表时检查是否有重复表名
	 * @param tableName
	 * @return true 重复
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
	 * 对外IndexBlock是用这个DB File的id，
	 * 其他meta, schema不对外开放
	 * @param blockID
	 * @return
	 */
	IndexBlock getIndexBlock(int blockID) {
	    return (IndexBlock)bufferAccess
	    		.getLockedBlock(dbFileID, blockID, Block.INDEX_BLOCK);
	}
	
	/**
	 * 对外IndexIndoBlock是用这个DB File的id
	 * @param blockID
	 * @return
	 */
	IndexInfoBlock getIndexInfoBlock(int blockID) {
	    return (IndexInfoBlock)bufferAccess
	    		.getLockedBlock(dbFileID, blockID, Block.INDEX_INFO_BLOCK);
	}
	
	/**
	 * 对外提供DataBlock
	 * @param fileID
	 * @param blockID
	 * @return
	 */
	DataBlock getDataBlock(Integer fileID, int blockID) {
	    return bufferAccess.getBlock(fileID, blockID);
	}
	
	/**
	 * 得到新的数据库主文件块id
	 * @return
	 */
	int getNewDBBlockID() {
	    return bufferAccess.getNewBlockID(dbFileID);
	}
	
	/**
	 * 得到新的表文件块id
	 * @param fileID
	 * @return
	 */
	int getNewDataBlockID(Integer fileID) {
	    return bufferAccess.getNewBlockID(fileID);
	}
	
	/**
	 * 得到指定表的块数
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
     * 读取数据库设置
     */
    private void loadConfig() {
	    InputStream in = null;
	    dbProperties = new Properties();
	    try {
            in = new FileInputStream(CONFIG_FILE);
            dbProperties.load(in);
        } catch (FileNotFoundException e) {
            // 第一次初始化
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
     * 保存数据库设置
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
 
