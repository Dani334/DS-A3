package Message;

public class Accept extends Message {
    public int proposalValue;
    public Accept(int proposalNumber, int proposalValue, int from, int to) {
        super();
        this.name = "Accept";
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
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
        System.out.println("Proposal Value: " + this.proposalValue);
        System.out.println("Time sent: " + this.time);
        System.out.println();
    }
}
