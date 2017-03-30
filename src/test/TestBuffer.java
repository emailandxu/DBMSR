package test;
import recordmanagement.RecordManagement;
import recordmanagement.Table;
import recordmanagement.Tuple;
import buffer.DataBlock;

/*
 * Created on 2005-3-26
 *
 */

/**
 * @author zh
 */
public class TestBuffer {

    public static void main(String[] args) {

        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("zhzh");
        

        Table table = recordManagement.getTable("school");
        
        DataBlock b1 = table.getBlock(0);
        DataBlock b2 = table.getBlock(1);
        DataBlock b11 = table.getBlock(0);
        
        Tuple p = b1.getTuple((short)0);
        
        Tuple p2 = b2.getTuple((short)0);
        
        recordManagement.closeDB();
    }
}
