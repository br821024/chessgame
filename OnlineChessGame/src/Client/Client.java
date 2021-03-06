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
		//Client client = new Client("https://gurokami.no-ip.org",48763);
		Client client = new Client(48763);
	}
	
	/* Constructor */
	public Client(String hostname,int port){	// set hostname to connect
		try {
			Socket socket = new Socket(InetAddress.getByName(new URL(hostname).getHost()).getHostAddress(),port);
			ClientThread controller = new ClientThread(socket);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
	public Client(int port){					// use localhost
		try {
			Socket socket = new Socket("127.0.0.1",port);
			ClientThread controller = new ClientThread(socket);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
}

class ClientThread implements Runnable,EndThread {
	
	/* Custom type variable */
	private GUI 			gui 	= new GUI(this);
	
	/* Default type variable */
	private String			account			= null;
	private Socket			socket 			= null;
	private StringTokenizer token 			= null;
	private InputStream		inputstream 	= null;
	private OutputStream	outputstream 	= null;
	
	public State state = State.CONNECT;
	
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
		while(state != State.END){
			
			/* Recive messgae from Server */
			String receive = receive();
			
			/* Format the message */
			token = new StringTokenizer(receive, Constant.DELIMITER);
			String command = token.nextToken(); // read Server's command
			
			if(command.equals("End")){ // handle End command
				setState(State.END);
			}
			
			if(state == State.CONNECT){
				
				System.out.println("State: Connected");

				if(command.equals("Success")){
					
					System.out.println("Login Success!");
					
					setState(State.LOGIN);	// State Changed
				}
			}
			else if(state == State.LOGIN){
				
				System.out.println("State: Login");
				
				if(command.equals("Done")){
					setState(State.CONNECT);
				} /* Room command handle by GUI */
				else if(command.equals("Create")){ gui.createRoom(token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken())); }
				else if(command.equals("Delete")){ gui.deleteRoom(token.nextToken()); }
				else if(command.equals("Join")){ gui.joinRoom(token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken())); }
				else if(command.equals("Quit")){ gui.quitRoom(token.nextToken(),Integer.parseInt(token.nextToken())); }
				else if(command.equals("Room")){
					String header = command;
					
					while(header.equals("Room")){
						String account = token.nextToken();
						String hostname = token.nextToken();
						int side = Integer.parseInt(token.nextToken());
						String player = token.nextToken();
						gui.createRoom(account, hostname, side);
						if(!player.equals("null")){
							gui.joinRoom(account, player, side*(-1));
						}
						if(token.hasMoreTokens()){ header = token.nextToken(); }
						else { header = ""; }
					}
				}
				else if(command.equals("Message")){
					gui.showMessage(token.nextToken());
				}
				else if(command.equals("AddUser")){
					String account = token.nextToken();
					gui.addUser(account);
				}
				else if(command.equals("DeleteUser")){
					gui.deleteUser(token.nextToken());
				}
				else if(command.equals("User")){
					String header = command;
					
					while(header.equals("User")){
						String account = token.nextToken();
						String hostname = token.nextToken();
						gui.addUser(account);
						if(token.hasMoreTokens()){ header = token.nextToken(); }
						else { header = ""; }
					}
					send("Initial|2");
				}
				else if(command.equals("Success")){
					setState(State.WAITING);
				}
			}
			else if(state == State.WAITING){
				
				System.out.println("State: Waiting");
				
				if(command.equals("Done")){
					setState(State.LOGIN);
				}
				else if(command.equals("Start")){
					setState(State.PLAYING, Integer.parseInt(token.nextToken()));
				}
			}
			else if(state == State.PLAYING){
				System.out.println("State: Playing");
				if(command.equals("Done")){
					setState(State.LOGIN);
				}
				else if(command.equals("Move")){
					gui.moveChess(	Integer.parseInt(token.nextToken()),
									Integer.parseInt(token.nextToken()),
									Integer.parseInt(token.nextToken()),
									Integer.parseInt(token.nextToken()),
									Integer.parseInt(token.nextToken()));
				}
				else if(command.equals("Win")){
					gui.Endgame(Integer.parseInt(token.nextToken()));
				}
			}
		}
		
		try { socket.close(); System.out.println("Client Thread End"); }
		catch (IOException e){ System.out.println("Client: "+ e.toString()); }
	}
	public String getAccount(){
		return new String(account);
	}
	public void setAccount(String account){
		this.account = account;
	}
	public void setState(State nextstate, int... args){	// switch from state to nextstate
		
		/* Switch State */
		State prestate = state;
		state = nextstate;
		
		/* notice GUI to hide/show windows */
		switch(nextstate){
			case END:
				gui.setState(prestate, nextstate);
				break;
			case CONNECT:
				gui.setState(prestate, nextstate);
				break;
			case LOGIN:
				gui.setState(prestate, nextstate);
				gui.setUITitle(account);
				send("Initial|1");
				break;
			case WAITING:
				gui.setState(prestate, nextstate);
				break;
			case PLAYING:
				gui.setState(prestate, nextstate, args[0]);
				break;
			default:
				gui.setState(prestate, nextstate);
		}
	}
	public void send(String message){		// send Message to Server
		try {
			outputstream.write(message.getBytes());
			System.out.println("Client Send: "+message);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); state = State.END; }
	}
	private String receive(){				// recieve Message from Server
		String message = null;
		try {	
			byte[] buffer = new byte[1000];
			inputstream.read(buffer);
			message = new String(buffer).trim();
			System.out.println("Client Recv: "+message);
		} catch (IOException e){ System.out.println("Client: "+ e.toString()); state = State.END; }
		return message;
	}

	public void End() {
		send("End");
	}
}

