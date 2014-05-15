import java.util.Date;
import java.util.LinkedList;

/**
 * Data structure for a file chunk that is transmitted between clients.
 * 
 * @author Lauren Zou
 */
public class FileChunk {
    private String name;
    private byte[] chunk;
    private int sequenceNumber;
    private Client destination;

    /*
     * Contains a list of clients in the form
     * "<ip address>:<port number> at <timestamp>"
     */
    private LinkedList<String> clientsTraversed;

    public FileChunk(String name, byte[] chunk, int sequenceNumber) {
        this.name = name;
        this.chunk = chunk;
        this.sequenceNumber = sequenceNumber;

        clientsTraversed = new LinkedList<String>();
    }

    public FileChunk(String name, byte[] chunk, int sequenceNumber,
            String clientsTraversedStr) {
        this(name, chunk, sequenceNumber);

        String[] traversedArr = clientsTraversedStr.trim().split("\n");
        for (String traversed : traversedArr) {
            clientsTraversed.add(traversed);
        }
    }

    public byte[] getChunk() {
        return chunk;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getName() {
        return name;
    }

    public void setDestination(Client client) {
        this.destination = client;
    }

    public Client getDestination() {
        return destination;
    }

    public void addClientTraversed(Client client) {
        String str = client.getIpAddressPortNumberString() + " at "
                + RoutingTable.FORMAT.format(new Date());
        clientsTraversed.add(str);
    }

    public String getClientsTraversed() {
        String str = "";
        for (String clientTraversed : clientsTraversed) {
            str += clientTraversed + "\n";
        }
        return str.trim();
    }
}
