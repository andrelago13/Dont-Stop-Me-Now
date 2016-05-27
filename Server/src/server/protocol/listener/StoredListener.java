package server.protocol.listener;

import java.util.Observable;

import server.protocol.Channel;
import server.protocol.message.StoredMessage;

public class StoredListener implements Listener {
	private Channel channel;
	
	public StoredListener(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void start() {
		this.channel.addObserver(this);
	}

	@Override
	public void stop() {
		this.channel.deleteObserver(this);
	}

	@Override
	public void update(Observable o, Object obj) {
		if (!(obj instanceof StoredMessage))
			return;
	}

}
