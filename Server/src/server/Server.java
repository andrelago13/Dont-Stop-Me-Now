package server;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.UUID;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import api.API;
import server.Server.Type;
import server.protocol.*;

public class Server {
	public enum Type{
		PRIMARY,
		BACKUP
	}
	
	private Type type;
	private SocketListener scktListener;
	
	public static void main(String[] args) throws Exception {
		Server server = new Server(args);
	}

	private Server(String[] args) throws Exception {
		if (!this.validateArgs(args)) {
			System.out.println("Usage: Server <String_address> <Integer_TCPsocket_port> <Integer_HTTPSsocket_port>");
			System.exit(1);
		}
		
		this.setupServerSync(args);
		
		this.setHttpsConnection(Integer.parseInt(args[2]));
	}

	private void setupServerSync(String[] args) throws Exception {		
		this.scktListener = new SocketListener(this, args[0], Integer.parseInt(args[1]));
		this.scktListener.start();
	}

	private boolean validateArgs(String[] args) {
		return args.length == 3;
	}
	
	private void setHttpsConnection(Integer port) throws Exception {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 0);
		server.setHttpsConfigurator(new HttpsConfigurator(createSSLContext()));
		API api = new API(1, "localhost", "postgres", "123456", "AIzaSyC3YVaWGgXvwyiFf_7Z7zZoYtsU4j6qKbQ");
		server.createContext("/api", api);
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
	
	public void setType(Server.Type order) {
		this.type = order;
		if(this.type == Server.Type.PRIMARY)
			System.out.println("Server set as PRIMARY SERVER");
		else if(this.type == Server.Type.BACKUP)
			System.out.println("Server set as BACKUP SERVER");
		
	}

	public Server.Type getType() {
		return this.type;
	}
}