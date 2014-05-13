import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReadSocket extends Thread {
    public static int PACKET_BUFFER_LENGTH = 65507;
    private BFClient bfclient;
    private DatagramSocket socket;

    public ReadSocket(BFClient bfclient) {
        this.bfclient = bfclient;

        try {
            socket = new DatagramSocket(bfclient.getPortNumber());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        byte[] buffer = new byte[PACKET_BUFFER_LENGTH];

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                Message message = new Message(packet);
                switch(message.getMessageType()) {
                    case ROUTE_UPDATE:
                        bfclient.updateRoutingTable(message);
                        break;
                    case TRANSFER:
                        // TODO
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
