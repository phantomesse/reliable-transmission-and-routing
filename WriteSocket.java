import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Iterator;

public class WriteSocket extends Thread {
    private BFClient bfclient;
    private DatagramSocket socket;

    private int timeout;

    public WriteSocket(BFClient bfclient, int timeout) {
        this.bfclient = bfclient;
        this.timeout = timeout;

        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendRouteUpdate() {
        Collection<Client> routingTableClients = bfclient.getRoutingTable()
                .getClients();

        Iterator<Client> iter = routingTableClients.iterator();
        while (iter.hasNext()) {
            Client client = iter.next();

            // Check if client should be dead by now
            long lastHeardFrom = client.getLastHeardFrom();
            long now = System.currentTimeMillis();
            long difference = (now - lastHeardFrom) / 1000;
            if (difference > timeout * 3) {
                // Link down this client
                bfclient.getRoutingTable().linkDown(client);
            }
            
            // Build a routing message that excludes this client in the
            // message
            String messageStr = "";
            Iterator<Client> iter2 = routingTableClients.iterator();
            while (iter2.hasNext()) {
                Client clientToAdd = iter2.next();
                if (!clientToAdd.equals(client)) {
                    messageStr += client.getIpAddressPortNumberString()
                            + " " + client.getCost() + "\n";
                }
            }

            Message message = new Message(Message.MessageType.ROUTE_UPDATE,
                    null, bfclient.getPortNumber(), messageStr);
            DatagramPacket packet = message.encode(client);
            try {
                socket.send(packet);
            } catch (IOException e) {
                // Ignore if packet failed
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
                // We're being interrupted. That's okay.
            }
        }
    }
}
