package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BackupDatabase {

	public void databaseBackup()
	{
		List<String> cmds = new ArrayList<String> ();
		cmds.add("C:\\Program Files (x86)\\PostgreSQL\\9.5\bin\\pg_dump.exe");
		cmds.add("-i");
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
		cmds.add("C:\\dbbackup.sql");   // nome do file output após backup
		cmds.add("db.sql"); //path da db a ser feito o backup

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.environment().put("PGPASSWORD", "pass"); //password do postgres

		try {
			Process process = pb.start();
			// Para analisar os possiveis erros que possam aparecer
			final BufferedReader buf = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = buf.readLine();    

			while (line != null) {    
				System.err.println(line);    
				line = buf.readLine(); 
			}    
			buf.close();
			process.waitFor();
			process.destroy();

			System.out.println("Backup bem sucedido.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
