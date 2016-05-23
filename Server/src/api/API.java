package api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import jdk.nashorn.internal.scripts.JO;

public class API implements HttpHandler {
	Connection db;
	int pathOffset;

	public API(int pathOffset) throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		this.db = DriverManager.getConnection("jdbc:postgresql://localhost/", "postgres", "123456");
		this.pathOffset = pathOffset;
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		String method = t.getRequestMethod();
		URI uri = t.getRequestURI();
		String[] paths = uri.getPath().replaceFirst("^/", "").split("/");
		String query = uri.getQuery();
		if (query == null)
			query = "";
		Map<String, String> map = queryToMap(query);
		try {
			process(t, method, paths, map);
		} catch (SQLException e) {
			e.printStackTrace();
			respond(t, formatError(method, map, "Unknown error."), 500);
		}
	}

	private void process(HttpExchange t, String method, String[] paths, Map<String, String> query)
			throws IOException, SQLException {

		if (!checkAuth(t)) {
			respond(t, formatError(method, null, "Authentication failed"), 401);
			return;
		}

		InputStream is = t.getRequestBody();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int b;
		while ((b = is.read()) != -1) {
			buffer.write(b);
		}
		buffer.flush();
		byte[] body = buffer.toByteArray();

		switch (paths[pathOffset]) {
		case "events":
			eventsEndpoint(t, method, paths, query, body);
			break;
		default: {
			String response = "{a:\"aaaa\", b}";
			respond(t, response, 200);
		}
		}
	}

	private boolean checkAuth(HttpExchange t) {
		String token = t.getRequestHeaders().getFirst("Authorization");
		System.out.println("Token: " + token);
		return true;
	}

	private void eventsEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query, byte[] body)
			throws IOException, SQLException {
		if (paths.length < pathOffset + 2) { //events/
			switch (method) {
			case "GET":
				eventList(t, method, paths, query);
				break;
			case "POST":
				eventCreate(t, method, paths, query, body);
				break;
			default:
				respond(t, formatError(method, query, "Invalid method."), 404);
			}
			return;
		}
		int eventID;
		try {
			eventID = Integer.parseInt(paths[pathOffset + 1]);
		} catch (NumberFormatException e) {
			respond(t, formatError(method, query, "Invalid event ID."), 400);
			return;
		}
		if (paths.length >= pathOffset + 3) { //events/<id>/...
			switch (paths[pathOffset + 2]) {
			case "comments":
				eventCommentsEndpoint(t, method, paths, query, body, eventID);
				break;
			case "confirmations":
				eventConfirmationsEndpoint(t, method, paths, query, body, eventID);
				break;
			case "photo":
				eventPhotoEndpoint(t, method, paths, query, body, eventID);
			default:
				respond(t, formatError(method, query, "Unknown verb '" + paths[pathOffset + 1] + "'."), 404);
			}
		}
		else { //events/<id>
			switch (method) {
			case "GET": {
				Statement stmt = this.db.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT *, ST_X(coords::geometry) AS longitude, ST_Y(coords::geometry) AS latitude FROM Events WHERE id = " + eventID);
				if (!rs.next())
					respond(t, formatError(method, query, "Event does not exist."), 404);
				else
					respond(t, eventResultToJSON(rs).toString(), 200);
			}
			break;
			case "DELETE": {
				Statement stmt = this.db.createStatement();
				if (stmt.executeUpdate("DELETE FROM Events WHERE id = " + eventID) == 0)
					respond(t, formatError(method, query, "Event does not exist."), 404);
				else
					respond(t, formatSuccess(method, query), 200);
			}
			break;
			default:
				respond(t, formatError(method, query, "Invalid method."), 404);
			}
		}
	}

	private void eventCreate(HttpExchange t, String method, String[] paths, Map<String, String> query, byte[] body)
			throws IOException, SQLException {
		try {
			JSONObject jo = new JSONObject(body).getJSONObject("create_event");
			PreparedStatement stmt = this.db.prepareStatement(
					"INSERT INTO Events (creator, type, description, location, coords) VALUES (?, ?, ?, ?, Geography(Point(?, ?)::geometry))");
			stmt.setInt(1, 1); // TODO;
			stmt.setInt(2, jo.getInt("type"));
			stmt.setString(3, jo.getString("description"));

			if (jo.has("location"))
				stmt.setString(4, jo.getString("location"));
			else
				stmt.setNull(5, java.sql.Types.NULL);

			if (jo.has("longitude") && jo.has("latitude")) {
				stmt.setFloat(5, (float)jo.getDouble("longitude"));
				stmt.setFloat(6, (float)jo.getDouble("latitude"));
			} else {
				stmt.setNull(5, java.sql.Types.NULL);
				stmt.setNull(6, java.sql.Types.NULL);
			}

			if (stmt.executeUpdate() == 0)
				respond(t, formatError(method, query, "Invalid request parameters."), 400);
			else
				respond(t, formatSuccess(method, query), 200);
		} catch (JSONException e) {
			respond(t, formatError(method, query, "Invalid request body."), 400);
			return;
		}

	}

	private String findMissingArgument(Map<String, String> query, String... requireds) {
		for (String required : requireds) {
			if (query.get(required) == null)
				return required;
		}
		return null;
	}

	private JSONObject eventResultToJSON(ResultSet rs) throws JSONException, SQLException {
		JSONObject jo = new JSONObject();
		jo.put("id", rs.getInt("id"));
		jo.put("type", rs.getInt("type"));
		jo.put("description", rs.getString("description"));
		try {
			rs.getString("distance");
			jo.put("distance", rs.getFloat("distance"));
		} catch (SQLException e) {
		}

		String location = rs.getString("location");
		String longitude = rs.getString("longitude");
		String latitude = rs.getString("latitude");
		if (location != null)
			jo.put("location", location);
		if (longitude != null && latitude != null) {
			jo.put("longitude", longitude);
			jo.put("latitude", latitude);
		}

		jo.put("datetime", rs.getTimestamp("datetime"));
		return jo;
	}

	private void eventList(HttpExchange t, String method, String[] paths, Map<String, String> query)
			throws IOException, SQLException {
		if (!method.equals("GET")) {
			String response = formatError(method, query, "Invalid method.");
			respond(t, response, 404);
		} else {
			ResultSet rs;
			Statement stmt;
			boolean onlymine = false;
			if (query.get("onlymine") != null)
				onlymine = Boolean.parseBoolean(query.get("onlymine"));
			if (query.get("longitude") == null || query.get("latitude") == null) {
				stmt = this.db.prepareStatement("SELECT *, ST_X(coords::geometry) AS longitude, ST_Y(coords::geometry) AS latitude FROM Events"
						+ (onlymine ? " WHERE creator = ?" : "")
						+ " ORDER BY datetime DESC"
						);
				PreparedStatement ps = (PreparedStatement) stmt;
				if (onlymine)
					ps.setInt(1, 1); // TODO
				rs = ps.executeQuery();
			} else {
				Float radius = null;
				if (query.get("radius") != null) 
				{
					try { radius = Float.parseFloat(query.get("radius")); }
					catch (NumberFormatException e) {};
				}
				String sqlQuery = "SELECT *,"
						+ " ST_X(coords::geometry) AS longitude, ST_Y(coords::geometry) AS latitude, ST_DISTANCE(coords, Geography(Point(?, ?)::geometry)) AS distance"
						+ " FROM Events";
				if (radius == null) {
					if (onlymine)
						sqlQuery += " WHERE creator = ?";
				} else {
					sqlQuery += " WHERE ST_DWithin(coords, Geography(Point(?, ?)::geometry), ?)";
					if (onlymine)
						sqlQuery += " AND creator = ?";
				}
				sqlQuery += " ORDER BY datetime DESC";
				
				stmt = this.db.prepareStatement(sqlQuery);
				PreparedStatement ps = (PreparedStatement)stmt;
				float longitude = Float.parseFloat(query.get("longitude"));
				float latitude = Float.parseFloat(query.get("latitude"));
				int index = 1;
				ps.setFloat(index++, longitude);
				ps.setFloat(index++, latitude);
				if (radius != null) {
					ps.setFloat(index++, longitude);
					ps.setFloat(index++, latitude);
					ps.setFloat(index++, radius);
				}
				if (onlymine)
					ps.setFloat(index++, 1); // TODO
				rs = ps.executeQuery();
			}
			JSONArray ja = new JSONArray();
			while (rs.next()) {
				ja.put(eventResultToJSON(rs));
			}
			respond(t, ja.toString(), 200);
			rs.close();
			stmt.close();
		}
	}

	private void eventPhotoEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query, byte[] body, int eventID) throws IOException, SQLException {
		switch (method) {
		case "GET": {
			PreparedStatement stmt = this.db.prepareStatement("SELECT photo FROM events WHERE id = ?");
			stmt.setInt(1, eventID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				byte[] photo = rs.getBytes("photo");
				if (rs.wasNull()) {
					respond(t, formatError(method, query, "Event has no photo."), 404);
				} else {
					Headers h = t.getResponseHeaders();
					h.add("Content-Type", "image/jpeg");
					t.sendResponseHeaders(200, photo.length);
					OutputStream os = t.getResponseBody();
					os.write(photo);
					os.close();
				}
			} else {
				respond(t, formatError(method, query, "Event not found."), 404);
			}
			rs.close();
			break;
		}
		case "PUT": {
			PreparedStatement stmt = this.db.prepareStatement("UPDATE Events SET photo = ? WHERE id = ?");
			stmt.setBytes(1, body);
			stmt.setInt(2, eventID);

			if (stmt.executeUpdate() == 0)
				respond(t, formatError(method, query, "Invalid request parameters."), 400);
			else
				respond(t, formatSuccess(method, query), 200);

			break;
		}
		case "DELETE": {
			PreparedStatement stmt = this.db.prepareStatement("UPDATE Events SET photo = NULL WHERE id = ?");
			stmt.setInt(1, eventID);

			if (stmt.executeUpdate() == 0)
				respond(t, formatError(method, query, "Invalid request parameters."), 400);
			else
				respond(t, formatSuccess(method, query), 200);

			break;
		}
		default: {
			String response = formatError(method, query, "Invalid method.");
			respond(t, response, 404);
		}
		}
	}

	private void eventCommentsEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query, byte[] body, int eventID) throws IOException, SQLException {
		switch (method) {
		case "GET": {
			PreparedStatement stmt = this.db.prepareStatement("SELECT * FROM Comments WHERE Comments.event = ? ORDER BY datetime DESC");
			stmt.setInt(1, eventID);
			ResultSet rs = stmt.executeQuery();
			JSONArray ja = new JSONArray();
			while (rs.next()) {
				ja.put(commentResultToJSON(rs));
			}
			respond(t, ja.toString(), 200);
			rs.close();
			stmt.close();
			break;
		}
		case "POST": {
			try {
				JSONObject jo = new JSONObject(body).getJSONObject("create_comment");
				PreparedStatement stmt = this.db.prepareStatement(
						"INSERT INTO Comments (event, message) VALUES (?, ?)");
				stmt.setInt(1, jo.getInt("eventid"));
				stmt.setString(2, jo.getString("message"));

				if (stmt.executeUpdate() == 0)
					respond(t, formatError(method, query, "Invalid request parameters."), 400);
				else
					respond(t, formatSuccess(method, query), 200);
			} catch (JSONException e) {
				respond(t, formatError(method, query, "Invalid request body."), 400);
			}
			break;
		}
		default: {
			String response = formatError(method, query, "Invalid method.");
			respond(t, response, 404);
		}
		}
	}

	private void eventConfirmationsEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query, byte[] body, int eventID) throws IOException, SQLException {
		switch (method) {
		case "PUT": {
			try {
				JSONObject jo = new JSONObject(body).getJSONObject("event_confirm");
				PreparedStatement stmt = this.db.prepareStatement(
						"INSERT INTO Confirmations (creator, type, event) VALUES (?, ?, ?) ON CONFLICT DO UPDATE SET type = ? WHERE creator = ? AND event = ?");
				stmt.setInt(1, 1); // TODO
				stmt.setBoolean(2, jo.getBoolean("type"));
				stmt.setInt(3, jo.getInt("eventid"));
				stmt.setBoolean(3, jo.getBoolean("type"));
				stmt.setInt(4, 1); // TODO
				stmt.setInt(5, jo.getInt("eventid"));

				if (stmt.executeUpdate() == 0)
					respond(t, formatError(method, query, "Invalid request parameters."), 400);
				else
					respond(t, formatSuccess(method, query), 200);
			} catch (JSONException e) {
				respond(t, formatError(method, query, "Invalid request body."), 400);
			}
			break;
		}
		case "DELETE": {
			try {
				JSONObject jo = new JSONObject(body).getJSONObject("event_confirm");
				PreparedStatement stmt = this.db.prepareStatement("DELETE FROM Confirmations WHERE event = ? AND creator = ?");
				stmt.setInt(1, jo.getInt("eventid"));
				stmt.setInt(2, 1); // TODO
				if (stmt.executeUpdate() == 0)
					respond(t, formatError(method, query, "No confirmation to delete."), 404);
				else
					respond(t, formatSuccess(method, query), 200);
			} catch (JSONException e) {
				respond(t, formatError(method, query, "Invalid request body."), 400);
			}
			break;
		}
		default: {
			String response = formatError(method, query, "Invalid method.");
			respond(t, response, 404);
		}
		}
	}

	private JSONObject commentResultToJSON(ResultSet rs) throws JSONException, SQLException {
		JSONObject jo = new JSONObject();
		jo.put("id", rs.getInt("id"));
		jo.put("message", rs.getString("message"));
		jo.put("datetime", rs.getTimestamp("datetime"));
		return jo;
	}

	/***********************************
	 ************** UTILS **************
	 ***********************************/

	private String formatError(String method, Map<String, String> query, String errorMsg) {
		JSONObject jo = new JSONObject();
		JSONObject joError = new JSONObject();
		joError.put("oper", method);
		if (query != null)
			for (Map.Entry<String, String> entry : query.entrySet()) {
				joError.put(entry.getKey(), entry.getValue());
			}
		joError.put("msg", errorMsg);
		jo.put("error", joError);
		return jo.toString();
	}

	private String formatSuccess(String method, Map<String, String> query) {
		JSONObject jo = new JSONObject();
		JSONObject joSuccess = new JSONObject();
		joSuccess.put("oper", method);
		if (query != null)
			for (Map.Entry<String, String> entry : query.entrySet()) {
				joSuccess.put(entry.getKey(), entry.getValue());
			}
		jo.put("success", joSuccess);
		return jo.toString();
	}

	private void respond(HttpExchange t, String response, int code) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Content-Type", "application/json");
		t.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private Map<String, String> queryToMap(String query) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0].toLowerCase(), pair[1]);
			} else {
				result.put(pair[0].toLowerCase(), "");
			}
		}
		return result;
	}
}
