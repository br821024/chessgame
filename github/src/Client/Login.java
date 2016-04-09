package Client;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Login extends JDialog {
	
	JLabel L_account = new basicLabel("±b¸¹¡G",15,12,60,30);
	JLabel L_password = new basicLabel("±K½X¡G",15,52,60,30);
	JTextArea T_account = new inputTextArea(80,18,205,20);
	JTextArea T_password = new inputTextArea(80,58,205,20);
	JButton B_Register = new basicButton("µù¥U",300,16,70,24);
	JButton B_Login = new basicButton("µn¤J",300,56,70,24);
	
	public Login(ClientThread controller){
		B_Register.addActionListener(new LoginListener(controller,"Register"));
		B_Login.addActionListener(new LoginListener(controller,"Login"));
		addWindowListener(new CloseListener(controller));
		setTitle("Login");
		setSize(400,132);
		setLayout(null);
		setVisible(true);
		add(L_account);
		add(T_account);
		add(L_password);
		add(T_password);
		add(B_Register);
		add(B_Login);
	}
	
	public String getLoginInfo(){ // getText from Account and Password
		String LoginInfo = T_account.getText().trim()+"|"+T_password.getText().trim();
		return LoginInfo;
	}
	
	class LoginListener implements ActionListener{
		String message = null;
		String action = null;
		ClientThread controller = null;
		
		public LoginListener(ClientThread controller, String action) {
			this.controller = controller;
			this.action = action;
		}
		
		public void actionPerformed(ActionEvent e) {
			message = action+"|"+getLoginInfo();
			controller.send(message);
		}
	}
}

class basicLabel extends JLabel {
	public basicLabel (String text,int x,int y,int w,int h){
		setText(text);
		setBounds(x,y,w,h);
		setVisible(true);
		setFont(new Font("·L³n¥¿¶ÂÅé", Font.PLAIN, 20));
	}
}

class basicButton extends JButton {
	public basicButton(String text,int x,int y,int w,int h){
		setText(text);
		setBounds(x,y,w,h);
		setVisible(true);
	}
}

class inputTextArea extends JTextArea {
	public inputTextArea (int x,int y,int w,int h){
		setLineWrap(false);
		setBounds(x,y,w,h);
		setVisible(true);
	}
}

class CloseListener implements WindowListener{
	
	ClientThread controller = null;
	int type = 0;
	
	public CloseListener(ClientThread controller){
		this.controller = controller;
	}
	
	public CloseListener(){
		type = -1;
	}
	
	public void windowClosing(WindowEvent e) { // Execute when click [x] to close
		if(type==0)
			controller.End();
		else
			System.exit(0);
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
}