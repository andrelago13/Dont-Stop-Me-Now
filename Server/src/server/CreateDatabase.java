package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CreateDatabase {

	public static void databaseCreate(String pathFile, String databaseName, String password) throws IOException, InterruptedException
	{
		List<String> cmds = new ArrayList<String> ();
		cmds.add(pathFile);
		cmds.add("-h");
		cmds.add("localhost");
		cmds.add("-p");
		cmds.add("5432");
		cmds.add("-U");
		cmds.add("postgres");  
		cmds.add(databaseName);

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.environment().put("PGPASSWORD", password); //password do postgres
		
		long startTime = System.currentTimeMillis();
		try { 
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
		} catch (IOException e) {      
			e.printStackTrace();      
		} catch (InterruptedException ie) {      
			ie.printStackTrace();      
		}        

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		System.out.println("Time elapsed -> " + elapsedTime + "ms" );
		System.out.println("Create bem sucedido.");

	}
	public static void main(String[] args) {  
		try {
			databaseCreate("C://Program Files (x86)//PostgreSQL//9.5//bin//createdb.exe", "DataBaseRestore", "123456");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  
	} 
}
