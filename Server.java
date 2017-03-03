import java.io.*;
import java.net.InetAddress;

/*
 * <srvc_port> is the port number where the server provides the service
 * <mcast_addr> is the IP address of the multicast group used by the server to advertise its service.
 * <mcast_port> is the multicast group port number used by the server to advertise its service.
*/

public class Server {
    public static void main(String[] args) throws IOException {
    	
    	if (args.length != 3) {
    		System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
    		return;
    	}
    	
    	int srvc_port = Integer.parseInt(args[0]);
    	InetAddress mcast_addr = InetAddress.getByName(args[1]);
    	int mcast_port = Integer.parseInt(args[2]);
        new ServerThread(srvc_port, mcast_addr, mcast_port).start();
    }
}