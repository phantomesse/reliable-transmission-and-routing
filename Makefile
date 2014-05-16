all: BFClient.class

%.class: %.java
	javac $<

clean:
	rm *.class output
	