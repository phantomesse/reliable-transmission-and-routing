package old;
import java.util.Iterator;

public enum Message {
	ROUTE_UPDATE, LINKUP, LINKDOWN;

	/**
	 * Creates a route update message.
	 */
	public byte[] encodeRouteUpdate(RoutingTable routingTable) {
		if (this != ROUTE_UPDATE) {
			return null;
		}

		String message = "<start " + this.name() + ">";

		Iterator<RoutingTable.NeighborClient> iter = routingTable
				.getNeighbors().iterator();
		while (iter.hasNext()) {
			RoutingTable.NeighborClient client = iter.next();
			message += "\n" + client.getIPAddress() + " " + client.getPortNumber()
					+ " " + client.getLinkCost();
		}
		
		message += "\n<end " + this.name() + ">";
		
		return message.getBytes();
	}
	
	/**
	 * Decodes a route update message.
	 */
	public RoutingTable decodeRouteUpdate(byte[] messageBytes) {
		if (this != ROUTE_UPDATE) {
			return null;
		}
		
		RoutingTable routingTable = new RoutingTable();
		String[] message = (new String(messageBytes)).split("\n");
		for (int i = 1; i < message.length - 1; i++) {
			String[] line = message[i].split(" ");
			routingTable.add(line[0], Integer.parseInt(line[1]), Double.parseDouble(message[2]));
		}
		return routingTable;
	}

}
