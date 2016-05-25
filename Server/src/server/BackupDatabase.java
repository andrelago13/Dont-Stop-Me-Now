package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BackupDatabase {

	public static void databaseBackup(String pathFile, String outputFile, String password) throws IOException, InterruptedException
	{
		List<String> cmds = new ArrayList<String> ();
		cmds.add(pathFile);
		cmds.add("-h");
		cmds.add("localhost");
		cmds.add("-p");
		cmds.add("5432");
		cmds.add("-U");
		cmds.add("postgres");    // username do postgres
		cmds.add("-F");    
		cmds.add("c");    
		cmds.add("-b");    
		cmds.add("-v");    
		cmds.add("-f"); 
		cmds.add(outputFile);   // nome do file output após backup
		cmds.add("postgres"); //path da db a ser feito o backup

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.environment().put("PGPASSWORD", password); //password do postgres

		Process process = pb.start();
		// Para analisar os possiveis erros que possam aparecer
		final BufferedReader buf = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line = buf.readLine();    

		while (line != null) {    
			System.out.println(line);    
			line = buf.readLine(); 
		}    
		buf.close();
		process.waitFor();
		process.destroy();

		System.out.println("Backup bem sucedido.");

	}
	public static void main(String[] args) {  
		try {
			databaseBackup("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_dump.exe","C:\\Users\\Leonardo\\Desktop\\test\\dbm.sql", "social21");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	} 
}
