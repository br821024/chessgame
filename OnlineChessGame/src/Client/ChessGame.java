package Client;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ChessGame extends basicFrame {
	
	ClientThread controller = null;
	JLabel chessboard 	= new basicLabel("",0,0,450,450);
	JLabel information 	= new basicLabel("",460,425,140,20);
	
	Chess choose = null;
	int side = 0;
	
	public ChessGame(ClientThread controller){
		super("ChessGame",300,200,600,488);
		add(chessboard);
		add(information);
		addWindowListener(new CloseListener(controller));
		addMouseListener(new GameListener(this,controller));
		this.controller = controller;

		try {
			BufferedImage Image = ImageIO.read(new File("Chess/Chessboard.png"));
			chessboard.setIcon(new ImageIcon(Image));
		}
		catch (IOException e) { System.out.println("Game: "+e.toString()); }
	}
	
	public void selectChess(int x,int y){
		information.setText(chessboard.getComponentAt(x,y).getClass().getSimpleName());
		if(chessboard.getComponentAt(x,y) instanceof Chess){
			choose = (Chess)chessboard.getComponentAt(x,y);
			information.setText(choose.getClass().getSimpleName());
		}
	}
	public void checkAction(int x,int y){
		
	}
	
	public void initialize(int side){
		this.side = side;
		removeChess();
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
	public void addChess(Chess chess){
		chessboard.add(chess);
	}
	public void removeChess(){
		chessboard.removeAll();
	}
	public Point getMouse(){
		return chessboard.getMousePosition();
	}
	public void showinfo(String information){
		this.information.setText(information);
	}
}

abstract class Chess extends basicLabel {
	private BufferedImage display = null;
	
	private String ImagePath = "Chess/";
	private boolean firstmove = true;
	
	public Chess(int side,int x, int y){
		super("",0,0,50,50);
		moveto((x-1),(y-1));
		
		if(side==1) ImagePath += "w";
		else ImagePath += "b";
	}
	public void moveto(int x,int y){
		if(x>-1&&x<8&&y>-1&&y<8) setLocation(25+50*x,25+50*y);
	}
	public void setdisplay(){
		ImagePath += this.getClass().getSimpleName()+".png";
		try{
			display = ImageIO.read(new File(ImagePath));
			this.setIcon(new ImageIcon(display));
		} catch (IOException e) { System.out.println("Can't find Image file: "+ImagePath); }
	}
}
class King extends Chess {
	public King(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}
class Queen extends Chess {
	public Queen(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}
class Bishop extends Chess {
	public Bishop(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}
class Knight extends Chess {
	public Knight(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}
class Rook extends Chess {
	public Rook(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}
class Pawn extends Chess {
	public Pawn(int side, int x, int y){
		super(side, x, y);
		setdisplay();
		setVisible(true);
	}
}

class GameListener implements MouseListener{
	
	private ClientThread	controller	= null;
	private ChessGame 		chessgame	= null;
	
	public GameListener(ChessGame chessgame,ClientThread controller){
		this.controller	= controller;
		this.chessgame	= chessgame;
	}
	
	public void mouseClicked(MouseEvent arg0) {
		int x = (int) chessgame.getMouse().getX();
		int y = (int) chessgame.getMouse().getY();
		
		if(chessgame.choose==null) chessgame.selectChess(x,y);
		else chessgame.checkAction(x,y);
		
		//x/=50; y/=50;
		//chessgame.showinfo("[X="+(char)(65+x)+",Y="+(y+1)+"]");
		//chessgame.showinfo("[X="+x+",Y="+y+"]");
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
}