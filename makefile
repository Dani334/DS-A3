JFLAGS = -g -cp lib/*.jar Message.java Prepare.java Promise.java Accept.java Accepted.java
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
		Node.java \
		Message.java \
		Prepare.java \
		Promise.java \
		Accept.java \
		Accepted.java 
 
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class