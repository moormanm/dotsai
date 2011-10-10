
import java.util.LinkedList;




public class GameState {

	GameState() {
	 
	}
	public Segment lastMove;
	
	public static int dimX = 8;
	public static int dimY = 8;
	
	public String toString() {
		String ret = new String();
 		//check x moves
		for(int y =0; y < dimY; y++) {
			for(int x=0; x < dimX; x++) {
				if(y < (dimY - 1)) {
					  if(segY[y][x] != null) {
						 ret += "| ";
					  }
					  else { 
						ret += "  ";
					  }
				}
				
				if(x < (dimX - 1)){
				  if(segX[y][x] != null) {
					 ret += "_ ";
				  }
				  else { 
					ret += "  ";
				  }
				}

				
				
			}
			ret+= System.getProperty("line.separator");
			
		}
		ret+= System.getProperty("line.separator");
		
		for(int i =0; i < dimY-1; i++) {
			for(int j=0; j <dimX-1; j++) {
				if(claimedUnits[i][j] == Player.P1) {
					ret+= "X ";
				}
				else if(claimedUnits[i][j] == Player.P2) {
					ret+="O ";
				}
				else {
					ret+="  ";
				}
			
			}
			ret+= System.getProperty("line.separator");
		}
		return ret;
	}
	
	
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
		
		for(int y =0; y < dimY; y++) {
			for(int x=0; x < dimX-1; x++) {
				segX[y][x] = null;
			}
		}
		for(int y =0; y < dimY-1; y++) {
			for(int x=0; x < dimX; x++) {
			   segY[y][x] = null;
			}
		}
		for(int i =0; i < dimY-1; i++) {
			for(int j=0; j <dimX-1; j++) {
				claimedUnits[i][j] = null;
			}
		}
		
		
		lastMove = null;
		
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
	
	
	public int numSegmentsForPoint(int x, int y) {
		
		int ret =0;
		//Get the x segments that touch this point
		if(x>0) {
			ret += segX[y][x-1]  == null ? 0 : 1;
		}
		if(x<dimX-1) {
			ret += segX[y][x]  == null ? 0 : 1;
		}
		
		
		//Get the y segments that touch this point
		if(y > 0) {
			ret += segY[y-1][x]  == null ? 0 : 1;
		}
		if(y < (dimY - 2)) {
			ret += segY[y][x]  == null ? 0 : 1;
		}
		
		return ret;
	}
	
	public void doMove(Segment s, Player p) {
	   
	
	    
		
	   Player seg[][];
	   if(s.isY) {
		   seg = segY;
	   }
	   else {
		   seg = segX;
	   }
	   assert(seg[s.y][s.x] == null);
	   
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
	   lastMove = s;
	   return;
	}
	
	public static GameState.Player otherPlayer(GameState.Player p) {
		if(p == GameState.Player.P1) return GameState.Player.P2;
		return GameState.Player.P1;
	}
	
	public LinkedList<Segment> openSegments() {
		LinkedList<Segment> ret = new LinkedList<Segment>();
		
 		//check x moves
		for(int y =0; y < dimY; y++) {
			for(int x=0; x < dimX-1; x++) {
				if(segX[y][x] == null)
					ret.add(new Segment(x,y,false));
			}
		}
		
 		//check y moves
		for(int y =0; y < dimY-1; y++) {
			for(int x=0; x < dimX; x++) {
				if(segY[y][x] == null)
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
	
	public enum Player { P1, P2};
	
	
	
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
	public boolean hasOpenSegments() {
 		//check x moves
		for(int y =0; y < dimY; y++) {
			for(int x=0; x < dimX-1; x++) {
				if(segX[y][x] == null)
					return true;
			}
		}
		
 		//check y moves
		for(int y =0; y < dimY-1; y++) {
			for(int x=0; x < dimX; x++) {
				if(segY[y][x] == null)
					return true;
			}
		}
		
		return false;
	}
	
	//A method that checks if a segment would claim a unit. Does not apply the move to the game state.
	public static boolean segmentWouldClaimUnit(GameState gst, Segment s) {
		
		   GameState.Player seg[][];
		   if(s.isY) {
			   seg = gst.segY;
		   }
		   else {
			   seg = gst.segX;
		   }
		   
		   //apply the segment
		   seg[s.y][s.x] = GameState.Player.P1;

		   boolean ret = false;
		   
		   //If it's a Y segment, check the left and right areas for enclosure
		   if(s.isY){
			   if(s.x != 0) {
				   if(gst.isUnitEnclosed(s.x-1, s.y)) {
					   ret = true;
				   }
			   }
			   if(s.x != GameState.dimX-1) {
				   if(gst.isUnitEnclosed(s.x, s.y)) {
					   ret = true;
				   }
			   }
		   }
		   //If it's an X segment, check the top and bottom
		   else {
			   if(s.y != 0) {
				   if(gst.isUnitEnclosed(s.x, s.y-1) ) {
					   ret = true;
				   }
			   }
			   
			   if(s.y != GameState.dimY-1) {
				   if(gst.isUnitEnclosed(s.x, s.y)) {
					   ret = true;
				   }
			   }
				   
		   }
		   
		   //Undo the move
		   seg[s.y][s.x] = null;
		   
		   return ret;
	}
	
	
}
