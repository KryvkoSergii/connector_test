package connectornew;

/**
 * Created by srg on 04.07.16.
 */
public class ScenarioPairContainer<T> {
    private byte method;
    private T command;
    private Variables[] variables;


    //Constructors
    public ScenarioPairContainer(byte method, T command) {
        this.method = method;
        this.command = command;
    }


    //Getters and Setters
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

    public Variables[] getVariables() {
        return variables;
    }

    public void setVariables(Variables[] variables) {
        this.variables = variables;
    }


    //Methods
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScenarioPairContainer{");
        sb.append("method=").append(method);
        sb.append(", command=").append(command);
        sb.append('}');
        return sb.toString();
    }
}
