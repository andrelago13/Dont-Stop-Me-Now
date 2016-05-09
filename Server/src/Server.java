import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsServer;

public class Server {
	
	public static void main(String[] args) throws IOException {
		Server server = new Server();
	}
	
	public Server() throws IOException {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(443), 0);
		server.createContext("/test", new RequestHandler());
		server.setExecutor(null);
		server.start();
	}
	
	static class RequestHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			String response = "This is the response";
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
		}
	}
}
