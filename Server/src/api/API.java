package api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class API implements HttpHandler {
	@Override
	public void handle(HttpExchange t) throws IOException {
		String method = t.getRequestMethod();
		URI uri = t.getRequestURI();
		String[] paths = uri.getPath().replaceFirst("^/", "").split("/");
		String query = uri.getQuery();
		process(t, method, paths, query == null ? null : queryToMap(query));
	}

	private void process(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Content-Type", "application/json");
		
		switch (paths[1]) {
		case "event":
			eventEndpoint(t, method, paths, query);
			break;
		default: {
			String response = "{a:\"aaaa\", b}";
			t.sendResponseHeaders(200, response.getBytes().length);
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		}
	}

	private void eventEndpoint(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException {
		switch (paths[2]) {
		case "list":
			eventListVerb(t, method, paths, query);
			break;
		}
	}

	private void eventListVerb(HttpExchange t, String method, String[] paths, Map<String, String> query) throws IOException {
		String response = "{\"events\":[{\"id\":1,\"name\":\"Evento 1\",\"type\":\"mobile_radar\"},{\"id\":2,\"name\":\"Evento 2\",\"type\":\"traffic_stop\"}]}";
		t.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	private Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<String, String>();
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
