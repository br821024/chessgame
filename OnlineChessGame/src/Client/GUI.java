package Client;

import java.util.StringTokenizer;
import javax.swing.JFrame;

public class GUI extends JFrame {
	
	/* Custom type variable */
	private ClientThread 	controller 	= null;
	private Login 			loginUI 	= null;
	private Center 			centerUI 	= null;
	private ChessGame		gameUI		= null;
	private Loading			loadUI		= null;
	
	public enum State {
		
		/* Costum type variable */
		END(-1), CONNECT(0), LOGIN(1), WAITING(2),PLAYING(3);
		
		/* Default type variable */
		private int value;
		
		/* Constructor */
		private State(int value){
			this.value = value;
		}
	}
	
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
		if(action.equals("Create")){
			centerUI.addRoom(new roomButton(controller,token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken())));
		}
		else if(action.equals("Join")){
			centerUI.joinRoom(token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken()));
		}
		else if(action.equals("Quit")){
			centerUI.quitRoom(token.nextToken(),Integer.parseInt(token.nextToken()));
		}
		else if(action.equals("Room")){
			String account = token.nextToken();
			String host = token.nextToken();
			int side = Integer.parseInt(token.nextToken());
			roomButton room = new roomButton(controller,account,host,side);
			centerUI.addRoom(room);
			if(token.hasMoreTokens()){
				centerUI.joinRoom(account,token.nextToken(),side*(-1));
			}
		}
	}
	public void moveChess(int px,int py,int x,int y){
		gameUI.moveChess(px,py,x,y);
	}
	public void setState(int previous, int next, int... args){	// update GUI to fit each State
		
		/* Custom type variable */
		State prestate = getState(previous);
		State nextstate = getState(next);
		
		/* Hide Window */
		if(prestate != null){
			switch(prestate){
				case CONNECT:
					loginUI.setVisible(false);
					break;
				case LOGIN:
					centerUI.setVisible(false);
					break;
				case WAITING:
					loadUI.End();
					break;
				case PLAYING:
			}
		}

		/* Show Window */
		if(nextstate != null){
			switch(nextstate){
				case END:
					End(previous);
					break;
				case CONNECT:
					loginUI.setVisible(true);
					break;
				case LOGIN:
					centerUI.setVisible(true);
					break;
				case WAITING:
					loadUI = new Loading("Waiting for Player",controller);
					break;
				case PLAYING:
					Play(args[0]);
			}
		}
	}
	private State getState(int value){							// get State from value
		State result = null;
		for(State state: State.values()){
			if(state.value == value){
				result = state;
			}
		}
		return result;
	}
	private void Play(int Side){
		int color = 1;
		if(Side == 1) color *= -1;
		centerUI.setVisible(false);
		gameUI.initialize(color);
		gameUI.setVisible(true);
	}
	private void End(int statevalue)
	{
		loginUI.dispose();
		centerUI.dispose();
		if(statevalue == State.WAITING.value){
			loadUI.End();
		}
		else if(statevalue == State.PLAYING.value){
			gameUI.dispose();
		}
	}	
}

class Loading extends basicDialog implements Runnable,EndThread {
	
	/* Custom type variable */
	private ClientThread controller = null;
	private basicLabel text = null;
	
	/* Default type variable */
	private String 	message = null;
	private boolean loading = true;
	
	public Loading(String message,ClientThread controller) {
		super("",400,300,260,120);
		this.text = new basicLabel(message,30,0,230,80);
		this.controller = controller;
		this.message 	= message;
		this.add(text);
		setVisible(true);
		addWindowListener(new CloseListener((EndThread)this));
		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		String dots = "";
		int i = 0;
		while(loading){
			/* Delay 700 miliseconds */
			try{ Thread.sleep(700);	}catch(InterruptedException e){}
			/* Loop when i equals 4 */
			if(i==4){ i = 0; dots = "";	}
			/* add new dot to Text */
			i++; dots += ".";
			/* update the Text */
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