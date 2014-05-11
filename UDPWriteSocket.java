package old2;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

public class UDPWriteSocket extends Thread {
	private BFClient client;
	private DatagramSocket socket;

	private int timeout;

	public UDPWriteSocket(BFClient client, int timeout) {
		this.client = client;
		this.timeout = timeout;

		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void sendLinkDown(Client toClient) {
		DatagramPacket packet = Message.encodeLinkdown(client.getPortNumber());
		packet.setAddress(toClient.getIpAddress());
		packet.setPort(toClient.getPortNumber());
		
		try {
			socket.send(packet);
		} catch (IOException e) {
			// Ignore if packet failed
		}
		
		// Unblock the routing table
		client.getRoutingTable().setBlockAdding(false);
	}
	
	public void sendLinkUp(Client toClient, double cost) {
		DatagramPacket packet = Message.encodeLinkup(client.getPortNumber(), cost);
		packet.setAddress(toClient.getIpAddress());
		packet.setPort(toClient.getPortNumber());
		
		try {
			socket.send(packet);
		} catch (IOException e) {
			// Ignore if packet failed
		}
		
		// Unblock the routing table
		client.getRoutingTable().setBlockAdding(false);
	}

	public void sendRouteUpdate() {
		DatagramPacket packet = Message.encodeRoutingUpdate(
				client.getPortNumber(), client.getRoutingTable());

		ArrayList<String> killList = new ArrayList<String>();

		Collection<Client> clients = client.getRoutingTable().getClients();
		for (Client client : clients) {
			// Check if client should be dead by now
			long lastHeardFrom = client.getLastHeardFrom();
			long now = System.currentTimeMillis();
			long difference = (now - lastHeardFrom) / 1000;
			if (difference > timeout * 3) {
				// Add client to kill list
				killList.add(Client.getIpAddressPortNumberString(client));
			} else {
				// Send the route update to this client
				packet.setAddress(client.getIpAddress());
				packet.setPort(client.getPortNumber());
				try {
					socket.send(packet);
				} catch (IOException e) {
					// Ignore if packet failed
				}
			}
		}

		// Kill all the clients in the kill list
		client.getRoutingTable().kill(killList);
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
