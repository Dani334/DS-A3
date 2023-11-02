package Message;

public class Accepted extends Message {
    public int proposalValue;
    public Accepted(int proposalNumber, int proposalValue, int from, int to) {
        super();
        this.name = "Accepted";
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
        this.from = from;
        this.to = to;
    }
    
}
