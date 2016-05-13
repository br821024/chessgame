package Client;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.JButton;

public class Center extends basicFrame implements TextWindow {

	ClientThread controller = null;
	
	ArrayList<roomButton> roomlist = new ArrayList<roomButton>(); 
	
	JButton B_Create 	= new basicButton("創建房間",850,40,150,30);
	JButton B_Profile 	= new basicButton("個人資訊",850,80,150,30);
	JButton B_Leave 	= new basicButton("離開大廳",850,120,150,30);
	JButton B_Exit 		= new basicButton("離開遊戲",850,160,150,30);
	
	public Center(ClientThread controller){
		super("Center",100,50,1030,796);
		B_Create.addActionListener(new SendListener(controller,"Create"));
		B_Profile.addActionListener(new SendListener(controller,"Profile"));
		B_Leave.addActionListener(new SendListener(controller,"Logout"));
		B_Exit.addActionListener(new SendListener(controller,"End"));
		addWindowListener(new CloseListener(controller));
		this.controller = controller;
		add(B_Create);
		add(B_Profile);
		add(B_Leave);
		add(B_Exit);
	}
	
	public void updateRoom(){
		int x = 0; int y = 0;
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).isFull){
				roomlist.get(i).setBounds(20,620+40*(y),500,30);
				y++;
			}
			else{
				roomlist.get(i).setBounds(20+250*(x%3),20+40*(x/3),230,30);
				x++;
			}
		}
	}
	public void addRoom(roomButton room){
		roomlist.add(room);
		this.add(room);
		updateRoom();
	}
	public void joinRoom(String account,String player,int side){
		roomButton target = getRoom(account);
		target.isFull = true;
		target.setplayer(player,side);
		updateRoom();
	}
	public void leaveRoom(String account,String player,int side){
		roomButton target = getRoom(account);
		target.isFull = false;
		target.setplayer("",side);
		updateRoom();
	}
	public roomButton getRoom(String account){
		roomButton room = null;
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).equals(account)) room = roomlist.get(i);
		}
		return room;
	}
	public void deleteRoom(String account){
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).equals(account)){
				roomlist.get(i).setVisible(false);
				roomlist.remove(i);
			}
		}
	}

	public String getText(String action) {
		return null;
	}
}

class roomButton extends JButton {
	
	public boolean isFull = false;
	
	private String 		Text 	= null;
	private String 		account = null;
	private String[] 	player 	= {"","",""};
	private final String[] 	Symbol = {"\u25cb","\u25cf","\u25d1"}; // \u25cb \u25cf \u2658 \u265e
	private final int formatlength = 32;
	
	public roomButton(ClientThread controller,String account,String player,int side){ // Side = 0 White = 1 Black = 2 Don't mind
		addActionListener(new SendListener(controller,"Join",account,String.valueOf(side)));
		setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		this.account = account;
		setplayer(player,side);
	}
	
	public boolean equals(String account){
		if(account.equals(this.account)) return true;
		else return false;
	}
	public void setplayer(String player,int side){	
		if(this.player[2].equals("")) this.player[side] = player;
		else{
			this.player[1-side] = this.player[2];
			this.player[side] = player;
			this.player[2] = "";
		}
		formatText(side);
	}
	private void formatText(int side){
		if(!isFull){
			Text = Symbol[side]+" %1$"+format(player[side],1)+"s"+player[side]+"%2$"+format(player[side],0)+"s";
			Text = String.format(Text,"","");
		}
		else{
			Text = "%1$"+format(player[0],1)+"s"+Symbol[0]+" "+player[0]+"%2$"+format(player[0],0)+"s"+"　v.s　"+" %1$"+format(player[1],0)+"s"+Symbol[1]+" "+player[1]+"%2$-"+format(player[1],1)+"s";
			Text = String.format(Text,"","","","");
		}
		setText(Text);
	}	
	private int format(String name, int type){
		if(type==0)
			return (formatlength-name.length())/2;
		if(type==1 && name.length()%2==1)
			return (formatlength-name.length())/2+1;
		else
			return (formatlength-name.length())/2;
	}
}