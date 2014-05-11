package old2;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class RoutingTable {
	private HashMap<String /* ipAddress:portNumber */, Client> routingTable;
	private boolean blockAdding;

	public RoutingTable() {
		routingTable = new HashMap<String, Client>();
		blockAdding = false;
	}

	/**
	 * Adds a client to the routing table. Returns true if the routing table has
	 * changed. False otherwise.
	 */
	public boolean add(InetAddress ipAddress, int portNumber, double cost,
			InetAddress linkIpAddress, int linkPortNumber) {
		if (blockAdding) {
			return false;
		}
		
		String key = Client.getIpAddressPortNumberString(linkIpAddress,
				linkPortNumber);
		
		// Calculate the actual cost, which should be the sum of the cost and
		// the cost to get to the link
		if (!ipAddress.equals(linkIpAddress) && portNumber != linkPortNumber && cost != Double.POSITIVE_INFINITY) {
			Client link = routingTable.get(Client.getIpAddressPortNumberString(
					linkIpAddress, linkPortNumber));
			double linkCost = link.getCost();
			cost += linkCost;
		}

		// Check if client already exists in routing table
		if (routingTable.containsKey(key)) {
			// Client already exists in the routing table
			Client client = routingTable.get(key);
			client.updateLastHeardFrom();
		
			// Check if this is a link down (cost is set to infinity)
			if (cost == Double.POSITIVE_INFINITY && client.getIpAddress().equals(linkIpAddress) && client.getPortNumber() == linkPortNumber) {
				client.setCost(cost);
				return true;
			}

			// Check if the current cost in the routing table is higher than the
			// new cost
			if (client.getCost() <= cost) {
				// The current cost is already low, so we don't have to do
				// anything
				return false;
			} else {
				// New cost is better, so add the new info in
				client.setCost(cost);
				client.setLink(linkIpAddress, linkPortNumber);
				return true;
			}
		}

		// Client does not already exist in the routing table, so add it
		routingTable.put(key, new Client(ipAddress, portNumber, cost,
				linkIpAddress, linkPortNumber));
		return true;
	}

	public boolean add(InetAddress ipAddress, int portNumber, double cost) {
		return add(ipAddress, portNumber, cost, ipAddress, portNumber);
	}
	
	/**
	 * Retrieves a client in the routing table.
	 */
	public Client get(InetAddress ipAddress, int portNumber) {
		String key = Client.getIpAddressPortNumberString(ipAddress, portNumber);
		return routingTable.get(key);
	}

	/**
	 * Removes the list of clients (indicated by their client keys).
	 */
	public void kill(ArrayList<String> clientKeys) {
		for (String key : clientKeys) {
			routingTable.remove(key);
		}
	}
	
	/**
	 * Sets the cost of specified client to infinity.
	 */
	public void linkDown(InetAddress ipAddress, int portNumber) {
		String key = Client.getIpAddressPortNumberString(ipAddress, portNumber);
		Client client = routingTable.get(key);
		client.setCost(Double.POSITIVE_INFINITY);
	}
	
	public void setBlockAdding(boolean blockAdding) {
		this.blockAdding = blockAdding;
	}

	/**
	 * Gets a route update message which is a newline delimited list formatted
	 * <ip address>:<port number> <cost>.
	 * 
	 * @return
	 */
	public String getRouteUpdateMessage() {
		String message = "";
		Iterator<Client> iter = routingTable.values().iterator();
		while (iter.hasNext()) {
			Client client = iter.next();
			message += "\n" + Client.getIpAddressPortNumberString(client) + " "
					+ client.getCost();
		}
		return message.isEmpty()? message : message.substring(1);
	}

	/**
	 * Returns a {@link Collection} of {@link Client}s in the routing table.
	 */
	public Collection<Client> getClients() {
		return routingTable.values();
	}

	@Override
	public String toString() {
		// Get current time
		SimpleDateFormat currentTime = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String str = currentTime.format(new Date())
				+ " Distance vector list is:";

		// Handle case where routing table is empty
		if (routingTable.isEmpty()) {
			return currentTime.format(new Date()) + " There's nothing here! :(";
		}
		
		// Add each vector
		Iterator<Client> iter = routingTable.values().iterator();
		while (iter.hasNext()) {
			str += "\n" + iter.next().toString();
		}

		return str;
	}
}