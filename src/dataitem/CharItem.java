/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.ParseException;

/**
 * @author zh
 */
public class CharItem implements DataItem {
    private char data;
    
    public CharItem(String value) throws ParseException {
        if (value != null && value.length() == 1) {
            data = value.charAt(0);
            return;
        }
        throw new ParseException(value + " is not char value", 0);
    }
    
    public CharItem(char dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        char d = ((CharItem)o).data;
        return d == data ?
                0 : data - d;
    }
    
    public boolean equals(Object obj) {
        return obj instanceof CharItem
                && ((CharItem)obj).data == data ;
    }
    
    public char getData() {
        return data;
    }
    
    public String toString() {
        return Character.toString(data);
    }

    public DataItem add(DataItem o) {
        char d = ((CharItem)o).data;
        return new CharItem((char)(data + d));
    }

    public DataItem substract(DataItem o) {
        char d = ((CharItem)o).data;
        return new CharItem((char)(data - d));
    }

    public DataItem multiply(DataItem o) {
        char d = ((CharItem)o).data;
        return new CharItem((char)(data * d));
    }

    public DataItem divide(DataItem o) {
        char d = ((CharItem)o).data;
        return new CharItem((char)(data / d));
    }
}
