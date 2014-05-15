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
            return new Message(packet);
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

    @Override
    public void run() {
        while (true) {
            Message message = receive();

            System.out.println("Received something!");
            
            if (message == null) {
                continue;
            }

            // Process message
            switch (message.getType()) {
                case ROUTE_UPDATE:
                    Iterator<Entry<String, Double>> routeUpdateMessageIterator = message
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
                    FileChunk chunkReceived = message.getFileChunk();
                    
                    System.out.println("Received a transfer something!");

                    InetAddress destinationAddress = chunkReceived
                            .getDestination().getIpAddress();
                    if (isIpAddressMe(destinationAddress)
                            && chunkReceived.getDestination().getPortNumber() == bfclient
                                    .getPortNumber()) {
                        // Destination is me. We have reached the destination
                        bfclient.processChunk(chunkReceived);
                    } else {
                        bfclient.transfer(chunkReceived);
                    }
            }
        }
    }
}
