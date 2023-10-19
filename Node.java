import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;

public class Node extends Thread {
    
    private int nodeID;

    private int lastProposalNumber;

    private int highestAcceptedProposal;
    
    private int acceptedProposal = -1;
    private int acceptedValue = -1;

    private int numNotAccepted = 0;
    private int numAccepted = 0;

    private Vector<Integer> acceptorIds = new Vector<>();

    private ServerSocket serverSocket;
    
    ObjectInputStream inObj;
    ObjectOutputStream outObj;

    Boolean PREPARE = true;
    Boolean PROMISE = true;
    Boolean ACCEPT = true;
    Boolean ACCEPTED = true;

    public Node(int nodeID) throws IOException {
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

    public void sendOutPrepares(int proposalNumber) throws Exception {

        this.lastProposalNumber = proposalNumber;

        // Random rand = new Random();

        // boolean keepGenerating = true;
        // int numGen = 0;
        // int[] nums = new int[4];
        
        
        // while(keepGenerating) {
        //     int rec = rand.nextInt(9) + 1;

        //     boolean newNum = true;
        //     for(int num : nums) if(rec == num) newNum = false;

        //     if(rec != this.nodeID && newNum) {
        //         nums[numGen] = rec;
        //         numGen++;
        //     }

        //     if(numGen == 4) keepGenerating = false;
        // }

        // for(int acceptor : nums) 
        //     this.sendPrepare(acceptor, this.lastProposalNumber);

        for(int i = 1; i <= 9; i++) {
            if(i != this.nodeID)
                this.sendPrepare(i, this.lastProposalNumber);
        }
        
        this.lastProposalNumber++;
    }

    public void sendPrepare(int targetID, int proposalNumber) throws Exception {

        Message prepare = new Prepare(proposalNumber, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(prepare);
        outObj.flush();
        socket.close();

        
    }

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
        

        Promise promise = new Promise(proposalNumber, this.acceptedProposal, this.acceptedValue, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(promise);
        outObj.flush(); 
        socket.close();

        this.highestAcceptedProposal = proposalNumber;

    }

    public void handlePromise(Promise message) throws Exception {

        int acceptorsHighest = message.acceptedValue;

        if(acceptorsHighest == -1) {
            numNotAccepted++;

        } else {
            if(acceptorsHighest > this.highestAcceptedProposal) {
                
            }
        }

        acceptorIds.add(message.from);
        if(numNotAccepted >= 4) {

            int proposalNumber = message.proposalNumber;
            int proposalValue = this.nodeID;

            for(int acceptor : this.acceptorIds)
                this.sendAccept(acceptor, proposalNumber, proposalValue);

        }

    }

    public void sendAccept(int targetID, int proposalNumber, int proposalValue) throws Exception {

        Accept accept = new Accept(proposalNumber, proposalValue, this.nodeID, targetID);

        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accept);
        outObj.flush();
        socket.close();
    }

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
            Node m1 = new Node(1);
            Node m2 = new Node(2);
            Node m3 = new Node(3);
            Node m4 = new Node(4);
            Node m5 = new Node(5);
            Node m6 = new Node(6);
            Node m7 = new Node(7);
            Node m8 = new Node(8);
            Node m9 = new Node(9);

            Node[] nodes = {m1, m2, m3, m4, m5, m6, m7, m8, m9};

            for(Node node : nodes) node.start();

            Random rand = new Random();

            int p1 = rand.nextInt(3);
            int p2 = p1;
            while(p1 == p2) p2 = rand.nextInt(3);
            
            nodes[p1].sendOutPrepares(1);
            Thread.sleep(3000);
            nodes[p2].sendOutPrepares(2);
            

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

    }
    

}
