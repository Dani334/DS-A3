package Message;

public class Nack extends Message {

    public Nack(int proposalNumber, int from, int to) {
        super();
        this.name = "Nack";
        this.proposalNumber = proposalNumber;
        this.from = from;
        this.to = to;
    }
}
