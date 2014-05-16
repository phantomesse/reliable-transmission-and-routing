Lauren Zou (ljz2112)
Computer Networks
Programming 2

Brief Description of Code
-------------------------
My Bellman-Ford reliable transmission and routing program consists of various parts.

MAIN EXECUTION: BFClient.java
-----------------------------
Main class of the program. Handles communication between the graphical user interface, the sockets (both reading and writing), and the routing table.

DATA STRUCTURE: Client.java
---------------------------
A {@link Client} class that can be used for multiple purposes. Contains information about a client, mainly the IP address and the port number. Also contains information about the client for the routing table such as cost and link.

DATA STRUCTURE: RoutingTable.java
---------------------------------
Data structure for the routing table.

DATA STRUCTURE: FileChunk.java
------------------------------
Data structure for a file chunk that is transmitted between clients.

GUI: BFClientUI.java
--------------------
Graphical user interface for the BFClient. Uses many custom graphical user interface components:

    CommandUI.java
    --------------
    Graphical user interface component for the prompt panel. Contains controls for executing various commands such as LINKUP and LINKDOWN.

    Console.java
    ------------
    Graphical user interface component for the output.

    FileChunkUI.java
    ----------------
    Graphical user interface component for the FileChunk. Uses check boxes to show whether a client has a chunk or not. Note that, even though a client may have a check box ticked, it may not have that file chunk to send to other clients since it may have gotten that file chunk from another client.

    RoutingTableUI.java
    -------------------
    Graphical user interface component for displaying the routing table.

COMMUNICATION: Message.java
---------------------------
Abstract class for messages that are sent in DatagramPacket objects. There are four classes that extend this class which represent the four different types of inter-client communication protocols:

    LinkDownMessage.java
    --------------------
    A message for communicating link downs.

    LinkUpMessage.java
    ------------------
    A message for communicating link ups.

    RouteUpdateMessage.java
    -----------------------
    A message for communicating route updates.

    TransferMessage.java
    --------------------
    A message for FileChunk transfers.

SOCKET: ReadSocket.java
-----------------------
A DatagramSocket for reading in messages.

SOCKET: WriteSocket.java
------------------------
A DatagramSocket for writing messages.



Details on Development Environment
----------------------------------
I chose to develop this project in Java. BFClientUI is GUI that uses the Javax Swing library.



Instructions on Running the Code
--------------------------------
There is a Makefile that will compile the code

make - compiles BFClient.java, which should also compile the other classes
make clean - removes all the *.class files

To run the BFClient: java BFClient <config file>



Sample Commands to Invoke Code
------------------------------
java BFClient config.txt

I have included the following test configuration files:
    config.txt
    config1.txt
    config2.txt
    config3.txt

Also, I have included two chunk files:
    chunk1
    chunk2



Additional Functionality
------------------------
Graphical User Interface:
    I wrote a graphical user interface using the Javax Swing Library which displays a JFrame when the BFClient is loaded. The graphical user interface is useful, since it allows the user to pick the clients to apply actions to (such as LINKDOWN, LINKUP, and TRANSFER) from a dropdown menu. The IP address and port number of the client is also displayed at the top of the window so that a user with multiple clients open on one screen can differentiate between which window belongs to which client.

Automatically Updating Routing Table Display:
    The GUI includes a custom table that displays the routing table and automatically updates whenever there is a change in the routing table. This routing table is implemented using a custom table model so that the display does not flicker too much when the table is updated.

File Chunk Status Display:
    The GUI also includes a display for the status of the file chunks using check boxes. A client that starts off with a file chunk will have a check box checked off. The check box on the left represents the first chunk and the checkbox on the right represents the second chunk. When a client receives a chunk, the appropriate checkbox is checked off. This status display is useful in knowing which file chunks the client currently has.



Protocol for Inter-Client Communications and Chunk Transfer
-----------------------------------------------------------
My inter-client communications protocol has four different kinds of messages. In the code, all of these messages extend an abstract class Message to avoid too much redundant code. Each message has a header that is unique to the message type. The four message types are LINKDOWN, LINKUP, ROUTE_UPDATE, and TRANSFER. These types are denoted as the first element in the header. The second element in the header is the port number of the client that is sending the message. The TRANSFER message requires additional parameters in the header such as file name, sequence number, destination client, and path string length (which denotes the length of the string that describes the path of the file chunk across clients).

LINKDOWN
--------
Syntax: <message type> <port number>
        <link down client ip address>:<link down client port number>

Ex: LINKDOWN 30000
    127.0.0.1:4115

LINKUP
------
Syntax: <message type> <port number>
        <link up client ip address>:<link up client port number>

Ex: LINKUP 30000
    127.0.0.1:4115 3.0

ROUTE_UPDATE Message
--------------------
Syntax: <message type> <port number>
        <routing table entry client ip address>:<routing table entry client port number> <cost>
        <routing table entry client ip address>:<routing table entry client port number> <cost>
        ...

Ex: ROUTE_UPDATE 30000
    127.0.0.1:4115 5.5
    127.0.0.1:10000 8.5
    127.0.0.1:20000 4.1

TRANSFER Message
----------------
Syntax: <message type> <port number> <file name> <file sequence number> <destination client ip address>:<destination client port number> <length of path string>
        <path string>
        <chunk binary>

The path string is a newline delimited string in the form <ip address>:<port number> at <timestamp>

Ex: TRANSFER 30000 chunk1 1 127.0.0.1:20000 26
    127.0.0.1:4115 at 19:48:58
    <chunk binary>