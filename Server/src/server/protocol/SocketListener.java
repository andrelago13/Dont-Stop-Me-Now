package server.protocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import server.BackupDatabase;
import server.CreateDatabase;
import server.DeleteDatabase;
import server.RestoreDatabase;
import server.Server;

public class SocketListener extends Thread {
	public static int FIRST_BACKUP_TIMEOUT = 10000;
	public static int SECOND_BACKUP_TIMEOUT = 5000;
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private Server server;
	private String address;
	private Integer port;
	private String ip;
	private boolean first_attempt = true;
	
	public SocketListener(Server server, String address, Integer port) throws Exception {
		this.server = server;
		this.port = port;
		this.address = address;
		this.ip = Utils.getIPv4();
		System.out.println("Server set at IP: " + ip);
	}

	private void setup() {
		Server.Type role = Server.Type.BACKUP;
		try {
			this.socket = new Socket(this.address, this.port);
		} catch (IOException e) {
			this.setupServerSocket();
			role = Server.Type.PRIMARY;
		} finally{
			this.server.setType(role);
		}
		
	}
	
	private void setupServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			System.out.println("ERROR CREATING SERVER SOCKET");
		}
	}
	

	@Override
	public void run() {
		this.setup();
		while (true) {
			if (this.server.getType() == Server.Type.PRIMARY) {
				this.primaryHandler();
			}
			else if (this.server.getType() == Server.Type.BACKUP) {
				this.backupHandler();
			}
		}
	}

	private void primaryHandler() {
		try {
			BackupDatabase.databaseBackup("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_dump.exe","./backup.sql", "123456");
			this.socket = this.serverSocket.accept();
			this.sendBackup();
		} catch (Exception e) {
			System.err.println("SERVER SYNC ERROR: Failed to connect to a BACKUP server. Shutting down...");
			System.exit(1);
		}
	}

	private void backupHandler() {
		try {
			if(this.first_attempt)
				Thread.sleep(SocketListener.FIRST_BACKUP_TIMEOUT);
			else 
				Thread.sleep(SocketListener.SECOND_BACKUP_TIMEOUT);
			this.socket = new Socket(this.address, this.port);
			this.first_attempt = true;
			this.receiveBackup();
		} catch (Exception e) {
			if(this.first_attempt){
				System.out.println("Failed first attempt to connect to PRIMARY server.");
				this.first_attempt = false;
				return;
			}
			this.setupServerSocket();
	        try {
	        	System.out.println("Connection to PRIMARY server failed. Switching to PRIMARY role");
	        	DeleteDatabase.databaseDelete("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\dropdb.exe", "DataBaseRestore", "123456");
	        	CreateDatabase.databaseCreate("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\createdb.exe", "DataBaseRestore", "123456");
				RestoreDatabase.databaseRestore("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_restore.exe", "DataBaseRestore", "./t/teste.sql", "123456");
			} catch (IOException | InterruptedException e1) {
				System.err.println("ERROR ON DATABASE MANAGEMENT DURING ROLE SWAPING");
			}
			this.server.setType(Server.Type.PRIMARY);
			this.first_attempt = true;
		}
	}

	private void sendBackup() throws Exception {
		
		File file = new File("./backup.sql");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis); 
          
        //Get socket's output stream
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        
        //Read File Contents into contents array 
        byte[] contents;
        long fileLength = file.length(); 
        System.out.println("Backup file length: " + file.length());
        long current = 0;
         
        System.nanoTime();
        while(current!=fileLength){ 
            int size = 10000;
            if(fileLength - current >= size)
                current += size;    
            else{ 
                size = (int)(fileLength - current); 
                current = fileLength;
            } 
            contents = new byte[size]; 
            bis.read(contents, 0, size); 
            os.write(contents);
           // System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
        }   
        os.flush();
        os.close();
        this.socket.close();
        //File transfer done. 
        
        System.out.println("File sent succesfully!");
	}

	private void receiveBackup() throws Exception {
		//Initialize the FileOutputStream to the output file's full path.
        FileOutputStream fos = new FileOutputStream("./t/teste.sql");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = socket.getInputStream();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //No of bytes read in one read() call
        int bytesRead = 0;

		System.out.println("Starting Backup protocol");
        while((bytesRead = is.read()) != -1){
        	baos.write(bytesRead);
        }

        byte[] data = baos.toByteArray();
        bos.write(data);
        
        bos.flush(); 
        baos.close();
        this.socket.close();
        System.out.println("File saved successfully!");
	}

}