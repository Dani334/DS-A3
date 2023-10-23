JFLAGS = -g -cp lib/*.jar Message.java Prepare.java Promise.java Accept.java Accepted.java Nack.java Proposer.java Node.java
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
		Node.java \
		Proposer.java \
		Message.java \
		Prepare.java \
		Promise.java \
		Accept.java \
		Accepted.java \
		Nack.java
 
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class