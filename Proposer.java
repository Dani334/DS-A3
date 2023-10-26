import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class Proposer extends Node {

    private int round = 1;
    private int lastProposalNumber;
    public int receivedPromises = 0;
    public int receivedReplies = 0;

    public int previousHighestProposalNumber;
    public int previousHighestProposalValue;

    public int numNotAccepted = 0;
    public int numAccepted = 0;
    public int NACKed = 0;
    
    public Vector<RoundStats> stats = new Vector<RoundStats>();
    public LocalTime timestampStart;
    public LocalTime timestampEnd;

    public CountDownLatch latchPromise; 
    public CountDownLatch latchAccept; 
    public CountDownLatch latchReply;
    private static final Object lock = new Object();

    public Vector<Integer> acceptorIds = new Vector<>();

    private Boolean STAT = false;
    
    public Proposer(int nodeID, int proposalNumber) throws Exception {
        super(nodeID, proposalNumber);
        lastProposalNumber = proposalNumber;
        latchPromise = new CountDownLatch(4);
        latchAccept = new CountDownLatch(4);
        latchReply = new CountDownLatch(8);
    }

    @Override
    public void run() {
        try {
            Phase1(this.lastProposalNumber);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void restartPhase1() throws Exception {
        round++;
        latchPromise = new CountDownLatch(4);
        latchAccept = new CountDownLatch(4);
        latchReply = new CountDownLatch(8);
        this.acceptorIds.clear();
        this.receivedPromises = 0;
        this.numAccepted = 0;
        this.NACKed = 0;
        this.numNotAccepted = 0;
        this.receivedReplies = 0;
        Phase1(lastProposalNumber);
    }

    public void Phase1(int proposalNumber) throws Exception {

        this.lastProposalNumber = proposalNumber;
        this.timestampStart = LocalTime.now();

        for(int i = 1; i <= 9; i++) {
            if(i != this.nodeID)
                this.sendPrepare(i, this.lastProposalNumber);
        }
        
        this.lastProposalNumber++;

        latchReply.await(10000, TimeUnit.MILLISECONDS);
        if(receivedReplies >= 8) {
            long countDown = latchPromise.getCount();
            for(int i = 0; i < countDown; i++) {
                latchPromise.countDown();
            }
        }
        latchPromise.await(2000, TimeUnit.MILLISECONDS);
        Phase2(proposalNumber);
    }

    public void Phase2(int proposalNumber) throws Exception {

        if(this.receivedPromises < 4) {
            this.timestampEnd = LocalTime.now();
            RoundStats stat = new RoundStats();
            stat.saveStats(nodeID, round, proposalNumber, numAccepted, NACKed, receivedPromises, -1, timestampStart, timestampEnd);
            stats.add(stat);
            if(STAT) synchronized(lock) {stat.printStats();}
            restartPhase1();
        } else {

            int proposalValue = -1;
            if(numNotAccepted >= 4) {
                proposalValue = this.nodeID;
            } else {
                proposalValue = this.previousHighestProposalValue;
            }
            
            
            for(int i = 0; i < acceptorIds.size(); i++) {
                this.sendAccept(acceptorIds.elementAt(i), proposalNumber, proposalValue);
            }

            // if(NACKed + receivedPromises >= 8) {
            //     for(int i = 0; i < latchAccept.getCount(); i++) {
            //         latchAccept.getCount();
            //     }
            // }

            this.latchAccept.await(2000, TimeUnit.MILLISECONDS);
            this.timestampEnd = LocalTime.now();
            
            RoundStats stat = new RoundStats();
            stat.saveStats(nodeID, round, proposalNumber, numAccepted, NACKed, receivedPromises, proposalValue, timestampStart, timestampEnd);
            stats.add(stat);
            if(STAT) synchronized(lock) {stat.printStats();}

            // if we received less than 4 Accepted's, it means we do not have a majority, so we reset variables and try again
            // It could mean that there is already a value accepted or it could mean some messages were dropped.
            if(this.numAccepted < 4) {
                restartPhase1();
            } else {
                // Do we have to send the accepted value?
                this.acceptedProposal = proposalNumber;
                this.acceptedValue = proposalValue;
                
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
