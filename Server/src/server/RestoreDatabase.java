package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RestoreDatabase {

	public static void databaseRestore() throws IOException, InterruptedException
	{
		List<String> cmds = new ArrayList<String> ();
		cmds.add("C:\\Program Files (x86)\\PostgreSQL\\9.5\\bin\\pg_restore.exe");
		cmds.add("-h");
		cmds.add("localhost");
		cmds.add("-p");
		cmds.add("5432");
		cmds.add("-U");
		cmds.add("postgres");    // username do postgres
		cmds.add("-d");	
		cmds.add("postgres");		// nome da database original		
		cmds.add("-v");
		cmds.add("C:\\Users\\Leonardo\\Desktop\\test\\dbm.sql");   // nome do file output após backup

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.environment().put("PGPASSWORD", "social21"); //password do postgres

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

		System.out.println("Restore bem sucedido.");

	}

	public static void main(String[] args) {  
		try {
			databaseRestore();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	} 
}
