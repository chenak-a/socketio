import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

class Utils {

  public static Scanner input;

  public Utils() {
    input = new Scanner(System.in);
  }

  public String getIp() throws IOException {
    String ip = "";
    Boolean invalidIP = true;
    do{
    	try {
    	      System.out.print("Enter your IP Address (ex:127.0.0.1) : ");
    	      ip = input.next();
    	      // Split each 4 bytes in list of 1 byte.
    	      String[] bytes = ip.split("\\.");
    	      
    	      // Verify that the IP it has the right length
    	      if (bytes.length != 4) { 
    	        throw new IllegalArgumentException("There are less than or greater than 4 octets");
    	      } else {
    	        for (String byte_ : bytes) {
    	        	// Verify that the octets of IP is valid
    	          if (Integer.valueOf(byte_) > 255 || Integer.valueOf(byte_) < 0) {
    	        	
    	            throw new IllegalArgumentException("Your number is either negative or above 255.");
    	          }
    	        }
    	        // If the IP is valid stop the while loop
    	        invalidIP = false;
    	      }
    	    } catch (NumberFormatException N) {
    	      System.out.println("Invalid IP Address! A given input isn't a number.");
    	    } catch (Exception e) {
    	      System.out.println("Invalid IP Address! - " + e.getMessage());
    	    }
    }while(invalidIP);
   
    return ip;
  }

  public int getPort() {
	int port = 0;
    Boolean invalidPort = true;
    do{
	    try {
		    System.out.print("Enter a Port number that is between 5000 and 5050 (ex:5049): ");
		    // Get the port number (input) from the user
		    port = Integer.parseInt(input.next());
		    // Make sure the number is valid
		    if (port < 5000 || port > 5050) {
		    	throw new IllegalArgumentException("Your Port number is either less than 5000 or greater than 5050.");
		      }
		    // If the number is valid stop the while loop
		    invalidPort = false;
		    } catch (NumberFormatException N) {
	    	   System.out.println("Invalid Port number! A given input isn't a number.");
	    	} catch (Exception e) {
		      System.out.println("Invalid Port number! - " + e.getMessage());
		      
	    }
    }while(invalidPort);
    return port;
  }

  public void sendfile(Socket socket, String path, String fileName)
    throws IOException {
    DataOutput out = new DataOutputStream(socket.getOutputStream());

    // Open file
    File file = new File(path + "\\" + fileName);
    Boolean fileExist = file.isFile();
    out.writeBoolean(fileExist);

    if (fileExist) {
      // Size of file
      int filelength = (int) file.length();

      // Read file as bytes
      BufferedInputStream buffer = new BufferedInputStream(
        new FileInputStream(file)
      );

      // Create list of byte to put file in
      byte[] bufferList = new byte[filelength];

      // Fill Buffer list with the file bytes
      int bufferlenght = buffer.read(bufferList, 0, filelength);
      out.writeUTF(fileName);
      // Send length of list
      out.writeInt(bufferlenght);
      // Send byte
      out.write(bufferList, 0, bufferlenght);

      buffer.close();
    } else {
      throw new IllegalArgumentException("This file does not exist.");
    }
  }

  public void getfile(Socket socket, String path) throws IOException {
	// Create a new input stream
    DataInputStream in = new DataInputStream(socket.getInputStream());
    
    // Receive the response to verify if the file exists
    if (in.readBoolean()) {
    	// Receive the file name
      String fileName = in.readUTF();

     // receive the file size
      int sizefile = in.readInt();
      // Create a buffer with the size of the file
      byte[] bufferList = new byte[sizefile];
      
      // Receive the data (bits) of the file
      in.readFully(bufferList, 0, sizefile);
      
      // Create a new output stream for the file
      OutputStream os = new FileOutputStream(path + "\\" + fileName);
      
      // Write in the file
      os.write(bufferList);
      os.close();
    } else {
      throw new IllegalArgumentException("This file does not exist.");
    }
  }

  public String[] getkey(String word) {
	  // Split the string into 2 words and put it in a list
    String[] splitStr = word.split("\\s+", 2);

    return splitStr;
  }
}
