import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message {
    public enum MessageType {
        ROUTE_UPDATE, TRANSFER;
    }

    private MessageType messageType;
    private Client fromClient;
    private String message;

    public Message(MessageType messageType, InetAddress fromIpAddress,
            int portNumber, String message) {
        this.messageType = messageType;
        this.fromClient = new Client(fromIpAddress, portNumber);
        this.message = message;
    }

    public Message(DatagramPacket packet) {
        String messageStr = (new String(packet.getData())).trim();

        int fromPortNumber = packet.getPort();
        if (messageStr.startsWith(MessageType.ROUTE_UPDATE.name())) {
            this.messageType = MessageType.ROUTE_UPDATE;
            this.message = messageStr.substring(MessageType.ROUTE_UPDATE.name()
                    .length() + 1);
            
            int index = this.message.indexOf("\n");
            if (index < 0) {
                fromPortNumber = Integer.parseInt(this.message);
                this.message = "";
            } else {
                fromPortNumber = Integer.parseInt(this.message.substring(0,
                        this.message.indexOf("\n")));
                this.message = this.message
                        .substring(this.message.indexOf("\n") + 1);
            }
        } else {
            this.messageType = MessageType.TRANSFER;
            this.message = messageStr;
        }

        this.fromClient = new Client(packet.getAddress(), fromPortNumber);
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
     * Encodes a message into a {@link DatagramPacket} to send.
     */
    public DatagramPacket encode(Client toClient) {
        String message = this.message;
        if (messageType == MessageType.ROUTE_UPDATE) {
            message = MessageType.ROUTE_UPDATE.name() + "\n"
                    + fromClient.getPortNumber()
                    + "\n" + message;
        }

        return new DatagramPacket(message.getBytes(),
                message.getBytes().length, toClient.getIpAddress(),
                toClient.getPortNumber());
    }
}
