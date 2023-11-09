package testCases;

import java.util.Random;

import main.Node;
import main.Proposer;

public class testCase_runThroughProposer {

    public int startPort = 6000;

    static boolean STAT = false;
    
    public Proposer proposer1;
    public Proposer proposer2;
    public Node[] nodes;

    /**
     * Starts up a 2 proposer system with no delays
     * 
     * @throws Exception
     */
    public void startUp() throws Exception {

        Random rand = new Random();

        int p1 = rand.nextInt(3) + 1;
        int p2 = p1;
        while(p1 == p2) p2 = rand.nextInt(3) + 1;
        
        proposer1 = new Proposer(p1, 1);
        proposer2 = new Proposer(p2, 1);

        nodes =  new Node[9];
        for(int i = 1; i <= 9; i++) {
            nodes[i-1] = new Node(i, proposer1, proposer2, false);
            nodes[i-1].start();
        }

        nodes[p1-1].PREPARE = true;
        nodes[p1-1].PROMISE = true;
        nodes[p1-1].ACCEPT = true;
        nodes[p1-1].ACCEPTED = true;
        nodes[p1-1].NACK = true;
        nodes[p1-1].RESPONSE = true;

        nodes[p2-1].PREPARE = true;
        nodes[p2-1].PROMISE = true;
        nodes[p2-1].ACCEPT = true;
        nodes[p2-1].ACCEPTED = true;
        nodes[p2-1].NACK = true;
        nodes[p2-1].RESPONSE = true;

        proposer1.STAT = true;
        proposer2.STAT = true;

        proposer1.start();
        proposer2.start();

        proposer1.join();
        proposer2.join();

    }

    /**
     * Starts up a 2 proposer system with delays
     * 
     * @throws Exception
     */
    public void startUpDelay() throws Exception {

        Random rand = new Random();

        int p1 = rand.nextInt(3) + 1;
        int p2 = p1;
        while(p1 == p2) p2 = rand.nextInt(3) + 1;
        
        proposer1 = new Proposer(p1, 1);
        proposer2 = new Proposer(p2, 1);

        nodes =  new Node[9];
        for(int i = 1; i <= 9; i++) {
            nodes[i-1] = new Node(i, proposer1, proposer2, true);
            nodes[i-1].start();
        }

        nodes[p1-1].PREPARE = true;
        nodes[p1-1].PROMISE = true;
        nodes[p1-1].ACCEPT = true;
        nodes[p1-1].ACCEPTED = true;
        nodes[p1-1].NACK = true;
        nodes[p1-1].RESPONSE = true;

        nodes[p2-1].PREPARE = true;
        nodes[p2-1].PROMISE = true;
        nodes[p2-1].ACCEPT = true;
        nodes[p2-1].ACCEPTED = true;
        nodes[p2-1].NACK = true;
        nodes[p2-1].RESPONSE = true;

        proposer1.STAT = true;
        proposer2.STAT = true;

        proposer1.start();
        proposer2.start();

        proposer1.join();
        proposer2.join();

    }

    /** 
     * closes sockets and threads
     * 
     */
    public void close() throws Exception {
        for(Node node : nodes) {
            node.interrupt();
            node.close();
        }
    }
    
    public static void main(String args[]) {

        try {
            
            testCase_runThroughProposer test = new testCase_runThroughProposer();
            System.out.println("-----   NO DELAY   -----");
            test.startUp();
            test.close();
            System.out.println("-----   END NO DELAY   -----");
            System.out.println();
            System.out.println("-----   DELAY   -----");
            test.startUpDelay();
            System.out.println("-----   END DELAY   -----");
            

            
        } catch (Exception e) {
            System.out.println("Exception Occured: " + e.getMessage());
        }

    }

}
