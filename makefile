JFLAGS = -g -cp lib/*.jar CommunicationHandler.java
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
		CommunicationHandler.java \
		Proposer.java \
		Acceptor.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class