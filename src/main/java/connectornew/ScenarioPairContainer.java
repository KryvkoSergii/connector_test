package connectornew;

/**
 * Created by srg on 04.07.16.
 */
public class ScenarioPairContainer<T> {
    private byte method;
    private T command;


    //Constructors
    public ScenarioPairContainer(byte method, T command) {
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

    public T getCommand() {
        return command;
    }

    public void setCommand(T command) {
        this.command = command;
    }
}
