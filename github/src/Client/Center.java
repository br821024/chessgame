package Client;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class Center extends JFrame {

	ClientThread controller = null;
	
	ArrayList<roomButton> roomlist = new ArrayList<roomButton>(); 
	
	JButton B_Create = new basicButton("創建房間",850,40,150,30);
	JButton B_Profile = new basicButton("個人資訊",850,80,150,30);
	JButton B_Leave = new basicButton("離開大廳",850,120,150,30);
	JButton B_Exit = new basicButton("離開遊戲",850,160,150,30);
	
	public Center(ClientThread controller){
		B_Create.addActionListener(new RoomListener(controller,"Create"));
		B_Profile.addActionListener(new RoomListener(controller,"Profile"));
		B_Leave.addActionListener(new RoomListener(controller,"Logout"));
		B_Exit.addActionListener(new RoomListener(controller,"End"));
		addWindowListener(new CloseListener(controller));
		this.controller = controller;
		setSize(1030,796);
		setLayout(null);
		setVisible(false);
		add(B_Create);
		add(B_Profile);
		add(B_Leave);
		add(B_Exit);
	}
	
	public void updateRoom(){
		int x = 0; int y = 0;
		for(int i=0;i<roomlist.size();i++){
			roomButton target = roomlist.get(i);
			if(target.isFull){
				target.setBounds(20,620+40*(y),500,30);
				y++;
			}
			else{
				target.setBounds(20+250*(x%3),20+40*(x/3),230,30);
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
			if(roomlist.get(i).account.equals(account)) room = roomlist.get(i);
		}
		return room;
	}
	public void deleteRoom(String account){
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).account.equals(account)) roomlist.remove(i);
		}
	}
}

class RoomListener implements ActionListener{
	
	ClientThread controller = null;
	String account = null;
	String action = null;

	int side = -1;
	
	public RoomListener(ClientThread controller,String action,String account,int side){
		this.controller = controller;
		this.account = account;
		this.action = action;
		this.side = side;
	}
	public RoomListener(ClientThread controller,String action) {
		this.controller = controller;
		this.action = action;
	}

	public void actionPerformed(ActionEvent arg0) {
		if(action!=null){
			if(action.equals("Create")){
				new setDialog(this);
			}
			else if(action.equals("Join")){
				if(side==2) new setDialog(this);
				else controller.send(action+"|"+account+"|"+(1-side));
			}
			else controller.send(action);
		}
	}
	
	public void actionPerformed(ActionEvent arg0,int side){
		if(action.equals("Join")) controller.send(action+"|"+account+"|"+side);
		else controller.send(action+"|"+side);
	}
}

class setDialog extends JDialog {
	public setDialog(RoomListener center) {
		setTitle("Setting");
		setSize(146,180);
		setLocation(125,165);
		setLayout(null);
		setVisible(true);
		for(int i=0;i<3;i++){
			add(new setButton(center,this,i));
		}
	}
	
	class setButton extends JButton {
		public setButton(RoomListener listener,setDialog dialog,int side) { // Side = 0 White = 1 Black = 2 Don't mind
			switch(side){
				case 0:
					setText("白棋");
					break;
				case 1:
					setText("黑棋");
					break;
				case 2:
					setText("隨意");
					break;
				default:
					setText("Error");
			}
			addActionListener(new setListener(listener,dialog,side));
			setBounds(15,15+side*40,100,30);
		}
	}
	
	class setListener implements ActionListener{
		
		RoomListener listener = null;
		JDialog dialog = null;
		int side = -1;
		
		public setListener(RoomListener listener,JDialog dialog,int side) {
			this.listener = listener;
			this.dialog = dialog;
			this.side = side;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			listener.actionPerformed(arg0,side);
			dialog.dispose();
		}
	}
}

class roomButton extends JButton {
	
	private String[] Symbol = {"\u25cb","\u25cf","\u25d1"}; // \u25cb \u25cf \u2658 \u265e
	private String Text = null;
	
	private final int formatlength = 32;
	
	public String account = null;
	public String[] player = {"","",""};
	
	public boolean isFull = false;
	
	public roomButton(ClientThread controller,String account,String player,int side){ // Side = 0 White = 1 Black = 2 Don't mind
		addActionListener(new RoomListener(controller,"Join",account,side));
		setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		this.account = account;
		setplayer(player,side);
	}
	
	public void setplayer(String player,int side){
		this.player[side] = player;
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
	
	public int format(String name, int type){
		if(type==0)
			return (formatlength-name.length())/2;
		if(type==1 && name.length()%2==1)
			return (formatlength-name.length())/2+1;
		else
			return (formatlength-name.length())/2;
	}
}