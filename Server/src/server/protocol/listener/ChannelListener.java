package server.protocol.listener;

import java.io.IOException;

import server.protocol.Channel;
import server.protocol.message.Message;


public class ChannelListener extends Thread {
	private volatile boolean running = true;
	private Channel channel;

	public ChannelListener(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Message msg = this.channel.receive();
				this.channel.notifyObservers(msg);
			} catch (IOException e) {
				if (this.channel.isOpen())
					e.printStackTrace();
				this.terminate();
			}
		}
	}

	public void terminate() {
		this.running = false;
	}
}
