package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {
	
	private ArrayList<ServerThread> threadlist = new ArrayList<ServerThread>();
	private ArrayList<Room>	roomlist = new ArrayList<Room>();
	
	public static void main(String[] args){
		
		ServerSocket serversocket = null;
		ServerThread serverthread = null;
		Server server = new Server();
		boolean process = true;
		int port = 48763;
		
		try{
			while(process){
				serversocket = new ServerSocket(port);
				serverthread = new ServerThread(serversocket.accept(),server);
				server.addThread(serverthread);
				serversocket.close();
			}
		}catch (IOException e){ System.out.println("Server: "+ e.toString()); }
	}
	
	public void addThread(ServerThread serverthread){
		threadlist.add(serverthread);
	}
	
	public void endThread(){
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).status==-1){
				threadlist.remove(i);
				System.out.println("Remove "+i+" in "+threadlist.size()+" Thread");
			}
		}
	}
	
	public void createRoom(ServerThread serverthread){ // add new room to Room list
		updateRoom(); // notify all thread that new Room is created
	}
	
	public void updateRoom(){ // notify all thread that is logged in
	}
}

class Room {

}

class ServerThread implements Runnable {
	
	private Server server	= null;
	private Socket socket	= null;
	private String account	= null;
	private MySQL database	= null;
	private StringTokenizer token		= null;
	private InputStream inputstream		= null;
	private OutputStream outputstream	= null;
	
	public int status; // Thread status 0=connected 1=log in 2=
	
	public ServerThread(Socket sc,Server sv) {
		try {
			status = 0;
			socket = sc;
			account = "Guest";
			database = new MySQL();
			inputstream = sc.getInputStream();
			outputstream = sc.getOutputStream();
		}catch (IOException e){ System.out.println("Server: "+ e.toString()); }
		
		Thread thread = new Thread(this); thread.start();
		System.out.println("ServerThread Created!");
	}
	
	public void run() {
		while(status!=-1){
			Boolean result = false;
			String receive = receive();					// Receive message from Client [ Blocked ]
			token = new StringTokenizer(receive,"|");	// Format the message [Command]|[arg0]|[arg1]|[arg2]...
			String command = token.nextToken();			// [Command]

			if(command.equals("End")) End();			// Client Side Close Program
			else System.out.println("[Command] = "+ command);
	
			if(status==0){
				account = token.nextToken();
				System.out.println("[Account] = "+ account);
				if(command.equals("Login")) result = Login();
				else if(command.equals("Register")) result = Register();
				
				if(result==true){ send("Success!"); status++; result = false; }
			}
			else if(status==1){
				if(command.equals("Logout")){ status--; send("Logout!"); }
			}
		}
		
		try { socket.close(); System.out.println("Server Thread ["+ account +"] End"); }
		catch (IOException e){ System.out.println("Server: "+e.toString()); }
	}
	
	public void End(){ // End this Thread
		database.End();
		status = -1;
		send("End");
	}
	public void send(String message){ // Send message to Client
		System.out.println("Server Send: "+ message);
		try { outputstream.write(message.getBytes()); }
		catch (IOException e){ System.out.println("Server: "+ e.toString()); status = -1; }
	}
	private String receive(){ // Receive message from Client
		String receive = null;
		try {
			byte[] buffer = new byte[1000];
			inputstream.read(buffer); receive = new String(buffer).trim();
			System.out.println("Server Recv: "+ receive);
		} catch (IOException e){ System.out.println("Server: "+ e.toString()); status = -1; }
		return receive;
	}

	public boolean Login() { // Login with User_account and User_password
		Boolean result = false;
		if(database.checkaccount(account)>0){
			if(database.login(account,token.nextToken())) result = true;
		}
		return result;
	}
	public boolean Register(){ // Register with User_account and User_password
		Boolean result = false;
		if(database.checkaccount(account)==0){
			if(database.register(account,token.nextToken())) result = true; 
		}
		return result;
	}
}