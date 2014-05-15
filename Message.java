import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Message {
    public enum MessageType {
        ROUTE_UPDATE, TRANSFER;

        public byte[] createHeader(int fromPort) {
            return createHeader(name() + " " + fromPort);
        }

        public byte[] createHeader(int fromPort, int sequenceNumber,
                Client destination, String fileName) {
            return createHeader(name() + " " + fromPort + " " + sequenceNumber
                    + " " + destination.getIpAddressPortNumberString() + " "
                    + fileName);
        }

        private byte[] createHeader(String headerStr) {
            byte[] temp = headerStr.getBytes();

            byte[] header = new byte[HEADER_SIZE];

            for (int i = 0; i < temp.length; i++) {
                header[i] = temp[i];
            }

            return header;
        }
    }

    private static final int HEADER_SIZE = 2048; // bytes
    private static final int FOOTER_SIZE = 2048; // bytes

    private MessageType type;
    private Client fromClient;
    private HashMap<String, Double> routeUpdate;
    private FileChunk fileChunk;

    public Message(Client fromClient, HashMap<String, Double> routeUpdate) {
        this.type = MessageType.ROUTE_UPDATE;
        this.fromClient = fromClient;
        this.routeUpdate = routeUpdate;
    }

    public Message(Client fromClient, FileChunk fileChunk) {
        this.type = MessageType.TRANSFER;
        this.fromClient = fromClient;
        this.fileChunk = fileChunk;
    }

    public Message(DatagramPacket packet) {
        byte[] data = packet.getData();

        // Get the header
        byte[] header = new byte[HEADER_SIZE];
        for (int i = 0; i < header.length; i++) {
            header[i] = data[i];
        }

        // Parse the header
        int sequenceNumber = 0;
        Client destination = null;
        String fileName = "";
        String[] headerArr = (new String(header)).split(" ");
        try {
            type = MessageType.valueOf(headerArr[0]);
            int fromPortNumber = Integer.parseInt(headerArr[1].trim());
            fromClient = new Client(packet.getAddress(), fromPortNumber);
            if (type == MessageType.TRANSFER) {
                sequenceNumber = Integer.parseInt(headerArr[2]);
                destination = new Client(headerArr[3]);
                System.out.println("Destination is " + headerArr[3]);
                fileName = headerArr[4];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get the message
        byte[] message = new byte[data.length - HEADER_SIZE];
        for (int i = 0; i < message.length; i++) {
            message[i] = data[i + HEADER_SIZE];
        }

        // Parse the message
        switch (type) {
            case ROUTE_UPDATE:
                routeUpdate = new HashMap<String, Double>();
                String[] messageArr = (new String(message)).trim().split("\n");
                for (String messageStr : messageArr) {
                    if (messageStr.isEmpty()) {
                        continue;
                    }
                    String[] info = messageStr.split(" ");
                    String clientStr = info[0];
                    double cost = Double.parseDouble(info[1]);
                    routeUpdate.put(clientStr, cost);
                }
                break;
            case TRANSFER:
                // Extract the footer
                byte[] footer = new byte[FOOTER_SIZE];
                for (int i = message.length - FOOTER_SIZE; i < message.length; i++) {
                    message[i] = footer[i - message.length + FOOTER_SIZE];
                }
                String footerStr = new String(footer);

                byte[] temp = new byte[message.length - FOOTER_SIZE];
                for (int i = 0; i < message.length - FOOTER_SIZE; i++) {
                    temp[i] = message[i];
                }

                fileChunk = new FileChunk(fileName, temp, sequenceNumber,
                        footerStr);
                fileChunk.addClientTraversed(fromClient);
                fileChunk.setDestination(destination);
                break;
        }
    }

    public DatagramPacket encode(Client toClient, Client destination) {
        byte[] header = type == MessageType.ROUTE_UPDATE ? type
                .createHeader(fromClient.getPortNumber()) : type
                .createHeader(
                        fromClient.getPortNumber(),
                        fileChunk.getSequenceNumber(), destination,
                        fileChunk.getName());

        byte[] message = null;
        byte[] footer = new byte[0];
        switch (type) {
            case ROUTE_UPDATE:
                String messageStr = "";
                Iterator<Entry<String, Double>> iter = routeUpdate.entrySet()
                        .iterator();
                while (iter.hasNext()) {
                    Entry<String, Double> entry = iter.next();

                    // Make sure to not include the toClient in the route update
                    // message
                    if (entry.getKey().equals(
                            toClient.getIpAddressPortNumberString())) {
                        continue;
                    }

                    messageStr += "\n" + entry.getKey() + " "
                            + entry.getValue();
                }
                messageStr = messageStr.isEmpty() ? messageStr : messageStr
                        .substring(1);
                message = messageStr.getBytes();
                break;
            case TRANSFER:
                message = fileChunk.getChunk();

                footer = new byte[FOOTER_SIZE];
                byte[] footerShort = fileChunk.getClientsTraversed().getBytes();
                for (int i = 0; i < footerShort.length; i++) {
                    footer[i] = footerShort[i];
                }

                break;
        }

        // Concatenate header and message and footer
        byte[] data = new byte[header.length + message.length + footer.length];
        for (int i = 0; i < header.length; i++) {
            data[i] = header[i];
        }
        for (int i = 0; i < message.length; i++) {
            data[header.length + i] = message[i];
        }
        for (int i = 0; i < footer.length; i++) {
            data[header.length + message.length + i] = footer[i];
        }
        
        System.out.println("Sending to " + toClient.getIpAddress().getHostAddress() + " at " + toClient.getPortNumber());

        return new DatagramPacket(data, data.length, toClient.getIpAddress(),
                toClient.getPortNumber());
    }

    public MessageType getType() {
        return type;
    }

    public Client getFromClient() {
        return fromClient;
    }

    public HashMap<String, Double> getRouteUpdate() {
        return routeUpdate;
    }

    public FileChunk getFileChunk() {
        return fileChunk;
    }
}
