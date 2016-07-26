package Client;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Module {	// Test
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
	private boolean setSide  = false;
	
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
			new setDialog(this);
		}
		else { // goto Sending process 
			sendMessage();
		}
	}
	public void sendMessage(){
		/* Message Format: Action|args1|args2|... */
		String message = action + Constant.DELIMITER;
		
		if(window != null) { // Check to Avoid Null Pointer
			message += window.getText(action);
		}
		else{
			message += arguments;
		}
		
		/* Thread send Message */
		sender.send(message);
	}
	public void addArguments(String arg){ // add new arguments 
		arguments += arg.trim() + Constant.DELIMITER;
	}
}

class setDialog extends basicDialog {
	
	private SendListener sendListener = null;

	public setDialog(SendListener sendListener){
		super("Setting",125,165,146,180);	// set Title/X/Y/W/H
		addWindowListener(new CloseListener(this));
		this.setVisible(true);
		
		this.sendListener = sendListener;	// set sendListener
		
		/* add Button */
		add(new setButton(this,Constant.WHITE_SIDE));
		add(new setButton(this,Constant.BLACK_SIDE));
		add(new setButton(this,Constant.RANDOM)); 
	}
	
	public void setSide(int side){
		/* add side arguments into the message before sending */
		sendListener.addArguments(String.valueOf(side));
		sendMessage();	// send the message
	}
	public void sendMessage(){
		sendListener.sendMessage();
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
		setSize(w,h);
		setLayout(null);
		setVisible(false);
	}
}

class basicDialog extends JDialog {		// predefined JDialog
	public basicDialog(String text,int x,int y,int w,int h){
		setTitle(text);
		setLocation(x,y);
		setSize(w,h);
		setLayout(null);
		setVisible(false);
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