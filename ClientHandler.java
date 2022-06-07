import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ClientHandler extends Thread {

  private Socket socket;

  private String directory;
  private Utils util;
  private DataOutput dataOut;
  private HashMap<String, Consumer<String[]>> command;

  public ClientHandler(Socket socket, Utils util) throws IOException {
    this.socket = socket;
    this.dataOut = new DataOutputStream(socket.getOutputStream());
    this.directory = System.getProperty("user.dir");
    this.util = util;
    
    command = new HashMap<String, Consumer<String[]>>();
    
    command.put(
      "cd",
      cmd -> {
        // Run command line
        try {
        	// Verify that the directory's name was given
            if (cmd.length == 2) {
            // Create a new file
              File newdir = new File(this.directory + "//" + cmd[1]);
              // Verify if the directory exists
              Boolean dirExsiste = newdir.exists();
              // Send the confirmation that the directory exists
              dataOut.writeBoolean(dirExsiste);
              if (dirExsiste) {
            	  // New directory name
                String newDirectery = newdir.toString();
                if (cmd[1].compareTo("..") == 0) {
                	// Get the parent name of the current directory
                  newDirectery = Paths.get(this.directory).getParent().toString();
                } else if(cmd[1].compareTo(".") == 0){
                	// Keep same path name
                	newDirectery = this.directory;
                }
                // Update path name
                this.directory = newDirectery;
                // Send the new path name
                dataOut.writeUTF(newDirectery);
              }
            } else {
            	// Send boolean that there is no arguments
            	dataOut.writeBoolean(false);
            }
        
        } catch (IOException e) {
          // Auto-generated catch block
          e.printStackTrace();
        }
      }
    );
    
    command.put(
      "ls",
      cmd -> {
        try {
        	// Verify that no argument was given 
        	if (cmd.length == 1){
        		// Create a String containing the list of all files and directories
                String Filelist = Stream
                  .of(new File(this.directory).listFiles())
                  .map(File::getName)
                  .collect(Collectors.joining("\n"));
                // Send the String containing the list
            	dataOut.writeUTF(Filelist);
        	} else{
        		// Help user to find the command
        		dataOut.writeUTF("Do you mean 'ls' ?");
        	}
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
 
      }
    );
    
    command.put(
      "mkdir",
      cmd -> {
        try {
        	String message = "No argument was given.";
        	// Verify that the directory name is given
            if (cmd.length == 2) {
            	// Check if the directory exists
                File newdir = new File(this.directory + "//" + cmd[1]);
                Boolean fileDontExist = !newdir.exists();
                
                if (fileDontExist) {
                	// Create a directory
                  Files.createDirectory(newdir.toPath());
           
                }
                // Create the message
                message = (fileDontExist) ? "This directory " + cmd[1] + " has been created" : "This file already exists" ;
                
              }
            // Send the message
            this.dataOut.writeUTF(message);
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
      }
    );
    
    command.put(
      "upload",
      cmd -> {
        try {
          // Receive file from user
          if (cmd.length == 2) {
            // Protocol to receive file
            this.util.getfile(socket, this.directory);
          }
        }catch (IllegalArgumentException e) {} 
        catch (IOException e) {
        	// Add to logs of error
        	e.printStackTrace();
        }
      }
    );
    
    command.put(
      "download",
      cmd -> {
        try {
          if (cmd.length == 2) {
            // Protocol to send file
            this.util.sendfile(socket, this.directory, cmd[1]);
          }
        } catch (IllegalArgumentException e) {} 
        catch (IOException e) {
	        // Auto-generated catch block
	        e.printStackTrace();
        }
      }
    );
    
    command.put("exit", cmd -> { /* Could send the entire activity of the user (warnings) to database */ } ); 
  }




  public void logs(String input) {
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd@HH:mm:ss"
    );
    String formattedDate = myDateObj.format(myFormatObj);
    System.out.println(
      "[" +
      socket.getInetAddress().toString().replace("/", "") +
      ":" +
      socket.getPort() +
      "-" +
      formattedDate +
      "]:" +
      input
    );
  }

  public void run() {
    try {
    	
      String userInput = "";
      
      do {
    	  
        DataInputStream in = new DataInputStream(socket.getInputStream());
        userInput = in.readUTF();
        logs(userInput);
        String[] key = util.getkey(userInput);
        command.get(key[0]).accept(key);
      } while (userInput.compareTo("exit") != 0);
      
    } catch (SocketException e) {}
    catch (IOException e) {
    	// Auto-generated catch block
    	e.printStackTrace();
    	
    } finally {
      try {
    	  socket.close();
      } catch (IOException e) {
    	// Auto-generated catch block
    	e.printStackTrace();
      }
      
    }
    
  }
  
}
