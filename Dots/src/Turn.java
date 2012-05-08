import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
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
			if ( myUnitsAfter > theirUnitsAfter ) {
				winBonus = Integer.MAX_VALUE / 4;
			}
			else if(  myUnitsAfter == theirUnitsAfter ) {
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

	
		ai.scratchPad.copyTo(tmpState);
		
		
		// Get mandatory states
		LinkedList<Segment> mandSegs = GameState.getMandatorySegments(ai.scratchPad, true);
		
		
		// if there were mandatory segments..
		for(int i =1; i <= mandSegs.size(); i++) {
			// Init the scratch pad
			tmpState.copyTo(this.ai.scratchPad);
			
	
			// Create a new turn
			Turn subTurn = new Turn(this.ai, p, isRoot ? null : this);
			subTurn.moves.addAll(0, mandSegs.subList(0, i));
			
			//Apply each move
			for(Segment segz : subTurn.moves) {
				ai.scratchPad.doMove2(segz, p);
			}
			
			// Add a new turn for each possible move
			LinkedList<Segment> segs = ai.scratchPad.openSegments();
			
			// No more moves case
			if (segs.size() == 0) {
				ret.add(subTurn);
			}
			
			// Explore the basic move possibilities 
			for(Segment lastMove : segs) {

				if(GameState.segmentWouldClaimUnit(ai.scratchPad, lastMove)) {
				   continue;
				}
				
		    	// Create a new turn
			    Turn nt = new Turn(ai, p, isRoot ? null : this);
					
				subTurn.copyMovesTo(nt);
					
				nt.moves.add(lastMove);
				ret.add(nt);
			}

		}


		//// Pick up basic moves
		
		// ReInit the scratch pad
		tmpState.copyTo(this.ai.scratchPad);

		LinkedList<Segment> openSegs = this.ai.scratchPad.openSegments();
		for (Segment s : openSegs) {
			if( GameState.segmentWouldClaimUnit(ai.scratchPad, s)) {
				//System.out.println("Seg would claim!!");
				continue;
			}
			Turn basicTurn = new Turn(ai, p, isRoot ? null : this);
			basicTurn.moves.add(s);
			ret.add(basicTurn);
		}
		
		
		//Remove "duplicate" moves. Not really duplicates, but they produce the same result for the 
		//other player.
		//System.out.println("Turns before reduction: " + ret.size());
		//reduceTurns(ret, ai.gs);
		//System.out.println("Turns after reduction: " + ret.size());
		return ret;

	}
	
	static final GameState reducePad = new GameState();
	static void reduceTurns(LinkedList<Turn> turnList, GameState gst) {
		//init a temporary state
		gst.copyTo(reducePad);
		
		
		HashMap<String, String> segSet = new HashMap<String,String>();
		Iterator<Turn> i = turnList.iterator();
		HashSet<BitSet> hits = new HashSet<BitSet>();
		while(i.hasNext()) {
			Turn t = i.next();
		
			//Try this move
			reducePad.copyTo(tmpState);
			if(t.moves.size() > 1) {
			  LinkedList<Segment> tmpMoves = new LinkedList<Segment>(t.moves);
			  LinkedList<Segment> sv = t.moves;
			  tmpMoves.removeLast();
			  t.moves = tmpMoves;
			  Turn.applyTurnsToGameState(tmpState, t);
			  t.moves = sv;
			}
			else {
				Turn.applyTurnsToGameState(tmpState, t);
			}
			

			//Get the mandatory segments for the other player
			LinkedList<Segment> msegs = GameState.getMandatorySegments(tmpState, false);
			if(msegs.size() == 0) {
				continue;
			}
			
			
			//Add this to the move set
			BitSet bs = tmpState.asBitSet();
			if(!hits.contains(bs)) {
			 hits.add(bs);
			}
			else {
				i.remove();
				continue;
			}
		}
			
	}

}