package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

public class Client {
	public static void main(String[] args){ 	// Client start at here
		//new Client("https://gurokami.no-ip.org",48763);
		new Client(48763);
	}
	public Client(String hostname,int port){	// connect with URL
		try {
			Socket socket = new Socket(InetAddress.getByName(new URL(hostname).getHost()).getHostAddress(),port);
			ClientThread client = new ClientThread(socket);
			GUI gui = new GUI(client);
			client.setGUI(gui);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
	public Client(int port){					// use local host
		try {
			Socket socket = new Socket("127.0.0.1",port);
			ClientThread client = new ClientThread(socket);
			GUI gui = new GUI(client);
			client.setGUI(gui);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
}

class ClientThread implements Runnable {
	
	int	status = 0;
	GUI gui = null;
	Socket	socket = null;
	StringTokenizer token = null;
	InputStream		inputstream = null;
	OutputStream	outputstream = null;
	
	public ClientThread(Socket Sc){
		try {
			socket = Sc;
			inputstream = socket.getInputStream();
			outputstream = socket.getOutputStream();
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		while(status!=-1){
			String receive = receive();					// Receive message from Server
			token = new StringTokenizer(receive,"|");	// Format the message [Command]|[arg0]|[arg1]|[arg2]...
			String command = token.nextToken();			// [Command]
			if(command.equals("End")) End();
			
			if(status==0){		// state Connected
				if(command.equals("Success")){ status++; gui.Login(); System.out.println("Login Success!"); }
			}
			else if(status==1){	// state Login
				if(command.equals("Logout!")){ status--; gui.Logout(); }
				else if(command.equals("Success")){ status++; gui.Wait(); }
				else if(command.equals("Create")){ gui.Room(command,token); }
				else if(command.equals("Join")){ gui.Room(command,token); }
				else if(command.equals("Room")){ gui.Room(command,token); }
			}
			else if(status==2){	// state Waiting
				if(command.equals("Start")){ status++; gui.Play(Integer.parseInt(token.nextToken()));}
			}
			else if(status==3){	// state Playing
				
			}
		}
		
		try { socket.close(); System.out.println("Client Thread End"); }
		catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}

	public void setGUI(GUI gui){
		this.gui = gui; 
	}
	public void send(String message){ // Send message to Server
		try {
			outputstream.write(message.getBytes());
			System.out.println("Client Send: "+message);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); status = -1; }
	}
	public void End(){ // End this Thread
		gui.End();
		status = -1;
		send("End");
	}
	private String receive(){ // Receive message from Server
		String receive = null;
		try {	
			byte[] buffer = new byte[1000];
			inputstream.read(buffer); receive = new String(buffer).trim();
			System.out.println("Client Recv: "+receive);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); status = -1; }
		return receive;
	}
}

