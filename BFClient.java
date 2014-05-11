package old;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class BFClient {
	private int portNumber;

	private String fileChunkToTransfer;
	private int fileSequenceNumber;

	private RoutingTable routingTable;
	private UDPWriterSocket writerSocket;
	private UDPReaderSocket readerSocket;

	public BFClient(String configFilePath) {
		// Parse the config file into the client
		try {
			Scanner fileReader = new Scanner(new File(configFilePath));

			// Read in the client info
			String[] line = fileReader.nextLine().split(" ");
			this.portNumber = Integer.parseInt(line[0]);
			int timeout = Integer.parseInt(line[1]);
			readerSocket = new UDPReaderSocket(this.portNumber);
			if (line.length > 2) {
				this.fileChunkToTransfer = line[2];
				this.fileSequenceNumber = Integer.parseInt(line[3]);
			}

			// Read in the client's neighbors
			routingTable = new RoutingTable();
			while (fileReader.hasNextLine()) {
				line = fileReader.nextLine().split(" ");
				String[] ipAddressPort = line[0].split(":");
				routingTable.add(ipAddressPort[0],
						Integer.parseInt(ipAddressPort[1]),
						Double.parseDouble(line[1]));
			}
			writerSocket = new UDPWriterSocket(timeout, routingTable);
			
			// Start the sockets
			writerSocket.start();
			readerSocket.start();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find " + configFilePath);
			return;
		}
	}

	/**
	 * Destroys an existing link.
	 * 
	 * @param ipAddress
	 * @param portNumber
	 */
	public void linkDown(String ipAddress, int portNumber) {

	}
	
	/**
	 * Adds a link.
	 * 
	 * @param ipAddress
	 * @param portNumber
	 * @param linkCost
	 */
	public void linkUp(String ipAddress, int portNumber, double linkCost) {
		
	}
	
	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("usage: java BFClient <config file>");
			System.exit(0);
		}

		// Create the client using the config file
		BFClient client = new BFClient(args[0]);

		// Create the client UI
		new BFClientUI(client);
	}
}
