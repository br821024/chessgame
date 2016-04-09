package Server;

import java.sql.*;

public class MySQL {
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultset = null;
	private String user = "root";
	private String pass = "root";
	
	public MySQL(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/chessgame?autoReconnect=true&useSSL=True",user,pass);
			statement = connection.createStatement();
			System.out.println("Connected with MySQL Server!");
		}
		catch(ClassNotFoundException e){System.out.println("SQL: "+e.toString());}
		catch(SQLException e){System.out.println("SQL: "+e.toString());}
	}
	
	public void initialize(){
		createDB();
		createTable();
	}
	
	private void createDB(){
		try{
			statement.executeUpdate("CREATE DATABASE IF NOT EXISTS chessgame");
		}catch(SQLException e){}
	}
	
	private void createTable(){
		try{
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS login(account_id INT(20) UNSIGNED NOT NULL AUTO_INCREMENT,user_id VARCHAR(32) NOT NULL,user_pass VARCHAR(32) NOT NULL ,user_name VARCHAR(32) NOT NULL DEFAULT \"Unknown User\", PRIMARY KEY(account_id) , UNIQUE(user_id));");
		}catch(SQLException e){}
	}
	
	public int checkaccount(String account){
		int result = -1;
		try{
			PreparedStatement prstmt = null;
			prstmt = connection.prepareStatement("SELECT COUNT(*) FROM login WHERE user_id = ?");
			prstmt.setString(1,account);
			resultset = prstmt.executeQuery();
			if(resultset.next()){result = resultset.getInt(1);}
			prstmt.close();
		}catch(SQLException e){System.out.print("MySQL: Checkaccount Error"+ e.toString()); initialize();}
		return result;
	}
	
	public Boolean login(String account,String password){
		Boolean result = false;
		try{
			PreparedStatement prstmt = null;
			prstmt = connection.prepareStatement("SELECT user_pass FROM login WHERE user_id = ?");
			prstmt.setString(1,account);
			resultset = prstmt.executeQuery();
			if(resultset.next()){
				if(resultset.getString(1).equals(password))	result = true;
			}
			prstmt.close();
		}
		catch(SQLException e){System.out.println("MySQL: Login Error: "+e.toString()); initialize();}
		return result;
	}
	
	public Boolean register(String account,String password){
		try{
			PreparedStatement prstmt = null;
			prstmt = connection.prepareStatement("INSERT INTO login VALUES(?,?,?,DEFAULT)");
			prstmt.setNull(1,Types.INTEGER);
			prstmt.setString(2,account);
			prstmt.setString(3,password);
			prstmt.execute();
			prstmt.close();
			return true;
		}catch(SQLException e){System.out.println("MySQL: Register Error: "+e.toString()); initialize(); return false;}
	}
	
	public void End(){
		try {
			connection.close();
		} catch (SQLException e) { System.out.println("MySQL: "+e.toString()); }
	}
}

