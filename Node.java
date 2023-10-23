import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Node extends Thread {
    
    protected int nodeID;

    protected int lastProposalNumber;

    protected int highestAcceptedProposal;
    
    protected int acceptedProposal = -1;
    protected int acceptedValue = -1;

    protected int previousHighestProposalNumber;
    protected int previousHighestProposalValue;

    protected int receivedPromises = 0;

    protected int numNotAccepted = 0;
    protected int numAccepted = 0;

    protected Vector<Integer> acceptorIds = new Vector<>();

    protected ServerSocket serverSocket;
    
    ObjectInputStream inObj;
    ObjectOutputStream outObj;

    Proposer p1;
    Proposer p2;

    Boolean PREPARE = true;
    Boolean PROMISE = true;
    Boolean ACCEPT = true;
    Boolean ACCEPTED = true;

    public Node(int nodeID, int proposalNumber) throws Exception {
        this.nodeID = nodeID;
        this.lastProposalNumber = proposalNumber;
    }

    public Node(int nodeID, Proposer p1, Proposer p2) throws IOException {
        
        this.p1 = p1;
        this.p2 = p2;
        this.nodeID = nodeID;
        highestAcceptedProposal = -1;
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
            handleAccepted();
        }
        
    
    }

    public void printMessage(Message message) {

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

    // public void sendOutPrepares(int proposalNumber) throws Exception {

    //     this.lastProposalNumber = proposalNumber;

    //     for(int i = 1; i <= 9; i++) {
    //         if(i != this.nodeID)
    //             this.sendPrepare(i, this.lastProposalNumber);
    //     }
        
    //     this.lastProposalNumber++;
    // }

    // public void sendPrepare(int targetID, int proposalNumber) throws Exception {

    //     Message prepare = new Prepare(proposalNumber, this.nodeID, targetID);
    //     Socket socket = new Socket("127.0.0.1", 6000 + targetID);
    //     outObj = new ObjectOutputStream(socket.getOutputStream());
    //     outObj.writeObject(prepare);
    //     outObj.flush();
    //     socket.close();

    // }

    public void handlePrepare(Prepare message) throws Exception {
        
        int proposalNumber = message.proposalNumber;
        
        if(proposalNumber > this.highestAcceptedProposal) {
            this.highestAcceptedProposal = proposalNumber;
            sendPromise(this.highestAcceptedProposal, message.from);
        } else {
            // send NACK
        }
        
    }

    public void sendPromise(int proposalNumber, int targetID) throws Exception {
        
        // double rand = new Random().nextDouble();
        // if(rand > 0.8) {
        //     System.out.println("offline");
        //     return;
        // }

        Promise promise = new Promise(proposalNumber, this.acceptedProposal, this.acceptedValue, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(promise);
        outObj.flush(); 
        socket.close();

        this.highestAcceptedProposal = proposalNumber;

    }

    public void handlePromise(Promise message) throws Exception {

        int toId = message.to;
        if(toId == p1.nodeID) {
            p1.latch.countDown();
            p1.acceptorIds.add(message.from);
            p1.receivedPromises++;
        } else {
            p2.latch.countDown();
            p2.acceptorIds.add(message.from);
            p2.receivedPromises++;
        }

        int acceptorsProposal = message.acceptedProposal;

        if(acceptorsProposal == -1) {
            if(toId == p1.nodeID) {
                p1.numNotAccepted++;
            } else {
                p2.numNotAccepted++;
            }
        } else {
            if(acceptorsProposal > previousHighestProposalNumber) {
                if(toId == p1.nodeID) {
                    p1.previousHighestProposalNumber = acceptorsProposal;
                    p1.previousHighestProposalValue = message.acceptedValue;
                } else {
                    p2.previousHighestProposalNumber = acceptorsProposal;
                    p2.previousHighestProposalValue = message.acceptedValue;
                }
            }   
        }

    }

    // public void sendOutAccepts(int proposalNumber) throws Exception {
        
    //     while(true) {
    //         if(this.receivedPromises >= 4) break;
    //     }
    //     int proposalValue = -1;
    //     if(numNotAccepted >= 4) {
    //         proposalValue = this.nodeID;
    //     } else {
    //         proposalValue = previousHighestProposalValue;
    //     }
        

    //     for(int i = 0; i < acceptorIds.size(); i++) {
    //         this.sendAccept(acceptorIds.elementAt(i), proposalNumber, proposalValue);
    //     }

    // }

    // public void sendAccept(int targetID, int proposalNumber, int proposalValue) throws Exception {

    //     Accept accept = new Accept(proposalNumber, proposalValue, this.nodeID, targetID);

    //     Socket socket = new Socket("127.0.0.1", 6000 + targetID);
    //     outObj = new ObjectOutputStream(socket.getOutputStream());
    //     outObj.writeObject(accept);
    //     outObj.flush();
    //     socket.close();
    // }

    public void handleAccept(Accept accept) throws Exception {
        
        int recievedFrom = accept.from;
        int proposalNumber = accept.proposalNumber;
        int proposalValue = accept.proposalValue;

        if(proposalNumber >= this.highestAcceptedProposal) {
            
            this.acceptedProposal = proposalNumber;
            this.acceptedValue = proposalValue;
            sendAccepted(recievedFrom, proposalNumber, proposalValue);
        } else {
            // send nack
        }

    }

    public void sendAccepted(int targetID, int proposalNumber, int proposalValue) throws Exception {
        

        Accepted accepted = new Accepted(proposalNumber, proposalValue, this.nodeID, targetID);
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accepted);
        outObj.flush();
        socket.close();
    }

    public void handleAccepted() {
        this.numAccepted++;
    }

    public static void main(String args[]) {
        try {

            Random rand = new Random();

            int p1 = rand.nextInt(3) + 1;
            int p2 = p1;
            while(p1 == p2) p2 = rand.nextInt(3) + 1;
            
            Proposer proposer1 = new Proposer(p1, 1);
            Proposer proposer2 = new Proposer(p2, 2);

            Node[] nodes =  new Node[9];
            for(int i = 1; i <= 9; i++) {
                // if(i != p1 && i != p2) {
                    nodes[i-1] = new Node(i, proposer1, proposer2);
                    nodes[i-1].start();
                // }
            }

            proposer1.start();
            // proposer2.start();

            // proposer2.Phase1(3);

            

        } catch (Exception e) {

            System.out.println("Exception Occured: " + e.getMessage());
            e.printStackTrace();
        }

    }
    

}
