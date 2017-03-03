import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerThread extends Thread {

	protected MulticastSocket socket = null;
	protected boolean moreMessages = true;
	private long FIVE_SECONDS = 5000;
	protected InetAddress mcast_addr = null;
	protected int mcast_port;
	protected int srvc_port;

	public ServerThread(int srvc_port, InetAddress mcast_addr, int mcast_port) throws IOException {
		this("ServerThread", srvc_port, mcast_addr, mcast_port);
	}

	public ServerThread(String name, int srvc_port, InetAddress mcast_addr, int mcast_port) throws IOException {
		super(name);
		this.mcast_addr = mcast_addr;
		this.mcast_port = mcast_port;
		this.srvc_port = srvc_port;
		socket = new MulticastSocket(srvc_port);
		socket.joinGroup(mcast_addr);
	}

	public void run() {
		
		
		//String outMessage = InetAddress.getLocalHost().getHostAddress().toString() + " " + srvc_port;
		//System.out.println(outMessage);
		byte[] autoBuffer = "hello from server".getBytes();
		DatagramPacket autoPacket = new DatagramPacket(autoBuffer, autoBuffer.length, mcast_addr, mcast_port);
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = () -> {
			try {
				socket.send(autoPacket);
				
				System.out.println("Sending multicast");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to multicast");
				e.printStackTrace();
			}
				};

		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
		
		
		
		while (moreMessages) {
			try {
				
				
							
				
				
				// create file if it doesn't exist
				File f = new File("../src/registers.txt");
				if(! f.exists())
					f.createNewFile();
				
				// prepare to write
				Writer fileWriter = new FileWriter(f, true);

				// prepare to read
				Reader reader = new FileReader(f);
				BufferedReader br = new BufferedReader(reader);
				
				// receive request
				byte[] contents = new byte[1024];
				byte[] buf = null;
				
				System.out.println("criou buf vazios");
				InetAddress hostAddr = InetAddress.getLocalHost();
				DatagramPacket receivedPacket = new DatagramPacket(contents, contents.length, hostAddr, srvc_port);
				System.out.println("criou packet");
				socket.receive(receivedPacket);
				System.out.println("recebeu conteudo");
				
				
				String received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
				
				String[] parts = received.split("_");
				String plate = null;
				String answer = null;
				StringBuilder message = new StringBuilder();
				System.out.println("vai construir msg");
				// case register
				
				
				if(parts[0].equals("register")){
					System.out.println("Registering");
					plate = parts[1];
					fileWriter.append("\n" + plate);
					fileWriter.append(" ");
					StringBuilder owner = new StringBuilder();
					for(int j = 2; j < parts.length ; j++)
					{
						owner.append(parts[j]);
						if(j < parts.length - 1)
							owner.append(" ");
					}
					fileWriter.append(owner);
					message.append(plate);
					message.append(" ");
					message.append(owner);
					answer = message.toString();
				} // case lookup
				else{ 
					System.out.println("looking up");
					plate = parts[1];
					String line;
					String information[] = null;
					while ((line = br.readLine()) != null) {
						information = line.split(" ");
						if(information[0].equals(plate)){
							System.out.println("exist");
							StringBuilder name = new StringBuilder();
							for(int i = 1 ; i < information.length ;i++){
								name.append(information[i]);
								System.out.println(information[i]);
								if(i < information.length - 1)
									name.append(" ");
							}
							answer = name.toString();
							break;
						}
					}
					br.close();
				}
				fileWriter.close();
				System.out.println("testou casos de look e register");
				
				// send the response to the client at "address" and "port"
				buf = answer.getBytes();
				
				//buf = "ola".getBytes();
				
				//InetAddress group = InetAddress.getByName("230.0.0.1");
				DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, receivedPacket.getAddress(), receivedPacket.getPort());
				//DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, mcast_addr, mcast_port);
				socket.send(sendPacket);

			
				sleep((long)(2 * FIVE_SECONDS));
				
			} 
			catch (IOException | InterruptedException e) {
				e.printStackTrace();
				moreMessages = false;
			}
		}
		
		try {
			socket.leaveGroup(mcast_addr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		socket.close();
		
	}
}
