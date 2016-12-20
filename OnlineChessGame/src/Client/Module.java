package Client;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.text.DefaultCaret;

public class Module {
	public static void main(String[] args){
		
	}
}

interface TextWindow {	// Windows with TextArea 
	public String getText(String action);
}
interface EndThread {	// Thread needs Terminate Operation
	public void End();
}

class SendListener implements ActionListener{
	
	/* Custom type variables */
	private ClientThread 	sender	= null;
	private TextWindow 		window	= null;
	
	/* Default type variables */
	private String 	action 		= "";
	private String 	arguments 	= "";
	private boolean setSide = false;
	private boolean setName	= false;
	
	/* SendListener which needs to read TextField */
	public SendListener(ClientThread sender, String action, TextWindow window){
		this.sender = sender;
		this.window = window;
		this.action = action;
	}
	/* Basic SendListener */
	public SendListener(ClientThread sender, String action, String...args){
		this.sender = sender;
		this.action = action;
		
		if(action.equals( "Create" )) {
			setSide = true;
		}else if(action.equals( "Profile" )) {
			setName = true;
		}
			
		for(String arg: args) {	// Go through all the arguments
			if((action.equals( "Join" ) && arg.equals( Constant.RANDOM ))) {
				setSide = true;
			}
			else { // Combine all the arguments
				arguments += arg.trim() + Constant.DELIMITER;
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(setSide == true) { // Set Side before Sending
			arguments = "";
			JDialog dialog = new setDialog(this);
		} else if(setName == true) {
			arguments = "";
			new textDialog(this);
		} else { // goto Sending process 
			sendMessage();
		}
	}
	public void sendMessage(){
		/* Message Format: Action|args1|args2|... */
		String message = action + Constant.DELIMITER;
		if(window != null) { // Check to Avoid Null Pointer
			String parameter = window.getText(action);
			message += parameter;
			if(action.equals("Login")||action.equals("Register")){
				StringTokenizer token = new StringTokenizer(parameter,"|");
				String account = token.nextToken();
				sender.setAccount(account);
			}
			if(parameter!=null){
				sender.send(message);
			}
		}
		else{
			message += arguments;
			sender.send(message);
		}
	}
	public void addArguments(String arg){ // add new arguments 
		arguments += arg.trim() + Constant.DELIMITER;
	}
}
class textDialog extends basicDialog implements TextWindow{
	
	private SendListener sendListener = null;
	private JTextArea textarea = new basicTextArea(10,10,100,25);
	private JButton	B_setText = new basicButton("設定",120,10,60,25);
	
	public textDialog(SendListener sendListener){
		super("Change Nickname",125,165,195,72); // set Title/X/Y/W/H
		this.sendListener = sendListener; // set sendListener
		addWindowListener(new CloseListener(this));
		setVisible(true);
		B_setText.addActionListener(new textListener(this));
		add(B_setText);
		add(textarea);
	}
	public void setText(String nickname){
		sendListener.addArguments(nickname);
		sendListener.sendMessage();
	}
	
	@Override
	public String getText(String action) {
		// TODO Auto-generated method stub
		String text = null;
		text = textarea.getText().trim();
		if(!text.equals("")){
			textarea.setText("");
			return text;
		}
		return null;
	}
	
	class textListener implements ActionListener{
		
		/* Custom type variables */
		textDialog dialog = null;
		
		public textListener(textDialog dialog) {
			this.dialog	= dialog;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			String text = dialog.getText("");
			if(text!=null){
				dialog.setText(text);
			}
			dialog.dispose();
		}
	}
}
class setClass extends basicDialog {
	
	private ClientThread sender = null;
	private String message = null;
	
	public setClass(ClientThread sender,String message){
		super("Promotion",125,165,190,110);
		this.sender = sender;
		this.message = message;
		setVisible(true);
		
		JButton B_Queen = new basicButton("Queen",10,12,75,24);
		B_Queen.addActionListener(new classListener(this,Constant.QUEEN));
		JButton B_Bishop = new basicButton("Bishop",98,12,75,24);
		B_Bishop.addActionListener(new classListener(this,Constant.BISHOP));
		JButton B_Knight = new basicButton("Knight",10,48,75,24);
		B_Knight.addActionListener(new classListener(this,Constant.KNIGHT));
		JButton B_Rook = new basicButton("Rook",98,48,75,24);
		B_Rook.addActionListener(new classListener(this,Constant.ROOK));
		add(B_Queen);
		add(B_Bishop);
		add(B_Knight);
		add(B_Rook);
	}
	public void SendMessage(int cc) {
		sender.send(message+cc+Constant.DELIMITER);
	}
	class classListener implements ActionListener{
		private setClass window = null;
		private int cc = 0;
		public classListener(setClass window ,int cc){
			this.cc = cc;
			this.window = window;
		}
		public void actionPerformed(ActionEvent e) {
			if(window!=null){
				window.SendMessage(cc);
			}
			window.dispose();
		}
	}
}
class setDialog extends basicDialog {
	
	private SendListener sendListener = null;

	public setDialog(SendListener sendListener){
		super("Choose Side",125,165,126,170); // set Title/X/Y/W/H
		addWindowListener(new CloseListener(this));
		setVisible(true);
		
		this.sendListener = sendListener; // set sendListener
		
		/* add Button */
		add(new setButton(this,Constant.WHITE_SIDE));
		add(new setButton(this,Constant.BLACK_SIDE));
		add(new setButton(this,Constant.RANDOM)); 
	}
	
	public void setSide(int side){
		/* add side arguments into the message before sending */
		sendListener.addArguments(String.valueOf(side));
		sendListener.sendMessage();	// send the message
	}
	
	class setButton extends JButton {
		public setButton(setDialog dialog,int side) {
			switch(side){
				case Constant.WHITE_SIDE:
					setBounds(15,15,100,30);
					setText("白棋");
					break;
				case Constant.BLACK_SIDE:
					setBounds(15,55,100,30);
					setText("黑棋");
					break;
				case Constant.RANDOM:
					setBounds(15,95,100,30);
					setText("隨意");
			}
			addActionListener(new setListener(dialog,side));
		}
	}
	
	class setListener implements ActionListener{
		
		/* Custom type variables */
		setDialog dialog = null;
		
		/* Default type variables */
		int side = Constant.RANDOM;
		
		public setListener(setDialog dialog,int side) {
			this.dialog	= dialog;
			this.side	= side;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			dialog.setSide(side);
			dialog.dispose();
		}
	}
}

class Warndialog extends basicDialog {
	public Warndialog(String text, int x, int y, int w, int h) {
		super(text, x, y, w, h);
	}
}
class ListListener implements MouseListener{
	private Center center = null;
	private JList list = null;
	public ListListener(Center center,JList list){
		this.center = center;
		this.list = list;
	}
	public void mouseClicked(MouseEvent event){
		if (event.getClickCount() == 2) {	// click count to 2
            int index = list.getSelectedIndex();
            DefaultListModel<String> userlist = (DefaultListModel) list.getModel();
            String account = userlist.get(index);
            center.setTarget(account);
        }
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
}
class CloseListener implements WindowListener{
	
	/* Custom type variables */
	private EndThread	 	controller;
	private JDialog 		dialog;
	private JFrame			frame;

	/* Constructor */
	public CloseListener(){
		controller	= null;
		dialog		= null;
		frame		= null;
	}
	public CloseListener(EndThread controller){
		this();
		this.controller = controller;
	}
	public CloseListener(JDialog dialog){
		this();
		this.dialog = dialog;
	}
	public CloseListener(JFrame frame){
		this();
		this.frame = frame;
	}

	public void windowClosing(WindowEvent e) { // Trigger when clicking [x]
		if(controller!=null){
			if(controller instanceof Loading) ((Loading) controller).Quit();
			controller.End();
		}
		else if(dialog!=null) dialog.dispose();
		else if(frame!=null) frame.dispose();
		else System.exit(0);
	}
	
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}

class basicFrame extends JFrame {		// predefined JFrame
	public basicFrame(String text,int x,int y,int w,int h){
		setTitle(text);
		setLocation(x,y);
		setSize(w,h+7);
		setLayout(null);
		setVisible(false);
		setResizable(false);
		setLocationRelativeTo(null);
	}
}
class basicDialog extends JDialog {		// predefined JDialog
	public basicDialog(String text,int x,int y,int w,int h){
		setTitle(text);
		setLocation(x,y);
		setSize(w,h);
		setLayout(null);
		setVisible(false);
		setAlwaysOnTop(true);
		setResizable(false);
		setLocationRelativeTo(null);
	}
}
class basicScrollPane extends JScrollPane {
	public basicScrollPane(int x,int y,int w,int h){
		Border border = BorderFactory.createLineBorder(Color.WHITE,3);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		setBackground(Color.BLACK);
		setBorder(border);
		setBounds(x,y,w,h);	
		setLayout(null);
	}
}
class ScrollTextArea extends JScrollPane {
	JTextArea textarea = null;
	public ScrollTextArea(int x,int y,int w,int h){
		textarea = new basicTextArea(x,y,w,h);
		setViewportView(textarea);
		DefaultCaret caret = (DefaultCaret)textarea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		textarea.setLineWrap(true);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setBounds(x,y,w,h);
	}
	public JTextArea getTextArea(){
		return textarea;
	}
}
class basicTextArea extends JTextArea {
	public basicTextArea(int x,int y,int w,int h){
		setBounds(x,y,w,h);
	}
}
class ScrollList extends JScrollPane {
	JList list = null;
	public ScrollList(int x,int y,int w,int h,int selectIndex,int rowCount){
		list = new basicList(x,y,w,h,selectIndex,rowCount);
		setViewportView(list);
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setBounds(x,y,w,h);
	}
	public JList getList(){
		return list;
	}
}
class basicList extends JList {
	public basicList(int x,int y,int w,int h,int selectIndex,int rowCount){
		super(new DefaultListModel());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setSelectedIndex(selectIndex);
		setVisibleRowCount(rowCount);
		setBounds(x,y,w,h);
	}
}
class ColorTextLabel extends basicLabel {
	public ColorTextLabel(String text,int x,int y,int w,int h,Color color){
		super(text,x,y,w,h);
		setForeground(color);
	}
}
class basicLabel extends JLabel {		// predefined JLabel
	public basicLabel(String text,int x,int y,int w,int h){
		setText(text);
		setBounds(x,y,w,h);
		setVisible(true);
		setFont(new Font("微軟正黑體", Font.PLAIN, 20));
	}
}
class basicButton extends JButton {		// predefined JButton
	public basicButton(String text,int x,int y,int w,int h){
		setText(text);
		setBounds(x,y,w,h);
		setVisible(true);
	}
}
class inputTextField extends JTextField {	// predefined JTextArea
	public inputTextField (int x,int y,int w,int h){
		setBounds(x,y,w,h);
		setVisible(true);
	}
}
class passwordField extends JPasswordField { // predefined JPasswordField
	public passwordField (char display, int x, int y, int w, int h){
		this.setEchoChar(display);
		setBounds(x,y,w,h);
		setVisible(true);
	}
}