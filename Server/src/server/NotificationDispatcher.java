package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NotificationDispatcher {
	Connection db;
	public NotificationDispatcher(Connection db) {
		this.db = db;
	}
	
	public void notifyEvent(int eventID) throws SQLException {
		PreparedStatement ps = this.db.prepareStatement("SELECT facebookID, address, port FROM Users, Events WHERE"
				+ "Users.coords IS NOT NULL"
				+ "AND Events.id = ?"
				+ "AND ST_DWithin(Users.coords, Geography(Point(?, ?)::geometry), Users.radius)");
		ps.setInt(1, eventID);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			rs.getString("address");
			rs.getInt("port");
		}
	}
}
