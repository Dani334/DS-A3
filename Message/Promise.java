package Message;

public class Promise extends Message {
    public int acceptedProposal;
    public int acceptedValue;

    public Promise(int proposalNumber, int acceptedProposal, int acceptedValue, int from, int to) {
        super();
        this.name = "Promise";
        this.proposalNumber = proposalNumber;
        this.acceptedProposal = acceptedProposal;
        this.acceptedValue = acceptedValue;
        this.from = from;
        this.to = to;
    }

    /**
     * Prints the message details
     */
    @Override
    public void printMessage() {
        System.out.println(this.name);
        System.out.println("From id: " + this.from);
        System.out.println("To id: " + this.to);
        System.out.println("Proposal Number: " + this.proposalNumber);
        System.out.println("Accepted Proposal Number: " + this.acceptedProposal);
        System.out.println("Accepted Proposal Value: " + this.acceptedValue);
        System.out.println("Time sent: " + this.time);
        System.out.println();
    }
}
