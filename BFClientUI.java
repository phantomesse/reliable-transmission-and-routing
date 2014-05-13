import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultCaret;

public class BFClientUI {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;
    private static final int PADDING = 10;
    private static final Color CONSOLE_BG_COLOR = new Color(38, 38, 38);
    private static final Color CONSOLE_INPUT_BG_COLOR = new Color(64, 64, 64);
    private static final Color CONSOLE_FG_COLOR = new Color(188, 201, 133);
    private static final Color CONSOLE_INPUT_FG_COLOR = new Color(253, 255, 232);
    private static final Color TABLE_BG_COLOR = new Color(250, 250, 250);
    private static final Color TABLE_FG_COLOR = CONSOLE_BG_COLOR;
    private static final Font FONT = new Font("Consolas", Font.BOLD, 12);

    private BFClient bfclient;

    private JFrame frame;
    private JTextArea console;
    private JTextField consoleInput;
    private JTable routingTableDisplay;

    public BFClientUI(BFClient bfclient) {
        this.bfclient = bfclient;
    }

    public synchronized void updateRoutingTable() {
        DefaultTableModel model = (DefaultTableModel) (routingTableDisplay.getModel());
        
        // Remove all rows
        int rows = model.getRowCount();
        for (int i = rows - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        
        // Repopulate rows
        String[] columnNames = {
                "Destination", "Cost", "Link", "Last Heard From"
        };
        String[][] data = bfclient.getRoutingTable().getRoutingTableDisplayInfo();
        model = new DefaultTableModel(data, columnNames);
        routingTableDisplay.setModel(model);
        
        routingTableDisplay.revalidate();
    }

    public void createAndRunGUI() {
        // Create and set up frame
        frame = new JFrame("BFClient");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Create routing table display
        String[] columnNames = {
                "Destination", "Cost", "Link", "Last Heard From"
        };
        String[][] data = bfclient.getRoutingTable()
                .getRoutingTableDisplayInfo();
        routingTableDisplay = new JTable(new DefaultTableModel(data, columnNames));
        routingTableDisplay.setBackground(TABLE_BG_COLOR);
        routingTableDisplay.setForeground(TABLE_FG_COLOR);
        JPanel routingTableDisplayPanel = new JPanel(new BorderLayout());
        routingTableDisplayPanel.add(routingTableDisplay.getTableHeader(),
                BorderLayout.PAGE_START);
        routingTableDisplayPanel.add(routingTableDisplay, BorderLayout.CENTER);
        panel.add(routingTableDisplayPanel, BorderLayout.NORTH);

        // Set table column widths
        TableColumn column = null;
        for (int i = 0; i < 4; i++) {
            column = routingTableDisplay.getColumnModel().getColumn(i);
            int weight = 0; // out of 10
            switch (i) {
                case 0:
                    weight = 3;
                    break;
                case 1:
                    weight = 1;
                    break;
                case 2:
                    weight = 3;
                    break;
                case 3:
                    weight = 3;
                    break;
            }
            column.setPreferredWidth(WIDTH * weight / 10);
        }

        // Create the console
        console = new JTextArea();
        console.setBackground(CONSOLE_BG_COLOR);
        console.setForeground(CONSOLE_FG_COLOR);
        console.setFont(FONT);
        console.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        console.setEditable(false);
        DefaultCaret caret = (DefaultCaret) console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane consoleScrollPane = new JScrollPane(console);
        consoleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(consoleScrollPane, BorderLayout.CENTER);

        // Create the console input area
        JPanel consoleInputPanel = new JPanel(new BorderLayout());
        consoleInputPanel.setBackground(CONSOLE_INPUT_BG_COLOR);
        JLabel commandLabel = new JLabel("Command:");
        commandLabel.setForeground(CONSOLE_INPUT_FG_COLOR);
        commandLabel.setFont(FONT);
        commandLabel.setBorder(new EmptyBorder(PADDING / 2, PADDING,
                PADDING / 2, 0));
        consoleInputPanel.add(commandLabel, BorderLayout.WEST);
        consoleInput = new JTextField();
        consoleInput.setBackground(CONSOLE_INPUT_BG_COLOR);
        consoleInput.setForeground(CONSOLE_INPUT_FG_COLOR);
        consoleInput.setCaretColor(CONSOLE_INPUT_FG_COLOR);
        consoleInput.setFont(FONT);
        consoleInput.setBorder(new EmptyBorder(PADDING / 2, PADDING,
                PADDING / 2, PADDING));
        consoleInputPanel.add(consoleInput, BorderLayout.CENTER);
        panel.add(consoleInputPanel, BorderLayout.SOUTH);

        // Add action listener for input box to send to server
        consoleInput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = consoleInput.getText();

                if (text.equals("")) {
                    // Nothing was entered
                    return;
                }

                console.setText(console.getText() + "$ " + text + "\n");
                consoleInput.setText("");

                // Get response from BFClient
                String response = bfclient.executeCommand(text);
                console.setText(console.getText() + response + "\n\n");
            }
        });

        // Set focus on console input
        frame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                // Set focus on input box
                consoleInput.requestFocus();
            }

            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                bfclient.close();
            }
        });

        // Display the window.
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
