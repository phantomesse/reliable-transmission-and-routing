import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class LinkDownMessage extends Message {
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
            return Message.MessageType.LINKDOWN.name() + " " + fromClientPortNumber;
        }
    }
    
    private Client clientDown;
    
    public LinkDownMessage(Client fromClient, Client clientDown) {
        super(Message.MessageType.LINKDOWN, fromClient);
        this.clientDown = clientDown;
    }
    
    public LinkDownMessage(DatagramPacket packet) {
        super(Message.MessageType.LINKDOWN, packet);
        
        // Parse header
        Header header = new Header(headerStr);
        fromClient = new Client(fromClient.getIpAddress(), header.getFromClientPortNumber());
        
        // Parse message
        try {
            clientDown = new Client((new String(message)).trim());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    public DatagramPacket encode(Client toClient) {
        // Create header
        Header header = new Header(fromClient.getPortNumber());
        
        // Create message
        String messageStr = clientDown.getIpAddressPortNumberString();
        message = messageStr.getBytes();
        
        return encode(toClient, header.toString(), message);
    }
    
    public Client getClientDown() {
        return clientDown;
    }
}