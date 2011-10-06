
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JPanel;


public class GameState {

	GameState() {
	 
	}
	public int dimX = 10;
	public int dimY = 10;
	
	
	//Segments
	public Player[][] segX = new Player[dimY][dimX-1];
    public Player[][] segY = new Player[dimY-1][dimX];
	
	//Claimed units
	public Player[][] claimedUnits = new Player[dimY][dimX];
	
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
	
	public LinkedList<Segment> openMoves() {
		return null;
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
