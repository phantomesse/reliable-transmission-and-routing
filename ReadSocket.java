import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map.Entry;

public class ReadSocket extends Thread {

    public static int PACKET_BUFFER_LENGTH = 65507;
    private BFClient bfclient;
    private DatagramSocket socket;

    public ReadSocket(BFClient bfclient) {
        this.bfclient = bfclient;

        try {
            socket = new DatagramSocket(bfclient.getPortNumber());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
        interrupt();
    }

    public Message receive() {
        byte[] buffer = new byte[PACKET_BUFFER_LENGTH];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try {
            socket.receive(packet);
            return Message.decode(packet);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean isIpAddressMe(InetAddress ipAddress) {
        if (ipAddress.isAnyLocalAddress() || ipAddress.isLoopbackAddress())
            return true;

        try {
            return NetworkInterface.getByInetAddress(ipAddress) != null;
        } catch (SocketException e) {
            return false;
        }
    }

    private boolean isMe(Client client) {
        return isIpAddressMe(client.getIpAddress())
                && client.getPortNumber() == bfclient.getPortNumber();
    }

    @Override
    public void run() {
        while (true) {
            Message message = receive();

            if (message == null) {
                continue;
            }

            // Process message
            switch (message.getType()) {
                case LINKUP:
                    // Get the client that is up
                    Client clientUp = ((LinkUpMessage) message).getClientUp();
                    
                    if (isMe(clientUp)) {
                        clientUp = bfclient.getRoutingTable().get(message.getFromClient());
                        
                        // Check if the link matches where this LINKUP message is from
                        Client link = clientUp.getLink();
                        if (link.getIpAddressPortNumberString().equals(message.getFromClient().getIpAddressPortNumberString())) {
                            // Link matches, so update the cost
                            clientUp.setCost(((LinkUpMessage) message).getCost());
                            
                            // Send a route update
                            bfclient.sendRouteUpdate();
                        }
                    }
                    
                    break;
                case LINKDOWN:
                    // Get the client that is down
                    Client clientDown = ((LinkDownMessage) message)
                            .getClientDown();

                    if (isMe(clientDown)) {
                        // Link that is down is the from client
                        clientDown = message.getFromClient();

                        // Link down in routing table
                        bfclient.getRoutingTable().linkdown(clientDown);
                        
                        // Set any links that are through this client to INFINITY
                        Iterator<Client> iter = bfclient.getRoutingTable().getClients().iterator();
                        while (iter.hasNext()) {
                            Client client = iter.next();
                            if (client.getLink().getIpAddressPortNumberString().equals(client.getLink().getIpAddressPortNumberString())) {
                                client.setCost(Double.POSITIVE_INFINITY);
                                
                                // Send link down
                                //bfclient.sendLinkDown(clientDown);
                            }
                        }
                    } else {
                        // TODO
                    }

                    bfclient.updateRoutingTableUI();

                    System.out.println("The client "
                            + clientDown.getIpAddressPortNumberString()
                            + " is down!");

                    break;
                case ROUTE_UPDATE:
                    Iterator<Entry<String, Double>> routeUpdateMessageIterator = ((RouteUpdateMessage) message)
                            .getRouteUpdate().entrySet().iterator();

                    // Update routing table
                    boolean routingTableChanged = false;
                    while (routeUpdateMessageIterator.hasNext()) {
                        Entry<String, Double> entry = routeUpdateMessageIterator
                                .next();
                        Client client = null;
                        try {
                            client = new Client(entry.getKey());
                        } catch (UnknownHostException e) {
                            // Something went wrong
                        }
                        double cost = entry.getValue();

                        // Check if this is a linkdown
                        boolean changed = bfclient.getRoutingTable()
                                .update(client,
                                        message.getFromClient(), cost);
                        if (!routingTableChanged) {
                            routingTableChanged = changed;
                        }
                    }
                    bfclient.getRoutingTable().touch(message.getFromClient());

                    bfclient.updateRoutingTableUI();

                    // Send route update if necessary
                    if (routingTableChanged) {
                        bfclient.sendRouteUpdate();
                    }
                    break;
                case TRANSFER:
                    FileChunk chunkReceived = ((TransferMessage) message)
                            .getFileChunk();

                    System.out.println("Received a transfer something!");

                    if (isMe(chunkReceived.getDestination())) {
                        // Destination is me. We have reached the destination
                        bfclient.processChunk(chunkReceived);
                    } else {
                        bfclient.transfer(chunkReceived);
                    }
            }
        }
    }
}
