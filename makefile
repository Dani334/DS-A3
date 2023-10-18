JFLAGS = -g -cp lib/*.jar
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
		Node.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class