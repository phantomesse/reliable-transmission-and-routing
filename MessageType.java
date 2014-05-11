package old2;
public enum MessageType {
	LINKDOWN, LINKUP, ROUTING_UPDATE, TRANSFER;

	public String getHeader() {
		return "START " + name() + "\n";
	}

	public String getFooter() {
		return "\nEND " + name() + "\n";
	}

	public static MessageType determineMessageType(String message) {
		try {
			return MessageType.valueOf(message.split("\n")[0]
					.substring("START ".length()));
		} catch (Exception e) {
			return null;
		}

	}
}