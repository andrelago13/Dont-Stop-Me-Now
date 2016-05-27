package server;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import api.API;
import server.protocol.Channel;
import server.protocol.listener.*;

public class Server {
	public enum Type{
		Primary,
		Backup
	}
	
	private Channel channel;
	private Listener putListener;
	private Listener storedListener;
	private Type type;
	
	public static void main(String[] args) throws Exception {
		Server server = new Server(args);
	}

	private Server(String[] args) throws Exception {
		this.setupServerSync(args);
		
		this.setHttpsConnection();
		
		this.channel.open();
		this.putListener.start();
		this.storedListener.start();
	}
	

	private void setupServerSync(String[] args) throws Exception {
		if (!this.validateArgs(args)) {
			System.out.println("Usage: Server <InetAddress_address> <Integer_port>");
			System.exit(1);
		}
		this.channel = new Channel(InetAddress.getByName(args[0]), Integer.parseInt(args[1]));
		this.putListener = new PutListener(this.channel);
		this.storedListener = new StoredListener(this.channel);
	}

	private boolean validateArgs(String[] args) {
		return args.length >= 2;
	}
	
	private void setHttpsConnection() throws Exception {
		HttpsServer server = HttpsServer.create(new InetSocketAddress(443), 0);
		server.setHttpsConfigurator(new HttpsConfigurator(createSSLContext()));
		server.createContext("/api", new API(1));
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
}