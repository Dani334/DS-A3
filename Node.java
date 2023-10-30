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

    /**
     * This constructor is used by the proposer sub-class and initialises node id
     * 
     * @param nodeID Id of the node
     * @throws Exception
     */
    public Node(int nodeID) throws Exception {
        this.nodeID = nodeID;
    }

    /**
     * This constructor is used by the Node class to initialise nodeID, proposers and create sockets for connections
     * 
     * @param nodeID id of the node
     * @param p1 Proposer 1
     * @param p2 Proposer 2
     * @throws IOException
     */
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

    /**
     * This functions starts a new Thread instance, and waits for connections through sockets
     * 
     */
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

    /**
     * Initialises object streams for connection, hands the message to the relevant function to handle it
     * 
     * @param socket the socket forming the connection
     * @throws Exception
     */
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

    /**
     * Prints the message if the right variables are set
     * 
     * @param message the message to be printed
     */
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

    /**
     * Handles a prepare message
     * Will send a promise if the proposal number is higher than any promised proposal numbers
     * Sends a Nack otherwise
     * 
     * @param message the prepare message
     * @throws Exception
     */
    public void handlePrepare(Prepare message) throws Exception {

        int proposalNumber = message.proposalNumber;
        
        if(proposalNumber > this.highestPromisedProposal) {
            this.highestPromisedProposal = proposalNumber;
            sendPromise(this.highestPromisedProposal, message.from);
        } else {
            sendNack(message.from, proposalNumber);
        }
        
    }

    /**
     * Sends a promise back to the proposer
     * 
     * @param proposalNumber the proposal number of the prepare message
     * @param targetID the id of the proposer to send the promise to
     * @throws Exception
     */
    public void sendPromise(int proposalNumber, int targetID) throws Exception {

        Promise promise = new Promise(proposalNumber, this.acceptedProposal, this.acceptedValue, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(promise);
        outObj.flush(); 
        socket.close();

        this.highestPromisedProposal = proposalNumber;

    }

    /**
     * Handles a promise message from an acceptor
     * Sets the relevant variables of the proposer
     * If the proposer recieves a promise with a proposal number not equal to -1, it knows it can set its proposal value
     * 
     * @param message the promise messgae
     * @throws Exception
     */
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


    /**
     * Handles the accept message from a proposer
     * If the acceptor has not already promised to a higher proposal number, it will send an Accepted message
     * If not, it sends a Nack
     * 
     * @param accept the accept message
     * @throws Exception
     */
    public void handleAccept(Accept accept) throws Exception {
        
        int recievedFrom = accept.from;
        int proposalNumber = accept.proposalNumber;
        int proposalValue = accept.proposalValue;

        this.acceptedProposal = proposalNumber;
        this.acceptedValue = proposalValue;
        if(proposalNumber >= this.highestPromisedProposal) sendAccepted(recievedFrom, proposalNumber, proposalValue);  
        else sendNack(recievedFrom, proposalNumber);

        
    }

    /**
     * Sends an Accepted message to the proposer
     * 
     * @param targetID the id of the proposer
     * @param proposalNumber proposal number that has been accepted
     * @param proposalValue proposal value that has been accepted
     * @throws Exception
     */
    public void sendAccepted(int targetID, int proposalNumber, int proposalValue) throws Exception {

        Accepted accepted = new Accepted(proposalNumber, proposalValue, this.nodeID, targetID);
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accepted);
        outObj.flush();
        socket.close();
    }

    /**
     * Sends a Nack to the proposer
     * 
     * @param targetID the id of the proposer
     * @param proposalNumber the proposer number that is being Nacked
     * @throws Exception
     */
    public void sendNack(int targetID, int proposalNumber) throws Exception {

        Nack nack = new Nack(proposalNumber, this.nodeID, targetID);
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(nack);
        outObj.flush();
        socket.close();

    }

    /**
     * Handles an Accepted message received from an acceptor
     * Increases the numAccepted member variable of the proposer and counts down the latch
     * 
     * @param accept the Accepted message
     */
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

    /**
     * Handles a Nack message received from an acceptor
     * Increases the NACKed member variable of the relevant proposer
     * 
     * @param nack The nack message
     */
    public void handleNack(Nack nack) {
        int toId = nack.to;
        if(toId == p1.nodeID) {
            p1.NACKed++;
        } else if(toId == p2.nodeID) {
            p2.NACKed++;
        }
    }

    /**
     * Starts up the Proposer and Acceptor nodes
     * 
     * @param args Unused
     */
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
