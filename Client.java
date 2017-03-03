import java.io.*;
import java.net.*;
/*
 * java Client 230.0.0.1 4446 lookup 64-NS-33
 * <mcast_addr> is the IP address of the multicast group used by the server to advertise its service;
 * <mcast_port> is the port number of the multicast group used by the server to advertise its service;
 * <oper> is ''register'' or ''lookup'', depending on the operation to invoke;
 * <opnd> * is the list of operands of the specified operation:
	<plate number> <owner name>, for register;
	<plate number>, for lookup.
 */

public class Client {
	protected static String operation = null;
	public static int connect(MulticastSocket socket, byte[] contents , InetAddress mcast_addr, int mcast_port) throws IOException{
		boolean connected = false;
		DatagramPacket packet_information = new DatagramPacket(contents, contents.length, mcast_addr , mcast_port);
		int srvc_port = 0;
		InetAddress srvc_addr = null;
		int i = 0;
		while(! connected)
		{
			System.out.println(++i + "th" + " try to connect with server");
			socket.receive(packet_information);
			srvc_port = packet_information.getPort();
			srvc_addr = packet_information.getAddress();
			System.out.println("<" + mcast_addr + ">" +
					"<" + mcast_port + ">" + 
					"<" + srvc_addr.toString() + 
					">" + "<" + srvc_port + ">");
			connected = true;
		}
		return srvc_port;
	} 
	public static StringBuilder analiseArgs(MulticastSocket socket, String[] args){
		if(args.length < 4) {
			System.out.println("Usage: java Client <mcast_addr> <mcast_port> <oper> <opnd>*");
			return null;
		}
		String plate = args[3];

		StringBuilder message = new StringBuilder();
		// case register
		if(args.length == 5){
			operation = "register_";
			message.append(operation);
			message.append(plate);
			message.append("_");
			for(int i = 4; i < args.length ; i++){
				message.append(args[i]);
				if(i < args.length - 1)
					message.append("_");
			}
		}// case lookup
		else if(args.length == 4){
			operation = "lookup_";
			message.append(operation);
			message.append(plate);
		}
		else{ System.out.println("Operation invalid; only register or lookup"); socket.close();}
		return message;
	}
	public static void main(String[] args) throws IOException {

		// analise args 
		
		int mcast_port = Integer.parseInt(args[1]);
		InetAddress mcast_addr = InetAddress.getByName(args[0]);
		MulticastSocket socket = new MulticastSocket(mcast_port);
		socket.joinGroup(mcast_addr);
		StringBuilder message = analiseArgs(socket, args);

		// get information of service from the server
		byte[] contents = new byte[2048];	
		int srvc_port = connect(socket, contents, mcast_addr, mcast_port);
		System.out.println("Connected, preparing message to server");

		// prepare string and send request		
		String messageC = message.toString();
		contents = messageC.getBytes();
		DatagramPacket packet = new DatagramPacket(contents, contents.length, mcast_addr , srvc_port);
		socket.send(packet);
		System.out.println(operation.split("_")[0] + " sent");
		
		// get response
		packet = new DatagramPacket(contents, contents.length);
		socket.receive(packet);
		System.out.println("Answer to " + operation.split("_")[0]);
		// display response
		String received = new String(packet.getData(), 0, packet.getLength());
		System.out.println("Message Received " + received);
		socket.leaveGroup(mcast_addr);		socket.close();
	}
}