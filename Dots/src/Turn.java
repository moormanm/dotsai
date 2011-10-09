import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
	static final GameState evalPad = new GameState();
	int eval(GameState.Player p) {
		int myUnits = this.ai.gs.getClaimedArea(p);
		int theirUnits = this.ai.gs.getClaimedArea(GameState.otherPlayer(p));
		this.ai.gs.copyTo(evalPad);
		applyTurnsToGameState(evalPad, this);
		
		
		myUnits = evalPad.getClaimedArea(p) - myUnits;
		theirUnits = evalPad.getClaimedArea(GameState.otherPlayer(p)) - theirUnits;
		Segment s = moves.getLast();
		
		
		//If this is the end game state, and this is winning, assing a HUGE value
		int winBonus = 0;
		if(evalPad.hasOpenSegments() == false) {
			if(evalPad.getClaimedArea(p) > evalPad.getClaimedArea(GameState.otherPlayer(p))) {
				winBonus = Integer.MAX_VALUE / 2;
				System.out.println("Win bonus!!");
			}
			else {
				//losing, make it really small
				winBonus = -1 * Integer.MAX_VALUE / 2;
				System.out.println("Lose bonus!!");
			}
			
		}
		
	
		int cnt = 0;
		int tmp = 0;
		if(s.isY) {
			tmp = evalPad.numSegmentsForPoint(s.x, s.y);
			if(tmp > 0) cnt += tmp - 1;
			tmp = evalPad.numSegmentsForPoint(s.x, s.y+1);
			if(tmp > 0) cnt += tmp - 1;
		}
		else {
			tmp = evalPad.numSegmentsForPoint(s.x, s.y);
			if(tmp > 0) cnt += tmp - 1;
			tmp = evalPad.numSegmentsForPoint(s.x+1, s.y);
			if(tmp > 0) cnt += tmp - 1;
		}
		

		assert(cnt <= 6 && cnt >= 0);
		
		int retVal =  (myUnits - theirUnits) * 7 + winBonus;
		//System.out.println("ret is " + retVal + " for segment " + s);
		return retVal;
		
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
	}
	
	
	Turn parent;
	LinkedList<Segment> moves = new LinkedList<Segment>();
	private Turn() {
		ai = null;
	}
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

	final static GameState tmpState = new GameState();
	LinkedList<Turn> possibleTurns(GameState.Player p) {
		
		LinkedList<Turn> ret = new LinkedList<Turn>();
		
		//Init the scratch pad
		this.ai.gs.copyTo(this.ai.scratchPad);
		
		
	    //Apply parent turns to get to the current state
		applyTurnsToGameState(this.ai.scratchPad, this);
		
		

		//Get open segments for this state
		LinkedList<Segment> openSegs = this.ai.scratchPad.openSegments(); 
		

		//Loop through, add mandatory states
		for(Segment s: openSegs) {
			
			
			if(this.ai.segmentWouldClaimUnit(this.ai.scratchPad, s)) {
				//Create a new turn
				Turn subTurn = new Turn(this.ai, p, this );
				subTurn.moves.add(s);
				
				//init a temporary state
				ai.scratchPad.copyTo(tmpState);
				
				//do the first move
				tmpState.doMove(s,p);
				
				boolean keepGoing = true;
				while(keepGoing) {
					keepGoing = false;
					//Keep doing chain moves
					LinkedList<Segment> segs = tmpState.openSegments();
					for(Segment s2: segs) {
		              if(ai.segmentWouldClaimUnit(tmpState, s2)) {
		            	  keepGoing=true;
		            	  tmpState.doMove(s2, p);
		            	  subTurn.moves.add(s2);
		              }
					}
				}
				
				//Add new turns for the last move
				LinkedList<Segment> segs = tmpState.openSegments();
				//No more moves case
				if(segs.size() == 0) {
					ret.add(subTurn);
				}
				
				for(Segment lastMove : segs) {
					//Create a new move
					Turn nt = new Turn(ai, p, this);
					
					//Take a copy of the moves from subTurn
					subTurn.copyMovesTo(nt);
					
					//Add the last move
					nt.moves.add(lastMove);
					
					ret.add(nt);
					
					
				}
        		
        		
			}
		}
		
		//Pick up basic moves if no jump moves were found
		//Get open segments for this state
		if(ret.size() == 0) {
		  openSegs = this.ai.scratchPad.openSegments(); 

  		  //Loop through, add mandatory states
		  for(Segment s: openSegs) {
			  Turn basicTurn = new Turn(ai, p, this);
			  basicTurn.moves.add(s);
			  ret.add(basicTurn);
		  }
		}
		return ret;
		
	}
	
	//Reliably sorts segment lists
	class SegmentSort implements Comparator<Segment> {
		@Override
		public int compare(Segment a, Segment b) {
			String aStr = a.encode();
			String bStr = b.encode();
			return aStr.compareTo(bStr);
		}
	} 
	
	static final Turn instance = new Turn();
	static final SegmentSort segCmp = instance.new SegmentSort();
	static final LinkedList<Segment> tmpMoveList = new LinkedList<Segment>(); 
	public String encode() {
		String ret = new String();
		//Make a copy of the move list
		tmpMoveList.clear();
		for(Segment s: moves) {
			tmpMoveList.add(s);
		}
		Collections.sort(tmpMoveList, segCmp);
		
		for(Segment s: tmpMoveList) {
			ret += s.encode();
		}
		
		return ret;
	}
}