public class Accepted extends Message {
    public int proposalValue;
    Accepted(int proposalNumber, int proposalValue, int from, int to) {
        this.name = "Accepted";
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
        this.from = from;
        this.to = to;
    }
    
}
