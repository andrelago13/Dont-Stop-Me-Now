package server.protocol.listener;

import java.util.Observable;
import java.util.Observer;

public interface Listener extends Observer {
	public void start();
	public void stop();
	
	@Override
	public void update(Observable o, Object obj);
}
