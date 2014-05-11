package old2;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
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

	private RoutingTable routingTable;
	private UDPWriteSocket writeSocket;
	private UDPReadSocket readSocket;

	public BFClient(String configFilePath) {
		// Parse through config file
		parseConfigFile(configFilePath);

		// Instantiate sockets
		writeSocket = new UDPWriteSocket(this, timeout);
		readSocket = new UDPReadSocket(this);
		writeSocket.start();
		readSocket.start();

		// Listen for user commands
		Scanner in = new Scanner(System.in);
		System.out.print("Command: ");
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(" ");
			try {
				Command command = Command.valueOf(line[0].toUpperCase());
				switch (command) {
				case LINKDOWN:
					if (line.length < 3) {
						System.out
								.println("usage: LINKDOWN <ip address> <port number>");
						break;
					}
					try {
						InetAddress ipAddress = InetAddress.getByName(line[1]);
						int portNumber = Integer.parseInt(line[2]);
						linkDown(ipAddress, portNumber);
					} catch (UnknownHostException e) {
						System.out.println("Invalid IP address.");
						break;
					} catch (NumberFormatException e) {
						System.out.println("Invalid port number.");
					} catch (Exception e) {
						System.out
								.println("Something went wrong! LINKDOWN was not executed.");
						e.printStackTrace();
					}
					break;
				case LINKUP:
					if (line.length < 4) {
						System.out
								.println("usage: LINKUP <ip address> <port number> <weight>");
						break;
					}
					try {
						Client client = Client
								.parseIpAddressPortNumberString(line[1] + ":"
										+ line[2]);
						double cost = Double.parseDouble(line[3]);
						linkUp(client.getIpAddress(), client.getPortNumber(),
								cost);
					} catch (UnknownHostException e) {
						System.out
								.println("Invalid IP address / port number combination.");
						break;
					} catch (NumberFormatException e) {
						System.out.println("Invalid weight.");
					} catch (Exception e) {
						System.out
								.println("Something went wrong! LINKUP was not executed.");
					}
					break;
				case SHOWRT:
					showRoutingTable();
					break;
				case CLOSE:
					System.out.println("Goodbye!");
					writeSocket.interrupt();
					readSocket.interrupt();
					System.exit(0);
				case TRANSFER:
					if (line.length < 3) {
						System.out
								.println("usage: TRANSFER <ip address> <port number>");
						break;
					}
					try {
						InetAddress ipAddress = InetAddress.getByName(line[1]);
						int portNumber = Integer.parseInt(line[2]);
						transfer(ipAddress, portNumber);
					} catch (UnknownHostException e) {
						System.out.println("Invalid IP address.");
						break;
					} catch (NumberFormatException e) {
						System.out.println("Invalid port number.");
					} catch (Exception e) {
						System.out
								.println("Something went wrong! TRANSFER was not executed.");
					}
					break;
				}
			} catch (IllegalArgumentException e) {
				System.out
						.println("Sorry, we did not understand your command. Please try again!");
			}

			System.out.print("\nCommand: ");
		}
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
				Client client = Client.parseIpAddressPortNumberString(line[0]);
				double cost = Double.parseDouble(line[1]);
				routingTable.add(client.getIpAddress(), client.getPortNumber(),
						cost);
			}
		} catch (FileNotFoundException e) {
			die("could not read " + configFilePath);
		} catch (Exception e) {
			die("something went wrong while parsing " + configFilePath);
		}
	}

	/**
	 * Removes a link from the routing table and sends an update to tell
	 * everyone else to do so.
	 */
	private void linkDown(InetAddress ipAddress, int portNumber) {
		routingTable.setBlockAdding(true);
		routingTable.linkDown(ipAddress, portNumber);
		System.out.println(routingTable.get(ipAddress, portNumber));
		writeSocket.sendLinkDown(new Client(ipAddress, portNumber));
	}

	/**
	 * Removes a link from the routing table based on a message from the read
	 * socket.
	 */
	public void linkDown(Message message) {
		routingTable.setBlockAdding(true);
		routingTable.linkDown(message.getFromClient().getIpAddress(), message
				.getFromClient().getPortNumber());
		routingTable.setBlockAdding(false);
	}

	/**
	 * Restores a link to the mentioned neighbor with the given weight.
	 */
	private void linkUp(InetAddress ipAddress, int portNumber, double cost) {
		routingTable.setBlockAdding(true);
		// Check if the link was down in the first place
		Client client = routingTable.get(ipAddress, portNumber);
		if (client.getCost() != Double.POSITIVE_INFINITY) {
			System.out.println(Client.getIpAddressPortNumberString(client)
					+ " was not down in the first place!");
			return;
		}

		routingTable.add(ipAddress, portNumber, cost);
		System.out.println(routingTable.get(ipAddress, portNumber));
		writeSocket.sendLinkUp(new Client(ipAddress, portNumber), cost);
	}

	public void linkUp(Message message) {
		routingTable.setBlockAdding(true);
		routingTable.add(message.getFromClient().getIpAddress(), message
				.getFromClient().getPortNumber(), Double.parseDouble(message
				.getMessage().trim()));
		routingTable.setBlockAdding(false);
	}

	/**
	 * Prints the routing table to the screen.
	 */
	private void showRoutingTable() {
		System.out.println(routingTable.toString());
	}

	private void transfer(InetAddress ipAddress, int portNumber) {
		// TODO
	}

	public RoutingTable getRoutingTable() {
		return routingTable;
	}

	public int getPortNumber() {
		return portNumber;
	}

	/**
	 * Updates the routing table when a packet is received.
	 */
	public void updateRoutingTable(Message message) {
		boolean routingTableChanged = false;

		String[] vectorLines = message.getMessage().split("\n");
		for (String vectorLine : vectorLines) {
			String[] line = vectorLine.split(" ");
			try {
				// Get ip address, port, and cost
				Client client = Client.parseIpAddressPortNumberString(line[0]);
				double cost = Double.parseDouble(line[1]);

				// Add to routing table
				boolean changed = routingTable.add(client.getIpAddress(),
						client.getPortNumber(), cost, message.getFromClient()
								.getIpAddress(), message.getFromClient()
								.getPortNumber());
				if (!routingTableChanged) {
					routingTableChanged = changed;
				}
			} catch (UnknownHostException e) {
				// Something went wrong
			}
		}

		if (routingTableChanged) {
			// Send out route update
			writeSocket.sendRouteUpdate();
		}
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
