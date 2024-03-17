Project Description
-------------------

1.1 Part 1
Implement a distributed system consisting of n nodes, numbered 0 to n − 1, arranged in a certain
topology. The topology and information about other parameters will be provided in a configuration
file.
All channels in the system are bidirectional, reliable and satisfy the first-in-first-out (FIFO)
property. You can implement a channel using a reliable socket connection (with TCP or SCTP).
For each channel, the socket connection should be created at the beginning of the program and
should stay intact until the end of the program. All messages between neighboring nodes are
exchanged over these connections.
All nodes execute the following protocol:
 Initially, each node in the system is either active or passive. At least one node must be
active at the beginning of the protocol.
 While a node is active, it sends anywhere from minPerActive to maxPerActive messages, and
then turns passive. For each message, it makes a uniformly random selection of one of its
neighbors as the destination. Also, if the node stays active after sending a message, then it
waits for at least minSendDelay time units before sending the next message.

 Only an active node can send a message.
 A passive node, on receiving a message, becomes active if it has sent fewer than maxNumber
messages (summed over all active intervals). Otherwise, it stays passive.
We refer to the protocol described above as the MAP protocol.
1.2 Part 2

Implement the Chandy and Lamport’s protocol for recording a consistent global snapshot as dis-
cussed in the class. Assume that the snapshot protocol is always initiated by node 0 and all channels

in the topology are bidirectional. Use the snapshot protocol to detect the termination of the MAP

protocol described in Part 1. The MAP protocol terminates when all nodes are passive and all channels are empty. To detect termination of the MAP protocol, augment the Chandy and Lamport’s

snapshot protocol to collect the information recorded at each node at node 0 using a converge-cast
operation over a spanning tree. The tree can be built once in the beginning or on-the-fly for an
instance using MARKER messages.
Note that, in this project, the messages exchanged by the MAP protocol are application messages
and the messages exchanged by the snapshot protocol are control messages. The rules of the MAP
protocol (described in Part 1) only apply to application messages. They do not apply to control
messages.
1.3 Part 3
To test that your implementation of the Chandy and Lamport’s snapshot protocol is correct,
implement Fidge/Mattern’s vector clock protocol described in the class. The vector clock of a node
is part of the local state of the node and its value is also recorded whenever a node records its local
state. Node 0, on receiving the information recorded by all the nodes, uses these vector timestamps
to verify that the snapshot is indeed consistent. Note that only application messages will carry
vector timestamps.
1.4 Part 4

Design and implement a protocol for bringing all nodes to a halt after node 0 has detected termi-
nation of the MAP protocol.


To run the program you need follow these steps:

1. Modify the netid in launcher.sh and cleanup.sh to your netid
2. Modify the PROJDIR and CONFIGLOCAL in launcher.sh and cleanup.sh to your working directory
3. Run this command chmod +x build.sh launcher.sh cleanup.sh cleanFiles.sh
    - if at any point in running the scripts you recieve an error that is something like "token error: token is ""
    and you modify your scripts in Windows then you are experiencing conflict from the use of carriage returns.
    Run the following command on the file and try again to remove the carriage returns (<filename> is replaced with the file's name):
        sed -i -e 's/\r$//' <filename>
    - Enable graphic display with your tunneling software [ex in mobaxterm, enable x server] to view terminal popup windows
4. Run javac *.java
5. Run ./launcher.sh to run the program
6. Run ./cleanup.sh to kill erroneous processes
