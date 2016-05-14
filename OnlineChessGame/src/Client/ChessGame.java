package Client;

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
	JLabel information 	= new basicLabel("",460,420,140,30);
	
	boolean	myturn	= false;
	Chess	choose	= null;
	int		side	= 0;
	
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
		//System.out.println("selectChess("+x+","+y+")");
		choose = getChess(x,y);
		if(choose!=null){
			if(choose.side!=side) choose = null; 
			else information.setText(choose.getClass().getSimpleName());
		}
	}
	public Chess getChess(int x,int y){
		//System.out.println("getChess("+(x*50+25)+","+(y*50+25)+")");
		Chess chess = null;
		if(chessboard.getComponentAt((x*50+25),(y*50+25)) instanceof Chess){
			chess = (Chess) chessboard.getComponentAt((x*50+25),(y*50+25));
		}
		return chess;
	}
	public void moveChess(int px,int py,int x,int y){
		if(!myturn){ py = 7-py; y = 7-y; }
		System.out.println("moveChess("+px+","+py+","+x+","+y+")");
		Chess chess = getChess(px,py);
		Chess enemy = getChess(x,y);
		if(enemy!=null){ chessboard.remove(enemy); }
		chess.moveto(x,y);
		myturn = !myturn;	// switch turn
	}
	public boolean checkAction(int x,int y){
		boolean result = false;
		//System.out.println("checkAction("+x+","+y+") start");
		if(choose!=null){
			int px = ((int)choose.getLocation().getX()-25)/50;
			int py = ((int)choose.getLocation().getY()-25)/50;
			//System.out.println("checkMove("+px+","+py+","+x+","+y+") start");
			if(choose.checkMove(px,py,x,y)){
				if(!(choose instanceof Knight)){
					//System.out.println("checkKnight End "+!(choose instanceof Knight));
					if(checkPath(px,py,x,y)) result = true;
				}
				else result = true;
			}
			if(result){
				if(choose instanceof Pawn){
					//System.out.println("checkPawn End "+(choose instanceof Pawn));
					if(Math.abs(x-px)!=0){
						if(getChess(x,y)==null) result = false;
						else{
							//System.out.println("Enemy Exsist");
							if(getChess(x,y).side==choose.side) result = false;
						}
					}
					else{
						if(getChess(x,y)!=null) result = false;
						else choose.firstmove = false;
					}
				}
				else{
					if(getChess(x,y)!=null){
						if(getChess(x,y).side==choose.side) result = false;
					}
				}
			}
		}
		return result;
	}
	public boolean checkPath(int px,int py,int x,int y){
		boolean result = true;
		int mx = Math.abs(x-px);
		int my = Math.abs(y-py);
		int dx = (x-px)/getAbs(x-px);
		int dy = (y-py)/getAbs(y-py);
			
		while(mx+my>0){
			if(mx>0){ px+=dx; mx--;	} 
			if(my>0){ py+=dy; my--;	}
			if(px!=x||py!=y){
				if(getChess(px,py)!=null) result = false;
			}
		}
		return result;
	}
	public void initialize(int side){
		if(side==1) myturn = true;
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
	public void showinfo(String information){
		this.information.setText(information);
	}
	
	public Point getMouse(){
		return chessboard.getMousePosition();
	}
	public int getAbs(int value){
		if(Math.abs(value)==0) return 1; 
		else return Math.abs(value);
	}
}

abstract class Chess extends basicLabel {
	private BufferedImage display = null;
	
	private String ImagePath = "Chess/";
	protected boolean firstmove = true;
	int side = 0;
	
	public Chess(int side,int x, int y){
		super("",0,0,50,50);
		moveto((x-1),(y-1));
		
		if(side==1){
			ImagePath += "w"; this.side = 1;
		}
		else{
			ImagePath += "b"; this.side = -1;
		}
	}
	public void moveto(int x,int y){	// (0,0)-(7,7)
		if(x>-1&&x<8&&y>-1&&y<8) setLocation(25+50*x,25+50*y);
	}
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = true;
		if(px==x&&py==y) result = false;
		return result;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)*Math.abs(y-py)<=1&&Math.abs(x-px)<=1&&Math.abs(y-py)<=1) result = true;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)==Math.abs(y-py)||Math.abs(x-px)*Math.abs(y-py)==0) result = true;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)>0&&Math.abs(x-px)==Math.abs(y-py)) result = true;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)==3&&Math.abs(x-px)<3&&Math.abs(y-py)<3) result = true;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(Math.abs(x-px)+Math.abs(y-py)>0&&Math.abs(x-px)*Math.abs(y-py)==0) result = true;
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
	public boolean checkMove(int px,int py,int x,int y){
		boolean result = false;
		if(super.checkMove(px,py,x,y)){
			if(y-py<0&&Math.abs(x-px)+Math.abs(py-y)<=2){
				if(Math.abs(y-py)==2&&!firstmove) result = false;
				else result = true;
			}
		}
		return result;
	}
}

class GameListener implements MouseListener{
	private ChessGame 		chessgame	= null;
	private ClientThread 	controller	= null;
	
	public GameListener(ChessGame chessgame,ClientThread controller){
		this.chessgame	= chessgame;
		this.controller = controller;
	}
	
	public void mouseClicked(MouseEvent arg0) {
		int x = 0; int y = 0;
		if(chessgame.getMouse()!=null){
			x = (int) chessgame.getMouse().getX();
			y = (int) chessgame.getMouse().getY();
		}
		
		if(x>25&&x<425&&y>25&&y<425){
			x = (x-25)/50; y = (y-25)/50;
			if(chessgame.choose==null){
				chessgame.selectChess(x,y);
			}
			else{
				if(chessgame.myturn){
					boolean result = chessgame.checkAction(x,y);
					if(result){
						System.out.println("checkAction End "+result);
						int px = (int)(chessgame.choose.getLocation().getX()-25)/50;
						int py = (int)(chessgame.choose.getLocation().getY()-25)/50;
						chessgame.choose = null;
						controller.send("Move|"+px+"|"+py+"|"+x+"|"+y+"|");
					}
				}
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