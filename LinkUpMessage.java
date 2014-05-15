import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class LinkUpMessage extends Message {
    private class Header {
        private int fromClientPortNumber;

        public Header(int fromClientPortNumber) {
            this.fromClientPortNumber = fromClientPortNumber;
        }

        public Header(String headerStr) {
            fromClientPortNumber = Integer.parseInt(headerStr.split(" ")[1]);
        }

        public int getFromClientPortNumber() {
            return fromClientPortNumber;
        }

        @Override
        public String toString() {
            return Message.MessageType.LINKUP.name() + " "
                    + fromClientPortNumber;
        }
    }

    private Client clientUp;
    private double cost;

    public LinkUpMessage(Client fromClient, Client clientUp, double cost) {
        super(Message.MessageType.LINKUP, fromClient);
        this.clientUp = clientUp;
        this.cost = cost;
    }

    public LinkUpMessage(DatagramPacket packet) {
        super(Message.MessageType.LINKUP, packet);

        // Parse header
        Header header = new Header(headerStr);
        fromClient = new Client(fromClient.getIpAddress(),
                header.getFromClientPortNumber());

        // Parse message
        String[] messageArr = (new String(message)).trim().split(" ");
        try {
            clientUp = new Client(messageArr[0].trim());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        cost = Double.parseDouble(messageArr[1].trim());
    }
    
    public DatagramPacket encode(Client toClient) {
        // Create header
        Header header = new Header(fromClient.getPortNumber());
        
        // Create message
        String messageStr = clientUp.getIpAddressPortNumberString() + " " + cost;
        message = messageStr.getBytes();
        
        return encode(toClient, header.toString(), message);
    }
    
    public Client getClientUp() {
        return clientUp;
    }
    
    public double getCost() {
        return cost;
    }
}
