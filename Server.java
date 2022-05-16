import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
public class Server {
    private static ServerSocket listener;
    public static void main(String[] args) throws Exception{
    	//"192.168.120.1"
    	try {
    		
	    	//Number of client connected
	      
	        Utils utils = new Utils();  
			//IP address
			String serverAddress = utils.getIp();
			// Server Port
			int serverPort = utils.getPort();
	       
	        //Create connection between client and server
	        listener = new ServerSocket();
	        listener.setReuseAddress(true);
	        InetAddress serverIp = InetAddress.getByName(serverAddress);
	        //assign to listener the IP address to listen to and port
	        listener.bind(new InetSocketAddress(serverIp,serverPort));
	        System.out.println("Server is running : at " +serverAddress + " Port " +serverPort );
	        try{
	            while (true){
	                new ClientHandler(listener.accept(),utils).start();
	            }
	        }finally {
	            listener.close();
	        }
    	}catch(Exception e) {
				System.out.println("Error exit");
		}
    }
}
