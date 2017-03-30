package test;
import recordmanagement.RecordManagement;
import recordmanagement.Tuple;
import executionengine.Retrieve;

public class TestEngine {

	public static void main(String[] args) {
		RecordManagement recordManagement = RecordManagement.getInstance();
        recordManagement.loadDB("zhzh");
        
        //以下测试Retrieve        
        Retrieve retrieve = new Retrieve(recordManagement.getTable("staff"));
        retrieve.open();
        
        Tuple tuple = null;
        int i = 0;
        while(retrieve.hasNext()){
        	tuple = retrieve.getNext();
        	System.out.print(tuple.getItem( (byte)0 ));
        	System.out.println(" " + tuple.getItem((byte)1));
        	//System.out.print(" "+tuple.getItem( (byte)2 ));
        	//System.out.println(" "+tuple.getItem( (byte)3 ));
        	i++;
        }
        System.out.println(i);        
        //以上测试Retrieve
        
        
        /*
        //以下测试CrudeJoin
        Retrieve retrieve1 = new Retrieve(recordManagement.getTable("song"));
        Retrieve retrieve2 = new Retrieve(recordManagement.getTable("staff"));
        
        CrudeJoin crudeJoin = new CrudeJoin(retrieve1, retrieve2);
        crudeJoin.open();
        
        Tuple tuple = null;
        int i = 0;
        while(crudeJoin.hasNext()){
        	tuple = crudeJoin.getNext();
        	System.out.print(tuple.getItem( (byte)0 ));
        	System.out.print(" " + tuple.getItem((byte)1));
        	System.out.print(" "+tuple.getItem( (byte)2 ));
        	System.out.print(" "+tuple.getItem( (byte)3 ));
        	System.out.print(" "+tuple.getItem( (byte)4));
        	System.out.println(" "+tuple.getItem( (byte)5));
        	       	
        	i++;
        }
        System.out.println(i);
        //以上测试CrudeJoin
        */
        /*
        //以下测试Project
        Retrieve retrieve = new Retrieve(recordManagement.getTable("song"));
        Attribute[] attributeList = new Attribute[1];
        attributeList[0] = new Attribute(recordManagement.getTable("song"), (byte)-1);
        
        Project project = new Project(attributeList, retrieve);        
        project.open();
        
        Tuple tuple = null;
        int i = 0;
        while(project.hasNext()){
        	tuple = project.getNext();
        	System.out.print(tuple.getItem( (byte)0 ));
        	System.out.print(" " + tuple.getItem((byte)1));
        	System.out.print(" "+tuple.getItem( (byte)2 ));
        	System.out.println(" "+tuple.getItem( (byte)3 ));
        	
        	i++;
        }
        System.out.println(i);
        //以上测试Project
        */
        recordManagement.closeDB();
	}
}
