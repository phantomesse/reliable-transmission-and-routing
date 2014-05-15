import java.net.DatagramPacket;
import java.net.UnknownHostException;

public class TransferMessage extends Message {

    private class Header {
        private int fromClientPortNumber;
        private String fileName;
        private int sequenceNumber;
        private Client destination;
        private int pathStringLength;

        public Header(int fromClientPortNumber, String fileName,
                int sequenceNumber,
                Client destination, int pathStringLength) {
            this.fromClientPortNumber = fromClientPortNumber;
            this.fileName = fileName;
            this.sequenceNumber = sequenceNumber;
            this.destination = destination;
            this.pathStringLength = pathStringLength;
        }

        public Header(String headerStr) {
            String[] header = headerStr.split(" ");
            this.fromClientPortNumber = Integer.parseInt(header[1]);
            this.fileName = header[2];
            this.sequenceNumber = Integer.parseInt(header[3]);
            try {
                this.destination = new Client(header[4]);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            this.pathStringLength = Integer.parseInt(header[5]);
        }

        public int getFromClientPortNumber() {
            return fromClientPortNumber;
        }

        public String getFileName() {
            return fileName;
        }

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        public Client getDestination() {
            return destination;
        }

        public int getPathStringLength() {
            return pathStringLength;
        }

        @Override
        public String toString() {
            return Message.MessageType.TRANSFER.name() + " " + fromClientPortNumber + " " + fileName + " " + sequenceNumber
                    + " " + destination.getIpAddressPortNumberString() + " "
                    + pathStringLength;
        }
    }

    private FileChunk fileChunk;

    public TransferMessage(Client fromClient, FileChunk fileChunk) {
        super(Message.MessageType.TRANSFER, fromClient);
        this.fileChunk = fileChunk;
    }

    public TransferMessage(DatagramPacket packet) {
        super(Message.MessageType.TRANSFER, packet);

        // Parse header
        Header header = new Header(headerStr);
        fromClient = new Client(fromClient.getIpAddress(),
                header.getFromClientPortNumber());

        // Parse clients traversed
        byte[] clientsTraversedBytes = new byte[header.getPathStringLength()];
        for (int i = 0; i < clientsTraversedBytes.length; i++) {
            clientsTraversedBytes[i] = message[i];
        }
        String clientsTraversed = new String(clientsTraversedBytes);

        // Parse chunk
        byte[] chunk = new byte[message.length - clientsTraversedBytes.length];
        for (int i = 0; i < chunk.length; i++) {
            chunk[i] = message[i + clientsTraversedBytes.length];
        }

        // Create the file chunk
        fileChunk = new FileChunk(header.getFileName(), chunk,
                header.getSequenceNumber(), clientsTraversed);
        fileChunk.setDestination(header.getDestination());
        
        // Add to clients traversed
        fileChunk.addClientTraversed(fromClient);
    }

    public DatagramPacket encode(Client toClient) {
        // Create header
        Header header = new Header(fromClient.getPortNumber(),
                fileChunk.getName(), fileChunk.getSequenceNumber(),
                fileChunk.getDestination(), fileChunk.getClientsTraversed().getBytes().length);

        // Create message
        byte[] clientsTraversed = fileChunk.getClientsTraversed().getBytes();
        byte[] chunk = fileChunk.getChunk();
        message = new byte[clientsTraversed.length + chunk.length];
        for (int i = 0; i < clientsTraversed.length; i++) {
            message[i] = clientsTraversed[i];
        }
        for (int i = 0; i < chunk.length; i++) {
            message[i + clientsTraversed.length] = chunk[i];
        }

        return encode(toClient, header.toString(), message);
    }
    
    public FileChunk getFileChunk() {
        return fileChunk;
    }
}
