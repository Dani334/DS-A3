package Message;

import java.io.Serializable;
import java.time.LocalTime;

public class Message implements Serializable {
    public LocalTime time;
    public int to;
    public int from;
    public int proposalNumber;
    public String name;

    public Message() {
        time = LocalTime.now();
    }
}
