package server.protocol.listener;

import java.util.Observable;

import server.protocol.Channel;
import server.protocol.message.PutMessage;

public class PutListener implements Listener {
	private Channel channel;
	
	public PutListener(Channel channel) {
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
		if (!(obj instanceof PutMessage))
			return;

		/*PutMessage msg = (PutMessage) obj;
		Chunk chunk = new Chunk(msg.getFileId(), msg.getChunkNum(), msg.getBody());
		if(msg.getVersion().compareTo(Protocol.enhancedBackupVersion) == 0)
			new StoreChunk(this.dbs, chunk, true).start();
		else new StoreChunk(this.dbs, chunk, false).start();*/ //TODO
	}
}
