package old;
public enum Command {
	LINKDOWN("ip address", "port number"), LINKUP("ip address", "port number", "weight"), SHOWRT, CLOSE, TRANSFER(
			"destination ip address", "port number");

	private CommandArgumentsPanel commandArgumentsPanel;

	private Command(String... parameters) {
		commandArgumentsPanel = new CommandArgumentsPanel(this, parameters);
	}

	public CommandArgumentsPanel getCommandArgumentsPanel() {
		return commandArgumentsPanel;
	}
}
