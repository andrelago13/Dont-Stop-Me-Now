package server.protocol.message;

import server.protocol.Protocol;

public class PutMessage extends Message {
	byte[] body;
	public PutMessage() {}
	public PutMessage(int fileId, byte[] body) {
		super(fileId);
		this.body = body;
		this.generateData();
	}

	@Override
	public Type getType() {
		return Message.Type.PUT;
	}
	
	@Override
	protected void generateData() {
		byte[] a = (super.toString() + " " + Protocol.CRLF + Protocol.CRLF).getBytes();
		this.data = new byte[a.length + body.length];
		System.arraycopy(a, 0, this.data, 0, a.length);
		System.arraycopy(this.body, 0, this.data, a.length, this.body.length);
	}
	
	public byte[] getBody() {
		return this.body;
	}
}