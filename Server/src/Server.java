import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

public class Server {
	
	public static void main(String[] args) throws Exception {
		Server server = new Server();
	}
	
	public Server() throws Exception {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(443), 0);
		server.setHttpsConfigurator(new HttpsConfigurator(createSSLContext()));
		server.createContext("/test", new RequestHandler());
		server.setExecutor(null);
		server.start();
	}
	
	private static SSLContext createSSLContext() throws Exception {
		SSLContext sslContext = SSLContext.getInstance("TLS");
	    char[] keystorePassword = "123456".toCharArray();
	    KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(new FileInputStream("keys/server.keys"), keystorePassword);
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, keystorePassword);
	    sslContext.init(kmf.getKeyManagers(), null, null);
	    return sslContext;
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
