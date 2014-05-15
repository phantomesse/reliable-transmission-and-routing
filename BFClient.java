import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Main class of the program. Handles communication between the graphical user
 * interface, the sockets (both reading and writing), and the routing table.
 * 
 * @author Lauren Zou
 */
public class BFClient {

    public enum Command {
        LINKDOWN, LINKUP, SHOWRT, TRANSFER, CLOSE;
    }

    private int portNumber;
    private FileChunk fileChunk;

    private final WriteSocket writeSocket;
    private final ReadSocket readSocket;

    private final RoutingTable routingTable;

    private FileChunk[] myChunks;

    BFClientUI gui;

    public BFClient(int portNumber, int timeout, RoutingTable pRoutingTable,
            FileChunk fileChunk) {
        this.portNumber = portNumber;
        this.routingTable = pRoutingTable;
        this.fileChunk = fileChunk;

        myChunks = new FileChunk[2];
        if (this.fileChunk != null) {
            myChunks[this.fileChunk.getSequenceNumber() - 1] = this.fileChunk;
        }

        // Set up sockets
        writeSocket = new WriteSocket(this, timeout);
        readSocket = new ReadSocket(this);

        // Set up GUI
        gui = new BFClientUI(this);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.run();

                writeSocket.start();
                readSocket.start();
            }
        });

    }

    /**
     * Performs a link down without notifying peoples
     */
    public void linkdownNoSend(Client linkDownClient) {
        // Link down in our table
        routingTable.linkdown(linkDownClient);

        // Set any links that are through this client to INFINITY
        Iterator<Client> iter = routingTable.getClients().iterator();
        while (iter.hasNext()) {
            Client client = iter.next();
            if (client
                    .getLink()
                    .getIpAddressPortNumberString()
                    .equals(linkDownClient
                            .getIpAddressPortNumberString())) {

                // Set the cost
                client.setCost(Double.POSITIVE_INFINITY);
            }
        }
    }

    public String linkdown(Client linkDownClient) {
        // Check if the client we're trying to link down is our direct neighbor
        if (!routingTable.isDirectNeighbor(linkDownClient)) {
            return "Sorry, I can only link down direct neighbors!";
        }

        linkdownNoSend(linkDownClient);

        // Update the UI
        gui.updateRoutingTableUI(routingTable);

        // Tell the link down client that we're breaking up
        writeSocket.linkDown(linkDownClient);

        // Tell everyone that this link down happened
        writeSocket.sendRouteUpdate();

        return routingTable.get(linkDownClient).toString();
    }

    public void sendLinkDown(Client clientDown) {
        writeSocket.linkDown(clientDown);
    }

    public String linkup(Client linkUpClient, double cost) {
        // Check if the link we're trying to link up is currently down
        linkUpClient = routingTable.get(linkUpClient);
        if (linkUpClient.getCost() != Double.POSITIVE_INFINITY) {
            return "Sorry, link to "
                    + linkUpClient.getIpAddressPortNumberString()
                    + " is not down!";
        }

        // Check if the client we're trying to link up is our direct neighbor
        if (!routingTable.isDirectNeighbor(linkUpClient)) {
            return "Sorry, I can only link up direct neighbors!";
        }

        linkUpClient.setCost(cost);

        // Update the UI
        gui.updateRoutingTableUI(routingTable);

        // Tell the link up client that we're getting back together
        writeSocket.linkUp(linkUpClient, cost);

        // Tell everyone that this link up happened
        writeSocket.sendRouteUpdate();

        return routingTable.get(linkUpClient).toString();
    }

    public String transfer(Client destination) {
        // Check if we have a file chunk to send
        if (fileChunk == null) {
            return "Sorry, I don't have a file chunk to transfer!";
        }

        // Check if we have a path that does not cost infinity to this
        // destination
        if (routingTable.get(destination).getCost() == Double.POSITIVE_INFINITY) {
            return "Sorry, I don't have a finite path to "
                    + destination.getIpAddressPortNumberString() + "!";
        }

        // Get link to destination
        Client link = routingTable.get(destination).getLink();

        writeSocket.transferChunk(fileChunk, link, destination);

        return fileChunk.getName() + " chunk " + fileChunk.getSequenceNumber()
                + " transferred to next hop "
                + link.getIpAddressPortNumberString();
    }

    /**
     * Handle a transfer from the read socket
     */
    public void transfer(FileChunk chunk) {
        // Get the destination
        Client destination = chunk.getDestination();

        // Get link to destination
        Client link = routingTable.get(destination).getLink();

        writeSocket.transferChunk(chunk, link, destination);

        // Print to console
        String message = chunk.getName() + " chunk "
                + chunk.getSequenceNumber()
                + " transferred to next hop "
                + link.getIpAddressPortNumberString();
        gui.getConsole().add(message + "\n");
    }

    /**
     * Processes a chunk received by the read socket.
     */
    public void processChunk(FileChunk chunk) {
        gui.getConsole().add(
                chunk.getName() + " was received at "
                        + RoutingTable.FORMAT.format(new Date())
                        + ". Here was the path:\n"
                        + chunk.getClientsTraversed());

        // Add chunk to my chunks
        myChunks[chunk.getSequenceNumber() - 1] = chunk;

        // Check if the chunk array is complete
        boolean completeFile = true;
        for (FileChunk chunkArrayChunk : myChunks) {
            if (chunkArrayChunk == null) {
                completeFile = false;
            }
        }

        if (completeFile) {
            // Create a file out of the two chunks
            byte[] chunk1 = myChunks[0].getChunk();
            byte[] chunk2 = myChunks[1].getChunk();
            byte[] file = new byte[chunk1.length + chunk2.length];
            for (int i = 0; i < chunk1.length; i++) {
                file[i] = chunk1[i];
            }
            for (int i = 0; i < chunk2.length; i++) {
                file[i + chunk1.length] = chunk2[i];
            }

            // Create the file
            BufferedOutputStream output = null;
            try {
                try {
                    output = new BufferedOutputStream(new FileOutputStream(
                            "output"));
                    output.write(file);
                } finally {
                    output.close();
                }

                gui.getConsole().add("Saved file to output.\n");
            } catch (IOException e) {
                System.out.println("Could not save output.");
            }
        }

        // Update the file chunk UI
        gui.updateFileChunkUI(myChunks);
    }

    /**
     * Shuts down the client
     */
    public void close() {
        writeSocket.close();
        readSocket.close();
        gui.close();
        System.exit(0);
    }

    public void updateRoutingTableUI() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                return null;
            }

            @Override
            protected void done() {
                gui.updateRoutingTableUI(routingTable);
            }
        };

        worker.execute();
    }

    public void sendRouteUpdate() {
        writeSocket.sendRouteUpdate();
    }

    public int getPortNumber() {
        return portNumber;
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public FileChunk getFileChunk() {
        return fileChunk;
    }

    public FileChunk[] getMyChunks() {
        return myChunks;
    }

    public BFClientUI getGUI() {
        return gui;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            die("usage: BFClient <config file>");
        }

        parseConfigFile(args[0]);
    }

    /**
     * Parses a configuration file into a <code>BFClient</code>.
     */
    private static BFClient parseConfigFile(String configFilePath) {
        try {
            Scanner scanner = new Scanner(new File(configFilePath));

            // Configure this client
            String[] line = scanner.nextLine().split(" ");
            int portNumber = Integer.parseInt(line[0]);
            int timeout = Integer.parseInt(line[1]); // specified in seconds

            FileChunk fileChunk = null;
            if (line.length > 2) {
                String fileChunkToTransfer = line[2];
                int fileSequenceNumber = Integer.parseInt(line[3]);
                InputStream inputStream = null;
                try {
                    // Read in file chunk to transfer
                    File file = new File(fileChunkToTransfer);
                    byte[] chunk = new byte[(int) file.length()];
                    inputStream = new BufferedInputStream(new FileInputStream(
                            file));
                    int totalBytesRead = 0;
                    while (totalBytesRead < chunk.length) {
                        int bytesRemaining = chunk.length - totalBytesRead;
                        int bytesRead = inputStream.read(chunk, totalBytesRead,
                                bytesRemaining);
                        if (bytesRead > 0) {
                            totalBytesRead = totalBytesRead + bytesRead;
                        }
                    }
                    fileChunk = new FileChunk(fileChunkToTransfer, chunk,
                            fileSequenceNumber);
                } catch (FileNotFoundException e) {
                    die("could not read " + fileChunkToTransfer);
                } finally {
                    inputStream.close();
                }
            }

            // Build the routing table
            RoutingTable routingTable = new RoutingTable();
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().split(" ");
                Client client = new Client(line[0]);
                double cost = Double.parseDouble(line[1]);
                routingTable.update(client, client, cost);
            }

            return new BFClient(portNumber, timeout, routingTable, fileChunk);
        } catch (FileNotFoundException e) {
            die("could not read " + configFilePath);
            return null;
        } catch (Exception e) {
            die("something went wrong while parsing " + configFilePath);
            return null;
        }
    }

    private static void die(String message) {
        System.err.println(message);
        System.exit(1);
    }
}