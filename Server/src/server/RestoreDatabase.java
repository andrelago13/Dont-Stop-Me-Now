package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RestoreDatabase {

	public void databaseRestore()
	{
		List<String> cmds = new ArrayList<String> ();
		cmds.add("C:\\Program Files (x86)\\PostgreSQL\\9.5\bin\\pg_restore.exe");
		cmds.add("-i");
		cmds.add("-h");
		cmds.add("localhost");
		cmds.add("-p");
		cmds.add("5432");
		cmds.add("-U");
		cmds.add("postgres");    // username do postgres
		cmds.add("-d");	
		cmds.add("db.sql");		// nome da database original		
		cmds.add("-v");
		cmds.add("C:\\dbbackup.sql");   // nome do file output após backup

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

			System.out.println("Restore bem sucedido.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
