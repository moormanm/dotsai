import java.util.LinkedList;




public class AI {


	AI(GameState gs) {
		this.gs = gs;
	}
	GameState gs;
	
	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();
	
	void takeTurn() {
		Turn t1 = new Turn();
		LinkedList<Turn> list = t1.possibleTurns(GameState.Player.P2);
		float max = -9999999;
		Turn bestTurn = null;
		if(list.size() == 0) {
			System.out.println("No moves!");
			return;
		}
		for(Turn t : list) {
		   
		
		   float tmp =  minimax(t, GameState.Player.P2, 0);
		   //System.out.println("Turn " + t.toString() + "  : " + tmp);
		    
		   if(tmp > max) {
			   bestTurn = t;
			   max = tmp; 
		   }
		}
	    
		for(Segment s : bestTurn.moves) {
			gs.doMove(s, GameState.Player.P2);
		}
	}
	
	GameState.Player otherPlayer(GameState.Player p) {
		if(p == GameState.Player.P1) return GameState.Player.P2;
		return GameState.Player.P1;
	}

	int maxDepth = 0;
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
	
	class Turn {
	
		public String toString() {
			String ret = new String();
			LinkedList<Turn> parents = new LinkedList<Turn>();
			Turn par = parent;
			int tcount =1;
			while(par != null) {
				tcount++;
				parents.addFirst(par);
				par = par.parent;
			}
			
			parents.addLast(this);
			for(Turn t : parents) {
				for(Segment s: t.moves) {
					ret += s.toString() + "; ";
				}
			}
			
			ret += tcount;
			
			return ret;
		}
		GameState.Player p;
		final GameState evalPad = new GameState();
		float eval(GameState.Player p) {
			int units = gs.getClaimedArea(p);
			gs.copyTo(evalPad);
			applyTurnsToGameState(evalPad, this, p);
			
			units = evalPad.getClaimedArea(p) - units;
			
			Segment s = moves.getLast();
			
			//TODO: Evaluate the last move.
			
			return units;
			
		}
		void applyTurnsToGameState(GameState state, Turn turns, GameState.Player p) {
			if(state == null || turns == null) return;
			
			LinkedList<Turn> turnList = new LinkedList<Turn>();
			Turn turn = turns.parent;
		    while(turn != null) {
		    	turnList.addFirst(turn);
		    	turn = turn.parent;
		    }
		    
		    turnList.addLast(this);
		    
		    for(Turn t: turnList) {
		    	 for(Segment s: t.moves) {
		    		 state.doMove(s, p);
		    	 }
		    }
		    System.out.println(state.toString());
		}
		
		
		Turn parent;
		LinkedList<Segment> moves = new LinkedList<Segment>();
		
		public Turn() {
			
		}
		
		void copyMovesTo(Turn subTurn) {
			subTurn.moves.clear();
			for(Segment s: moves) {
				subTurn.moves.add(s);
			}
		}

		LinkedList<Turn> possibleTurns(GameState.Player p) {
			
			LinkedList<Turn> ret = new LinkedList<Turn>();
			
			//Init the scratch pad
			gs.copyTo(scratchPad);
			
			
		    //Apply parent turns to get to the current state
			applyTurnsToGameState(scratchPad, parent, p);
			
			

			//Get open segments for this state
			LinkedList<Segment> openSegs = scratchPad.openSegments(); 
			
			LinkedList<Turn> Q = new LinkedList<Turn>();
			
			for(Segment s: openSegs) {
				Turn subTurn = new Turn();
				subTurn.parent = parent;
				subTurn.moves.add(s);
				
				if(segmentWouldClaimUnit(scratchPad, s)) {
					Q.add(subTurn);
				}
				else {
					ret.add(subTurn);
				}
			}
			
			while(Q.size() > 0) {
				Turn turn = Q.poll();
				
				//init the tertiary scratch pad
				scratchPad.copyTo(scratchPad2);
				
				//Apply the parent turns to the game state
				applyTurnsToGameState(scratchPad2, turn, p);
				
				System.out.println(scratchPad2.toString());
				//Get open segs for this state
				openSegs = scratchPad2.openSegments();
				
				if(openSegs.size() == 0) {
					ret.add(turn);
				}
				for(Segment s: openSegs) {
					//Create a new turn, copy move list
					Turn subTurn = new Turn();
					subTurn.parent = parent;
					turn.copyMovesTo(subTurn);
					subTurn.moves.addLast(s);
					
					if(segmentWouldClaimUnit(scratchPad2, s)) {
						Q.add(subTurn);
					}
					else {
						ret.add(subTurn);
					}
				}
			}
				
				
			//For each segment:
			   //If this segment would result in a score move:
			     //Create a new turn with this segment as the first move and Push it on the Q
		       //Else:
			     //Create a new turn with this segment as the first move and add to the ret list
			
			
			//While Q > 0:
			   //Pop Q -> turn
			   //Get the open segments for this state
			   //For each segment:
			     //Create subturn from turn
			     //Add this segment to subturn.moves
			     //If this segment would result in a score move:
			       //push subturn onto the Q
			     //Else:
			       //add subturn to ret list 
			  
	        
		   
			
			//
			
			
			
			return ret;
			
		}
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
		   
		   return false;
		   
	}
	

}
