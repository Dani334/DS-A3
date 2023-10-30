# DS-A3

## Paxos Implementation
There are two main files to the Paxos implementation, Node and Proposer. The proposer is a sub-class of Node, so that it can use it's socket connection capabilities. The program is initialised by creating two proposers and then creating each node. The nodes know about the proposer's (so that it can set their member variables) but does not know the ID of the proposer. The only reason I took this route is for easier implementation and it is hard coded to only have 2 proposers (as per the assignment specification, that is all we need to worry about). 

The protocol starts by randomly creating two proposers between 1-3. It then starts all the nodes on a listening loop, awaiting for all new connections. Note all socket connections use port 6001-6009. Then in each testCase java file, it will perform different tests and gather information to see if they passed.

There are some extra classes in use

Message.java
Prepare.java
Promise.java
Accept.java
Accepted.java
Nack.java

All used to create the messages used in communication

AND

RoundStats.java

To save the stats of each round of each proposer used for analysis.

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

On any processes that show up. If you cannot kill any of these processes, please change `serverSocket = new ServerSocket(6000 + nodeID);` to start from anything other than 6000 with enough room for the next 9 ports available.

Once all ports have been freed up you can run `make` to compile all classes and then run the following test cases

```

java testCase_immediateReplies
java testCase_delayedReplies

```