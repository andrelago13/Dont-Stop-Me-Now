package server.protocol.message;

public class StoredMessage extends Message {
	public StoredMessage() {}
	public StoredMessage(int fileId) {
		super(fileId);
		this.generateData();
	}

	@Override
	public Type getType() {
		return Message.Type.STORED;
	}

	@Override
	protected void generateData() {
		this.data = (super.toString()).getBytes();
	}
}
