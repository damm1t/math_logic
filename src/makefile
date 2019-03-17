.PHONY: all compile run pack clean out

all: out compile

run:
	java -XX:+UseSerialGC -jar out.jar

compile:
	kotlinc *.kt -d out.jar -include-runtime