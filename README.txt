Lauren Zou (ljz2112)
Computer Networks

Protocol for Inter-Client Communications
----------------------------------------
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
Syntax: <message type> <port number>

Ex: TRANSFER 30000 chunk1 1 127.0.0.1:20000 26
    127.0.0.1:4115 at 19:48:58
    <chunk binary>