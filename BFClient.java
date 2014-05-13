import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BFClient {
    private enum Command {
        LINKDOWN, LINKUP, SHOWRT, CLOSE, TRANSFER;
    }

    private int portNumber;
    private int timeout;

    private String fileChunkToTransfer;
    private int fileSequenceNumber;

    private WriteSocket writeSocket;
    private ReadSocket readSocket;

    private RoutingTable routingTable;

    private BFClientUI gui;

    public BFClient(String configFilePath) {
        // Parse through config file
        parseConfigFile(configFilePath);

        // Instantiate sockets
        writeSocket = new WriteSocket(this, timeout);
        readSocket = new ReadSocket(this);
        writeSocket.start();
        readSocket.start();

        gui = new BFClientUI(this);

//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
                gui.createAndRunGUI();
//            }
//        });
    }

    /**
     * Parses through a config file to instantiate various elements for
     * {@link BFClient}.
     */
    private void parseConfigFile(String configFilePath) {
        try {
            Scanner scanner = new Scanner(new File(configFilePath));

            // Configure this client
            String[] line = scanner.nextLine().split(" ");
            portNumber = Integer.parseInt(line[0]);
            timeout = Integer.parseInt(line[1]); // specified in seconds
            if (line.length > 2) {
                fileChunkToTransfer = line[2];
                fileSequenceNumber = Integer.parseInt(line[3]);
            }

            // Build the routing table
            routingTable = new RoutingTable();
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().split(" ");
                Client client = new Client(line[0]);
                double cost = Double.parseDouble(line[1]);
                routingTable.update(client, client, cost);
            }
        } catch (FileNotFoundException e) {
            die("could not read " + configFilePath);
        } catch (Exception e) {
            die("something went wrong while parsing " + configFilePath);
        }
    }

    /**
     * Executes a command. Responds with the appropriate response string.
     */
    public String executeCommand(String commandStr) {
        String[] command = commandStr.split(" ");

        // Determine the type of command
        Command commandType = null;
        try {
            commandType = Command.valueOf(command[0].toUpperCase());
        } catch (Exception e) {
            return "Sorry, we did not understand your command. Please try again!";
        }

        switch (commandType) {
            case LINKDOWN:
                if (command.length < 3) {
                    return "usage: LINKDOWN <ip address> <port number>";
                }
                try {
                    InetAddress ipAddress = InetAddress.getByName(command[1]);
                    int portNumber = Integer.parseInt(command[2]);
                    return linkDown(ipAddress, portNumber);
                } catch (UnknownHostException e) {
                    return "Invalid IP address.";
                } catch (NumberFormatException e) {
                    return "Invalid port number.";
                } catch (Exception e) {
                    e.printStackTrace(); // TODO: remove this
                    return "Something went wrong!";
                }
            case LINKUP:
                return linkUp();
            case SHOWRT:
                return showRoutingTable();
            case CLOSE:
                close();
                break;
            case TRANSFER:
                return transfer();
        }

        return "";
    }

    private String linkDown(InetAddress ipAddress, int portNumber) {
        // Link down on our routing table
        routingTable.linkDown(new Client(ipAddress, portNumber));
        
        // Send route update
        writeSocket.sendRouteUpdate();
        
        return routingTable.get(new Client(ipAddress, portNumber)).toString();
    }

    private String linkUp() {
        // TODO
        return "Command not implemented.";
    }

    private String showRoutingTable() {
        return routingTable.toString();
    }

    public void close() {
        writeSocket.interrupt();
        readSocket.interrupt();
        System.exit(0);
    }

    private String transfer() {
        // TODO
        return "Command not implemented.";
    }

    /**
     * Updates the routing table based on incoming {@link Message}.
     */
    public void updateRoutingTable(Message message) {
        // Touch the client that sent this message
        Client fromClient = message.getFromClient();
        routingTable.touch(fromClient);

        String messageStr = message.getMessage().trim();
        if (messageStr.isEmpty()) {
            gui.updateRoutingTable();
            return; // Nothing to do here
        }
        
        // Parse through message string
        boolean routingTableChanged = false;
        String[] messageArr = messageStr.split("\n");
        for (String vectorStr : messageArr) {
            if (vectorStr.trim().isEmpty()) {
                continue;
            }
            String[] vector = vectorStr.split(" ");
            try {
                Client destinationClient = new Client(vector[0]);
                double cost = Double.parseDouble(vector[1]);
                boolean changed = routingTable.update(destinationClient, fromClient, cost);
                routingTableChanged = changed? true : routingTableChanged;
            } catch (UnknownHostException e) {
                // Something went wrong
            }
        }
        gui.updateRoutingTable();
        
        // Send route update if routing table changed
        if (routingTableChanged) {
            writeSocket.sendRouteUpdate();
        }
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public int getPortNumber() {
        return portNumber;
    }

    private static void die(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            die("usage: BFClient <config file>");
        }

        new BFClient(args[0]);

    }
}
