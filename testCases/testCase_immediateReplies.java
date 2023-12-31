package testCases;

import java.util.Random;

import helper.RoundStats;
import main.Node;
import main.Proposer;


public class testCase_immediateReplies {
    public static int testDelay = 15;
    public int startPort = 6000;

    static boolean STAT = false;
    
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
            nodes[i-1] = new Node(i, proposer1, proposer2, false);
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
            nodes[i-1] = new Node(i, proposer1, proposer2, false);
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
            nodes[i-1] = new Node(i, proposer1, null, false);
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
        
        if(proposer2 != null) {
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
             * TEST 2: One proposer will propose, the second will propose 5 seconds after, a cases is passed if the FIRST proposer to propose becomes elected
             * TEST 3: Only one proposer proposes, a case is passed if this proposer becomes elected
             */

            // Set how many of each test you want to run (the more, the longer it will take)
            int test1Cases = 30;
            int test2Cases = 5;
            int test3Cases = 30;
            
            // Test 1
            System.out.println("*****  TEST 1 *****");
            System.out.println("Both proposers startup and propose at the same time");
            System.out.println("This test passes if EITHER of the two proposers becomes elected");
            System.out.println("Will also test to see whether the proposer that was not elected has an accepted value of the elected member");

            System.out.println("Running " + test1Cases + " test cases, please wait");
            testCase_immediateReplies[] test1 = new testCase_immediateReplies[test1Cases];
            int passed1 = 0;
            for(int i = 0; i < test1Cases; i++) {
                test1[i] = new testCase_immediateReplies();
                test1[i].startUp();
                int id = test1[i].electedID();
                if(id == test1[i].proposer1.nodeID && test1[i].proposer2.acceptedValue == id) {
                    passed1++;
                    test1[i].passed = true;
                }
                else if(id == test1[i].proposer2.nodeID && test1[i].proposer1.acceptedValue == id) {
                    passed1++;
                    test1[i].passed = true;
                }
                else if(id == -1) {
                    test1[i].passed = false;
                }
                else {
                    test1[i].passed = false;
                }
                
                if(STAT) test1[i].printStats();
                test1[i].close();
                Thread.sleep(testDelay);
            }
            
            System.out.println("Test1 cases passed: " + passed1 + "/" + test1Cases);
            if(passed1 != test1Cases) {
                int i = 0;
                for(testCase_immediateReplies test : test1) {
                    if(!test.passed) {
                        System.out.println("Test case: " + i + " failed");
                        if(STAT) test.printStats();
                    }
                    i++;
                }
            }

            System.out.println("Moving onto test 2");
            System.out.println();
            
            // Test 2
            System.out.println("*****  TEST 2 *****");
            System.out.println("First proposer proposes, then second one proposes after 5 seconds");
            System.out.println("This test passes if the first proposer is elected as 5 seconds is more than enough time to be elected");            
            System.out.println("Will also test to see whether the proposer that was not elected has an accepted value of the elected member");

            System.out.println("Running " + test2Cases + " test cases, please wait");
            testCase_immediateReplies[] test2 = new testCase_immediateReplies[test2Cases];
            int passed2 = 0;
            for(int i = 0; i < test2Cases; i++) {
                test2[i] = new testCase_immediateReplies();
                test2[i].startUpDelay();
                int id = test2[i].electedID();
                if(id == test2[i].proposer1.nodeID && test2[i].proposer2.acceptedValue == id) {
                    passed2++;
                    test2[i].passed = true;
                }
                else if(id == test2[i].proposer2.nodeID) {
                    test2[i].passed = false;
                }
                else if(id == -1) {
                    test2[i].passed = false;
                }
                else {
                    test2[i].passed = false;
                }
                test2[i].close();
                Thread.sleep(testDelay);
            }
            
            System.out.println("Test2 cases passed: " + passed2 + "/" + test2Cases);
            if(passed2 != test2Cases) {
                int i = 0;
                for(testCase_immediateReplies test : test2) {
                    if(!test.passed) {
                        System.out.println("Test case: " + i + " failed");
                        if(STAT) test.printStats();
                    }
                    i++;
                }
            }

            System.out.println("Moving onto test 3");
            System.out.println();

            // Test 3
            System.out.println("*****  TEST 3 *****");
            System.out.println("Only one proposer proposes");
            System.out.println("This test passes if the proposer is elected");            

            System.out.println("Running " + test3Cases + " test cases, please wait");
            testCase_immediateReplies[] test3 = new testCase_immediateReplies[test3Cases];
            int passed3 = 0;
            for(int i = 0; i < test3Cases; i++) {
                test3[i] = new testCase_immediateReplies();
                test3[i].startUpOneProposer();
                int id = test3[i].electedID();
                if(id == test3[i].proposer1.nodeID) {
                    passed3++;
                    test3[i].passed = true;
                }
                else if(id == -1) {
                    test3[i].passed = false;
                }
                else {
                    test3[i].passed = false;
                }
                test3[i].close();
                Thread.sleep(testDelay);
            }
    
            System.out.println("Test3 cases passed: " + passed3 + "/" + test3Cases);
            if(passed3 != test3Cases) {
                int i = 0;
                for(testCase_immediateReplies test : test3) {
                    if(!test.passed) {
                        System.out.println("Test case: " + i + " failed");
                        if(STAT) test.printStats();
                    }
                    i++;
                }
            }

        } catch (Exception e) {

            System.out.println("Exception Occured: " + e.getMessage());
            e.printStackTrace();
        }


    }
    
}
