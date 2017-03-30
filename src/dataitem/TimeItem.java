/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author zh
 */
public class TimeItem implements DataItem {
    private Date data;
    
    /**
     * 格式 1902-6-6 14:4:5
     * 或无时间
     * @param value
     * @throws ParseException
     */
    public TimeItem(String value) throws ParseException{
        try {
            data = DateFormat.getDateTimeInstance().parse(value);
        } catch (ParseException e) {
            data = DateFormat.getDateInstance().parse(value);
        }
    }
    
    public TimeItem(Date dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        return data.compareTo(((TimeItem)o).data);
    }
    
    public boolean equals(Object obj) {
        return data.equals(obj);
    }
    
    public Date getData() {
        return data;
    }
    
    public String toString() {
        return DateFormat.getDateTimeInstance().format(data);
    }

    public DataItem add(DataItem d) {
        throw new UnsupportedOperationException();
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
