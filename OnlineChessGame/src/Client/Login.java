package Client;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Login extends basicDialog implements TextWindow {
	
	JLabel L_account 		= new basicLabel("±b¸¹¡G",15,12,60,30);
	JLabel L_password 		= new basicLabel("±K½X¡G",15,52,60,30);
	JTextArea T_account 	= new inputTextArea(80,18,205,20);
	JTextArea T_password 	= new inputTextArea(80,58,205,20);
	JButton B_Register 		= new basicButton("µù¥U",300,16,70,24);
	JButton B_Login 		= new basicButton("µn¤J",300,56,70,24);
	
	public Login(ClientThread controller){
		super("Login",500,200,400,132);
		B_Register.addActionListener(new SendListener(controller,"Register",this));
		B_Login.addActionListener(new SendListener(controller,"Login",this));
		addWindowListener(new CloseListener(controller));
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
		if(action.equals("Login")||action.equals("Register")){
			arguments = T_account.getText().trim()+"|"+T_password.getText().trim();
		}
		else System.out.println("TextWindow: Undefined Action Type");
		return arguments;
	}
}