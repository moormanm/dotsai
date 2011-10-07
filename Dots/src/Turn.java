import java.util.LinkedList;




class Turn {

	/**
	 * 
	 */
	private final AI ai;
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
		int units = this.ai.gs.getClaimedArea(p);
		this.ai.gs.copyTo(evalPad);
		applyTurnsToGameState(evalPad, this);
		
		units = evalPad.getClaimedArea(p) - units;
		
		Segment s = moves.getLast();
		
		//TODO: Evaluate the last move.
		
		return units;
		
	}
	
	//Applys this turn and parent turns to game state
	 static void applyTurnsToGameState(GameState state, Turn turns) {
		if(state == null || turns == null) return;
		
		//Reorder the turns
		LinkedList<Turn> turnList = new LinkedList<Turn>();
		Turn turn = turns.parent;
	    while(turn != null) {
	    	turnList.addFirst(turn);
	    	turn = turn.parent;
	    }
	    //Add the last turn
	    turnList.addLast(turns);
	    
	    for(Turn t: turnList) {
	    	 for(Segment s: t.moves) {
	    		 state.doMove(s, t.p);
	    	 }
	    }
	    System.out.println(state.toString());
	}
	
	
	Turn parent;
	LinkedList<Segment> moves = new LinkedList<Segment>();
	
	public Turn(AI ai, GameState.Player p, Turn parent) {
		this.ai = ai;
		this.p = p;
		this.parent = parent;
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
		this.ai.gs.copyTo(this.ai.scratchPad);
		
		
	    //Apply parent turns to get to the current state
		applyTurnsToGameState(this.ai.scratchPad, this);
		
		

		//Get open segments for this state
		LinkedList<Segment> openSegs = this.ai.scratchPad.openSegments(); 
		
		LinkedList<Turn> Q = new LinkedList<Turn>();
		
		for(Segment s: openSegs) {
			//Create a new turn
			Turn subTurn = new Turn(this.ai, p, this );
			subTurn.moves.add(s);
			
			if(this.ai.segmentWouldClaimUnit(this.ai.scratchPad, s)) {
				Q.add(subTurn);
			}
			else {
				ret.add(subTurn);
			}
		}
		
		while(Q.size() > 0) {
			Turn turn = Q.poll();
			
			//init the tertiary scratch pad
			this.ai.scratchPad.copyTo(this.ai.scratchPad2);
			
			//Apply the parent turns to the game state
			applyTurnsToGameState(this.ai.scratchPad2, turn);
			
			System.out.println(this.ai.scratchPad2.toString());
			
			//Get open segs for this state
			openSegs = this.ai.scratchPad2.openSegments();
			
			if(openSegs.size() == 0) {
				ret.add(turn);
			}
			for(Segment s: openSegs) {
				//Create a new turn, copy move list
				Turn subTurn = new Turn(this.ai, p, turn.parent);
				turn.copyMovesTo(subTurn);
				subTurn.moves.addLast(s);
				
				if(this.ai.segmentWouldClaimUnit(this.ai.scratchPad2, s)) {
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