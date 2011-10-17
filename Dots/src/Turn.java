import java.util.Collections;
import java.util.HashMap;
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
		
		//Give a sacrifice bonus if this is a sacrificial move
		int sacrificeBonus = 0;
		//Fetch the sacrificial move status
		boolean firstSegmentClaimsUnit = false;
		
		//Undo the move, check if we claim a unit, and then redo it
		evalPad.undoMove2(moves.get(0));
		if(GameState.segmentWouldClaimUnit(evalPad, moves.get(0))) {
			firstSegmentClaimsUnit = true;
		}
		evalPad.doMove2(moves.get(0), p);
		
		
		if(firstSegmentClaimsUnit) {
		  Segment connSeg = new Segment(0,0,false);
		  theLoop:
		  for (int y = 0; y < GameState.dimY - 1; y++) {
  			for (int x = 0; x < GameState.dimX - 1; x++) {
				if (evalPad.claimedUnits[y][x] == null
						&& evalPad.numSegmentsAroundUnit(x, y, connSeg) == 3) {
					sacrificeBonus = 3;
					break theLoop;
				}
			}
		  }
		}
		
		//assign an overall objective value for these turns
		int retVal = (myUnits - theirUnits) * 2 + winBonus + sacrificeBonus;
		
				
		
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
		//System.out.println("Turns before reduction: " + ret.size());
		reduceTurns(ret, ai.gs);
		//System.out.println("Turns after reduction: " + ret.size());
		return ret;

	}
	
	static final GameState reducePad = new GameState();
	static void reduceTurns(LinkedList<Turn> turnList, GameState gst) {
		//Can't reduce less than 3 turns. One turn might always exist for double cross play.
		if(turnList.size() < 3) {
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
		
		HashMap<String, String> segSet = new HashMap<String,String>();
		Iterator<Turn> i = turnList.iterator();
		LinkedList<String> hitList = new LinkedList<String>();
		while(i.hasNext()) {
			Turn t = i.next();
			
			//if this is a double cross move, keep it
			if(t.moves.size() == prefix.size()) {
				continue;
			}
			
			
			//if this move would result in no mandatory segments for the other player, keep it
			if(!GameState.segmentWouldConnect3(reducePad,t.moves.getLast())) {
				continue;
			}
			
			//Get the would be player 2 moves, should this move be made
			reducePad.doMove2(t.moves.getLast(), GameState.Player.P1);
			LinkedList<Segment> hits = GameState.getMandatorySegments(reducePad, true);
			reducePad.undoMove2(t.moves.getLast());
			hitList.clear();
		
			//Add each segment to the hitlist
			for(Segment s: hits) {
					hitList.add(s.encode());
			}
			//Add the parent move as well
			hitList.add(t.moves.getLast().encode());
		
			//Sort the hitlist
			Collections.sort(hitList);
			
			//Rebuild the segments
			String resultingMoveSet = "";
			for(String str : hitList) {
				resultingMoveSet += str + ";";
			}
			
			if(segSet.containsKey(resultingMoveSet)) {
			//	System.out.println("Pruning duplicate turn with last move: " + t.moves.getLast());
		//		System.out.println("It's equal to : " + segSet.get(resultingMoveSet));
				//remove this item. it's a duplicate
				i.remove();
				continue;
			}
			else {
				segSet.put(resultingMoveSet, t.moves.getLast().toString());
			}

		}
			
	}
		
		
		
		
		



}