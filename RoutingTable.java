package old;
import java.util.HashSet;
import java.util.Set;


public class RoutingTable {
	public class NeighborClient {
		private String ipAddress;
		private int portNumber;
		private double linkCost;
		
		public NeighborClient(String ipAddress, int portNumber, double linkCost) {
			this.ipAddress = ipAddress;
			this.portNumber = portNumber;
			this.linkCost = linkCost;
		}
		
		public String getIPAddress() {
			return ipAddress;
		}
		
		public int getPortNumber() {
			return portNumber;
		}
		
		public double getLinkCost() {
			return linkCost;
		}
	}
	
	private Set<NeighborClient> neighbors;
	
	public RoutingTable() {
		neighbors = new HashSet<NeighborClient>();
	}
	
	public void add(String ipAddress, int portNumber, double linkCost) {
		neighbors.add(new NeighborClient(ipAddress, portNumber, linkCost));
	}
	
	public Set<NeighborClient> getNeighbors() {
		return neighbors;
	}
}
