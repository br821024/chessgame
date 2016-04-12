package Client;

import javax.swing.JFrame;

public class GUI extends JFrame {
	
	ClientThread controller = null;
	Login login = null;
	Center center = null;
	
	public GUI(ClientThread client){
		controller = client;
		login = new Login(controller);
		center = new Center(controller);
	}
	
	public void Login(){
		login.setVisible(false);
		center.setVisible(true);
	}
	public void Logout(){
		login.setVisible(true);
		center.setVisible(false);;
	}
	public void Play(int Side){
		
	}
	public void End(){
		login.dispose();
		center.dispose();
	}
	
	public void createroom(String account,String username,int side){
		center.createroom(account, username, side);
	}
	public void joinroom(String account,String username){
		center.joinroom(account,username);
	}
}