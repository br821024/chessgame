package Client;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Register extends basicFrame implements TextWindow {

	/* Custom type variable */
	private HashHandler		hash = new HashHandler();
	
	/* Default type variable */
	private JLabel			L_account 	= new basicLabel("±b¸¹¡G",15,12,60,30);
	private JLabel			L_password	= new basicLabel("±K½X¡G",15,52,60,30);
	private JButton			B_Register	= new basicButton("µù¥U",300,16,70,24);
	private JButton			B_Login 	= new basicButton("µn¤J",300,56,70,24);
	private JTextField		T_account 	= new inputTextField(80,18,205,20);
	private JPasswordField	T_password	= new passwordField('*',80,58,205,20);

	
	public Register(ClientThread send_controller){
		super("Register",500,200,390,122);
		B_Register.addActionListener(new SendListener(send_controller,"Register",this));
		B_Login.addActionListener(new SendListener(send_controller,"Login",this));
		this.addWindowListener(new CloseListener(send_controller));
		add(L_account);
		add(T_account);
		add(L_password);
		add(T_password);
		add(B_Register);
		add(B_Login);
		
		setVisible(true);
	}

	public String getText(String action) {	// getText method for Register UI
		String arguments = "";
		String account	= null;
		String password = null;
		if(action.equals("Login") || action.equals("Register")){
			account  = T_account.getText().trim();
			password = String.valueOf(T_password.getPassword()).trim();
			arguments = account+Constant.DELIMITER+hash.MD5(password)+Constant.DELIMITER;
		}
		else{ System.out.println("TextWindow: Undefined Action Type"); }
		if(account.equals("") || password.equals("")){
			return null;
		}
		return arguments;
	}
}