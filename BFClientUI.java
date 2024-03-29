import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Graphical user interface for the {@link BFClient}.
 * 
 * @author Lauren Zou
 */
public class BFClientUI {
    private static final int WIDTH = 650;
    private static final int HEIGHT = 300;

    private BFClient bfclient;

    private JFrame frame;
    private RoutingTableUI routingTableUI;
    private CommandUI commandUI;
    private FileChunkUI fileChunkUI;
    private Console console;

    public BFClientUI(BFClient bfclient) {
        this.bfclient = bfclient;
    }

    public void run() {
        // Get bfclient info
        Client client = null;
        try {
            client = new Client(InetAddress.getLocalHost(),
                    bfclient.getPortNumber());
        } catch (UnknownHostException e1) {
            // Shouldn't get here
        }

        // Create and set up frame
        frame = new JFrame("BFClient " + client.getIpAddressPortNumberString());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Add the routing table
        routingTableUI = new RoutingTableUI(bfclient.getRoutingTable());
        panel.add(routingTableUI, BorderLayout.NORTH);

        // Add the console
        console = new Console();
        panel.add(console, BorderLayout.CENTER);

        // Add the file chunk UI
        JPanel bottomPanel = new JPanel(new BorderLayout());
        fileChunkUI = new FileChunkUI(bfclient.getMyChunks());
        bottomPanel.add(fileChunkUI, BorderLayout.EAST);

        // Add the command UI
        commandUI = new CommandUI(this, BFClient.Command.values());
        bottomPanel.add(commandUI, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Set focus on console input
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bfclient.close();
            }
        });

        // Display the window.
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public BFClient getBFClient() {
        return bfclient;
    }

    public Console getConsole() {
        return console;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void updateRoutingTableUI(RoutingTable routingTable) {
        routingTableUI.update(routingTable);
        commandUI.populateClientComboBox();
    }

    public void updateFileChunkUI(FileChunk[] chunks) {
        fileChunkUI.update(chunks);
    }

    public void close() {
        frame.setVisible(false);
        frame.dispose();
    }
}
