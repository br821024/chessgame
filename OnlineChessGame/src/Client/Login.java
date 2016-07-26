package Client;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends basicDialog implements TextWindow {
	
	/* Default type variable */
	private JLabel			L_account 	= new basicLabel("±b¸¹¡G",15,12,60,30);
	private JLabel			L_password	= new basicLabel("±K½X¡G",15,52,60,30);
	private JButton			B_Register	= new basicButton("µù¥U",300,16,70,24);
	private JButton			B_Login 	= new basicButton("µn¤J",300,56,70,24);
	private JTextField		T_account 	= new inputTextField(80,18,205,20);
	private JPasswordField	T_password	= new passwordField('*',80,58,205,20);
	
	public Login(ClientThread controller){
		super("Login",500,200,400,132);
		B_Register.addActionListener(new SendListener(controller,"Register",this));
		B_Login.addActionListener(new SendListener(controller,"Login",this));
		this.addWindowListener(new CloseListener(controller));
		add(L_account);
		add(T_account);
		add(L_password);
		add(T_password);
		add(B_Register);
		add(B_Login);
		
		setVisible(true);
	}

	public String getText(String action) {
		String arguments = "";
		if(action.equals("Login") || action.equals("Register")){
			arguments = T_account.getText().trim()+Constant.DELIMITER+
						MD5(String.valueOf(T_password.getPassword()));
		}
		else{
			System.out.println("TextWindow: Undefined Action Type");
		}
		return arguments;
	}
	public String MD5(String plaintext){
		String result = null;
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(plaintext.getBytes());
			byte[] bytes = messagedigest.digest();
			result = javax.xml.bind.DatatypeConverter.printHexBinary(bytes);		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}