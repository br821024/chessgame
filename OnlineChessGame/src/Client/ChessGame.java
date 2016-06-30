package Client;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class ChessGame extends basicFrame {
	
	/* Custom type variable */
	private ClientThread controller = null;
	private JLabel chessboard 	= new basicLabel("",0,0,450,450);
	private JLabel information 	= new basicLabel("",460,420,140,30);
	
	/* Default type variable */
	private JButton giveup		= new basicButton("放棄比賽",467,278,100,25);
	private JButton surrender	= new basicButton("發起投降",467,318,100,25);
	private JButton leaveRoom	= new basicButton("離開房間",467,358,100,25);
	private JButton leaveGame	= new basicButton("離開遊戲",467,398,100,25);
	
	public boolean	myturn	= false;
	public Chess	choose	= null;
	public int		side	= Constant.OBSERVER;
	
	/* Constructor */
	public ChessGame(ClientThread controller){
		super("ChessGame",300,200,600,488);
		addWindowListener(new CloseListener(controller));
		addMouseListener(new GameListener(this,controller));
		add(giveup);
		add(surrender);
		add(leaveRoom);
		add(leaveGame);
		add(chessboard);
		add(information);
		this.controller = controller;

		try {
			BufferedImage Image = ImageIO.read(new File("Chess/Chessboard.png"));
			chessboard.setIcon(new ImageIcon(Image));	// set Background Image
		}
		catch (IOException e) { System.out.println("Game: "+e.toString()); }
	}

	public boolean checkAction(int x,int y){ // check if the movement is legal
		boolean result = false;
		//System.out.println("checkAction("+x+","+y+") start");
		if(choose!=null){
			int px = ((int)choose.getLocation().getX()-25)/50;
			int py = ((int)choose.getLocation().getY()-25)/50;
			//System.out.println("checkMove("+px+","+py+","+x+","+y+") start");
			
			if(choose.checkMove(px,py,x,y)){				// check if move distance > 0
				if(!(choose instanceof Knight)){			// check if class is Knight
					if(checkPath(px,py,x,y)){				// check if the path is empty
						result = true;
					}
				}
				else{
					result = true;							// Knight class bypass the check 
				}
			}
			
			if(result){										// bypass other test if result is false
				if(choose instanceof Pawn){					// check if class is Pawn
					if(Math.abs(x-px)!=0){					// check if it has lateral vector
						if(getChess(x,y)==null) result = false;
						else{
							if(getChess(x,y).side==choose.side) result = false;
						}
					}
					else{
						if(getChess(x,y)!=null) result = false;
						else choose.firstmove = false;
					}
				}
				else{
					Chess enemy = getChess(x,y);
					if(enemy!=null){						// check if there is Chess on the destination
						if(enemy.side==choose.side){		// check if the Chess is on opposite side
							result = false;
						}
					}
				}
			}
		}
		return result;
	}
	public boolean checkPath(int px,int py,int x,int y){ // check if the path is empty
		
		boolean result = true;
		int mx = Math.abs(x-px);		// Lateral Movement
		int my = Math.abs(y-py);		// Straight Movement
		int dx = (x-px)/getAbs(x-px);	// Lateral Vector
		int dy = (y-py)/getAbs(y-py);	// Straight Vector
		
		/* go through the path and check if there is Chess on it */
		while(mx+my>0){
			if(mx>0){ px+=dx; mx--;	} 
			if(my>0){ py+=dy; my--;	}
			if(px != x || py != y){
				if(getChess(px,py)!=null) result = false;
			}
		}
		return result;
	}
	public void initialize(int side){
		/* Set user information */
		if(side==Constant.WHITE_SIDE){
			myturn = true;	// White side goes first
		}
		this.side = side;
		removeChess();		// remove all Chess on board
		
		/* Create all the Chess (User View) */
		addChess(new King(side,5,8));
		addChess(new King(side*(-1),5,1)); 
		addChess(new Queen(side,4,8));
		addChess(new Queen(side*(-1),4,1));
		addChess(new Bishop(side,6,8));
		addChess(new Bishop(side,3,8));
		addChess(new Bishop(side*(-1),6,1));
		addChess(new Bishop(side*(-1),3,1));
		addChess(new Knight(side,7,8));
		addChess(new Knight(side,2,8));
		addChess(new Knight(side*(-1),7,1));
		addChess(new Knight(side*(-1),2,1));
		addChess(new Rook(side,8,8));
		addChess(new Rook(side,1,8));
		addChess(new Rook(side*(-1),8,1));
		addChess(new Rook(side*(-1),1,1));
		for(int i=1;i<=8;i++){
			addChess(new Pawn(side,i,7));
			addChess(new Pawn(side*(-1),i,2));
		}
	}
	public void showinfo(String information){			// set Text display on information
		this.information.setText(information);
	}
	public Point getMouse(){ 							// get user Mouse position
		return chessboard.getMousePosition();
	}
	public void selectChess(int x,int y){ 				// choose which Chess Object to move
		//System.out.println("selectChess("+x+","+y+")");
		choose = getChess(x,y);
		if(choose!=null){
			if(choose.side!=side) choose = null; 
			else information.setText(choose.getClass().getSimpleName());
		}
	}
	public void moveChess(int px,int py,int x,int y){	// move the Chess Object
		if(!myturn){ // if not my turn the movement of y is opposite
			py = 7-py;
			y = 7-y;
		}
		System.out.println("moveChess("+px+","+py+","+x+","+y+")");
		Chess chess = getChess(px,py);	// get Chess to move
		/* Check if enemy exist */
		Chess enemy = getChess(x,y);
		if(enemy!=null){
			chessboard.remove(enemy);	// remove Chess
		}
		chess.moveto(x,y);
		myturn = !myturn;	// switch turn
	}
	private Chess getChess(int x,int y){ 				// get Chess Object on board
		//System.out.println("getChess("+(x*50+25)+","+(y*50+25)+")");
		Chess chess = null;
		if(chessboard.getComponentAt((x*50+25),(y*50+25)) instanceof Chess){
			chess = (Chess) chessboard.getComponentAt((x*50+25),(y*50+25));
		}
		return chess;
	}
	private void addChess(Chess chess){ 				// add Chess to the board
		chessboard.add(chess);
	}
	private void removeChess(){ 						// remove all the Chess on board
		chessboard.removeAll();
	}
	private int getAbs(int value){ 						// change absolute 0 to 1 (divide zero handle)
		if(Math.abs(value)==0) return 1; 
		else return Math.abs(value);
	}
}

abstract class Chess extends basicLabel {
	
	/* Default type variable */
	private BufferedImage display = null;
	
	private String ImagePath = "Chess/";
	protected boolean firstmove = true;
	int side = Constant.OBSERVER;
	
	public Chess(int side,int x, int y){
		super("",0,0,50,50);	// set Size
		moveto((x-1),(y-1));	// set Position (on board)
		
		/* set ImagePath */
		if(side==Constant.WHITE_SIDE){
			this.side = Constant.WHITE;
			ImagePath += "w";
		}
		else{
			this.side = Constant.BLACK;
			ImagePath += "b";
		}
	}
	public void moveto(int x,int y){ 						// move the chess to (x,y)
		if(x>-1&&x<8&&y>-1&&y<8){ // check if the move is inside the board
			setLocation(25+50*x,25+50*y);
		}
	}
	public boolean checkMove(int px,int py,int x,int y){	// check if the Chess move
		boolean result = true;
		if(px==x&&py==y) result = false;
		return result;
	}
	public void setdisplay(){								// set display Image for Chess
		ImagePath += this.getClass().getSimpleName()+".png";
		try{
			display = ImageIO.read(new File(ImagePath));
			this.setIcon(new ImageIcon(display));
		} catch (IOException e) { System.out.println("Can't find Image file: "+ImagePath); }
	}
}

/* Chess Class */
class King extends Chess {
	public King(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)*Math.abs(y-py)<=1&&Math.abs(x-px)<=1&&Math.abs(y-py)<=1){
				result = true;
			}
		}
		return result;
	}
}
class Queen extends Chess {
	public Queen(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)==Math.abs(y-py) || Math.abs(x-px)*Math.abs(y-py)==0){
				result = true;
			}
		}
		return result;
	}
}
class Bishop extends Chess {
	public Bishop(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)>0&&Math.abs(x-px)==Math.abs(y-py)){
				result = true;
			}
		}
		return result;
	}
}
class Knight extends Chess {
	public Knight(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)==3&&Math.abs(x-px)<3&&Math.abs(y-py)<3){
				result = true;
			}
		}
		return result;
	}
}
class Rook extends Chess {
	public Rook(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)>0&&Math.abs(x-px)*Math.abs(y-py)==0){
				result = true;
			}
		}
		return result;
	}
}
class Pawn extends Chess {
	public Pawn(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(y-py<0&&Math.abs(x-px)+Math.abs(py-y)<=2){
				if(Math.abs(y-py)==2 && !firstmove){
					result = false;
				}
				else{
					result = true;
				}
			}
		}
		return result;
	}
}

class GameListener implements MouseListener{
	
	/* Custom type variable */
	private ChessGame 		chessgame	= null;
	private ClientThread 	controller	= null;
	
	/* Constructor */
	public GameListener(ChessGame chessgame,ClientThread controller){
		this.chessgame	= chessgame;
		this.controller = controller;
	}
	
	public void mouseClicked(MouseEvent arg0) {
		
		/* get Mouse position on the board */
		int x = 0; int y = 0;
		if(chessgame.getMouse()!=null){
			x = (int) chessgame.getMouse().getX();
			y = (int) chessgame.getMouse().getY(); 
		}
		
		if(x>25&&x<425&&y>25&&y<425){ // check if the mouse is inside the board
			
			x = (x-25)/50; y = (y-25)/50;			// format the position to fit the board
			if(chessgame.choose==null){				// check if the choice has be done
				chessgame.selectChess(x,y);			// choose a Chess to move 
			}
			else{
				if(chessgame.myturn){				// move only available when it's my turn
					if(chessgame.checkAction(x,y)){ // check if the movement is legal
						
						//System.out.println("checkAction End");
						
						/* get the position of Chess */
						int px = (int)(chessgame.choose.getLocation().getX()-25)/50;
						int py = (int)(chessgame.choose.getLocation().getY()-25)/50;
				
						/* send command to Server */
						controller.send("Move"+Constant.DELIMITER+
											px+Constant.DELIMITER+
											py+Constant.DELIMITER+
											 x+Constant.DELIMITER+
											 y);
					}
				}
				
				/* release the Chess */
				chessgame.choose = null;
				chessgame.showinfo("");
			}
		}
		
		/*if(x>25&&x<425&&y>25&&y<425){
			x = (x-25)/50; y = (y-25)/50;
			chessgame.showinfo("[X="+(char)(65+x)+",Y="+(y+1)+"]");
			//chessgame.showinfo("[X="+x+",Y="+y+"]");
		}*/
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}	
}