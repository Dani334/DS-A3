JFLAGS = -g -cp lib/*.jar Message/Message.java Message/Prepare.java Message/Promise.java Message/Accept.java Message/Accepted.java Message/Nack.java main/Proposer.java main/Node.java helper/RoundStats.java
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
		main/Node.java \
		main/Proposer.java \
		testCases/testCase_immediateReplies.java \
		testCases/testCase_delayedReplies.java
 
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
	$(RM) Message/*.class
	$(RM) main/*.class
	$(RM) helper/*.class
	$(RM) testCases/*.class