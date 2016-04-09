package Client;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class Center extends JFrame {

	ClientThread controller = null;
	
	JButton B_Create = new basicButton("創建房間",850,40,150,30);
	JButton B_Profile = new basicButton("個人資訊",850,80,150,30);
	JButton B_Leave = new basicButton("離開大廳",850,120,150,30);
	JButton B_Exit = new basicButton("離開遊戲",850,160,150,30);
	
	public Center(ClientThread controller){
		B_Create.addActionListener(new CenterListener(controller));
		B_Profile.addActionListener(new CenterListener(controller,"Profile"));
		B_Leave.addActionListener(new CenterListener(controller,"Logout"));
		B_Exit.addActionListener(new CenterListener(controller,"End"));
		addWindowListener(new CloseListener(controller));
		this.controller = controller;
		setSize(1030,796);
		setLayout(null);
		setVisible(false);
		add(B_Create);	//創建房間
		add(B_Profile);	//個人資訊
		add(B_Leave);	//離開大廳
		add(B_Exit);	//離開遊戲
	}
}

class CenterListener implements ActionListener{
	
	ClientThread controller = null;
	Center center = null;
	String action = null;
	int type = -1;
	int side = -1;
	
	public CenterListener(ClientThread controller,String action) {
		this.controller = controller;
		this.action = action;
	}
	
	public CenterListener(ClientThread controller) {
		this.controller = controller;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if(action!=null){
			controller.send(action+"|");
		}
		else{
			new setDialog(this);
		}
	}
	
	public void createRoom(int side){
		controller.send("Create|"+side);
	}
}

class setDialog extends JDialog {
	public setDialog(CenterListener center) {
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
		public setButton(CenterListener center,setDialog dialog,int side) { // Side = 0 White = 1 Black = 2 Don't mind
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
			addActionListener(new setListener(center,dialog,side));
			setBounds(15,15+side*40,100,30);
		}
	}
	
	class setListener implements ActionListener{
		
		CenterListener center = null;
		setDialog dialog = null;
		int side = -1;
		
		public setListener(CenterListener center,setDialog dialog,int side) {
			this.center = center;
			this.dialog = dialog;
			this.side = side;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			center.createRoom(side);
			dialog.dispose();
		}
	}
}

class roomButton extends JButton {
	
	private String[] Symbol = {"\u25cb","\u25cf","\u25d1"}; // \u25cb \u25cf \u2658 \u265e
	private String Text = null;
	
	public String[] player = {"","",""};
	public final int formatlength = 32;
	
	public roomButton(ClientThread controller,String account,int index,String player,int side){ // Side = 0 White = 1 Black = 2 Don't mind
		addActionListener(new RoomListener(account,controller));
		setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		setBounds(index,index,230,30);
		setplayer(player,side);
	}
	
	public void setplayer(String player,int side){
		this.player[side] = player;
		formatText(side);
	}
	
	private void formatText(int side){
		if(player[0].equals("") || player[1].equals("")){
			Text = Symbol[side]+" %1$"+format(player[side],1)+"s"+player[side]+"%2$"+format(player[side],0)+"s";
			Text = String.format(Text,"","");
			setSize(230,30);
		}
		else{
			Text = "%1$"+format(player[0],1)+"s"+Symbol[0]+" "+player[0]+"%2$"+format(player[0],0)+"s"+"　v.s　"+" %1$"+format(player[1],0)+"s"+Symbol[1]+" "+player[1]+"%2$-"+format(player[1],1)+"s";
			Text = String.format(Text,"","","","");
			setSize(500,30);
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

class RoomListener implements ActionListener {
	
	ClientThread controller;
	String account;
	
	public RoomListener(String account,ClientThread controller){
		this.controller = controller;
		this.account = account;
	}

	public void actionPerformed(ActionEvent arg0) {
		controller.send("Join|"+account);
	}
}