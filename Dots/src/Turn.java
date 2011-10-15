import java.util.HashSet;
import java.util.Iterator;
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
	static final GameState evalPad = new GameState();

	
	int eval(GameState.Player p) {
		
		//Get the claimed areas of the current game state
		int myUnits = this.ai.gs.getClaimedArea(p);
		int theirUnits = this.ai.gs.getClaimedArea(GameState.otherPlayer(p));

		//Initialize a scratch pad state
		this.ai.gs.copyTo(evalPad);
			
		//Apply the turn to the scratch pad
		applyTurnsToGameState(evalPad, this);
		

		//System.out.println(evalPad);
		//Get the number of claimed areas after the turns have been applied
		int myUnitsAfter = evalPad.getClaimedArea(p);
		int theirUnitsAfter = evalPad.getClaimedArea(GameState.otherPlayer(p));

		///Get the deltas
		myUnits = myUnitsAfter - myUnits;
		theirUnits = theirUnitsAfter - theirUnits;
		int diff = myUnitsAfter - theirUnitsAfter;

		// If this is the end game state, assign a HUGE bonus or penalty
		int winBonus = 0;
		
		//if there are no segments left, or the difference is greater than the remaining units 
		if (evalPad.hasOpenSegments() == false) { 
			if (diff > 0) {
				winBonus = Integer.MAX_VALUE / 2;
			} else if (diff == 0) {
				winBonus = Integer.MAX_VALUE / 4;
			} else {
				winBonus = -1 * Integer.MAX_VALUE / 2;
			}

		}
		
		
		//assign an overall objective value for these turns
		int retVal = (myUnits - theirUnits)*4 + winBonus;
		
		
		//The immediate turn should have a bearing on the overall value. 
		return retVal ;

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

		// Get mandatory states
		LinkedList<Segment> mandSegs = GameState.getMandatorySegments(ai.scratchPad, false);
		
		// if there were mandatory segments..
		if(mandSegs.size() > 0) {
			// Create a new turn
			Turn subTurn = new Turn(this.ai, p, isRoot ? null : this);
			subTurn.moves = mandSegs;
			
			// Add a new turn for each possible "last move" of the consecutive move sequence
			LinkedList<Segment> segs = ai.scratchPad.openSegments();
			
			// No more moves case
			if (segs.size() == 0) {
				ret.add(subTurn);
			}
			
			//Double cross strategy case
			if(mandSegs.size() >= 2) {
				// Create a new turn
				Turn nt = new Turn(ai, p, isRoot ? null : this);
				
				// Take a copy of the moves from subTurn
				subTurn.copyMovesTo(nt);
				
				// Remove the last move
				nt.moves.removeLast();
				
				ret.add(nt);

			}

			for (Segment lastMove : segs) {
				// Create a new turn
				Turn nt = new Turn(ai, p, isRoot ? null : this);

				// Take a copy of the moves from subTurn
				subTurn.copyMovesTo(nt);

				// Add the last move
				nt.moves.add(lastMove);

				ret.add(nt);
			}
			
			
		}


		// Pick up basic moves if no chain moves were found
		if (ret.size() == 0) {
			LinkedList<Segment> openSegs = this.ai.scratchPad.openSegments();
			for (Segment s : openSegs) {
				Turn basicTurn = new Turn(ai, p, isRoot ? null : this);
				basicTurn.moves.add(s);
				ret.add(basicTurn);
			}
		}
		
		//Remove "duplicate" moves. Not really duplicates, but they produce the same result for the 
		//other player.
		reduceTurns(ret, ai.gs);
		
		//System.out.println("Branching factor: " + ret.size());
		return ret;

	}
	
	static final GameState reducePad = new GameState();
	static void reduceTurns(LinkedList<Turn> turnList, GameState gst) {
		//Can't reduce a single turn
		if(turnList.size() < 2) {
			return;
		}
		
		//init a temporary state
		gst.copyTo(reducePad);
		Turn.applyTurnsToGameState(reducePad, turnList.get(0).parent);
		
		//Get the prefix moves
		LinkedList<Segment> prefix = new LinkedList<Segment>();
		if(turnList.get(0).moves.size() > 1) {
			for(Segment s: turnList.get(0).moves ) {
				prefix.add(s);
			}
			prefix.removeLast();
			
			for(Segment s: prefix) {
				reducePad.doMove2(s, GameState.Player.P1);
			}
		}
		
		HashSet<String> segSet = new HashSet<String>();
		Iterator<Turn> i = turnList.iterator();
		top:
		while(i.hasNext()) {
			Turn t = i.next();
			
			//if this is a double cross move, add it regardless
			if(t.moves.size() == prefix.size()) {
				continue;
			}
			
			if(segSet.contains(t.moves.getLast().encode())) {
				//remove this item. it's a duplicate
				i.remove();
				continue top;
			}
			reducePad.doMove2(t.moves.getLast(), GameState.Player.P1);
			LinkedList<Segment> hits = GameState.getMandatorySegments(reducePad, true);
			reducePad.undoMove2(t.moves.getLast());
			for(Segment s: hits) {
					segSet.add(s.encode());
			}
		}
			
	}
		
		
		
		
		



}