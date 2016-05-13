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
import javax.swing.JTextArea;

interface TextWindow {	// Windows with TextArea 
	public String getText(String action);
}

interface EndThread {
	public void End();
}

class SendListener implements ActionListener{
	
	private ClientThread 	controller	= null;
	private TextWindow 		textwindow	= null;
	
	private String 	action 		= "";
	private String 	arguments 	= "";
	private boolean setSide  = false;
	
	public SendListener(ClientThread controller, String action, TextWindow textwindow){
		this.controller = controller;
		this.textwindow = textwindow;
		this.action 	= action;
	}
	public SendListener(ClientThread controller, String action, String...args){
		this.controller = controller;
		this.action 	= action;
		
		if(action.equals("Create")) setSide = true;
		
		for(String arg: args){
			if((action.equals("Join")&&arg.equals("2"))){
				setSide = true;
			}
			else arguments += arg.trim() + "|";
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(setSide)
			new setDialog(this);
		else
			sendMessage();
	}
	public void sendMessage(){
		String message = action+"|";
		
		if(textwindow!=null)
			message += textwindow.getText(action);
		else
			message += arguments;
		
		controller.send(message);
	}
	public void addArguments(String arg){
		arguments += arg.trim() + "|";
	}
}

class setDialog extends basicDialog {
	
	private SendListener sendListener = null;

	public setDialog(SendListener sendListener){
		super("Setting",125,165,146,180);
		this.sendListener = sendListener;
		addWindowListener(new CloseListener(this));
		for(int i=0;i<3;i++) add(new setButton(this,i));
		setVisible(true);
	}
	
	public void setSide(int side){
		sendListener.addArguments(String.valueOf(side));
		sendListener.sendMessage();
	}
	
	class setButton extends JButton {
		public setButton(setDialog dialog,int side) { // Side = 0 White = 1 Black = 2 Don't mind
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
			addActionListener(new setListener(dialog,side));
			setBounds(15,15+side*40,100,30);
		}
	}
	
	class setListener implements ActionListener{
		
		setDialog dialog = null;
		int side = -1;
		
		public setListener(setDialog dialog,int side) {
			this.dialog = dialog;
			this.side = side;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			dialog.setSide(side);
			dialog.dispose();
		}
	}
}

class basicFrame extends JFrame {
	public basicFrame(String text,int x,int y,int w,int h){
		setTitle(text);
		setLocation(x,y);
		setSize(w,h);
		setLayout(null);
		setVisible(false);
	}
}

class basicDialog extends JDialog {
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

class inputTextArea extends JTextArea {	// predefined JTextArea
	public inputTextArea (int x,int y,int w,int h){
		setLineWrap(false);
		setBounds(x,y,w,h);
		setVisible(true);
	}
}

class CloseListener implements WindowListener{
	
	private EndThread	 	controller 	= null;
	private JDialog 		dialog 		= null;
	private JFrame			frame		= null;
	
	public CloseListener(EndThread controller){
		this.controller = controller;
	}
	public CloseListener(JDialog dialog){
		this.dialog = dialog;
	}
	public CloseListener(JFrame frame){
		this.frame = frame;
	}
	public CloseListener(){
	}
	
	public void windowClosing(WindowEvent e) { // Trigger by clicking [x]
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