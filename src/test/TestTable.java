package test;
import recordmanagement.AttributeType;
import recordmanagement.RecordManagement;
import recordmanagement.Schema;
import recordmanagement.Table;
import recordmanagement.TempSchema;

/*
 * Created on 2005-3-19
 *
 */

/**
 * @author zh
 */
public class TestTable {

    public static void main(String[] args) {
        RecordManagement recordManagement = RecordManagement.getInstance();
     //  createDB(recordManagement);
        recordManagement.loadDB("zhzh");
        
      //  dropIndex(recordManagement);
     //   createIndex(recordManagement);
   //     recordManagement.removeTable("mydbd");
        addTable(recordManagement, "song");
    //    createTempTable(recordManagement);
        
        recordManagement.closeDB();
    }
    
    public static void addTable(RecordManagement recordManagement,
            String tableName) {
        byte attributeSize = 4;
        byte keyAttribute = 1;
        
        String[] attributeName = new String[]{"name", "isbn", "singer", "solo"};
        AttributeType[] attributeType = new AttributeType[]{
                AttributeType.getTypeString((byte)20),
                AttributeType.getTypeInteger(),
                AttributeType.getTypeString((byte)20),
                AttributeType.getTypeBoolean()};
        
        Table table = recordManagement.createTable(tableName, attributeSize,
                keyAttribute, attributeName, attributeType);
    }
    
    public static void createTempTable(RecordManagement recordManagement) {
        String tableName = "tempt";
        byte attributeSize = 2;
        byte keyAttribute = 0;
        
        String[] attributeName = new String[]{"name", "address"};
        AttributeType[] attributeType = new AttributeType[]{
                AttributeType.getTypeInteger(),
                AttributeType.getTypeString((byte)10)};
        
        Schema schema = new TempSchema(tableName, attributeSize, 
                attributeName, attributeType);
        
        Table table = recordManagement.createTempTable(schema);
        Table table2 = recordManagement.createTempTable(schema);
        
        recordManagement.removeTempTable(table);
        recordManagement.removeTempTable(table2);
    }
    
    public static void createDB(RecordManagement recordManagement) {
        recordManagement.createDB("zhzh");
        addTable(recordManagement, "staff");
     //   addTable(recordManagement, "staff2");
    }
    
    public static void createIndex(RecordManagement recordManagement) {
        Table table = recordManagement.getTable("staff");
        table.createIndex((byte)1);
    }
    
    public static void dropIndex(RecordManagement recordManagement) {
        Table table = recordManagement.getTable("staff");
        table.dropIndex((byte)1);
    }
}
