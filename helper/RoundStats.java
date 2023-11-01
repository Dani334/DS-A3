package helper;
import java.time.LocalTime;

public class RoundStats {

    public int nodeID;
    public int round;
    public int proposalNumber;
    public int numAccepted;
    public int numNACKed;
    public int receivedPromises;
    public int proposalValue;
    public LocalTime timestampStart;
    public LocalTime timestampEnd;

    public void saveStats(int nodeID, int round, int proposalNumber, int numAccepted, int numNACKed, int receivedPromises, int proposalValue, LocalTime timestampStart, LocalTime timestampEnd) {
        this.nodeID = nodeID;
        this.round = round;
        this.proposalNumber = proposalNumber;
        this.numAccepted = numAccepted;
        this.numNACKed = numNACKed;
        this.receivedPromises = receivedPromises;
        this.proposalValue = proposalValue;
        this.timestampStart = timestampStart;
        this.timestampEnd = timestampEnd;
    }

    public void printStats() {
        System.out.println("*****  M" + nodeID + " stats  *******");
        System.out.println("Round: " + round);
        System.out.println("Proposal Number: " + proposalNumber);
        System.out.println("Accepted: " + numAccepted);
        System.out.println("Nacked: " + numNACKed);
        System.out.println("Received Promises: " + receivedPromises);
        System.out.println("Proposal Value: " + proposalValue);
        System.out.println("Start of proposal: " + timestampStart);
        System.out.println("End of round: " + timestampEnd);
        System.out.println();
    }
    
}
