import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


public class GameBoardPanel extends JLayeredPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JLabel p1Score = new JLabel();
	public JLabel p2Score = new JLabel();
	final DrawingPanel drawingPanel = new DrawingPanel();
	private class DrawingPanel extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public static final int margin = 20;
		public DrawingPanel() {
			setLayout(null);
			setSize(500,500);
			setVisible(true);
			setOpaque(false);

		}
		 @Override
		public void paintComponent(Graphics g_) {
			    super.paintComponent(g_);
			    int m = margin;
			    Graphics2D g = (Graphics2D)g_;
			    int lw = 3;
			    int u = unitSize;
			    GameState.Player p;
			    //Draw the X segments
			    for(int y=0; y < GameState.dimY; y++) {
			    	for(int x=0; x < GameState.dimX -1; x++) {
			    		if( (p = gameState.segX[y][x]) !=null) {
			    			if(p == GameState.Player.P1) {
			    				g.setColor(p1seg);
			    			}
			    			else {
			    				g.setColor(p2seg);
			    			}
			    			rect.setRect(m+x*u, m+y*u - (int)Math.round(lw / 2.0), u, lw);
			    			g.fill(rect);
			    		}
			    	}
			    }
			    
			    //Draw the Y segments
			    for(int y=0; y < GameState.dimY-1; y++) {
			    	for(int x=0; x < GameState.dimX; x++) {
			    		if( (p = gameState.segY[y][x]) !=null) {
			    			if(p == GameState.Player.P1) {
			    				g.setColor(p1seg);
			    			}
			    			else {
			    				g.setColor(p2seg);
			    			}
			    			rect.setRect(m+x*u - (int)Math.round(lw / 2.0), m+y*u, lw, u);
			    			g.fill(rect);
			    		}
			    	}
			    }
			    
			    //Draw the claimed areas
			    for(int y=0; y < GameState.dimY; y++) {
			    	for(int x=0; x < GameState.dimX; x++) {
			    		p = gameState.claimedUnits[y][x];
			    		if( p == null) {
			    			continue;
			    		}
			    		else if(p == GameState.Player.P1) {
			    			g.setColor(p1area);
			    		}
			    		else if(p == GameState.Player.P2) {
			    			g.setColor(p2area);
			    		}
			    		
			    		//Draw the box
			    		rect.setRect(m+x*u+ Math.round(lw/2), m+y*u+ Math.round(lw/2), u-lw, u-lw);
			    		g.fill(rect);
			    	}
			    }
			    
			    //Draw the lastMove
			    if(gameState.lastMove != null) {
			       int x = gameState.lastMove.x;
			       int y = gameState.lastMove.y;
			       
			       if(gameState.lastMove.isY) {
			    	   g.setColor( gameState.segY[y][x] == GameState.Player.P1 ? p1seg : p2seg);
			    	   rect.setRect(m+x*u  - (int)Math.round(lw*3 / 2.0), m+y*u, lw*3, u);
			       }
			       else {
			    	   g.setColor( gameState.segX[y][x] == GameState.Player.P1 ? p1seg : p2seg);
			    	   rect.setRect(m+x*u, m+y*u  - (int)Math.round(lw*3 / 2.0), u, lw*3);
			       }
			       g.fill(rect);
			    }
			    
			  }
	}
	
	class BoardButtonListener implements ActionListener {

		public DotsButton lastClicked;
		
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
				lastClicked.setSelected(false);
				lastClicked=db;
				
				return;
			}
			
			
			
			
			int b4 = gameState.getClaimedArea(GameState.Player.P1);
			gameState.doMove(s, GameState.Player.P1);
			drawingPanel.repaint();
			lastClicked.setSelected(false);
		    lastClicked = null;
		    db.setSelected(false);
		    
		    
		    //If no area claimed, computer's turn
		    if(gameState.getClaimedArea(GameState.Player.P1) - b4 == 0) {
			  AI ai = new AI(gameState);
			  ai.takeTurn(GameState.Player.P2);
			  drawingPanel.repaint();
		    }
		    
		    //Update score
		    p1Score.setText(Integer.toString(gameState.getClaimedArea(GameState.Player.P1)));
		    p2Score.setText(Integer.toString(gameState.getClaimedArea(GameState.Player.P2)));
		    
		    
		    //if no more open segments, lock the panel, notify that the game is over
		    if(gameState.hasOpenSegments() == false ) {
		    	handleGameOver();
		    }
		}
		
	}

	void handleGameOver() {
    	int p1 = gameState.getClaimedArea(GameState.Player.P1);
    	int p2 = gameState.getClaimedArea(GameState.Player.P2);
    	String str = "";
    	if(p1 > p2) {
    	  str = "Red wins!";	
    	}
    	else if (p1 == p2) {
    	  str = "Draw!";
    	}
    	else {
    	  str = "Blue wins!";
    	}
    	Utilities.showInfo(str, "Game Over");
    	
    	AI.commit();
    	
    	lockControls(true);
	}
	
	class DotsButton extends JRadioButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int x;
		public int y;
		
	}
	BoardButtonListener boardButtonListener = new BoardButtonListener();
	public int unitSize = 50;
	GameState gameState;
	JRadioButton buttons[][];
	public void lockControls(boolean val) {
		for(JRadioButton[] row : buttons) {
			for(JRadioButton b : row) {
				b.setEnabled(!val);
			}
		}
	}
	public GameBoardPanel(GameState gs) {
		
		setPreferredSize(new Dimension(600,600));
		gameState = gs;
		
		add(drawingPanel, JLayeredPane.DEFAULT_LAYER);
		
		
		
		buttons = new DotsButton[GameState.dimY][GameState.dimX];
		
		//Build the radio button array
		for(int y=0; y < GameState.dimY; y++) {
			for(int x=0; x < GameState.dimX; x++) {
				
				DotsButton j = new DotsButton(); 
				buttons[y][x] = j;
				j.x = x; j.y = y;
				j.setLocation(new Point(DrawingPanel.margin + x*unitSize - (int) Math.round((j.getPreferredSize().width / 2.0)),
						                DrawingPanel.margin + y*unitSize - (int) Math.round((j.getPreferredSize().height / 2.0))));
				j.setSize(j.getPreferredSize());
				j.setOpaque(false);
				j.addActionListener(boardButtonListener);
				add(j, JLayeredPane.POPUP_LAYER);
			}
		}
		
		JPanel scorePanel = new JPanel(new GridLayout(2,2, 15, 15));
		Utilities.standardBorder(scorePanel, "Score");
		scorePanel.setSize(120,90);
		scorePanel.setLocation(new Point(DrawingPanel.margin + GameState.dimX*unitSize,  DrawingPanel.margin + unitSize));
		JLabel s1 = Utilities.standardLabel("Red :");
		JLabel s2 = Utilities.standardLabel("Blue :");
		scorePanel.add(s1);
		scorePanel.add(p1Score);
		scorePanel.add(s2);
		scorePanel.add(p2Score);
		add(scorePanel, JLayeredPane.POPUP_LAYER);
		lockControls(true);
		
		
	}
	
	private Color p1seg = new Color(255,0,0);
	private Color p1area = new Color(255,127,80);
	private Color p2seg = new Color(0,0,255);
	private Color p2area = new Color(80,127,255);
	private static Rectangle2D.Double rect = new Rectangle2D.Double();
	 
	
	  public GameState.Player randomPlayer() {
		  if(Math.random() > .5) return GameState.Player.P1;
		  return GameState.Player.P2;
	  }
	public void startNewGame(int difficulty) {

		p1Score.setText("0");
		p2Score.setText("0");
		gameState.reset();
		AI.maxDepth = difficulty;
		AI.reset();
		
		if(boardButtonListener.lastClicked != null) {
			boardButtonListener.lastClicked.setSelected(false);
			boardButtonListener.lastClicked = null;
		}
	
		lockControls(false);
		drawingPanel.repaint();
		
		

		
	}
}
