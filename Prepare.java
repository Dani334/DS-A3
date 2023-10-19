public class Prepare extends Message {
    
    Prepare(int proposalNumber, int from, int to) {
        this.name = "Prepare";
        this.proposalNumber = proposalNumber;
        this.from = from;
        this.to = to;
    }
}
