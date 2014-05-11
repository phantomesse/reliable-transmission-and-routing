package old;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class BFClientUI implements ActionListener {
	private static Color BACKGROUND_COLOR = new Color(200, 200, 200);
	public static Font FONT = new Font("Consolas", Font.PLAIN, 12);
	private static int WIDTH = 600;
	private static int HEIGHT = 300;

	private BFClient client;

	private JFrame frame;

	private JTextPane console;
	private JScrollPane consoleScrollPane;

	private JComboBox commandList;
	private JPanel commandPanel;
	private CommandArgumentsPanel commandArguments;
	private JButton commandButton;

	public BFClientUI(BFClient client) {
		this.client = client;

		// Instantiate the GUI
		frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		// Make console
		console = new JTextPane();
		console.setBackground(BACKGROUND_COLOR);
		console.setEditable(false);
		console.setBorder(new EmptyBorder(10, 10, 10, 10));
		consoleScrollPane = new JScrollPane(console);
		consoleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel.add(consoleScrollPane, BorderLayout.CENTER);

		// Make the drop down menu for commands
		commandList = new JComboBox(Command.values());
		commandList.setSelectedIndex(0);
		commandList.addActionListener(this);
		commandList.setFont(FONT);

		// Make the button for executing the command
		commandButton = new JButton("GO!");
		commandButton.addActionListener(this);
		commandButton.setFont(FONT);
		JPanel commandButtonPanel = new JPanel();
		commandButtonPanel.setOpaque(false);
		commandButtonPanel.add(commandButton);

		// Make the command panel
		commandPanel = new JPanel(new BorderLayout());
		commandPanel.setBackground(BACKGROUND_COLOR);
		commandPanel.add(commandList, BorderLayout.WEST);
		commandPanel.add(commandButtonPanel, BorderLayout.EAST);
		panel.add(commandPanel, BorderLayout.SOUTH);

		// Fill in the command panel arguments
		replaceCommandArgumentsPanel();

		// Show the GUI
		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void replaceCommandArgumentsPanel() {
		Command command = (Command) commandList.getSelectedItem();
		BorderLayout layout = (BorderLayout) commandPanel.getLayout();
		try {
			commandPanel.remove(layout.getLayoutComponent(BorderLayout.CENTER));
		} catch (NullPointerException e) {
			// Don't need to do anything
		}
		commandArguments = command.getCommandArgumentsPanel();
		commandPanel.add(commandArguments, BorderLayout.CENTER);
		commandPanel.validate();
		commandPanel.repaint();
	}

	/**
	 * Prints a command to the console.
	 */
	private void printCommand(String command, String arguments) {
		SimpleAttributeSet commandStyle = new SimpleAttributeSet();
		StyleConstants.setBold(commandStyle, true);
		
		try {
			Document doc = console.getDocument();
			doc.insertString(doc.getLength(), command + " ", commandStyle);
			doc.insertString(doc.getLength(), arguments + "\n", null);
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}

		scrollConsoleToBottom();
	}
	
	/**
	 * Prints the routing table
	 */
/*	private void printRoutingTable() {
		SimpleAttributeSet commandStyle = new SimpleAttributeSet();
		StyleConstants.setBold(commandStyle, true);
		
		try {
			Document doc = console.getDocument();
			doc.insertString(doc.getLength(), command + " ", commandStyle);
			doc.insertString(doc.getLength(), arguments + "\n", null);
		} catch (BadLocationException exc) {
			exc.printStackTrace();
		}
		
		scrollConsoleToBottom();
	}*/
	
	private void scrollConsoleToBottom() {
		console.setCaretPosition(console.getDocument().getLength());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source.equals(commandList)) {
			replaceCommandArgumentsPanel();
		} else if (source.equals(commandButton)) {
			// Check if all the fields are filled out
			String notFilledOut = commandArguments.isFilledOut();
			if (notFilledOut != null) {
				JOptionPane.showMessageDialog(frame, notFilledOut, "Oh noes!",
						JOptionPane.PLAIN_MESSAGE);
				return;
			}

			// Execute command
			printCommand(commandArguments.getCommand(), commandArguments.getArguments());
		}
	}
}
