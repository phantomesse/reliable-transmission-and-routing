package old;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class InputField extends JTextField implements FocusListener {
	private static final long serialVersionUID = 1L;

	private String placeholderText;

	public InputField(String placeholderText) {
		super(placeholderText, (int) Math.ceil(placeholderText.length()
				* (3.0 / 4.0)));
		
		this.placeholderText = placeholderText;
		
		setFont(BFClientUI.FONT);
		addFocusListener(this);
	}

	/**
	 * Checks if this input field is filled out.
	 */
	public boolean isFilledOut() {
		return getText().isEmpty() ? false : !getText().equals(placeholderText);
	}

	public String getPlaceholderText() {
		return placeholderText;
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
