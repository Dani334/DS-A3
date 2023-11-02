package Message;

public class Response extends Message {
    
    public int proposalValue;
    public Response(int proposalNumber, int proposalValue, int from, int to) {
        super();
        this.name = "Response";
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
        this.from = from;
        this.to = to;
    }

}
