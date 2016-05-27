package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationDispatcher {
	public static final int NUM_THREADS = 50;
	Connection db;
	ExecutorService threadPool;
	public NotificationDispatcher(Connection db) {
		this.db = db;
		this.threadPool = Executors.newFixedThreadPool(20);
	}
	
	public void notifyEvent(int eventID) throws SQLException, UnknownHostException {
		PreparedStatement ps = this.db.prepareStatement("SELECT facebookID, address, port FROM Users, Events WHERE"
				+ "Users.coords IS NOT NULL"
				+ "AND Events.id = ?"
				+ "AND ST_DWithin(Users.coords, Geography(Point(?, ?)::geometry), Users.radius)");
		ps.setInt(1, eventID);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			InetAddress address = InetAddress.getByName(rs.getString("address"));
			int port = rs.getInt("port");
			this.threadPool.execute(new ClientNotifier(eventID, address, port));
		}
	}
	
	public class ClientNotifier implements Runnable {
		private int eventID;
		private InetAddress address;
		private int port;
		public ClientNotifier(int eventID, InetAddress address, int port) {
			this.eventID = eventID;
			this.address = address;
			this.port = port;
		}
		@Override
		public void run() {
			try {
				Socket s = new Socket(address, port);
				s.getOutputStream().write(eventID);
				s.close();
			} catch (IOException e) {
				e.printStackTrace(); // TODO
			}
			
		}
		
	}
}
