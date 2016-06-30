package Client;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JButton;

public class Center extends basicFrame implements TextWindow {

	/* Custom type variable */
	private ClientThread controller = null;
	
	/* Default type variable */
	private ArrayList<roomButton> roomlist = new ArrayList<roomButton>(); 
	private JButton B_Create 	= new basicButton("創建房間",850,40,150,30);
	private JButton B_Profile 	= new basicButton("個人資訊",850,80,150,30);
	private JButton B_Leave 	= new basicButton("離開大廳",850,120,150,30);
	private JButton B_Exit 		= new basicButton("離開遊戲",850,160,150,30);
	
	public Center(ClientThread controller){
		super("Center",100,50,1030,796);	// set Title/X/Y/W/H
		this.controller = controller;		// set Controller
		this.addWindowListener(new CloseListener(controller));
		
		/* Define button function and add into Component Layer */
		B_Create.addActionListener(new SendListener(controller,"Create"));
		B_Profile.addActionListener(new SendListener(controller,"Profile"));
		B_Leave.addActionListener(new SendListener(controller,"Logout"));
		B_Exit.addActionListener(new SendListener(controller,"End"));
		add(B_Create);
		add(B_Profile);
		add(B_Leave);
		add(B_Exit);
	}
	
	public void updateRoom(){
		
		/* Default type variable */
		int x = 0;	// # of the waiting room
		int y = 0;	// # of the Full room
		
		for(int i=0;i<roomlist.size();i++){
			/* get Button and defined its position */
			if(roomlist.get(i).countplayer()==2){
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
		roomlist.add(room);				// add Room into array
		this.add(room);					// add Room into Component Layer
		updateRoom();					// update the position of room button
	}
	public void joinRoom(String account,String player,int side){
		roomButton target = getRoom(account);
		if(target!=null){				// check to avoid null pointer 
			target.join(player,side);
			updateRoom();
		}
	}
	public void quitRoom(String account,int side){
		roomButton target = getRoom(account);
		if(target!=null){
			target.join("",side);
			if(target.countplayer()==0) deleteRoom(account);
			updateRoom();
		}
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
	
	/* Default type variable */
	private String 		owner	= null;		// Room owner's account(One account only host one room)
	private String 		Text 	= null;		// Title show on the button
	private final int formatlength = 32;

	private Hashtable<Integer,String> player = new Hashtable<Integer,String>();
	private Hashtable<Integer,String> symbol = new Hashtable<Integer,String>();
	
	/* Constructor */
	public roomButton(ClientThread controller,String account,String name,int side){
		addActionListener(new SendListener(controller,"Join",account,String.valueOf(side)));
		setFont(new Font("Arial Unicode MS", Font.PLAIN, 16));
		symbol.put(Constant.WHITE_SIDE,"\u25cb");
		symbol.put(Constant.BLACK_SIDE,"\u25cf");
		symbol.put(Constant.RANDOM,"\u25d1");
		owner = account;		// set room owner's Account
		join(name,side);		// set the host on target side
	}

	public boolean equals(String account){
		boolean result = false;
		if(account.equals(owner)){
			result = true;
		}
		return result;
	}
	public int countplayer(){
		return player.size();
	}
	public void join(String name,int side){
		/* set the random host to the opposite side of join player */
		if(player.containsKey(Constant.RANDOM)){
			String hostname = player.remove(Constant.RANDOM);
			player.put(side*(-1),hostname);
		}
		
		if(countplayer()==2){
			// room is full join as Constant.OBSERVER
		}
		else{
			player.put(side,name);
		}
		
		formatText();
	}
	private void formatText(){				 // format the title of the button
		if(countplayer()==2){
			Text =	"%1$"+format(player.get(Constant.WHITE),1)+"s"+symbol.get(Constant.WHITE)+" "+player.get(Constant.WHITE)+"%2$"+format(player.get(Constant.WHITE),0)+"s"+"　v.s "+
					"%1$"+format(player.get(Constant.BLACK),0)+"s"+symbol.get(Constant.BLACK)+" "+player.get(Constant.BLACK)+"%2$-"+format(player.get(Constant.BLACK),1)+"s";
			Text = String.format(Text,"","","","");
		}
		else{
			/* Default type variable */
			Enumeration name = player.elements();
			Enumeration keys = player.keys();
			String hostname = null;
			int side = 0;
			
			/* get host information */
			if(name.hasMoreElements()){
				hostname = (String) name.nextElement();
			}
			if(keys.hasMoreElements()){
				side = (Integer) keys.nextElement();
			}
			
			Text = symbol.get(side)+" %1$"+format(player.get(side),1)+"s"+player.get(side)+"%2$"+format(player.get(side),0)+"s";
			Text = String.format(Text,"","");
		}
		
		setText(Text);
	}	
	private int format(String name, int type){	// return value is amount of spaces required
		if(type==0) {
			return (formatlength-name.length())/2;
		}
		
		if(type==1 && name.length()%2 == 1) {
			return (formatlength-name.length())/2+1;
		}
		else{
			return (formatlength-name.length())/2;
		}
	}
}