import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Graphical user interface component for the {@link FileChunk}. Uses check
 * boxes to show whether a client has a chunk or not. Note that, even though a
 * client may have a check box ticked, it may not have that file chunk to send
 * to other clients since it may have gotten that file chunk from another
 * client.
 * 
 * @author Lauren Zou
 */
@SuppressWarnings("serial")
public class FileChunkUI extends JPanel {
    private JCheckBox[] checks;

    public FileChunkUI(FileChunk[] myChunks) {
        super(new BorderLayout());

        add(new JLabel("File Chunks:"), BorderLayout.WEST);

        checks = new JCheckBox[myChunks.length];
        JPanel panel = new JPanel();
        for (int i = 0; i < myChunks.length; i++) {
            checks[i] = new JCheckBox();
            checks[i].setEnabled(false);
            checks[i].setSelected(myChunks[i] != null);

            panel.add(checks[i]);
        }
        add(panel, BorderLayout.CENTER);
    }

    public void update(FileChunk[] myChunks) {
        for (int i = 0; i < myChunks.length; i++) {
            checks[i].setSelected(myChunks[i] != null);
        }
    }
}
