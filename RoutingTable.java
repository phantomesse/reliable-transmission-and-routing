import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data structure for the routing table.
 * 
 * @author Lauren Zou
 */
public class RoutingTable {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat(
            "HH:mm:ss");
    private ConcurrentHashMap<String /* ipAddress:portNumber */, Client> routingTable;

    public RoutingTable() {
        routingTable = new ConcurrentHashMap<String, Client>();
    }

    /**
     * Checks if a client is a direct neighbor.
     */
    public boolean isDirectNeighbor(Client client) {
        Client link = routingTable.get(client.getIpAddressPortNumberString())
                .getLink();

        return client.getIpAddressPortNumberString().equals(
                link.getIpAddressPortNumberString());
    }

    public Client get(Client client) {
        return routingTable.get(client.getIpAddressPortNumberString());
    }

    public synchronized void linkdown(Client client) {
        routingTable.get(client.getIpAddressPortNumberString())
                .setCost(Double.POSITIVE_INFINITY);
    }

    public synchronized void touch(Client client) {
        routingTable.get(client.getIpAddressPortNumberString())
                .updateLastHeardFrom();
    }

    /**
     * Adds or updates an entry in the routing table. Destination and link
     * {@link Client} objects should have at least an {@link InetAddress} and a
     * port number. Returns true if the routing table has been updated and false
     * if the routing table has not been updated.
     */
    public synchronized boolean update(Client destinationClient,
            Client linkClient, double cost) {
        // Get the actual cost to the destination client
        cost = getActualCost(destinationClient, linkClient, cost);

        // Check if routing table already has the destination client
        if (routingTable.containsKey(destinationClient
                .getIpAddressPortNumberString())) {
            // Destination client already exists in the routing table
            destinationClient = routingTable.get(destinationClient
                    .getIpAddressPortNumberString());

            // Check if this update is a link down
            if (cost == Double.POSITIVE_INFINITY
                    && destinationClient.getLink()
                            .getIpAddressPortNumberString()
                            .equals(linkClient.getIpAddressPortNumberString())) {
                // This is a link down
                if (destinationClient.getCost() != Double.POSITIVE_INFINITY) {
                    destinationClient.setCost(cost);
                    return true;
                } else {
                    return false;
                }
            }

            // Only update the link if the new cost is smaller
            if (cost < destinationClient.getCost()) {
                destinationClient.setCost(cost);
                destinationClient.setLink(linkClient.getIpAddress(),
                        linkClient.getPortNumber());
                return true;
            }

            // Nothing to change
            return false;
        } else {
            // Destination client does not already exist in the routing table,
            // so add the client to the routing table.
            add(destinationClient, linkClient, cost);
            return true;
        }
    }

    /**
     * Adds an entry to the routing table without any checking.
     */
    private void add(Client destinationClient, Client linkClient, double cost) {
        Client client = new Client(destinationClient.getIpAddress(),
                destinationClient.getPortNumber(), cost,
                linkClient.getIpAddress(), linkClient.getPortNumber());
        routingTable.put(destinationClient.getIpAddressPortNumberString(),
                client);
    }

    /**
     * Gets the actual cost from to a destination client by appending it to the
     * cost of the link client.
     */
    private double getActualCost(Client destinationClient, Client linkClient,
            double cost) {
        // Check if the cost is infinity. If so, there's nothing we can do.
        if (cost == Double.POSITIVE_INFINITY) {
            return cost;
        }

        // Check if the destination and link clients are the same
        boolean same = destinationClient.getIpAddress().equals(
                linkClient.getIpAddress())
                && destinationClient.getPortNumber() == linkClient
                        .getPortNumber();

        if (same) {
            // The destination and link clients are the same, so there is no
            // need to append to the cost
            return cost;
        }

        // Get the cost to the link client
        if (!routingTable.containsKey(
                linkClient.getIpAddressPortNumberString())) {
            // Link client does not exist, so add it with a cost of infinity
            add(linkClient, linkClient, Double.POSITIVE_INFINITY);
        }
        double linkClientCost = routingTable.get(
                linkClient.getIpAddressPortNumberString()).getCost();

        return linkClientCost + cost;
    }

    public Collection<Client> getClients() {
        return routingTable.values();
    }

    @Override
    public String toString() {
        // Get current time
        String currentTime = FORMAT.format(new Date());

        // Handle the case where the routing table is empty
        if (routingTable.isEmpty()) {
            return currentTime + " There's nothing here! :(";
        }

        // Add each vector
        String str = currentTime + " Distance vector list is:";
        Iterator<Client> iter = routingTable.values().iterator();
        while (iter.hasNext()) {
            str += "\n" + iter.next().toString();
        }

        return str;
    }
}