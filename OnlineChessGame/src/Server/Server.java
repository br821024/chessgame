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
	
	public static void main(String[] args){				// Server Start at here
		
		ServerSocket 	serversocket 	= null;
		ServerThread 	serverthread 	= null;
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
	
	public void addThread(ServerThread serverthread){	// add thread to thread list
		threadlist.add(serverthread);
	}
	public void endThread(){							// remove thread from thread list
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).status==-1){
				threadlist.remove(i);
				System.out.println("Remove "+i+"th Thread in "+threadlist.size()+" Thread");
			}
		}
	}
	public void initialRoom(ServerThread user){
		for(int i=0;i<roomlist.size();i++){
			user.send(roomlist.get(i).getRoomInfo());
		}
	}
	public void updateRoom(String action,String account,String username,int side){
		String result = "";
		if(action.equals("Create")) result = "Create|"+account+"|"+username+"|"+side;
		else if(action.equals("Join")) result = "Join|"+account+"|"+username+"|"+side;
		else if(action.equals("Quit")) result = "Quit|"+account;
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).status==1) threadlist.get(i).send(result);
		}
	}
	public void joinRoom(String account,ServerThread user,String username,int side){
		getRoom(account).join(user,username,side);
	}
	public void addRoom(String account,ServerThread host,String username,int side){
		roomlist.add(new Room(this,host,account,username,side));
		updateRoom("Create",account,username,side);
	}
	public Room getRoom(String account){				// get room from room list
		Room target = null;
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).account.equals(account)) target = roomlist.get(i);
		}
		return target;
	}
	public void deleteRoom(String account){				// delete room in room list
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).account.equals(account)){
				roomlist.remove(i);
				updateRoom("Quit",account,"",-1);
			}
		}
	}
}

class Room {

	private Server 			server 	= null;
	private ServerThread 	host 	= null;
	private ServerThread 	player 	= null;

	public int side = -1;	// Side = 0 White = 1 Black = 2 Don't mind
	public String account = null;
	
	public Room(Server server,ServerThread host,String account,String username,int side){ 
		this.host 		= host;
		this.side 		= side;
		this.server 	= server;
		this.account	= account;
	}
	public void join(ServerThread playerthread,String username,int side){	// user join the room 
		if(player==null){
			player = playerthread;
			if(this.side==2){
				if(side==2) this.side = (int)Math.random()*100%2;
				else this.side = 1-side;
			}
			host.send("Start|"+this.side);			// match start
			player.send("Success");
			player.send("Start|"+(1-this.side));	// match start
			server.updateRoom("Join",account,username,(1-this.side));
		}		
	}
	public String getRoomInfo(){	// get room information to String
		String result = "Room|"+account+"|"+host.getname()+"|"+side+"|";
		if(player!=null) result = result+player.getname();
		return result;
	}
}

class ServerThread implements Runnable {
	
	private Server 	server		= null;
	private Socket 	socket		= null;
	private String 	account		= null;
	private MySQL 	database	= null;
	
	private StringTokenizer token			= null;
	private InputStream 	inputstream		= null;
	private OutputStream 	outputstream	= null;
	
	public int status;		// Thread status 0=connected 1=log in 2=
	
	public ServerThread(Socket sc,Server sv) {
		try {
			status = 0;
			socket = sc;
			server = sv;
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
	
			if(status==0){			// state Connected
				account = token.nextToken();
				if(command.equals("Login")) result = Login();
				else if(command.equals("Register")) result = Register();
				
				if(result==true){ send("Success"); status++; result=false; server.initialRoom(this); }
			}
			else if(status==1){		// state Login
				if(command.equals("Logout")){ status--; send("Logout!"); }
				else if(command.equals("Create")){ status++;
					Create(getname(),Integer.parseInt(token.nextToken())); 
				}
				else if(command.equals("Join")){ status++; 
					Join(token.nextToken(),getname(),Integer.parseInt(token.nextToken()));
				}
			}
			else if(status==2){		// state Waiting
				if(command.equals("Quit")){ status--; send("Success"); server.deleteRoom(account); }
			}
			else if(status==3){		// state Playing
				
			}
		}
		
		try { socket.close(); System.out.println("Server Thread ["+ account +"] End"); }
		catch (IOException e){ System.out.println("Server: "+e.toString()); }
		
		server.endThread();
	}
	
	private void End(){ 		// End this Thread
		database.End();
		status = -1;
		send("End");
	}
	private String receive(){	// Receive message from Client
		String receive = null;
		try {
			byte[] buffer = new byte[1000];
			inputstream.read(buffer); receive = new String(buffer).trim();
			System.out.println("Server Recv: "+ receive);
		} catch (IOException e){ System.out.println("Server: "+ e.toString()); status = -1; }
		return receive;
	}
	private boolean Login() {	// Login with User_account and User_password
		Boolean result = false;
		if(database.checkaccount(account)>0){
			if(database.login(account,token.nextToken())) result = true;
		}
		return result;
	}
	private boolean Register(){ // Register with User_account and User_password
		Boolean result = false;
		if(database.checkaccount(account)==0){
			if(database.register(account,token.nextToken())) result = true; 
		}
		return result;
	}
	private void Create(String username,int side){
		server.addRoom(account,this,username,side);
		send("Success");
	}
	private void Join(String account,String username,int side){
		server.joinRoom(account,this,username,side);
	}
	public String getname(){			// get name from database
		String result = database.getUserName(account);
		return result;
	}
	public void send(String message){	// Send message to Client
		System.out.println("Server Send: "+ message);
		try { outputstream.write(message.getBytes()); }
		catch (IOException e){ System.out.println("Server: "+ e.toString()); status = -1; }
	}
}