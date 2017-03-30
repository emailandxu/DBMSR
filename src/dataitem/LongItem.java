/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.ParseException;

/**
 * @author zh
 */
public class LongItem implements DataItem {
    private long data;
    
    public LongItem(String value) throws ParseException {
        try {
            data = Long.parseLong(value);
        } catch (Exception e) {
            throw new ParseException(value + " is not long value", 0);
        }
    }
    
    public LongItem(long dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        long d = ((LongItem)o).data;
        return d == data ?
                0 : data < d ? -1 : 1;
    }
    
    public boolean equals(Object obj) {
        return obj instanceof LongItem
                && ((LongItem)obj).data == data ;
    }
    
    public long getData() {
        return data;
    }
    
    public String toString() {
        return Long.toString(data);
    }

    public DataItem add(DataItem o) {
        long d = ((LongItem)o).data;
        return new LongItem(data + d);
    }

    public DataItem substract(DataItem o) {
        long d = ((LongItem)o).data;
        return new LongItem(data - d);
    }

    public DataItem multiply(DataItem o) {
        long d = ((LongItem)o).data;
        return new LongItem(data * d);
    }

    public DataItem divide(DataItem o) {
        long d = ((LongItem)o).data;
        return new LongItem(data / d);
    }

}
