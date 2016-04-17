package Client;

import java.util.StringTokenizer;

import javax.swing.JFrame;

public class GUI extends JFrame {
	
	ClientThread controller = null;
	Login login = null;
	Center center = null;
	
	public GUI(ClientThread client){
		controller = client;
		login = new Login(controller);
		center = new Center(controller);
		//center.setVisible(true);
		//center.addRoom(new roomButton(controller,"Aneros","Aneros",0));
		//center.addRoom(new roomButton(controller,"Anoymous","Anoymous",0));
		//center.addRoom(new roomButton(controller,"Anoymous1","Anoymous1",0));
		//center.addRoom(new roomButton(controller,"Anoymous2","Anoymous2",2));
		//center.addRoom(new roomButton(controller,"Anoymous3","Anoymous3",2));
		//center.addRoom(new roomButton(controller,"Anoymous4","Anoymous4",2));
		//center.joinRoom("Aneros","join1",1);
	}
	
	public void Room(String action,StringTokenizer token){
		if(action.equals("Create")){ center.addRoom(new roomButton(controller,token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken()))); }
		else if(action.equals("Join")){ center.joinRoom(token.nextToken(),token.nextToken(),Integer.parseInt(token.nextToken())); }
		else if(action.equals("Room")){
			String account = token.nextToken();
			String host = token.nextToken();
			roomButton room = null;
			int side = Integer.parseInt(token.nextToken());
			room = new roomButton(controller,account,host,side);
			center.addRoom(room);
			if(token.hasMoreTokens()) center.joinRoom(account,token.nextToken(),1-side);
		}
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
		center.setVisible(false);
		login.setVisible(true);
	}
	public void Wait(){
		center.setVisible(false);
		login.setVisible(true);
	}
	public void End(){
		login.dispose();
		center.dispose();
	}
	
}