package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {
	
	/* Default type variable */
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
	
	public void broadcast(String message){
		String extendmessage = "Message"+Constant.DELIMITER+message+"\n"+Constant.DELIMITER;
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).state == State.LOGIN){
				threadlist.get(i).send(extendmessage);
			}
		}
	}
	public void PrivateMessage(String message,String account){
		String extendmessage = "Message"+Constant.DELIMITER+"[Private] "+message+"\n"+Constant.DELIMITER; 
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).state == State.LOGIN && threadlist.get(i).getaccount().equals(account)){
				threadlist.get(i).send(extendmessage);
				break;
			}
		}
	}
	public void addThread(ServerThread serverthread){	// add thread to thread list
		threadlist.add(serverthread);
	}
	public void endThread(){							// remove thread from thread list
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).state.value == State.END.value){
				threadlist.remove(i);
				System.out.println("Remove "+i+"th Thread in "+threadlist.size()+" Thread");
			}
		}
	}
	public boolean isOnline(String account){			// check if the user is online
		boolean result = false;
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).equal(account) && threadlist.get(i).state != State.CONNECT){
				result = true;
			}
		}
		return result;
	}
	public void initialUser(ServerThread user){
		System.out.println("Prepare initial message...User");
		String message = "";
		for(int i=0; i<threadlist.size(); i++){
			if(threadlist.get(i).state == State.LOGIN){
				message += threadlist.get(i).getUserInfo();
			}
		}
		user.send(message);
	}
	public void updateUser(String action,String parameter){
		String message = action+parameter;
		for(int i=0; i<threadlist.size(); i++){
			if(threadlist.get(i).state == State.LOGIN){
				threadlist.get(i).send(message);
			}
		}
	}
	public void initialRoom(ServerThread user){
		System.out.println("Prepare initial message...Room");
		String message = "";
		for(int i=0; i<roomlist.size(); i++){
			message += roomlist.get(i).getRoomInfo();
		}
		user.send(message);
	}
	public void updateRoom(String action, String account, String username, int side){
		String result = "";
		if(action.equals("Create")){
			result = "Create"+Constant.DELIMITER+
					  account+Constant.DELIMITER+
					 username+Constant.DELIMITER+
					 	 side;
		}
		else if(action.equals("Join")){
			result = "Join"+Constant.DELIMITER+
					account+Constant.DELIMITER+
				   username+Constant.DELIMITER+
				       side;
		}
		else if(action.equals("Quit")){
			result = "Quit"+Constant.DELIMITER+
					account+Constant.DELIMITER+
					   side;
		}
		else if(action.equals("Delete")){
			result = "Delete"+Constant.DELIMITER+
					  account+Constant.DELIMITER;
		}
		for(int i=0;i<threadlist.size();i++){
			if(threadlist.get(i).state == State.LOGIN){
				threadlist.get(i).send(result);
			}
		}
	}
	public Room joinRoom(String host_account, ServerThread user, int side){
		Room room = getRoom(host_account).join(user, side);
		return room;
	}
	public Room addRoom(ServerThread host, int side){
		Room room = new Room(this, host, side);
		roomlist.add(room);
		updateRoom("Create", host.getaccount(), host.getname(), side);
		return room;
	}
	public Room getRoom(String account){				// get room from room list
		Room target = null;
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).account.equals(account)){
				target = roomlist.get(i);
			}
		}
		return target;
	}
	public void deleteRoom(String account){
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).account.equals(account)){
				roomlist.remove(i);
			}
		}
		updateRoom("Delete", account, "", Constant.RANDOM);
	}
}

class Room {

	/* Custom type variable */
	private MySQL			database	= null;
	private Server 			server 		= null;
	private ServerThread 	host 		= null;
	private ServerThread 	player 		= null;

	/* Default type variable */
	private ArrayList<ServerThread> observer = new ArrayList<ServerThread>();
	private boolean process = false;
	private String 	record 	= "";
	
	public int 		side 	= Constant.OBSERVER;
	public String 	account = null;
	
	public Room(Server server, ServerThread hostthread, int side){ 
		database		= new MySQL();
		this.host 		= hostthread;
		this.side 		= side;
		this.server 	= server;
		this.account	= host.getaccount();
	}
	public synchronized Room join(ServerThread playerthread, int side){	// user join the room 
		if(player == null){
			player = playerthread;
			/* Case: Random */
			if(this.side == Constant.RANDOM){				// host choose Random side
				if(side == Constant.RANDOM){				// player choose Random too
					int randomside[] = {Constant.BLACK,Constant.WHITE}; // get a random side
					int random = (int)Math.random()*100%2;
					this.side = randomside[random];
				}
				else{										// player pick up a side
					this.side = side*(-1);					// host become the opposite side
				}
			}
			host.setState(State.PLAYING);
			player.send("Success");
			player.setState(State.PLAYING);
			start();
			server.updateRoom("Join",account,playerthread.getname(),this.side*(-1));
			return this;
		}
		else{
			observer.add(playerthread);
			playerthread.send("Success");
			playerthread.send("Start"+Constant.DELIMITER+Constant.OBSERVER);
			System.out.println(record);
			for(int i=0;i<record.length();i=i+4){
				
				try { Thread.sleep(150); }
				catch (InterruptedException e) { e.printStackTrace(); }
				
				int y = 0;
				if((i+4)>record.length()){
					y = Integer.parseInt(record.substring(i+3));
				}
				else{
					y = Integer.parseInt(record.substring(i+3,i+4));
				}
				playerthread.send("Move"+Constant.DELIMITER+Integer.parseInt(record.substring(i,i+1))
										+Constant.DELIMITER+Integer.parseInt(record.substring(i+1,i+2))
										+Constant.DELIMITER+Integer.parseInt(record.substring(i+2,i+3))
										+Constant.DELIMITER+y);
			}
		}
		return null;
	}
	public void Endgame(ServerThread winner){
		String message = "Win" + Constant.DELIMITER;
		if(host.getaccount() == winner.getaccount()){
			message += side;
		}
		else{
			message += side*(-1);
		}
		if(host.state == State.PLAYING){
			host.send(message);
		}
		if(player.state == State.PLAYING){
			player.send(message);
		}
		for(int i=0;i<observer.size();i++){
			observer.get(i).send(message);
		}
		process = false;
		server.deleteRoom(account);
	}
	public void start(){
		process = true;
		host.send("Start"+Constant.DELIMITER+this.side);
		player.send("Start"+Constant.DELIMITER+this.side*(-1));
		for(int i=0;i<observer.size();i++){
			observer.get(i).send("Start"+Constant.DELIMITER+Constant.OBSERVER);
		}
	}
	public void quit(ServerThread user){
		if(host.equal(user.getaccount())){
			if(process){
				Endgame(player);
			}
			else{
				server.deleteRoom(account);
			}
		}
		else if(player.equal(user.getaccount())){
			if(process){
				Endgame(host);
			}
		}
		else server.updateRoom("Quit",account,null,Constant.OBSERVER); // observer quit #
	}
	public void Surrender(ServerThread loser){
		if(host.equal(loser.getaccount())){
			if(process){
				Endgame(player);
			}
		}
		else if(player.equal(loser.getaccount())){
			if(process){
				Endgame(host);
			}
		}
	}
	public void record(int px,int py,int x,int y){	
		System.out.println("Room: rec."+px+py+x+y);
		record = record + px + py + x + y;
	}
	public synchronized void broadcast(String message){
		if(process){
			host.send(message);
			player.send(message);
			for(int i=0;i<observer.size();i++){
				observer.get(i).send(message);
			}
		}
	}
	public String getRoomInfo(){	// get room information to String
		String result = "Room"+Constant.DELIMITER+
					   account+Constant.DELIMITER+
				host.getname()+Constant.DELIMITER+
						  side+Constant.DELIMITER;
		
		if(player != null){
			result += player.getname()+Constant.DELIMITER;
		} else {
			result += "null"+Constant.DELIMITER;
		}
		return result;
	}
}

class ServerThread implements Runnable {
	
	/* Custom type variable */
	private Server 	server		= null;
	private Socket 	socket		= null;
	private String 	account		= null;
	private MySQL 	database	= null;
	private Room	room		= null;
	
	/* Default type variable */
	private StringTokenizer token			= null;
	private InputStream 	inputstream		= null;
	private OutputStream 	outputstream	= null;
	
	public State state = State.CONNECT;
	
	/* Constructor */
	public ServerThread(Socket sc,Server sv) {
		try {
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
		while(state != State.END){
			Boolean result = false;
			
			/* Receive message from Client */
			String receive = receive();
			
			/* Format the message */
			token = new StringTokenizer(receive,Constant.DELIMITER);
			
			String command = token.nextToken();		// read Command
			if(command.equals("End")){				// handle End Command
				setState(State.END);
			}
			else System.out.println("[Command] = "+ command);
	
			if(state == State.CONNECT){				// state Connected
				
				account = token.nextToken();		// get account
				
				if(command.equals("Login")){
					result = Login();
				}
				else if(command.equals("Register")){
					result = Register();
				}
				
				if(result == true){
					send("Success");
					setState(State.LOGIN);
					result = false;
				}
			}
			else if(state == State.LOGIN){			// state Login
				if(command.equals("Logout")){
					setState(State.CONNECT);
					send("Done");
				}
				else if(command.equals("Create")){
					setState(State.WAITING);
					room = createRoom(Integer.parseInt(token.nextToken())); 
				}
				else if(command.equals("Join")){
					setState(State.WAITING);
					room = joinRoom(token.nextToken(), Integer.parseInt(token.nextToken()));
				}
				else if(command.equals("Message")){
					String message = token.nextToken();
					if(token.hasMoreTokens()){
						String target = token.nextToken();
						server.PrivateMessage(message,target);
					}else{
						server.broadcast(message);
					}
				}
				else if(command.equals("Profile")){
					String nickname = token.nextToken();
					if(!database.getUserName(account).equals(nickname)){
						database.setUserName(account,nickname);
					}
				}
				else if(command.equals("Initial")){
					int step = Integer.parseInt(token.nextToken());
					switch(step){
						case 1:
							server.initialUser(this);
							break;
						case 2:
							server.initialRoom(this);
							break;
					}
				}
			}
			else if(state == State.WAITING){		// state Waiting
				if(command.equals("Quit")){
					quitRoom();
					send("Done");
					setState(State.LOGIN);
				}
			}
			else if(state == State.PLAYING){		// state Playing
				if(command.equals("Quit")){
					setState(State.LOGIN);
				}
				else if(command.equals("Move")){
					room.broadcast(receive);
					room.record(Integer.parseInt(token.nextToken()),
								Integer.parseInt(token.nextToken()),
								Integer.parseInt(token.nextToken()),
								Integer.parseInt(token.nextToken()));
				}
				else if(command.equals("Surrender")){
					room.Surrender(this);
				}
				else if(command.equals("Win")){
					room.Endgame(this);
				}
			}
		}
		
		try { socket.close(); System.out.println("Server Thread ["+ account +"] End"); }
		catch (IOException e){ System.out.println("Server: "+e.toString()); }
		
		server.endThread();
	}
	
	public String getUserInfo(){
		String info = "";
		info += "User"+Constant.DELIMITER+
				getaccount()+Constant.DELIMITER+
				getname()+Constant.DELIMITER;
		return info;
	}
	public boolean equal(String account){
		boolean result = false;
		if(this.account.equals(account)){
			result = true;
		}
		return result;
	}
	public void setState(State nextstate){	// switch from state to nextstate
		
		/* Switch State */
		State prestate = state;
		state = nextstate;
		
		switch(prestate){
			case LOGIN:
				server.updateUser("Delete",this.getUserInfo());
				break;
			case PLAYING:
				if(nextstate != state.END){
					send("Done");
				}
				quitRoom();
		}
		
		/* notice GUI to hide/show windows */
		switch(nextstate){
			case LOGIN:
				server.updateUser("Add",this.getUserInfo());
				break;
			case END:
				End();
		}
	}
	private void End(){ 		// End this Thread
		database.End();
		send("End");
	}
	private String receive(){	// Receive message from Client
		String receive = null;
		try {
			byte[] buffer = new byte[1000];
			inputstream.read(buffer); receive = new String(buffer).trim();
			System.out.println("Server Recv: "+ receive);
		} catch (IOException e){ System.out.println("Server: "+ e.toString()); setState(State.END); }
		return receive;
	}
	private boolean Login() {	// Login with User_account and User_password
		boolean result = false;
		if(database.checkaccount(account)>0){
			if(database.login(account,token.nextToken())){
				if(!server.isOnline(account)){
					result = true;
				}
			}
		}
		return result;
	}
	private boolean Register(){ // Register with User_account and User_password
		boolean result = false;
		if(database.checkaccount(account)==0){
			if(database.register(account,token.nextToken())){
				result = true;
			}
		}
		return result;
	}
	private Room createRoom(int side){
		Room room = server.addRoom(this,side);
		send("Success");
		return room;
	}
	private Room joinRoom(String account,int side){
		Room room = server.joinRoom(account, this, side);
		return room;
	}
	private void quitRoom(){
		if(room!=null){
			room.quit(this);
		}
		room = null;
	}
	public String getaccount(){
		String result = account;
		return result;
	}
	public String getname(){			// get name from database
		String result = database.getUserName(account);
		return result;
	}
	public void send(String message){	// Send message to Client
		System.out.println("Server Send: "+ message);
		try { outputstream.write(message.getBytes()); }
		catch (IOException e){ System.out.println("Server: "+ e.toString()); setState(State.END); }
	}
}