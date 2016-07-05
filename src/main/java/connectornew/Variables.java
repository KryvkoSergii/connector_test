package connectornew;

/**
 * Created by srg on 05.07.16.
 */
public class Variables {
    private String name;
    private byte positionInArray;
    private byte type;
    private int beginPosition;
    private int endPosition;


    //Constructors
    public Variables() {
    }

    public Variables(String name, byte positionInArray, byte type, int beginPosition, int endPosition) {
        this.name = name;
        this.positionInArray = positionInArray;
        this.type = type;
        this.beginPosition = beginPosition;
        this.endPosition = endPosition;
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

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public byte getPositionInArray() {
        return positionInArray;
    }

    public void setPositionInArray(byte positionInArray) {
        this.positionInArray = positionInArray;
    }
}
