import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * {@link DatagramSocket} for writing messages.
 * 
 * @author Lauren Zou
 */
public class WriteSocket extends Thread {
    private BFClient bfclient;
    private DatagramSocket socket;
    private int timeout; // in seconds

    public WriteSocket(BFClient bfclient, int timeout) {
        this.bfclient = bfclient;
        this.timeout = timeout;

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public int getTimeout() {
        return timeout;
    }

    public void close() {
        socket.close();
        interrupt();
    }

    public void linkUp(Client clientUp, double cost) {
        LinkUpMessage message = new LinkUpMessage(new Client(null,
                bfclient.getPortNumber()), clientUp, cost);

        DatagramPacket packet = message.encode(clientUp);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // Something went wrong
        }
    }

    /**
     * Announces a link down to a neighbor.
     */
    public void linkDown(Client clientDown) {
        LinkDownMessage message = new LinkDownMessage(new Client(null,
                bfclient.getPortNumber()), clientDown);

        DatagramPacket packet = message.encode(clientDown);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // Something went wrong
        }
    }

    /**
     * Transfers a chunk to a client.
     */
    public void transferChunk(FileChunk chunk, Client toClient,
            Client destination) {
        chunk.setDestination(destination);

        TransferMessage message = new TransferMessage(
                new Client(null, bfclient.getPortNumber()),
                chunk);

        DatagramPacket packet = message.encode(toClient);
        try {
            socket.send(packet);
        } catch (IOException e) {
            // Something went wrong
        }
    }

    /**
     * Sends a route update to all of our neighbors.
     */
    public void sendRouteUpdate() {
        RoutingTable routingTable = bfclient.getRoutingTable();
        Iterator<Client> iter = routingTable.getClients().iterator();

        // Create route update message
        HashMap<String, Double> routeUpdateMessage = new HashMap<String, Double>();
        while (iter.hasNext()) {
            Client client = iter.next();

            // Check if client should be dead by now
            if (routingTable.isDirectNeighbor(client)) {
                long lastHeardFrom = client.getLastHeardFrom();
                long now = System.currentTimeMillis();
                long difference = (now - lastHeardFrom) / 1000;
                if (difference > timeout * 3) {
                    // Link down this client
                    bfclient.linkdownNoSend(client);
                    bfclient.getGUI().updateRoutingTableUI(routingTable);
                }
            }

            routeUpdateMessage.put(client.getIpAddressPortNumberString(),
                    client.getCost());
        }
        RouteUpdateMessage message = new RouteUpdateMessage(
                new Client(null, bfclient.getPortNumber()), routeUpdateMessage);

        // Send message to neighbors
        iter = routingTable.getClients().iterator();
        while (iter.hasNext()) {
            Client toClient = iter.next();

            // Don't send route update messages to neighbors with infinite costs
            if (toClient.getCost() == Double.POSITIVE_INFINITY) {
                continue;
            }

            // Only send route update messages to direct neighbors
            if (routingTable.isDirectNeighbor(toClient)) {
                DatagramPacket packet = message.encode(toClient);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    // Something went wrong
                }
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            sendRouteUpdate();

            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException e) {
                close();
            }
        }
    }
}
