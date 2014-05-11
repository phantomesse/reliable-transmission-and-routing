package old2;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message {
	private MessageType messageType;
	private Client fromClient;
	private String message;

	public Message(MessageType messageType, InetAddress fromIpAddress,
			int portNumber, String message) {
		this.messageType = messageType;
		this.fromClient = new Client(fromIpAddress, portNumber);
		this.message = message;
	}

	/**
	 * Parses a {@link DatagramPacket} into a {@link Message}. Returns null if
	 * the packet is not valid or is corrupted.
	 */
	public static Message processPacket(DatagramPacket packet) {
		// Convert the packet data to
		String messageStr = (new String(packet.getData())).trim();

		// Determine the type of message
		MessageType messageType = MessageType.determineMessageType(messageStr);

		// Strip away the header and the footer
		messageStr = messageStr.substring(
				messageStr.indexOf(messageType.getHeader())
						+ messageType.getHeader().length(),
				messageStr.indexOf(messageType.getFooter().trim()));

		// Get port number from the first line of the message
		int portNumber = packet.getPort();
		if (messageType != MessageType.TRANSFER) {
			portNumber = Integer.parseInt(messageStr.split("\n")[0]);
			messageStr = messageStr.substring(messageStr.indexOf("\n") + 1);
		}

		// Create a Message object
		return new Message(messageType, packet.getAddress(), portNumber,
				messageStr);
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public Client getFromClient() {
		return fromClient;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Helper class for encoding a {@link DatagramPacket} to send.
	 */
	private static DatagramPacket encodeMessage(MessageType messageType,
			int portNumber, String message) {
		message = messageType.getHeader() + portNumber + "\n" + message
				+ messageType.getFooter();
		return new DatagramPacket(message.getBytes(), message.getBytes().length);
	}

	/**
	 * Encodes a LINKDOWN message
	 */
	public static DatagramPacket encodeLinkdown(int portNumber) {
		return encodeMessage(MessageType.LINKDOWN, portNumber, "");
	}

	/**
	 * Encodes a LINKDOWN message
	 */
	public static DatagramPacket encodeLinkup(int portNumber, double cost) {
		return encodeMessage(MessageType.LINKUP, portNumber, "" + cost);
	}

	/**
	 * Encodes the vectors of a routing table into a {@link DatagramPacket} to
	 * be sent.
	 */
	public static DatagramPacket encodeRoutingUpdate(int portNumber,
			RoutingTable routingTable) {
		return encodeMessage(MessageType.ROUTING_UPDATE, portNumber,
				routingTable.getRouteUpdateMessage());
	}
}
