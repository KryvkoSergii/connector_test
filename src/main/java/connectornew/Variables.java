package connectornew;

/**
 * Created by srg on 05.07.16.
 */
public class Variables implements Comparable {
    private String name;
    private int positionInArray;
    private byte type;
    private int beginPosition;
    private int length;


    //Constructors
//    public Variables() {
//    }

    public Variables(String name, int positionInArray, byte type, int beginPosition, int length) {
        this.name = name;
        this.positionInArray = positionInArray;
        this.type = type;
        this.beginPosition = beginPosition;
        this.length = length;
    }

    //Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(int beginPosition) {
        this.beginPosition = beginPosition;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPositionInArray() {
        return positionInArray;
    }

    public void setPositionInArray(int positionInArray) {
        this.positionInArray = positionInArray;
    }

    @Override
    public int compareTo(Object o) {
        Variables v = (Variables) o;
        return Integer.valueOf(positionInArray).compareTo(v.getPositionInArray());
    }
}
