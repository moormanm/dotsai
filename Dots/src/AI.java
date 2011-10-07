import java.util.LinkedList;




public class AI {


	AI(GameState gs) {
		this.gs = gs;
	}
	GameState gs;
	
	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();
	
	void takeTurn() {
		Turn t1 = new Turn(this, GameState.Player.P2, null);
		LinkedList<Turn> list = t1.possibleTurns(GameState.Player.P2);
		float max = -9999999;
		Turn bestTurn = null;
		if(list.size() == 0) {
			System.out.println("No moves!");
			return;
		}
		for(Turn t : list) {
		   
		
		   
		   float tmp =  -1 * minimax(t, GameState.Player.P1, 0);
		   System.out.println("Turn " + t.toString() + "  : " + tmp);
		    
		   if(tmp > max) {
			   bestTurn = t;
			   max = tmp; 
		   }
		}
	    
		for(Segment s : bestTurn.moves) {
			System.out.println("Doing move: " + s);
			gs.doMove(s, GameState.Player.P2);
		}
	}
	
	GameState.Player otherPlayer(GameState.Player p) {
		if(p == GameState.Player.P1) return GameState.Player.P2;
		return GameState.Player.P1;
	}

	int maxDepth = 4;
	float minimax(Turn t, GameState.Player p, int depth) {
		if(depth >= maxDepth) {
			return t.eval(p);
		}
		

		LinkedList<Turn> children = t.possibleTurns(p);
		if(children.size() == 0) { 
			return t.eval(p);
		}
		float maxVal = -999999;
		float tmp = 0;
		Turn bestTurn = null;
		
		int i = 0;
		for(Turn child : children) {
			tmp = -1 * minimax(child, otherPlayer(p), ++depth);
			if(tmp > maxVal) {
				bestTurn = child;
				maxVal = tmp;
			}
		}
		return maxVal;
	}
	
	public boolean segmentWouldClaimUnit(GameState gst, Segment s) {
		
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
			   if(s.x != gst.dimX-1) {
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
			   
			   if(s.y != gst.dimY-1) {
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
