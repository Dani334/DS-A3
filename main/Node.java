package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import Message.*;


public class Node extends Thread {

    public int startPort = 6000;
    
    public int nodeID;

    public int highestPromisedProposal;
    
    public int acceptedProposal = -1;
    public int acceptedValue = -1;

    public Queue<Message> messageQueue;

    private ServerSocket serverSocket;
    
    public ObjectInputStream inObj;
    public ObjectOutputStream outObj;

    private Proposer p1;
    private Proposer p2;

    public boolean delayed;

    protected static final Object lock = new Object();

    public Boolean PREPARE = false;
    public Boolean PROMISE = false;
    public Boolean ACCEPT = false;
    public Boolean ACCEPTED = false;
    public Boolean NACK = false;
    public Boolean RESPONSE = false;

    /**
     * This constructor is used by the proposer sub-class and initialises node id
     * 
     * @param nodeID Id of the node
     * @throws Exception
     */
    public Node(int nodeID, int startPort) throws Exception {
        
        this.nodeID = nodeID;
        messageQueue = new LinkedList<>();
        delayed = false;
    }

    /**
     * This constructor is used by the Node class to initialise nodeID, proposers and create sockets for connections
     * 
     * @param nodeID id of the node
     * @param p1 Proposer 1
     * @param p2 Proposer 2
     * @throws IOException
     */
    public Node(int nodeID, Proposer p1, Proposer p2, boolean delayed) throws IOException {
        
        this.delayed = delayed;
        this.p1 = p1;
        this.p2 = p2;
        this.nodeID = nodeID;
        highestPromisedProposal = -1;
        messageQueue = new LinkedList<>();
        try {
            serverSocket = new ServerSocket(startPort + nodeID);
        } catch(Exception e) {
            System.out.println("Constructor exception occured: " + e.getMessage());
        }
    }

    /**
     * This function starts a new Thread instance, and waits for connections through sockets
     * 
     */
    @Override
    public void run() {
        try {
            while(true) {
                if(serverSocket != null) {
                    Socket socket = serverSocket.accept();
                    handleConnection(socket);
                }
            }
        } catch (Exception e) {
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
        
        Message message = (Message) inObj.readObject();
        
        messageQueue.add(message);

        
        if(p1 != null && this.nodeID == p1.nodeID) {
            p1.latchReply.countDown();
            p1.receivedReplies++;
        } else if(p2 != null && this.nodeID == p2.nodeID) {
            p2.latchReply.countDown();
            p2.receivedReplies++;
        }
        
        
        reply();  
    
    }
    
    /**
     * This method is used to reply to all messages in the queue if it can given delays and message drops
     * 
     * @throws Exception
     */
    public void reply() throws Exception {

        double M2_delay_chance = new Random().nextDouble();
        double M3_delay_chance = new Random().nextDouble();
        boolean M3_notDropped = true;
        int M4_9Delay = new Random().nextInt(2500) + 1;

        while(!messageQueue.isEmpty()) {
            
            boolean canReply = false;
            if(delayed) {
                
                LocalTime time = LocalTime.now();

                if(nodeID == 1) canReply = true;

                else if(nodeID == 2) {
                    // member 2 has a 30% chance of an instant reply, 7.5 second delay otherwise
                    if(M2_delay_chance > 0.3) {
                        if(messageQueue.peek().time.until(time, ChronoUnit.MILLIS) > 7500) canReply = true;

                    } 
                    else canReply = true;
                } 
                else if(nodeID == 3) {
                    // member 3 has a 10% chance of dropping a message, else it has a 5 second response delay
                    if(M3_delay_chance > 0.9 && M3_notDropped) {
                        M3_notDropped = false;
                        messageQueue.poll();
                    } else if(messageQueue.peek().time.until(time, ChronoUnit.MILLIS) > 5000) canReply = true;
                } 
                else if(nodeID > 3) {
                    // members 4-9 have a random interval between 1 millisecond and 2500 milliseconds to reply
                    if(messageQueue.peek().time.until(time, ChronoUnit.MILLIS) > M4_9Delay) canReply = true;
                }
            } 
            else canReply = true;

            
            if(canReply) {
                Message message = messageQueue.poll();
                
                synchronized(lock) {print(message);}

                if(message.name.equals("Prepare")) {
                    handlePrepare((Prepare) message);
                }
                else if(message.name.equals("Promise")) {
                    Promise promise = (Promise) message;
                    handlePromise(promise);
                    
                }
                else if(message.name.equals("Accept")) {
                    Accept accept = (Accept) message;
                    handleAccept(accept);
                }
                else if(message.name.equals("Accepted")) {
                    Accepted accepted = (Accepted) message;
                    handleAccepted(accepted);
                } else if(message.name.equals("Nack")) {
                    Nack nack = (Nack) message;
                    handleNack(nack);
                } else if(message.name.equals("Response")) {
                    Response response = (Response) message;
                    handleResponse(response);
                }
            }

        }

    }

    /**
     * Checks what type the message is and calls the relevant message method
     * 
     * @param message
     */
    public void print(Message message) {

        if(message.name.equals("Prepare") && PREPARE) message.printMessage();
        if(message.name.equals("Promise") && PROMISE){
            Promise promise = (Promise) message;
            promise.printMessage();  
        } 
        if(message.name.equals("Accept") && ACCEPT) {
            Accept accept = (Accept) message;
            accept.printMessage();  
        }
        if(message.name.equals("Accepted") && ACCEPTED) {
            Accepted accepted = (Accepted) message;
            accepted.printMessage();  
        }
        if(message.name.equals("Nack") && NACK) message.printMessage();
        if(message.name.equals("Response") && RESPONSE) {
            Response response = (Response) message;
            response.printMessage();  
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
        Socket socket = new Socket("127.0.0.1", startPort + targetID);
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
        if(p1 != null && toId == p1.nodeID) {
            p1.latchPromise.countDown();
            p1.acceptorIds.add(message.from);
            p1.receivedPromises++;
        } else if(p2 != null && toId == p2.nodeID) {
            p2.latchPromise.countDown();
            p2.acceptorIds.add(message.from);
            p2.receivedPromises++;
        }
        int acceptorsProposal = message.acceptedProposal;
        
        if(acceptorsProposal == -1) {
            if(toId == p1.nodeID) {
                p1.numNotAccepted++;
            } else if(p2 != null && toId == p2.nodeID) {
                p2.numNotAccepted++;
            }
        } else {

            if(p1 != null && acceptorsProposal > p1.previousHighestProposalNumber && toId == p1.nodeID) {
                p1.previousHighestProposalNumber = acceptorsProposal;
                p1.previousHighestProposalValue = message.acceptedValue;
            } else if(p2 != null && acceptorsProposal > p2.previousHighestProposalNumber && toId == p2.nodeID) {
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
        
        Socket socket = new Socket("127.0.0.1", startPort + targetID);
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
        
        Socket socket = new Socket("127.0.0.1", startPort + targetID);
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
        if(p1 != null && toId == p1.nodeID) {
            p1.numAccepted++;
            p1.latchAccept.countDown();
        } else if(p2 != null && toId == p2.nodeID) {
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
        if(p1 != null && toId == p1.nodeID) {
            p1.latchNack.countDown();
            p1.NACKed++;
        } else if(p2 != null && toId == p2.nodeID) {
            p2.latchNack.countDown();
            p2.NACKed++;
        }
    }

    /**
     * Handles the case of receiving a response message and sets the accepted proposal and value
     * 
     * @param response The response message received
     */
    public void handleResponse(Response response) {
        this.acceptedProposal = response.proposalNumber;
        this.acceptedValue = response.proposalValue;
        int toId = response.to;
        if(p1 != null && toId == p1.nodeID) {
            p1.acceptedProposal = response.proposalNumber;
            p1.acceptedValue = response.proposalValue;
        } else if(p2 != null && toId == p2.nodeID) {
            p2.acceptedProposal = response.proposalNumber;
            p2.acceptedValue = response.proposalValue;
        }
    
    }

    /**
     * Closes the serverSocket of each node
     * 
     * @throws Exception
     */
    public void close() throws Exception {
        serverSocket.close();
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
            Proposer proposer2 = new Proposer(2, 1);

            Node[] nodes =  new Node[9];
            for(int i = 1; i <= 9; i++) {
                nodes[i-1] = new Node(i, proposer1, proposer2, true);
                nodes[i-1].start();
            }

            proposer1.start();
            // proposer2.start();

            proposer1.join();
            // proposer2.join();
            System.out.println("Consensus reached");
            

        } catch (Exception e) {

            System.out.println("Exception Occured: " + e.getMessage());
            e.printStackTrace();
        }

    }
    

}
