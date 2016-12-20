package Client;

import java.awt.Color;
import java.util.StringTokenizer;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class GUI extends JFrame {
	
	/* Custom type variable */
	private ClientThread 	controller 	= null;
	private Register 		RegisterUI 	= null;
	private Center 			centerUI 	= null;
	private ChessGame		gameUI		= null;
	private Loading			loadUI		= null;

	public GUI(ClientThread controller){
		
		this.controller = controller;
		RegisterUI 	= new Register(controller);
		centerUI 	= new Center(controller);
		gameUI 		= new ChessGame(controller);
		
		/*
		gameUI.setVisible(true);
		gameUI.initialize(Constant.OBSERVER);
		*/
		//JDialog dialog = new textDialog(null);
		/*
		centerUI.setVisible(true);
		centerUI.addRoom("ABC","Apple",2);
		centerUI.addRoom("123","Pineapple",2);
		centerUI.addRoom("456","CCLemon",2);
		centerUI.addRoom("789","Dickhead",2);
		centerUI.addRoom("101","Enderman",2);
		centerUI.addRoom("112","Fingurine",2);
		centerUI.addRoom("12345","GGininder",2);
		centerUI.addRoom("1234567","Handicap",2);
		centerUI.addRoom("123456789","Illusion",2);
		centerUI.joinRoom("ABC","Pen",-1);
		centerUI.joinRoom("123","Pan",-1);
		centerUI.joinRoom("456","Yeah",-1);
		centerUI.addUser("Addict");
		centerUI.addUser("Binbou");
		centerUI.addUser("Clinton");
		centerUI.addUser("Divine");
		*/
	}
	public void showMessage(String message){
		centerUI.showMessage(message);
	}
	public void setUITitle(String account){
		centerUI.setTitle("User : "+account);
		gameUI.setTitle("User : "+account);
	}
	public void addUser(String account){
		centerUI.addUser(account);
	}
	public void deleteUser(String account){
		centerUI.deleteUser(account);
	}
	public void createRoom(String account,String host,int side) {
		centerUI.addRoom(account,host,side);
	}
	public void deleteRoom(String account) {
		centerUI.deleteRoom(account);
	}
	public void joinRoom(String account,String player,int side) {
		centerUI.joinRoom(account, player, side);
	}
	public void quitRoom(String account,int side) {
		centerUI.quitRoom(account, side);
	}
	public void Endgame(int side){
		gameUI.Endgame(side);
	}
	public void moveChess(int px,int py,int x,int y,int cc){
		gameUI.moveChess(px,py,x,y,cc);
	}
	public void setState(State prestate, State nextstate, int... args){	// update GUI to fit each State
		
		/* Handle previos State */
		if(prestate != null){
			switch(prestate){
				case CONNECT:
					RegisterUI.setVisible(false);
					break;
				case LOGIN:
					centerUI.setVisible(false);
					break;
				case WAITING:
					loadUI.End();
					break;
				case PLAYING:
					gameUI.setVisible(false);
			}
		}

		/* Handle next State */
		if(nextstate != null){
			switch(nextstate){
				case END:
					End();
					break;
				case CONNECT:
					RegisterUI.setVisible(true);
					break;
				case LOGIN:
					centerUI.initialUser();
					centerUI.initialRoom();
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
	private void Play(int Side){
		centerUI.setVisible(false);
		gameUI.initialize(Side);
		gameUI.setVisible(true);
	}
	private void End()
	{
		RegisterUI.dispose();
		centerUI.dispose();
		gameUI.dispose();
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
		super("",400,300,250,110);
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