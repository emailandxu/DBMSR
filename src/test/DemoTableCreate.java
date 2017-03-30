package test;
import java.text.ParseException;
import java.util.Random;

import recordmanagement.AttributeType;
import recordmanagement.RecordManagement;
import recordmanagement.Table;
import dataitem.DataItem;
import dataitem.IntegerItem;
import dataitem.StringItem;
import dataitem.TimeItem;

/*
 * Created on 2005-3-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author vole
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DemoTableCreate {
    
    private static int[] departmentID = new int[] {
            200001, 200002, 200003, 200004,
            200005, 200006, 200007, 200008, 200009, 
    };
    
    private static String[] departmentName = new String[] {
            "Computer Science", "Gardening", "Inter Financial",
            "Applied Mechanics", "Applied Maths", "Biographical",
            "Electronic Engineering", "Mathematics", "Physics"
    };
    
    private static Random random = new Random();

    public static void main(String[] args) {
        createDB();
        addDepartmentTable();
        addStudentTable();
        insertDepartment();
        insertStudent();
    }
    
    public static void createDB() {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.createDB("college");
        recordManagement.closeDB();
    }
    
    public static void addStudentTable() {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("college");
        
        byte attributeSize = 5;
        byte keyAttribute = 2;
        
        String[] attributeName =
            new String[]{"name", "age", "id", "department", "enrollment"};
        AttributeType[] attributeType = new AttributeType[]{
                AttributeType.getTypeString((byte)20),
                AttributeType.getTypeInteger(),
                AttributeType.getTypeInteger(),
                AttributeType.getTypeInteger(),
                AttributeType.getTypeTime()};
        
        Table table = recordManagement.createTable("student", attributeSize,
                keyAttribute, attributeName, attributeType);
        
        recordManagement.closeDB();
    }
    
    public static void addDepartmentTable() {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("college");
        
        byte attributeSize = 2;
        byte keyAttribute = 0;
        
        String[] attributeName =
            new String[]{"id", "name"};
        AttributeType[] attributeType = new AttributeType[]{
                AttributeType.getTypeInteger(),
                AttributeType.getTypeString((byte)30)};
        
        Table table = recordManagement.createTable("department", attributeSize,
                keyAttribute, attributeName, attributeType);
        
        recordManagement.closeDB();
    }

    public static void insertDepartment() {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("college");
        
        Table table = recordManagement.getTable("department");

        for (int i = 0; i < departmentID.length; i++) {
            DataItem[] items = new DataItem[] {
            		new IntegerItem(departmentID[i]),
            		new StringItem(departmentName[i])
                    };
            table.insertTuple(items);
        }
        recordManagement.closeDB();
    }
    
    public static void insertStudent() {
        RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("college");
        
        Table table = recordManagement.getTable("student");

        for (int i = 0; i < 2000000; i++) {
            DataItem[] items = null;
            try {
                items = new DataItem[] {
                		new StringItem(randomString(15)),
                		new IntegerItem(18 + random.nextInt(10)),
                		new IntegerItem(i + 10000),
                		new IntegerItem(departmentID[random.nextInt(departmentID.length)]),
                		new TimeItem( randomTime() ) };
                table.insertTuple(items);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        recordManagement.closeDB();
    }
    
    private static String randomString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char)('a' + random.nextInt(26));
        }
        return new String(chars);
    }
    
    private static String randomTime() {
        char[] chars = new char[] {
                '1', '9', '8' , '0', '-', '1', '-', '1'
        };
        chars[3] = (char)('0' + random.nextInt(10));
        chars[5] = (char)('0' + random.nextInt(10));
        chars[7] = (char)('0' + random.nextInt(10));
        return new String(chars);
    }
}
