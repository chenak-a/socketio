import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.function.Consumer;

class ClientHandler extends Thread
{

	private Socket socket;

	private String directory;
	private Utils utile;

	private static HashMap<String ,Consumer<String[]>> command;
	public ClientHandler(Socket socket,Utils utile){
		this.socket = socket;
		this.directory = System.getProperty("user.dir");
		this.utile = utile;
		command = new HashMap<String ,Consumer<String[]>> ();
		command.put("cd", cmd ->{try {
				String cmdlane = "";
				for (String word : cmd) {
					cmdlane += word + " ";
				}
				// run command line  
				this.callCmd(cmdlane,true,true);

		} catch (IOException e) {
			System.out.println("An error accured while executing the command");
		}});
		command.put("ls", cmd ->{try {
			// run command line 
			this.callCmd("dir",false,true);
		} catch (IOException e) {
			System.out.println("An error accured while executing the command");
		}});
		command.put("mkdir", cmd ->{try {
			String cmdlane = "";
			for (String word : cmd) {
				cmdlane += word + " ";
			}
			// run command line 
			this.callCmd(cmdlane,false,false);
		} catch (IOException e) {
			System.out.println("An error accured while executing the command");
		}});
		command.put("upload", cmd->{try {
			// Receive file from user
			if(cmd.length == 2) {
				// Protocol to receive file 
				this.utile.getfile(socket,this.directory);
			}
		}catch (IllegalArgumentException e) {
			// add to logs of error
			//System.out.println("an error has occurred : "+e.getMessage());
		}
		catch (IOException e) {
			// add to logs of error
			System.out.println("an error has occurred : "+e.getMessage());
			
		}});
		command.put("download", cmd->{try {
			if(cmd.length == 2) {
				// Protocol to send file 
				this.utile.sendfile(socket, this.directory , cmd[1]);
			}
			
		} catch (IllegalArgumentException e) {
			// add to logs of error
			//System.out.println("an error has occurred : "+e.getMessage());
		}
		catch (IOException e) {
			// add to logs of error
			System.out.println("an error has occurred : "+e.getMessage());
		}});
		command.put("exit", cmd->{});
		
	}
	public void callCmd(String cmd,Boolean changedirectory,Boolean returnvalue) throws IOException  {
		
		
		try {
	
			ProcessBuilder processBuilder = new ProcessBuilder();
			if(changedirectory) {
				cmd += " && chdir";
			}
			
			processBuilder.command("cmd.exe","/c",cmd);
			processBuilder.directory(new File(this.directory));
			Process process = processBuilder.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream())) ;
            String line = ""; 
            String word = "";
            while ((line = input.readLine()) != null) { 
            
   			    if(changedirectory) {
   			    	this.directory = line;
   			    	
            	}
   			    if(!word.contains(line + "\n")) {
   			    	word += line + "\n";
   			    }
   			   
   			    
            }
	        if(returnvalue) {
	        	DataOutput out = new DataOutputStream(socket.getOutputStream());
	        	out.writeUTF(word);
	    		
            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("an error has occurred : "+e.getMessage());
		}
		

	
		
	}
	public void logs(String input) {
		LocalDateTime myDateObj = LocalDateTime.now();
		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
		String formattedDate = myDateObj.format(myFormatObj);
		System.out.println("["+socket.getInetAddress().toString().replace("/", "")+":"+socket.getPort()+"-"+formattedDate+"]:"+input);

	}
	public void run (){
		try{
			String userInput ="";
			do {
				DataInputStream in = new DataInputStream(socket.getInputStream());
				userInput = in.readUTF();
				logs(userInput);
				String[] key = utile.getkey(userInput);
				command.get(key[0]).accept(key);
			}while( userInput.compareTo("exit") != 0);
		
		}catch (IOException e){
			System.out.println("an error has occurred : "+e.getMessage());
		}finally {
			try{
				socket.close();
			}catch (IOException e){

			}
		}
	}
}


