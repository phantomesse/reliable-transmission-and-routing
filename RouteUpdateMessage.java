import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class RouteUpdateMessage extends Message {

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
            return Message.MessageType.ROUTE_UPDATE.name() + " "
                    + fromClientPortNumber;
        }
    }

    private HashMap<String, Double> routeUpdate;

    public RouteUpdateMessage(Client fromClient,
            HashMap<String, Double> routeUpdate) {
        super(Message.MessageType.ROUTE_UPDATE, fromClient);
        this.routeUpdate = routeUpdate;
    }

    public RouteUpdateMessage(DatagramPacket packet) {
        super(Message.MessageType.ROUTE_UPDATE, packet);

        // Parse header
        Header header = new Header(headerStr);
        fromClient = new Client(fromClient.getIpAddress(),
                header.getFromClientPortNumber());

        // Parse message
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
    }

    public DatagramPacket encode(Client toClient) {
        // Create header
        Header header = new Header(fromClient.getPortNumber());

        // Create message
        String messageStr = "";
        Iterator<Entry<String, Double>> iter = routeUpdate.entrySet()
                .iterator();
        while (iter.hasNext()) {
            Entry<String, Double> entry = iter.next();

            messageStr += "\n" + entry.getKey() + " " + entry.getValue();
        }
        messageStr = messageStr.isEmpty() ? messageStr : messageStr
                .substring(1);
        message = messageStr.getBytes();

        return encode(toClient, header.toString(), message);
    }

    public HashMap<String, Double> getRouteUpdate() {
        return routeUpdate;
    }
}
