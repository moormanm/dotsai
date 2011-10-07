import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class GameBoardPanel extends JLayeredPane {
	
	final DrawingPanel drawingPanel = new DrawingPanel();
	private class DrawingPanel extends JPanel{
		public static final int margin = 20;
		public DrawingPanel() {
			setLayout(null);
			setSize(500,500);
			setVisible(true);
			setOpaque(false);

		}
		 public void paintComponent(Graphics g_) {
			    super.paintComponent(g_);
			    int m = margin;
			    Graphics2D g = (Graphics2D)g_;
			    int lw = 3;
			    int u = unitSize;
			    GameState.Player p;
			    //Draw the X segments
			    for(int y=0; y < gameState.dimY; y++) {
			    	for(int x=0; x < gameState.dimX -1; x++) {
			    		if( (p = gameState.segX[y][x]) !=null) {
			    			if(p == GameState.Player.P1) {
			    				g.setColor(p1seg);
			    			}
			    			else {
			    				g.setColor(p2seg);
			    			}
			    			rect.setRect(m+x*u, m+y*u, u, lw);
			    			g.fill(rect);
			    		}
			    	}
			    }
			    
			    //Draw the Y segments
			    for(int y=0; y < gameState.dimY-1; y++) {
			    	for(int x=0; x < gameState.dimX; x++) {
			    		if( (p = gameState.segY[y][x]) !=null) {
			    			if(p == GameState.Player.P1) {
			    				g.setColor(p1seg);
			    			}
			    			else {
			    				g.setColor(p2seg);
			    			}
			    			rect.setRect(m+x*u, m+y*u, lw, u);
			    			g.fill(rect);
			    		}
			    	}
			    }
			    
			    //Draw the claimed areas
			    for(int y=0; y < gameState.dimY; y++) {
			    	for(int x=0; x < gameState.dimX; x++) {
			    		p = gameState.claimedUnits[y][x];
			    		if( p == null) {
			    			continue;
			    		}
			    		else if(p == GameState.Player.P1) {
			    			g.setColor(Color.RED);
			    		}
			    		else if(p == GameState.Player.P2) {
			    			g.setColor(Color.BLUE);
			    		}
			    		
			    		//Draw the box
			    		rect.setRect(m+x*u+lw,m+y*u+lw, u-lw, u-lw);
			    		g.fill(rect);
			    	}
			    }
			    
			  }
	}
	
	class BoardButtonListener implements ActionListener {

		DotsButton lastClicked;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			DotsButton db = (DotsButton)e.getSource();
			if(lastClicked == null) {
				lastClicked = db;
				return;
			}
			
			if(lastClicked == db) {
				db.setSelected(true);
				return;
			}
			
			//unset last clicked
			assert(lastClicked.isSelected());
			//lastClicked.setSelected(false);
			
			//Check validity of move
			int xDist = Math.abs(lastClicked.x - db.x);
			int yDist = Math.abs(lastClicked.y - db.y);
			
			if((xDist > 1 || yDist > 1) || (xDist == 1 && yDist == 1)) {
				//not valid
				lastClicked.setSelected(false);
				lastClicked=db;
				return;
			}
			
			//Make the move
			Segment s;
			
			
			
			if(yDist == 1) {
			  int minY = Math.min(db.y, lastClicked.y);
			  s = new Segment(db.x, minY, true );
			}
			else {
			  int minX = Math.min(db.x, lastClicked.x);
			  s = new Segment(minX, db.y, false);
			}
			
			if(!gameState.isMoveValid(s)) {
				//not valid
				System.out.println("Not valid");
				lastClicked.setSelected(false);
				lastClicked=db;
				return;
			}
			
			
			int b4 = gameState.getClaimedArea(GameState.Player.P1);
			gameState.doMove(s, GameState.Player.P1);
			drawingPanel.repaint();
			lastClicked.setSelected(false);
		    lastClicked = db;
		    
		    //If no area claimed, computer's turn
		    if(gameState.getClaimedArea(GameState.Player.P1) - b4 == 0) {
			  AI ai = new AI(gameState);
			  ai.takeTurn();
			  drawingPanel.repaint();
		    }
			
			
		}
		
	}
	
	class DotsButton extends JRadioButton {
		public int x;
		public int y;
		
	}
	public int unitSize = 50;
	GameState gameState;
	JRadioButton buttons[][];
	public GameBoardPanel(GameState gs) {
		
		setPreferredSize(new Dimension(600,600));
		gameState = gs;
		
		add(drawingPanel, JLayeredPane.DEFAULT_LAYER);
		
		BoardButtonListener boardButtonListener = new BoardButtonListener();
		
		buttons = new DotsButton[gs.dimY][gs.dimX];
		
		//Build the radio button array
		for(int y=0; y < gameState.dimY; y++) {
			for(int x=0; x < gameState.dimX; x++) {
				
				DotsButton j = new DotsButton(); 
				buttons[y][x] = j;
				j.x = x; j.y = y;
				j.setLocation(new Point(DrawingPanel.margin + x*unitSize - (int) Math.round((j.getPreferredSize().width / 2.25)),
						                DrawingPanel.margin + y*unitSize - (int) Math.round((j.getPreferredSize().height / 2.25))));
				j.setSize(j.getPreferredSize());
				j.setOpaque(false);
				j.addActionListener(boardButtonListener);
				add(j, JLayeredPane.POPUP_LAYER);
			}
		}
		
		

		
		
	}
	
	private Color p1seg = new Color(150,0,0);
	private Color p2seg = new Color(0,0,150);
	private static Rectangle2D.Double rect = new Rectangle2D.Double();
	 
	
	  public GameState.Player randomPlayer() {
		  if(Math.random() > .5) return GameState.Player.P1;
		  return GameState.Player.P2;
	  }
	public void startNewGame() {
		/*
		for(int i=0; i< gameState.dimY; i++) {
			for(int j=0; j< gameState.dimX; j++) {
				
				if(i != gameState.dimY-1) {
					gameState.doMove(new Segment(j,i,true), randomPlayer());	
				}
				if(j != gameState.dimX-1) {
					gameState.doMove(new Segment(j,i,false), randomPlayer());	
				}
				
				
			}
		}
		
		gameState.claimedUnits[7][7] = GameState.Player.P1;
		*/
		//test

		
		

		
	}
}
