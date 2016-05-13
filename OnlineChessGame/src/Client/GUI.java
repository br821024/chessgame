package Client;

import java.util.StringTokenizer;

import javax.swing.JFrame;

public class GUI extends JFrame {
	
	ClientThread 	controller 	= null;
	Login 			loginUI 	= null;
	Center 			centerUI 	= null;
	ChessGame		gameUI		= null;
	Loading			loadUI		= null;
	
	public GUI(ClientThread controller){
		
		this.controller = controller;
		loginUI 	= new Login(controller);
		centerUI 	= new Center(controller);
		gameUI 		= new ChessGame(controller);
		
		/*
		center.setVisible(true);
		center.addRoom(new roomButton(controller,"ABC","ABC",1));
		center.addRoom(new roomButton(controller,"123","123",0));
		center.addRoom(new roomButton(controller,"456","456",2));
		center.addRoom(new roomButton(controller,"789","789",0));
		center.addRoom(new roomButton(controller,"101","101",1));
		center.addRoom(new roomButton(controller,"112","112",2));
		center.joinRoom("ABC","DEF",0);
		center.joinRoom("123","456",1);
		center.joinRoom("456","789",0);
		*/		
	}
	
	public void Room(String action,StringTokenizer token){
		if(action.equals("Create")){ centerUI.addRoom(new roomButton(controller,token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken()))); }
		else if(action.equals("Join")){ centerUI.joinRoom(token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken())); }
		else if(action.equals("Quit")){ centerUI.deleteRoom(token.nextToken()); }
		else if(action.equals("Room")){
			String account = token.nextToken();
			String host = token.nextToken();
			roomButton room = null;
			int side = Integer.parseInt(token.nextToken());
			room = new roomButton(controller,account,host,side);
			centerUI.addRoom(room);
			if(token.hasMoreTokens()) centerUI.joinRoom(account,token.nextToken(),1-side);
		}
	}
	public void Login(){
		loginUI.setVisible(false);
		centerUI.setVisible(true);
	}
	public void Logout(){
		loginUI.setVisible(true);
		centerUI.setVisible(false);;
	}
	public void Play(int Side){
		int color = 1;
		if(Side==1) color *= -1;
		loadUI.End(); loadUI = null;
		centerUI.setVisible(false);
		gameUI.initialize(color); gameUI.setVisible(true);
	}
	public void Wait(){
		loadUI = new Loading("Waiting for Player",controller);
	}
	public void End(){
		loginUI.dispose();
		centerUI.dispose();
		if(loadUI!=null) loadUI.End();
		if(gameUI!=null) gameUI.dispose();
	}	
}

class Loading extends basicDialog implements Runnable,EndThread {
	
	private ClientThread controller = null;
	private basicLabel text = null;
	private String 	message = null;
	private boolean loading = true;
	
	public Loading(String message,ClientThread controller) {
		super("",400,300,260,120);
		text = new basicLabel(message,30,0,230,80);
		this.controller = controller;
		this.message 	= message;
		this.add(text);
		addWindowListener(new CloseListener((EndThread)this));
		setVisible(true);
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		String dots = "";
		int i = 0;
		while(loading){
			try{ Thread.sleep(700);	}catch(InterruptedException e){}
			if(i==4){ i = 0; dots = "";	}
			i++; dots += ".";
			text.setText(message+dots);
		}
		this.dispose();
	}
	public void Quit(){
		controller.send("Quit");
	}
	public void End(){
		loading = false;
	}
}