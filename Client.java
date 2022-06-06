import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {

  private static Socket socket;
  
  private static HashMap<String, Consumer<String[]>> command;
  private static Utils util;
 
  public static void initilzate() {
    util = new Utils();
    command = new HashMap<String, Consumer<String[]>>();
    
    command.put(
      "cd",
      cmd -> {
        try {
          // Create stream to read incoming data
          DataInputStream in = new DataInputStream(socket.getInputStream());
          // Read data
          if (in.readBoolean()) {
            String directory = in.readUTF();
            System.out.println("The server is at: " + directory);
          } else {
        	  System.out.println("This directory doesn't exist");
          }
        } catch (IOException e) {
          System.out.println("An error occured while executing this command!");
        }
      }

    );
    
    command.put(
      "ls",
      cmd -> {
        try {
          // Create stream to read incoming data
          DataInputStream in = new DataInputStream(socket.getInputStream());
          // Read data
          String lsOutput = in.readUTF();
          System.out.println(lsOutput);
        } catch (IOException e) {
          System.out.println("An error occured while executing this command");
        }
      }
    );
    
    command.put("mkdir", cmd -> {
    	try {
			DataInputStream in = new DataInputStream(socket.getInputStream());
			System.out.println(in.readUTF());
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

    });
    command.put(
      "upload",
      cmd -> {
        try {
          if (cmd.length == 2) {
            //Send file to User
            util.sendfile(socket, System.getProperty("user.dir"), cmd[1]);
            System.out.println("The file " + cmd[1] + " has been uploaded successfully!");
          } else {
        	  System.out.println("A file name is required!");
          }
        } catch (IllegalArgumentException e) {
          System.out.println(e.getMessage());
        } catch (IOException e) {
          System.out.println("An error occured while uploading");
        }
      }
    );
    // Download protocol
    command.put(
      "download",
      cmd -> {
        try {
          // Receive file from user
          if (cmd.length == 2) {
            util.getfile(socket, System.getProperty("user.dir"));
            System.out.println("The file " + cmd[1] + " has been downloaded successfully!");
          } else {
        	  System.out.println("A file name is required!");
          }
        } catch (IllegalArgumentException e) {
          System.out.println(e.getMessage());
        } catch (IOException e) {
          System.out.println("An error occured while downloading");
        }
      }
    );
    // End of session
    command.put(
      "exit",
      cmd -> {
        System.out.println("End of session, you have been disconnected!");
      }
    );
  }

  public static void main(String[] args) throws Exception {
    try {
      System.out.println("Client is running");
      initilzate();
      //IP address
      String serverAddress = util.getIp();
      // Port number
      int port = util.getPort();
      // Create a connection to Port at the IP address
      socket = new Socket(serverAddress, port);
      // Assign a reader

      String input = "";
      Scanner myObj;
      do {
        System.out.print("Enter command line : ");
        DataOutput out = new DataOutputStream(socket.getOutputStream());
        //Create stream to read inputs
        myObj = new Scanner(System.in);
        // Read input from the user
        input = myObj.nextLine();
        String[] key = util.getkey(input);

        if (command.containsKey(key[0])) {
          // Create stream to send information
          out = new DataOutputStream(socket.getOutputStream());
          // Send Command line to server
          out.writeUTF(input);
          // Depending on key we use the require step to send or receive information from server
          command.get(key[0]).accept(key);
        } else {
        	System.out.println("This command doesn't exist!");
        }
        // If input equal 0, then exit
      } while (input.compareTo("exit") != 0);

      myObj.close();
      socket.close();
    } catch (Exception e) {
      e.getCause();
      System.out.println("Error while exiting");
    }
  }
}
