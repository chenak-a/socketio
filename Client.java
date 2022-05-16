import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
public class Client {
	private static Socket socket;
	private static HashMap<String ,Consumer<String[]>> command;
	private static Utils utile;
	
	public static void initilzate() {
		utile = new Utils(); 
		command = new HashMap<String ,Consumer<String[]>> ();
		command.put("cd", cmd->{
		try {
			// create stream to read incoming data
			DataInputStream in = new DataInputStream(socket.getInputStream());
			// read data
			String directory = in.readUTF();
			System.out.println(directory);
			
			
		} catch (IOException e) {
			System.out.println("An error accured while executing the command");
		}
	
		});
		command.put("ls", cmd->{
		try {
			// create stream to read incoming data
			DataInputStream in = new DataInputStream(socket.getInputStream());
			// read data
			String lsOutput = in.readUTF();
			System.out.println(lsOutput);
			
		} catch (IOException e) {
			System.out.println("An error accured while executing the command");
		}
	
		});
		command.put("mkdir", cmd ->{});
		command.put("upload", cmd->{
			
		try {
			
			if(cmd.length == 2) {
				//Send file to User
				utile.sendfile(socket,System.getProperty("user.dir"),cmd[1]);
			}
			
			
		}catch (IllegalArgumentException e){
			System.out.println(e.getMessage());
			
		}
		catch (IOException e) {
			System.out.println("An error accured while upload");
		}});
		// download protocol 
		command.put("download", cmd->{
		try {
			// Receive file from user
			if(cmd.length == 2) {
				utile.getfile(socket,System.getProperty("user.dir"));
			}
		}catch (IllegalArgumentException e){
			System.out.println(e.getMessage());
			
		} catch (IOException e) {
			System.out.println("An error accured while download");
		}
			});
		// End of session
		command.put("exit", cmd ->{System.out.println("End of session");});
	}
	public static void main(String[] args) throws Exception{
		try {
			
			System.out.println("client is running");
			initilzate();
			//IP address
			String serverAddress = utile.getIp();
			// Port number
			int port = utile.getPort();
			//Create a connection to Port at the IP address
			socket = new Socket(serverAddress,port);
			//Assign a reader 
			
			
			String input = "";
			Scanner myObj;
			do {
				System.out.print("Enter command ligne : ");
				DataOutput out = new DataOutputStream(socket.getOutputStream());;
				//Create stream to read inputs
				myObj = new Scanner(System.in);
				// read input from the user
				input = myObj.nextLine();
				
			
				String[] key = utile.getkey(input);
				if(command.containsKey(key[0]) ) {
					// Create stream to send information
					out = new DataOutputStream(socket.getOutputStream());
					// Send Command line to server
					out.writeUTF(input);
					// Depending on key we use the require step to send or receive information from server
					command.get(key[0]).accept(key);
				}
			
			// if input equal 0 exit 
			}while(input.compareTo("exit") != 0);
			
			myObj.close();
			socket.close();
			
			
			
			
		}
		catch(Exception e) {
			e.getCause();
			System.out.println("Error exit");
		}

	}
}