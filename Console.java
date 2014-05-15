import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
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
        JScrollPane scrollPane = new JScrollPane(console);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addCommand(String command) {
        StyledDocument doc = console.getStyledDocument();
        Style style = console.addStyle("commandStyle", null);
        StyleConstants.setForeground(style, Color.red);

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
