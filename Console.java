import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class Console extends JPanel {
    private JTextPane console;

    public Console() {
        super(new BorderLayout());

        console = new JTextPane();
        console.setEditable(false);
        console.setBorder(new EmptyBorder(10, 10, 10, 10));
        console.setBackground(new Color(240, 240, 240));
        console.setForeground(Color.DARK_GRAY);

        JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addCommand(String command) {
        StyledDocument doc = console.getStyledDocument();
        Style style = console.addStyle("commandStyle", null);
        StyleConstants.setForeground(style, new Color(240, 60, 125));

        try {
            doc.insertString(doc.getLength(), command + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        scroll();
    }

    public void add(String message) {
        StyledDocument doc = console.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        scroll();
    }

    /**
     * Scrolls to the bottom.
     */
    private void scroll() {
        console.setCaretPosition(console.getDocument().getLength());
    }
}
