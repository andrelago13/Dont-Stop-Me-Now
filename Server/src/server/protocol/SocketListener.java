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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


import server.BackupDatabase;
import server.RestoreDatabase;
import server.Server;

public class SocketListener extends Thread {
	public static int BACKUP_TIMEOUT = 10000;
	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private Server server;
	private String address;
	private Integer port;
	
	public SocketListener(Server server, String address, Integer port) throws Exception {
		this.server = server;
		this.port = port;
		this.address = address;
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
			this.socket = this.serverSocket.accept();
			Thread.sleep(10000);
			this.sendBackup();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void backupHandler() {
		try {
			this.socket = new Socket(this.address, this.port);
			this.receiveBackup();
		} catch (Exception e) {
			this.setupServerSocket();
			this.server.setType(Server.Type.PRIMARY);
		}
	}

	private void sendBackup() throws Exception {
		BackupDatabase.databaseBackup("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_dump.exe","./backup.sql", "123456");
		
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
         
        long start = System.nanoTime();
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
		System.out.println("Starting Backup protocol");
		//Initialize the FileOutputStream to the output file's full path.
        FileOutputStream fos = new FileOutputStream("./t/teste.sql");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = socket.getInputStream();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //No of bytes read in one read() call
        int bytesRead = 0;
        
        while((bytesRead = is.read()) != -1)
        	baos.write(bytesRead);
        
        byte[] data = baos.toByteArray();
        bos.write(data);
        
        bos.flush(); 
        baos.close();
        this.socket.close();
        System.out.println("File saved successfully!");
        
        RestoreDatabase.databaseRestore("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_restore.exe", "DataBaseRestore", "./t/teste.sql", "123456");
	}

}