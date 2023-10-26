import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class Node extends Thread {
    
    protected int nodeID;

    protected int highestPromisedProposal;
    
    protected int acceptedProposal = -1;
    protected int acceptedValue = -1;

    private ServerSocket serverSocket;
    
    protected ObjectInputStream inObj;
    protected ObjectOutputStream outObj;

    private Proposer p1;
    private Proposer p2;

    private static final Object lock = new Object();

    private Boolean PREPARE = false;
    private Boolean PROMISE = false;
    private Boolean ACCEPT = false;
    private Boolean ACCEPTED = false;
    private Boolean NACK = false;

    public Node(int nodeID, int proposalNumber) throws Exception {
        this.nodeID = nodeID;
    }

    public Node(int nodeID, Proposer p1, Proposer p2) throws IOException {
        
        this.p1 = p1;
        this.p2 = p2;
        this.nodeID = nodeID;
        highestPromisedProposal = -1;
        try {
            serverSocket = new ServerSocket(6000 + nodeID);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while(true) {
                Socket socket = serverSocket.accept();
                handleConnection(socket);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void handleConnection(Socket socket) throws Exception {
        
        inObj = new ObjectInputStream(socket.getInputStream());
        outObj = new ObjectOutputStream(socket.getOutputStream());

        if(this.nodeID == p1.nodeID) {
            p1.latchReply.countDown();
            p1.receivedReplies++;
        } else if(this.nodeID == p2.nodeID) {
            p2.latchReply.countDown();
            p2.receivedReplies++;
        }

        Message message = (Message) inObj.readObject();

        if(message.name.equals("Prepare")) {
            if(PREPARE) printMessage(message);
            handlePrepare((Prepare) message);
        }
        else if(message.name.equals("Promise")) {
            Promise promise = (Promise) message;
            if(PROMISE) printMessage(promise);
            handlePromise(promise);
            
        }
        else if(message.name.equals("Accept")) {
            Accept accept = (Accept) message;
            if(ACCEPT) printMessage(accept);
            handleAccept(accept);
        }
        else if(message.name.equals("Accepted")) {
            Accepted accepted = (Accepted) message;
            if(ACCEPTED) printMessage(accepted);
            handleAccepted(accepted);
        } else if(message.name.equals("Nack")) {
            Nack nack = (Nack) message;
            if(NACK) printMessage(nack);
            handleNack(nack);
        }
        
    
    }

    public void printMessage(Message message) {

        synchronized(lock) {
            System.out.println(message.name);
            System.out.println("From id: " + message.from);
            System.out.println("To id: " + message.to);
            System.out.println("Proposal Number: " + message.proposalNumber);
            
            if(message.name.equals("Promise")) {
                Promise promise = (Promise) message;
                System.out.println("Accepted Proposal Number: " + promise.acceptedProposal);
                System.out.println("Accepted Proposal Value: " + promise.acceptedValue);
            } else if(message.name.equals("Accept")) {
                Accept accept = (Accept) message;
                System.out.println("Proposal Value: " + accept.proposalValue);
            } else if(message.name.equals("Accepted")) {
                Accepted accepted = (Accepted) message;
                System.out.println("Proposal Value: " + accepted.proposalValue);
            }
            System.out.println();

        }
    }

    public void handlePrepare(Prepare message) throws Exception {

        int proposalNumber = message.proposalNumber;
        
        if(proposalNumber > this.highestPromisedProposal) {
            this.highestPromisedProposal = proposalNumber;
            sendPromise(this.highestPromisedProposal, message.from);
        } else {
            sendNack(message.from, proposalNumber);
        }
        
    }

    public void sendPromise(int proposalNumber, int targetID) throws Exception {

        Promise promise = new Promise(proposalNumber, this.acceptedProposal, this.acceptedValue, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(promise);
        outObj.flush(); 
        socket.close();

        this.highestPromisedProposal = proposalNumber;

    }

    public void handlePromise(Promise message) throws Exception {

        int toId = message.to;
        if(toId == p1.nodeID) {
            p1.latchPromise.countDown();
            p1.acceptorIds.add(message.from);
            p1.receivedPromises++;
        } else if(toId == p2.nodeID) {
            p2.latchPromise.countDown();
            p2.acceptorIds.add(message.from);
            p2.receivedPromises++;
        }

        int acceptorsProposal = message.acceptedProposal;

        if(acceptorsProposal == -1) {
            if(toId == p1.nodeID) {
                p1.numNotAccepted++;
            } else if(toId == p2.nodeID) {
                p2.numNotAccepted++;
            }
        } else {

            if(acceptorsProposal > p1.previousHighestProposalNumber && toId == p1.nodeID) {
                p1.previousHighestProposalNumber = acceptorsProposal;
                p1.previousHighestProposalValue = message.acceptedValue;
            } else if(acceptorsProposal > p2.previousHighestProposalNumber && toId == p2.nodeID) {
                p2.previousHighestProposalNumber = acceptorsProposal;
                p2.previousHighestProposalValue = message.acceptedValue;
            } 
        }

    }

    public void handleAccept(Accept accept) throws Exception {
        
        int recievedFrom = accept.from;
        int proposalNumber = accept.proposalNumber;
        int proposalValue = accept.proposalValue;

        this.acceptedProposal = proposalNumber;
        this.acceptedValue = proposalValue;
        if(proposalNumber >= this.highestPromisedProposal) sendAccepted(recievedFrom, proposalNumber, proposalValue);  
        else sendNack(recievedFrom, proposalNumber);

        
    }

    public void sendAccepted(int targetID, int proposalNumber, int proposalValue) throws Exception {

        Accepted accepted = new Accepted(proposalNumber, proposalValue, this.nodeID, targetID);
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accepted);
        outObj.flush();
        socket.close();
    }

    public void sendNack(int targetID, int proposalNumber) throws Exception {

        Nack nack = new Nack(proposalNumber, this.nodeID, targetID);
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(nack);
        outObj.flush();
        socket.close();

    }

    public void handleAccepted(Accepted accept) {
        int toId = accept.to;
        if(toId == p1.nodeID) {
            p1.numAccepted++;
            p1.latchAccept.countDown();
        } else if(toId == p2.nodeID) {
            p2.numAccepted++;
            p2.latchAccept.countDown();
        }
    }

    public void handleNack(Nack nack) {
        int toId = nack.to;
        if(toId == p1.nodeID) {
            p1.NACKed++;
        } else if(toId == p2.nodeID) {
            p2.NACKed++;
        }
    }

    public static void main(String args[]) {
        try {

            Random rand = new Random();

            int p1 = rand.nextInt(3) + 1;
            int p2 = p1;
            while(p1 == p2) p2 = rand.nextInt(3) + 1;
            
            Proposer proposer1 = new Proposer(p1, 1);
            Proposer proposer2 = new Proposer(p2, 1);

            Node[] nodes =  new Node[9];
            for(int i = 1; i <= 9; i++) {
                nodes[i-1] = new Node(i, proposer1, proposer2);
                nodes[i-1].start();
            }

            proposer1.start();
            proposer2.start();
            

        } catch (Exception e) {

            System.out.println("Exception Occured: " + e.getMessage());
            e.printStackTrace();
        }

    }
    

}
