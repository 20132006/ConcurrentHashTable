all: RUN

RUN:
	gcc -Wall -O3 -o hashtable hashtable.c -lrt -pthread
clean:
	rm hashtable
