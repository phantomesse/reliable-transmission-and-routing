package old2;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {
	private InetAddress ipAddress;
	private int portNumber;
	private double cost;
	private Client link;
	private long lastHeardFrom; // Time stamp of the last time we heard from

	// this client

	public Client(InetAddress ipAddress, int portNumber) {
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
		this.cost = Double.POSITIVE_INFINITY;
		this.lastHeardFrom = System.currentTimeMillis();
	}

	public Client(InetAddress ipAddress, int portNumber, double cost) {
		this(ipAddress, portNumber);
		this.cost = cost;
	}

	public Client(InetAddress ipAddress, int portNumber, double cost,
			InetAddress linkIpAddress, int linkPortNumber) {
		this(ipAddress, portNumber, cost);
		this.link = new Client(linkIpAddress, linkPortNumber);
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Client getLink() {
		return link;
	}

	public void setLink(InetAddress linkIpAddress, int linkPortNumber) {
		this.link = new Client(linkIpAddress, linkPortNumber);
	}

	public long getLastHeardFrom() {
		return lastHeardFrom;
	}

	public void updateLastHeardFrom() {
		lastHeardFrom = System.currentTimeMillis();
	}

	/**
	 * Converts an {@link InetAddress} ip address and a port number into a
	 * string in the form <ip address>:<port number>.
	 */
	public static String getIpAddressPortNumberString(InetAddress ipAddress,
			int portNumber) {
		return ipAddress.getHostAddress() + ":" + portNumber;
	}

	/**
	 * Converts a {@link Client} with an ip address and a port number into a
	 * string in the form <ip address>:<port number>.
	 */
	public static String getIpAddressPortNumberString(Client client) {
		return client.getIpAddress().getHostAddress() + ":"
				+ client.getPortNumber();
	}

	/**
	 * Converts a string in the form <ip address>:<port number> into a
	 * {@link Client} with the respective ip address and port number.
	 */
	public static Client parseIpAddressPortNumberString(
			String ipAddressPortNumberString) throws UnknownHostException {
		String[] str = ipAddressPortNumberString.split(":");
		InetAddress ipAddress = InetAddress.getByName(str[0]);
		int portNumber = Integer.parseInt(str[1]);
		return new Client(ipAddress, portNumber);
	}

	@Override
	public String toString() {
		return String.format("Destination = %-20s Cost = %-5s Link = (%-20s",getIpAddressPortNumberString(ipAddress, portNumber) + ",", cost + ",", getIpAddressPortNumberString(link) + ")");
	}
}