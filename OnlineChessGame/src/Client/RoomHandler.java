package Client;

import java.awt.Container;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class RoomHandler {
	
	/* Custom type Variable */
	private ArrayList<roomButton> roomlist = null;
	private ClientThread send_controller = null;
	
	/* Default type Variable */
	private JScrollPane waitpane = null;
	private JScrollPane fullpane = null;
	
	public RoomHandler(ClientThread controller,JScrollPane waitpane, JScrollPane fullpane){
		roomlist	= new ArrayList<roomButton>();
		send_controller = controller;
		this.waitpane	= waitpane;
		this.fullpane	= fullpane;
	}
	
	public void initialRoom(){
		clearRoom();
		updateRoom();
	}
	public void clearRoom(){
		while(roomlist.size()>0){
			System.out.println("Remove room: "+roomlist.get(0).getOwner());
			roomlist.remove(0);
		}
		waitpane.removeAll();
		fullpane.removeAll();
	}
	public void updateRoom(){
		
		/* Default type variable */
		int waitroom = 0;	// # of the waiting room
		int fullroom = 0;	// # of the Full room
		int roomcount = 2;	// # of the room in 1 row
		
		int width = 230;
		int height = 30;
		int offset = 20;
		int x_offset = offset+width;
		int y_offset = offset+height;
		
		for(int i=0;i<roomlist.size();i++){
			/* get Button and defined its position */
			System.out.println("Set roomlist "+i+"th "+roomlist.get(i).getOwner());
			roomButton room = roomlist.get(i);
			if(roomlist.get(i).countplayer()==2){
				room.setBounds(25,20+y_offset*fullroom,roomcount*width+offset,height);
				fullpane.add(room);	// add Room into Component Layer
				fullroom++;
			}
			else{
				room.setBounds(25+x_offset*(waitroom%roomcount),20+y_offset*(waitroom/roomcount),width,height);
				waitpane.add(room);	// add Room into Component Layer
				waitroom++;
			}
		}
		waitpane.repaint();
		fullpane.repaint();
	}
	public void addRoom(String account,String host,int side){
		roomButton room = new roomButton(send_controller,account,host,side);
		roomlist.add(room);		// add Room into array
		updateRoom();			// update the position of room button
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
			if(target.countplayer()==0){
				deleteRoom(account);
			}
			else{
				updateRoom();
			}
		}
	}
	public roomButton getRoom(String account){
		roomButton room = null;
		for(int i=0;i<roomlist.size();i++){
			if(roomlist.get(i).equals(account)) {
				room = roomlist.get(i);
			}
		}
		return room;
	}
	public void deleteRoom(String account){
		for(int i=0;i<roomlist.size();i++){
			roomButton room = roomlist.get(i);
			if(room.equals(account)){
				Container parent = room.getParent();
				if(parent!=null){
					parent.remove(room);
				}
				roomlist.remove(i);
				break;
			}
		}
		updateRoom();
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
		public synchronized void join(String name,int side){
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
				Text =	"%1$"+format(player.get(Constant.WHITE),1)+"s"+symbol.get(Constant.WHITE)+" "+player.get(Constant.WHITE)+"%2$"+format(player.get(Constant.WHITE),0)+"s"+"¡@v.s "+
						"%1$"+format(player.get(Constant.BLACK),0)+"s"+symbol.get(Constant.BLACK)+" "+player.get(Constant.BLACK)+"%2$-"+format(player.get(Constant.BLACK),1)+"s";
				Text = String.format(Text,"","","","");
			}
			else{
				/* Default type variable */
				Enumeration<String> name = player.elements();
				Enumeration<Integer> keys = player.keys();
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
		public String getOwner(){
			return String.valueOf(owner);
		}
	}
}