import java.util.Collections;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class AI {

	// default depth
	public static int maxDepth = 7;
	int deepestNodeDepth = 0;

	AI(GameState gs) {
		this.gs = gs;
	}

	GameState gs;

	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();

	Turn iterativeDeepeningSearch(GameState.Player p, int startingDepth,
			int iterativeMaxDepth, long maxTimeMillis) {
		// Make the root turn
		Turn t1 = new Turn(this, p, null);
		t1.isRoot = true;
		LinkedList<Turn> list = t1.possibleTurnsForPlayer(p);
		TurnContainer bestTurnContainer = new TurnContainer();

		// Shuffle the list of possible turns so it appears that the AI is more
		// human like
		//Collections.shuffle(list);

		Turn bestTurn = null;

		if (list.size() == 0) {
			return null;
		}

		// Get current time
		long startingTime = System.currentTimeMillis();

		// Do an iteration, incrementing maxDepth
		do {
			System.out.println("incrementing... Depth is " + startingDepth);
			bestTurn = null;
			deepestNodeDepth = 0;
			int max = Integer.MIN_VALUE;
			for (Turn t : list) {
				int tmp = -alphabeta(t, GameState.otherPlayer(p),
						startingDepth - 1, Integer.MIN_VALUE,
						Integer.MAX_VALUE, bestTurnContainer);
				// Uncomment this line to do minimax instead of alpha beta
				// int tmp = -minimax(t, GameState.Player.P1, maxDepth-1);
				if (tmp > max) {
					bestTurn = t;
					max = tmp;
				}
			}
			startingDepth++;
		} while (System.currentTimeMillis() < (startingTime + maxTimeMillis)
				&& startingDepth <= iterativeMaxDepth);

		return bestTurn;

	}

	int stopProc = 0;
	
	// tells the AI to take a turn on gamestate
	void takeTurn(GameState.Player p) {

		TurnContainer bestTurnContainer = new TurnContainer();

		// System.out.println("fragBefore : " + fragBefore);
		Turn t1 = new Turn(this, p, null);
		t1.isRoot = true;
		LinkedList<Turn> list = t1.possibleTurnsForPlayer(p);

		// Shuffle the list of possible turns so it appears that the AI is more
		// human like
		Collections.shuffle(list);

		int max = Integer.MIN_VALUE;
		Turn bestTurn = null;
		if (list.size() == 0) {
			System.out.println("No moves!");
			return;
		}
		Vector<Integer> results = new Vector<Integer>();
		LinkedList<Turn> evaledTurns = new LinkedList<Turn>();

		
		for (Turn t : list) {
		
			
			int tmp = -alphabeta(t, GameState.otherPlayer(p), list.size() < 20 ? maxDepth+1 : maxDepth - 1,
					Integer.MIN_VALUE, Integer.MAX_VALUE, bestTurnContainer);
			results.add(tmp);
			evaledTurns.add(bestTurnContainer.t);
			// System.out.println("Turn :" + t + " is  : " + tmp);

			// Uncomment this line to do minimax instead of alpha beta
			// int tmp = -minimax(t, GameState.Player.P1, maxDepth-1);
			if (tmp > max) {
				bestTurn = bestTurnContainer.t;
				max = tmp;
			}
		}

		bestTurn = intuitiveBestTurn(evaledTurns, results, max, bestTurn);

		/*
		 * Turn bestTurn = iterativeDeepeningSearch(p, 6, Integer.MAX_VALUE,
		 * 500); System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		 */
		System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		System.out.println("Deepest node depth :" + deepestNodeDepth);
		while (bestTurn.parent != null) {
			bestTurn = bestTurn.parent;
		}
		for (Segment s : bestTurn.moves) {
			gs.doMove(s, p);
		}

	}

	// Stupid class used to pass around turn objects by reference
	class TurnContainer {
		public Turn t;
	}

	// Alpha beta search method. Like minimax, but more efficient
	int alphabeta(Turn t, GameState.Player p, int depth, int alpha, int beta,
			TurnContainer bestTurnContainer) {
		
		if (depth == 0) {
			bestTurnContainer.t = t;
			return t.eval(p);
		}

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if (children.size() == 0) {
			
			bestTurnContainer.t = t;
			return t.eval(p);
		}
		

		int tmp = 0;
		outer: for (Turn child : children) {
			tmp = -alphabeta(child, GameState.otherPlayer(p), depth - 1, -beta,
					-alpha, bestTurnContainer);
			alpha = Math.max(alpha, tmp);
			if (alpha >= beta) {
				// System.out.println("Pruning branch");
				// Prune branch
				break outer;
			}
		}
		return alpha;
	}

	// Minimax search method
	int minimax(Turn t, GameState.Player p, int depth) {
		if (depth <= 0) {
			return t.eval(p);
		}

		LinkedList<Turn> children = t.possibleTurnsForPlayer(p);
		if (children.size() == 0) {
			return t.eval(p);
		}
		int maxVal = Integer.MIN_VALUE;
		int tmp = 0;
		for (Turn child : children) {
			tmp = -minimax(child, GameState.otherPlayer(p), depth - 1);
			if (tmp > maxVal) {
				maxVal = tmp;
			}
		}
		return maxVal;
	}

	Turn intuitiveBestTurn(LinkedList<Turn> turnList, Vector<Integer> evals,
			int max, Turn bestTurnBefore) {
		int fragBefore = GameState.getNumFragments(gs);
		int unitsClaimedBefore = gs.getClaimedArea(GameState.Player.P1)
				+ gs.getClaimedArea(GameState.Player.P2);
		int i = 0;
		Turn bestTurn = bestTurnBefore;
		int maxFrag = Integer.MIN_VALUE;
		boolean fragOverride = false;
		int bestMoveCount = 0;

		
		
		
		if (false) {
			System.out.println("Going DEEP!");
			System.out.println("Total available moves are: " + turnList.size());
		}

		// Otherwise, decide based on fragmentation factor of best move
		else {
			for (Turn t : turnList) {
				while (t.parent != null) {
					t = t.parent;
				}
				if (max == evals.get(i++)) {

					gs.copyTo(scratchPad);
					Turn.applyTurnsToGameState(scratchPad, t);
					int fragAfter = GameState.getNumFragments(scratchPad);
					if (fragAfter - fragBefore > maxFrag
							&& fragAfter - fragBefore > 0) {
						maxFrag = fragAfter - fragBefore;
						bestTurn = t;
						System.out.println("Assigning based on Frag factor: "
								+ (fragAfter - fragBefore));
						fragOverride = true;
					}
				}
			}
			System.out.println("Duplicate best moves are: " + bestMoveCount);
		}
		return bestTurn;
	}

	
	
	
    final GameState reducePad = new GameState();
	void reducePossibleTurns(LinkedList<Turn> turnList) {
		if(turnList.size() == 0) return;
		
		//Init the hitmaps
		boolean[][] hitMapX = gs.hitMapX;
		boolean[][] hitMapY = gs.hitMapY;
		for(int i = 0; i < gs.dimY; i++) {
			for(int j = 0; j < gs.dimX-1; j++) {
				hitMapX[i][j] = false;
			}
		}
		for(int i = 0; i < gs.dimY-1; i++) {
			for(int j = 0; j < gs.dimX; j++) {
				hitMapY[i][j] = false;
			}
		}
		
		
		//Init the scratchpad
		gs.copyTo(reducePad);
		Turn lastTurn = turnList.get(0);
		if(lastTurn.parent != null) {
			lastTurn = lastTurn.parent;
		}
		Turn.applyTurnsToGameState(reducePad, lastTurn);
		
		
		boolean canReduce = true;
		Turn marker = null;
		while(true) {
			//Examine the first item of the list
			Turn t = turnList.poll();
			
			//If we've gone full through, break
			if(marker == t) {
				break;
			}
			//Try reduction
			if(reduceMove(t, reducePad, hitMapX, hitMapY)) {
				//add it back
				turnList.addLast(t);
				marker = t;
			}
			
			
			
		}
		
		
	}

	final GameState temp = new GameState();
	boolean reduceMove(Turn turn, GameState rPad, boolean hitMapX[][], boolean hitMapY[][]) {
		
	
		
		//Get the first move out of the turn
		Segment s = turn.moves.get(0);
		
		//Check if this is already hit. If it's not, mark it as so.
		if(s.isY) {
			if(hitMapY[s.y][s.x] == true) {
				return false;
			}
			else {
				hitMapY[s.y][s.x] = true;
			}
		}
		else {
			if(hitMapX[s.y][s.x] == true ) {
				return false;
     		}
			else {
				hitMapX[s.y][s.x] = true;
			}
		}
	
		//Copy off gamestate
		rPad.copyTo(temp);
		
		//Apply the move to the state. Will have to undo it later.
		for(Segment seg : turn.moves) {
			rPad.doMove(seg, turn.p);
		}
		
		
		//expand the hits
		LinkedList<Segment> segs = rPad.openSegments();

		
		//For each move in the turn
		while(segs.size() > 0) {
			Segment seg = segs.poll();
			if(GameState.segmentWouldClaimUnit(rPad, seg)) {
				if(seg.isY) {
					hitMapY[seg.y][seg.x] = true;
				}
				else {
					hitMapX[seg.y][seg.x] = true;
				}
				rPad.doMove(seg, GameState.otherPlayer(turn.p));
			}
			
		}
		
		//Restore
		temp.copyTo(rPad);
		
		return true;
	}
	
}
