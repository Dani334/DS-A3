package main;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import Message.Accept;
import Message.Message;
import Message.Prepare;
import helper.RoundStats;

public class Proposer extends Node {

    private int round = 1;
    private int lastProposalNumber;
    public int receivedPromises = 0;
    public int receivedReplies = 0;

    public int previousHighestProposalNumber;
    public int previousHighestProposalValue;

    public int numNotAccepted = 0;
    public int numAccepted = 0;
    public int NACKed = 0;
    
    public Vector<RoundStats> stats = new Vector<RoundStats>();
    public LocalTime timestampStart;
    public LocalTime timestampEnd;

    public CountDownLatch latchPromise; 
    public CountDownLatch latchAccept; 
    public CountDownLatch latchReply;
    private static final Object lock = new Object();

    public Vector<Integer> acceptorIds = new Vector<>();

    private Boolean STAT = false;
    
    /**
     * Calls the constructor of the super-class Node, initialises proposlal number, and latches
     * 
     * @param nodeID the id of the proposer
     * @param proposalNumber the initial propsoal number to propose
     * @throws Exception
     */
    public Proposer(int nodeID, int proposalNumber) throws Exception {
        super(nodeID);
        lastProposalNumber = proposalNumber;
        latchPromise = new CountDownLatch(4);
        latchAccept = new CountDownLatch(4);
        latchReply = new CountDownLatch(8);
    }

    /**
     * Creates a thread instance and starts Phase1 of Paxos
     * 
     */
    @Override
    public void run() {
        try {
            Phase1(this.lastProposalNumber);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Called on a failed round, resets member variables and starts phase 1 again
     * 
     * @throws Exception
     */
    public void restartPhase1() throws Exception {
        round++;
        latchPromise = new CountDownLatch(4);
        latchAccept = new CountDownLatch(4);
        latchReply = new CountDownLatch(8);
        this.acceptorIds.clear();
        this.receivedPromises = 0;
        this.numAccepted = 0;
        this.NACKed = 0;
        this.numNotAccepted = 0;
        this.receivedReplies = 0;
        Phase1(lastProposalNumber);
    }

    /**
     * This method initialises Phase 1 of the paxos protocol
     * It will send prepare messages out to all nodes (including the other proposer), then it will wait until it recieves 8 replies
     * or until it times out.
     * It will then begin phase 2
     * 
     * @param proposalNumber the proposal number of the proposal
     * @throws Exception
     */
    public void Phase1(int proposalNumber) throws Exception {

        this.lastProposalNumber = proposalNumber;
        this.timestampStart = LocalTime.now();

        for(int i = 1; i <= 9; i++) {
            if(i != this.nodeID)
                this.sendPrepare(i, this.lastProposalNumber);
        }
        
        this.lastProposalNumber++;

        if(awaitPhase2()) {
            synchronized(lock) {
                System.out.println("Moving on to phase 2");
                System.out.println("Replies: " + this.receivedReplies);
                System.out.println("Promises: " + this.receivedPromises);
            }

        } else {
            synchronized(lock) {
                System.out.println("failed to receive 8 replies or 4 promises");
                System.out.println("Replies: " + this.receivedReplies);
                System.out.println("Promises: " + this.receivedPromises);
            }
        }

        if(this.receivedPromises < 4) {
            this.timestampEnd = LocalTime.now();
            RoundStats stat = new RoundStats();
            stat.saveStats(nodeID, round, proposalNumber, numAccepted, NACKed, receivedPromises, -1, timestampStart, timestampEnd);
            stats.add(stat);
            if(STAT) synchronized(lock) {stat.printStats();}
            restartPhase1();
        } else {

            Phase2(proposalNumber);
        }

    }

    /**
     * This method begins phase 2 of the paxos protocol
     * It will check to see how many promises it received and if it is less than 4, restart phas 1.
     * If it receives a majority of promises (>4), it will send an accept message to those that sent the promises
     * It will then await accepted messages and in the case where we recieve 4, we set the accepted proposal number and value
     * It will restart phase 1 in the case of receiving <4 accepted messages
     * 
     * @param proposalNumber
     * @throws Exception
     */
    public void Phase2(int proposalNumber) throws Exception {

        int proposalValue = -1;
        if(numNotAccepted >= 4) {
            proposalValue = this.nodeID;
        } else {
            proposalValue = this.previousHighestProposalValue;
        }
        
        
        for(int i = 0; i < acceptorIds.size(); i++) {
            this.sendAccept(acceptorIds.elementAt(i), proposalNumber, proposalValue);
        }

        if(awaitAccepted()) {
            System.out.println("true returned");
            System.out.println("Accepteds receieved: " + numAccepted);
        } else {
            System.out.println("false returned");
            System.out.println("Accepteds receieved: " + numAccepted);
        }
        
        this.timestampEnd = LocalTime.now();
        
        RoundStats stat = new RoundStats();
        stat.saveStats(nodeID, round, proposalNumber, numAccepted, NACKed, receivedPromises, proposalValue, timestampStart, timestampEnd);
        stats.add(stat);
        if(STAT) synchronized(lock) {stat.printStats();}

        // if we received less than 4 Accepted's, it means we do not have a majority, so we reset variables and try again
        // It could mean that there is already a value accepted or it could mean some messages were dropped.
        if(this.numAccepted < 4) {
            restartPhase1();
        } else {
            
            this.acceptedProposal = proposalNumber;
            this.acceptedValue = proposalValue;
            
        }

    }

    /**
     * This method will enter an infinite loop until either 4 Promises or 8 Replies are receieved in which it will return true
     * Or until a timeout occurs (4 Promises and 8 Replies both not reached) in which it will return false
     * 
     * @return ready - true or false depending on if the phase is ready to transition to phase 2
     * @throws Exception
     */
    public boolean awaitPhase2() throws Exception {

        LocalTime timeNow = LocalTime.now();
        boolean ready = false;
        while(!ready) {
            ready = latchReply.await(0, TimeUnit.MILLISECONDS) || latchPromise.await(0, TimeUnit.MILLISECONDS);
            LocalTime timeWhile = LocalTime.now();
            
            if(timeNow.until(timeWhile, ChronoUnit.MILLIS) > 30000) {
                return false;
            }
        }

        return ready;
    }

    /**
     * This method will enter an infinite loop until 4 Accepteds receieved in which it will return true
     * Or until a timeout occurs (4 Accepteds not reached) in which it will return false
     * 
     * @return ready - true or false depending on if 4 Accpeteds reached after a certain time
     * @throws Exception
     */
    public boolean awaitAccepted() throws Exception {

        LocalTime timeNow = LocalTime.now();
        System.out.println("TIME NOW: " + timeNow);
        boolean ready = false;
        while(!ready) {
            ready = latchAccept.await(0, TimeUnit.MILLISECONDS);
            LocalTime timeWhile = LocalTime.now();
            if(timeNow.until(timeWhile, ChronoUnit.MILLIS) > 60000) {
                System.out.println("TIME WHILE: " + timeWhile);
                return false;
            }
        }

        return ready;
    }

    /**
     * Sends the prepare message to a given node
     * 
     * @param targetID id of the node to send the prepare
     * @param proposalNumber proposal number of the prepare message
     * @throws Exception
     */
    public void sendPrepare(int targetID, int proposalNumber) throws Exception {

        Message prepare = new Prepare(proposalNumber, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(prepare);
        outObj.flush();
        socket.close();

    }

    /**
     * Sends an accept message to a given node
     * 
     * @param targetID id of the node to send the accept message
     * @param proposalNumber propsoal number of the promised proposal
     * @param proposalValue proposal value of the proposal
     * @throws Exception
     */
    public void sendAccept(int targetID, int proposalNumber, int proposalValue) throws Exception {

        Accept accept = new Accept(proposalNumber, proposalValue, this.nodeID, targetID);
        Socket socket = new Socket("127.0.0.1", 6000 + targetID);
        outObj = new ObjectOutputStream(socket.getOutputStream());
        outObj.writeObject(accept);
        outObj.flush();
        socket.close();
        
    }

}
