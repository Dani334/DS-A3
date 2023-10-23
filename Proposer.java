import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

public class Proposer extends Node {

    private int lastProposalNumber;
    protected int receivedPromises = 0;

    protected int previousHighestProposalNumber;
    protected int previousHighestProposalValue;

    protected int numNotAccepted = 0;
    protected int numAccepted = 0;

    protected final CountDownLatch latch = new CountDownLatch(4);
    protected Vector<Integer> acceptorIds = new Vector<>();
    
    public Proposer(int nodeID, int proposalNumber) throws Exception {
        super(nodeID, proposalNumber);
        lastProposalNumber = 1;
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

        this.latch.await();
        Phase2(proposalNumber);
    }

    public void Phase2(int proposalNumber) throws Exception {
        
        while(true) {
            if(this.receivedPromises >= 4) break;
        }

        int proposalValue = -1;
        if(numNotAccepted >= 4) {
            proposalValue = this.nodeID;
        } else {
            proposalValue = previousHighestProposalValue;
        }
        
        
        for(int i = 0; i < acceptorIds.size(); i++) {
            this.sendAccept(acceptorIds.elementAt(i), proposalNumber, proposalValue);
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

    // public void handleAccepted() {
    //     this.numAccepted++;
    // }

}
