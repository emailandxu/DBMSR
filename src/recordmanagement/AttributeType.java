package recordmanagement;

import diskaccess.DiskManagement;


/**
 * @author zh 
 *
 */
public abstract class AttributeType {
 
	public static final byte BOOLEAN = 1;
	 
	public static final byte CHAR = 2;
	 
	public static final byte INTEGER = 3;
	 
	public static final byte LONG = 4;
	 
	public static final byte DOUBLE = 5;
	 
	public static final byte TIME = 6;
	 
	public static final byte STRING = 7;
	 
	protected byte length;
	 
	private byte type;
	
	private short indexCapacity;
	 
    private AttributeType(byte length, byte type) {
        this.length = length;
        this.type = type;
        /**
         * keyNum = 4, pointer多一个 = 4
         * pointer = 4, isNull = 1
         * 存放数组多一维
         */
        this.indexCapacity = (short)(( DiskManagement.BLOCK_SIZE - 8)
        								/ ( 5 + length) - 1);
	}
    
	private static final AttributeType typeBoolean
	    = new AttributeType( (byte) 1, BOOLEAN) {
        public byte getEncodedType() {
            return BOOLEAN;
        }
        
        public String toString() {
            return "BOOLEAN";
        }
    };

    private static final AttributeType typeChar
        = new AttributeType((byte) 2, CHAR) {
        public byte getEncodedType() {
            return CHAR;
        }
        
        public String toString() {
            return "CHAR";
        }
    };

    private static final AttributeType typeDouble
        = new AttributeType((byte) 8, DOUBLE) {
        public byte getEncodedType() {
            return DOUBLE;
        }
        
        public String toString() {
            return "DOUBLE";
        }
    };

    private static final AttributeType typeInteger
        = new AttributeType( (byte) 4, INTEGER) {
        public byte getEncodedType() {
            return INTEGER;
        }
        
        public String toString() {
            return "INTEGER";
        }
    };

    private static final AttributeType typeLong
        = new AttributeType((byte) 8, LONG) {
        public byte getEncodedType() {
            return LONG;
        }
        
        public String toString() {
            return "LONG";
        }
    };

    private static final AttributeType typeTime
        = new AttributeType((byte) 8, TIME) {
        public byte getEncodedType() {
            return TIME;
        }
        
        public String toString() {
            return "TIME";
        }
    };
    
    private static final class typeString extends AttributeType {
        
        /**
         * 转化为byte的长度
         * @param length unicode串的长度
         */
        public typeString(byte length) {
            super((byte)(length * 2), STRING);
        }
        
        public byte getEncodedType() {
            return (byte)(STRING + this.length);
        }
        
        public String toString() {
            return "STRING";
        }
        
    }

	public static AttributeType decodeType(byte t) {
	    switch (t) {
        case BOOLEAN:
            return typeBoolean;
        case CHAR:
            return typeChar;
        case INTEGER:
            return typeInteger;
        case LONG:
            return typeLong;
        case DOUBLE:
            return typeDouble;
        case TIME:
            return typeTime;
        default:
            return new typeString( (byte)((t - STRING)/2) );
        }
	}
	 
	public byte getByteLength() {
		return length;
	}
	 
	public byte getType() {
		return type;
	}
	 
    public short getIndexCapacity() {
        return indexCapacity;
    }
    
	public abstract byte getEncodedType();
	
	public static AttributeType getTypeBoolean() {
		return typeBoolean;
	}
	 
	public static AttributeType getTypeChar() {
		return typeChar;
	}
	 
	public static AttributeType getTypeDouble() {
		return typeDouble;
	}
	 
	public static AttributeType getTypeInteger() {
		return typeInteger;
	}
	 
	public static AttributeType getTypeLong() {
		return typeLong;
	}
	 
	public static AttributeType getTypeTime() {
		return typeTime;
	}
	 
	public static AttributeType getTypeString(byte l) {
		return new typeString(l);
	}
	 
}
 
