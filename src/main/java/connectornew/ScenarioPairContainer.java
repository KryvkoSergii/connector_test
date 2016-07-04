package connectornew;

/**
 * Created by srg on 04.07.16.
 */
public class ScenarioPairContainer {
    private byte method;
    private String command;


    //Constructors
    public ScenarioPairContainer(byte method, String command) {
        this.method = method;
        this.command = command;
    }


    //Methods
    public byte getMethod() {
        return method;
    }

    public void setMethod(byte method) {
        this.method = method;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
