package api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class API implements HttpHandler {
	Connection db;
	public API() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		this.db = DriverManager.getConnection("jdbc:postgresql://localhost/", "postgres", "123456");
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		String method = t.getRequestMethod();
		URI uri = t.getRequestURI();
		String[] paths = uri.getPath().replaceFirst("^/", "").split("/");
		String query = uri.getQuery();
		try {
			process(t, method, paths, query == null ? null : queryToMap(query));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void process(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException, SQLException {
		Headers h = t.getResponseHeaders();
		h.add("Content-Type", "application/json");

		switch (paths[1]) {
		case "event":
			eventEndpoint(t, method, paths, query);
			break;
		default: {
			String response = "{a:\"aaaa\", b}";
			respond(t, response, 200);
		}
		}
	}

	private void eventEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException, SQLException {
		switch (paths[2]) {
		case "list":
			eventListVerb(t, method, paths, query);
			break;
		}
	}

	private void eventListVerb(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException, SQLException {
		if (!method.equals("GET")) {
			String response = formatError(method, query, "Invalid method.");
			respond(t, response, 404);
		}
		else {
			Statement stmt = this.db.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Events ORDER BY datetime DESC");
			JSONArray ja = new JSONArray();
			while (rs.next()) {
				JSONObject jo = new JSONObject();
				jo.put("id", rs.getInt("id"));
				jo.put("type", rs.getInt("type"));
				jo.put("description", rs.getString("description"));

				String location = rs.getString("location");
				String coords = rs.getString("coords");
				if (location != null) jo.put("location", location);
				if (coords != null) jo.put("coordinates", coords);

				jo.put("datetime", rs.getTimestamp("datetime"));
				ja.put(jo);
			}
			respond(t, ja.toString(), 200);
			rs.close();
			stmt.close();
		}
	}

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

	private void respond(HttpExchange t, String response, int code) throws IOException {
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
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}
}
