package Message;
public class Promise extends Message {
    public int acceptedProposal;
    public int acceptedValue;

    public Promise(int proposalNumber, int acceptedProposal, int acceptedValue, int from, int to) {
        this.name = "Promise";
        this.proposalNumber = proposalNumber;
        this.acceptedProposal = acceptedProposal;
        this.acceptedValue = acceptedValue;
        this.from = from;
        this.to = to;
    }
}
