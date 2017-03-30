package test;
import java.util.Random;

import recordmanagement.RecordManagement;
import recordmanagement.Table;
import recordmanagement.Tuple;
import buffer.DataBlock;
import dataitem.DataItem;
import dataitem.IntegerItem;
import dataitem.StringItem;

/*
 * Created on 2005-3-22
 *
 * TODO
 */

/**
 * @author vole
 *
 * TODO
 */
public class TestTuple {
    
    private static Random random = new Random();

    public static void main(String[] args) {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("zhzh");
        
        insert(recordManagement);
     //   delete(recordManagement);
        
        recordManagement.closeDB();
    }
    
    public static void insert(RecordManagement recordManagement) {
        Table table = recordManagement.getTable("staff");

        for (int i = 0; i < 100; i++) {
            DataItem[] items = new DataItem[] {
            		new IntegerItem(random.nextInt(500)),
            		new StringItem(randomString(20)),
                    
                    //new StringItem(randomString(10)),
                    //new BooleanItem(random.nextBoolean()),
                    };
            table.insertTuple(items);
        }
    }
    
    public static void delete(RecordManagement recordManagement) {
        Table table = recordManagement.getTable("staff");

        for (int i = 0; i < table.getTableBlockSize(); i++) {
            DataBlock dataBlock = table.getBlock(i);
            int tupleSize = dataBlock.getTupleSize();
            if (tupleSize > 0) {
                Tuple tuple = dataBlock.removeTuple((short)0);
            }
        }
    }
    
    public static String randomString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char)('a' + random.nextInt(26));
        }
        return new String(chars);
    }
}
