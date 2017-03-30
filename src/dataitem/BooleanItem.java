/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.ParseException;

/**
 * @author zh
 */
public class BooleanItem implements DataItem{
    private boolean data;
    
    private static BooleanItem TRUE = new BooleanItem(true);
    private static BooleanItem FALSE = new BooleanItem(false);
	
    public BooleanItem(String value) throws ParseException {
        if ("true".equalsIgnoreCase(value)) {
            data = true;
            return;
        }
        else if ("false".equalsIgnoreCase(value)) {
            data =  false;
            return;
        }
        throw new ParseException(value + " is not boolean value", 0);
    }
    
    public BooleanItem(boolean dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        boolean d = ((BooleanItem)o).data;
        return d == data ?
                0 : data ? 1 : -1;
    }
    
    public boolean equals(Object obj) {
        return obj instanceof BooleanItem
                && ((BooleanItem)obj).data == data ;
    }

    public boolean getData() {
        return data;
    }
    
    public String toString() {
        return Boolean.toString(data);
    }
    
    public static BooleanItem getTrue(){
    	return BooleanItem.TRUE;
    }
    public static BooleanItem getFalse(){
    	return BooleanItem.FALSE;
    }

    public DataItem add(DataItem o) {
        boolean d = ((BooleanItem)o).data;
        return d ? TRUE : data ? TRUE : FALSE;
    }

    public DataItem substract(DataItem o) {
        throw new UnsupportedOperationException();
    }

    public DataItem multiply(DataItem o) {
        boolean d = ((BooleanItem)o).data;
        return d && data ? TRUE : FALSE;
    }

    public DataItem divide(DataItem o) {
        throw new UnsupportedOperationException();
    }
}
