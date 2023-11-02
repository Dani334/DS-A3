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

    /**
     * Prints the message details
     */
    public void printMessage() {
        System.out.println(this.name);
        System.out.println("From id: " + this.from);
        System.out.println("To id: " + this.to);
        System.out.println("Proposal Number: " + this.proposalNumber);
        System.out.println("Time sent: " + this.time);
        System.out.println();
    }
}
