package old;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;

public class UDPWriterSocket extends Thread {
	private int timeout; // seconds
	private DatagramSocket socket;
	private RoutingTable routingTable;

	public UDPWriterSocket(int timeout, RoutingTable routingTable) {
		this.routingTable = routingTable;
		this.timeout = timeout;

		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message to everyone.
	 */
	private void shout(String message) {
		Iterator<RoutingTable.NeighborClient> iter = routingTable
				.getNeighbors().iterator();
		while (iter.hasNext()) {
			RoutingTable.NeighborClient client = iter.next();

			try {
				DatagramPacket packet = new DatagramPacket(message.getBytes(),
						message.getBytes().length, InetAddress.getByName(client
								.getIPAddress()), client.getPortNumber());
				socket.send(packet);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Sends a copy of the routing table to everyone.
	 */
	private void sendRouteUpdate() {
		
	}

	@Override
	public void run() {
		shout("Hello world!");

		while (true) {
			try {
				Thread.sleep(timeout * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
