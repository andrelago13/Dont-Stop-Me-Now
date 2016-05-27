package server.protocol.message;

import java.net.InetAddress;

public abstract class Message {
	public enum Type {
		PUT,
		STORED
	}
	int fileId;
	byte[] data;
	InetAddress senderAddr;
	int senderPort;
	public Message() { } // Default constructor to allow the use of the abstract methods as if they were static abstract
	
	public Message(int fileId) {
		this.fileId = fileId;
	}
	
	public void setSenderAddr(InetAddress senderAddr) {
		this.senderAddr = senderAddr;
	}
	
	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}
	
	public InetAddress getSenderAddr() {
		return this.senderAddr;
	}
	
	public int getSenderPort() {
		return this.senderPort;
	}
	
	public abstract Type getType();
	
	public int getFileId() {
		return this.fileId;
	}

	public byte[] getData() {
		return this.data;
	}
	
	@Override
	public String toString() {
		return this.getType().toString() + " " + this.fileId;
	}
	
	protected abstract void generateData();

}
