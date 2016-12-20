package Client;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ChessGame extends basicFrame {
	
	/* Custom type variable */
	public 	Chess			choose		= null;
	
	private InvisibleChess	enpassant	= null;
	private ClientThread 	controller	= null;
	private JLabel 			chessboard 	= new basicLabel("",0,0,450,450);
	private JLabel			orderboard	= new basicLabel("",0,0,450,450);
	private JLabel 			information = new basicLabel("",460,420,140,30);
	private JLabel			L_player	= new basicLabel("PLAYER",481,18,100,25);
	private JLabel			L_symbol	= null;	// 確定顏色後生成

	/* Default type variable */
	private JButton giveup		= new basicButton("放棄比賽",467,278,100,25);
	private JButton surrender	= new basicButton("發起投降",467,318,100,25);
	private JButton leaveRoom	= new basicButton("離開房間",467,358,100,25);
	private JButton leaveGame	= new basicButton("離開遊戲",467,398,100,25);
	
	public int		turn	= Constant.WHITE_SIDE;
	public int		side	= Constant.OBSERVER;
	
	/* Constructor */
	public ChessGame(ClientThread controller){
		super("ChessGame",300,200,590,478);
		addWindowListener(new CloseListener(controller));
		addMouseListener(new GameListener(this,controller));
		giveup.addActionListener(new SendListener(controller,"Giveup"));
		surrender.addActionListener(new SendListener(controller,"Surrender"));
		leaveRoom.addActionListener(new SendListener(controller,"Quit"));
		leaveGame.addActionListener(new SendListener(controller,"End"));
		
		//add(giveup);
		add(surrender);
		add(leaveRoom);
		add(leaveGame);
		add(orderboard);
		add(chessboard);
		add(L_player);
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
				///*
				if(choose instanceof King && Math.abs(x-px)==2){
					result = false;
					if(choose.firstmove){
						int rx = 0; // x position for Rook
						if(x>px){ // Right Side Castling
							rx = 7;
						}
						
						Chess rook = getChess(rx,py);
						if(rook != null){
							if(rook instanceof Rook){
								if(rook.firstmove){
									if(checkPath(rx,py,px,py)){
										System.out.println("Anti bot start! "+x+","+y);
										result = true;
										int dx = (x-px)/getAbs(x-px);
										int i = px;
										while(i!=(x+dx)){
											System.out.println("anti("+i+","+py+")");
											if(!anti(i,py)){
												System.out.println("Break!");
												result = false;
												break;
											}
											i+=dx;
										}
									}
								}
							}
						}
					}
				}
				//*/
				if(choose instanceof Pawn){					// check if class is Pawn
					if(Math.abs(x-px)!=0){					// check if it has lateral vector
						if(getChess(x,y)==null) result = false;
						else{
							if(getChess(x,y).side==choose.side) result = false;
						}
					}
					else{
						if(getChess(x,y)!=null) result = false;
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
	public Boolean anti(int px,int py){
		Boolean result = true;
		ArrayList<Chess> allclass = new ArrayList<Chess>();
		allclass.add(new King(side,1,1));
		allclass.add(new Queen(side,1,1));
		allclass.add(new Bishop(side,1,1));
		allclass.add(new Knight(side,1,1));
		allclass.add(new Rook(side,1,1));
		allclass.add(new Pawn(side,1,1));
		for(int x=0;x<8;x++){
			for(int y=0;y<8;y++){
				Chess chess = getChess(x,y);
				if(chess!=null){
					if(chess.side!=side){
						for(int i=0;i<allclass.size();i++){
							if(allclass.get(i).checkMove(px, py, x, y)){
								if(chess.getClass().equals(allclass.get(i).getClass())){
									if(chess instanceof Knight){
										return false;
									} else {
										if(checkPath(px,py,x,y)){
											return false;
										}
									}	
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	public void peek(){
		for(int x=0;x<8;x++){
			for(int y=0;y<8;y++){
				if(checkAction(x,y)){
					Chess chess = getChess(x,y);
					if(chess==null){
						addPeek(x,y);
					} else {
						if(chess instanceof InvisibleChess){
							if(turn==side){
								addPeek(x,y);
								if(choose instanceof Pawn){
									addTarget(x,y);
								}
							}
						} else {
							addTarget(x,y);
							addOrder(x,y);
						}
					}
				}
			}
		}
	}
	public boolean checkPath(int px,int py,int x,int y){ // check if the path is empty
		
		//System.out.println("CheckPath("+px+","+py+","+x+","+y+")");
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
	public void setSide(int side){
		this.side = side;
		if(L_symbol!=null){
			remove(L_symbol);
		}
		L_symbol = new Symbol(side,1,1);
		L_symbol.setBounds(487,43,62,71);
		add(L_symbol);
	}
	public void initialize(int side){
		/* Set user information */
		turn = Constant.WHITE_SIDE;
		setSide(side);
		removeChess();		// remove all Chess on board
		removeOrder();		// remove all Mark on board
		
		int color = 0;
		int reverse = 0;
		if(side == Constant.OBSERVER){
			color = Constant.WHITE_SIDE;
			giveup.setVisible(false);
			surrender.setVisible(false);
		}
		else{
			color = side;
			giveup.setVisible(true);
			surrender.setVisible(true);
		}
		
		if(color == Constant.BLACK){
			reverse = 9;
		}
		
		/* Create all the Chess (User View) */
		addChess(new King(color,color*(5-reverse),8));
		addChess(new King(color*(-1),color*(5-reverse),1)); 
		addChess(new Queen(color,color*(4-reverse),8));
		addChess(new Queen(color*(-1),color*(4-reverse),1));
		addChess(new Bishop(color,color*(6-reverse),8));
		addChess(new Bishop(color,color*(3-reverse),8));
		addChess(new Bishop(color*(-1),color*(6-reverse),1));
		addChess(new Bishop(color*(-1),color*(3-reverse),1));
		addChess(new Knight(color,color*(7-reverse),8));
		addChess(new Knight(color,color*(2-reverse),8));
		addChess(new Knight(color*(-1),color*(7-reverse),1));
		addChess(new Knight(color*(-1),color*(2-reverse),1));
		addChess(new Rook(color,color*(8-reverse),8));
		addChess(new Rook(color,color*(1-reverse),8));
		addChess(new Rook(color*(-1),color*(8-reverse),1));
		addChess(new Rook(color*(-1),color*(1-reverse),1));
		for(int i=1;i<=8;i++){
			addChess(new Pawn(color,color*(i-reverse),7));
			addChess(new Pawn(color*(-1),color*(i-reverse),2));
		}
		repaint();
	}
	public void showinfo(String information){			// set Text display on information
		this.information.setText(information);
	}
	public Point getMouse(){ 							// get user Mouse position
		return orderboard.getMousePosition();
	}
	public void selectChess(int x,int y){ 				// choose which Chess Object to move
		//System.out.println("selectChess("+x+","+y+")");
		choose = getChess(x,y);
		if(choose!=null){
			if(choose.side != side) choose = null;
			else {
				information.setText(choose.getClass().getSimpleName()+" ("+x+":"+y+")");
				addOrder(x,y);
				peek();
			}
		}
	}
	public void moveChess(int px,int py,int x,int y,int cc){	// move the Chess Object
		
		if(turn != side && side != Constant.OBSERVER){ // if not my turn the movement of y is opposite
			py = 7-py;
			y = 7-y;
		}
		else{
			if(side == Constant.OBSERVER){
				if(turn != Constant.WHITE_SIDE){
					py = 7-py;
					y = 7-y;
				}
			}
		}
		
		if( side == Constant.BLACK){
			px = 7-px;
			x = 7-x;
		}
		
		removeOrder();
		
		//System.out.println("moveChess("+px+","+py+","+x+","+y+")");
		/* Check if enemy exist */
		Chess enemy = getChess(x,y);
		Chess chess = getChess(px,py);
		if(enemy!=null){
			if(enemy instanceof King){
				if(side != enemy.side){	// Winner notice Server
					controller.send("Win");
				}
			}
			else if(enemy instanceof InvisibleChess){
				if(chess instanceof Pawn){
					enemy = enpassant.getTarget();
				}
			}
			chessboard.remove(enemy);	// remove Chess
		}
		if(enpassant != null){
			resetEnPassant();
		}
		/* Move the Selected Chess */
		
		if(chess instanceof Pawn && Math.abs(py-y)==2){
			setEnPassant((px+x)/2,(py+y)/2);
			enpassant.setTarget(chess);
		}
		if(chess instanceof King && Math.abs(px-x)==2){
			int rx = 0; // x position for Rook
			if(x>px){ // Right Side Castling
				rx = 7;
			}
			Chess rook = getChess(rx,y);
			rook.moveto((px+x)/2,y);
		}
				
		/* Promotion */
		if(cc>Constant.PAWN){
			int side = chess.side;
			chessboard.remove(chess);
			chess = null;
			switch(cc){
				case Constant.ROOK:
					chess = new Rook(side,1,1);
					break;
				case Constant.BISHOP:
					chess = new Bishop(side,1,1);
					break;
				case Constant.KNIGHT:
					chess = new Knight(side,1,1);
					break;
				case Constant.QUEEN:
					chess = new Queen(side,1,1);
			}
			chess.moveto(x,y);
			addChess(chess);
		} else {
			chess.moveto(x,y);
		}
		addOrder(x,y);
		repaint();
		turn = turn*(-1);	// switch turn
	}
	public void Surrender(){
		controller.send("Surrender");
	}
	public void Endgame(int side){
		String message = null;
		turn = Constant.RANDOM;
		if(side == Constant.WHITE){
			message = "White Win !";
		}
		else{
			message = "Black Win !";
		}
		System.out.println(message);
		JDialog dialog = new messageDialog(controller,"Quit",message);
	}
	private Chess getChess(int x,int y){ // get Chess Object on board
		Chess chess = null;
		if(chessboard.getComponentAt((x*50+25),(y*50+25)) instanceof Chess){
			chess = (Chess) chessboard.getComponentAt((x*50+25),(y*50+25));
		}
		return chess;
	}
	public void resetEnPassant(){
		chessboard.remove(enpassant);
		enpassant = null;
	}
	public void setEnPassant(int x,int y){
		enpassant = new InvisibleChess(x,y);
		chessboard.add(enpassant);
	}
	private void addChess(Chess chess){ // add Chess to the board
		chessboard.add(chess);
	}
	private void addPeek(int x,int y){
		Peek peek = new Peek(x,y);
		orderboard.add(peek);
	}
	private void addTarget(int x,int y){
		Target target = new Target(x,y);
		orderboard.add(target);
	}
	private void addOrder(int x,int y){
		Order order = new Order(x,y);
		orderboard.add(order);
	}
	private void removeChess(){ // remove all the Chess on board
		chessboard.removeAll();
	}
	public void removeOrder(){
		orderboard.removeAll();
		orderboard.repaint();
	}
	public int getAbs(int value){ // change absolute 0 to 1 (divide zero handle)
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
		super("",50*x-25,50*y-25,50,50);	// set Size
		
		/* set ImagePath */
		if(side==Constant.WHITE_SIDE){
			this.side = Constant.WHITE;
			ImagePath += "w";
		}
		else if(side==Constant.BLACK_SIDE){
			this.side = Constant.BLACK;
			ImagePath += "b";
		}
	}
	public void moveto(int x,int y){ // move the chess to (x,y)
		firstmove = false;
		if(x>-1&&x<8&&y>-1&&y<8){ // check if the move is inside the board
			setLocation(25+50*x,25+50*y);
		}
	}
	public boolean checkMove(int px,int py,int x,int y){	// check if the Chess move
		boolean result = true;
		if(px==x&&py==y) result = false;
		return result;
	}
	public void setDisplay(){								// set display Image for Chess
		ImagePath += this.getClass().getSimpleName()+".png";
		try{
			display = ImageIO.read(new File(ImagePath));
			this.setIcon(new ImageIcon(display));
		} catch (IOException e) { System.out.println("Can't find Image file: "+ImagePath); }
	}
}
class Symbol extends Chess {
	public Symbol(int side ,int x,int y){
		super(side,1,1);
		setDisplay();
		setVisible(true);
	}
}
class InvisibleChess extends Chess {
	Chess target = null;
	public InvisibleChess(int x,int y){
		super(0,x+1,y+1);
		//setDisplay();
		//setVisible(true);
	}
	public void setTarget(Chess chess){
		target = chess;
	}
	public Chess getTarget(){
		return target;
	}
}
class CellImage extends basicLabel {
	
	private BufferedImage display = null;
	private String ImagePath = "Chess/";
	
	public CellImage(int x, int y){
		super("",0,0,50,50);	// set Size
		
		ImagePath += this.getClass().getSimpleName()+".png";
		
		try{
			display = ImageIO.read(new File(ImagePath));
			this.setIcon(new ImageIcon(display));
		} catch (IOException e) { System.out.println("Can't find Image file: "+ImagePath); }
		
		if(x>-1&&x<8&&y>-1&&y<8){ // check if the move is inside the board
			setLocation(25+50*x,25+50*y);
		}
		
		setVisible(true);
	}
}
class Peek extends CellImage {
	public Peek(int x, int y) {
		super(x,y);
	}
}
class Order extends CellImage {
	public Order(int x, int y) {
		super(x,y);
	}
}
class Target extends CellImage {
	public Target(int x, int y) {
		super(x,y);
	}
}
/* Chess Class */
class King extends Chess {
	public King(int side, int x, int y){
		super(side, x, y);
		setDisplay();
		setVisible(true);
	}
	public boolean checkMove(int px,int py,int x,int y){ // implements class movement rule
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)<=2&&Math.abs(x-px)<=2&&Math.abs(y-py)<=1){
				result = true;
			}
		}
		return result;
	}
}
class Queen extends Chess {
	public Queen(int side, int x, int y){
		super(side, x, y);
		setDisplay();
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
		setDisplay();
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
		setDisplay();
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
		setDisplay();
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
		setDisplay();
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

class messageDialog extends basicDialog {
	
	private JLabel		message = new JLabel("Text here.",SwingConstants.CENTER);
	private JButton		agree	= new basicButton("同意",32,70,80,24);
	private JButton		confirm = new basicButton("取消",172,70,80,24);
	
	public messageDialog(ClientThread controller,String type,String text){
		super("Message",200,165,290,140);
		setVisible(true);
		message.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
		message.setBounds(25,15,235,30);
		confirm.addActionListener(new messageLisitener(this));
		agree.addActionListener(new messageLisitener(this));
		add(message);
		add(confirm);
		add(agree);
		
		SendListener sendlistener = null;
		message.setText(text);
		if(type.equals("Quit")){
			agree.setText("離開");
		}
		sendlistener = new SendListener(controller,type);
		agree.addActionListener(sendlistener);
	}
	
	class messageLisitener implements ActionListener{

		private JDialog messageDialog = null;
		
		public messageLisitener(JDialog messageDialog) {
			this.messageDialog = messageDialog;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			messageDialog.dispose();
		}
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
		
		chessgame.removeOrder();
		
		if(x>25&&x<425&&y>25&&y<425){ // check if the mouse is inside the board
			
			x = (x-25)/50; y = (y-25)/50;				// format the position to fit the board
			if(chessgame.choose==null){					// check if the choice has be done
				chessgame.selectChess(x,y);				// choose a Chess to move
			}
			else{
				if(chessgame.turn == chessgame.side){	// move only available when it's my turn
					if(chessgame.checkAction(x,y)){		// check if the movement is legal
						
						/* get the position of Chess */
						int px = (int)(chessgame.choose.getLocation().getX()-25)/50;
						int py = (int)(chessgame.choose.getLocation().getY()-25)/50;
				
						if( chessgame.side == Constant.BLACK){
							px = 7-px;
							x = 7-x;
						}
						
						/* send command to Server */
						String message = "Move"	+Constant.DELIMITER+px
												+Constant.DELIMITER+py
												+Constant.DELIMITER+x
												+Constant.DELIMITER+y
												+Constant.DELIMITER;
								
						if( chessgame.choose instanceof Pawn  ){
							if( y==0 || y==7 ){
								System.out.println("Promotion!");
								new setClass(controller,message);
							} else {
								controller.send(message+"0"+Constant.DELIMITER);
							}
						}else{
							
							controller.send(message+"0"+Constant.DELIMITER);
						}
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