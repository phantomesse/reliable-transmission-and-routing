package old;
import java.awt.FlowLayout;

import javax.swing.JPanel;

public class CommandArgumentsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private Command command;
	private InputField[] parameterFields;

	public CommandArgumentsPanel(Command command, String[] parameters) {
		super(new FlowLayout(FlowLayout.LEFT));
		setOpaque(false);
		
		this.command = command;

		parameterFields = new InputField[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameterFields[i] = new InputField(parameters[i]);
			add(parameterFields[i]);
		}
	}

	/**
	 * Checks if all of the parameter fields are filled out.
	 * 
	 * @return null if everything is filled out or a list of parameters that are
	 *         not filled out
	 */
	public String isFilledOut() {
		String notFilledOut = "";
		for (InputField parameterField : parameterFields) {
			if (!parameterField.isFilledOut()) {
				notFilledOut += "<li>" + parameterField.getPlaceholderText()
						+ "</li>";
			}
		}
		return notFilledOut.isEmpty() ? null
				: "<html>The following fields are not filled out:<ul>"
						+ notFilledOut + "</ul></html>";
	}
	
	public String getCommand() {
		return command.name();
	}
	
	public String getArguments() {
		String arguments = "";
		for (InputField parameterField : parameterFields) {
			arguments += " " + parameterField.getText();
		}
		return arguments.isEmpty()? arguments : arguments.substring(1);
	}
}
