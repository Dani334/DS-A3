package testCases;

import java.util.Random;

import helper.RoundStats;
import main.Node;
import main.Proposer;


public class testCase_delayedReplies {

    public int startPort = 6000;

    static boolean STAT = true;
    
    public Proposer proposer1;
    public Proposer proposer2;
    public Node[] nodes;

    public boolean passed;

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
            nodes[i-1] = new Node(i, proposer1, proposer2, true);
            nodes[i-1].start();
        }

        proposer1.start();
        proposer2.start();

        proposer1.join();
        proposer2.join();

    }

    /**
     * Starts up a 2 proposer system with a 5 second delay for the second proposer
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

        proposer1.start();
        Thread.sleep(5000);
        proposer2.start();

        proposer1.join();
        proposer2.join();
    }

    /**
     * Starts up a one proposer system
     * 
     * @throws Exception
     */
    public void startUpOneProposer() throws Exception {
        Random rand = new Random();

        int p1 = rand.nextInt(3) + 1;
        
        proposer1 = new Proposer(p1, 1);

        nodes =  new Node[9];
        for(int i = 1; i <= 9; i++) {
            nodes[i-1] = new Node(i, proposer1, null, true);
            nodes[i-1].start();
        }

        proposer1.start();
        proposer1.join();

    }

    /**
     * Prints the stats
     * 
     */
    public void printStats() {
        System.out.println("Printing stats");
        System.out.println("Proposer 1 Stats");
        for(RoundStats stat : proposer1.stats) {
            stat.printStats();
        }
        
        if(this.proposer2 != null) {
            System.out.println("Proposer 2 Stats");
            for(RoundStats stat : proposer2.stats) {
                stat.printStats();
            }
        }
    }

    /**
     * Checks which proposer is elected
     * 
     * @return the id of the elected proposer
     */
    public int electedID() {
        int p1 = 0;
        int p2 = 0;
        for(Node node : nodes) {
            if(node.acceptedValue == proposer1.nodeID) p1++;
            if(proposer2 != null && node.acceptedValue == proposer2.nodeID) p2++; 
        }
        
        if(p1 >= 4) return proposer1.nodeID;
        else if(p2 >= 4) return proposer2.nodeID;
        else return -1;
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
            /*
             * TEST 1: both proposers propose simultaneously, a case is passed if EITHER proposer becomes elected
             * TEST 2: Only one proposer proposes, a case is passed if this proposer becomes elected
             */

            // set which tests to run
            boolean TEST1 = true;
            boolean TEST2 = true;

            // Set how many of each test you want to run (the more, the longer it will take)
            int test1Cases = 1;
            int test2Cases = 3;
            int test3Cases = 30;
            
            // Test 1
            if(TEST1) {
                System.out.println("*****  TEST 1 *****");
                System.out.println("Both proposers startup and propose at the same time");
                System.out.println("This test passes if EITHER of the two proposers becomes elected");

                System.out.println("Running " + test1Cases + " test cases, please wait");
                testCase_delayedReplies[] test1 = new testCase_delayedReplies[test1Cases];
                int passed1 = 0;
                for(int i = 0; i < test1Cases; i++) {
                    System.out.println("Test: " + i);
                    test1[i] = new testCase_delayedReplies();
                    test1[i].startUp();
                    int id = test1[i].electedID();
                    if(id == test1[i].proposer1.nodeID) {
                        passed1++;
                        test1[i].passed = true;
                    }
                    else if(id == test1[i].proposer2.nodeID) {
                        passed1++;
                        test1[i].passed = true;
                    }
                    else if(id == -1) {
                        test1[i].passed = false;
                    }
                    else {
                        test1[i].passed = false;
                    }
                    test1[i].close();
                }
                
                System.out.println("Test1 cases passed: " + passed1 + "/" + test1Cases);
                if(passed1 != test1Cases) {
                    int i = 0;
                    for(testCase_delayedReplies test : test1) {
                        if(!test.passed) {
                            System.out.println("Test case: " + i + " failed");
                            if(STAT) test.printStats();
                        }
                        i++;
                    }
                }

                System.out.println("Moving onto test 2");
                System.out.println();
            }

            if(TEST2) {
                // Test 2
                System.out.println("*****  TEST 2 *****");
                System.out.println("One proposer proposes");
                System.out.println("This test passes if the proposer is elected");

                System.out.println("Running " + test2Cases + " test cases, please wait");
                testCase_delayedReplies[] test2 = new testCase_delayedReplies[test2Cases];
                int passed2 = 0;
                for(int i = 0; i < test2Cases; i++) {
                    
                    test2[i] = new testCase_delayedReplies();
                    test2[i].startUpOneProposer();
                    int id = test2[i].electedID();
                    if(id == test2[i].proposer1.nodeID) {
                        passed2++;
                        test2[i].passed = true;
                    } else if(id == -1) {
                        System.out.println("m1");
                        test2[i].passed = false;
                    }
                    else {
                        System.out.println("m2");
                        test2[i].passed = false;
                    }
                    test2[i].close();
                }
                
                System.out.println("Test2 cases passed: " + passed2 + "/" + test2Cases);
                if(passed2 != test2Cases) {
                    int i = 0;
                    for(testCase_delayedReplies test : test2) {
                        if(!test.passed) {
                            System.out.println("Test case: " + i + " failed");
                            if(STAT) test.printStats();
                        }
                        i++;
                    }
                }
            }

            

        } catch (Exception e) {
            System.out.println("Exception occured: " + e.getMessage());
        }
    }
    
}
