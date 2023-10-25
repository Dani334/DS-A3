import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Proposer extends Node {

    private int lastProposalNumber;
    public int receivedPromises = 0;

    public int previousHighestProposalNumber;
    public int previousHighestProposalValue;

    public int numNotAccepted = 0;
    public int numAccepted = 0;
    public int NACKed = 0;
    public Boolean[] NACKedNodes = new Boolean[9]; 

    public CountDownLatch latchPromise; 
    public CountDownLatch latchAccept; 
    private static final Object lock = new Object();

    public Vector<Integer> acceptorIds = new Vector<>();
    
    public Proposer(int nodeID, int proposalNumber) throws Exception {
        super(nodeID, proposalNumber);
        lastProposalNumber = proposalNumber;
        latchPromise = new CountDownLatch(4);
        latchAccept = new CountDownLatch(4);
    }

    @Override
    public void run() {
        try {
            Phase1(this.lastProposalNumber);

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void Phase1(int proposalNumber) throws Exception {

        this.lastProposalNumber = proposalNumber;

        for(int i = 1; i <= 9; i++) {
            if(i != this.nodeID)
                this.sendPrepare(i, this.lastProposalNumber);
        }
        
        this.lastProposalNumber++;

        this.latchPromise.await(2000, TimeUnit.MILLISECONDS);
        Phase2(proposalNumber);
    }

    public void Phase2(int proposalNumber) throws Exception {

        int proposalValue = -1;
        if(numNotAccepted >= 4) {
            proposalValue = this.nodeID;
        } else {
            proposalValue = this.previousHighestProposalValue;
        }
        
        
        for(int i = 0; i < acceptorIds.size(); i++) {
            this.sendAccept(acceptorIds.elementAt(i), proposalNumber, proposalValue);
        }

        this.latchAccept.await(2000, TimeUnit.MILLISECONDS);

        synchronized(lock) {
            System.out.println("*****Member " + this.nodeID + " stats*******");
            int propNum = this.lastProposalNumber - 1;
            System.out.println("Proposal Number: " + propNum);
            System.out.println("Accepted: " + this.numAccepted);
            System.out.println("Nacked: " + this.NACKed);
            System.out.println("Received Promises: " + this.receivedPromises);
            System.out.println();
        }

        // if we received less than 4 Accepted's, it means we do not have a majority, so we reset variables and try again
        // It could mean that there is already a value accepted or it could mean some messages were dropped.
        if(this.numAccepted < 4) {
            latchPromise = new CountDownLatch(4);
            latchAccept = new CountDownLatch(4);
            this.acceptorIds.clear();
            this.receivedPromises = 0;
            this.numAccepted = 0;
            this.NACKed = 0;
            this.numNotAccepted = 0;
            Phase1(this.lastProposalNumber);
        } else {
            // Do we have to send the accepted value?
            this.acceptedProposal = proposalNumber;
            this.acceptedValue = proposalValue;
            synchronized(lock) {
                System.out.println("Accepted Proposal Number: " + this.acceptedProposal);
                System.out.println("Accepted Proposal Value: " + this.acceptedValue);
                System.out.println();

            }
        }


    }

    public void sendPrepare(int targetID, int proposalNumber) throws Exception {

        Message prepare = new Prepare(proposalNumber, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(prepare);
        outObj.flush();
        socket.close();

    }

    public void sendAccept(int targetID, int proposalNumber, int proposalValue) throws Exception {

        Accept accept = new Accept(proposalNumber, proposalValue, this.nodeID, targetID);

        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accept);
        outObj.flush();
        socket.close();
        
    }

}
