package Message;
import java.io.Serializable;

public class Message implements Serializable {
    public int to;
    public int from;
    public int proposalNumber;
    public String name;
}
