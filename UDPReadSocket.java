package old2;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReadSocket extends Thread {
	public static int PACKET_BUFFER_LENGTH = 65507;
	private BFClient client;
	private DatagramSocket socket;

	public UDPReadSocket(BFClient client) {
		this.client = client;

		try {
			socket = new DatagramSocket(client.getPortNumber());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		byte[] buffer = new byte[PACKET_BUFFER_LENGTH];

		while (true) {
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			try {
				socket.receive(packet);
				Message message = Message.processPacket(packet);
				switch (message.getMessageType()) {
				case LINKDOWN:
					client.linkDown(message);
					break;
				case LINKUP:
					client.linkUp(message);
					break;
				case ROUTING_UPDATE:
					client.updateRoutingTable(message);
					break;
				case TRANSFER:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
