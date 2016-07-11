package Server;

import java.sql.*;

public class MySQL {
	
	/* Default type variable */
	private Connection 	connection	= null;
	private Statement 	statement	= null;
	private ResultSet 	resultset	= null;

	private String user = "root";
	private String pass = "root";

	/* Constructor */
	public MySQL(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost/?autoReconnect=true&useSSL=True", user, pass);
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
		}catch(SQLException e){System.out.println("SQL: "+e.toString());}
	}
	
	private void createTable(){
		try{
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS chessgame.login(account_id INT UNSIGNED NOT NULL AUTO_INCREMENT,user_id VARCHAR(62) NOT NULL,user_pass VARCHAR(62) NOT NULL ,user_name VARCHAR(62) NOT NULL DEFAULT \"Anonymous\", PRIMARY KEY(account_id) , UNIQUE(user_id)) AUTO_INCREMENT=2000;");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS chessgame.game(game_id INT UNSIGNED NOT NULL AUTO_INCREMENT, host_id VARCHAR(62) NOT NULL, player_id VARCHAR(62) NOT NULL, record TEXT NOT NULL, PRIMARY KEY(game_id), FOREIGN KEY(host_id) REFERENCES chessgame.login(user_id) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(player_id) REFERENCES chessgame.login(user_id) ON DELETE CASCADE ON UPDATE CASCADE) AUTO_INCREMENT=40000;");
			//CREATE TABLE IF NOT EXISTS chessgame.game(game_id INT(20) UNSIGNED NOT NULL AUTO_INCREMENT, host_id VARCHAR(32) NOT NULL, player_id VARCHAR(32) NOT NULL, record TEXT NOT NULL, PRIMARY KEY(game_id), FOREIGN KEY(host_id) REFERENCES chessgame.login(user_id) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(player_id) REFERENCES chessgame.login(user_id) ON DELETE CASCADE ON UPDATE CASCADE) AUTO_INCREMENT=20000;
		}catch(SQLException e){System.out.println("SQL: "+e.toString());}
	}
	
	public int checkaccount(String account){
		int result = -1;
		try{
			PreparedStatement prstmt = null;
			prstmt = connection.prepareStatement("SELECT COUNT(*) FROM chessgame.login WHERE user_id = ?");
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
			prstmt = connection.prepareStatement("SELECT user_pass FROM chessgame.login WHERE user_id = ?");
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
			prstmt = connection.prepareStatement("INSERT INTO chessgame.login VALUES(?,?,?,DEFAULT)");
			prstmt.setNull(1,Types.INTEGER);
			prstmt.setString(2,account);
			prstmt.setString(3,password);
			prstmt.execute();
			prstmt.close();
			return true;
		}catch(SQLException e){System.out.println("MySQL: Register Error: "+e.toString()); initialize(); return false;}
	}
	
	public String getUserName(String account){
		try{
			PreparedStatement prstmt = null;
			prstmt = connection.prepareStatement("SELECT user_name FROM chessgame.login WHERE user_id = ?");
			prstmt.setString(1,account);
			resultset = prstmt.executeQuery();
			if(resultset.next()) return resultset.getString(1);
		}catch(SQLException e){System.out.println("MySQL: "+e.toString());}
		return null;
	}
	
	public void End(){
		try {
			connection.close();
		} catch (SQLException e) { System.out.println("MySQL: "+e.toString()); }
	}
}

