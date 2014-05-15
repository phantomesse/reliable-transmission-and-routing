import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FileChunkUI extends JPanel {
    private JCheckBox[] checks;

    public FileChunkUI(FileChunk[] myChunks) {
        super(new BorderLayout());

        add(new JLabel("File Chunks"), BorderLayout.PAGE_START);

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
