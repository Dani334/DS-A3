package Message;
public class Prepare extends Message {
    
    public Prepare(int proposalNumber, int from, int to) {
        super();
        this.name = "Prepare";
        this.proposalNumber = proposalNumber;
        this.from = from;
        this.to = to;
    }
}
