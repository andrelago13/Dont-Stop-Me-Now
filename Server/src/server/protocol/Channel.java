package server.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

import server.protocol.listener.ChannelListener;
import server.protocol.message.Message;

public class Channel extends Observable {

	private InetAddress address;
	private int port;
	private ServerSocket socket;
	private ChannelListener listener;
	private boolean isOpen;

	public Channel(InetAddress address, int port) throws Exception {
		this.address = address;
		this.port = port;	
		this.isOpen = false;
	}

	public synchronized void open() throws IOException {
		if (this.socket != null)
			this.close();

		this.socket = new ServerSocket(0);
		this.setChanged();
		this.listener = new ChannelListener(this);
		this.listener.start();
		this.isOpen = true;
	}

	public synchronized void close() throws IOException {
		this.isOpen = false;
		this.listener.terminate();
		this.socket.close();
	}

	public void send(Message message) throws IOException {
		Socket outSocket = new Socket(this.address, this.port);
		DataOutputStream out = new DataOutputStream(outSocket.getOutputStream());
		out.writeBytes(message.toString());
	}

	public Message receive() throws IOException {
		Socket inSocket = this.socket.accept();
		InputStream in = inSocket.getInputStream();
		byte[] bytes = new byte[64 * 1024];
		if(in.read(bytes) > 0)
			return Protocol.parseMessage(bytes);
		else return null;
	}

	public synchronized boolean isOpen() {
		return this.isOpen && !this.socket.isClosed();
	}

	@Override
	public void notifyObservers(Object arg) {
		this.setChanged();
		super.notifyObservers(arg);
	}
}
