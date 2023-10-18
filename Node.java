import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Node extends Thread {
    
    private int nodeID;

    private int highestAcceptedProposal;
    private int acceptedValue;
    private int numNotAccepted = 0;
    private int numAccepted = 0;

    private ServerSocket serverSocket;
    private List<Socket> sockets = new ArrayList<>();

    BufferedReader in;
    PrintWriter out;

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
                sockets.add(socket);
                handleConnection(socket);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void handleConnection(Socket socket) throws Exception {

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream());
        
        // while(true) {
            String line, req;
            req = "";
            line = in.readLine();
            while(line != null && !line.isEmpty()) {
                req += line + "\n";
                line = in.readLine();
            }
            System.out.println(req);
            
            if(req.contains("Prepare-")) handlePrepare(req);
            if(req.contains("Promise-")) handlePromise(req);
            if(req.contains("Accept-")) handleAccept(req);
            if(req.contains("Accepted-")) handleAccepted();

        // }
    }

    public void sendPrepare(int targetID, int proposalNumber) throws Exception {

        String prepare = "Prepare-" + nodeID + "\n" +
                         proposalNumber + "\n\n";

        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        out = new PrintWriter(socket.getOutputStream());
        out.write(prepare);
        out.flush();
        socket.close();
        
    }

    public void handlePrepare(String req) throws Exception {
        String messageLines[] = req.split("\n");
        int proposalNumber = Integer.valueOf(messageLines[1]);
        
        int receivedFrom = Integer.valueOf(messageLines[0].substring(messageLines[0].length()-1));
        
        if(proposalNumber > highestAcceptedProposal) {
            highestAcceptedProposal = proposalNumber;
            sendPromise(proposalNumber, receivedFrom);
        }
        
    }

    public void sendPromise(int proposalNumber, int targetID) throws Exception {
        
        String promise = "Promise-" + nodeID + "\n" +
                             proposalNumber + "\n" +
                             "-1\n" +
                             "-1\n\n";

        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        out = new PrintWriter(socket.getOutputStream());
        out.write(promise);
        out.flush(); 
        socket.close();

        this.highestAcceptedProposal = proposalNumber;

    }

    public void handlePromise(String req) throws Exception {

        String messageLines[] = req.split("\n");
        int acceptorsHighest = Integer.valueOf(messageLines[2]);
        if(acceptorsHighest == -1) {
            numNotAccepted++;

        } else {
            if(acceptorsHighest > this.highestAcceptedProposal) {
                
            }
        }

        if(numNotAccepted == 4) {
            int proposalNumber = Integer.valueOf(messageLines[1]);

            int proposalValue = ThreadLocalRandom.current().nextInt(1, 5 + 1);
            this.sendAccept(2, proposalNumber, proposalValue);
            this.sendAccept(3, proposalNumber, proposalValue);
            this.sendAccept(4, proposalNumber, proposalValue);
            this.sendAccept(5, proposalNumber, proposalValue);

        }

    }

    public void sendAccept(int targetID, int proposalNumber, int proposalValue) throws Exception {

        String accept = "Accept-" + this.nodeID + "\n" +
                        proposalNumber + "\n" +
                        proposalValue + "\n\n";
        

        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        out = new PrintWriter(socket.getOutputStream());
        out.write(accept);
        out.flush();
        socket.close();
    }

    public void handleAccept(String req) throws Exception {
        
        String messageLines[] = req.split("\n");
        int recievedFrom = Integer.valueOf(messageLines[0].substring(messageLines[0].length()-1));
        int proposalNumber = Integer.valueOf(messageLines[1]);
        int proposalValue = Integer.valueOf(messageLines[2]);
        
        

        if(proposalNumber >= this.highestAcceptedProposal) {
            this.acceptedValue = proposalValue;
            sendAccepted(recievedFrom, proposalNumber, proposalValue);
        }

    }

    public void sendAccepted(int targetID, int proposalNumber, int proposalValue) throws Exception {
        
        
        String accepted = "Accepted-" + this.nodeID + "\n" +
                           proposalNumber + "\n" +
                           proposalValue + "\n\n";
        
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        out = new PrintWriter(socket.getOutputStream());
        out.write(accepted);
        out.flush();
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

            m1.start();
            m2.start();
            m3.start();
            m4.start();
            m5.start();
            
            m1.sendPrepare(2, 1);
            m1.sendPrepare(3, 1);
            m1.sendPrepare(4, 1);
            m1.sendPrepare(5, 1);
            

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    

}
