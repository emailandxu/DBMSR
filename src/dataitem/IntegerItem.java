/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.ParseException;

/**
 * @author zh
 */
public class IntegerItem implements DataItem{
    private int data;
    
    public IntegerItem(String value) throws ParseException {
        try {
            data = Integer.parseInt(value);
        } catch (Exception e) {
            throw new ParseException(value + " is not integer value", 0);
        }
    }
    
    public IntegerItem(int dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        int d = ((IntegerItem)o).data;
        return d == data ?
                0 : data - d;
    }
    
    public boolean equals(Object obj) {
        return obj instanceof IntegerItem
                && ((IntegerItem)obj).data == data ;
    }
    
    public int getData() {
        return data;
    }
    
    public String toString() {
        return Integer.toString(data);
    }

    public DataItem add(DataItem o) {
        int d = ((IntegerItem)o).data;
        return new IntegerItem(data + d);
    }

    public DataItem substract(DataItem o) {
        int d = ((IntegerItem)o).data;
        return new IntegerItem(data - d);
    }

    public DataItem multiply(DataItem o) {
        int d = ((IntegerItem)o).data;
        return new IntegerItem(data * d);
    }

    public DataItem divide(DataItem o) {
        int d = ((IntegerItem)o).data;
        return new IntegerItem(data / d);
    }
}
