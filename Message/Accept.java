package Message;
public class Accept extends Message {
    public int proposalValue;
    public Accept(int proposalNumber, int proposalValue, int from, int to) {
        this.name = "Accept";
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
        this.from = from;
        this.to = to;
    }
}
