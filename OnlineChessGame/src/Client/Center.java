package Client;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class Center extends basicFrame implements TextWindow {

	/* Custom type variable */
	private ClientThread controller = null;
	private RoomHandler roomhandler = null;
	
	/* Default type variable */
	private JLabel 	L_Waitroom	= new ColorTextLabel("加入房間",35,20,100,20,Color.WHITE);
	private JLabel 	L_Fullroom	= new ColorTextLabel("進入觀戰",35,230,100,20,Color.WHITE);
	private JLabel	L_Message	= new ColorTextLabel("查看訊息",35,440,100,20,Color.WHITE);
	private JButton B_Send		= new basicButton("傳送訊息",463,705,100,30);
	private JButton B_Create 	= new basicButton("創建房間",625,491,140,35);
	private JButton B_Profile 	= new basicButton("修改暱稱",625,556,140,35);
	private JButton B_Leave 	= new basicButton("離開大廳",625,621,140,35);
	private JButton B_Exit 		= new basicButton("離開遊戲",625,686,140,35);
	private ScrollList	P_Userlist	= new ScrollList(610,20,175,407,0,20);
	private JScrollPane P_Waitroom	= new basicScrollPane(25,45,537,170);
	private JScrollPane P_Fullroom	= new basicScrollPane(25,255,537,170);
	private ScrollTextArea P_Message	= new ScrollTextArea(25,465,537,180);
	private ScrollTextArea P_SendText	= new ScrollTextArea(25,665,417,70);
	private JTextField	T_account 	= new inputTextField(463,665,100,25);
	private JLabel	L_background	= new basicLabel("",0,0,590,796);
	
	private DefaultListModel<String> userlist = null;
	
	public static void main(String[] args){
		Center center = new Center(null);
		center.setVisible(true);
	}
	public Center(ClientThread controller){
		super("Center",100,50,805,786);		// set Title/X/Y/W/H
		this.controller = controller;		// set Controller
		this.addWindowListener(new CloseListener(controller));
		roomhandler = new RoomHandler(controller,P_Waitroom,P_Fullroom);
		/* Define button function and add into Component Layer */
		B_Create.addActionListener(new SendListener(controller,"Create"));
		B_Create.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		B_Profile.addActionListener(new SendListener(controller,"Profile"));
		B_Profile.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		B_Leave.addActionListener(new SendListener(controller,"Logout"));
		B_Leave.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		B_Exit.addActionListener(new SendListener(controller,"End"));
		B_Exit.setFont(new Font("微軟正黑體", Font.PLAIN, 18));
		B_Send.addActionListener(new SendListener(controller,"Message",this));
		
		try{
			BufferedImage image = ImageIO.read(new File("Chess/background.jpg"));
			L_background.setIcon(new ImageIcon(image));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		P_Waitroom.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		P_Fullroom.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		P_Message.getTextArea().setText("/* 歡迎使用線上西洋棋系統 */\n");
		P_Message.getTextArea().setEditable(false);
		//P_Message.getTextArea().append("a\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\n");
		//P_Message.getTextArea().append("a\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\na\n");
		
		P_Userlist.getList().addMouseListener(new ListListener(this,P_Userlist.getList()));
		userlist = (DefaultListModel) P_Userlist.getList().getModel();

		add(P_Userlist);
		add(B_Create);
		add(B_Profile);
		add(B_Leave);
		add(B_Exit);
		add(B_Send);
		add(T_account);
		add(L_Waitroom);
		add(P_Waitroom);
		add(L_Fullroom);
		add(P_Fullroom);
		add(L_Message);
		add(P_Message);
		add(P_SendText);
		add(L_background);
	}
	
	public void setTarget(String account){
		T_account.setText(account);
	}
	public void showMessage(String message){
		P_Message.getTextArea().append(message);
	}
	public void updateRoom(){
		roomhandler.updateRoom();
	}
	public void addUser(String account){
		boolean result = true;
		for(int i=0;i<userlist.getSize();i++){
			if(userlist.get(i).equals(account)){
				result = false;
				break;
			}
		}
		if(result){
			userlist.addElement(account);
		}
	}
	public void initialUser(){
		userlist.removeAllElements();
	}
	public void deleteUser(String account){
		for(int i=0;i<userlist.getSize();i++){
			if(userlist.get(i).equals(account)){
				userlist.remove(i);
				break;
			}
		}
	}
	public void addRoom(String account,String host,int side){
		roomhandler.addRoom(account,host,side);
	}
	public void deleteRoom(String account){
		roomhandler.deleteRoom(account);
	}
	public void joinRoom(String account,String player,int side){
		roomhandler.joinRoom(account, player, side);
	}
	public void quitRoom(String account,int side){
		roomhandler.quitRoom(account, side);
	}
	public void initialRoom(){
		roomhandler.initialRoom();
	}

	public String getText(String action) {
		String header = controller.getAccount()+" : ";
		String message = null;
		if(action.equals("Message")){
			message = P_SendText.getTextArea().getText().trim();
			if(message.equals("")){
				return null;
			} else {
				message = header + message + Constant.DELIMITER;
			}
			String target = T_account.getText().trim();
			if(!target.equals("")){
				message += target+Constant.DELIMITER;
			}
			P_SendText.getTextArea().setText("");
		}
		else{ System.out.println("TextWindow: Undefined Action Type"); }
		return message;
	}
}