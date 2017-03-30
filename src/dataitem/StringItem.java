/*
 * Created on 2005-3-13
 *
 */
package dataitem;

/**
 * @author zh
 */
public class StringItem implements DataItem {
    private String data;
    
    public StringItem(String dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        String d = ((StringItem)o).data;
        return data.compareTo(d);
    }
    
    public boolean equals(Object obj) {
        return obj instanceof StringItem
                && ((StringItem)obj).data.equals(data);
    }
    
    public String getData() {
        return data;
    }
    
    public String toString() {
        return data;
    }

    public DataItem add(DataItem o) {
        String d = ((StringItem)o).data;
        return new StringItem(data + d);
    }

    public DataItem substract(DataItem d) {
        throw new UnsupportedOperationException();
    }

    public DataItem multiply(DataItem d) {
        throw new UnsupportedOperationException();
    }

    public DataItem divide(DataItem d) {
        throw new UnsupportedOperationException();
    }

}
