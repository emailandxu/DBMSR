/*
 * Created on 2005-3-13
 *
 */
package dataitem;

import java.text.ParseException;

/**
 * @author zh
 */
public class DoubleItem implements DataItem {
    private double data;
    
    public DoubleItem(String value) throws ParseException {
        try {
            data = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParseException(value + " is not double value", 0);
        }
    }
    
    public DoubleItem(double dd) {
        data = dd;
    }
    
    public int compareTo(Object o) {
        double d = ((DoubleItem)o).data;
        return Double.compare(data, d);
    }
    
    public boolean equals(Object obj) {
        return obj instanceof DoubleItem
                && Double.doubleToLongBits(((DoubleItem)obj).data)
                	== Double.doubleToLongBits(data);
    }
    
    public double getData() {
        return data;
    }
    
    public String toString() {
        return Double.toString(data);
    }

    public DataItem add(DataItem o) {
        double d = ((DoubleItem)o).data;
        return new DoubleItem(d + data);
    }

    public DataItem substract(DataItem o) {
        double d = ((DoubleItem)o).data;
        return new DoubleItem(d - data);
    }

    public DataItem multiply(DataItem o) {
        double d = ((DoubleItem)o).data;
        return new DoubleItem(d * data);
    }

    public DataItem divide(DataItem o) {
        double d = ((DoubleItem)o).data;
        return new DoubleItem(d / data);
    }

}
