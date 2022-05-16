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
import java.util.InputMismatchException;
import java.util.Scanner;
public class Utils {
	public static Scanner input;
	public Utils() {
		input = new Scanner (System.in);
	}
	public String getIp() throws IOException  {
		String ip ="";
		try {
			
			System.out.print("Entre your localHost Ip Address (ex:127.0.0.1) : ");
			ip = input.next();
			//split each 4 Octet in list of 1 Octet
			String[] octets = ip.split("\\.");
	
			if(octets.length != 4) {
				throw new IllegalArgumentException("there are less then or greater then 4 octet");
			}else {
				for(String octet : octets ) {
					if(Integer.valueOf(octet) >255 || Integer.valueOf(octet) < 0 ) {
						throw new IllegalArgumentException("your have a negative numbre or numbre greater then 255 ");
					}
				}
			}
			
		}catch(NumberFormatException N) {
			System.out.println("Invalide Ip address"+ " error : is not Number");
			throw new IllegalArgumentException();
		}
		catch(Exception e) {
			System.out.println("Invalide Ip address"+ " error : "+e.getMessage());
			throw new IllegalArgumentException();
		}

		
		return ip;
	}
	public  int getPort()  {
		int port = 0;
	
		try {
			System.out.print("Entre a Port numbre that is between 5000 and 5050 (ex:5049): ");
			port = input.nextInt();
			if(port < 5000 || port > 5050) {
				throw new IllegalArgumentException("your have a port numbre less then 5000 or numbre greater then 5050 ");
			
			}
		}catch(InputMismatchException  N) {
			System.out.println("Invalide Port address"+ " error : is not Number");
			throw new IllegalArgumentException();
		}
		catch(Exception e) {
			System.out.println("Invalide Port address"+ " error : "+e.getMessage());
			throw new IllegalArgumentException();
		}
		
		
		return port;
	}
	
	public void sendfile(Socket socket,String path,String fileName) throws IOException {
		DataOutput out = new DataOutputStream(socket.getOutputStream());
		
		// Open file
		File file = new File(path+"\\"+fileName);
		Boolean fileExist = file.isFile();
		out.writeBoolean(fileExist);
		
		if(fileExist) {
			// size of file
			int filelength = (int) file.length();
		
			// read file as bytes
			BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(file));
			
			// create list of byte to put file in 
			byte[] bufferList = new byte[filelength] ;
		
			// fill Buffer list with the file bytes
			int bufferlenght = buffer.read(bufferList, 0, filelength);
			out.writeUTF(fileName);
			//send length of list
			out.writeInt(bufferlenght);
			//send byte 
			out.write(bufferList, 0, bufferlenght);
		
			
			buffer.close();
		}else {
		
			throw new IllegalArgumentException("file giving does not exist");
		}
	
		

	}
	public void getfile(Socket socket,String path) throws IOException {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		if(in.readBoolean()) {
		
			String fileName = in.readUTF();
			
			int sizefile = in.readInt();
			byte[] bufferList = new byte[sizefile] ;
			in.read(bufferList, 0, sizefile);
			
			OutputStream os = new FileOutputStream(path+"\\"+fileName);

	        os.write(bufferList);
	       
	        os.close();
		}else {
			throw new IllegalArgumentException("file giving does not exist");
		}
		
	
	
	}
	public String[] getkey(String word) {
		
		String[] splitStr = word.split("\\s+",2);
		
		
		return splitStr;
		
	}
	
}
