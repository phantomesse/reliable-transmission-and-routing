package old;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReaderSocket extends Thread {
	public static int PACKET_BUFFER_LENGTH = 65507;

	private DatagramSocket socket;

	public UDPReaderSocket(int portNumber) {
		try {
			socket = new DatagramSocket(portNumber);
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
				byte[] data = packet.getData();
				String str = new String(data);
				System.out.println(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
