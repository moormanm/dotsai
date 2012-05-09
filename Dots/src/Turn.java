import java.util.LinkedList;


class Turn {

	private final AI ai;
	boolean isRoot = false;
	
	@Override
	public String toString() {
		String ret = new String();
		
				
		LinkedList<Turn> parents = new LinkedList<Turn>();
		Turn par = parent;
		int tcount = 1;
		while (par != null) {
			tcount++;
			parents.addFirst(par);
			par = par.parent;
		}

		parents.addLast(this);
		for (Turn t : parents) {
			ret += t.p + ":: ";
			for (Segment s : t.moves) {
				ret += s.toString() + "; ";
			}
		}

		ret += tcount;

		return ret;
	}

	GameState.Player p;
	
	int eval(GameState.Player p) {
		
		//Get the claimed areas of the current game state
		int myUnits = this.ai.working.getClaimedArea(p);
		int theirUnits = this.ai.working.getClaimedArea(GameState.otherPlayer(p));


		// If this is the end game state, assign a HUGE bonus or penalty
		int winBonus = 0;
		
		//if there are no segments left, this is an end game state
		if (ai.working.hasOpenSegments() == false) {
			if ( myUnits > theirUnits ) {
				winBonus = Integer.MAX_VALUE / 4;
			}
			else if(  myUnits == theirUnits ) {
				winBonus = Integer.MAX_VALUE / 8;
			}
			else {
				winBonus = -1 * Integer.MAX_VALUE / 4;
			}
		}
		

		//assign an overall objective value for these turns
		int retVal = (myUnits - theirUnits) + winBonus;
				
		
		//The immediate turn should have a bearing on the overall value. 
		return retVal;

	}

	static void doMoves(LinkedList<Segment> moves, GameState.Player p, GameState gs) {
		for(Segment m : moves) {
			gs.doMove(m, p);
		}
	}
	static void undoMoves(LinkedList<Segment> moves, GameState.Player p, GameState gs) {
		for(Segment m : moves) {
			gs.undoMove(m, p);
		}
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
		for (Segment s : moves) {
			subTurn.moves.add(s);
		}
	}


	LinkedList<Turn> possibleTurnsForPlayer(GameState.Player p) {

		LinkedList<Turn> ret = new LinkedList<Turn>();

	
		
		// Get mandatory states
		LinkedList<Segment> mandSegs = GameState.getMandatorySegments(ai.working);
		
		
		// if there were mandatory segments..
		for(int i =1; i <= mandSegs.size(); i++) {

	
			// Create a new turn
			Turn subTurn = new Turn(this.ai, p, isRoot ? null : this);
			subTurn.moves.addAll(0, mandSegs.subList(0, i));
			
			//Apply each move
			for(Segment segz : subTurn.moves) {
				ai.working.doMove2(segz, p);
			}
			
			// Add a new turn for each possible move
			LinkedList<Segment> segs = ai.working.openSegments();
			
			// No more moves case
			if (segs.size() == 0) {
				ret.addFirst(subTurn);
			}
			
			// Explore the basic move possibilities 
			for(Segment lastMove : segs) {

				if(GameState.segmentWouldClaimUnit(ai.working, lastMove)) {
				   continue;
				}
				
		    	// Create a new turn
			    Turn nt = new Turn(ai, p, isRoot ? null : this);
					
				subTurn.copyMovesTo(nt);
					
				nt.moves.add(lastMove);
				ret.addFirst(nt);
			}
			//Undo each move
			for(Segment segz : subTurn.moves) {
				ai.working.undoMove2(segz);
			}

		}


		//// Pick up basic moves

		LinkedList<Segment> openSegs = this.ai.working.openSegments();
		for (Segment s : openSegs) {
			if( GameState.segmentWouldClaimUnit(ai.working, s)) {
				//System.out.println("Seg would claim!!");
				continue;
			}
			Turn basicTurn = new Turn(ai, p, isRoot ? null : this);
			basicTurn.moves.add(s);
			ret.add(basicTurn);
		}
		
		

		return ret;

	}
	

}