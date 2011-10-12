import java.util.LinkedList;

class Turn {

	/**
	 * 
	 */

	private final AI ai;

	boolean isRoot = false;
	
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
			for (Segment s : t.moves) {
				ret += s.toString() + "; ";
			}
		}

		ret += tcount;

		return ret;
	}

	GameState.Player p;
	static final GameState evalPad = new GameState();

	
	int eval(GameState.Player p) {
		
		//Get the claimed areas of the current game state
		int myUnits = this.ai.gs.getClaimedArea(p);
		int theirUnits = this.ai.gs.getClaimedArea(GameState.otherPlayer(p));
		
		
		//Initialize a scratch pad state
		this.ai.gs.copyTo(evalPad);
		
		//Apply this turn set to the scratch pad
		applyTurnsToGameState(evalPad, this);

		//Get the number of claimed areas after the turns have been applied
		int myUnitsAfter = evalPad.getClaimedArea(p);
		int theirUnitsAfter = evalPad.getClaimedArea(GameState.otherPlayer(p));

		///Get the deltas
		myUnits = myUnitsAfter - myUnits;
		theirUnits = theirUnitsAfter - theirUnits;
		int diff = myUnitsAfter - theirUnitsAfter;

		// If this is the end game state, assign a HUGE bonus or penalty
		int winBonus = 0;
		if (evalPad.hasOpenSegments() == false) {
			if (diff > 0) {
				winBonus = Integer.MAX_VALUE / 2;
		//		System.out.println("Win bonus for player " + p.toString());
			} else if (diff == 0) {
				//System.out.println("Draw bonus for player " + p.toString());
				winBonus = Integer.MAX_VALUE / 4;
			} else {
				winBonus = -1 * Integer.MAX_VALUE / 2;
				//System.out.println("Lose bonus for player " + p.toString());
			}

		}
		
		
		//assign an overall objective value for these turns
		int retVal = (myUnits - theirUnits) + winBonus;
		

		
		return retVal;

	}

	// Applys this turn and parent turns to game state
	static void applyTurnsToGameState(GameState state, Turn turns) {
		if (state == null || turns == null)
			return;

		// Reorder the turns
		LinkedList<Turn> turnList = new LinkedList<Turn>();
		Turn turn = turns.parent;
		while (turn != null) {
			turnList.addFirst(turn);
			turn = turn.parent;
		}
		// Add the last turn
		turnList.addLast(turns);

		for (Turn t : turnList) {
			for (Segment s : t.moves) {
				state.doMove(s, t.p);
			}
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

	final static GameState tmpState = new GameState();

	LinkedList<Turn> possibleTurnsForPlayer(GameState.Player p) {

		LinkedList<Turn> ret = new LinkedList<Turn>();

		// Init the scratch pad
		this.ai.gs.copyTo(this.ai.scratchPad);

		// Apply parent turns to get to the current state
		applyTurnsToGameState(this.ai.scratchPad, this);

		// Get open segments for this state
		LinkedList<Segment> openSegs = this.ai.scratchPad.openSegments();

		// Loop through, add mandatory states
		for (Segment s : openSegs) {

			//If this segment would claim a unit, it's a requisite move
			if (GameState.segmentWouldClaimUnit(this.ai.scratchPad, s)) {
				// Create a new turn
				Turn subTurn = new Turn(this.ai, p, isRoot ? null : this);
				subTurn.moves.add(s);

				// init a temporary state
				ai.scratchPad.copyTo(tmpState);

				// do the first move
				tmpState.doMove(s, p);

				//Loop through to pick up consecutive moves for this turn
				boolean keepGoing = true;
				while (keepGoing) {
					keepGoing = false;
					// Keep doing chain moves
					LinkedList<Segment> segs = tmpState.openSegments();
					for (Segment s2 : segs) {
						if (GameState.segmentWouldClaimUnit(tmpState, s2)) {
							keepGoing = true;
							tmpState.doMove(s2, p);
							subTurn.moves.add(s2);
						}
					}
				}

				// Add a new turn for each possible "last move" of the consecutive move sequence
				LinkedList<Segment> segs = tmpState.openSegments();
				// No more moves case
				if (segs.size() == 0) {
					ret.add(subTurn);
				}

				for (Segment lastMove : segs) {
					// Create a new move
					Turn nt = new Turn(ai, p, isRoot ? null : this);

					// Take a copy of the moves from subTurn
					subTurn.copyMovesTo(nt);

					// Add the last move
					nt.moves.add(lastMove);

					ret.add(nt);

				}

			}
		}

		// Pick up basic moves if no jump moves were found
		if (ret.size() == 0) {
			openSegs = this.ai.scratchPad.openSegments();
			for (Segment s : openSegs) {
				Turn basicTurn = new Turn(ai, p, isRoot ? null : this);
				basicTurn.moves.add(s);
				ret.add(basicTurn);
			}
		}
		return ret;

	}

}