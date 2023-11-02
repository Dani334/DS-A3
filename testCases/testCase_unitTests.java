package testCases;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Message.*;
import main.*;

public class testCase_unitTests {
    
    int startPort = 6000;

    ServerSocket serverSocket;
    Socket socketOut;
    Socket socketIn;
    Node testNode;
    Proposer proposerTest;

    ObjectOutputStream outObj;
    ObjectInputStream inObj;

    /**
     * Initalises variables for socket reading/writing
     * 
     * @throws Exception
     */
    public void startNode() throws Exception {

        proposerTest = new Proposer(0, 1);
        serverSocket = new ServerSocket(startPort + 1);
        testNode = new Node(0, proposerTest, null, false);
        testNode.start();
        socketOut = new Socket("127.0.0.1", startPort);
        outObj = new ObjectOutputStream(socketOut.getOutputStream());
        

    }

    /**
     * Closes socket reading/writing
     * 
     * @throws Exception
     */
    public void closeNode() throws Exception {
        serverSocket.close();
        testNode.close();
        socketOut.close();
        outObj.close();
    }

    /**
     * Unit Test 1
     * Tests the functionality of the handlePrepare method in Node.java
     * 3 Tests conducted
     * 1. Proposal number = 0, highest promised proposal = 0
     * 2. Propsoal number = -1, highest promised proposal = 0
     * 3, Proposal number = 1000, highest promised proposal = 50
     * 
     * @return true if all cases pass, false if not
     * @throws Exception
     */
    public boolean handlePrepareTest() throws Exception {

        
        int passed = 0;
        
        // Test 1
        startNode();
        testNode.highestPromisedProposal = 0;
        Prepare prepare = new Prepare(0, 1, 0);
        outObj.writeObject(prepare);
        outObj.flush();
        
        Socket socketIn = serverSocket.accept();
        
        ObjectInputStream inObj = new ObjectInputStream(socketIn.getInputStream());
        Message message = (Message) inObj.readObject();
        if(message.name.equals("Nack")) {
            System.out.println("Test 1.1 Successful");
            passed++;
        } else {
            System.out.println("Test 1.1 Unsuccessful");
        }

        closeNode();
        
        // Test 2
        startNode();
        testNode.highestPromisedProposal = 0;
        prepare = new Prepare(-1, 1, 0);
        outObj.writeObject(prepare);
        outObj.flush();

        socketIn = serverSocket.accept();
        inObj = new ObjectInputStream(socketIn.getInputStream());
        message = (Message) inObj.readObject();
        if(message.name.equals("Nack")) {
            System.out.println("Test 1.2 Successful");
            passed++;
        } else {
            System.out.println("Test 1.2 Unsuccessful");
        }
        closeNode();

        // Test 3
        startNode();
        testNode.highestPromisedProposal = 50;
        prepare = new Prepare(1000, 1, 0);
        outObj.writeObject(prepare);
        outObj.flush();

        socketIn = serverSocket.accept();
        inObj = new ObjectInputStream(socketIn.getInputStream());
        message = (Message) inObj.readObject();
        if(message.name.equals("Promise")) {
            System.out.println("Test 1.3 Successful");
            passed++;
        } else {
            System.out.println("Test 1.3 Unsuccessful");
        }
        closeNode();

        System.out.println("Unit 1 Tests Passed: " + passed + "/3");
        if(passed == 3) return true;
        return false;

    }

    /**
     * Unit test 2
     * Tests the functionality of the handlePromise method in Node.java
     * 2 tests conducted
     * 1. Promise contains no previous accepted values (-1 values) - numNotAccepted increments
     * 2. Promise contains previous values - previousHighestAcceptedValue set to message proposal value
     * 
     * @return true if all cases pass, false if not
     */
    public boolean handlePromiseTest() throws Exception {
        int passed = 0;

        // Test 1
        startNode();
        Promise promise = new Promise(proposerTest.lastProposalNumber, -1, -1, 1, 0);
        outObj.writeObject(promise);
        outObj.flush();
        Thread.sleep(200);
        if(proposerTest.numNotAccepted > 0) {
            System.out.println("Unit 2.1 Successful");
            passed++;
        } else System.out.println("Unit 2.1 Unsuccessful");
        closeNode();

        // Test 2
        startNode();
        promise = new Promise(proposerTest.lastProposalNumber, 2, 1, 1, 0);
        outObj.writeObject(promise);
        outObj.flush();
        Thread.sleep(200);
        if(proposerTest.previousHighestProposalValue == 1 && proposerTest.previousHighestProposalNumber == 2) {
            System.out.println("Unit 2.2 Successful");
            passed++;
        } else System.out.println("Unit 2.2 Unsuccessful");
        closeNode();

        System.out.println("Unit 2 Tests Passed: " + passed + "/2");
        if(passed == 2) return true;
        return false;
    }

    /**
     * Unit Test 3
     * Tests the functionality of the handleAccepted method in Node.java
     * 1 Test conducted
     * 1. Sends an accepted - numAccepted increments
     * 
     * @return true if all cases pass, false if not
     * @throws Exception
     */
    public boolean handleAcceptedTest() throws Exception {
        int passed = 0;

        // Test 1
        startNode();
        Accepted accepted = new Accepted(5, 2, 1, 0);
        outObj.writeObject(accepted);
        outObj.flush();
        Thread.sleep(200);
        if(proposerTest.numAccepted > 0) {
            System.out.println("Unit 2.1 Successful");
            passed++;
        } else System.out.println("Unit 2.1 Unsuccessful");
        closeNode();

        System.out.println("Unit 3 Tests Passed: " + passed + "/1");
        if(passed == 1) return true;
        return false;

    }

    /**
     * Unit Test 4
     * Tests the functionality of the handleNack method in Node.java
     * 1 Test conducted
     * 1. Sends a nack - NACKed increments
     * 
     * @return true if all cases pass, false if not
     * @throws Exception
     */
    public boolean handleNackTest() throws Exception {
        int passed = 0;

        // Test 1
        startNode();
        Nack nack = new Nack(5, 1, 0);
        outObj.writeObject(nack);
        outObj.flush();
        Thread.sleep(200);
        if(proposerTest.NACKed > 0) {
            System.out.println("Unit 4.1 Successful");
            passed++;
        } else System.out.println("Unit 4.1 Unsuccessful");
        closeNode();

        System.out.println("Unit 4 Tests Passed: " + passed + "/1");
        if(passed == 1) return true;
        return false;

    }

    /**
     * Unit Test 5
     * Tests the functionality of the handleResponse method in Node.java
     * 1 Test conducted
     * 1. Sends a Response - Accepted values on node sets to those in message
     * 
     * @return true if all cases pass, false if not
     * @throws Exception
     */
    public boolean handleResponseTest() throws Exception {
        int passed = 0;

        // Test 1
        startNode();
        Response response = new Response(5, 2, 1, 0);
        outObj.writeObject(response);
        outObj.flush();
        Thread.sleep(200);
        if(testNode.acceptedProposal == 5 && testNode.acceptedValue == 2) {
            System.out.println("Unit 5.1 Successful");
            passed++;
        } else System.out.println("Unit 5.1 Unsuccessful");
        closeNode();

        System.out.println("Unit 5 Tests Passed: " + passed + "/1");
        if(passed == 1) return true;
        return false;

    }

    public static void main(String args[]) {
        

        try {
            testCase_unitTests test = new testCase_unitTests();
            // Test 1 handle prepare
            test.handlePrepareTest();

            // Test 2 handle promise
            test.handlePromiseTest();

            // Test 3 handle accepted
            test.handleAcceptedTest();

            // Test 4 handle nack
            test.handleNackTest();

            // Test 5 handle response
            test.handleResponseTest();


        } catch(Exception e) {
            System.out.println("Exception Occured: " + e.getMessage());
        }
    }


}
