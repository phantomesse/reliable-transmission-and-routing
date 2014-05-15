import java.net.DatagramPacket;

public abstract class Message {
    public enum MessageType {
        ROUTE_UPDATE, TRANSFER, LINKDOWN, LINKUP;
    }
    
    private static final int HEADER_SIZE = 512; // bytes
    private MessageType type;
    protected Client fromClient;
    protected String headerStr;
    protected byte[] message;
    
    public Message(MessageType type, Client fromClient) {
        this.type = type;
        this.fromClient = fromClient;
    }

    public Message(MessageType type, DatagramPacket packet) {
        this.type = type;
        fromClient = new Client(packet.getAddress(), 0);
        byte[] data = packet.getData();

        // Get the header from the data
        byte[] header = new byte[HEADER_SIZE];
        for (int i = 0; i < header.length; i++) {
            header[i] = data[i];
        }
        headerStr = (new String(header)).trim();
        
        // Get the message from the data
        message = new byte[packet.getLength() - HEADER_SIZE];
        for (int i = 0; i < message.length; i++) {
            message[i] = data[i + HEADER_SIZE];
        }
    }
    
    /**
     * Decodes a {@link DatagramPacket}. Returns the appropriate message class.
     */
    public static Message decode(DatagramPacket packet) {
        byte[] header = new byte[HEADER_SIZE];
        byte[] data = packet.getData();
        
        for (int i = 0; i < header.length; i++) {
            header[i] = data[i];
        }
        
        MessageType type = MessageType.valueOf((new String(header)).split(" ")[0]);
        switch(type) {
            case ROUTE_UPDATE:
                return new RouteUpdateMessage(packet);
            case TRANSFER:
                return new TransferMessage(packet);
            case LINKDOWN:
                return new LinkDownMessage(packet);
            case LINKUP:
                return new LinkUpMessage(packet);
        }
        
        return null;
    }

    protected DatagramPacket encode(Client toClient, String headerStr,
            byte[] message) {
        byte[] data = new byte[HEADER_SIZE + message.length];

        // Put header into the data array
        byte[] header = headerStr.getBytes();
        for (int i = 0; i < header.length; i++) {
            data[i] = header[i];
        }

        // Put message into the data array
        for (int i = 0; i < message.length; i++) {
            data[HEADER_SIZE + i] = message[i];
        }

        return new DatagramPacket(data, data.length, toClient.getIpAddress(),
                toClient.getPortNumber());
    }

    public Client getFromClient() {
        return fromClient;
    }
    
    public MessageType getType() {
        return type;
    }
}
