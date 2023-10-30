import java.time.LocalTime;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

public class testCase_immediateReplies {


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

            proposer1.join();
            proposer2.join();

            // for(Node node : nodes) System.out.println(node.acceptedValue);
            System.out.println("CONSENSUS REACHED, MEMBER " + nodes[0].acceptedValue + " ELECTED, PRINTING STATS");

            Vector<LocalTime> p1startTimes = new Vector<LocalTime>();
            Vector<LocalTime> p1endTimes = new Vector<LocalTime>();

            Vector<LocalTime> p2startTimes = new Vector<LocalTime>();
            Vector<LocalTime> p2endTimes = new Vector<LocalTime>();

            System.out.println("Proposer 1 Stats");
            for(RoundStats stat : proposer1.stats) {
                stat.printStats();
                p1startTimes.add(stat.timestampStart);
                p1endTimes.add(stat.timestampEnd);
            }
            
            System.out.println("Proposer 2 Stats");
            for(RoundStats stat : proposer2.stats) {
                stat.printStats();
                p2startTimes.add(stat.timestampStart);
                p2endTimes.add(stat.timestampEnd);
            }

            Collections.sort(p1startTimes);
            Collections.sort(p1endTimes);
            Collections.sort(p2startTimes);
            Collections.sort(p2endTimes);

            System.out.println(p1endTimes.firstElement());
            System.out.println(p2endTimes.firstElement());
            if(p1endTimes.firstElement().compareTo(p2endTimes.firstElement()) == -1) {
                System.out.println("Member " + proposer1.nodeID + " proposed first");
                System.out.println("Is member " + proposer1.nodeID + " elected?");
                if(proposer1.nodeID == nodes[0].acceptedValue) System.out.println("Test passed, member " + proposer1.nodeID + " elected");
                else System.out.println("Test failed, member " + proposer1.nodeID + " was not elected");
            } else {
                System.out.println("Member " + proposer2.nodeID + " proposed first");
                System.out.println("Is member " + proposer2.nodeID + " elected?");
                if(proposer2.nodeID == nodes[0].acceptedValue) System.out.println("Test passed, member " + proposer2.nodeID + " elected");
                else System.out.println("Test failed, member " + proposer2.nodeID + " was not elected");
            }
            

        } catch (Exception e) {

            System.out.println("Exception Occured: " + e.getMessage());
            e.printStackTrace();
        }


    }
    
}
