
import java.awt.Point;
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
    
    
    
	public boolean hitMapX[][] = new boolean[dimY][dimX-1];
    public boolean hitMapY[][] = new boolean[dimY-1][dimX];
	
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
	
	public void doMove2(Segment s, Player p) {
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
	}
	public void undoMove2(Segment s) {
		   Player seg[][];
		   if(s.isY) {
			   seg = segY;
		   }
		   else {
			   seg = segX;
		   }
		   assert(seg[s.y][s.x] != null);
		   
		   //apply the segment
		   seg[s.y][s.x] = null;
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

	    public static int rep[][] = new int[dimX-1][dimY-1];
        public static void fillFragment(int x, int y, int val, int rep[][], GameState gs ) {
          //return if this point is already taken on the real state or if it's already 
          //colored on the representation
          if(rep[y][x] != 0) { 
            return;
          } 
          

          LinkedList<Point> q = new LinkedList<Point>();
          
          //init Q
          q.add(new Point(x,y));
          
          while(q.size() > 0) {
        	  
        	  //remove an element
        	  Point p = q.poll();
        	  
        	  
        	  
              //color this point on the rep
              rep[p.y][p.x] = val;
              
              //Check the right and left side
              if((p.y < gs.dimY - 1) &&
            	  p.x < gs.dimX - 2 &&
                  gs.segY[p.y][p.x+1] == null &&
                  rep[p.y][p.x+1] == 0) {
            	  q.add(new Point(p.x+1, p.y));
              }
              if((p.y < gs.dimY - 1) &&
            	  p.x > 0 && 
            	  p.x < (gs.dimX-1) &&
            	  gs.segY[p.y][p.x] == null && 
            	  rep[p.y][p.x-1] == 0) {
            	  q.add(new Point(p.x-1, p.y));
              }
              
              //Check the top and bottom
              if((p.x < gs.dimX - 1) &&
                 (p.y > 0) &&
            	  gs.segX[p.y][p.x] == null &&
            	  rep[p.y-1][p.x] == 0) {
                  q.add(new Point(p.x, p.y-1));        	  
              }
              if((p.x < gs.dimX ) &&
                 (p.y < gs.dimY - 2) &&
                  gs.segX[p.y+1][p.x] == null &&
                  rep[p.y+1][p.x] == 0) {
                       q.add(new Point(p.x, p.y+1));        	  
              }
          
          }
          return;
        
        }
        
        static int getNumFragments(GameState gs) {
    		int i = 1;
    		int count = 0;

    		//clear the rep
    		for(int y = 0; y < GameState.dimY-1; y++) {
    			for(int x = 0; x < GameState.dimX -1; x++) {
    				GameState.rep[y][x] = 0;
    			}
    		}
    		
    		
    		for(int y = 0; y < GameState.dimY-1; y++) {
    			for(int x = 0; x < GameState.dimX -1; x++) {
    				if(GameState.rep[y][x] == 0) {
    				   count++;
    				   GameState.fillFragment(x,y, i++, GameState.rep, gs);
    				}
    			}
    		}
    	/*	
    		System.out.println("count is " + count);
    		for(int[] row : GameState.rep) {
    			for(int val : row) {
    				System.out.print(val + " ");
    			}
    			System.out.println( );
    		}
    		*/
    		return count;
        }
        
    	public int numSegmentsAroundUnit(int x, int y, Segment connectiveSegmentOut) {
    		int cnt =0;
    		if(segX[y][x] != null) {
    			cnt++;
    		}
    		else {
    			connectiveSegmentOut.x = x; 
    			connectiveSegmentOut.y = y;
    			connectiveSegmentOut.isY = false;
    		}
    		if(segX[y+1][x] != null) {
    			cnt++;
    		}
    		else {
    			connectiveSegmentOut.x = x; 
    			connectiveSegmentOut.y = y+1;
    			connectiveSegmentOut.isY = false;
    		}
    		if(segY[y][x] != null) {
    			cnt++;
    		}
    		else {
    			connectiveSegmentOut.x = x; 
    			connectiveSegmentOut.y = y;
    			connectiveSegmentOut.isY = true;
    		}
    		if(segY[y][x+1] != null) {
    			cnt++;
    		}
    		else {
    			connectiveSegmentOut.x = x+1; 
    			connectiveSegmentOut.y = y;
    			connectiveSegmentOut.isY = true;
    		}
    		return cnt;
    		
    	}
    	
    	
    	
    	
    		public static LinkedList<Segment> expandSegment(GameState gst, Segment root) {
    		LinkedList<Segment> ret = new LinkedList<Segment>();
    		ret.add(root);
    		GameState.Player seg[][];

    		if(root.isY) {
    		  seg = gst.segY;
    	    }
    		else {
    		  seg = gst.segX;
    		}
    		   
    		
    		//apply the first segment
    		seg[root.y][root.x] = GameState.Player.P1;
    		
    		Segment connSeg = new Segment(0,0,false);
    		LinkedList<Segment> q = new LinkedList<Segment>();
    		q.add(root);
    		Segment ns = null;
    		while(q.size() > 0) {
    			//pull an item off the front of q
    			Segment s = q.poll();
    			
    	       //If it's a Y segment, consider the left side and right sides
    			if(s.isY){
    				 if(s.x != 0) {
    					   if(gst.numSegmentsAroundUnit(s.x-1, s.y, connSeg) == 3) {
    						   ns = new Segment(connSeg);
    						   q.add(ns);
    						   ret.add(ns);
    						   gst.doMove2(connSeg, GameState.Player.P1);
    					   }
    				 }
    				 if(s.x != GameState.dimX-1) {
    					   if(gst.numSegmentsAroundUnit(s.x, s.y, connSeg) == 3) {
    						   ns = new Segment(connSeg);
    						   q.add(ns);
    						   ret.add(ns);
    						   gst.doMove2(connSeg, GameState.Player.P1);
    					   }
    				 }
    			}
    			//If it's an X segment, check the top and bottom
    			else {
    			   if(s.y != 0) {
    				  if(gst.numSegmentsAroundUnit(s.x, s.y-1, connSeg) == 3 ) {
    					   ns = new Segment(connSeg);
    					   q.add(ns);
    					   ret.add(ns);
    					   gst.doMove2(connSeg, GameState.Player.P1);
    			      }
    			   }
    				   
    			   if(s.y != GameState.dimY-1) {
    				   if(gst.numSegmentsAroundUnit(s.x, s.y, connSeg) == 3) {
    					   ns = new Segment(connSeg);
    					   q.add(ns);
    					   ret.add(ns);
    					   gst.doMove2(connSeg, GameState.Player.P1);  
    				   }
    			   }
    					   
    			}
    		}
    		
    		//undo moves
    		for(Segment m : ret) {
    			gst.undoMove2(m);
    		}
    		
    		return ret;
    		
    	}
	
	
}
