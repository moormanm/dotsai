import java.util.Collections;
import java.util.LinkedList;
import java.util.Vector;

public class AI {

	// default depth
	public static int maxDepth = 3;

	AI(GameState gs) {
		this.gs = gs;
	}

	GameState gs;

	GameState scratchPad = new GameState();
	GameState scratchPad2 = new GameState();


	
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

		int c = 0;
		if( list.size() < GameState.dimX  && maxDepth == 3) {
			c = 8;
		}
		else if( list.size() < GameState.dimX + GameState.dimY && maxDepth == 3) {
			c = 4;
		}
		
		
		for (Turn t : list) {
		
			
			int tmp = -alphabeta(t, GameState.otherPlayer(p), maxDepth - 1 + c,
					Integer.MIN_VALUE, Integer.MAX_VALUE, bestTurnContainer);
			results.add(tmp);
			evaledTurns.add(bestTurnContainer.t);

			if (tmp > max) {
				bestTurn = bestTurnContainer.t;
				max = tmp;
			}
		}

		//If this is not easy mode, do post processing to further refine move
		if(maxDepth != 1) {
			bestTurn = intuitiveBestTurn(evaledTurns, results, max, bestTurn);
		}	
		
		
		System.out.println("Best Turn :" + bestTurn + " is  : " + max);
		System.out.println("C is : " + c);
		while (bestTurn.parent != null) {
			bestTurn = bestTurn.parent;
		}
		for (Segment s : bestTurn.moves) {
			gs.doMove(s, p);
		}

	}

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

	Turn intuitiveBestTurn(LinkedList<Turn> turnList, Vector<Integer> evals,
			int max, Turn bestTurnBefore) {
		int fragBefore = GameState.getNumFragments(gs);
		int i = 0;
		Turn bestTurn = bestTurnBefore;
		int maxFrag = Integer.MIN_VALUE;
		int bestMoveCount = 0;


			
		
		// discriminate best moves based on fragmentation factor
		for (Turn t : turnList) {
			while (t.parent != null) {
				t = t.parent;
			}
			if (max == evals.get(i++)) {
				bestMoveCount++;
				gs.copyTo(scratchPad);
				Turn.applyTurnsToGameState(scratchPad, t);
				int fragAfter = GameState.getNumFragments(scratchPad);
				if (fragAfter - fragBefore > maxFrag
						&& fragAfter - fragBefore > 0) {
					maxFrag = fragAfter - fragBefore;
					bestTurn = t;
					System.out.println("Assigning based on Frag factor: "
							+ (fragAfter - fragBefore));
				}
			}
		}
		
		System.out.println("Duplicate best moves are: " + bestMoveCount);
		return bestTurn;
	}

	
}
