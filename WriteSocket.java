import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;

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

    /**
     * Transfers a chunk to a client.
     */
    public void transferChunk(FileChunk chunk, Client toClient,
            Client destination) {
        System.out.println("Transferring a chunk to "
                + toClient.getIpAddressPortNumberString());

        Message message = new Message(
                new Client(null, bfclient.getPortNumber()),
                chunk);

        DatagramPacket packet = message.encode(toClient, destination);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("Transfer chunk to "
                    + toClient.getIpAddressPortNumberString()
                    + " failed to send");
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
            long lastHeardFrom = client.getLastHeardFrom();
            long now = System.currentTimeMillis();
            long difference = (now - lastHeardFrom) / 1000;
            if (difference > timeout * 3) {
                // Link down this client
                bfclient.getRoutingTable().linkdown(client);
                bfclient.getGUI().updateRoutingTableUI(routingTable);
            }

            routeUpdateMessage.put(client.getIpAddressPortNumberString(),
                    client.getCost());
        }
        Message message = new Message(
                new Client(null, bfclient.getPortNumber()), routeUpdateMessage);

        // Send message to neighbors
        iter = routingTable.getClients().iterator();
        while (iter.hasNext()) {
            Client toClient = iter.next();
            DatagramPacket packet = message.encode(toClient, null);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("Route update to "
                        + toClient.getIpAddressPortNumberString()
                        + " failed to send");
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
