import java.util.LinkedList;


public class AI {


	AI(GameState gs) {
		this.gs = gs;
	}
	GameState gs;
	
	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();
	
	class Turn {
		
		void applyTurnsToGameState(GameState state, Turn turns) {
			if(state == null || turns == null) return;
			
			LinkedList<Turn> turnList = new LinkedList<Turn>();
			Turn turn = turns;
		    while(turn != null) {
		    	turnList.addFirst(turn);
		    	turn = turn.parent;
		    }
		    
		    int cnt=0;
		    GameState.Player p;
		    for(Turn t: turnList) {
		    	 if(cnt++ % 2 == 0) { 
		    		 p = GameState.Player.P1;
		    	 }
		    	 else {
		    		 p = GameState.Player.P2;
		    	 }
		    	 for(Segment s: t.moves) {
		    		 state.doMove(s, p);
		    	 }
		    }
		}
		
		
		Turn parent;
		LinkedList<Segment> moves = new LinkedList<Segment>();
		
		public Turn() {
			
		}
		

		LinkedList<Turn> children() {
			
			LinkedList<Turn> ret = new LinkedList<Turn>();
			
			//Init the scratch pad
			gs.copyTo(scratchPad);
			
			
		    //Apply parent turns to get to the current state
			applyTurnsToGameState(scratchPad, parent);

			//Get open segments for this state
			LinkedList<Segment> openSegs = scratchPad.openSegments(); 
			
			LinkedList<Turn> Q = new LinkedList<Turn>();
			
			for(Segment s: openSegs) {
				if(segmentWouldClaimUnit(scratchPad, s)) {
					Turn subTurn = new Turn();
					subTurn.moves.add(s);
					Q.add(subTurn);
				}
				else {
					Turn subTurn = new Turn();
					subTurn.parent = this;
					subTurn.moves.add(s);
					ret.add(subTurn);
				}
			}
			
			while(Q.size() > 0) {
				Turn turn = Q.poll();
				
				//init the tertiary scratch pad
				scratchPad.copyTo(scratchPad2);
				applyTurnsToGameState(scratchPad2, turn);
				
				//Get open segs for this state
				openSegs = scratchPad2.openSegments();
				
				for(Segment s: openSegs) {
					turn.moves.add(s);
					if(segmentWouldClaimUnit(scratchPad2, s)) {
						Q.add(turn);
					}
					else {
						turn.parent = parent;
						ret.add(turn);
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
			
			
			
			return null;
			
		}
	}

	public boolean segmentWouldClaimUnit(GameState scratchPad2, Segment s) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
