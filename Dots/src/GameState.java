
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JPanel;


public class GameState {

	GameState() {
	 
	}
	public static int dimX = 4;
	public static int dimY = 4;
	
	
	
	//Segments
	public Player[][] segX = new Player[dimY][dimX-1];
    public Player[][] segY = new Player[dimY-1][dimX];
	
	//Claimed units
	public Player[][] claimedUnits = new Player[dimY][dimX];

	public void copyTo(GameState gs) {
		copyInline(gs.segX, segX);
		copyInline(gs.segY, segY);
		copyInline(gs.claimedUnits, claimedUnits);
		
	}
	private static void copyInline(Player[][] b, Player[][] a) {
		for(int y=0; y<a.length; y++) {
			for(int x=0; x<a[y].length; x++) {
              b[y][x] = a[y][x];				
			}
		}
	}
	public void reset() {
		
		for(Player[] row: segX) {
			for(Player p: row) {
				p = null;
			}
		}
		for(Player[] row: segY) {
			for(Player p: row) {
				p = null;
			}
		}
		
		for(Player[] row: claimedUnits) {
			for(Player p: row) {
				p = null;
			}
		}
		
	}
	public boolean isMoveValid(Segment s) {
	  //Move is valid if it	is not taken already
	  if(s.isY) {
		  return segY[s.y][s.x] == null;
	  }
	  else {
		  return segX[s.y][s.x] == null;
	  }
	}
	
	public boolean isUnitEnclosed(int x, int y) {
		return segX[y][x] != null &&
		       segX[y+1][x] != null &&
		       segY[y][x] != null &&
		       segY[y][x+1] !=null;
	}
	
	
	
	public void doMove(Segment s, Player p) {
	   Player seg[][];
	   if(s.isY) {
		   seg = segY;
	   }
	   else {
		   seg = segX;
	   }
	   
	   //apply the segment
	   seg[s.y][s.x] = p;

	   //If it's a Y segment, check the left and right areas for enclosure
	   if(s.isY){
		   if(s.x != 0) {
			   if(isUnitEnclosed(s.x-1, s.y)) {
				   claimedUnits[s.y][s.x-1] = p;
			   }
		   }
		   if(s.x != dimX-1) {
			   if(isUnitEnclosed(s.x, s.y)) {
				   claimedUnits[s.y][s.x] = p;
			   }
		   }
	   }
	   //If it's an X segment, check the top and bottom
	   else {
		   if(s.y != 0) {
			   if(isUnitEnclosed(s.x, s.y-1) ) {
				   claimedUnits[s.y-1][s.x] = p;
			   }
		   }
		   
		   if(s.y != dimY-1) {
			   if(isUnitEnclosed(s.x, s.y)) {
				   claimedUnits[s.y][s.x] = p;
			   }
		   }
			   
	   }
	   return;
	}
	
	
	public LinkedList<Segment> openSegments() {
		LinkedList<Segment> ret = new LinkedList<Segment>();
		
 		//check x moves
		for(int y =0; y < dimY; y++) {
			for(int x=0; x < dimX-1; x++) {
				if(segX[y][x] != null)
					ret.add(new Segment(x,y,false));
			}
		}
		
 		//check y moves
		for(int y =0; y < dimY-1; y++) {
			for(int x=0; x < dimX; x++) {
				if(segX[y][x] != null)
					ret.add(new Segment(x,y,true));
			}
		}
		
		return ret;
	}
	
	public void startNewGame() {
		//Setup board segments
		 segX = new Player[dimY][dimX-1];
		 segY = new Player[dimY-1][dimX];
		 
		 claimedUnits = new Player[dimX-1][dimY-1];
		 
	}
	
	public enum Player { P1, P2 };
	
	
	public int getClaimedArea(Player a) {
		int ret = 0;
		for(int i =0; i < dimY-1; i++) {
			for(int j=0; j <dimX-1; j++) {
				if(claimedUnits[i][j] == a) {
					ret++;	
				}
			}
		}
		return ret;
	}
	
	
	
	
}
