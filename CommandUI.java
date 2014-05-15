import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CommandUI extends JPanel implements ActionListener {

    private class InputField extends JTextField implements FocusListener {
        private String placeholderText;

        public InputField(String placeholderText) {
            super(placeholderText);
            addFocusListener(this);
            this.placeholderText = placeholderText;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (getText().equals(placeholderText)) {
                setText("");
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (getText().isEmpty()) {
                setText(placeholderText);
            }
        }
    }

    private BFClientUI gui;
    private JComboBox commandComboBox;
    private JComboBox clientComboBox;
    private InputField costField;
    private JButton submitButton;

    public CommandUI(BFClientUI gui, BFClient.Command[] commands) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBackground(new Color(240, 240, 240));

        this.gui = gui;

        commandComboBox = new JComboBox(commands);
        commandComboBox.addActionListener(this);
        add(commandComboBox);

        clientComboBox = new JComboBox();
        populateClientComboBox();
        clientComboBox.addActionListener(this);
        add(clientComboBox);

        costField = new InputField("cost");
        costField.setColumns(5);
        costField.setVisible(false);
        add(costField);

        submitButton = new JButton("Go!");
        submitButton.addActionListener(this);
        add(submitButton);
    }

    /**
     * Populates the client combo box with the clients in the routing table.
     */
    public void populateClientComboBox() {
        String select = (String) clientComboBox.getSelectedItem();
        clientComboBox.removeAllItems();

        Collection<Client> clients = gui.getBFClient().getRoutingTable()
                .getClients();
        for (Client client : clients) {
            clientComboBox.addItem(client.getIpAddressPortNumberString());
        }

        if (select != null) {
            clientComboBox.setSelectedItem(select);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source.equals(commandComboBox)) {
            BFClient.Command command = (BFClient.Command) commandComboBox
                    .getSelectedItem();
            switch (command) {
                case LINKDOWN:
                    clientComboBox.setVisible(true);
                    costField.setVisible(false);
                    submitButton.setVisible(true);
                    revalidate();
                    repaint();
                    break;
                case LINKUP:
                    clientComboBox.setVisible(true);
                    costField.setVisible(true);
                    submitButton.setVisible(true);
                    revalidate();
                    repaint();
                    break;
                case SHOWRT:
                    clientComboBox.setVisible(false);
                    costField.setVisible(false);
                    submitButton.setVisible(false);
                    revalidate();
                    repaint();
                    String routingTableStr = gui.getBFClient()
                            .getRoutingTable().toString();
                    gui.getConsole().addCommand(command.name());
                    gui.getConsole().add(routingTableStr + "\n");
                    break;
                case CLOSE:
                    clientComboBox.setVisible(false);
                    costField.setVisible(false);
                    submitButton.setVisible(false);
                    gui.getBFClient().close();
                    revalidate();
                    repaint();
                    return;
                case TRANSFER:
                    clientComboBox.setVisible(true);
                    costField.setVisible(false);
                    submitButton.setVisible(true);
                    revalidate();
                    repaint();
                    break;
            }
        } else if (source.equals(submitButton)) {
            BFClient.Command command = (BFClient.Command) commandComboBox
                    .getSelectedItem();

            // Get the ip address and port combo
            Client client = null;
            try {
                client = new Client((String) clientComboBox.getSelectedItem());
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(gui.getFrame(),
                        "Please make sure that the client is valid",
                        "Invalid Argument",
                        JOptionPane.PLAIN_MESSAGE);
                return;
            }

            String commandStr = command.name() + " "
                    + client.getIpAddress().getHostAddress() + " "
                    + client.getPortNumber();

            // Get the cost if LINKUP
            double cost = 0;
            if (command == BFClient.Command.LINKUP) {
                // Check if the cost is valid
                try {
                    cost = Double.parseDouble(costField.getText());

                    commandStr += " " + cost;
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(gui.getFrame(),
                            "Please make sure that the cost is a double!",
                            "Invalid Argument",
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }
            }

            // Print the command to console
            gui.getConsole().addCommand(commandStr);

            // Perform the command
            String returnMessage = null;
            switch (command) {
                case LINKDOWN:
                    returnMessage = gui.getBFClient().linkdown(client);
                    break;
                case LINKUP:
                    returnMessage = gui.getBFClient().linkup(client, cost);
                    break;
                case TRANSFER:
                    returnMessage = gui.getBFClient().transfer(client);
                    break;
                default:
                    break;
            }

            // Print the return message
            gui.getConsole().add(returnMessage + "\n");
        }
    }
}
