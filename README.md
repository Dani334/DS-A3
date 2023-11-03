# DS-A3

## Paxos Implementation
There are two main files to the Paxos implementation, Node.java and Proposer.java. The proposer is a sub-class of Node, so that it can use it's socket connection capabilities. The program is initialised by creating two proposers and then creating each node. The nodes know about the proposer's (so that it can set their member variables) but does not know the ID of the proposer. The only reason I took this route is for easier implementation and it is hard coded to only have 2 proposers (as per the assignment specification, that is all we need to worry about). 

The protocol starts by randomly creating two proposers between 1-3. It then starts all the nodes on a listening loop, awaiting for all new connections. Note all socket connections use port 6001-6009. Then in each testCase java file, it will perform different tests and gather information to see if they passed.

### Delays Implementation

The way I have simulated delays and dropping of messages is by using a message queue and timestamps on each message. Each message is receieved instantly but if the delayed boolean is set then it will randomise either the chance it replies instantly, the chance is drops the message or the time before replying to the message. The profiles were set up as follows:

- Member 1: reply instantly to everything no matter what
- Member 2: has a 30% chance to reply instantly to messages otherwise has a 7.5 second delay
- Member 3: has a 10% chance to drop a message. Also has a 100% chance for a 5 second delay if no messages dropped
- Members 4-9: have a randomised interval between 0 and 2.5 seconds delay

### Helper classes

There are some extra classes in use in Message/

- Message.java
- Prepare.java
- Promise.java
- Accept.java
- Accepted.java
- Nack.java
- Response

All used to create the messages used in communication

AND in helper/

- RoundStats.java

To save the stats of each round of each proposer used for analysis.

### Directory Structure

```
.
├── Message
│   ├── Accept.java
│   ├── Accepted.java
│   ├── Message.java
│   ├── Nack.java
│   ├── Prepare.java
│   ├── Promise.java
│   └── Response.java
├── README.md
├── freePorts.sh
├── helper
│   └── RoundStats.java
├── main
│   ├── Node.java
│   └── Proposer.java
├── makefile
└── testCases
    ├── testCase_delayedReplies.java
    ├── testCase_immediateReplies.java
    └── testCase_unitTests.java
```

## Testing

Before beginning testing please run the following commands:

```

chmod +x freePorts.sh
sh freePorts.sh

```

This will display any ports that are currently in use that my implementation relies on, please use the following command:

```

kill -9 <PID>

```

On any processes if they show up. If none show up, no need to kill and processes. If you cannot kill any of these processes, please change `public int startPort = 6000;` to start from anything other than 6000 with enough room for the next 9 ports available. This must be changed in the following files:

- Node.java
- Proposer.java
- testCase_immediateReplies.java
- testCase_delayedReplies.java
- testCase_unitTests.java

Once all ports have been freed up you can run `make` to compile all classes and then run the following test cases

```

java testCases/testCase_immediateReplies
java testCases/testCase_delayedReplies
java testCases/testCase_unitTests

```
Please analyse the terminal output for information regarding the passing of test cases.

Please note that the amount of test cases within each major test case can be set, for example in testCase_immediateReplies.java, `int test1Cases = 30;` can be set to equal a higher/lower number if you would like more randomised tests to be run, just ensure you are using `make` again before running `java testCases/testCase_immediateReplies`. You will find that the delayed replies test cases only has a few randomised tests conducted, which is due to some cases taking a long time and was set so that the tests do not take long to finish. 