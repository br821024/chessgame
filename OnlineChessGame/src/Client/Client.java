package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

public class Client {
	public static void main(String[] args){
		//new Client(48763);
		//new Client("https://gurokami.no-ip.org",48763);
		Client client = new Client(48763);
	}
	public Client(String hostname,int port){
		try {
			Socket socket = new Socket(InetAddress.getByName(new URL(hostname).getHost()).getHostAddress(),port);
			ClientThread controller = new ClientThread(socket);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
	public Client(int port){
		try {
			Socket socket = new Socket("127.0.0.1",port);
			ClientThread controller = new ClientThread(socket);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
}

class ClientThread implements Runnable,EndThread {
	
	GUI 			gui 	= new GUI(this);
	Socket			socket 			= null;
	StringTokenizer token 			= null;
	InputStream		inputstream 	= null;
	OutputStream	outputstream 	= null;
	
	int	status = 0;	// 0 = Connect, 1 = Login, 2 = Waiting, 3 = Playing, 4 = 
	
	public ClientThread(Socket sc){
		try {
			socket 			= sc;
			inputstream 	= sc.getInputStream();		// get InputStream
			outputstream 	= sc.getOutputStream();		// get OutputStream
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
			
			if(status==0){
				System.out.println("State: Connected");
				if(command.equals("Success")){ status++; gui.Login(); System.out.println("Login Success!"); }
			}
			else if(status==1){
				System.out.println("State: Login");
				if(command.equals("Logout!")){ status--; gui.Logout(); }
				else if(command.equals("Success")){ status++; gui.Wait(); }
				else if(command.equals("Create")){ gui.Room(command,token); }
				else if(command.equals("Join")){ gui.Room(command,token); }
				else if(command.equals("Quit")){ gui.Room(command,token); }
				else if(command.equals("Room")){ gui.Room(command,token); }
			}
			else if(status==2){
				System.out.println("State: Waiting");
				if(command.equals("Start")){ status++; gui.Play(Integer.parseInt(token.nextToken()));}
				if(command.equals("Success")){ status--; }
			}
			else if(status==3){	// state Playing
				System.out.println("State: Playing");
				if(command.equals("Move")){ gui.moveChess(	Integer.parseInt(token.nextToken()),
															Integer.parseInt(token.nextToken()),
															Integer.parseInt(token.nextToken()),
															Integer.parseInt(token.nextToken())); }
				else if(command.equals("")){}
			}
		}
		
		try { socket.close(); System.out.println("Client Thread End"); }
		catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}

	public void send(String message){
		try {
			outputstream.write(message.getBytes());
			System.out.println("Client Send: "+message);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); status = -1; }
	}
	public void End(){
		gui.End();		// End the GUI
		status = -1;	// End the ClientThread
		send("End");	// End the ServerThread
	}
	private String receive(){
		String message = null;
		try {	
			byte[] buffer = new byte[1000];
			inputstream.read(buffer); message = new String(buffer).trim();
			System.out.println("Client Recv: "+message);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); status = -1; }
		return message;
	}
}

